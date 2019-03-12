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
#include "jvm.h"
#include "class.h"
#include "rep.h"
#include "object_array.h"

#define MEMCPY(a,b,c) memcpy((void *) a, (void *) b, c)
static constexpr bool kDebug = false;
// static constexpr bool kDebug = true;

#define PRIM_CLASS(prim, klass) prim##klass
#define PRIM_CLASS_DEF(prim) static JClassRep* PRIM_CLASS(prim, Klass);

PRIM_CLASS_DEF(int)
PRIM_CLASS_DEF(short)
PRIM_CLASS_DEF(byte)
PRIM_CLASS_DEF(long)
PRIM_CLASS_DEF(float)
PRIM_CLASS_DEF(double)
PRIM_CLASS_DEF(char)
PRIM_CLASS_DEF(boolean)
PRIM_CLASS_DEF(void)

#define PRIM_NAME_CHECK(name, prim)		\
  if (strcmp(name, prim) == 0) {

#define PRIM_NAME_TO_CLASS(name, cname, prim)		\
  PRIM_NAME_CHECK(name, cname)				\
  return PRIM_CLASS(prim, Klass)->Wrap();

#define PRIM_IS_KLASS(cls, prim) \
  if (Unwrap(cls) == PRIM_CLASS(prim, Klass)) {	\
    return true;

#define PRIM_KLASS_SIZE(cls, prim, size)		\
  if (Unwrap(cls) == PRIM_CLASS(prim, Klass)) { \
    return size;

// dw475 TODO correctly set some fields
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
    newInfo->obj_size = classSize;       \
    newInfo->super_ptr = NULL;            \
    newInfo->cdv = NULL;                  \
    RegisterJavaClass(PRIM_CLASS(prim, Klass)->Wrap(), newInfo);	\
  } else ((void) 0)

// just copy over DV
// dw475 TODO should copy over sync vars
#define REGISTER_PRIM_CLASS(prim) \
  PRIM_CLASS(prim, Klass) = (JClassRep*)malloc(classSize); \
  memcpy(PRIM_CLASS(prim, Klass), globalArrayKlass, sizeof(JClassRep)); \
  Polyglot_native_##prim = reinterpret_cast<jclass>(PRIM_CLASS(prim, Klass)); \
  PRIM_REGISTER(prim);

static bool primKlassInit = false;
static int classSize = 0;

// Define class references for JLang compiler
jclass Polyglot_native_int;
jclass Polyglot_native_byte;
jclass Polyglot_native_short;
jclass Polyglot_native_long;
jclass Polyglot_native_float;
jclass Polyglot_native_double;
jclass Polyglot_native_char;
jclass Polyglot_native_boolean;
jclass Polyglot_native_void;

// For simplicity we store class information in a map.
// If we find this to be too slow, we could allocate extra memory for
// class objects and store the information inline with each instance.
static std::unordered_map<jclass, const JavaClassInfo*> classes;
static std::unordered_map<std::string, const jclass> cnames;

extern "C" {

// invoked by JLang compiler to intern string literals when they are loaded
void InternStringLit(jstring str) {
  *str = *internJString(str);
}

/**
 * Register a java class where cls points to the class object and
 * info points to the info object for that class
 */
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
    // printf("loaded class %s\n", info->name);

    assert(classes.count(cls) == 0 && "Java class was loaded twice!");
    classes.emplace(cls, info);
    std::string cname(info->name);
    // if (strstr(info->name, "FieldReflection") != NULL) {
    //   printf("reg name: %s\n", info->name);
    // }
    cnames.emplace(cname, cls);
}

} // extern "C"

/**
 * Returns true if the class name in signature format is
 * an array class name.
 * This assumes char* is non-null, C-string with len > 0.
 * Examples:
 *  "I" returns false
 *  "[I" returns true
 *  "Ljava/lang/String;" returns false
 */
bool isArrayClassName(const char* name) {
  return name[0] == '[';
}

/**
 * Returns true if cls is an array class
 */
bool isArrayClass(jclass cls) {
  auto cinfo = GetJavaClassInfo(cls);
  if (cinfo == NULL) {
    return JNI_FALSE;
  } else {
    return isArrayClassName(cinfo->name);
  }
}

/**
 * Returns the name of the component of the given name.
 * For example, if name is [[[I, this will return [[I.
 * This assumes char* is non-null, C-string with len >0
 */
const char* getComponentName(const char* name) {
  return &(name[1]);
}

/**
 * Initialize an array class with the given name (should be
 * a type signature).
 * This assumes char* is non-null, C-string with len > 0.
 * It also assumes that it has not been initialized.
 * Returns the newly created array class
 */
const jclass initArrayClass(const char* name) {
  int jclass_size = classSize;
  if (jclass_size == 0) {
    printf("WARNING: class size not yet initialized\n");
    jclass_size = sizeof(JClassRep);
  }
  jclass globalArrayKlass = getArrayKlass();
  jclass newKlazz = (jclass)malloc(jclass_size);
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

bool registeringClass = false;

/**
 * Register the primative classes
 */
const void RegisterPrimitiveClasses() {
  // avoid reentrance when registering the Class class
  // could avoid this by always loading the Class class in main
  // but this preserves laziness and appears to be same
  if (registeringClass) return;

  jclass globalArrayKlass = getArrayKlass();

  jclass ClassClass = NULL;
  try {
    ClassClass = cnames.at(std::string("java.lang.Class"));
  } catch (const std::out_of_range& oor) {
    registeringClass = true;
    ClassClass = LoadJavaClassFromLib("java.lang.Class");
    registeringClass = false;
  }
  const JavaClassInfo* cinfo = GetJavaClassInfo(ClassClass);
  classSize = cinfo->obj_size;

  REGISTER_PRIM_CLASS(int)

  REGISTER_PRIM_CLASS(byte)

  REGISTER_PRIM_CLASS(short)

  REGISTER_PRIM_CLASS(long)

  REGISTER_PRIM_CLASS(float)

  REGISTER_PRIM_CLASS(double)

  REGISTER_PRIM_CLASS(char)

  REGISTER_PRIM_CLASS(boolean)

  REGISTER_PRIM_CLASS(void)
}

/**
 * Returns the primative class object for the given name
 * This assumes char* is non-null, C-string
 */
jclass primitiveComponentNameToClass(const char* name) {
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

/**
 * Returns true of the given class object points to a
 * primative class object
 */
bool isPrimitiveClass(jclass cls) {
  PRIM_IS_KLASS(cls, int)
  } else PRIM_IS_KLASS(cls, byte)
  } else PRIM_IS_KLASS(cls, short)
  } else PRIM_IS_KLASS(cls, long)
  } else PRIM_IS_KLASS(cls, float)
  } else PRIM_IS_KLASS(cls, double)
  } else PRIM_IS_KLASS(cls, char)
  } else PRIM_IS_KLASS(cls, boolean)
  } else PRIM_IS_KLASS(cls, void)
  } else {
    return false;
  }
}

