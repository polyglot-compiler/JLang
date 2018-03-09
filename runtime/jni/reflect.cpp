#include "reflect.h"

#include <stdio.h>

extern "C" {

bool instanceof(jobject obj, void* compare_type_id) {
    if (obj == nullptr)
        return false;
    type_info* type_info = obj->dv->type_info;
    for (int32_t i = 0, end = type_info->size; i < end; ++i)
        if (type_info->super_type_ids[i] == compare_type_id)
            return true;
    return false;
}

jobject Java_java_lang_Object_getClass__(jobject obj) {
    return nullptr; // TODO
}

} // extern "C"
