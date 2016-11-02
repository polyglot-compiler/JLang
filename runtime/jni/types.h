#ifndef TYPES_H
#define TYPES_H

#include <stdint.h>

extern "C" {

class support_Array {
public:
    void* dv;
    int32_t len;
    char* data;
};

class java_lang_String {
public:
    void* dv;
    support_Array* chars;
};

using jint = int32_t;
using jlong = int64_t;
using jfloat = float;
using jdouble = double;
using jstring = java_lang_String;

} // extern "C"

#endif // TYPES_H
