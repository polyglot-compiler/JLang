#ifndef __JNI_H__
#define __JNI_H__

#include <cassert>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <vector>
#include "class.h"
#include "factory.h"
#include "native.h"
#include "rep.h"
#include "stack_trace.h"

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


template <typename T>
static T*
GetJavaFieldPtr(jobject obj, jfieldID id) {
    auto f = reinterpret_cast<const JavaFieldInfo*>(id);
    auto raw = reinterpret_cast<char*>(obj) + f->offset;
    return reinterpret_cast<T*>(raw);
}

template <typename T>
static T
GetJavaField(jobject obj, jfieldID id) {
    return *GetJavaFieldPtr<T>(obj, id);
}

template <typename T>
static void
SetJavaField(jobject obj, jfieldID id, T val) {
    *GetJavaFieldPtr<T>(obj, id) = val;
}

template <typename T>
static T*
GetJavaArrayData(jarray arr, jboolean *isCopy) {
    // Note: several JNI methods assume no copy.
    if (isCopy != nullptr)
        *isCopy = JNI_FALSE;
    return reinterpret_cast<T*>(Unwrap(arr)->Data());
}


// Returns false if the given index is out-of-bounds.
static bool
JavaArrayBoundsCheck(jarray arr, jsize index) {
    jsize len = Unwrap(arr)->Length();
    if (index < 0 || index >= len) {
        // TODO: Should technically throw ArrayIndexOutOfBoundsException.
        fprintf(stderr,
            "ERROR: Out-of-bounds array index %d in JNI method", index);
        abort();
        return false;
    }
    return true;
}

// Similar to GetJavaArrayData, but performs a
// bounds check for the specified region.
template <typename T>
static T*
GetJavaArrayDataForRegion(jarray arr, jsize start, jsize len) {
    if (len == 0)
        return nullptr;
    if (!JavaArrayBoundsCheck(arr, start)
            || !JavaArrayBoundsCheck(arr, start + len - 1))
        return nullptr;
    // Assumes no copy.
    return GetJavaArrayData<T>(arr, /*isCopy*/ nullptr);
}

template <typename T>
static void
GetJavaArrayRegion(jarray arr, jsize start, jsize len, T* buf) {
    if (auto data = GetJavaArrayDataForRegion<T>(arr, start, len)) {
        std::copy(data + start, data + start + len, buf);
    }
}

template <typename T>
static void
SetJavaArrayRegion(jarray arr, jsize start, jsize len, const T* buf) {
    if (auto data = GetJavaArrayDataForRegion<T>(arr, start, len)) {
        std::copy(buf, buf + len, data + start);
    }
}

// Parses one Java argument from a method signature and returns
// its single-character encoding. Updates the signature position.
static char
ParseJavaArg(const char*& s) {
    switch (*s) {
    case 'Z': case 'B': case 'C': case 'S':
    case 'I': case 'J': case 'F': case 'D':
        return *s++;
    case '[':
        ParseJavaArg(++s);
        return '[';
    case 'L':
        while (*s++ != ';')
            continue;
        return 'L';
    default:
        return '\0';
    }
}

static size_t
CountJavaArgs(const char* sig) {
    const char* s = sig;
    assert(*s == '(');
    ++s;

    size_t count = 0;
    while (*s != ')')
        ParseJavaArg(s), ++count;
    return count;
}

// Uses a Java method signature encoding to move
// a variable number of arguments into an array.
// Assumes the array is large enough.
static void
ForwardJavaArgs(const char* sig, va_list args, jvalue* out) {
    const char* s = sig;
    jvalue* o = out;

    assert(*s == '(');
    ++s;

    static_assert(
        sizeof(jint) == sizeof(int),
        "We assume that jint is represented with int.\n"
        "This is needed to handle vararg type promotions correctly.\n"
        "See http://en.cppreference.com/w/cpp/language/variadic_arguments");

    while (*s != ')') {
        // Remember that va_arg can only be used on int, long, double,
        // and pointers. We use static casts everywhere for visual consistency.
        switch (ParseJavaArg(s)) {
        case 'Z': o->z = static_cast<jboolean>(va_arg(args, jint));    break;
        case 'B': o->b = static_cast<jbyte>   (va_arg(args, jint));    break;
        case 'C': o->c = static_cast<jchar>   (va_arg(args, jint));    break;
        case 'S': o->s = static_cast<jshort>  (va_arg(args, jint));    break;
        case 'I': o->i = static_cast<jint>    (va_arg(args, jint));    break;
        case 'J': o->j = static_cast<jlong>   (va_arg(args, jlong));   break;
        case 'F': o->f = static_cast<jfloat>  (va_arg(args, jdouble)); break;
        case 'D': o->d = static_cast<jdouble> (va_arg(args, jdouble)); break;
        case '[': o->l = static_cast<jarray>  (va_arg(args, jarray));  break;
        case 'L': o->l = static_cast<jobject> (va_arg(args, jobject)); break;
        default:
            fprintf(stderr, "ERROR: Malformed method signature:\n\t%s\n", sig);
            abort();
        }
        ++o; // Advance out position.
    }
}

