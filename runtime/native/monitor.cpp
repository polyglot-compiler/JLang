// Copyright (C) 2019 Cornell University

#include "monitor.h"

Monitor &Monitor::Instance() {
    static Monitor *instance;
    if (instance == nullptr) {
        instance = new Monitor();
    }
    return *instance;
}

void Monitor::enter(jobject obj) { syncVars.push_back(obj); }

void Monitor::exit(jobject obj) {
    jobject enter = syncVars.back();
    syncVars.pop_back();
    // TODO: implement the actual monitor enter and exit.
    // A simple test to check the correctness of the inplementation of
    // synchronized keyword.
    if (enter != obj) {
        printf("EROOR: synchronized enter and exit should be in reverse order "
               "style.\n");
    }
}