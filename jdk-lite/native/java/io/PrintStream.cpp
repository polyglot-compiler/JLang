#include <cstdio>
#include <cstring>
#include <jni.h>

static void PrintJavaString(JNIEnv *env, jstring s) {
    auto chars = env->GetStringChars(s, /*isCopy*/ nullptr);
    auto len = env->GetStringLength(s);
    for (jint i = 0; i < len; ++i) {
        // Not perfect, with respect to character encoding.
        printf("%lc", chars[i]);
    }
    env->ReleaseStringChars(s, chars);
}

extern "C" {

void Java_java_io_PrintStream_println__(JNIEnv *env, jobject stream) {
    printf("\n");
}

void Java_java_io_PrintStream_print__Ljava_lang_String_2(JNIEnv *env, jobject stream, jstring s) {
    PrintJavaString(env, s);
}

void Java_java_io_PrintStream_println__Ljava_lang_String_2(JNIEnv *env, jobject stream, jstring s) {
    PrintJavaString(env, s);
    printf("\n");
}

void Java_java_io_PrintStream_print__Z(JNIEnv *env, jobject stream, jboolean n) {
    printf("%s", n ? "true" : "false");
}

void Java_java_io_PrintStream_println__Z(JNIEnv *env, jobject stream, jboolean n) {
    printf("%s\n", n ? "true" : "false");
}

void Java_java_io_PrintStream_print__B(JNIEnv *env, jobject stream, jbyte n) {
    printf("%d", n);
}

void Java_java_io_PrintStream_println__B(JNIEnv *env, jobject stream, jbyte n) {
    printf("%d\n", n);
}

void Java_java_io_PrintStream_print__S(JNIEnv *env, jobject stream, jshort n) {
    printf("%d", n);
}

void Java_java_io_PrintStream_println__S(JNIEnv *env, jobject stream, jshort n) {
    printf("%d\n", n);
}

void Java_java_io_PrintStream_print__C(JNIEnv *env, jobject stream, jchar c) {
    printf("%lc", c);
}

void Java_java_io_PrintStream_println__C(JNIEnv *env, jobject stream, jchar c) {
    printf("%lc\n", c);
}

void Java_java_io_PrintStream_print__I(JNIEnv *env, jobject stream, jint n) {
    printf("%d", n);
}

void Java_java_io_PrintStream_println__I(JNIEnv *env, jobject stream, jint n) {
    printf("%d\n", n);
}

void Java_java_io_PrintStream_print__J(JNIEnv *env, jobject stream, jlong n) {
    printf("%ld", n);
}

void Java_java_io_PrintStream_println__J(JNIEnv *env, jobject stream, jlong n) {
    printf("%ld\n", n);
}

void Java_java_io_PrintStream_print__F(JNIEnv *env, jobject stream, jfloat n) {
    printf("%f", n);
}

void Java_java_io_PrintStream_println__F(JNIEnv *env, jobject stream, jfloat n) {
    printf("%f\n", n);
}

void Java_java_io_PrintStream_print__D(JNIEnv *env, jobject stream, jdouble n) {
    printf("%f", n);
}

void Java_java_io_PrintStream_println__D(JNIEnv *env, jobject stream, jdouble n) {
    printf("%f\n", n);
}

void Java_java_io_PrintStream_flush__(JNIEnv *env, jobject stream) {
    fflush(stdout);
}

} // extern "C"
