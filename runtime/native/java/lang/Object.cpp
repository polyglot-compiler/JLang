#include "types.h"

extern "C" {

jint Java_java_lang_Object_hashCode__(jobject o) {
    auto addr = reinterpret_cast<intptr_t>(o);
    return static_cast<jint>(addr);
}

} // extern "C"
