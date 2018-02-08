//
// Created by Daniel Donenfeld on 3/5/17.
//

#ifndef JNI_EXCEPTION_H
#define JNI_EXCEPTION_H

#include <unwind.h>
#include "types.h"

extern "C" {

_Unwind_Exception *createJavaException(jobject *jexception);
void throwJavaException(_Unwind_Exception* exception);

} // extern "C"


#endif //JNI_EXCEPTION_H
