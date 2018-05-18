// This file defines important data structures associated with
// Java class objects. These data structures are used to support
// JVM/JNI functionality, such as reflection.
#include <cstdio>
#include <cstdlib>
#include <string>
#include <unordered_map>
#include "jni.h"
#include "class.h"

static constexpr bool kDebug = false;

extern "C" {
// These structs are generated statically for each class.
// The layout must precisely mirror the layout defined in PolyLLVM.

struct RawFieldData {
    char* name;
    int32_t offset;
};

struct RawClassData {
    char* name;

    int32_t num_fields;
    RawFieldData* fields;

    // char** field_names;
    // int32_t* field_offsets;

    // char** static_field_names;
    // void** static_field_ptrs;
};

} // extern "C"

// An optimized layout of class data.
struct ClassInfo {
    const char* name;
    std::unordered_map<std::string, int32_t> fieldIDs;

    ClassInfo(const RawClassData* data) {
        name = data->name;
        for (int32_t i = 0; i < data->num_fields; ++i) {
            fieldIDs.emplace(
                data->fields[i].name,
                data->fields[i].offset);
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

jfieldID GetJavaFieldId(jclass cls, const char* name) {
    auto& cache = info_map.at(cls).fieldIDs;
    auto it = cache.find(name);
    if (it == cache.end()) {
        // TODO: Should technically throw NoSuchFieldError.
        fprintf(stderr,
            "Could not find field %s in class %s. Aborting.\n",
            name, GetJavaClassName(cls));
        abort();
    }
    return reinterpret_cast<jfieldID>(it->second);
}

extern "C" {

// This should be called at most once per class.
void RegisterJavaClass(jclass cls, const RawClassData* data) {

    if (kDebug) {
        printf("[runtime] loading %s\n", data->name);
        for (int32_t i = 0; i < data->num_fields; ++i) {
            printf("  found field %s with offset %d\n",
                data->fields[i].name,
                data->fields[i].offset);
        }
    }

    if (info_map.count(cls)) {
        fprintf(stderr,
            "ERROR: Java class %s was loaded twice! Aborting.\n",
            data->name);
        abort();
    }
    info_map.emplace(cls, data);
}

} // extern "C"

