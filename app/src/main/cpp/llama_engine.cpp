#include "llama_engine.h"
#include <android/log.h>
#include <vector>
#include <string>
#include <algorithm>
#include <cstdlib>
#include <ctime>
#include "llama.h"

#define LOG_TAG "PratidinamLlamaEngine"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

LlamaEngine::LlamaEngine() {
    LOGD("LlamaEngine instance constructed");
}

LlamaEngine::~LlamaEngine() {
    shutdown();
}

bool LlamaEngine::initialize(const std::string& modelPath, int contextSize, int threads, int batchSize) {
    shutdown();

    LOGI("LlamaEngine: Initializing llama.cpp backend and loading GGUF model");
    double start_time = clock();

    // Initialize llama.cpp backend (safe to be called multiple times)
    llama_backend_init();

    // Load model from file path
    llama_model_params model_params = llama_model_default_params();
    model = llama_model_load_from_file(modelPath.c_str(), model_params);
    if (!model) {
        LOGE("LlamaEngine: Failed to load model file from path: %s", modelPath.c_str());
        return false;
    }

    // Set context properties
    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx = contextSize;
    ctx_params.n_batch = batchSize;
    ctx_params.n_threads = threads;
    ctx_params.n_threads_batch = threads;

    ctx = llama_init_from_model(model, ctx_params);
    if (!ctx) {
        LOGE("LlamaEngine: Failed to create context for model path: %s", modelPath.c_str());
        llama_model_free(model);
        model = nullptr;
        return false;
    }

    double load_duration = (double)(clock() - start_time) / CLOCKS_PER_SEC;
    LOGI("LlamaEngine: GGUF Model and context loaded successfully in %.2f seconds from path: %s", load_duration, modelPath.c_str());
    return true;
}

