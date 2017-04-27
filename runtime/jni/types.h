#ifndef TYPES_H
#define TYPES_H

#include <stdint.h>
#include <pthread.h>

extern "C" {

// List of interfaces implemented by a Java class.
class it {
public:
    it* next;
    char* interface_name;
};

class type_info {
public:
    int32_t size;
    void* super_type_ids[];
};

// Dispatch vector for a Java class.
class dv {
public:
    it* it;
    type_info* type_info;
};

class sync_vars {
	pthread_mutex_t* mutex;
	pthread_cond_t *condition_variable;
};

// Header of a Java object instance.
class jobject {
public:
    dv* dv;
    sync_vars* sync_vars;
};

class jarray : public jobject {
public:
    int32_t len;
    intptr_t data;
};

class jstring : public jobject {
public:
    jarray* chars;
};

using jbool = bool;
using jbyte = int8_t;
using jshort = int16_t;
using jchar = uint16_t;
using jint = int32_t;
using jlong = int64_t;
using jfloat = float;
using jdouble = double;

} // extern "C"

#endif // TYPES_H
