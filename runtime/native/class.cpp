//Copyright (C) 2018 Cornell University

// This file defines important data structures associated with
// Java class objects. These data structures are used to support
// JVM/JNI functionality, such as reflection.
#include <cassert>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <unordered_map>
#include <string>
#include <dlfcn.h>
#include <stdio.h>
#include "jni.h"
#include "class.h"
#include "rep.h"


#define MEMCPY(a,b,c) memcpy((void *) a, (void *) b, c)
static constexpr bool kDebug = false;

#define CLASS_SIZE 168
#define PRIM_CLASS(prim, klass) prim##klass
#define PRIM_CLASS_DEF(prim) static JClassRep* PRIM_CLASS(prim, Klass) \
 = (JClassRep*)malloc(CLASS_SIZE);

PRIM_CLASS_DEF(int)
PRIM_CLASS_DEF(short)
PRIM_CLASS_DEF(byte)
PRIM_CLASS_DEF(long)
PRIM_CLASS_DEF(float)
PRIM_CLASS_DEF(double)
PRIM_CLASS_DEF(char)
PRIM_CLASS_DEF(boolean)

#define PRIM_NAME_CHECK(name, prim)		\
  if (strcmp(name, prim) == 0) {

#define PRIM_NAME_TO_CLASS(name, cname, prim)		\
  PRIM_NAME_CHECK(name, cname)				\
  return PRIM_CLASS(prim, Klass)->Wrap();

#define PRIM_IS_KLASS(cls, prim) \
  if (Unwrap(cls) == PRIM_CLASS(prim, Klass)) {	\
    return true;

#define PRIM_REGISTER(prim)			\
  if(1) { \
    JavaClassInfo* newInfo = (JavaClassInfo*)malloc(sizeof(JavaClassInfo)); \
    int nameLen = strlen(#prim) + 1;					\
    char* newName = (char*) malloc(nameLen);				\
    memcpy(newName, #prim, nameLen);					\
    newInfo->name = newName;						\
    newInfo->isIntf = 0;              \
    newInfo->num_intfs = 0;              \
    newInfo->num_fields = 0;              \
    newInfo->num_static_fields = 0;              \
    newInfo->num_methods = 0;              \
    newInfo->obj_size = CLASS_SIZE;       \
    newInfo->super_ptr = NULL;            \
    newInfo->cdv = NULL;                  \
    RegisterJavaClass(PRIM_CLASS(prim, Klass)->Wrap(), newInfo);	\
  } else ((void) 0)


static bool primKlassInit = false;
jclass initArrayKlass();
jclass globalArrayKlass = NULL;

jclass Polyglot_native_int;
jclass Polyglot_native_byte;
jclass Polyglot_native_short;
jclass Polyglot_native_long;
jclass Polyglot_native_float;
jclass Polyglot_native_double;
jclass Polyglot_native_char;
jclass Polyglot_native_boolean;

// For simplicity we store class information in a map.
// If we find this to be too slow, we could allocate extra memory for
// class objects and store the information inline with each instance.
static std::unordered_map<jclass, const JavaClassInfo*> classes;
static std::unordered_map<std::string, const jclass> cnames;

extern "C" {

extern void Polyglot_jlang_runtime_ObjectArray_load_class();
extern jclass Polyglot_jlang_runtime_ObjectArray_class;


void RegisterJavaClass(jclass cls, const JavaClassInfo* info) {

    if (kDebug) {

        printf("loading %s %s with super class %s\n",
	       (info->isIntf ? "interface" : "class"),
            info->name,
            info->super_ptr
                ? GetJavaClassInfo(*info->super_ptr)->name
                : "[none]");

	for (int32_t i = 0; i < info->num_intfs; ++i) {
	  jclass* intf = info->intfs[i];
	  printf("  implements interface %p\n", *intf);
	}
        for (int32_t i = 0; i < info->num_fields; ++i) {
            auto* f = &info->fields[i];
            printf("  found field %s with offset %d\n", f->name, f->offset);
        }

        for (int32_t i = 0; i < info->num_static_fields; ++i) {
            auto* f = &info->static_fields[i];
            printf("  found static field %s with sig %s and ptr %p\n", f->name, f->sig, f->ptr);
        }

        for (int32_t i = 0; i < info->num_methods; ++i) {
            auto* m = &info->methods[i];
            printf(
                "  found method %s%s\n"
                "    offset %d\n"
                "    function pointer %p\n"
                "    trampoline pointer %p\n"
                , m->name, m->sig, m->offset, m->fnPtr, m->trampoline);
        }
    }

    assert(classes.count(cls) == 0 && "Java class was loaded twice!");
    classes.emplace(cls, info);
    std::string cname(info->name);
    cnames.emplace(cname, cls);
}

} // extern "C"

//Force this class load function to be called at initialization
jclass initArrayKlass() {
  Polyglot_jlang_runtime_ObjectArray_load_class();
  return Polyglot_jlang_runtime_ObjectArray_class;
}        

//This assumes char* is non-null, C-string with len > 0
bool isArrayClassName(const char* name) {
  return name[0] == '[';
}

bool isArrayClass(jclass cls) {
  auto cinfo = GetJavaClassInfo(cls);
  if (cinfo == NULL) {
    return JNI_FALSE;
  } else {
    return isArrayClassName(cinfo->name);
  }
}

//This assumes char* is non-null, C-string with len >0
const char*
getComponentName(const char* name) {
  return &(name[1]);
}

//This assumes char* is non-null, C-string with len > 0
//It also assumes that it has not been initialized
const jclass initArrayClass(const char* name) {
  if (globalArrayKlass == NULL) {
    globalArrayKlass = initArrayKlass();
  }
  jclass newKlazz = (jclass)malloc(sizeof(JClassRep));
  memcpy(newKlazz, globalArrayKlass, sizeof(JClassRep));
  JavaClassInfo* newInfo = (JavaClassInfo*)malloc(sizeof(JavaClassInfo));
  memcpy(newInfo, GetJavaClassInfo(globalArrayKlass), sizeof(JavaClassInfo));
  int nameLen = strlen(name) + 1; //add the '\0' terminator
  char* newName = (char*)malloc(nameLen);
  memcpy(newName, name, nameLen);
  newInfo->name = newName;
  RegisterJavaClass(newKlazz, newInfo);
  return newKlazz;
}

const void
RegisterPrimitiveClasses() {
  if (globalArrayKlass == NULL) {
    globalArrayKlass = initArrayKlass();
  }

  memcpy(PRIM_CLASS(int, Klass), globalArrayKlass, sizeof(JClassRep));
  Polyglot_native_int = reinterpret_cast<jclass>(PRIM_CLASS(int, Klass));
  PRIM_REGISTER(int);

  memcpy(PRIM_CLASS(byte, Klass), globalArrayKlass, sizeof(JClassRep));
  Polyglot_native_byte = reinterpret_cast<jclass>(PRIM_CLASS(byte, Klass));
  PRIM_REGISTER(byte);

  memcpy(PRIM_CLASS(short, Klass), globalArrayKlass, sizeof(JClassRep));
  Polyglot_native_short = reinterpret_cast<jclass>(PRIM_CLASS(short, Klass));
  PRIM_REGISTER(short);
  
  memcpy(PRIM_CLASS(long, Klass), globalArrayKlass, sizeof(JClassRep));
  Polyglot_native_long = reinterpret_cast<jclass>(PRIM_CLASS(long, Klass));
  PRIM_REGISTER(long);

  memcpy(PRIM_CLASS(float, Klass), globalArrayKlass, sizeof(JClassRep));
  Polyglot_native_float = reinterpret_cast<jclass>(PRIM_CLASS(float, Klass));
  PRIM_REGISTER(float);

  memcpy(PRIM_CLASS(double, Klass), globalArrayKlass, sizeof(JClassRep));
  Polyglot_native_double = reinterpret_cast<jclass>(PRIM_CLASS(double, Klass));
  PRIM_REGISTER(double);

  memcpy(PRIM_CLASS(char, Klass), globalArrayKlass, sizeof(JClassRep));
  Polyglot_native_char = reinterpret_cast<jclass>(PRIM_CLASS(char, Klass));
  PRIM_REGISTER(char);

  memcpy(PRIM_CLASS(boolean, Klass), globalArrayKlass, sizeof(JClassRep));
  Polyglot_native_boolean = reinterpret_cast<jclass>(PRIM_CLASS(boolean, Klass));
  PRIM_REGISTER(boolean);
}

//This assumes char* is non-null, C-string
jclass
primitiveComponentNameToClass(const char* name) {
  PRIM_NAME_TO_CLASS(name, "I", int)
  } else PRIM_NAME_TO_CLASS(name, "B", byte)
  } else PRIM_NAME_TO_CLASS(name, "S", short)
  } else PRIM_NAME_TO_CLASS(name, "J", long)
  } else PRIM_NAME_TO_CLASS(name, "F", float)
  } else PRIM_NAME_TO_CLASS(name, "D", double)
  } else PRIM_NAME_TO_CLASS(name, "C", char)
  } else PRIM_NAME_TO_CLASS(name, "Z", boolean)
  } else {
      return NULL;
  }
}

