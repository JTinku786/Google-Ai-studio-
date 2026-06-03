#include <jni.h>
#include <string>
#include <android/log.h>
#include <cstdio>
#include "llama_engine.h"

#define LOG_TAG "PratidinamNativeLlama"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

static std::string g_modelPath = "";
static bool g_isLoaded = false;
static int g_contextSize = 512;
static int g_threads = 4;
static int g_batchSize = 128;
static LlamaEngine* g_engine = nullptr;

extern "C" JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM* vm, void* reserved) {
    LOGI("Pratidinam JNI load check - Native library loaded into Android runtime successfully");
    return JNI_VERSION_1_6;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_data_llm_native_NativeLlamaEngine_loadModel(
    JNIEnv* env,
    jobject /* this */,
    jstring modelPath,
    jint contextSize,
    jint threads,
    jint batchSize
) {
    if (modelPath == nullptr) {
        LOGE("loadModel: Failed because model path parameter is null");
        return JNI_FALSE;
    }

    const char* nativePath = env->GetStringUTFChars(modelPath, nullptr);
    if (nativePath == nullptr) {
        LOGE("loadModel: Failed to resolve model path string inside JNI");
        return JNI_FALSE;
    }

    LOGI("loadModel: Attempting to open model file directly. Path: %s", nativePath);

    // Verify file actually can be opened to prevent crash on nonexistent file path
    FILE* f = fopen(nativePath, "rb");
    if (!f) {
        LOGE("loadModel error: Failed to open GGUF file: %s (Verify file exists and app possesses correct directory permissions)", nativePath);
        env->ReleaseStringUTFChars(modelPath, nativePath);
        g_isLoaded = false;
        return JNI_FALSE;
    }
    
    // Read first 4 bytes to check if it's GGUF header
    char header[4];
    size_t read_bytes = fread(header, 1, 4, f);
    fclose(f);

    LOGI("loadModel: Model file checked. Reading header tags...");

    if (read_bytes < 4 || header[0] != 'G' || header[1] != 'G' || header[2] != 'U' || header[3] != 'F') {
        LOGE("loadModel error: Invalid GGUF header parsed! Selected file is not a valid GGUF file format.");
        env->ReleaseStringUTFChars(modelPath, nativePath);
        g_isLoaded = false;
        return JNI_FALSE;
    }

    g_modelPath = nativePath;
    g_contextSize = contextSize;
    g_threads = threads;
    g_batchSize = batchSize;

    if (g_engine == nullptr) {
        g_engine = new LlamaEngine();
    }

    // Call actual llama.cpp core loader and context initialization
    bool init_success = g_engine->initialize(g_modelPath, g_contextSize, g_threads, g_batchSize);
    g_isLoaded = init_success;

    if (init_success) {
        LOGI("loadModel success: GGUF Model loader and context initialized successfully in memory for: %s", nativePath);
    } else {
        LOGE("loadModel error: llama.cpp model loading or context creation crashed/failed for: %s", nativePath);
    }

    env->ReleaseStringUTFChars(modelPath, nativePath);
    return init_success ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_data_llm_native_NativeLlamaEngine_generate(
    JNIEnv* env,
    jobject /* this */,
    jstring prompt,
    jint maxTokens,
    jfloat temperature,
    jint topK,
    jfloat topP
) {
    if (!g_isLoaded) {
        LOGE("generate failed: No local GGUF model is loaded in memory!");
        return env->NewStringUTF("Error: No local GGUF model is loaded.");
    }

    if (prompt == nullptr) {
        return env->NewStringUTF("");
    }

    const char* nativePrompt = env->GetStringUTFChars(prompt, nullptr);
    if (nativePrompt == nullptr) {
        return env->NewStringUTF("Error: JNI prompt resolution failure.");
    }
    std::string promptStr(nativePrompt);
    env->ReleaseStringUTFChars(prompt, nativePrompt);

    LOGI("generate: Triggering real on-device token inference execution.");
    LOGD("generate: Full prompt content passed to JNI: %s", promptStr.c_str());

    if (g_engine == nullptr) {
        LOGE("generate failed: g_engine is null but g_isLoaded indicator is true!");
        return env->NewStringUTF("Error: Engine is not initialized.");
    }

    std::string response = g_engine->generateText(promptStr, maxTokens, temperature);

    return env->NewStringUTF(response.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_data_llm_native_NativeLlamaEngine_unloadModel(
    JNIEnv* /* env */,
    jobject /* this */
) {
    LOGI("unloadModel: Releasing model and freeing llama.cpp context memory.");
    g_modelPath = "";
    g_isLoaded = false;
    if (g_engine != nullptr) {
        g_engine->shutdown();
        delete g_engine;
        g_engine = nullptr;
    }
}
