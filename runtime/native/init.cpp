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
    LoadJavaClassFromLib("java.lang.String");
    LoadJavaClassFromLib("java.lang.System");
    LoadJavaClassFromLib("java.lang.Thread");
    LoadJavaClassFromLib("java.lang.ThreadGroup");
    LoadJavaClassFromLib("java.lang.reflect.Method");
    LoadJavaClassFromLib("java.lang.ref.Finalizer");
    LoadJavaClassFromLib("java.lang.Class");

    // setup currentThread for main thread
    currentThread = GetMainThread();
    Threads::Instance().threads[currentThread].threadStatus = true;

    // initialize the system class
    Polyglot_java_lang_System_initializeSystemClass__();
}