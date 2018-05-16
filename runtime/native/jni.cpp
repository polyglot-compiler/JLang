#include <cstdio>
#include <cstdlib>
#include <jni.h>
#include "stack_trace.h"

#include "rep.h"
#include "class.h"
#include "native.h"

[[noreturn]] static void JniUnimplemented(const char* name) {
  fprintf(stderr,
    "- - - - - - - - - - - - - - - - - - - - - - - - - - -\n"
    "The following JNI method is currently unimplemented:\n"
    "  %s\n"
    "It is defined in " __FILE__ ".\n"
    "Aborting for now.\n"
    "- - - - - - - - - - - - - - - - - - - - - - - - - - -\n"
    , name);
  DumpStackTrace();
  abort();
}

// Begin helper methods.

static char* GetFieldPtr(jobject obj, jfieldID id) {
    return reinterpret_cast<char*>(obj) + reinterpret_cast<intptr_t>(id);
}

template <typename T>
static T GetField(jobject obj, jfieldID id) {
    auto ptr = GetFieldPtr(obj, id);
    return *reinterpret_cast<T*>(ptr);
}

template <typename T>
static void SetField(jobject obj, jfieldID id, T val) {
    auto ptr = GetFieldPtr(obj, id);
    *reinterpret_cast<T*>(ptr) = val;
}

// Begin official API.

extern "C" {

jint jni_GetVersion(JNIEnv *env) {
    // This is the correct version for Java SE 7 too.
    return JNI_VERSION_1_6;
}

jclass jni_DefineClass(JNIEnv *env, const char *name, jobject loader, const jbyte *buf, jsize len) {
    JniUnimplemented("DefineClass");
}

jclass jni_FindClass(JNIEnv *env, const char *name) {
    JniUnimplemented("FindClass");
}

jmethodID jni_FromReflectedMethod(JNIEnv *env, jobject method) {
    JniUnimplemented("FromReflectedMethod");
}

jfieldID jni_FromReflectedField(JNIEnv *env, jobject field) {
    JniUnimplemented("FromReflectedField");
}

jobject jni_ToReflectedMethod(JNIEnv *env, jclass cls, jmethodID methodID, jboolean isStatic) {
    JniUnimplemented("ToReflectedMethod");
}

jclass jni_GetSuperclass(JNIEnv *env, jclass sub) {
    JniUnimplemented("GetSuperclass");
}

jboolean jni_IsAssignableFrom(JNIEnv *env, jclass sub, jclass sup) {
    JniUnimplemented("IsAssignableFrom");
}

jobject jni_ToReflectedField(JNIEnv *env, jclass cls, jfieldID fieldID, jboolean isStatic) {
    JniUnimplemented("ToReflectedField");
}

jint jni_Throw(JNIEnv *env, jthrowable obj) {
    JniUnimplemented("Throw");
}

jint jni_ThrowNew(JNIEnv *env, jclass clazz, const char *msg) {
    JniUnimplemented("ThrowNew");
}

jthrowable jni_ExceptionOccurred(JNIEnv *env) {
    JniUnimplemented("ExceptionOccurred");
}

void jni_ExceptionDescribe(JNIEnv *env) {
    JniUnimplemented("ExceptionDescribe");
}

void jni_ExceptionClear(JNIEnv *env) {
    JniUnimplemented("ExceptionClear");
}

void jni_FatalError(JNIEnv *env, const char *msg) {
    JniUnimplemented("FatalError");
}

jint jni_PushLocalFrame(JNIEnv *env, jint capacity) {
    JniUnimplemented("PushLocalFrame");
}

jobject jni_PopLocalFrame(JNIEnv *env, jobject result) {
    JniUnimplemented("PopLocalFrame");
}

jobject jni_NewGlobalRef(JNIEnv *env, jobject lobj) {
    JniUnimplemented("NewGlobalRef");
}

void jni_DeleteGlobalRef(JNIEnv *env, jobject gref) {
    JniUnimplemented("DeleteGlobalRef");
}

void jni_DeleteLocalRef(JNIEnv *env, jobject obj) {
    JniUnimplemented("DeleteLocalRef");
}

jboolean jni_IsSameObject(JNIEnv *env, jobject obj1, jobject obj2) {
    return obj1 == obj2;
}

jobject jni_NewLocalRef(JNIEnv *env, jobject ref) {
    JniUnimplemented("NewLocalRef");
}

jint jni_EnsureLocalCapacity(JNIEnv *env, jint capacity) {
    JniUnimplemented("EnsureLocalCapacity");
}

jobject jni_AllocObject(JNIEnv *env, jclass clazz) {
    JniUnimplemented("AllocObject");
}

jobject jni_NewObject(JNIEnv *env, jclass clazz, jmethodID methodID, ...) {
    JniUnimplemented("NewObject");
}

jobject jni_NewObjectV(JNIEnv *env, jclass clazz, jmethodID methodID, va_list args) {
    JniUnimplemented("NewObjectV");
}

jobject jni_NewObjectA(JNIEnv *env, jclass clazz, jmethodID methodID, const jvalue *args) {
    JniUnimplemented("NewObjectA");
}

jclass jni_GetObjectClass(JNIEnv *env, jobject obj) {
    return Unwrap(obj)->Cdv()->Class()->Wrap();
}

jboolean jni_IsInstanceOf(JNIEnv *env, jobject obj, jclass clazz) {
    JniUnimplemented("IsInstanceOf");
}

jmethodID jni_GetMethodID(JNIEnv *env, jclass clazz, const char *name, const char *sig) {
    JniUnimplemented("GetMethodID");
}

jobject jni_CallObjectMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {
    JniUnimplemented("CallObjectMethod");
}

jobject jni_CallObjectMethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args) {
    JniUnimplemented("CallObjectMethodV");
}

