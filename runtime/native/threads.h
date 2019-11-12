// Copyright (C) 2019 Cornell University

#include <functional>
#include <jvm.h>
#include <thread>
#include <unordered_map>

extern thread_local jobject currentThread;
extern thread_local bool currentThreadState;

class Threads {
  public:
    Threads(const Threads &threads) = delete;
    Threads &operator=(const Threads &threads) = delete;
    static Threads &Instance();

    void startThread(jobject jthread);
    void join();

    pthread_mutex_t globalMutex;

  private:
    Threads();
    std::unordered_map<jobject, pthread_t> threads;
};
