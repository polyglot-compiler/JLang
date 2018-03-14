//
// Created by Daniel Donenfeld on 3/3/17.
//

#ifndef JNI_REFLECT_H
#define JNI_REFLECT_H

#include "rep.h"

extern "C" {

bool instanceof(jobject obj, void *compare_type_id);

}

#endif //JNI_REFLECT_H
