#include <jni.h>
#include "rep.h"

extern "C" {

jint Java_java_lang_Object_hashCode(JNIEnv* env, jobject o) {
    auto addr = reinterpret_cast<intptr_t>(o);
    return static_cast<jint>(addr);
}

jclass Java_java_lang_Object_getClass(JNIEnv* env, jobject o) {
    return Unwrap(o)->Cdv()->Class()->Wrap();
}

} // extern "C"