jobject jni_CallObjectMethodA(JNIEnv *env, jobject obj, jmethodID methodID, const jvalue* args) {
    JniUnimplemented("CallObjectMethodA");
}

jboolean jni_CallBooleanMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {
    JniUnimplemented("CallBooleanMethod");
}

jboolean jni_CallBooleanMethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args) {
    JniUnimplemented("CallBooleanMethodV");
}

jboolean jni_CallBooleanMethodA(JNIEnv *env, jobject obj, jmethodID methodID, const jvalue* args) {
    JniUnimplemented("CallBooleanMethodA");
}

jbyte jni_CallByteMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {
    JniUnimplemented("CallByteMethod");
}

jbyte jni_CallByteMethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args) {
    JniUnimplemented("CallByteMethodV");
}

jbyte jni_CallByteMethodA(JNIEnv *env, jobject obj, jmethodID methodID, const jvalue *args) {
    JniUnimplemented("CallByteMethodA");
}

jchar jni_CallCharMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {
    JniUnimplemented("CallCharMethod");
}

jchar jni_CallCharMethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args) {
    JniUnimplemented("CallCharMethodV");
}

jchar jni_CallCharMethodA(JNIEnv *env, jobject obj, jmethodID methodID, const jvalue *args) {
    JniUnimplemented("CallCharMethodA");
}

jshort jni_CallShortMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {
    JniUnimplemented("CallShortMethod");
}

jshort jni_CallShortMethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args) {
    JniUnimplemented("CallShortMethodV");
}

jshort jni_CallShortMethodA(JNIEnv *env, jobject obj, jmethodID methodID, const jvalue *args) {
    JniUnimplemented("CallShortMethodA");
}

jint jni_CallIntMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {
    JniUnimplemented("CallIntMethod");
}

jint jni_CallIntMethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args) {
    JniUnimplemented("CallIntMethodV");
}

jint jni_CallIntMethodA(JNIEnv *env, jobject obj, jmethodID methodID, const jvalue *args) {
    JniUnimplemented("CallIntMethodA");
}

jlong jni_CallLongMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {
    JniUnimplemented("CallLongMethod");
}

jlong jni_CallLongMethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args) {
    JniUnimplemented("CallLongMethodV");
}

jlong jni_CallLongMethodA(JNIEnv *env, jobject obj, jmethodID methodID, const jvalue *args) {
    JniUnimplemented("CallLongMethodA");
}

jfloat jni_CallFloatMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {
    JniUnimplemented("CallFloatMethod");
}

jfloat jni_CallFloatMethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args) {
    JniUnimplemented("CallFloatMethodV");
}

