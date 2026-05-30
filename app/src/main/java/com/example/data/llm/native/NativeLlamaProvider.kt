package com.example.data.llm.native

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

class NativeLlamaProvider : LlmProvider {
    private val TAG = "NativeLlamaProvider"
    private var isLoaded = false
    private var loadedModelPath: String? = null

    override suspend fun isModelLoaded(): Boolean = withContext(Dispatchers.IO) {
        return@withContext isLoaded && NativeLlamaEngine.isNativeLibraryLoaded()
    }

    override suspend fun loadModel(modelPath: String, config: LlmConfig): LlmResult = withContext(Dispatchers.IO) {
        if (!NativeLlamaEngine.isNativeLibraryLoaded()) {
            return@withContext LlmResult(
                text = "",
                success = false,
                latencyMs = 0,
                errorMessage = "Native JNI engine library ('pratidinam_llama') is not loaded. Try smaller GGUF or rebuild project."
            )
        }

        try {
            val success: Boolean
            val latency = measureTimeMillis {
                success = NativeLlamaEngine.loadModel(
                    modelPath = modelPath,
                    contextSize = config.contextSize,
                    threads = config.threads,
                    batchSize = config.batchSize
                )
            }

            if (success) {
                isLoaded = true
                loadedModelPath = modelPath
                Log.i(TAG, "GGUF Model loaded successfully in $latency ms from: $modelPath")
                return@withContext LlmResult(
                    text = "Successfully loaded GGUF model.",
                    success = true,
                    latencyMs = latency
                )
            } else {
                isLoaded = false
                loadedModelPath = null
                return@withContext LlmResult(
                    text = "",
                    success = false,
                    latencyMs = latency,
                    errorMessage = "Failed to parse or map GGUF file header. The file might be corrupted or not valid GGUF."
                )
            }
        } catch (e: Throwable) {
            isLoaded = false
            loadedModelPath = null
            Log.e(TAG, "Crash while loading GGUF model natively", e)
            return@withContext LlmResult(
                text = "",
                success = false,
                latencyMs = 0,
                errorMessage = "Error loading GGUF: ${e.localizedMessage}"
            )
        }
    }

    override suspend fun generate(request: LlmRequest): LlmResult = withContext(Dispatchers.IO) {
        if (!isLoaded || loadedModelPath == null) {
            return@withContext LlmResult(
                text = "",
                success = false,
                latencyMs = 0,
                errorMessage = "Local GGUF model is not loaded. Please select and load a valid GGUF model in Settings."
            )
        }

        if (!NativeLlamaEngine.isNativeLibraryLoaded()) {
            return@withContext LlmResult(
                text = "",
                success = false,
                latencyMs = 0,
                errorMessage = "Native JNI library is missing."
            )
        }

        try {
            var answerStr = ""
            // Format prompt with Qwen2.5 system/user structure
            val formattedPrompt = """
                <|im_start|>system
                ${request.systemPrompt}<|im_end|>
                <|im_start|>user
                ${request.userPrompt}<|im_end|>
                <|im_start|>assistant
            """.trimIndent()

            val latency = measureTimeMillis {
                answerStr = NativeLlamaEngine.generate(
                    prompt = formattedPrompt,
                    maxTokens = request.maxTokens,
                    temperature = request.temperature,
                    topK = 40,
                    topP = 0.9f
                )
            }

            Log.i(TAG, "GGUF generation completed in $latency ms. Output length: ${answerStr.length}")
            return@withContext LlmResult(
                text = answerStr,
                success = true,
                latencyMs = latency
            )
        } catch (e: Throwable) {
            Log.e(TAG, "Error executing local native GGUF generation", e)
            return@withContext LlmResult(
                text = "",
                success = false,
                latencyMs = 0,
                errorMessage = "Local GGUF execution failure: ${e.localizedMessage}"
            )
        }
    }

    override suspend fun unloadModel(): Unit {
        withContext(Dispatchers.IO) {
            if (NativeLlamaEngine.isNativeLibraryLoaded()) {
                try {
                    NativeLlamaEngine.unloadModel()
                } catch (e: Throwable) {
                    Log.e(TAG, "Error seeking memory release on native model unload", e)
                }
            }
            isLoaded = false
            loadedModelPath = null
            Log.i(TAG, "GGUF Model unloaded successfully.")
        }
    }
}
