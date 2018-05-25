#include <jni.h>
#include <cinttypes>

extern "C" {

jint Java_java_lang_Object_hashCode__(JNIEnv* env, jobject o) {
    auto addr = reinterpret_cast<intptr_t>(o);
    return static_cast<jint>(addr);
}

jclass Java_java_lang_Object_getClass__(JNIEnv* env, jobject o) {
    return env->GetObjectClass(o);
}

} // extern "C"
