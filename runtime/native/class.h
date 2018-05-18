// Provides reflection-like support, especially for use by JNI.
// PolyLLVM emits static information for each class, and registers
// that information by calling RegisterJavaClass upon loading a class.
// The other functions defined here provide access to
// class information in an organized way.
//
// For example, this interface can be used to get Java field offsets,
// Java method pointers, Java class names.
#pragma once

#include "jni.h"

// Returns the name of the given class (e.g., "java.lang.Object").
const char* GetJavaClassName(jclass cls);

// Returns a field ID, which should be interpreted as the byte offset of
// the named field. Returns null if the field cannot be found.
jfieldID GetJavaFieldId(jclass cls, const char* name);

// Returns a method ID, which is an opaque pointer to information
// need to invoke the specified method.
jmethodID GetJavaMethodID(jclass cls, const char* name, const char* sig);