jfloat jni_CallFloatMethodA(JNIEnv *env, jobject obj, jmethodID methodID, const jvalue *args) {
    JniUnimplemented("CallFloatMethodA");
}

jdouble jni_CallDoubleMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {
    JniUnimplemented("CallDoubleMethod");
}

jdouble jni_CallDoubleMethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args) {
    JniUnimplemented("CallDoubleMethodV");
}

jdouble jni_CallDoubleMethodA(JNIEnv *env, jobject obj, jmethodID methodID, const jvalue *args) {
    JniUnimplemented("CallDoubleMethodA");
}

void jni_CallVoidMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {
    JniUnimplemented("CallVoidMethod");
}

void jni_CallVoidMethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args) {
    JniUnimplemented("CallVoidMethodV");
}

void jni_CallVoidMethodA(JNIEnv *env, jobject obj, jmethodID methodID, const jvalue* args) {
    JniUnimplemented("CallVoidMethodA");
}

jobject jni_CallNonvirtualObjectMethod(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, ...) {
    JniUnimplemented("CallNonvirtualObjectMethod");
}

jobject jni_CallNonvirtualObjectMethodV(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, va_list args) {
    JniUnimplemented("CallNonvirtualObjectMethodV");
}

jobject jni_CallNonvirtualObjectMethodA(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, const jvalue* args) {
    JniUnimplemented("CallNonvirtualObjectMethodA");
}

jboolean jni_CallNonvirtualBooleanMethod(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, ...) {
    JniUnimplemented("CallNonvirtualBooleanMethod");
}

jboolean jni_CallNonvirtualBooleanMethodV(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, va_list args) {
    JniUnimplemented("CallNonvirtualBooleanMethodV");
}

jboolean jni_CallNonvirtualBooleanMethodA(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, const jvalue* args) {
    JniUnimplemented("CallNonvirtualBooleanMethodA");
}

jbyte jni_CallNonvirtualByteMethod(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, ...) {
    JniUnimplemented("CallNonvirtualByteMethod");
}

jbyte jni_CallNonvirtualByteMethodV(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, va_list args) {
    JniUnimplemented("CallNonvirtualByteMethodV");
}

jbyte jni_CallNonvirtualByteMethodA(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, const jvalue *args) {
    JniUnimplemented("CallNonvirtualByteMethodA");
}

jchar jni_CallNonvirtualCharMethod(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, ...) {
    JniUnimplemented("CallNonvirtualCharMethod");
}

jchar jni_CallNonvirtualCharMethodV(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, va_list args) {
    JniUnimplemented("CallNonvirtualCharMethodV");
}

jchar jni_CallNonvirtualCharMethodA(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, const jvalue *args) {
    JniUnimplemented("CallNonvirtualCharMethodA");
}

jshort jni_CallNonvirtualShortMethod(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, ...) {
    JniUnimplemented("CallNonvirtualShortMethod");
}

jshort jni_CallNonvirtualShortMethodV(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, va_list args) {
    JniUnimplemented("CallNonvirtualShortMethodV");
}

jshort jni_CallNonvirtualShortMethodA(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, const jvalue *args) {
    JniUnimplemented("CallNonvirtualShortMethodA");
}

jint jni_CallNonvirtualIntMethod(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, ...) {
    JniUnimplemented("CallNonvirtualIntMethod");
}

jint jni_CallNonvirtualIntMethodV(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, va_list args) {
    JniUnimplemented("CallNonvirtualIntMethodV");
}

jint jni_CallNonvirtualIntMethodA(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, const jvalue *args) {
    JniUnimplemented("CallNonvirtualIntMethodA");
}

jlong jni_CallNonvirtualLongMethod(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, ...) {
    JniUnimplemented("CallNonvirtualLongMethod");
}

jlong jni_CallNonvirtualLongMethodV(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, va_list args) {
    JniUnimplemented("CallNonvirtualLongMethodV");
}

jlong jni_CallNonvirtualLongMethodA(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, const jvalue *args) {
    JniUnimplemented("CallNonvirtualLongMethodA");
}

jfloat jni_CallNonvirtualFloatMethod(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, ...) {
    JniUnimplemented("CallNonvirtualFloatMethod");
}

jfloat jni_CallNonvirtualFloatMethodV(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, va_list args) {
    JniUnimplemented("CallNonvirtualFloatMethodV");
}