// This type must precisely match the type used by PolyLLVM.
template <typename T>
using JniTrampolineType = T (*)(void*, const jvalue*);


// Calls a Java method directly, without using a dispatch vector.
// All other method calling functions below delegate to this one.
template <typename T>
static T
CallJavaNonvirtualMethod(jmethodID id, const jvalue* args) {
    auto m = reinterpret_cast<const JavaMethodInfo*>(id);
    if (!m->fnPtr || !m->trampoline) {
        fprintf(stderr,
            "ERROR: Attempting to call Java method with trampoline %p\n"
            "       and function pointer %p\n", m->fnPtr, m->trampoline);
        abort();
    }
    auto trampoline = reinterpret_cast<JniTrampolineType<T>>(m->trampoline);
    return trampoline(m->fnPtr, args);
}

// Calls a Java instance method directly, without using a dispatch vector.
template <typename T>
static T
CallJavaNonvirtualMethod(jobject obj, jmethodID id, const jvalue* args) {
    auto m = reinterpret_cast<const JavaMethodInfo*>(id);
    auto num_args = CountJavaArgs(m->sig);

    // Carefully include implicit receiver.
    auto forward_args = std::vector<jvalue>(num_args + 1);
    forward_args[0].l = obj;
    std::copy(args, args + num_args, forward_args.begin() + 1);

    return CallJavaNonvirtualMethod<T>(id, args);
}

// Calls a Java instance method using the dispatch vector of [obj].
template <typename T>
static T
CallJavaInstanceMethod(jobject obj, jmethodID id, const jvalue* args) {
    auto m = reinterpret_cast<const JavaMethodInfo*>(id);

    // We could implement virtual dispatch by using dispatch vector
    // offsets. However, it's slightly simpler to just look up more
    // precise method info from the class of the current object,
    // which will include a direct function pointer. Then we
    // can do a direct call.
    auto clazz = Unwrap(obj)->Cdv()->Class()->Wrap();
    m = GetJavaMethodInfo(clazz, m->name, m->sig);
    id = reinterpret_cast<jmethodID>(const_cast<JavaMethodInfo*>(m));

    // TODO: The above lookup may fail due to type erasure,
    // in particular if [obj] has a generic super class with substituted
    // type parameter(s) which do not match the erasure type(s).
    // See https://docs.oracle.com/javase/tutorial/java/generics/bridgeMethods.html

    return CallJavaNonvirtualMethod<T>(obj, id, args);
}


template <typename T>
static T
CallJavaInstanceMethod(jobject obj, const char* name, const char* sig, const jvalue* args) {
  auto clazz = Unwrap(obj)->Cdv()->Class()->Wrap();
  auto m = GetJavaMethodInfo(clazz, name, sig);
  auto id = reinterpret_cast<jmethodID>(const_cast<JavaMethodInfo*>(m));
  return CallJavaNonvirtualMethod<T>(obj, id, args);
}

// Calls a Java instance method using the dispatch vector of [obj].
template <typename T>
static T
CallJavaInstanceMethod(jobject obj, jmethodID id, va_list args) {
    auto m = reinterpret_cast<const JavaMethodInfo*>(id);
    auto num_args = CountJavaArgs(m->sig);
    auto forward_args = std::vector<jvalue>(num_args);
    ForwardJavaArgs(m->sig, args, forward_args.data());
    return CallJavaInstanceMethod<T>(obj, id, forward_args.data());
}

template <typename T>
static T
CallJavaInstanceMethod(jobject obj, jclass intf, const char* name, const char* sig, const jvalue* args) {
  auto methodInfo = GetJavaMethodInfo(intf, name, sig);
  return reinterpret_cast<T>(__getInterfaceMethod(obj, methodInfo->intf_id_hash, methodInfo->intf_id, methodInfo->offset));
}
#endif
