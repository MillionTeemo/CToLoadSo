#include <jni.h>
#include <string>

#define JNI_ON_LOAD_RESULT_ERR  -1 //返回错误

extern "C" JNIEXPORT jstring JNICALL
Java_com_zltech_ctoloadso_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}