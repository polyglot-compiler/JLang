#include "init.h"

#include "class.h"
#include "jvm.h"
#include "factory.h"
#include "threads.h"

extern "C" {
void Polyglot_java_lang_System_initializeSystemClass__();
} // extern "C"

void InitializeMainThread() {
    // initialize classes
    FindClass("java.lang.String");
    FindClass("java.lang.System");
    FindClass("java.lang.Thread");
    FindClass("java.lang.ThreadGroup");
    FindClass("java.lang.reflect.Method");
    FindClass("java.lang.ref.Finalizer");
    FindClass("java.lang.Class");

    // setup currentThread for main thread
    currentThread = GetMainThread();
    Threads::Instance().threads[currentThread].threadStatus = true;

    // initialize the system class
    Polyglot_java_lang_System_initializeSystemClass__();
}