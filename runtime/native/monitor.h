// Copyright (C) 2019 Cornell University

#pragma once

#include <deque>
#include <jni.h>

class Monitor {
  private:
    std::deque<jobject> syncObjs;
    Monitor() = default;
    bool hasEntered(jobject obj);

  public:
    Monitor(const Monitor &) = delete;
    Monitor &operator=(const Monitor &) = delete;
    
    static Monitor &Instance();
    void enter(jobject obj);
    void exit(jobject obj);

    void wait(jobject obj, jlong ms);
    void notify(jobject obj);
    void notifyAll(jobject obj);
};
