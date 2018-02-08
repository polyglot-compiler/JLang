#include <inttypes.h>
#include <stdio.h>
#include <string.h>
#include "types.h"

static void printJavaString(jstring* s) {
    jchar* data = (jchar*) &s->chars->data;
    jint len = s->chars->len;
    for (jint i = 0; i < len; ++i) {
        printf("%lc", data[i]);
    }
}

extern "C" {

void Java_java_lang_System_PrintStream_println__() {
    printf("\n");
}

void Java_java_lang_System_PrintStream_print__Ljava_lang_String_2(jstring* s) {
    printJavaString(s);
}

void Java_java_lang_System_PrintStream_println__Ljava_lang_String_2(jstring* s) {
    printJavaString(s);
    printf("\n");
}

void Java_java_lang_System_PrintStream_print__Z(jbool n) {
    printf("%s", n ? "true" : "false");
}

void Java_java_lang_System_PrintStream_println__Z(jbool n) {
    printf("%s\n", n ? "true" : "false");
}

void Java_java_lang_System_PrintStream_print__B(jbyte n) {
    printf("%d", n);
}

void Java_java_lang_System_PrintStream_println__B(jbyte n) {
    printf("%d\n", n);
}

void Java_java_lang_System_PrintStream_print__S(jshort n) {
    printf("%d", n);
}

void Java_java_lang_System_PrintStream_println__S(jshort n) {
    printf("%d\n", n);
}

void Java_java_lang_System_PrintStream_print__C(jchar c) {
    printf("%lc", c);
}

void Java_java_lang_System_PrintStream_println__C(jchar c) {
    printf("%lc\n", c);
}

void Java_java_lang_System_PrintStream_print__I(jint n) {
    printf("%d", n);
}

void Java_java_lang_System_PrintStream_println__I(jint n) {
    printf("%d\n", n);
}

void Java_java_lang_System_PrintStream_print__J(jlong n) {
    // Portable formatting for int64_t
    printf("%" PRId64, n);
}

void Java_java_lang_System_PrintStream_println__J(jlong n) {
    // Portable formatting for int64_t
    printf("%" PRId64 "\n", n);
}

void Java_java_lang_System_PrintStream_print__F(jfloat n) {
    printf("%f", n);
}

void Java_java_lang_System_PrintStream_println__F(jfloat n) {
    printf("%f\n", n);
}

void Java_java_lang_System_PrintStream_print__D(jdouble n) {
    printf("%f", n);
}

void Java_java_lang_System_PrintStream_println__D(jdouble n) {
    printf("%f\n", n);
}


//TEMP: Remove when java library included properly.
jarray* Java_support_Factory_createByteArray__I(jint len);
jarray* Java_support_Factory_createObjectArray__I(jint len);
jstring* Java_support_Factory_createString___3B(jarray* bytes);

jstring* cstring_to_jstring(const char* cstr) {
    size_t len = strlen(cstr);
    jarray* jargBytes = Java_support_Factory_createByteArray__I(len);
    for (int j = 0; j < len; ++j)
        ((int8_t*) &jargBytes->data)[j] = cstr[j];
    jstring* jstr = Java_support_Factory_createString___3B(jargBytes);
    return jstr;
}

jstring* Java_java_lang_String_valueOf__Z(jbool b) {
   const char* str = b ? "true" : "false";
   return cstring_to_jstring(str);
}

jstring* Java_java_lang_String_valueOf__B(jbyte n) {
    size_t len = 5; //Max of 4 chars for byte, 1 char for null terminator
    char str[len];
    sprintf(str, "%d", n);
    return cstring_to_jstring(str);
}

jstring* Java_java_lang_String_valueOf__C(jchar c) {
    size_t len = 3; //Max of 2 chars for jchar (as java characters are 2 bytes), 1 char for null terminator
    char str[len];
    sprintf(str, "%lc", c);
    return cstring_to_jstring(str);
}

jstring* Java_java_lang_String_valueOf__S(jshort n) {
    size_t len = 7; //Max of 6 chars for short, 1 char for null terminator
    char str[len];
    sprintf(str, "%d", n);
    return cstring_to_jstring(str);
}

jstring* Java_java_lang_String_valueOf__I(jint n) {
    size_t len = 12; //Max of 11 chars for int, 1 char for null terminator
    char str[len];
    sprintf(str, "%d", n);
    return cstring_to_jstring(str);
}

jstring* Java_java_lang_String_valueOf__J(jlong n) {
    size_t len = 21; //Max of 20 chars for long, 1 char for null terminator
    char str[len];
    sprintf(str, "%" PRId64, n);
    return cstring_to_jstring(str);
}

void Java_java_lang_System_PrintStream_flush__() {
    fflush(stdout);
}

} // extern "C"
