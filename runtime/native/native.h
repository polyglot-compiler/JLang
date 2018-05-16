#ifndef NATIVE_H
#define NATIVE_H
#include "jni.h"

void
RegisterJavaNativeFunc(
    jclass cls,            // e.g., java.lang.Object
    const char* name,      // e.g., wait
    const char* signature, // e.g., (J)V
    void* func
);

#endif // NATIVE_H