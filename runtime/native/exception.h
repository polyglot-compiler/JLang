//Copyright (C) 2018 Cornell University

#pragma once

#include <unwind.h>
#include "rep.h"
#include "jni.h"

void throwClassNotFoundException(JNIEnv *env, const char* name);
extern "C" {

_Unwind_Exception *createUnwindException(jobject jexception);
void throwUnwindException(_Unwind_Exception* exception);

} // extern "C"
