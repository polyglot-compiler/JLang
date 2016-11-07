#include <inttypes.h>
#include "types.h"

#include <stdio.h>

extern "C" {

static void printWithFormat(jstring* s, const char* format) {
    int64_t* data = (int64_t*) &s->chars->data;
    jint len = s->chars->len;

    // TODO: This does not support 16-bit characters.
    char chars[len];
    for (jint i = 0; i < len; ++i) {
        chars[i] = (char) data[i];
    }

    printf(format, chars);
}

void Java_placeholder_Print_print__Ljava_lang_String_2(jstring* s) {
    printWithFormat(s, "%s");
}

void Java_placeholder_Print_println__Ljava_lang_String_2(jstring* s) {
    printWithFormat(s, "%s\n");
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
