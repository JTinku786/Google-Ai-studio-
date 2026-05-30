package com.example.data.llm.native

interface LlmProvider {
    suspend fun isModelLoaded(): Boolean
    suspend fun loadModel(modelPath: String, config: LlmConfig): LlmResult
    suspend fun generate(request: LlmRequest): LlmResult
    suspend fun unloadModel()
}

data class LlmConfig(
    val contextSize: Int = 512,
    val maxTokens: Int = 120,
    val temperature: Float = 0.3f,
    val topK: Int = 40,
    val topP: Float = 0.9f,
    val threads: Int = 4,
    val batchSize: Int = 128
)
