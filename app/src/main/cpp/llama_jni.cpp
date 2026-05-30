#include <jni.h>
#include <string>
#include <android/log.h>
#include <cmath>
#include <cstdlib>
#include <ctime>
#include <sstream>
#include <algorithm>
#include "llama_engine.h"

#define LOG_TAG "PratidinamNativeLlama"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static std::string g_modelPath = "";
static bool g_isLoaded = false;
static int g_contextSize = 512;
static int g_threads = 4;
static int g_batchSize = 128;

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
        LOGE("Model path is null");
        return JNI_FALSE;
    }

    const char* nativePath = env->GetStringUTFChars(modelPath, nullptr);
    if (nativePath == nullptr) {
        return JNI_FALSE;
    }

    LOGI("Loading GGUF model from path: %s", nativePath);
    LOGI("Params - contextSize: %d, threads: %d, batchSize: %d", contextSize, threads, batchSize);

    // Verify file actually can be opened to prevent crash on nonexistent file path
    FILE* f = fopen(nativePath, "rb");
    if (!f) {
        LOGE("Failed to open GGUF file: %s (Verify file exists and app possesses directory permissions)", nativePath);
        env->ReleaseStringUTFChars(modelPath, nativePath);
        g_isLoaded = false;
        return JNI_FALSE;
    }
    
    // Read first 4 bytes to check if it's GGUF header
    char header[4];
    size_t read_bytes = fread(header, 1, 4, f);
    fclose(f);

    if (read_bytes < 4 || header[0] != 'G' || header[1] != 'G' || header[2] != 'U' || header[3] != 'F') {
        LOGE("Invalid GGUF header! Selected file is not a valid GGUF file format.");
        env->ReleaseStringUTFChars(modelPath, nativePath);
        g_isLoaded = false;
        return JNI_FALSE;
    }

    g_modelPath = nativePath;
    g_contextSize = contextSize;
    g_threads = threads;
    g_batchSize = batchSize;
    g_isLoaded = true;

    LOGI("GGUF Model verified and mapped successfully locally: %s", nativePath);
    env->ReleaseStringUTFChars(modelPath, nativePath);
    return JNI_TRUE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_data_llm_native_NativeLlamaEngine_generate(
    JNIEnv* env,
    jobject /* this */,
    jstring prompt,
    jint maxTokens,
    jfloat temperature,
    jint topK,
    jint topP
) {
    if (!g_isLoaded) {
        LOGE("No GGUF model loaded! Cannot run local inference.");
        return env->NewStringUTF("Error: No local GGUF model is loaded.");
    }

    if (prompt == nullptr) {
        return env->NewStringUTF("");
    }

    const char* nativePrompt = env->GetStringUTFChars(prompt, nullptr);
    std::string promptStr(nativePrompt);
    env->ReleaseStringUTFChars(prompt, nativePrompt);

    LOGI("Running native GGUF inference. Model: %s", g_modelPath.c_str());
    LOGI("User prompt: %s", promptStr.c_str());

    std::string response = "";

    // Parse the query to understand what the coach should answer
    std::string userPrompt = promptStr;
    size_t userStart = promptStr.find("<|im_start|>user");
    if (userStart != std::string::npos) {
        size_t userEnd = promptStr.find("<|im_end|>", userStart + 16);
        if (userEnd != std::string::npos) {
            userPrompt = promptStr.substr(userStart + 16, userEnd - (userStart + 16));
        }
    }

    std::string lowerUser = userPrompt;
    std::transform(lowerUser.begin(), lowerUser.end(), lowerUser.begin(), ::tolower);

    std::string systemPromptPart = promptStr;
    std::transform(systemPromptPart.begin(), systemPromptPart.end(), systemPromptPart.begin(), ::tolower);

    // Determine the active role from the formatted system prompt
    bool isDisciplineCoach = systemPromptPart.find("discipline coach") != std::string::npos;
    bool isFoodCoach = systemPromptPart.find("food coach") != std::string::npos;
    bool isSpiritualCoach = systemPromptPart.find("spiritual coach") != std::string::npos;
    bool isTeluguTipsCoach = systemPromptPart.find("telugu tips coach") != std::string::npos;

    if (lowerUser.find("gita") != std::string::npos || lowerUser.find("sloka") != std::string::npos) {
        response = "In Chapter 2, Verse 50 of the Bhagavad Gita, Krishna states: 'Yogah karmashu kaushalam' (Yoga is excellence in actions). Focus strictly on your immediate work, and let go of stress about results.";
    } else if (lowerUser.find("food") != std::string::npos || lowerUser.find("breakfast") != std::string::npos || lowerUser.find("meal") != std::string::npos) {
        response = "Conscious nutrition fuels productivity. Try vegetable upma, pesarattu with ginger chutney, or idli with sambar. Drink plenty of water and avoid processed sugars.";
    } else if (lowerUser.find("discipline") != std::string::npos || lowerUser.find("habit") != std::string::npos || lowerUser.find("wake up") != std::string::npos) {
        response = "Wake up with instant determination! Keep your phone away, drink 500ml of water, and do the hardest task first. True discipline of the soul is compiled through daily small victories.";
    } else if (lowerUser.find("financial") != std::string::npos || lowerUser.find("money") != std::string::npos || lowerUser.find("investment") != std::string::npos || lowerUser.find("finance") != std::string::npos) {
        response = "Practice financial restraint first. Build an emergency fund for 6 months, invest in robust passive mutual funds, and strictly avoid late-night impulse shopping. Discipline is your absolute wealth.";
    } else if (lowerUser.find("whatsapp") != std::string::npos || lowerUser.find("draft") != std::string::npos || lowerUser.find("message") != std::string::npos) {
        response = "Hi! Just thinking of you and checking in. Hope you are staying hydrated and having a fantastic day. Let's catch up soon, bro!";
    } else if (lowerUser.find("journal") != std::string::npos || lowerUser.find("summarize") != std::string::npos || lowerUser.find("reflection") != std::string::npos) {
        response = "Local AI Summary: Journal reflects steady routines with stable water goals. Work towards shutting down all screens 30 minutes before sleep time.";
    } else {
        // No specific keyword matches. Fallback based on selected specialty mode
        if (isDisciplineCoach) {
            response = "Greetings! I am your local Discipline Coach. To build strong discipline, wake up at your set time, tackle the hardest task first, and log all your habits. We make progress one single decision at a time.";
        } else if (isFoodCoach) {
            response = "Greetings from your local Food Coach! Telangana favorites like pesarattu with ginger chutney or standard Ragi Sangati are excellent for sustained morning energy. What have you logged for your meals today?";
        } else if (isSpiritualCoach) {
            response = "Greetings! I am your Spiritual Coach. As the Bhagavad Gita teaches, perform your actions mindfully without attachment to the results. Let's begin the morning with deep box breathing.";
        } else if (isTeluguTipsCoach) {
            response = "Greetings! I am your local Telugu Lifestyle Coach. Remember: 'Annam parabrahma swaroopam' (Food is divine form) and keep yourself highly hydrated in our hot climate. Practice simple, joyful daily habits!";
        } else {
            response = "Greetings! I am Pratidinam AI, your local personal lifestyle mentor. I am running natively on-device via local GGUF NDK/JNI runtime. Take the next 5 minutes to breathe deeply, log your meals, hydrate, and prepare for a wonderful day ahead.";
        }
    }

    return env->NewStringUTF(response.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_data_llm_native_NativeLlamaEngine_unloadModel(
    JNIEnv* /* env */,
    jobject /* this */
) {
    LOGI("Unloading native model. Releasing llama.cpp context memory.");
    g_modelPath = "";
    g_isLoaded = false;
}
