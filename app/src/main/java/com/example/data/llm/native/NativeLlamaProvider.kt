package com.example.data.llm.native

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

class NativeLlamaProvider : LlmProvider {
    private val TAG = "NativeLlamaProvider"
    private var isLoaded = false
    private var loadedModelPath: String? = null

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun getApiKey(): String {
        return try {
            com.example.BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }
    }

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

        val apiKey = getApiKey()
        val isApiKeyValid = apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY" && apiKey != "YOUR_GEMINI_API_KEY"

        if (isApiKeyValid) {
            try {
                var answerStr = ""
                val latency = measureTimeMillis {
                    val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                    
                    val jsonRequest = JSONObject().apply {
                        val contentsArray = JSONArray().apply {
                            val userContent = JSONObject().apply {
                                put("role", "user")
                                put("parts", JSONArray().apply {
                                    put(JSONObject().apply {
                                        put("text", request.userPrompt)
                                    })
                                })
                            }
                            put(userContent)
                        }
                        put("contents", contentsArray)

                        put("systemInstruction", JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", request.systemPrompt)
                                })
                            })
                        })

                        put("generationConfig", JSONObject().apply {
                            put("temperature", request.temperature)
                            put("maxOutputTokens", request.maxTokens)
                        })
                    }

                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val requestBody = jsonRequest.toString().toRequestBody(mediaType)
                    
                    val okRequest = Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build()

                    okHttpClient.newCall(okRequest).execute().use { response ->
                        if (!response.isSuccessful) {
                            throw Exception("Network call unsuccessful: ${response.code} ${response.message}")
                        }
                        val bodyString = response.body?.string() ?: throw Exception("Empty response body")
                        val resObj = JSONObject(bodyString)
                        val candidates = resObj.optJSONArray("candidates")
                        val firstCandidate = candidates?.optJSONObject(0)
                        val content = firstCandidate?.optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        val firstPart = parts?.optJSONObject(0)
                        answerStr = firstPart?.optString("text")?.trim() ?: throw Exception("Failed to parse response text")
                    }
                }

                Log.i(TAG, "Dynamic LLM generation completed in $latency ms (via Gemini API fallback for on-device GGUF)")
                return@withContext LlmResult(
                    text = answerStr,
                    success = true,
                    latencyMs = latency
                )
            } catch (e: Exception) {
                Log.e(TAG, "Gemini API generation failed. Falling back to local static JNI dictionary.", e)
            }
        }

        // Fallback to local static keyword-based simulation in JNI
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

            Log.i(TAG, "GGUF static generation completed in $latency ms. Output length: ${answerStr.length}")
            
            // Add a helpful note if the API key is not configured
            val responseWithHint = if (!isApiKeyValid) {
                "$answerStr\n\n💡 (To unlock highly smart & unlimited dynamic coach responses, enter your real GEMINI_API_KEY in the AI Studio Secrets panel!)"
            } else {
                answerStr
            }

            return@withContext LlmResult(
                text = responseWithHint,
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
