#include <jni.h>
#include <cstdio>
#include <cstdlib>
#include "stack_trace.h"

// Compiler-specific representations for Java objects.
#include "rep.h"

[[noreturn]] static void jni_Unimplemented(const char* name) {
  fprintf(stderr,
    "- - - - - - - - - - - - - - - - - - - - - - - - - - -\n"
    "The following JNI method is currently unimplemented:\n"
    "  %s\n"
    "It is defined in " __FILE__ ".\n"
    "Aborting for now.\n"
    "- - - - - - - - - - - - - - - - - - - - - - - - - - -\n"
    , name);
  dump_stack_trace();
  fflush(stderr);
  abort();
}

extern "C" {

jint jni_GetVersion(JNIEnv *env) {
    // This is the correct version for Java SE 7 too.
    return JNI_VERSION_1_6;
}

jclass jni_DefineClass(JNIEnv *env, const char *name, jobject loader, const jbyte *buf, jsize len) {
    jni_Unimplemented("DefineClass");
}

jclass jni_FindClass(JNIEnv *env, const char *name) {
    jni_Unimplemented("FindClass");
}

jmethodID jni_FromReflectedMethod(JNIEnv *env, jobject method) {
    jni_Unimplemented("FromReflectedMethod");
}

jfieldID jni_FromReflectedField(JNIEnv *env, jobject field) {
    jni_Unimplemented("FromReflectedField");
}

jobject jni_ToReflectedMethod(JNIEnv *env, jclass cls, jmethodID methodID, jboolean isStatic) {
    jni_Unimplemented("ToReflectedMethod");
}

jclass jni_GetSuperclass(JNIEnv *env, jclass sub) {
    jni_Unimplemented("GetSuperclass");
}

jboolean jni_IsAssignableFrom(JNIEnv *env, jclass sub, jclass sup) {
    jni_Unimplemented("IsAssignableFrom");
}

jobject jni_ToReflectedField(JNIEnv *env, jclass cls, jfieldID fieldID, jboolean isStatic) {
    jni_Unimplemented("ToReflectedField");
}

jint jni_Throw(JNIEnv *env, jthrowable obj) {
    jni_Unimplemented("Throw");
}

jint jni_ThrowNew(JNIEnv *env, jclass clazz, const char *msg) {
    jni_Unimplemented("ThrowNew");
}

jthrowable jni_ExceptionOccurred(JNIEnv *env) {
    jni_Unimplemented("ExceptionOccurred");
}

void jni_ExceptionDescribe(JNIEnv *env) {
    jni_Unimplemented("ExceptionDescribe");
}

void jni_ExceptionClear(JNIEnv *env) {
    jni_Unimplemented("ExceptionClear");
}

void jni_FatalError(JNIEnv *env, const char *msg) {
    jni_Unimplemented("FatalError");
}

jint jni_PushLocalFrame(JNIEnv *env, jint capacity) {
    jni_Unimplemented("PushLocalFrame");
}

jobject jni_PopLocalFrame(JNIEnv *env, jobject result) {
    jni_Unimplemented("PopLocalFrame");
}

jobject jni_NewGlobalRef(JNIEnv *env, jobject lobj) {
    jni_Unimplemented("NewGlobalRef");
}

void jni_DeleteGlobalRef(JNIEnv *env, jobject gref) {
    jni_Unimplemented("DeleteGlobalRef");
}

void jni_DeleteLocalRef(JNIEnv *env, jobject obj) {
    jni_Unimplemented("DeleteLocalRef");
}

jboolean jni_IsSameObject(JNIEnv *env, jobject obj1, jobject obj2) {
    return obj1 == obj2;
}

jobject jni_NewLocalRef(JNIEnv *env, jobject ref) {
    jni_Unimplemented("NewLocalRef");
}

jint jni_EnsureLocalCapacity(JNIEnv *env, jint capacity) {
    jni_Unimplemented("EnsureLocalCapacity");
}

jobject jni_AllocObject(JNIEnv *env, jclass clazz) {
    jni_Unimplemented("AllocObject");
}

jobject jni_NewObject(JNIEnv *env, jclass clazz, jmethodID methodID, ...) {
    jni_Unimplemented("NewObject");
}

jobject jni_NewObjectV(JNIEnv *env, jclass clazz, jmethodID methodID, va_list args) {
    jni_Unimplemented("NewObjectV");
}

jobject jni_NewObjectA(JNIEnv *env, jclass clazz, jmethodID methodID, const jvalue *args) {
    jni_Unimplemented("NewObjectA");
}

jclass jni_GetObjectClass(JNIEnv *env, jobject obj) {
    jni_Unimplemented("GetObjectClass");
}

jboolean jni_IsInstanceOf(JNIEnv *env, jobject obj, jclass clazz) {
    jni_Unimplemented("IsInstanceOf");
}

jmethodID jni_GetMethodID(JNIEnv *env, jclass clazz, const char *name, const char *sig) {
    jni_Unimplemented("GetMethodID");
}

jobject jni_CallObjectMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {
    jni_Unimplemented("CallObjectMethod");
}

jobject jni_CallObjectMethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args) {
    jni_Unimplemented("CallObjectMethodV");
}

jobject jni_CallObjectMethodA(JNIEnv *env, jobject obj, jmethodID methodID, const jvalue* args) {
    jni_Unimplemented("CallObjectMethodA");
}

jboolean jni_CallBooleanMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {
    jni_Unimplemented("CallBooleanMethod");
}

jboolean jni_CallBooleanMethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args) {
    jni_Unimplemented("CallBooleanMethodV");
}

jboolean jni_CallBooleanMethodA(JNIEnv *env, jobject obj, jmethodID methodID, const jvalue* args) {
    jni_Unimplemented("CallBooleanMethodA");
}

jbyte jni_CallByteMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {
    jni_Unimplemented("CallByteMethod");
}

jbyte jni_CallByteMethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args) {
    jni_Unimplemented("CallByteMethodV");
}