std::string LlamaEngine::generateText(const std::string& prompt, int maxTokens, float temperature) {
    if (!model || !ctx) {
        LOGE("LlamaEngine: Model/context not loaded, cannot generate text");
        return "Error: On-device GGUF model or context is not loaded.";
    }

    const struct llama_vocab* vocab = llama_model_get_vocab(model);
    if (!vocab) {
        LOGE("LlamaEngine: Failed to get vocabulary from model");
        return "Error: Model vocabulary is unavailable.";
    }

    LOGI("LlamaEngine: Starting native text inference...");
    double start_time = clock();

    // Reset sequence memory cache (deleting sequences to start cleanly)
    llama_memory_t mem = llama_get_memory(ctx);
    if (mem) {
        llama_memory_seq_rm(mem, -1, -1, -1);
    }

    // Tokenize prompt
    std::vector<llama_token> tokens_list(prompt.length() + 8);
    int n_tokens = llama_tokenize(vocab, prompt.c_str(), prompt.length(), tokens_list.data(), tokens_list.size(), true, true);
    if (n_tokens < 0) {
        LOGI("LlamaEngine: Retrying tokenization with larger buffer size: %d", -n_tokens);
        tokens_list.resize(-n_tokens);
        n_tokens = llama_tokenize(vocab, prompt.c_str(), prompt.length(), tokens_list.data(), tokens_list.size(), true, true);
    }
    if (n_tokens < 0) {
        LOGE("LlamaEngine: Tokenization failed!");
        return "Error: Tokenization failed.";
    }
    tokens_list.resize(n_tokens);
    LOGI("LlamaEngine: Tokenized prompt into %d tokens", n_tokens);

    // Prepare llama batch structure for prompt evaluation
    int batch_size = 512;
    llama_batch batch = llama_batch_init(batch_size, 0, 1);

    auto batch_add = [](llama_batch& b, llama_token id, llama_pos pos, const std::vector<llama_seq_id>& seq_ids, bool logits) {
        b.token[b.n_tokens] = id;
        b.pos[b.n_tokens] = pos;
        b.n_seq_id[b.n_tokens] = seq_ids.size();
        for (size_t i = 0; i < seq_ids.size(); ++i) {
            b.seq_id[b.n_tokens][i] = seq_ids[i];
        }
        b.logits[b.n_tokens] = logits;
        b.n_tokens++;
    };

    int n_past = 0;
    // Chunk prompt tokens into evaluation batches
    for (size_t i = 0; i < tokens_list.size(); i += batch.n_tokens) {
        batch.n_tokens = 0;
        for (int j = 0; j < batch_size && i + j < tokens_list.size(); ++j) {
            bool is_last_token = (i + j == tokens_list.size() - 1);
            batch_add(batch, tokens_list[i + j], n_past + j, { 0 }, is_last_token);
        }
        n_past += batch.n_tokens;

        if (llama_decode(ctx, batch) != 0) {
            LOGE("LlamaEngine: llama_decode failed on prompt evaluation");
            llama_batch_free(batch);
            return "Error: Prompt evaluation failed.";
        }
    }

    // Initialize unified llama_sampler chain for token generation
    llama_sampler* smpl = llama_sampler_chain_init(llama_sampler_chain_default_params());
    llama_sampler_chain_add(smpl, llama_sampler_init_top_k(40));
    llama_sampler_chain_add(smpl, llama_sampler_init_top_p(0.9f, 1));
    llama_sampler_chain_add(smpl, llama_sampler_init_temp(temperature));
    llama_sampler_chain_add(smpl, llama_sampler_init_dist(time(nullptr)));

    // Generate loop
    std::string response = "";
    int tokens_generated = 0;
    llama_token curr_token = llama_sampler_sample(smpl, ctx, -1);
    llama_token eos_token = llama_vocab_eos(vocab);

    auto get_token_piece = [vocab](llama_token token) -> std::string {
        std::vector<char> result(128);
        int n_chars = llama_token_to_piece(vocab, token, result.data(), result.size(), 0, false);
        if (n_chars < 0) {
            result.resize(-n_chars);
            n_chars = llama_token_to_piece(vocab, token, result.data(), result.size(), 0, false);
        }
        if (n_chars > 0) {
            return std::string(result.data(), n_chars);
        }
        return "";
    };

    while (tokens_generated < maxTokens) {
        if (curr_token == eos_token) {
            LOGD("LlamaEngine: Generated EOS token");
            break;
        }

        std::string piece = get_token_piece(curr_token);
        // Break early if we hit Qwen ChatML end of turn tags/tokens
        if (piece == "<|im_end|>" || piece == "<|im_start|>") {
            LOGD("LlamaEngine: Generated ChatML turn-stop tag: %s", piece.c_str());
            break;
        }

        response += piece;
        tokens_generated++;

        // Add the newly sampled token to a single token batch for the next decode step
        batch.n_tokens = 0;
        batch_add(batch, curr_token, n_past, { 0 }, true);
        n_past++;

        if (llama_decode(ctx, batch) != 0) {
            LOGE("LlamaEngine: llama_decode failed during sequence generation");
            break;
        }

        curr_token = llama_sampler_sample(smpl, ctx, -1);
    }

    // Cleanup resources
    llama_batch_free(batch);
    llama_sampler_free(smpl);

    double duration = (double)(clock() - start_time) / CLOCKS_PER_SEC;
    double ms_per_token = tokens_generated > 0 ? (duration * 1000.0) / tokens_generated : 0.0;
    LOGI("LlamaEngine: On-device generation complete. Generated %d tokens in %.2f seconds (%.2f ms/token)",
         tokens_generated, duration, ms_per_token);

    return response;
}

void LlamaEngine::shutdown() {
    if (ctx) {
        LOGI("LlamaEngine: Freeing llama context");
        llama_free(ctx);
        ctx = nullptr;
    }
    if (model) {
        LOGI("LlamaEngine: Freeing llama model");
        llama_model_free(model);
        model = nullptr;
    }
}
