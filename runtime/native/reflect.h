#pragma once

#include "rep.h"
#include "class.h"

extern "C" {
  bool InstanceOf(jobject obj, void *compare_type_id);
} // extern "C"

static jobject
CreateConstructor(jclass declaring_clazz, const JavaClassInfo* clazz_info, JavaMethodInfo ctor_info);
