#pragma once

#include <unwind.h>
#include "rep.h"

extern "C" {

_Unwind_Exception *createUnwindException(jobject jexception);
void throwUnwindException(_Unwind_Exception* exception);

} // extern "C"
