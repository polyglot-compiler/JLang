#include <string.h>
#include "types.h"

extern "C" {

void Java_support_Array_Array__I(jarray* _this, jint len) {
    _this->len = len;
    memset(&_this->data, 0, len * sizeof(void*));
}

} // extern "C"
