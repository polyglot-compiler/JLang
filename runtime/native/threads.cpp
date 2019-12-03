// Copyright (C) 2019 Cornell University

#include "threads.h"

#include "monitor.h"

jobject GetMainThread() {
    // Caveat: The name of the main ThreadGroup is "system", which is different
    // from Java's behavior, "main".
    //
    // Even though there is a ThreadGroup(String name) constructor, we could
    // not use it since it assumes that the current thread has already been
    // initialized, which is not the case here. If we really want to follow
    // Java's behavior, we could patch the JDK to have another contructor for 
    // this.

    static jobject mainThread = nullptr;

    if (mainThread) {
        return mainThread;
    }

    // create JavaGroup object associated with main Thread.
    auto thread_group_clazz = FindClass("java.lang.ThreadGroup");
    jobject mainThreadGroup = CreateJavaObject(thread_group_clazz);
    CallJavaInstanceMethod<jobject>(mainThreadGroup, "<init>", "()V", nullptr);

    // create "main" String object
    const char mainChar[] = "main"; 
    const size_t mainCharLen = 4;
    jcharArray mainCharArray = CreateJavaCharArray(mainCharLen);
    jchar *data = reinterpret_cast<jchar *>(Unwrap(mainCharArray)->Data());
    // TODO: Not a proper character encoding conversion.
    for (size_t i = 0; i < mainCharLen; ++i) {
        data[i] = static_cast<jchar>(mainChar[i]);
    }
    jstring mainStr = CreateJavaString(mainCharArray);

    // create main Thread object.
    auto thread_clazz = FindClass("java.lang.Thread");
    mainThread = CreateJavaObject(thread_clazz);
    const jobject mainThreadArgs[] = {mainThreadGroup, mainStr};
    CallJavaInstanceMethod<jobject>(
        mainThread, "<init>",
        "(Ljava/lang/ThreadGroup;Ljava/lang/String;)V",
        reinterpret_cast<const jvalue *>(mainThreadArgs));
    
    return mainThread;
}

thread_local jobject currentThread = nullptr;

Threads &Threads::Instance() {
    static Threads *instance;
    if (instance == nullptr) {
        instance = new Threads();
    }
    return *instance;
}

void Threads::join() {
    for (auto &it : threads) {
        if (it.first == GetMainThread()) {
            // skip the main thread.
            return;
        }

        jboolean isDaemon = CallJavaInstanceMethod<jboolean>(it.first, "isDaemon", "()Z", nullptr);
        if (isDaemon) {
            continue;
        }
        int ret = pthread_join(it.second.tid, nullptr);
        if (ret) {
            perror("cannot join thread");
        }
    }
}

void* start_routine(void* jthread) {
    jobject thread = static_cast<jobject>(jthread);
    currentThread = thread;
    
    CallJavaInstanceMethod<void>(thread, "run", "()V", nullptr);

    Threads::Instance().threads[thread].threadStatus = false;
    // invoke jthread.notifyAll() when the thread terminates.
    // TODO: notifyAll() also needs to be invoked when exception happens
    Monitor::Instance().enter(thread);
    Monitor::Instance().notifyAll(thread);
    Monitor::Instance().exit(thread);

    return nullptr;
}

void Threads::startThread(jobject jthread) {
    Threads::Instance().threads[jthread].threadStatus = true;
    int ret = pthread_create(&threads[jthread].tid, nullptr, start_routine, jthread);
    if (ret) {
        perror("cannot start thread\n");
    }
}
