package com.example.data.llm.native

import android.util.Log

object NativeLlamaEngine {
    private const val TAG = "NativeLlamaEngine"
    private var isLibLoaded = false

    init {
        try {
            System.loadLibrary("pratidinam_llama")
            isLibLoaded = true
            Log.i(TAG, "Native library 'pratidinam_llama' loaded successfully!")
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "Failed to load native library 'pratidinam_llama': ${e.message}")
            isLibLoaded = false
        }
    }

    fun isNativeLibraryLoaded(): Boolean = isLibLoaded

    external fun loadModel(
        modelPath: String,
        contextSize: Int,
        threads: Int,
        batchSize: Int
    ): Boolean

    external fun generate(
        prompt: String,
        maxTokens: Int,
        temperature: Float,
        topK: Int,
        topP: Float
    ): String

    external fun unloadModel()
}
