#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include "types.h"

// static jarray* allocArray(jint len) {
//     return (jarray*) GC_malloc(sizeof(jarray)
//                                - sizeof(intptr_t)
//                                + len * sizeof(intptr_t));
// }

// static void initMultidimensionalArray(jarray* obj, intptr_t* lens,
//                                       int32_t depth, int32_t maxDepth) {
//     jint len = static_cast<jint>(lens[depth]);
//     obj->len = len;
//     if (depth + 1 == maxDepth) {
//         // This is the last dimension; zero all entries.
//         memset(&obj->data, 0, len * sizeof(obj->data));
//     } else {
//         // Recurse on each entry.
//         jarray** data = (jarray**) &obj->data;
//         jint sublen = static_cast<jint>(lens[depth + 1]);
//         for (jlong i = 0; i < len; ++i) {
//             data[i] = allocArray(sublen);
//             initMultidimensionalArray(
//                 data[i], lens, depth + 1, maxDepth
//             );
//         }
//     }
// }

extern "C" {

void Java_support_Array_clearEntries__(jarray* obj) {
    memset(&obj->data, 0, obj->len * sizeof(obj->data));
}

void Java_support_Array_setEntry__ILjava_lang_Object_2(jarray* obj,
                                                       jint i,
                                                       jobject* val) {
    ((jobject**) &obj->data)[i] = val;
}

// void Java_support_Array_Array___3I(jarray* obj, jarray* lens) {
//     initMultidimensionalArray(obj, &lens->data, 0, lens->len);
// }

} // extern "C"
