#include <csignal>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include "types.h"
#include "gc.h"

extern "C" {

void Java_polyllvm_runtime_MainWrapper_runMain___3Ljava_lang_String_2(jarray args);
jarray Java_polyllvm_runtime_Factory_createByteArray__I(jint len);
jarray Java_polyllvm_runtime_Factory_createObjectArray__I(jint len);
jstring Java_polyllvm_runtime_Factory_createString___3B(jarray bytes);

} // extern "C"

static void sigaction(int sig, siginfo_t* info, void* ucontext) {
    fprintf(stderr, "Aborting due to signal: %s\n", strsignal(sig));
    if (sig == SIGSEGV)
        fprintf(stderr, "This probably indicates a null pointer exception.\n");
    if (sig == SIGFPE)
        fprintf(stderr, "This indicates an arithmetic exception "
                        "(e.g., divide by zero).\n");
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
    jarray jargs = Java_polyllvm_runtime_Factory_createObjectArray__I(argc);
    for (int i = 0; i < argc; ++i) {
        size_t len = strlen(argv[i]);
        jarray jargBytes = Java_polyllvm_runtime_Factory_createByteArray__I(len);
        for (int j = 0; j < len; ++j)
            ((int8_t*) &jargBytes->data)[j] = argv[i][j];
        jstring jargString = Java_polyllvm_runtime_Factory_createString___3B(jargBytes);
        ((jstring*) &jargs->data)[i] = jargString;
    }

    try {
        Java_polyllvm_runtime_MainWrapper_runMain___3Ljava_lang_String_2(jargs);
    } catch (...) {
        printf("Aborting due to uncaught foreign (non-Java) exception.\n");
        fflush(stdout);
        abort();
    }
}
