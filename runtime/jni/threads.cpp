#include "types.h"
#include <pthread.h>

extern "C" {

pthread_mutex_t global_mutex = PTHREAD_MUTEX_INITIALIZER;

void init_mutex(jobject obj) {

}

void init_cond_var(jobject obj) {

}

} // extern "C"
