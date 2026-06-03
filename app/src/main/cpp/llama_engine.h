#ifndef LLAMA_ENGINE_H
#define LLAMA_ENGINE_H

#include <string>

struct llama_model;
struct llama_context;

// Header declarations representing local native LLM engine structures.
class LlamaEngine {
private:
    struct llama_model* model = nullptr;
    struct llama_context* ctx = nullptr;

public:
    LlamaEngine();
    ~LlamaEngine();

    bool initialize(const std::string& modelPath, int contextSize, int threads, int batchSize);
    std::string generateText(const std::string& prompt, int maxTokens, float temperature);
    void shutdown();
};

#endif // LLAMA_ENGINE_H