jfloat jni_CallNonvirtualFloatMethodA(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, const jvalue *args) {
    JniUnimplemented("CallNonvirtualFloatMethodA");
}

jdouble jni_CallNonvirtualDoubleMethod(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, ...) {
    JniUnimplemented("CallNonvirtualDoubleMethod");
}

jdouble jni_CallNonvirtualDoubleMethodV(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, va_list args) {
    JniUnimplemented("CallNonvirtualDoubleMethodV");
}

jdouble jni_CallNonvirtualDoubleMethodA(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, const jvalue *args) {
    JniUnimplemented("CallNonvirtualDoubleMethodA");
}

void jni_CallNonvirtualVoidMethod(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, ...) {
    JniUnimplemented("CallNonvirtualVoidMethod");
}

void jni_CallNonvirtualVoidMethodV(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, va_list args) {
    JniUnimplemented("CallNonvirtualVoidMethodV");
}

void jni_CallNonvirtualVoidMethodA(JNIEnv *env, jobject obj, jclass clazz, jmethodID methodID, const jvalue* args) {
    JniUnimplemented("CallNonvirtualVoidMethodA");
}

jfieldID jni_GetFieldID(JNIEnv *env, jclass clazz, const char *name, const char *sig) {
    auto res = GetJavaFieldId(clazz, name);
    if (!res) {
        // TODO: Should technically throw NoSuchFieldError.
        fprintf(stderr,
            "Could not find field %s in class %s. Aborting.\n",
            name, GetJavaClassName(clazz));
        abort();
    }
    return res;
}


// Calls a macro for each Java type and rep pair, e.g., "Object, jobject".
#define FOR_EACH_JAVA_TYPE_AND_REP(macro) \
    macro(Object, jobject) \
    macro(Boolean, jboolean) \
    macro(Byte, jbyte) \
    macro(Char, jchar) \
    macro(Short, jshort) \
    macro(Int, jint) \
    macro(Long, jlong) \
    macro(Float, jfloat) \
    macro(Double, jdouble)


// Declares JNI methods such as jni_GetObjectField.
#define DECLARE_JNI_GET_FIELD(type, rep) \
    rep jni_Get##type##Field(JNIEnv *env, jobject obj, jfieldID id) { \
       return GetField<rep>(obj, id); \
    }
FOR_EACH_JAVA_TYPE_AND_REP(DECLARE_JNI_GET_FIELD)


// Declares JNI methods such as jni_SetObjectField.
#define DECLARE_JNI_SET_FIELD(kind, rep) \
    void jni_Set##kind##Field(JNIEnv *env, jobject obj, jfieldID id, rep val) { \
       SetField<rep>(obj, id, val); \
    }
FOR_EACH_JAVA_TYPE_AND_REP(DECLARE_JNI_SET_FIELD)


jmethodID jni_GetStaticMethodID(JNIEnv *env, jclass clazz, const char *name, const char *sig) {
    JniUnimplemented("GetStaticMethodID");
}

jobject jni_CallStaticObjectMethod(JNIEnv *env, jclass clazz, jmethodID methodID, ...) {
    JniUnimplemented("CallStaticObjectMethod");
}

jobject jni_CallStaticObjectMethodV(JNIEnv *env, jclass clazz, jmethodID methodID, va_list args) {
    JniUnimplemented("CallStaticObjectMethodV");
}

jobject jni_CallStaticObjectMethodA(JNIEnv *env, jclass clazz, jmethodID methodID, const jvalue *args) {
    JniUnimplemented("CallStaticObjectMethodA");
}

jboolean jni_CallStaticBooleanMethod(JNIEnv *env, jclass clazz, jmethodID methodID, ...) {
    JniUnimplemented("CallStaticBooleanMethod");
}

jboolean jni_CallStaticBooleanMethodV(JNIEnv *env, jclass clazz, jmethodID methodID, va_list args) {
    JniUnimplemented("CallStaticBooleanMethodV");
}

jboolean jni_CallStaticBooleanMethodA(JNIEnv *env, jclass clazz, jmethodID methodID, const jvalue *args) {
    JniUnimplemented("CallStaticBooleanMethodA");
}

