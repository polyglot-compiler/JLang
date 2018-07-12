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
#include <utility>

extern "C" {
// These structs are generated statically for each class, and
// exist for the lifetime of the program.
// The layout must precisely mirror the layout defined in PolyLLVM.

// Concrete representation for the opaque type jfieldID.
struct JavaFieldInfo {
    char* name;
    int32_t offset;
};
//This is also a representation for the jfieldID type, but
//represented differently since static fields are implemented as global pointers.
struct JavaStaticFieldInfo {
    char* name;
    char* sig;
    void* ptr;
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
    jboolean isIntf;

    int32_t num_intfs;
    jclass** intfs;

    int32_t num_fields;
    JavaFieldInfo* fields;

    int32_t num_static_fields;
    JavaStaticFieldInfo* static_fields;

    int32_t num_methods;
    JavaMethodInfo* methods;

};

// Called by the runtime at most once per class to register
// the class information declared above.
void
RegisterJavaClass(jclass cls, const JavaClassInfo* data);

} // extern "C"

//Assumes non-null valid C-string for name
const jclass
GetPrimitiveClass(const char *name);

const JavaClassInfo*
GetJavaClassInfo(jclass cls);

//Assumes non-null valid C-string for name
//name is in java.lang.String format
const jclass
GetJavaClassFromName(const char* name);

//Assumes non-null valid C-string for name
//name is in java/lang/String format
const jclass
GetJavaClassFromPathName(const char* name);

const JavaFieldInfo*
GetJavaFieldInfo(jclass cls, const char* name);

const JavaStaticFieldInfo*
GetJavaStaticFieldInfo(jclass cls, const char* name, const char* sig);

const std::pair<JavaMethodInfo*,int32_t>
GetJavaMethodInfo(jclass cls, const char* name, const char* sig);
