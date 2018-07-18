#include "reflect.h"

#include <stdio.h>
#include "factory.h"
#include "class.h"
#define CTOR_CTOR Polyglot_java_lang_reflect_Constructor_Constructor__Ljava_lang_Class_2_3Ljava_lang_Class_2_3Ljava_lang_Class_2IILjava_lang_String_2_3B_3B
extern "C" {

  void CTOR_CTOR(jclass, jobjectArray, jobjectArray, jint, jint, jstring, jbyteArray, jbyteArray);

bool InstanceOf(jobject obj, void* type_id) {
    if (obj == nullptr)
        return false;
    type_info* type_info = Unwrap(obj)->Cdv()->SuperTypes();
    for (int32_t i = 0, end = type_info->size; i < end; ++i)
        if (type_info->super_type_ids[i] == type_id)
            return true;
    return false;
}
} // extern "C"

static jobject
CreateConstructor(jclass declaring_clazz, const JavaClassInfo* clazz_info, JavaMethodInfo ctor_info) {
  auto ctor_clazz = GetJavaClassFromName("java.lang.reflect.Constructor");
  auto ctor_clazz_info = GetJavaClassInfo(ctor_clazz);
  auto ctor_obj = CreateJavaObject(ctor_clazz);
  return NULL;
}