jbyte jni_CallStaticByteMethod(JNIEnv *env, jclass clazz, jmethodID methodID, ...) {
    JniUnimplemented("CallStaticByteMethod");
}

jbyte jni_CallStaticByteMethodV(JNIEnv *env, jclass clazz, jmethodID methodID, va_list args) {
    JniUnimplemented("CallStaticByteMethodV");
}

jbyte jni_CallStaticByteMethodA(JNIEnv *env, jclass clazz, jmethodID methodID, const jvalue *args) {
    JniUnimplemented("CallStaticByteMethodA");
}

jchar jni_CallStaticCharMethod(JNIEnv *env, jclass clazz, jmethodID methodID, ...) {
    JniUnimplemented("CallStaticCharMethod");
}

jchar jni_CallStaticCharMethodV(JNIEnv *env, jclass clazz, jmethodID methodID, va_list args) {
    JniUnimplemented("CallStaticCharMethodV");
}

jchar jni_CallStaticCharMethodA(JNIEnv *env, jclass clazz, jmethodID methodID, const jvalue *args) {
    JniUnimplemented("CallStaticCharMethodA");
}

jshort jni_CallStaticShortMethod(JNIEnv *env, jclass clazz, jmethodID methodID, ...) {
    JniUnimplemented("CallStaticShortMethod");
}

jshort jni_CallStaticShortMethodV(JNIEnv *env, jclass clazz, jmethodID methodID, va_list args) {
    JniUnimplemented("CallStaticShortMethodV");
}

jshort jni_CallStaticShortMethodA(JNIEnv *env, jclass clazz, jmethodID methodID, const jvalue *args) {
    JniUnimplemented("CallStaticShortMethodA");
}

jint jni_CallStaticIntMethod(JNIEnv *env, jclass clazz, jmethodID methodID, ...) {
    JniUnimplemented("CallStaticIntMethod");
}

jint jni_CallStaticIntMethodV(JNIEnv *env, jclass clazz, jmethodID methodID, va_list args) {
    JniUnimplemented("CallStaticIntMethodV");
}

jint jni_CallStaticIntMethodA(JNIEnv *env, jclass clazz, jmethodID methodID, const jvalue *args) {
    JniUnimplemented("CallStaticIntMethodA");
}

jlong jni_CallStaticLongMethod(JNIEnv *env, jclass clazz, jmethodID methodID, ...) {
    JniUnimplemented("CallStaticLongMethod");
}

jlong jni_CallStaticLongMethodV(JNIEnv *env, jclass clazz, jmethodID methodID, va_list args) {
    JniUnimplemented("CallStaticLongMethodV");
}

jlong jni_CallStaticLongMethodA(JNIEnv *env, jclass clazz, jmethodID methodID, const jvalue *args) {
    JniUnimplemented("CallStaticLongMethodA");
}

jfloat jni_CallStaticFloatMethod(JNIEnv *env, jclass clazz, jmethodID methodID, ...) {
    JniUnimplemented("CallStaticFloatMethod");
}

jfloat jni_CallStaticFloatMethodV(JNIEnv *env, jclass clazz, jmethodID methodID, va_list args) {
    JniUnimplemented("CallStaticFloatMethodV");
}

jfloat jni_CallStaticFloatMethodA(JNIEnv *env, jclass clazz, jmethodID methodID, const jvalue *args) {
    JniUnimplemented("CallStaticFloatMethodA");
}

jdouble jni_CallStaticDoubleMethod(JNIEnv *env, jclass clazz, jmethodID methodID, ...) {
    JniUnimplemented("CallStaticDoubleMethod");
}

jdouble jni_CallStaticDoubleMethodV(JNIEnv *env, jclass clazz, jmethodID methodID, va_list args) {
    JniUnimplemented("CallStaticDoubleMethodV");
}

jdouble jni_CallStaticDoubleMethodA(JNIEnv *env, jclass clazz, jmethodID methodID, const jvalue *args) {
    JniUnimplemented("CallStaticDoubleMethodA");
}

void jni_CallStaticVoidMethod(JNIEnv *env, jclass cls, jmethodID methodID, ...) {
    JniUnimplemented("CallStaticVoidMethod");
}

void jni_CallStaticVoidMethodV(JNIEnv *env, jclass cls, jmethodID methodID, va_list args) {
    JniUnimplemented("CallStaticVoidMethodV");
}

