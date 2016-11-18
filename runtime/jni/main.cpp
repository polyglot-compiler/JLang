#include <string.h>
#include <stdio.h>
#include "types.h"

extern "C" {

void java_entry_point(jarray* args);
jarray* Java_Factory_createByteArray__I(jint len);
jarray* Java_Factory_createObjectArray__I(jint len);
jstring* Java_Factory_createString___3B(jarray* bytes);

} // extern "C"

int main(int argc, char** argv) {
    // Ignore the 0th argument, which is the name of the program.
    --argc; ++argv;
    jarray* jargs = Java_Factory_createObjectArray__I(argc);
    for (int i = 0; i < argc; ++i) {
        size_t len = strlen(argv[i]);
        jarray* jargBytes = Java_Factory_createByteArray__I(len);
        for (int j = 0; j < len; ++j)
            ((int64_t*) &jargBytes->data)[j] = argv[i][j];
        jstring* jargString = Java_Factory_createString___3B(jargBytes);
        ((jstring**) &jargs->data)[i] = jargString;
    }
    java_entry_point(jargs);
}
