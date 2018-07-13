#include "nativetest.h"

extern "C" {
  JNIEXPORT jstring JNICALL
  Java_NativeTest_getNativeStringTest(JNIEnv *env, jclass ignored) {
    jstring jkey = env->NewStringUTF("testNativeCall");
    return jkey;
  }
}
