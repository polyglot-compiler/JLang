// Copyright (C) 2019 Cornell University

#include "monitor.h"

#include "rep.h"
#include "threads.h"

#include <algorithm>
#include <assert.h>
#include <unordered_map>
#include <pthread.h>

#define GC_THREADS
#include <gc.h>
#undef GC_THREADS

//
// Monitor
//

static constexpr bool kDebug = false;
// static constexpr bool kDebug = true;

// A map used for sanity check.
// It is from a lock object to the thread_id which is currently holding this
// lock and the level of the recursive mutex.
// This map is shared by all threads.
std::unordered_map<jobject, std::pair<pthread_t, int>> lockMap;

static void initSyncVars(jobject obj) {
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

// A fake object to hold the sync_var of class loading function.
// The class loading code could utilize this global object to ensure that
// every class is only initilized by one thread once.
extern "C" jobject getGlobalMutexObject() {
    static JObjectRep __Polyglot_native_GlobalMutexObject;
    if (__Polyglot_native_GlobalMutexObject.SyncVars() == nullptr) {
        initSyncVars(__Polyglot_native_GlobalMutexObject.Wrap());
    }
    return __Polyglot_native_GlobalMutexObject.Wrap();
}

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
    {
        ScopedLock lock(&mutex);

        if (kDebug) {
            // sanity check
            Monitor::syncObjs.push_back(obj);
        }

        if (Unwrap(obj)->SyncVars() == nullptr) {
            initSyncVars(obj);
        }
    }

    pthread_mutex_lock(&Unwrap(obj)->SyncVars()->mutex);

    if (kDebug) {
        // sanity check
        ScopedLock lock(&mutex);
        if (lockMap[obj].second == 0) {
            lockMap[obj].first = pthread_self();
            lockMap[obj].second = 1;
        } else {
            assert(pthread_self() == lockMap[obj].first);
            lockMap[obj].second++;
        }
    }
}

void Monitor::exit(jobject obj) {
    {
        ScopedLock lock(&mutex);

        // sanity check
        if (kDebug) {
            jobject enter = Monitor::syncObjs.back();
            Monitor::syncObjs.pop_back();
            if (enter != obj) {
                printf("EROOR: synchronized enter and exit should be in "
                       "reverse order "
                       "style.\n");
            }
            if (Unwrap(obj)->SyncVars() == nullptr) {
                printf("SyncVars must have already been initialized in "
                       "MonitorEnter\n");
            }
        }
    }

    if (kDebug) {
        // sanity check
        ScopedLock lock(&mutex);
        assert(pthread_self() == lockMap[obj].first && lockMap[obj].second > 0);
        if (lockMap[obj].second == 1) {
            lockMap[obj].first = 0;
            lockMap[obj].second = 0;
        } else {
            lockMap[obj].second--;
        }
    }

    pthread_mutex_unlock(&Unwrap(obj)->SyncVars()->mutex);
}

void Monitor::wait(jobject obj, jlong ms) {
    int times;
    {
        ScopedLock lock(&mutex);

        if (kDebug) {
            // sanity check
            if (!hasEntered(obj)) {
                printf("wait() must be called when the object is locked.");
            }

            // sanity check
            assert(pthread_self() == lockMap[obj].first &&
                   lockMap[obj].second > 0);
            times = lockMap[obj].second;
            lockMap[obj].first = 0;
            lockMap[obj].second = 0;
        }
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

    if (kDebug) {
        // sanity check
        ScopedLock lock(&mutex);
        assert(lockMap[obj].second == 0);
        lockMap[obj].first = pthread_self();
        lockMap[obj].second = times;
    }
}

void Monitor::notify(jobject obj) {
    {
        ScopedLock lock(&mutex);

        if (kDebug && !hasEntered(obj)) {
            printf("notify() must be called when the object is locked.");
        }
    }

    sync_vars *syncVars = Unwrap(obj)->SyncVars();
    pthread_cond_signal(&syncVars->cond);
}

void Monitor::notifyAll(jobject obj) {
    {
        ScopedLock lock(&mutex);

        if (kDebug && !hasEntered(obj)) {
            printf("notifyAll() must be called when the object is locked.");
        }
    }
    sync_vars *syncVars = Unwrap(obj)->SyncVars();
    pthread_cond_broadcast(&syncVars->cond);
}

bool Monitor::hasEntered(jobject obj) {
    // Check if syncVars are initialized as a shortcut.
    if (Unwrap(obj)->SyncVars() == nullptr) {
        return false;
    }

    return std::find(Monitor::syncObjs.rbegin(), Monitor::syncObjs.rend(),
                     obj) != Monitor::syncObjs.rend();
}

thread_local std::deque<jobject> Monitor::syncObjs;

pthread_mutex_t *Monitor::globalMutex() {
    return &Unwrap(getGlobalMutexObject())->SyncVars()->mutex;
}

//
// ScopedLock
//

ScopedLock::ScopedLock(pthread_mutex_t *_mutex) : mutex(_mutex) {
    pthread_mutex_lock(mutex);
}

ScopedLock::~ScopedLock() { pthread_mutex_unlock(mutex); }