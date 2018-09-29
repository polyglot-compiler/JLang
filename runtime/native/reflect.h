//Copyright (C) 2018 Cornell University

#pragma once

#include <stdio.h>
#include "rep.h"
#include "class.h"
#include "factory.h"
extern "C" {
  bool InstanceOf(jobject obj, void *compare_type_id);
} // extern "C"

jobject
CreateConstructor(jclass declaring_clazz, const JavaClassInfo* clazz_info, JavaMethodInfo ctor_info);
