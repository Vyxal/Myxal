#ifndef LLVM_COMPILER_HPP
#define LLVM_COMPILER_HPP

#include <jni.h>

extern "C" {
    JNIEXPORT void JNICALL Java_io_github_seggan_myxal_compiler_native_LlvmCompiler_visitString(JNIEnv *env, jobject obj, jstring str);
}

#endif