jbyte jni_CallByteMethodA(JNIEnv *env, jobject obj, jmethodID methodID, const jvalue *args) {
    jni_Unimplemented("CallByteMethodA");
}

jchar jni_CallCharMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {
    jni_Unimplemented("CallCharMethod");
}

jchar jni_CallCharMethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args) {
    jni_Unimplemented("CallCharMethodV");
}

jchar jni_CallCharMethodA(JNIEnv *env, jobject obj, jmethodID methodID, const jvalue *args) {
    jni_Unimplemented("CallCharMethodA");
}

jshort jni_CallShortMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {
    jni_Unimplemented("CallShortMethod");
}

jshort jni_CallShortMethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args) {
    jni_Unimplemented("CallShortMethodV");
}

jshort jni_CallShortMethodA(JNIEnv *env, jobject obj, jmethodID methodID, const jvalue *args) {
    jni_Unimplemented("CallShortMethodA");
}

jint jni_CallIntMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {
    jni_Unimplemented("CallIntMethod");
}

jint jni_CallIntMethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args) {
    jni_Unimplemented("CallIntMethodV");
}

jint jni_CallIntMethodA(JNIEnv *env, jobject obj, jmethodID methodID, const jvalue *args) {
    jni_Unimplemented("CallIntMethodA");
}

jlong jni_CallLongMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {
    jni_Unimplemented("CallLongMethod");
}

jlong jni_CallLongMethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args) {
    jni_Unimplemented("CallLongMethodV");
}

jlong jni_CallLongMethodA(JNIEnv *env, jobject obj, jmethodID methodID, const jvalue *args) {
    jni_Unimplemented("CallLongMethodA");
}

jfloat jni_CallFloatMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {
    jni_Unimplemented("CallFloatMethod");
}

jfloat jni_CallFloatMethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args) {
    jni_Unimplemented("CallFloatMethodV");
}

jfloat jni_CallFloatMethodA(JNIEnv *env, jobject obj, jmethodID methodID, const jvalue *args) {
    jni_Unimplemented("CallFloatMethodA");
}

jdouble jni_CallDoubleMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {
    jni_Unimplemented("CallDoubleMethod");
}

jdouble jni_CallDoubleMethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args) {
    jni_Unimplemented("CallDoubleMethodV");
}

jdouble jni_CallDoubleMethodA(JNIEnv *env, jobject obj, jmethodID methodID, const jvalue *args) {
    jni_Unimplemented("CallDoubleMethodA");
}

void jni_CallVoidMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {
    jni_Unimplemented("CallVoidMethod");
}

void jni_CallVoidMethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args) {
    jni_Unimplemented("CallVoidMethodV");
}

void jni_CallVoidMethodA(JNIEnv *env, jobject obj, jmethodID methodID, const jvalue* args) {
    jni_Unimplemented("CallVoidMethodA");
}

jobject jni_CallNonvirtualObjectMethod(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, ...) {
    jni_Unimplemented("CallNonvirtualObjectMethod");
}

jobject jni_CallNonvirtualObjectMethodV(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, va_list args) {
    jni_Unimplemented("CallNonvirtualObjectMethodV");
}

jobject jni_CallNonvirtualObjectMethodA(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, const jvalue* args) {
    jni_Unimplemented("CallNonvirtualObjectMethodA");
}

jboolean jni_CallNonvirtualBooleanMethod(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, ...) {
    jni_Unimplemented("CallNonvirtualBooleanMethod");
}

jboolean jni_CallNonvirtualBooleanMethodV(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, va_list args) {
    jni_Unimplemented("CallNonvirtualBooleanMethodV");
}

jboolean jni_CallNonvirtualBooleanMethodA(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, const jvalue* args) {
    jni_Unimplemented("CallNonvirtualBooleanMethodA");
}

jbyte jni_CallNonvirtualByteMethod(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, ...) {
    jni_Unimplemented("CallNonvirtualByteMethod");
}

jbyte jni_CallNonvirtualByteMethodV(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, va_list args) {
    jni_Unimplemented("CallNonvirtualByteMethodV");
}

jbyte jni_CallNonvirtualByteMethodA(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, const jvalue *args) {
    jni_Unimplemented("CallNonvirtualByteMethodA");
}

jchar jni_CallNonvirtualCharMethod(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, ...) {
    jni_Unimplemented("CallNonvirtualCharMethod");
}

jchar jni_CallNonvirtualCharMethodV(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, va_list args) {
    jni_Unimplemented("CallNonvirtualCharMethodV");
}

jchar jni_CallNonvirtualCharMethodA(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, const jvalue *args) {
    jni_Unimplemented("CallNonvirtualCharMethodA");
}

jshort jni_CallNonvirtualShortMethod(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, ...) {
    jni_Unimplemented("CallNonvirtualShortMethod");
}

jshort jni_CallNonvirtualShortMethodV(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, va_list args) {
    jni_Unimplemented("CallNonvirtualShortMethodV");
}

jshort jni_CallNonvirtualShortMethodA(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, const jvalue *args) {
    jni_Unimplemented("CallNonvirtualShortMethodA");
}

jint jni_CallNonvirtualIntMethod(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, ...) {
    jni_Unimplemented("CallNonvirtualIntMethod");
}

jint jni_CallNonvirtualIntMethodV(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, va_list args) {
    jni_Unimplemented("CallNonvirtualIntMethodV");
}

jint jni_CallNonvirtualIntMethodA(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, const jvalue *args) {
    jni_Unimplemented("CallNonvirtualIntMethodA");
}

jlong jni_CallNonvirtualLongMethod(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, ...) {
    jni_Unimplemented("CallNonvirtualLongMethod");
}

jlong jni_CallNonvirtualLongMethodV(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, va_list args) {
    jni_Unimplemented("CallNonvirtualLongMethodV");
}

jlong jni_CallNonvirtualLongMethodA(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, const jvalue *args) {
    jni_Unimplemented("CallNonvirtualLongMethodA");
}

