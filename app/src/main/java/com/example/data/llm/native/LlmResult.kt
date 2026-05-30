package com.example.data.llm.native

data class LlmResult(
    val text: String,
    val success: Boolean,
    val latencyMs: Long,
    val errorMessage: String? = null
)
