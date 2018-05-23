#include <cstring>
#include <jni.h>

extern "C" {

jstring JVM_GetClassName(JNIEnv *env, jclass cls);

// Need mutable C-strings here, for RegisterNatives to accept them.
char getNameName[] = "getName";
char getNameSig[] = "()Ljava.lang.String;";

static JNINativeMethod methods[] = {
    {getNameName, getNameSig, (void*) &JVM_GetClassName},
};

void Java_java_lang_Class_registerNatives(JNIEnv *env, jclass clazz) {
    env->RegisterNatives(clazz, methods, sizeof(methods)/sizeof(JNINativeMethod));
}

} // extern "C"