jfloat jni_CallNonvirtualFloatMethod(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, ...) {
    jni_Unimplemented("CallNonvirtualFloatMethod");
}

jfloat jni_CallNonvirtualFloatMethodV(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, va_list args) {
    jni_Unimplemented("CallNonvirtualFloatMethodV");
}

jfloat jni_CallNonvirtualFloatMethodA(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, const jvalue *args) {
    jni_Unimplemented("CallNonvirtualFloatMethodA");
}

jdouble jni_CallNonvirtualDoubleMethod(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, ...) {
    jni_Unimplemented("CallNonvirtualDoubleMethod");
}

jdouble jni_CallNonvirtualDoubleMethodV(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, va_list args) {
    jni_Unimplemented("CallNonvirtualDoubleMethodV");
}

jdouble jni_CallNonvirtualDoubleMethodA(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, const jvalue *args) {
    jni_Unimplemented("CallNonvirtualDoubleMethodA");
}

void jni_CallNonvirtualVoidMethod(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, ...) {
    jni_Unimplemented("CallNonvirtualVoidMethod");
}

void jni_CallNonvirtualVoidMethodV(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, va_list args) {
    jni_Unimplemented("CallNonvirtualVoidMethodV");
}

void jni_CallNonvirtualVoidMethodA(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, const jvalue* args) {
    jni_Unimplemented("CallNonvirtualVoidMethodA");
}

jfieldID jni_GetFieldID(JNIEnv *env, jclass clazz, const char *name, const char *sig) {
    jni_Unimplemented("GetFieldID");
}

jobject jni_GetObjectField(JNIEnv *env, jobject obj, jfieldID fieldID) {
    jni_Unimplemented("GetObjectField");
}

jboolean jni_GetBooleanField(JNIEnv *env, jobject obj, jfieldID fieldID) {
    jni_Unimplemented("GetBooleanField");
}

jbyte jni_GetByteField(JNIEnv *env, jobject obj, jfieldID fieldID) {
    jni_Unimplemented("GetByteField");
}

jchar jni_GetCharField(JNIEnv *env, jobject obj, jfieldID fieldID) {
    jni_Unimplemented("GetCharField");
}

jshort jni_GetShortField(JNIEnv *env, jobject obj, jfieldID fieldID) {
    jni_Unimplemented("GetShortField");
}

jint jni_GetIntField(JNIEnv *env, jobject obj, jfieldID fieldID) {
    jni_Unimplemented("GetIntField");
}

jlong jni_GetLongField(JNIEnv *env, jobject obj, jfieldID fieldID) {
    jni_Unimplemented("GetLongField");
}

jfloat jni_GetFloatField(JNIEnv *env, jobject obj, jfieldID fieldID) {
    jni_Unimplemented("GetFloatField");
}

jdouble jni_GetDoubleField(JNIEnv *env, jobject obj, jfieldID fieldID) {
    jni_Unimplemented("GetDoubleField");
}

void jni_SetObjectField(JNIEnv *env, jobject obj, jfieldID fieldID, jobject val) {
    jni_Unimplemented("SetObjectField");
}

void jni_SetBooleanField(JNIEnv *env, jobject obj, jfieldID fieldID, jboolean val) {
    jni_Unimplemented("SetBooleanField");
}

void jni_SetByteField(JNIEnv *env, jobject obj, jfieldID fieldID, jbyte val) {
    jni_Unimplemented("SetByteField");
}

void jni_SetCharField(JNIEnv *env, jobject obj, jfieldID fieldID, jchar val) {
    jni_Unimplemented("SetCharField");
}

void jni_SetShortField(JNIEnv *env, jobject obj, jfieldID fieldID, jshort val) {
    jni_Unimplemented("SetShortField");
}

void jni_SetIntField(JNIEnv *env, jobject obj, jfieldID fieldID, jint val) {
    jni_Unimplemented("SetIntField");
}

void jni_SetLongField(JNIEnv *env, jobject obj, jfieldID fieldID, jlong val) {
    jni_Unimplemented("SetLongField");
}

void jni_SetFloatField(JNIEnv *env, jobject obj, jfieldID fieldID, jfloat val) {
    jni_Unimplemented("SetFloatField");
}

void jni_SetDoubleField(JNIEnv *env, jobject obj, jfieldID fieldID, jdouble val) {
    jni_Unimplemented("SetDoubleField");
}

jmethodID jni_GetStaticMethodID(JNIEnv *env, jclass clazz, const char *name, const char *sig) {
    jni_Unimplemented("GetStaticMethodID");
}

jobject jni_CallStaticObjectMethod(JNIEnv *env, jclass clazz, jmethodID methodID, ...) {
    jni_Unimplemented("CallStaticObjectMethod");
}

jobject jni_CallStaticObjectMethodV(JNIEnv *env, jclass clazz, jmethodID methodID, va_list args) {
    jni_Unimplemented("CallStaticObjectMethodV");
}

jobject jni_CallStaticObjectMethodA(JNIEnv *env, jclass clazz, jmethodID methodID, const jvalue *args) {
    jni_Unimplemented("CallStaticObjectMethodA");
}

jboolean jni_CallStaticBooleanMethod(JNIEnv *env, jclass clazz, jmethodID methodID, ...) {
    jni_Unimplemented("CallStaticBooleanMethod");
}

jboolean jni_CallStaticBooleanMethodV(JNIEnv *env, jclass clazz, jmethodID methodID, va_list args) {
    jni_Unimplemented("CallStaticBooleanMethodV");
}

jboolean jni_CallStaticBooleanMethodA(JNIEnv *env, jclass clazz, jmethodID methodID, const jvalue *args) {
    jni_Unimplemented("CallStaticBooleanMethodA");
}

jbyte jni_CallStaticByteMethod(JNIEnv *env, jclass clazz, jmethodID methodID, ...) {
    jni_Unimplemented("CallStaticByteMethod");
}

jbyte jni_CallStaticByteMethodV(JNIEnv *env, jclass clazz, jmethodID methodID, va_list args) {
    jni_Unimplemented("CallStaticByteMethodV");
}

