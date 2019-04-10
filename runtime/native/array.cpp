#include "array.h"

extern "C" {
  extern void Polyglot_jlang_runtime_Array_load_class();
  extern DispatchVector Polyglot_jlang_runtime_Array_cdv;
}

DispatchVector* __globalRuntimeArrayCdv = nullptr;

DispatchVector* getRuntimeArrayCdv() {
    if (__globalRuntimeArrayCdv == nullptr) {
        Polyglot_jlang_runtime_Array_load_class();
        __globalRuntimeArrayCdv = &Polyglot_jlang_runtime_Array_cdv;
    }
    return __globalRuntimeArrayCdv;
}