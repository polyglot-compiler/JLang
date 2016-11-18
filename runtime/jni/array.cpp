#include <string.h>
#include "types.h"

extern "C" {

void Java_support_Array_clearEntries__(jarray* obj) {
    memset(&obj->data, 0, obj->len * sizeof(obj->data));
}

void Java_support_Array_setObjectEntry__ILjava_lang_Object_2(jarray* obj,
                                                             jint i,
                                                             jobject* val) {
    ((jobject**) &obj->data)[i] = val;
}

} // extern "C"