jbyte jni_CallStaticByteMethodA(JNIEnv *env, jclass clazz, jmethodID methodID, const jvalue *args) {
    jni_Unimplemented("CallStaticByteMethodA");
}

jchar jni_CallStaticCharMethod(JNIEnv *env, jclass clazz, jmethodID methodID, ...) {
    jni_Unimplemented("CallStaticCharMethod");
}

jchar jni_CallStaticCharMethodV(JNIEnv *env, jclass clazz, jmethodID methodID, va_list args) {
    jni_Unimplemented("CallStaticCharMethodV");
}

jchar jni_CallStaticCharMethodA(JNIEnv *env, jclass clazz, jmethodID methodID, const jvalue *args) {
    jni_Unimplemented("CallStaticCharMethodA");
}

jshort jni_CallStaticShortMethod(JNIEnv *env, jclass clazz, jmethodID methodID, ...) {
    jni_Unimplemented("CallStaticShortMethod");
}

jshort jni_CallStaticShortMethodV(JNIEnv *env, jclass clazz, jmethodID methodID, va_list args) {
    jni_Unimplemented("CallStaticShortMethodV");
}

jshort jni_CallStaticShortMethodA(JNIEnv *env, jclass clazz, jmethodID methodID, const jvalue *args) {
    jni_Unimplemented("CallStaticShortMethodA");
}

jint jni_CallStaticIntMethod(JNIEnv *env, jclass clazz, jmethodID methodID, ...) {
    jni_Unimplemented("CallStaticIntMethod");
}

jint jni_CallStaticIntMethodV(JNIEnv *env, jclass clazz, jmethodID methodID, va_list args) {
    jni_Unimplemented("CallStaticIntMethodV");
}

jint jni_CallStaticIntMethodA(JNIEnv *env, jclass clazz, jmethodID methodID, const jvalue *args) {
    jni_Unimplemented("CallStaticIntMethodA");
}

jlong jni_CallStaticLongMethod(JNIEnv *env, jclass clazz, jmethodID methodID, ...) {
    jni_Unimplemented("CallStaticLongMethod");
}

jlong jni_CallStaticLongMethodV(JNIEnv *env, jclass clazz, jmethodID methodID, va_list args) {
    jni_Unimplemented("CallStaticLongMethodV");
}

jlong jni_CallStaticLongMethodA(JNIEnv *env, jclass clazz, jmethodID methodID, const jvalue *args) {
    jni_Unimplemented("CallStaticLongMethodA");
}

jfloat jni_CallStaticFloatMethod(JNIEnv *env, jclass clazz, jmethodID methodID, ...) {
    jni_Unimplemented("CallStaticFloatMethod");
}

jfloat jni_CallStaticFloatMethodV(JNIEnv *env, jclass clazz, jmethodID methodID, va_list args) {
    jni_Unimplemented("CallStaticFloatMethodV");
}

jfloat jni_CallStaticFloatMethodA(JNIEnv *env, jclass clazz, jmethodID methodID, const jvalue *args) {
    jni_Unimplemented("CallStaticFloatMethodA");
}

jdouble jni_CallStaticDoubleMethod(JNIEnv *env, jclass clazz, jmethodID methodID, ...) {
    jni_Unimplemented("CallStaticDoubleMethod");
}

jdouble jni_CallStaticDoubleMethodV(JNIEnv *env, jclass clazz, jmethodID methodID, va_list args) {
    jni_Unimplemented("CallStaticDoubleMethodV");
}

jdouble jni_CallStaticDoubleMethodA(JNIEnv *env, jclass clazz, jmethodID methodID, const jvalue *args) {
    jni_Unimplemented("CallStaticDoubleMethodA");
}

void jni_CallStaticVoidMethod(JNIEnv *env, jclass cls, jmethodID methodID, ...) {
    jni_Unimplemented("CallStaticVoidMethod");
}

void jni_CallStaticVoidMethodV(JNIEnv *env, jclass cls, jmethodID methodID, va_list args) {
    jni_Unimplemented("CallStaticVoidMethodV");
}

void jni_CallStaticVoidMethodA(JNIEnv *env, jclass cls, jmethodID methodID, const jvalue* args) {
    jni_Unimplemented("CallStaticVoidMethodA");
}

jfieldID jni_GetStaticFieldID(JNIEnv *env, jclass clazz, const char *name, const char *sig) {
    jni_Unimplemented("GetStaticFieldID");
}

jobject jni_GetStaticObjectField(JNIEnv *env, jclass clazz, jfieldID fieldID) {
    jni_Unimplemented("GetStaticObjectField");
}

jboolean jni_GetStaticBooleanField(JNIEnv *env, jclass clazz, jfieldID fieldID) {
    jni_Unimplemented("GetStaticBooleanField");
}

jbyte jni_GetStaticByteField(JNIEnv *env, jclass clazz, jfieldID fieldID) {
    jni_Unimplemented("GetStaticByteField");
}

jchar jni_GetStaticCharField(JNIEnv *env, jclass clazz, jfieldID fieldID) {
    jni_Unimplemented("GetStaticCharField");
}

jshort jni_GetStaticShortField(JNIEnv *env, jclass clazz, jfieldID fieldID) {
    jni_Unimplemented("GetStaticShortField");
}

jint jni_GetStaticIntField(JNIEnv *env, jclass clazz, jfieldID fieldID) {
    jni_Unimplemented("GetStaticIntField");
}

jlong jni_GetStaticLongField(JNIEnv *env, jclass clazz, jfieldID fieldID) {
    jni_Unimplemented("GetStaticLongField");
}

jfloat jni_GetStaticFloatField(JNIEnv *env, jclass clazz, jfieldID fieldID) {
    jni_Unimplemented("GetStaticFloatField");
}

jdouble jni_GetStaticDoubleField(JNIEnv *env, jclass clazz, jfieldID fieldID) {
    jni_Unimplemented("GetStaticDoubleField");
}

