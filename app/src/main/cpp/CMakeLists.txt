cmake_minimum_required(VERSION 3.22.1)

project("pratidinam_llama")

# Create shared library for native GGUF JNI bridging.
# Note: For custom production llama.cpp compilation,
# the developer can bundle/compile the llama.cpp and ggml.c sources directly
# or compile them into static libraries and link them using:
# add_subdirectory(llama.cpp)
# target_link_libraries(pratidinam_llama PRIVATE llama ggml)
add_library(
    pratidinam_llama
    SHARED
    llama_jni.cpp
    llama_engine.cpp
)

# Find and link log library
find_library(
    log-lib
    log
)

target_link_libraries(
    pratidinam_llama
    ${log-lib}
)