bool isPrimitiveClass(jclass cls) {
  PRIM_IS_KLASS(cls, int)
  } else PRIM_IS_KLASS(cls, byte)
  } else PRIM_IS_KLASS(cls, short)
  } else PRIM_IS_KLASS(cls, long)
  } else PRIM_IS_KLASS(cls, float)
  } else PRIM_IS_KLASS(cls, double)
  } else PRIM_IS_KLASS(cls, char)
  } else PRIM_IS_KLASS(cls, boolean)
  } else {
    return false;
  }
}

const JavaClassInfo*
GetJavaClassInfo(jclass cls) {
  if (!primKlassInit) {
    RegisterPrimitiveClasses();
    primKlassInit = true;
  }
  try {
    return classes.at(cls);
  } catch (const std::out_of_range& oor) {
    return NULL;
  }
}

const jclass
GetJavaClassFromPathName(const char* name) {
  int name_len = strlen(name);
  char path_name[name_len + 1];
  for (int i = 0; i <= name_len; i++) {
    char c = name[i];
    if (c == '/') {
      c = '.';
    }
    path_name[i] = c;
  }
  return GetJavaClassFromName(path_name);
}


const jclass
GetJavaClassFromName(const char* name) {
  if (!primKlassInit) {
    RegisterPrimitiveClasses();
    primKlassInit = true;
  }
  try {
    return cnames.at(std::string(name));
  } catch (const std::out_of_range& oor) {
    if (isArrayClassName(name)) {
      return initArrayClass(name);
    } else {
      return NULL;
    }
  }
}

