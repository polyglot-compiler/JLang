#include "rep.h"

extern "C" {

jint Java_java_lang_Object_hashCode__(jobject o) {
    auto addr = reinterpret_cast<intptr_t>(o);
    return static_cast<jint>(addr);
}

jclass Java_java_lang_Object_getClass__(jobject o) {
    return Unwrap(o)->Cdv()->Class()->Wrap();
}

} // extern "C"
