#include <cstdio>
#include <cstdlib>
#include <execinfo.h>
#include "jni.h"

[[noreturn]] static void unlinked(const char* name) {
    fprintf(stderr,
        "- - - - - - - - - - - - - - - - - - - - - - - - - - -\n"
        "The following native method is currently unlinked:\n"
        "  %s\n"
        "This stub is defined in " __FILE__ ".\n"
        "Aborting for now.\n"
        "- - - - - - - - - - - - - - - - - - - - - - - - - - -\n"
        , name);

    // Dump stack trace.
    constexpr int max_frames = 256;
    void* callstack[max_frames];
    int frames = backtrace(callstack, max_frames);
    backtrace_symbols_fd(callstack, frames, fileno(stderr));

    fflush(stderr);
    abort();
}

static void warn(const char* name) {
    fprintf(stderr, "WARNING: Native method %s is currently unlinked, but will not abort.\n", name);
    fflush(stderr);
}

extern "C" {

// Begin weird anomalies.

void Polyglot_java_lang_Enum_compareTo__Ljava_lang_Object_2() {
    // For some reason Polyglot adds this method to java.lang.Enum with
    // the wrong argument type, in addition to the correct version.
    // This should be fixed, because this likely breaks
    // method dispatch for enums.
    unlinked("Polyglot_java_lang_Enum_compareTo__Ljava_lang_Object_2");
}

} // extern "C"
