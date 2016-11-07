#include <inttypes.h>
#include <string.h>
#include <cstdlib>
#include "types.h"

class it {
public:
    it* next;
    char* interface_name;
};


class dv {
public:
    it* it;
    void* type_info;
};

class java_obj {
public:
    dv* dv;
};



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

using jobject = java_obj;

void* __getInterfaceMethod(void* obj, char* interface_string, int methodIndex) {
    jobject* o = (jobject*) obj;
    it* itable = o->dv->it;
    while(itable != 0){
        if(strcmp(itable->interface_name, interface_string) == 0){
            return ((void **) itable)[methodIndex];
        } else {
            itable = itable->next;
        }
    }
    std::abort(); //Should not reach here
}

}