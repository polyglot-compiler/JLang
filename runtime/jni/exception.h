//
// Created by Daniel Donenfeld on 3/5/17.
//

#ifndef JNI_EXCEPTION_H
#define JNI_EXCEPTION_H

#include <unwind.h>

extern "C" {

typedef struct _Unwind_Exception UnwindException;

UnwindException *allocateJavaException(void *jexception);
void throwJavaException(UnwindException* exception);

}


#endif //JNI_EXCEPTION_H
