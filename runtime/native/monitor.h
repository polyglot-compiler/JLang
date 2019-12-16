// Copyright (C) 2019 Cornell University

#pragma once

#include <deque>
#include <unordered_map>
#include <jni.h>
#include <pthread.h>

#define GC_THREADS
#include <gc.h>
#undef GC_THREADS

class Monitor {
  public:
    Monitor(const Monitor &monitor) = delete;
    Monitor &operator=(const Monitor &monitor) = delete;

    static Monitor &Instance();
    void enter(jobject obj);
    void exit(jobject obj);

    void wait(jobject obj, jlong ms);
    void notify(jobject obj);
    void notifyAll(jobject obj);

    pthread_mutex_t *globalMutex();

  private:
    static thread_local std::deque<jobject> syncObjs;
    pthread_mutex_t mutex;

    Monitor();
    bool hasEntered(jobject obj);
};

class ScopedLock {
  private:
    pthread_mutex_t *mutex;

  public:
    ScopedLock(pthread_mutex_t *_mutex);
    ~ScopedLock();
};