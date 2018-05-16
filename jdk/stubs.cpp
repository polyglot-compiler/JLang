// This file defines symbols that are otherwise
// missing for various (usually unknown) reasons.
#include <cstdio>
#include <cstdlib>
#include "jni.h"

extern "C" {

// We always return null, representing the boot class loader.
jobject Java_java_lang_Class_getClassLoader0(JNIEnv*, jobject) {
    return nullptr;
}

void Polyglot_java_lang_Enum_compareTo__Ljava_lang_Object_2() {
    // For some reason Polyglot adds this method to java.lang.Enum with
    // the wrong argument type, in addition to the correct version.
    // This should be fixed, because this likely breaks
    // method dispatch for enums. For now we abort if called.
    fprintf(stderr, "This method should not be called\n");
    abort();
}

} // extern "C"
