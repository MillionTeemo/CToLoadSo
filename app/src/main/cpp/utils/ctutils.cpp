//
// Created by Admin on 2023/4/21.
//
#include <jni.h>
#include <string>
bool isValidUTF8(const char *str) {
    int len = strlen(str);
    int i = 0;
    while (i < len) {
        int c = (unsigned char) str[i];
        int n;
        if (c < 0x80) {
            n = 1;
        } else if (c < 0xc2) {
            return false;
        } else if (c < 0xe0) {
            n = 2;
        } else if (c < 0xf0) {
            n = 3;
        } else if (c < 0xf5) {
            n = 4;
        } else {
            return false;
        }
        for (int j = 1; j < n; j++) {
            if (i + j >= len) {
                return false;
            }
            if ((unsigned char) str[i+j] < 0x80 || (unsigned char) str[i+j] >= 0xc0) {
                return false;
            }
        }
        i += n;
    }
    return true;
}

jstring  stringFromJNI(JNIEnv *env,jclass cl,jbyteArray data ){
    char * c_data =(char *) env->GetByteArrayElements(data, nullptr );
    if (c_data == nullptr) {
        // Release any allocated memory and return null if unable to get byte array elements
        env->ReleaseByteArrayElements(data, (jbyte *)c_data, 0);
        return nullptr;
    }
    // Validate UTF-8 encoding
    if (!isValidUTF8(c_data)) {
        // Release any allocated memory and return null if invalid UTF-8 encoding
        env->ReleaseByteArrayElements(data, (jbyte *)c_data, 0);
        return nullptr;
    }
    jstring jstr = env->NewStringUTF(c_data);
    env->ReleaseByteArrayElements(data, (jbyte *)c_data, 0);
    return jstr;
}


static const char * ClassPathName_CApis = "com/zltech/ctoloadso/CApis";
static JNINativeMethod Method[]={
        {"stringFromJNI", "([B)Ljava/lang/String;", (void *) stringFromJNI},
};

inline int registerNativeMethods(JNIEnv *env, const char *className, JNINativeMethod *method,
                                 int methodsCount) {
    jclass clazz;
    clazz = env->FindClass(className);
    if (clazz == nullptr) {
        return JNI_FALSE;
    }

    if (env->RegisterNatives(clazz, method, methodsCount) < 0) {
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

extern "C" int registerNativeFunction(JavaVM *vm, JNIEnv *env){

    if (!registerNativeMethods(env, ClassPathName_CApis, Method,
                               sizeof(Method) /
                               sizeof(Method[0]))) {
        return JNI_FALSE;
    }
    return JNI_TRUE;
}