void jni_CallStaticVoidMethodA(JNIEnv *env, jclass cls, jmethodID methodID, const jvalue* args) {
    JniUnimplemented("CallStaticVoidMethodA");
}

jfieldID jni_GetStaticFieldID(JNIEnv *env, jclass clazz, const char *name, const char *sig) {
    JniUnimplemented("GetStaticFieldID");
}

jobject jni_GetStaticObjectField(JNIEnv *env, jclass clazz, jfieldID fieldID) {
    JniUnimplemented("GetStaticObjectField");
}

jboolean jni_GetStaticBooleanField(JNIEnv *env, jclass clazz, jfieldID fieldID) {
    JniUnimplemented("GetStaticBooleanField");
}

jbyte jni_GetStaticByteField(JNIEnv *env, jclass clazz, jfieldID fieldID) {
    JniUnimplemented("GetStaticByteField");
}

jchar jni_GetStaticCharField(JNIEnv *env, jclass clazz, jfieldID fieldID) {
    JniUnimplemented("GetStaticCharField");
}

jshort jni_GetStaticShortField(JNIEnv *env, jclass clazz, jfieldID fieldID) {
    JniUnimplemented("GetStaticShortField");
}

jint jni_GetStaticIntField(JNIEnv *env, jclass clazz, jfieldID fieldID) {
    JniUnimplemented("GetStaticIntField");
}

jlong jni_GetStaticLongField(JNIEnv *env, jclass clazz, jfieldID fieldID) {
    JniUnimplemented("GetStaticLongField");
}

jfloat jni_GetStaticFloatField(JNIEnv *env, jclass clazz, jfieldID fieldID) {
    JniUnimplemented("GetStaticFloatField");
}

jdouble jni_GetStaticDoubleField(JNIEnv *env, jclass clazz, jfieldID fieldID) {
    JniUnimplemented("GetStaticDoubleField");
}

void jni_SetStaticObjectField(JNIEnv *env, jclass clazz, jfieldID fieldID, jobject value) {
    JniUnimplemented("SetStaticObjectField");
}

void jni_SetStaticBooleanField(JNIEnv *env, jclass clazz, jfieldID fieldID, jboolean value) {
    JniUnimplemented("SetStaticBooleanField");
}

void jni_SetStaticByteField(JNIEnv *env, jclass clazz, jfieldID fieldID, jbyte value) {
    JniUnimplemented("SetStaticByteField");
}

void jni_SetStaticCharField(JNIEnv *env, jclass clazz, jfieldID fieldID, jchar value) {
    JniUnimplemented("SetStaticCharField");
}

void jni_SetStaticShortField(JNIEnv *env, jclass clazz, jfieldID fieldID, jshort value) {
    JniUnimplemented("SetStaticShortField");
}

void jni_SetStaticIntField(JNIEnv *env, jclass clazz, jfieldID fieldID, jint value) {
    JniUnimplemented("SetStaticIntField");
}

void jni_SetStaticLongField(JNIEnv *env, jclass clazz, jfieldID fieldID, jlong value) {
    JniUnimplemented("SetStaticLongField");
}

void jni_SetStaticFloatField(JNIEnv *env, jclass clazz, jfieldID fieldID, jfloat value) {
    JniUnimplemented("SetStaticFloatField");
}

void jni_SetStaticDoubleField(JNIEnv *env, jclass clazz, jfieldID fieldID, jdouble value) {
    JniUnimplemented("SetStaticDoubleField");
}

jstring jni_NewString(JNIEnv *env, const jchar *unicode, jsize len) {
    JniUnimplemented("NewString");
}

jsize jni_GetStringLength(JNIEnv *env, jstring str) {
    JniUnimplemented("GetStringLength");
}

const jchar *jni_GetStringChars(JNIEnv *env, jstring str, jboolean *isCopy) {
    JniUnimplemented("GetStringChars");
}

void jni_ReleaseStringChars(JNIEnv *env, jstring str, const jchar *chars) {
    JniUnimplemented("ReleaseStringChars");
}

jstring jni_NewStringUTF(JNIEnv *env, const char *utf) {
    JniUnimplemented("NewStringUTF");
}

jsize jni_GetStringUTFLength(JNIEnv *env, jstring str) {
    JniUnimplemented("GetStringUTFLength");
}

