//
// Created by Admin on 2023/4/21.
//
#include <jni.h>
#define JNI_ON_LOAD_RESULT_ERR  -1 //返回错误



int registerNativeFunction(JavaVM *vm, JNIEnv *env);

// allow the native library to perform any necessary initialization before the library's native methods can be called from Java code.
// vm parameter is a pointer to the JavaVM instance that the native library is being loaded into,
// reserved parameter is a pointer to a block of memory reserved for future use.
//Get the JNI environment pointer for the current thread using vm->GetEnv().
//It's worth noting that JNI_OnLoad is not called when a library is dynamically linked at runtime using System.loadLibrary().
// Instead, it is only called when a library is statically linked into a Java application or loaded using the -Djava.library. path
//
//
JNIEXPORT  jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env =0;
    if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ON_LOAD_RESULT_ERR;
    }
    if (registerNativeFunction(vm, env) != JNI_TRUE) {
        return  JNI_ON_LOAD_RESULT_ERR;
    }
    return JNI_VERSION_1_6;
}