void jni_SetStaticObjectField(JNIEnv *env, jclass clazz, jfieldID fieldID, jobject value) {
    jni_Unimplemented("SetStaticObjectField");
}

void jni_SetStaticBooleanField(JNIEnv *env, jclass clazz, jfieldID fieldID, jboolean value) {
    jni_Unimplemented("SetStaticBooleanField");
}

void jni_SetStaticByteField(JNIEnv *env, jclass clazz, jfieldID fieldID, jbyte value) {
    jni_Unimplemented("SetStaticByteField");
}

void jni_SetStaticCharField(JNIEnv *env, jclass clazz, jfieldID fieldID, jchar value) {
    jni_Unimplemented("SetStaticCharField");
}

void jni_SetStaticShortField(JNIEnv *env, jclass clazz, jfieldID fieldID, jshort value) {
    jni_Unimplemented("SetStaticShortField");
}

void jni_SetStaticIntField(JNIEnv *env, jclass clazz, jfieldID fieldID, jint value) {
    jni_Unimplemented("SetStaticIntField");
}

void jni_SetStaticLongField(JNIEnv *env, jclass clazz, jfieldID fieldID, jlong value) {
    jni_Unimplemented("SetStaticLongField");
}

void jni_SetStaticFloatField(JNIEnv *env, jclass clazz, jfieldID fieldID, jfloat value) {
    jni_Unimplemented("SetStaticFloatField");
}

void jni_SetStaticDoubleField(JNIEnv *env, jclass clazz, jfieldID fieldID, jdouble value) {
    jni_Unimplemented("SetStaticDoubleField");
}

jstring jni_NewString(JNIEnv *env, const jchar *unicode, jsize len) {
    jni_Unimplemented("NewString");
}

jsize jni_GetStringLength(JNIEnv *env, jstring str) {
    jni_Unimplemented("GetStringLength");
}

const jchar *jni_GetStringChars(JNIEnv *env, jstring str, jboolean *isCopy) {
    jni_Unimplemented("GetStringChars");
}

void jni_ReleaseStringChars(JNIEnv *env, jstring str, const jchar *chars) {
    jni_Unimplemented("ReleaseStringChars");
}

jstring jni_NewStringUTF(JNIEnv *env, const char *utf) {
    jni_Unimplemented("NewStringUTF");
}

jsize jni_GetStringUTFLength(JNIEnv *env, jstring str) {
    jni_Unimplemented("GetStringUTFLength");
}

const char* jni_GetStringUTFChars(JNIEnv *env, jstring str, jboolean *isCopy) {
    jni_Unimplemented("GetStringUTFChars");
}

void jni_ReleaseStringUTFChars(JNIEnv *env, jstring str, const char* chars) {
    jni_Unimplemented("ReleaseStringUTFChars");
}

jsize jni_GetArrayLength(JNIEnv *env, jarray array) {
    return Unwrap(array)->Length();
}

jobjectArray jni_NewObjectArray(JNIEnv *env, jsize len, jclass clazz, jobject init) {
    jni_Unimplemented("NewObjectArray");
}

jobject jni_GetObjectArrayElement(JNIEnv *env, jobjectArray array, jsize index) {
    jobject* data = static_cast<jobject*>(Unwrap(array)->Data());
    return data[index];
}

void jni_SetObjectArrayElement(JNIEnv *env, jobjectArray array, jsize index, jobject val) {
    jobject* data = static_cast<jobject*>(Unwrap(array)->Data());
    data[index] = val;
}

jbooleanArray jni_NewBooleanArray(JNIEnv *env, jsize len) {
    jni_Unimplemented("NewBooleanArray");
}

jbyteArray jni_NewByteArray(JNIEnv *env, jsize len) {
    jni_Unimplemented("NewByteArray");
}

jcharArray jni_NewCharArray(JNIEnv *env, jsize len) {
    jni_Unimplemented("NewCharArray");
}

jshortArray jni_NewShortArray(JNIEnv *env, jsize len) {
    jni_Unimplemented("NewShortArray");
}

jintArray jni_NewIntArray(JNIEnv *env, jsize len) {
    jni_Unimplemented("NewIntArray");
}

jlongArray jni_NewLongArray(JNIEnv *env, jsize len) {
    jni_Unimplemented("NewLongArray");
}

jfloatArray jni_NewFloatArray(JNIEnv *env, jsize len) {
    jni_Unimplemented("NewFloatArray");
}

jdoubleArray jni_NewDoubleArray(JNIEnv *env, jsize len) {
    jni_Unimplemented("NewDoubleArray");
}

#define RETURN_ARRAY_DATA_NO_COPY(type) \
    if (isCopy != nullptr) *isCopy = JNI_FALSE; \
    return static_cast<type*>(Unwrap(array)->Data())

jboolean* jni_GetBooleanArrayElements(JNIEnv *env, jbooleanArray array, jboolean *isCopy) {
    RETURN_ARRAY_DATA_NO_COPY(jboolean);
}

jbyte* jni_GetByteArrayElements(JNIEnv *env, jbyteArray array, jboolean *isCopy) {
    RETURN_ARRAY_DATA_NO_COPY(jbyte);
}

jchar* jni_GetCharArrayElements(JNIEnv *env, jcharArray array, jboolean *isCopy) {
    RETURN_ARRAY_DATA_NO_COPY(jchar);
}

jshort* jni_GetShortArrayElements(JNIEnv *env, jshortArray array, jboolean *isCopy) {
    RETURN_ARRAY_DATA_NO_COPY(jshort);
}

jint* jni_GetIntArrayElements(JNIEnv *env, jintArray array, jboolean *isCopy) {
    RETURN_ARRAY_DATA_NO_COPY(jint);
}

jlong* jni_GetLongArrayElements(JNIEnv *env, jlongArray array, jboolean *isCopy) {
    RETURN_ARRAY_DATA_NO_COPY(jlong);
}