const char* jni_GetStringUTFChars(JNIEnv *env, jstring str, jboolean *isCopy) {
    auto chars = Unwrap(str)->Chars();
    auto len = chars->Length();
    auto data = (jchar*) chars->Data();
    char* res = (char*) malloc(len);
    // TODO: Incorrect conversion from Java string chars to UTF-8 chars.
    for (jsize i = 0; i < len; ++i)
        res[i] = static_cast<char>(data[i]);
    *isCopy = true;
    return res;
}

void jni_ReleaseStringUTFChars(JNIEnv *env, jstring str, const char* chars) {
    free(const_cast<char*>(chars));
}

jsize jni_GetArrayLength(JNIEnv *env, jarray array) {
    return Unwrap(array)->Length();
}

jobjectArray jni_NewObjectArray(JNIEnv *env, jsize len, jclass clazz, jobject init) {
    JniUnimplemented("NewObjectArray");
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
    JniUnimplemented("NewBooleanArray");
}

jbyteArray jni_NewByteArray(JNIEnv *env, jsize len) {
    JniUnimplemented("NewByteArray");
}

jcharArray jni_NewCharArray(JNIEnv *env, jsize len) {
    JniUnimplemented("NewCharArray");
}

jshortArray jni_NewShortArray(JNIEnv *env, jsize len) {
    JniUnimplemented("NewShortArray");
}

jintArray jni_NewIntArray(JNIEnv *env, jsize len) {
    JniUnimplemented("NewIntArray");
}

jlongArray jni_NewLongArray(JNIEnv *env, jsize len) {
    JniUnimplemented("NewLongArray");
}

jfloatArray jni_NewFloatArray(JNIEnv *env, jsize len) {
    JniUnimplemented("NewFloatArray");
}

jdoubleArray jni_NewDoubleArray(JNIEnv *env, jsize len) {
    JniUnimplemented("NewDoubleArray");
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
    JniUnimplemented("GetBooleanArrayRegion");
}

void jni_GetByteArrayRegion(JNIEnv *env, jbyteArray array, jsize start, jsize len, jbyte *buf) {
    JniUnimplemented("GetByteArrayRegion");
}

void jni_GetCharArrayRegion(JNIEnv *env, jcharArray array, jsize start, jsize len, jchar *buf) {
    JniUnimplemented("GetCharArrayRegion");
}

void jni_GetShortArrayRegion(JNIEnv *env, jshortArray array, jsize start, jsize len, jshort *buf) {
    JniUnimplemented("GetShortArrayRegion");
}

void jni_GetIntArrayRegion(JNIEnv *env, jintArray array, jsize start, jsize len, jint *buf) {
    JniUnimplemented("GetIntArrayRegion");
}

void jni_GetLongArrayRegion(JNIEnv *env, jlongArray array, jsize start, jsize len, jlong *buf) {
    JniUnimplemented("GetLongArrayRegion");
}

void jni_GetFloatArrayRegion(JNIEnv *env, jfloatArray array, jsize start, jsize len, jfloat *buf) {
    JniUnimplemented("GetFloatArrayRegion");
}

void jni_GetDoubleArrayRegion(JNIEnv *env, jdoubleArray array, jsize start, jsize len, jdouble *buf) {
    JniUnimplemented("GetDoubleArrayRegion");
}

void jni_SetBooleanArrayRegion(JNIEnv *env, jbooleanArray array, jsize start, jsize l, const jboolean *buf) {
    JniUnimplemented("SetBooleanArrayRegion");
}

void jni_SetByteArrayRegion(JNIEnv *env, jbyteArray array, jsize start, jsize len, const jbyte *buf) {
    JniUnimplemented("SetByteArrayRegion");
}

void jni_SetCharArrayRegion(JNIEnv *env, jcharArray array, jsize start, jsize len, const jchar *buf) {
    JniUnimplemented("SetCharArrayRegion");
}

void jni_SetShortArrayRegion(JNIEnv *env, jshortArray array, jsize start, jsize len, const jshort *buf) {
    JniUnimplemented("SetShortArrayRegion");
}

void jni_SetIntArrayRegion(JNIEnv *env, jintArray array, jsize start, jsize len, const jint *buf) {
    JniUnimplemented("SetIntArrayRegion");
}

