#include <sys/time.h>
#include <assert.h>
#include "rep.h"

extern "C" {

	jlong Java_java_lang_System_currentTimeMillis() {
		timeval time;
		int status = gettimeofday(&time, NULL);
		assert(status != -1);
		return jlong(time.tv_sec) * 1000  +  jlong(time.tv_usec / 1000);
	}

	jlong Java_java_lang_System_nanoTime() {
		timeval time;
		int status = gettimeofday(&time, NULL);
		assert(status != -1);
		jlong usecs = jlong(time.tv_sec) * (1000 * 1000) + jlong(time.tv_usec);
		return 1000 * usecs;
	}

}

