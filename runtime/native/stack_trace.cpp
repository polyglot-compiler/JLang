#include <cstdio>
#include <execinfo.h>
#include "stack_trace.h"

void DumpStackTrace() {
    // Dump stack trace.
    constexpr int max_frames = 256;
    void* callstack[max_frames];
    int frames = backtrace(callstack, max_frames);
    backtrace_symbols_fd(callstack, frames, fileno(stderr));
}
