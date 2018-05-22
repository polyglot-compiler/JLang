#include <cstring>
#include <jni.h>

jstring Java_java_lang_String_valueOf__Z(JNIEnv *env, jboolean b) {
   const char* str = b ? "true" : "false";
   return env->NewStringUTF(str);
}

jstring Java_java_lang_String_valueOf__B(JNIEnv *env, jbyte n) {
    size_t len = 5; // Max of 4 chars for byte, 1 char for null terminator
    char str[len];
    sprintf(str, "%d", n);
    return env->NewStringUTF(str);
}

jstring Java_java_lang_String_valueOf__C(JNIEnv *env, jchar c) {
    size_t len = 3; // Max of 2 chars for jchar (as java characters are 2 bytes), 1 char for null terminator
    char str[len];
    sprintf(str, "%lc", c);
    return env->NewStringUTF(str);
}

jstring Java_java_lang_String_valueOf__S(JNIEnv *env, jshort n) {
    size_t len = 7; // Max of 6 chars for short, 1 char for null terminator
    char str[len];
    sprintf(str, "%d", n);
    return env->NewStringUTF(str);
}

jstring Java_java_lang_String_valueOf__I(JNIEnv *env, jint n) {
    size_t len = 12; // Max of 11 chars for int, 1 char for null terminator
    char str[len];
    sprintf(str, "%d", n);
    return env->NewStringUTF(str);
}

jstring Java_java_lang_String_valueOf__J(JNIEnv *env, jlong n) {
    size_t len = 21; // Max of 20 chars for long, 1 char for null terminator
    char str[len];
    sprintf(str, "%ld", n);
    return env->NewStringUTF(str);
}

jstring Java_java_lang_String_valueOf__F(JNIEnv *env, jfloat n) {
    size_t len = 50; // Overestimate.
    char str[len];
    sprintf(str, "%f", n);
    return env->NewStringUTF(str);
}

jstring Java_java_lang_String_valueOf__D(JNIEnv *env, jdouble n) {
    size_t len = 50; // Overestimate.
    char str[len];
    sprintf(str, "%f", n);
    return env->NewStringUTF(str);
}