void jni_SetLongArrayRegion(JNIEnv *env, jlongArray array, jsize start, jsize len, const jlong *buf) {
    JniUnimplemented("SetLongArrayRegion");
}

void jni_SetFloatArrayRegion(JNIEnv *env, jfloatArray array, jsize start, jsize len, const jfloat *buf) {
    JniUnimplemented("SetFloatArrayRegion");
}

void jni_SetDoubleArrayRegion(JNIEnv *env, jdoubleArray array, jsize start, jsize len, const jdouble *buf) {
    JniUnimplemented("SetDoubleArrayRegion");
}

jint
jni_RegisterNatives(
    JNIEnv *env, jclass clazz,
    const JNINativeMethod *methods, jint nMethods
) {
    for (jint i = 0; i < nMethods; ++i) {
        RegisterJavaNativeFunc(
            clazz,
            methods[i].name,
            methods[i].signature,
            methods[i].fnPtr
        );
    }
    return 0;
}

jint jni_UnregisterNatives(JNIEnv *env, jclass clazz) {
    JniUnimplemented("UnregisterNatives");
}

jint jni_MonitorEnter(JNIEnv *env, jobject obj) {
    JniUnimplemented("MonitorEnter");
}

jint jni_MonitorExit(JNIEnv *env, jobject obj) {
    JniUnimplemented("MonitorExit");
}

jint jni_GetJavaVM(JNIEnv *env, JavaVM **vm) {
    JniUnimplemented("GetJavaVM");
}

void jni_GetStringRegion(JNIEnv *env, jstring str, jsize start, jsize len, jchar *buf) {
    JniUnimplemented("GetStringRegion");
}

void jni_GetStringUTFRegion(JNIEnv *env, jstring str, jsize start, jsize len, char *buf) {
    JniUnimplemented("GetStringUTFRegion");
}

void* jni_GetPrimitiveArrayCritical(JNIEnv *env, jarray array, jboolean *isCopy) {
    JniUnimplemented("GetPrimitiveArrayCritical");
}

void jni_ReleasePrimitiveArrayCritical(JNIEnv *env, jarray array, void *carray, jint mode) {
    JniUnimplemented("ReleasePrimitiveArrayCritical");
}

const jchar* jni_GetStringCritical(JNIEnv *env, jstring string, jboolean *isCopy) {
    JniUnimplemented("GetStringCritical");
}

void jni_ReleaseStringCritical(JNIEnv *env, jstring string, const jchar *cstring) {
    JniUnimplemented("ReleaseStringCritical");
}

jweak jni_NewWeakGlobalRef(JNIEnv *env, jobject obj) {
    JniUnimplemented("NewWeakGlobalRef");
}

void jni_DeleteWeakGlobalRef(JNIEnv *env, jweak ref) {
    JniUnimplemented("DeleteWeakGlobalRef");
}

jboolean jni_ExceptionCheck(JNIEnv *env) {
    JniUnimplemented("ExceptionCheck");
}

jobject jni_NewDirectByteBuffer(JNIEnv* env, void* address, jlong capacity) {
    JniUnimplemented("NewDirectByteBuffer");
}

void* jni_GetDirectBufferAddress(JNIEnv* env, jobject buf) {
    JniUnimplemented("GetDirectBufferAddress");
}

jlong jni_GetDirectBufferCapacity(JNIEnv* env, jobject buf) {
    JniUnimplemented("GetDirectBufferCapacity");
}

jobjectRefType jni_GetObjectRefType(JNIEnv* env, jobject obj) {
    JniUnimplemented("GetObjectRefType");
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
    JniUnimplemented("DestroyJavaVM");
}

jint jni_AttachCurrentThread(JavaVM *vm, void **penv, void *args) {
    JniUnimplemented("AttachCurrentThread");
}

jint jni_DetachCurrentThread(JavaVM *vm) {
    JniUnimplemented("DetachCurrentThread");
}

jint jni_GetEnv(JavaVM *vm, void **penv, jint version) {
    JniUnimplemented("GetEnv");
}

jint jni_AttachCurrentThreadAsDaemon(JavaVM *vm, void **penv, void *args) {
    JniUnimplemented("AttachCurrentThreadAsDaemon");
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
