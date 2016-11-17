#ifndef TYPES_H
#define TYPES_H

#include <stdint.h>

extern "C" {

// List of interfaces implemented by a Java class.
class it {
public:
    it* next;
    char* interface_name;
};

// Dispatch vector for a Java class.
class dv {
public:
    it* it;
    void* type_info;
};

// Header of a Java object instance.
class jobject {
public:
    dv* dv;
};

class support_Array {
public:
    dv* dv;
    int32_t len;
    intptr_t data;
};

class java_lang_String {
public:
    dv* dv;
    support_Array* chars;
};

using jint = int32_t;
using jlong = int64_t;
using jfloat = float;
using jdouble = double;
using jstring = java_lang_String;
using jarray = support_Array;

} // extern "C"

#endif // TYPES_H
