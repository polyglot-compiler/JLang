#include "jni.h"

#include "factory.h"

// The name must match that used in polyllvm.runtime.Factory,
// and the mangling and calling conventions must match those used by PolyLLVM.
extern "C" {

jbooleanArray Polyglot_polyllvm_runtime_Factory_BooleanArray__I(jint);
jbyteArray    Polyglot_polyllvm_runtime_Factory_ByteArray__I   (jint);
jcharArray    Polyglot_polyllvm_runtime_Factory_CharArray__I   (jint);
jshortArray   Polyglot_polyllvm_runtime_Factory_ShortArray__I  (jint);
jintArray     Polyglot_polyllvm_runtime_Factory_IntArray__I    (jint);
jlongArray    Polyglot_polyllvm_runtime_Factory_LongArray__I   (jint);
jfloatArray   Polyglot_polyllvm_runtime_Factory_FloatArray__I  (jint);
jdoubleArray  Polyglot_polyllvm_runtime_Factory_DoubleArray__I (jint);

jstring Polyglot_polyllvm_runtime_Factory_String___3C(jarray);

} // extern "C"

jbooleanArray CreateJavaBooleanArray(jint len) { return Polyglot_polyllvm_runtime_Factory_BooleanArray__I(len); }
jbyteArray    CreateJavaByteArray   (jint len) { return Polyglot_polyllvm_runtime_Factory_ByteArray__I   (len); }
jcharArray    CreateJavaCharArray   (jint len) { return Polyglot_polyllvm_runtime_Factory_CharArray__I   (len); }
jshortArray   CreateJavaShortArray  (jint len) { return Polyglot_polyllvm_runtime_Factory_ShortArray__I  (len); }
jintArray     CreateJavaIntArray    (jint len) { return Polyglot_polyllvm_runtime_Factory_IntArray__I    (len); }
jlongArray    CreateJavaLongArray   (jint len) { return Polyglot_polyllvm_runtime_Factory_LongArray__I   (len); }
jfloatArray   CreateJavaFloatArray  (jint len) { return Polyglot_polyllvm_runtime_Factory_FloatArray__I  (len); }
jdoubleArray  CreateJavaDoubleArray (jint len) { return Polyglot_polyllvm_runtime_Factory_DoubleArray__I (len); }

jstring CreateJavaString(jcharArray chars) {
    return Polyglot_polyllvm_runtime_Factory_String___3C(chars);
}
