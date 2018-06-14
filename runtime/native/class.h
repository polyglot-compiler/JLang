// Provides reflection-like support, especially for use by JNI.
// PolyLLVM emits static information for each class, and registers
// that information by calling RegisterJavaClass upon loading a class.
// The other functions defined here provide access to
// class information in an organized way.
//
// For example, this interface can be used to get Java field offsets,
// Java method pointers, Java class names.
#pragma once

#include "jni.h"

extern "C" {
// These structs are generated statically for each class, and
// exist for the lifetime of the program.
// The layout must precisely mirror the layout defined in PolyLLVM.

// Concrete representation for the opaque type jfieldID.
struct JavaFieldInfo {
    char* name;
    int32_t offset;
};

// Concrete representation for the opaque type jmethodID.
struct JavaMethodInfo {
    char* name;       // Name (without signature).
    char* sig;        // JNI-specified signature encoding.
    int32_t offset;   // Offset into dispatch vector. -1 for static methods.
    void* fnPtr;      // Used for CallNonvirtual and CallStatic.
    void* trampoline; // Trampoline for casting the fnPtr to the correct type.
    void* intf_id;    // For interface methods, the interface id.
    int32_t intf_id_hash; // A precomputed hash of the intf_id.
};

struct JavaClassInfo {
    char* name;
    jclass* super_ptr;

    int32_t num_fields;
    JavaFieldInfo* fields;

    int32_t num_methods;
    JavaMethodInfo* methods;

    // TODO: static fields
};

// Called by the runtime at most once per class to register
// the class information declared above.
void
RegisterJavaClass(jclass cls, const JavaClassInfo* data);

} // extern "C"

const JavaClassInfo*
GetJavaClassInfo(jclass cls);

const jclass
GetJavaClassFromName(const char* name);

const JavaFieldInfo*
GetJavaFieldInfo(jclass cls, const char* name);

const JavaMethodInfo*
GetJavaMethodInfo(jclass cls, const char* name, const char* sig);
