// Copyright (C) 2019 Cornell University

#include "threads.h"

thread_local jobject currentThread = nullptr;
thread_local bool currentThreadState = false;

Threads::Threads() {
    pthread_mutexattr_t attr;
    pthread_mutexattr_init(&attr);
    pthread_mutexattr_settype(&attr, PTHREAD_MUTEX_RECURSIVE);
    if (pthread_mutex_init(&globalMutex, &attr) != 0) {
        perror("mutex init failed");
    }
}

Threads &Threads::Instance() {
    static Threads *instance;
    if (instance == nullptr) {
        instance = new Threads();
    }
    return *instance;
}

void Threads::join() {
    for (auto &it : threads) {
        int ret = pthread_join(it.second, nullptr);
        if (ret) {
            perror("cannot join thread");
        }
    }
}

void* start_routine(void* jthread) {
    jobject thread = static_cast<jobject>(jthread);
    currentThread = thread;
    currentThreadState = true;
    CallJavaInstanceMethod<jobject>(thread, "run", "()V", nullptr);
    return nullptr;
}

void Threads::startThread(jobject jthread) {
    int ret = pthread_create(&threads[jthread], nullptr, start_routine, jthread);
    if (ret) {
        perror("cannot start thread\n");
    }
}