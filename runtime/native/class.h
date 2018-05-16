#ifndef CLASS_H
#define CLASS_H
#include "jni.h"

// Returns the name of the given class (e.g., "java.lang.Object").
const char* GetJavaClassName(jclass cls);

// Returns a field ID, which should be interpreted as the byte offset of
// the named field. Returns null if the field cannot be found.
jfieldID GetJavaFieldId(jclass cls, const char* name);

#endif // CLASS_H
