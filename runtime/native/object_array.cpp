#include "object_array.h"

extern "C" {
  extern void Polyglot_jlang_runtime_ObjectArray_load_class();
  extern jclass Polyglot_jlang_runtime_ObjectArray_class;
}

// underscore becuase should never be used directly
jclass globalArrayKlass = NULL;

jclass getArrayKlass() {
  if (globalArrayKlass == NULL) {
    // Force this class load function to be called at initialization
    Polyglot_jlang_runtime_ObjectArray_load_class();
    globalArrayKlass = Polyglot_jlang_runtime_ObjectArray_class;
  }
  return globalArrayKlass;
}