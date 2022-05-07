#include <iostream>
#include "LlvmCompiler.hpp"

JNIEXPORT void JNICALL Java_io_github_seggan_myxal_compiler_native_LlvmCompiler_visitString(JNIEnv *env, jobject obj, jstring str) {
    const char *nativeString = env->GetStringUTFChars(str, 0);
    std::cout << nativeString << std::endl;
    env->ReleaseStringUTFChars(str, nativeString);
}