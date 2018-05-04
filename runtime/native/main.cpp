#include <csignal>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include "rep.h"
#include "gc.h"

extern "C" {

void Java_polyllvm_runtime_MainWrapper_runMain(jarray args);
jarray Java_polyllvm_runtime_Factory_createByteArray(jint len);
jarray Java_polyllvm_runtime_Factory_createObjectArray(jint len);
jstring Java_polyllvm_runtime_Factory_createString(jarray bytes);

} // extern "C"

static void sigaction(int sig, siginfo_t* info, void* ucontext) {
    const char* cause = "";
    if (sig == SIGSEGV)
        cause = "This likely indicates a null pointer exception.\n";
    if (sig == SIGFPE)
        cause = "This indicates an arithmetic exception "
                "(e.g., divide by zero).\n";
    fprintf(stderr,
        "- - - - - - - - - - - - - - - - - - - - - - - - - - -\n"
        "Aborting due to signal: %s\n%s"
        "- - - - - - - - - - - - - - - - - - - - - - - - - - -\n"
        , strsignal(sig), cause);
    fflush(stderr);
    abort();
}

int main(int argc, char** argv) {
    // Initialize the garbage collector.
    GC_INIT();

    // Set up signal handling to report (for example) null pointer exceptions.
    struct sigaction sa;
    sa.sa_sigaction = sigaction;
    sa.sa_mask = 0;
    sa.sa_flags =  SA_SIGINFO;
    sigaction(SIGSEGV, &sa, 0);
    sigaction(SIGBUS, &sa, 0);
    sigaction(SIGFPE, &sa, 0);

    // Ignore the 0th argument, which is the name of the program.
    --argc, ++argv;
    jarray jargs = Java_polyllvm_runtime_Factory_createObjectArray(argc);
    jstring* jargs_data = static_cast<jstring*>(Unwrap(jargs)->Data());
    for (int i = 0; i < argc; ++i) {
        size_t len = strlen(argv[i]);
        jarray jargBytes = Java_polyllvm_runtime_Factory_createByteArray(len);
        jbyte* data = static_cast<jbyte*>(Unwrap(jargBytes)->Data());
        memcpy(data, argv[i], len);
        jstring jargString = Java_polyllvm_runtime_Factory_createString(jargBytes);
        jargs_data[i] = jargString;
    }

    try {
        Java_polyllvm_runtime_MainWrapper_runMain(jargs);
    } catch (...) {
        fprintf(stderr,
            "- - - - - - - - - - - - - - - - - - - - - - - - - - -\n"
            "Aborting due to uncaught foreign (non-Java) exception.\n"
            "- - - - - - - - - - - - - - - - - - - - - - - - - - -\n");
        fflush(stderr);
        abort();
    }
}
