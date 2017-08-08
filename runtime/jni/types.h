#ifndef TYPES_H
#define TYPES_H

#include <stdint.h>
#include <pthread.h>
#include "interface.h"

extern "C" {

class type_info {
public:
    int32_t size;
    void* super_type_ids[];
};

// Interface table (a.k.a. interface dispatch vector).
class it {
public:
};

// Class dispatch vector.
class dv {
public:
	idv_ht* itt;
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