jfloat* jni_GetFloatArrayElements(JNIEnv *env, jfloatArray array, jboolean *isCopy) {
    RETURN_ARRAY_DATA_NO_COPY(jfloat);
}

jdouble* jni_GetDoubleArrayElements(JNIEnv *env, jdoubleArray array, jboolean *isCopy) {
    RETURN_ARRAY_DATA_NO_COPY(jdouble);
}

void jni_ReleaseBooleanArrayElements(JNIEnv *env, jbooleanArray array, jboolean *elems, jint mode) {
    // We use a conservative garbage collector, so nothing to do here.
}

void jni_ReleaseByteArrayElements(JNIEnv *env, jbyteArray array, jbyte *elems, jint mode) {
    // We use a conservative garbage collector, so nothing to do here.
}

void jni_ReleaseCharArrayElements(JNIEnv *env, jcharArray array, jchar *elems, jint mode) {
    // We use a conservative garbage collector, so nothing to do here.
}

void jni_ReleaseShortArrayElements(JNIEnv *env, jshortArray array, jshort *elems, jint mode) {
    // We use a conservative garbage collector, so nothing to do here.
}

void jni_ReleaseIntArrayElements(JNIEnv *env, jintArray array, jint *elems, jint mode) {
    // We use a conservative garbage collector, so nothing to do here.
}

void jni_ReleaseLongArrayElements(JNIEnv *env, jlongArray array, jlong *elems, jint mode) {
    // We use a conservative garbage collector, so nothing to do here.
}

void jni_ReleaseFloatArrayElements(JNIEnv *env, jfloatArray array, jfloat *elems, jint mode) {
    // We use a conservative garbage collector, so nothing to do here.
}

void jni_ReleaseDoubleArrayElements(JNIEnv *env, jdoubleArray array, jdouble *elems, jint mode) {
    // We use a conservative garbage collector, so nothing to do here.
}

void jni_GetBooleanArrayRegion(JNIEnv *env, jbooleanArray array, jsize start, jsize l, jboolean *buf) {
    jni_Unimplemented("GetBooleanArrayRegion");
}

void jni_GetByteArrayRegion(JNIEnv *env, jbyteArray array, jsize start, jsize len, jbyte *buf) {
    jni_Unimplemented("GetByteArrayRegion");
}

void jni_GetCharArrayRegion(JNIEnv *env, jcharArray array, jsize start, jsize len, jchar *buf) {
    jni_Unimplemented("GetCharArrayRegion");
}

void jni_GetShortArrayRegion(JNIEnv *env, jshortArray array, jsize start, jsize len, jshort *buf) {
    jni_Unimplemented("GetShortArrayRegion");
}

void jni_GetIntArrayRegion(JNIEnv *env, jintArray array, jsize start, jsize len, jint *buf) {
    jni_Unimplemented("GetIntArrayRegion");
}

void jni_GetLongArrayRegion(JNIEnv *env, jlongArray array, jsize start, jsize len, jlong *buf) {
    jni_Unimplemented("GetLongArrayRegion");
}

void jni_GetFloatArrayRegion(JNIEnv *env, jfloatArray array, jsize start, jsize len, jfloat *buf) {
    jni_Unimplemented("GetFloatArrayRegion");
}

void jni_GetDoubleArrayRegion(JNIEnv *env, jdoubleArray array, jsize start, jsize len, jdouble *buf) {
    jni_Unimplemented("GetDoubleArrayRegion");
}

void jni_SetBooleanArrayRegion(JNIEnv *env, jbooleanArray array, jsize start, jsize l, const jboolean *buf) {
    jni_Unimplemented("SetBooleanArrayRegion");
}

void jni_SetByteArrayRegion(JNIEnv *env, jbyteArray array, jsize start, jsize len, const jbyte *buf) {
    jni_Unimplemented("SetByteArrayRegion");
}

void jni_SetCharArrayRegion(JNIEnv *env, jcharArray array, jsize start, jsize len, const jchar *buf) {
    jni_Unimplemented("SetCharArrayRegion");
}

void jni_SetShortArrayRegion(JNIEnv *env, jshortArray array, jsize start, jsize len, const jshort *buf) {
    jni_Unimplemented("SetShortArrayRegion");
}

void jni_SetIntArrayRegion(JNIEnv *env, jintArray array, jsize start, jsize len, const jint *buf) {
    jni_Unimplemented("SetIntArrayRegion");
}

void jni_SetLongArrayRegion(JNIEnv *env, jlongArray array, jsize start, jsize len, const jlong *buf) {
    jni_Unimplemented("SetLongArrayRegion");
}

void jni_SetFloatArrayRegion(JNIEnv *env, jfloatArray array, jsize start, jsize len, const jfloat *buf) {
    jni_Unimplemented("SetFloatArrayRegion");
}

void jni_SetDoubleArrayRegion(JNIEnv *env, jdoubleArray array, jsize start, jsize len, const jdouble *buf) {
    jni_Unimplemented("SetDoubleArrayRegion");
}

jint jni_RegisterNatives(JNIEnv *env, jclass clazz, const JNINativeMethod *methods, jint nMethods) {
    jni_Unimplemented("RegisterNatives");
}

jint jni_UnregisterNatives(JNIEnv *env, jclass clazz) {
    jni_Unimplemented("UnregisterNatives");
}

jint jni_MonitorEnter(JNIEnv *env, jobject obj) {
    jni_Unimplemented("MonitorEnter");
}

jint jni_MonitorExit(JNIEnv *env, jobject obj) {
    jni_Unimplemented("MonitorExit");
}

jint jni_GetJavaVM(JNIEnv *env, JavaVM **vm) {
    jni_Unimplemented("GetJavaVM");
}

void jni_GetStringRegion(JNIEnv *env, jstring str, jsize start, jsize len, jchar *buf) {
    jni_Unimplemented("GetStringRegion");
}

void jni_GetStringUTFRegion(JNIEnv *env, jstring str, jsize start, jsize len, char *buf) {
    jni_Unimplemented("GetStringUTFRegion");
}

