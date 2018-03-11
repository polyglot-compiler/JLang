#include "types.h"

extern "C" {

jint Java_java_lang_Object_hashCode__(jobject o) {
    auto addr = reinterpret_cast<intptr_t>(o);
    return static_cast<jint>(addr);
}

jobject Java_java_lang_Object_getClass__(jobject obj) {
    return *obj->dv->class_obj;
}

} // extern "C"
