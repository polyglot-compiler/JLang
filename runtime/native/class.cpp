// This file defines important data structures associated with
// Java class objects. These data structures are used to support
// JVM/JNI functionality, such as reflection.
#include <cstdio>
#include <cstdlib>
#include <string>
#include <unordered_map>
#include "jni.h"

#include "class.h"

// These structs are generated statically for each class.
// The layout must precisely mirror the layout defined in PolyLLVM.
struct raw_class_data {
    char* name;

    int32_t num_fields;
    char** field_names;
    int32_t* field_offsets;

    // char** static_field_names;
    // void** static_field_ptrs;
};

// An optimized layout of class data.
struct class_info {
    const char* name;
    std::unordered_map<std::string, int32_t> fieldIDs;

    class_info(const raw_class_data* data) {
        name = data->name;
        for (int32_t i = 0; i < data->num_fields; ++i) {
            fieldIDs.emplace(
                data->field_names[i],
                data->field_offsets[i]);
        }
    }
};

// For simplicity we store class information in a map.
// If we find this to be too slow, we could allocate extra memory for
// class objects and store the information inline with each instance.
static std::unordered_map<jclass, const class_info> info_map;

const char* get_java_class_name(jclass cls) {
    return info_map.at(cls).name;
}

jfieldID get_java_field_id(jclass cls, const char* name) {
    auto& cache = info_map.at(cls).fieldIDs;
    auto it = cache.find(name);
    if (it == cache.end())
        return nullptr;
    // TODO
    printf("Found field %s with offset %d\n", name, it->second);
    return reinterpret_cast<jfieldID>(it->second);
}

extern "C" {

// This should be called at most once per class.
void register_java_class(jclass cls, const raw_class_data* data) {

    // TODO
    printf("Loading class: %s\n", data->name);
    for (int32_t i = 0; i < data->num_fields; ++i) {
        printf("  Found field %s with offset %d\n",
            data->field_names[i],
            data->field_offsets[i]);
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

