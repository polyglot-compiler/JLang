#include <inttypes.h>
#include <stdio.h>
#include "types.h"

static void printJavaStringWithFormat(jstring* s, bool newline) {
    uint16_t* data = (uint16_t*) &s->chars->data;
    jint len = s->chars->len;
    for (jint i = 0; i < len; ++i) {
        printf("%lc", data[i]);
    }
    if (newline) {
        printf("\n");
    }
}

extern "C" {

void Java_placeholder_Print_print__Ljava_lang_String_2(jstring* s) {
    printJavaStringWithFormat(s, /* newline */ false);
}

void Java_placeholder_Print_println__Ljava_lang_String_2(jstring* s) {
    printJavaStringWithFormat(s, /* newline */ true);
}

void Java_placeholder_Print_print__Z(jbool n) {
    printf("%s", n ? "true" : "false");
}

void Java_placeholder_Print_println__Z(jbool n) {
    printf("%s\n", n ? "true" : "false");
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
