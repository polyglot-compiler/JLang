// Copyright (C) 2019 Cornell University

#include <functional>
#include <jvm.h>
#include <thread>
#include <unordered_map>

class Threads {
  public:
    Threads(const Threads &threads) = delete;
    Threads &operator=(const Threads &threads) = delete;
    static Threads &Instance();

    ~Threads();
    void startThread(jobject jthread, std::function<void()> func);

    pthread_mutex_t globalMutex;

  private:
    Threads();
    std::unordered_map<jobject, std::thread> threads;
};