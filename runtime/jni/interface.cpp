#include <inttypes.h>
#include <string.h>
#include <cstdlib>
#include "types.h"

/*
 * Pseudo Code for Interface lookup:
 * it = get itable out of DV (is an i8**)
 * while (it != null){
 *   if (strcmp(it[1], INTERFACE_STRING) == 0 ){
 *     get itable, perform dispatch
 *     break
 *   } else {
 *     advance it to it[0]
 *   }
 * }
 *
 */

extern "C" {

void* __getInterfaceMethod(jobject* obj, char* interface_string, int methodIndex) {
    it* itable = obj->dv->it;
    while(itable != 0){
        if (strcmp(itable->interface_name, interface_string) == 0) {
            return ((void **) itable)[methodIndex];
        } else {
            itable = itable->next;
        }
    }
    std::abort(); //Should not reach here
}

} // extern "C"
