// Copyright (C) 2019 Cornell University

#pragma once

#include <deque>
#include <jni.h>

class Monitor {
  private:
    std::deque<jobject> syncVars;
    Monitor() = default;

  public:
    Monitor(const Monitor &) = delete;
    Monitor &operator=(const Monitor &) = delete;
    
    static Monitor &Instance();
    void enter(jobject obj);
    void exit(jobject obj);
};
