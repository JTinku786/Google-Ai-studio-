package com.example.data.llm.native

data class LlmRequest(
    val systemPrompt: String,
    val userPrompt: String,
    val maxTokens: Int = 120,
    val temperature: Float = 0.3f
)