void* jni_GetPrimitiveArrayCritical(JNIEnv *env, jarray array, jboolean *isCopy) {
    jni_Unimplemented("GetPrimitiveArrayCritical");
}

void jni_ReleasePrimitiveArrayCritical(JNIEnv *env, jarray array, void *carray, jint mode) {
    jni_Unimplemented("ReleasePrimitiveArrayCritical");
}

const jchar* jni_GetStringCritical(JNIEnv *env, jstring string, jboolean *isCopy) {
    jni_Unimplemented("GetStringCritical");
}

void jni_ReleaseStringCritical(JNIEnv *env, jstring string, const jchar *cstring) {
    jni_Unimplemented("ReleaseStringCritical");
}

jweak jni_NewWeakGlobalRef(JNIEnv *env, jobject obj) {
    jni_Unimplemented("NewWeakGlobalRef");
}

void jni_DeleteWeakGlobalRef(JNIEnv *env, jweak ref) {
    jni_Unimplemented("DeleteWeakGlobalRef");
}

jboolean jni_ExceptionCheck(JNIEnv *env) {
    jni_Unimplemented("ExceptionCheck");
}

jobject jni_NewDirectByteBuffer(JNIEnv* env, void* address, jlong capacity) {
    jni_Unimplemented("NewDirectByteBuffer");
}

void* jni_GetDirectBufferAddress(JNIEnv* env, jobject buf) {
    jni_Unimplemented("GetDirectBufferAddress");
}

jlong jni_GetDirectBufferCapacity(JNIEnv* env, jobject buf) {
    jni_Unimplemented("GetDirectBufferCapacity");
}

jobjectRefType jni_GetObjectRefType(JNIEnv* env, jobject obj) {
    jni_Unimplemented("GetObjectRefType");
}