jclass
GetComponentClass(jclass cls) {
  if (isArrayClass(cls)) {
    const JavaClassInfo* info = GetJavaClassInfo(cls);
    if (info != NULL) {
      const char* className = getComponentName(info->name);
      jclass primComponent = primitiveComponentNameToClass(className);
      if (primComponent == NULL) {
	return GetJavaClassFromName(className);
      } else {
	return primComponent;
      }
    }
  }
  return NULL;
}

const JavaStaticFieldInfo*
GetJavaStaticFieldInfo(jclass cls, const char* name, const char* sig) {
    auto* clazz = classes.at(cls);
    auto* fields = clazz->static_fields;
    for (int32_t i = 0, e = clazz->num_static_fields; i < e; ++i) {
        auto* f = &fields[i];
        if (strcmp(name, f->name) == 0 && strcmp(sig, f->sig) == 0) {
            return f;
        }
    }

    // TODO: Should technically throw NoSuchFieldError.
    fprintf(stderr,
        "Could not find static field %s in class %s. Aborting.\n",
        name, clazz->name);
    abort();
}

const JavaFieldInfo*
GetJavaFieldInfo(jclass cls, const char* name) {
    auto* clazz = classes.at(cls);
    auto* fields = clazz->fields;
    for (int32_t i = 0, e = clazz->num_fields; i < e; ++i) {
        auto* f = &fields[i];
        if (strcmp(name, f->name) == 0) {
            return f;
        }
    }

    // TODO: Should technically throw NoSuchFieldError.
    fprintf(stderr,
        "Could not find field %s in class %s. Aborting.\n",
        name, clazz->name);
    abort();
}

const std::pair<JavaMethodInfo*,int32_t>
TryGetJavaMethodInfo(jclass cls, const char* name, const char* sig, bool search_super) {
    auto* clazz = classes.at(cls);
    auto* methods = clazz->methods;
    for (int32_t i = 0, e = clazz->num_methods; i < e; ++i) {
        auto* m = &methods[i];
        if (strcmp(name, m->name) == 0 && strcmp(sig, m->sig) == 0) {
	  return std::pair<JavaMethodInfo*,int32_t>(m,i);
        }
    }

    // Recurse to super class.
    if (search_super) {
      if  (auto super_ptr = clazz->super_ptr) {
        // TODO: Technically might not want to recurse for 'private' methods.
	return TryGetJavaMethodInfo(*super_ptr, name, sig, true);
      }
    }
    return std::pair<JavaMethodInfo*,int32_t>(nullptr,-1);
}

const std::pair<JavaMethodInfo*,int32_t>
GetJavaMethodInfo(jclass cls, const char* name, const char* sig) {
  return TryGetJavaMethodInfo(cls, name, sig, true);
}

const std::pair<JavaMethodInfo*, int32_t>
GetJavaStaticMethodInfo(jclass cls, const char* name, const char* sig) {
  return TryGetJavaMethodInfo(cls, name, sig, false);
}

//java.lang.Object -> Polyglot_java_lang_Object_load_class
#define LOADER_PREFIX "Polyglot_"
#define LOADER_PREFIX_LEN 9
#define LOADER_SUFFIX "_load_class"
#define LOADER_SUFFIX_LEN 10
#define LOADER_NAME_CHARS (LOADER_PREFIX_LEN + LOADER_SUFFIX_LEN)

typedef jclass (*class_loader)();

jclass
LoadJavaClassFromLib(const char* name) {
 int name_len = strlen(name);
 int new_len = name_len  + LOADER_NAME_CHARS + 1;
 char class_load_name[new_len];
 strcpy(class_load_name, LOADER_PREFIX);
 int i = 0;
 for (; i < name_len; i++) {
   char next_char = name[i];
   class_load_name[LOADER_PREFIX_LEN + i] = (next_char == '.' || next_char == '/') ? '_' : next_char;
 }
 class_load_name[LOADER_PREFIX_LEN + i] = '\0';
 strcat(class_load_name, LOADER_SUFFIX);
 auto class_load_func = reinterpret_cast<class_loader>(dlsym(RTLD_DEFAULT, class_load_name));
 if (class_load_func != NULL) {
   return class_load_func();
 } else {
   return NULL;
 }
}
