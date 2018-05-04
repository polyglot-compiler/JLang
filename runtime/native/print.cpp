#include <inttypes.h>
#include <stdio.h>
#include <string.h>
#include <jni.h>
#include "rep.h"

static void printJavaString(jstring s) {
    auto chars = Unwrap(s)->Chars();
    jchar* data = static_cast<jchar*>(chars->Data());
    jint len = chars->Length();
    for (jint i = 0; i < len; ++i) {
        printf("%lc", data[i]);
    }
}

extern "C" {

void Java_java_lang_System_PrintStream_println__(JNIEnv* env) {
    printf("\n");
}

void Java_java_lang_System_PrintStream_print__Ljava_lang_String_2(JNIEnv* env, jstring s) {
    printJavaString(s);
}

void Java_java_lang_System_PrintStream_println__Ljava_lang_String_2(JNIEnv* env, jstring s) {
    printJavaString(s);
    printf("\n");
}

void Java_java_lang_System_PrintStream_print__Z(JNIEnv* env, jboolean n) {
    printf("%s", n ? "true" : "false");
}

void Java_java_lang_System_PrintStream_println__Z(JNIEnv* env, jboolean n) {
    printf("%s\n", n ? "true" : "false");
}

void Java_java_lang_System_PrintStream_print__B(JNIEnv* env, jbyte n) {
    printf("%d", n);
}

void Java_java_lang_System_PrintStream_println__B(JNIEnv* env, jbyte n) {
    printf("%d\n", n);
}

void Java_java_lang_System_PrintStream_print__S(JNIEnv* env, jshort n) {
    printf("%d", n);
}

void Java_java_lang_System_PrintStream_println__S(JNIEnv* env, jshort n) {
    printf("%d\n", n);
}

void Java_java_lang_System_PrintStream_print__C(JNIEnv* env, jchar c) {
    printf("%lc", c);
}

void Java_java_lang_System_PrintStream_println__C(JNIEnv* env, jchar c) {
    printf("%lc\n", c);
}

void Java_java_lang_System_PrintStream_print__I(JNIEnv* env, jint n) {
    printf("%d", n);
}

void Java_java_lang_System_PrintStream_println__I(JNIEnv* env, jint n) {
    printf("%d\n", n);
}

void Java_java_lang_System_PrintStream_print__J(JNIEnv* env, jlong n) {
    printf("%ld", n);
}

void Java_java_lang_System_PrintStream_println__J(JNIEnv* env, jlong n) {
    printf("%ld\n", n);
}

void Java_java_lang_System_PrintStream_print__F(JNIEnv* env, jfloat n) {
    printf("%f", n);
}

void Java_java_lang_System_PrintStream_println__F(JNIEnv* env, jfloat n) {
    printf("%f\n", n);
}

void Java_java_lang_System_PrintStream_print__D(JNIEnv* env, jdouble n) {
    printf("%f", n);
}

void Java_java_lang_System_PrintStream_println__D(JNIEnv* env, jdouble n) {
    printf("%f\n", n);
}


//TEMP: Remove when java library included properly.
jarray Java_polyllvm_runtime_Factory_createByteArray__I(jint len);
jstring Java_polyllvm_runtime_Factory_createString___3B(jarray bytes);

jstring cstring_to_jstring(const char* cstr) {
    size_t len = strlen(cstr);
    jarray jargBytes = Java_polyllvm_runtime_Factory_createByteArray__I(len);
    jbyte* data = static_cast<jbyte*>(Unwrap(jargBytes)->Data());
    memcpy(data, cstr, len);
    jstring jstr = Java_polyllvm_runtime_Factory_createString___3B(jargBytes);
    return jstr;
}

jstring Java_java_lang_String_valueOf__Z(JNIEnv* env, jboolean b) {
   const char* str = b ? "true" : "false";
   return cstring_to_jstring(str);
}

jstring Java_java_lang_String_valueOf__B(JNIEnv* env, jbyte n) {
    size_t len = 5; //Max of 4 chars for byte, 1 char for null terminator
    char str[len];
    sprintf(str, "%d", n);
    return cstring_to_jstring(str);
}

jstring Java_java_lang_String_valueOf__C(JNIEnv* env, jchar c) {
    size_t len = 3; //Max of 2 chars for jchar (as java characters are 2 bytes), 1 char for null terminator
    char str[len];
    sprintf(str, "%lc", c);
    return cstring_to_jstring(str);
}

jstring Java_java_lang_String_valueOf__S(JNIEnv* env, jshort n) {
    size_t len = 7; //Max of 6 chars for short, 1 char for null terminator
    char str[len];
    sprintf(str, "%d", n);
    return cstring_to_jstring(str);
}

jstring Java_java_lang_String_valueOf__I(JNIEnv* env, jint n) {
    size_t len = 12; //Max of 11 chars for int, 1 char for null terminator
    char str[len];
    sprintf(str, "%d", n);
    return cstring_to_jstring(str);
}

jstring Java_java_lang_String_valueOf__J(JNIEnv* env, jlong n) {
    size_t len = 21; //Max of 20 chars for long, 1 char for null terminator
    char str[len];
    sprintf(str, "%ld", n);
    return cstring_to_jstring(str);
}

jstring Java_java_lang_String_valueOf__F(JNIEnv* env, jfloat n) {
    size_t len = 50; // Overestimate.
    char str[len];
    sprintf(str, "%f", n);
    return cstring_to_jstring(str);
}

jstring Java_java_lang_String_valueOf__D(JNIEnv* env, jdouble n) {
    size_t len = 50; // Overestimate.
    char str[len];
    sprintf(str, "%f", n);
    return cstring_to_jstring(str);
}

void Java_java_lang_System_PrintStream_flush(JNIEnv* env) {
    fflush(stdout);
}

} // extern "C"
