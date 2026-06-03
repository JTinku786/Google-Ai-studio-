cmake_minimum_required(VERSION 3.22.1)

project("pratidinam_llama" C CXX)

set(CMAKE_C_STANDARD 11)
set(CMAKE_CXX_STANDARD 17)

# Disable llama.cpp examples, tests, and server to minimize compilation time
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "build examples" FORCE)
set(LLAMA_BUILD_TESTS OFF CACHE BOOL "build tests" FORCE)
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "build server" FORCE)

# Fetch llama.cpp source code using FetchContent
include(FetchContent)

FetchContent_Declare(
    llamacpp
    URL             https://github.com/ggml-org/llama.cpp/archive/b3000.tar.gz
    SOURCE_DIR      ${CMAKE_CURRENT_SOURCE_DIR}/llama.cpp
)

FetchContent_MakeAvailable(llamacpp)

# Create our shared JNI wrapper library
add_library(
    pratidinam_llama
    SHARED
    llama_jni.cpp
    llama_engine.cpp
)

# Include directories for JNI and llama.cpp headers
target_include_directories(
    pratidinam_llama
    PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${CMAKE_CURRENT_SOURCE_DIR}/llama.cpp/include
    ${CMAKE_CURRENT_SOURCE_DIR}/llama.cpp/src
)

# Find the Android log library
find_library(
    log-lib
    log
)

# Link llama.cpp, ggml, and log libraries
target_link_libraries(
    pratidinam_llama
    PRIVATE
    llama
    ggml
    ${log-lib}
)