/**
 * Returns the number of bytes used to store the given type in an array
 */
int arrayRepSize(jclass cls) {
  PRIM_KLASS_SIZE(cls, int, sizeof(int))
  } else PRIM_KLASS_SIZE(cls, byte, sizeof(char))
  } else PRIM_KLASS_SIZE(cls, short, sizeof(short))	
  } else PRIM_KLASS_SIZE(cls, long, sizeof(long))
  } else PRIM_KLASS_SIZE(cls, float, sizeof(float))
  } else PRIM_KLASS_SIZE(cls, double, sizeof(double))
  } else PRIM_KLASS_SIZE(cls, char, sizeof(char))
  } else PRIM_KLASS_SIZE(cls, boolean, sizeof(char))
  } else PRIM_KLASS_SIZE(cls, void, sizeof(void*))
  } else {
     return sizeof(void*);
  }
}
/**
 * Returns the class info object for the given java class object
 */
const JavaClassInfo* GetJavaClassInfo(jclass cls) {
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

/**
 * Returns the java class object for the given path
 * relative to the class path if the class has been loaded.
 * Example: java/lang/Class returns the Class class object.
 */
const jclass GetJavaClassFromPathName(const char* name) {
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

/**
 * Returns the java class object for the given class name.
 * If the class has not been loaded yet and is not an array
 * class, this function will return null.
 * Example: java.lang.Class returns the Class class object
 */
const jclass GetJavaClassFromName(const char* name) {
  // printf("get from name: %s\n", name);
  if (!primKlassInit) {
    RegisterPrimitiveClasses();
    primKlassInit = true;
  }
  try {
    return cnames.at(std::string(name));
  } catch (const std::out_of_range& oor) {
    if (isArrayClassName(name)) {
      // printf("init array class: %s\n", name);
      return initArrayClass(name);
    } else {
      return NULL;
    }
  }
}

/**
 * Returns the class inside the given array class's array.
 * If the given class is not an array class, this function
 * returns null.
 */
jclass GetComponentClass(jclass cls) {
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

/**
 * Return the field information for the given class's static field
 */
const JavaStaticFieldInfo* GetJavaStaticFieldInfo(jclass cls, const char* name, const char* sig) {
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

/**
 * Return the field information for the given class's field
 */
const JavaFieldInfo* GetJavaFieldInfo(jclass cls, const char* name) {
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

/**
 * Load a class with the given name into the executable.
 * The class name can be in the format java/lang/Class or
 * java.lang.Class
 */
jclass LoadJavaClassFromLib(const char* name) {
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
