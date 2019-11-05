// Copyright (C) 2019 Cornell University

#include "monitor.h"

#include "rep.h"

#include <algorithm>
#include <gc.h>
#include <pthread.h>

//
// Monitor
//

Monitor::Monitor() {
    if (pthread_mutex_init(&mutex, nullptr) != 0) {
        perror("mutex init failed");
    }
}

Monitor &Monitor::Instance() {
    static Monitor *instance;
    if (instance == nullptr) {
        instance = new Monitor();
    }
    return *instance;
}

void Monitor::enter(jobject obj) {
    ScopedLock lock(&mutex);

    // sanity check
    syncObjs.push_back(obj);

    // TODO: use global lock to synchronize initialization.
    if (Unwrap(obj)->SyncVars() == nullptr) {
        sync_vars *syncVars =
            reinterpret_cast<sync_vars *>(GC_MALLOC(sizeof(sync_vars)));

        pthread_mutexattr_t attr;
        pthread_mutexattr_init(&attr);
        pthread_mutexattr_settype(&attr, PTHREAD_MUTEX_RECURSIVE);
        if (pthread_mutex_init(&syncVars->mutex, &attr) != 0) {
            perror("mutex init failed");
        }

        if (pthread_cond_init(&syncVars->cond, nullptr) != 0) {
            perror("condition variable init failed");
        }

        Unwrap(obj)->SetSyncVars(syncVars);
    }

    pthread_mutex_lock(&Unwrap(obj)->SyncVars()->mutex);
}

void Monitor::exit(jobject obj) {
    ScopedLock lock(&mutex);

    // sanity check
    jobject enter = syncObjs.back();
    syncObjs.pop_back();
    if (enter != obj) {
        printf("EROOR: synchronized enter and exit should be in reverse order "
               "style.\n");
    }
    if (Unwrap(obj)->SyncVars() == nullptr) {
        printf("SyncVars must have already been initialized in MonitorEnter\n");
    }

    pthread_mutex_unlock(&Unwrap(obj)->SyncVars()->mutex);
}

void Monitor::wait(jobject obj, jlong ms) {
    ScopedLock lock(&mutex);

    // sanity check
    if (!hasEntered(obj)) {
        printf("wait() must be called when the object is locked.");
    }

    sync_vars *syncVars = Unwrap(obj)->SyncVars();
    if (ms == 0) {
        // wait until notified
        pthread_cond_wait(&syncVars->cond, &syncVars->mutex);
    } else {
        timespec t;
        clock_gettime(CLOCK_REALTIME, &t);
        t.tv_sec += ms / 1000;
        t.tv_nsec += (ms % 1000) * 1000;

        t.tv_sec += t.tv_nsec / 1'000'000'000;
        t.tv_nsec = t.tv_nsec % 1'000'000'000;

        pthread_cond_timedwait(&syncVars->cond, &syncVars->mutex, &t);
    }
}

void Monitor::notify(jobject obj) {
    ScopedLock lock(&mutex);

    if (!hasEntered(obj)) {
        printf("notify() must be called when the object is locked.");
    }

    sync_vars *syncVars = Unwrap(obj)->SyncVars();
    pthread_cond_signal(&syncVars->cond);
}

void Monitor::notifyAll(jobject obj) {
    ScopedLock lock(&mutex);

    if (!hasEntered(obj)) {
        printf("notifyAll() must be called when the object is locked.");
    }

    sync_vars *syncVars = Unwrap(obj)->SyncVars();
    pthread_cond_broadcast(&syncVars->cond);
}

bool Monitor::hasEntered(jobject obj) {
    ScopedLock lock(&mutex);

    // Check if syncVars are initialized as a shortcut.
    if (Unwrap(obj)->SyncVars() == nullptr) {
        return false;
    }

    return std::find(syncObjs.rbegin(), syncObjs.rend(), obj) !=
           syncObjs.rend();
}

//
// ScopedLock
//

ScopedLock::ScopedLock(pthread_mutex_t *_mutex) : mutex(_mutex) {
    pthread_mutex_lock(mutex);
}

ScopedLock::~ScopedLock() { pthread_mutex_unlock(mutex); }