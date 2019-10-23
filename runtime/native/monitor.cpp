// Copyright (C) 2019 Cornell University

#include "monitor.h"

#include <gc.h>
#include <pthread.h>

#include "rep.h"

Monitor &Monitor::Instance() {
    static Monitor *instance;
    if (instance == nullptr) {
        instance = new Monitor();
    }
    return *instance;
}

void Monitor::enter(jobject obj) {
    // sanity check
    syncVars.push_back(obj);

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
    static int i = 0;
    i++;
    // printf("lock %d\n", i);
    pthread_mutex_lock(&Unwrap(obj)->SyncVars()->mutex);
}

void Monitor::exit(jobject obj) {
    // sanity check
    jobject enter = syncVars.back();
    syncVars.pop_back();
    if (enter != obj) {
        printf("EROOR: synchronized enter and exit should be in reverse order "
               "style.\n");
    }

    if (Unwrap(obj)->SyncVars() == nullptr) {
        printf("SyncVars must have already been initialized in MonitorEnter\n");
    }
    static int i = 0;
    i++;
    // printf("unlock%d\n", i);
    pthread_mutex_unlock(&Unwrap(obj)->SyncVars()->mutex);
}