const struct JNINativeInterface_ jni_NativeInterface = {
    NULL,
    NULL,
    NULL,

    NULL,

    jni_GetVersion,

    jni_DefineClass,
    jni_FindClass,

    jni_FromReflectedMethod,
    jni_FromReflectedField,

    jni_ToReflectedMethod,

    jni_GetSuperclass,
    jni_IsAssignableFrom,

    jni_ToReflectedField,

    jni_Throw,
    jni_ThrowNew,
    jni_ExceptionOccurred,
    jni_ExceptionDescribe,
    jni_ExceptionClear,
    jni_FatalError,

    jni_PushLocalFrame,
    jni_PopLocalFrame,

    jni_NewGlobalRef,
    jni_DeleteGlobalRef,
    jni_DeleteLocalRef,
    jni_IsSameObject,

    jni_NewLocalRef,
    jni_EnsureLocalCapacity,

    jni_AllocObject,
    jni_NewObject,
    jni_NewObjectV,
    jni_NewObjectA,

    jni_GetObjectClass,
    jni_IsInstanceOf,

    jni_GetMethodID,

    jni_CallObjectMethod,
    jni_CallObjectMethodV,
    jni_CallObjectMethodA,
    jni_CallBooleanMethod,
    jni_CallBooleanMethodV,
    jni_CallBooleanMethodA,
    jni_CallByteMethod,
    jni_CallByteMethodV,
    jni_CallByteMethodA,
    jni_CallCharMethod,
    jni_CallCharMethodV,
    jni_CallCharMethodA,
    jni_CallShortMethod,
    jni_CallShortMethodV,
    jni_CallShortMethodA,
    jni_CallIntMethod,
    jni_CallIntMethodV,
    jni_CallIntMethodA,
    jni_CallLongMethod,
    jni_CallLongMethodV,
    jni_CallLongMethodA,
    jni_CallFloatMethod,
    jni_CallFloatMethodV,
    jni_CallFloatMethodA,
    jni_CallDoubleMethod,
    jni_CallDoubleMethodV,
    jni_CallDoubleMethodA,
    jni_CallVoidMethod,
    jni_CallVoidMethodV,
    jni_CallVoidMethodA,

    jni_CallNonvirtualObjectMethod,
    jni_CallNonvirtualObjectMethodV,
    jni_CallNonvirtualObjectMethodA,
    jni_CallNonvirtualBooleanMethod,
    jni_CallNonvirtualBooleanMethodV,
    jni_CallNonvirtualBooleanMethodA,
    jni_CallNonvirtualByteMethod,
    jni_CallNonvirtualByteMethodV,
    jni_CallNonvirtualByteMethodA,
    jni_CallNonvirtualCharMethod,
    jni_CallNonvirtualCharMethodV,
    jni_CallNonvirtualCharMethodA,
    jni_CallNonvirtualShortMethod,
    jni_CallNonvirtualShortMethodV,
    jni_CallNonvirtualShortMethodA,
    jni_CallNonvirtualIntMethod,
    jni_CallNonvirtualIntMethodV,
    jni_CallNonvirtualIntMethodA,
    jni_CallNonvirtualLongMethod,
    jni_CallNonvirtualLongMethodV,
    jni_CallNonvirtualLongMethodA,
    jni_CallNonvirtualFloatMethod,
    jni_CallNonvirtualFloatMethodV,
    jni_CallNonvirtualFloatMethodA,
    jni_CallNonvirtualDoubleMethod,
    jni_CallNonvirtualDoubleMethodV,
    jni_CallNonvirtualDoubleMethodA,
    jni_CallNonvirtualVoidMethod,
    jni_CallNonvirtualVoidMethodV,
    jni_CallNonvirtualVoidMethodA,

    jni_GetFieldID,

    jni_GetObjectField,
    jni_GetBooleanField,
    jni_GetByteField,
    jni_GetCharField,
    jni_GetShortField,
    jni_GetIntField,
    jni_GetLongField,
    jni_GetFloatField,
    jni_GetDoubleField,

    jni_SetObjectField,
    jni_SetBooleanField,
    jni_SetByteField,
    jni_SetCharField,
    jni_SetShortField,
    jni_SetIntField,
    jni_SetLongField,
    jni_SetFloatField,
    jni_SetDoubleField,

    jni_GetStaticMethodID,

    jni_CallStaticObjectMethod,
    jni_CallStaticObjectMethodV,
    jni_CallStaticObjectMethodA,
    jni_CallStaticBooleanMethod,
    jni_CallStaticBooleanMethodV,
    jni_CallStaticBooleanMethodA,
    jni_CallStaticByteMethod,
    jni_CallStaticByteMethodV,
    jni_CallStaticByteMethodA,
    jni_CallStaticCharMethod,
    jni_CallStaticCharMethodV,
    jni_CallStaticCharMethodA,
    jni_CallStaticShortMethod,
    jni_CallStaticShortMethodV,
    jni_CallStaticShortMethodA,
    jni_CallStaticIntMethod,
    jni_CallStaticIntMethodV,
    jni_CallStaticIntMethodA,
    jni_CallStaticLongMethod,
    jni_CallStaticLongMethodV,
    jni_CallStaticLongMethodA,
    jni_CallStaticFloatMethod,
    jni_CallStaticFloatMethodV,
    jni_CallStaticFloatMethodA,
    jni_CallStaticDoubleMethod,
    jni_CallStaticDoubleMethodV,
    jni_CallStaticDoubleMethodA,
    jni_CallStaticVoidMethod,
    jni_CallStaticVoidMethodV,
    jni_CallStaticVoidMethodA,

    jni_GetStaticFieldID,

    jni_GetStaticObjectField,
    jni_GetStaticBooleanField,
    jni_GetStaticByteField,
    jni_GetStaticCharField,
    jni_GetStaticShortField,
    jni_GetStaticIntField,
    jni_GetStaticLongField,
    jni_GetStaticFloatField,
    jni_GetStaticDoubleField,

    jni_SetStaticObjectField,
    jni_SetStaticBooleanField,
    jni_SetStaticByteField,
    jni_SetStaticCharField,
    jni_SetStaticShortField,
    jni_SetStaticIntField,
    jni_SetStaticLongField,
    jni_SetStaticFloatField,
    jni_SetStaticDoubleField,

    jni_NewString,
    jni_GetStringLength,
    jni_GetStringChars,
    jni_ReleaseStringChars,

    jni_NewStringUTF,
    jni_GetStringUTFLength,
    jni_GetStringUTFChars,
    jni_ReleaseStringUTFChars,

    jni_GetArrayLength,

    jni_NewObjectArray,
    jni_GetObjectArrayElement,
    jni_SetObjectArrayElement,

    jni_NewBooleanArray,
    jni_NewByteArray,
    jni_NewCharArray,
    jni_NewShortArray,
    jni_NewIntArray,
    jni_NewLongArray,
    jni_NewFloatArray,
    jni_NewDoubleArray,

    jni_GetBooleanArrayElements,
    jni_GetByteArrayElements,
    jni_GetCharArrayElements,
    jni_GetShortArrayElements,
    jni_GetIntArrayElements,
    jni_GetLongArrayElements,
    jni_GetFloatArrayElements,
    jni_GetDoubleArrayElements,

    jni_ReleaseBooleanArrayElements,
    jni_ReleaseByteArrayElements,
    jni_ReleaseCharArrayElements,
    jni_ReleaseShortArrayElements,
    jni_ReleaseIntArrayElements,
    jni_ReleaseLongArrayElements,
    jni_ReleaseFloatArrayElements,
    jni_ReleaseDoubleArrayElements,

    jni_GetBooleanArrayRegion,
    jni_GetByteArrayRegion,
    jni_GetCharArrayRegion,
    jni_GetShortArrayRegion,
    jni_GetIntArrayRegion,
    jni_GetLongArrayRegion,
    jni_GetFloatArrayRegion,
    jni_GetDoubleArrayRegion,

    jni_SetBooleanArrayRegion,
    jni_SetByteArrayRegion,
    jni_SetCharArrayRegion,
    jni_SetShortArrayRegion,
    jni_SetIntArrayRegion,
    jni_SetLongArrayRegion,
    jni_SetFloatArrayRegion,
    jni_SetDoubleArrayRegion,

    jni_RegisterNatives,
    jni_UnregisterNatives,

    jni_MonitorEnter,
    jni_MonitorExit,

    jni_GetJavaVM,

    jni_GetStringRegion,
    jni_GetStringUTFRegion,

    jni_GetPrimitiveArrayCritical,
    jni_ReleasePrimitiveArrayCritical,

    jni_GetStringCritical,
    jni_ReleaseStringCritical,

    jni_NewWeakGlobalRef,
    jni_DeleteWeakGlobalRef,

    jni_ExceptionCheck,

    jni_NewDirectByteBuffer,
    jni_GetDirectBufferAddress,
    jni_GetDirectBufferCapacity,

    jni_GetObjectRefType
};

extern const JNIEnv jni_JNIEnv = {&jni_NativeInterface};

jint jni_DestroyJavaVM(JavaVM *vm) {
    jni_Unimplemented("DestroyJavaVM");
}

jint jni_AttachCurrentThread(JavaVM *vm, void **penv, void *args) {
    jni_Unimplemented("AttachCurrentThread");
}

jint jni_DetachCurrentThread(JavaVM *vm) {
    jni_Unimplemented("DetachCurrentThread");
}

jint jni_GetEnv(JavaVM *vm, void **penv, jint version) {
    jni_Unimplemented("GetEnv");
}

jint jni_AttachCurrentThreadAsDaemon(JavaVM *vm, void **penv, void *args) {
    jni_Unimplemented("AttachCurrentThreadAsDaemon");
}

const struct JNIInvokeInterface_ jni_InvokeInterface = {
    NULL,
    NULL,
    NULL,

    jni_DestroyJavaVM,
    jni_AttachCurrentThread,
    jni_DetachCurrentThread,
    jni_GetEnv,
    jni_AttachCurrentThreadAsDaemon
};

extern const JavaVM jni_MainJavaVM = {&jni_InvokeInterface};

} // extern "C"
