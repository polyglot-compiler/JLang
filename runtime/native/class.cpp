// This file defines important data structures associated with
// Java class objects. These data structures are used to support
// JVM/JNI functionality, such as reflection.
#include <cassert>
#include <cstdio>
#include <cstdlib>
#include <string>
#include <unordered_map>
#include "jni.h"
#include "class.h"

static constexpr bool kDebug = false;

extern "C" {
// These structs are generated statically for each class, and
// exist for the lifetime of the program.
// The layout must precisely mirror the layout defined in PolyLLVM.

struct RawFieldData {
    char* name;
    int32_t offset;
};

struct RawMethodData {
    char* name;       // Name (without signature).
    char* sig;        // JNI-specified signature encoding.
    int32_t offset;   // Offset into dispatch vector. -1 for static methods.
    void* fnPtr;      // Used for CallNonvirtual and CallStatic.
    void* trampoline; // Trampoline for casting the fnPtr to the correct type.
};

struct RawClassData {
    char* name;
    jclass* super_ptr;

    int32_t num_fields;
    RawFieldData* fields;

    int32_t num_methods;
    RawMethodData* methods;

    // TODO: static fields
};

} // extern "C"

// An optimized collection of class data.
struct ClassInfo {
    const char* name;
    jclass super;

    // Maps field names to offsets.
    std::unordered_map<std::string, int32_t> fieldIDs;

    // Maps method names/signatures to method data.
    std::unordered_map<std::string, const RawMethodData*> methodIDs;

    ClassInfo(const RawClassData* data) {

        name = data->name;

        // Recall that super classes are loaded before subclasses,
        // so the pointer dereference here will return the correct value.
        super = data->super_ptr ? *data->super_ptr : nullptr;

        // Fields.
        for (int32_t i = 0; i < data->num_fields; ++i) {
            auto* f = &data->fields[i];
            fieldIDs.emplace(f->name, f->offset);
        }

        // Methods.
        for (int32_t i = 0; i < data->num_methods; ++i) {
            auto* m = &data->methods[i];
            std::string key;
            key += m->name;
            key += m->sig;
            methodIDs.emplace(key, m);
        }
    }
};

// For simplicity we store class information in a map.
// If we find this to be too slow, we could allocate extra memory for
// class objects and store the information inline with each instance.
static std::unordered_map<jclass, const ClassInfo> info_map;

const char* GetJavaClassName(jclass cls) {
    return info_map.at(cls).name;
}

jclass GetJavaSuperClass(jclass cls) {
    return info_map.at(cls).super;
}

jfieldID GetJavaFieldId(jclass cls, const char* name) {
    auto& cache = info_map.at(cls).fieldIDs;
    auto it = cache.find(name);

    if (it != cache.end()) {
        return reinterpret_cast<jfieldID>(it->second);
    } else {
        // TODO: Should technically throw NoSuchFieldError.
        fprintf(stderr,
            "Could not find field %s in class %s. Aborting.\n",
            name, GetJavaClassName(cls));
        abort();
    }
}

jmethodID TryGetJavaMethodID(jclass cls, const char* name, const char* sig) {
    std::string key;
    key += name;
    key += sig;
    auto& cache = info_map.at(cls).methodIDs;
    auto it = cache.find(key);

    if (it != cache.end()) {
        // Scary cast to wrap method data in opaque jmethodID.
        // We trust other native code to treat the jmethodID as 'const';
        auto no_const = const_cast<RawMethodData*>(it->second);
        return reinterpret_cast<jmethodID>(no_const);
    }
    else if (auto super = GetJavaSuperClass(cls)) {
        // TODO: Technically might not want to recurse for 'private' methods.
        return GetJavaMethodID(super, name, sig);
    }
    else {
        return nullptr;
    }
}

jmethodID GetJavaMethodID(jclass cls, const char* name, const char* sig) {
    if (auto res = TryGetJavaMethodID(cls, name, sig)) {
        return res;
    } else {
        // TODO: Should technically throw NoSuchMethodError.
        fprintf(stderr,
            "Could not find method %s%s in class %s. Aborting\n",
            name, sig, GetJavaClassName(cls));
        abort();
    }
}

extern "C" {

void RegisterJavaClass(jclass cls, const RawClassData* data) {

    if (kDebug) {

        printf("loading class %s with super class %s\n",
            data->name,
            data->super_ptr ? GetJavaClassName(*data->super_ptr) : "[none]");

        for (int32_t i = 0; i < data->num_fields; ++i) {
            auto* f = &data->fields[i];
            printf("  found field %s with offset %d\n", f->name, f->offset);
        }

        for (int32_t i = 0; i < data->num_methods; ++i) {
            auto* m = &data->methods[i];
            printf(
                "  found method %s%s\n"
                "    offset %d\n"
                "    function pointer %p\n"
                "    trampoline pointer %p\n"
                , m->name, m->sig, m->offset, m->fnPtr, m->trampoline);
        }
    }

    assert(info_map.count(cls) == 0 && "Java class was loaded twice!");
    info_map.emplace(cls, data);
}

} // extern "C"

