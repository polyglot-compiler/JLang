#include <jni.h>
#include <stdio.h>
#include "IntAdd.h"
 
JNIEXPORT void JNICALL Java_IntAdd_print(JNIEnv * e, jclass c, jint i){
    printf("%d", i);
}
//JNIEXPORT void JNICALL Java_HelloJNI_sayHello(JNIEnv *env, jobject thisObj) {
//     printf("Hello World!\n");
//        return;
//}
