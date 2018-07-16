//
// This file includes class declarations that represent the layout of
// Java objects, dispatch vectors, arrays, and more.
//
// Other native code can interact with these structures through the
// inlined functions defined here.
//
#pragma once

#include <type_traits>
#include <jni.h>
#include <cinttypes>
#include "interface.h"

// Ensure that our C++ representations are POD types,
// i.e., that they have the layout we'd expect from a C struct.
#define ASSERT_POD(layout) static_assert(   \
    std::is_trivial<layout>::value &&       \
    std::is_standard_layout<layout>::value, \
    "Java object representations should have the layout of C structs")

struct JObjectRep;
struct JArrayRep;
struct JStringRep;
struct JClassRep;

struct type_info {
    int32_t size;
    void* super_type_ids[];
};

// Class dispatch vector.
struct DispatchVector {
    JClassRep* Class() { return *class_; }
    idv_ht* Idv() { return idv_; }
    void SetIdv(idv_ht* idv) { idv_ = idv; }
    type_info* SuperTypes() { return super_types_; }
private:
    JClassRep** class_; // Notice: double-pointer.
	idv_ht* idv_;
    type_info* super_types_;
};

// Currently unimplemented.
struct sync_vars {
	// pthread_mutex_t* mutex;
	// pthread_cond_t *condition_variable;
};

// Representation for java.lang.Object.
struct JObjectRep {
    DispatchVector* Cdv() { return cdv_; }
    sync_vars* SyncVars() { return sync_vars_; }
    jobject Wrap() { return reinterpret_cast<jobject>(this); }
    void SetCdv(DispatchVector* cdv)  { cdv_ = cdv; }
private:
    DispatchVector* cdv_;
    sync_vars* sync_vars_;
};
ASSERT_POD(JObjectRep);

// Representation for Java built-in arrays.
struct JArrayRep {
    jsize Length() { return len_ ; }
    jsize ElemSize() { return elem_size_ ; }
    void* Data() { return data_; }
    JObjectRep* Super() { return &header_; }
    jarray Wrap() { return reinterpret_cast<jarray>(this); }
private:
    JObjectRep header_;
    jsize len_;
    jsize elem_size_;
    char data_[0];
};
ASSERT_POD(JArrayRep);

// Representation for java.lang.String.
struct JStringRep {
    JArrayRep* Chars() { return value_; }
    JObjectRep* Super() { return &header_; }
    jstring Wrap() { return reinterpret_cast<jstring>(this); }
private:
    JObjectRep header_;
    JArrayRep* value_;
};
ASSERT_POD(JStringRep);

// Representation for java.lang.Class.
struct JClassRep {
    JObjectRep* Super() { return &header_; }
    jclass Wrap() { return reinterpret_cast<jclass>(this); }
private:
    JObjectRep header_;
};
ASSERT_POD(JClassRep);

// These functions convert an opaque Java object reference into
// a pointer to the appropriate C++ representation.
inline JObjectRep* Unwrap(jobject o) {
    return reinterpret_cast<JObjectRep*>(o);
}
inline JArrayRep* Unwrap(jarray o) {
    return reinterpret_cast<JArrayRep*>(o);
}
inline JStringRep* Unwrap(jstring o) {
    return reinterpret_cast<JStringRep*>(o);
}
inline JClassRep* Unwrap(jclass o) {
    return reinterpret_cast<JClassRep*>(o);
}
