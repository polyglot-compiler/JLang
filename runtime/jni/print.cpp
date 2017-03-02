#include <inttypes.h>
#include <stdio.h>
#include "types.h"

static void printJavaString(jstring* s) {
    uint16_t* data = (uint16_t*) &s->chars->data;
    jint len = s->chars->len;
    for (jint i = 0; i < len; ++i) {
        printf("%lc", data[i]);
    }
}

extern "C" {

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

} // extern "C"
