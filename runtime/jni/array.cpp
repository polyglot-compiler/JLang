#include <string.h>
#include "types.h"

extern "C" {

void Java_support_Array_setObjectEntry__ILjava_lang_Object_2(jarray* obj,
                                                             jint i,
                                                             jobject* val) {
    ((jobject**) &obj->data)[i] = val;
}

} // extern "C"
