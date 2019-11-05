// Copyright (C) 2019 Cornell University

#include "threads.h"

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

Threads::~Threads() {
    for (auto &it : threads) {
        it.second.join();
    }
}

void Threads::startThread(jobject jthread, std::function<void()> func) {
    threads.emplace(jthread, func);
}