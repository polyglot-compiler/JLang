#include <inttypes.h>
#include <stdio.h>
#include "types.h"

static void printJavaStringWithFormat(const char* format, jstring* s) {
    intptr_t* data = (intptr_t*) &s->chars->data;
    jint len = s->chars->len;
    wchar_t chars[len+1];
    chars[len] = 0;
    for (jint i = 0; i < len; ++i)
        chars[i] = (wchar_t) data[i];
    printf(format, chars);
}

extern "C" {

void Java_placeholder_Print_print__Ljava_lang_String_2(jstring* s) {
    printJavaStringWithFormat("%ls", s);
}

void Java_placeholder_Print_println__Ljava_lang_String_2(jstring* s) {
    printJavaStringWithFormat("%ls\n", s);
}

void Java_placeholder_Print_print__I(jint n) {
    printf("%d", n);
}

void Java_placeholder_Print_println__I(jint n) {
    printf("%d\n", n);
}

void Java_placeholder_Print_print__J(jlong n) {
    // Portable formatting for int64_t
    printf("%" PRId64, n);
}

void Java_placeholder_Print_println__J(jlong n) {
    // Portable formatting for int64_t
    printf("%" PRId64 "\n", n);
}

void Java_placeholder_Print_print__F(jfloat n) {
    printf("%f", n);
}

void Java_placeholder_Print_println__F(jfloat n) {
    printf("%f\n", n);
}

void Java_placeholder_Print_print__D(jdouble n) {
    printf("%f", n);
}

void Java_placeholder_Print_println__D(jdouble n) {
    printf("%f\n", n);
}

} // extern "C"
