#include <stdexcept>
#include <cstring>

#include <unwind.h>
#include <inttypes.h>
#include <gc.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>

typedef struct _Unwind_Exception UnwindException;

extern "C" {

struct JavaException_t {
  void* jexception;

  // Note: This is properly aligned in unwind.h
  UnwindException unwindException;
};

/// Generates our _Unwind_Exception class from a given character array.
/// thereby handling arbitrary lengths (not in standard), and handling
/// embedded \0s.
/// See @link http://mentorembedded.github.com/cxx-abi/abi-eh.html @unlink
/// @param classChars char array to encode. NULL values not checkedf
/// @param classCharsSize number of chars in classChars. Value is not checked.
/// @returns class value
uint64_t genClass(const unsigned char classChars[], size_t classCharsSize)
{
  uint64_t ret = classChars[0];

  for (unsigned i = 1; i < classCharsSize; ++i) {
    ret <<= 8;
    ret += classChars[i];
  }

  return(ret);
}

const unsigned char polyLLVMExceptionClassChars[] =
{'p', 'g', 'l', 't', 'j', 'a', 'v', 'a'};

/**
 * [deleteFromUnwindOurException description]
 * @param reason      [description]
 * @param expToDelete Exception to delete
 */
void deleteJavaException(_Unwind_Reason_Code reason, UnwindException *expToDelete) {
  return; // The exception will be deleted by the garbage collector
}

/// Creates (allocates on the heap), an exception (OurException instance),
/// of the supplied type info type.
/// @param type type info type
UnwindException *allocateJavaException(void* jexception) {
  size_t size = sizeof(JavaException_t);
  JavaException_t *ret = (JavaException_t*) memset(GC_malloc(size), 0, size);
  (ret->jexception) = jexception;
  (ret->unwindException).exception_class = genClass(polyLLVMExceptionClassChars, 8);
  (ret->unwindException).exception_cleanup = deleteJavaException;

  return(&(ret->unwindException));
}

void throwJavaException(UnwindException* exception){
  _Unwind_RaiseException(exception);
  abort();
}

/// Read a uleb128 encoded value and advance pointer
/// See Variable Length Data in:
/// @link http://dwarfstd.org/Dwarf3.pdf @unlink
/// @param data reference variable holding memory pointer to decode from
/// @returns decoded value
static uintptr_t readULEB128(const uint8_t **data) {
  uintptr_t result = 0;
  uintptr_t shift = 0;
  unsigned char byte;
  const uint8_t *p = *data;

  do {
    byte = *p++;
    result |= (byte & 0x7f) << shift;
    shift += 7;
  }
  while (byte & 0x80);

  *data = p;

  return result;
}


/// Read a sleb128 encoded value and advance pointer
/// See Variable Length Data in:
/// @link http://dwarfstd.org/Dwarf3.pdf @unlink
/// @param data reference variable holding memory pointer to decode from
/// @returns decoded value
static uintptr_t readSLEB128(const uint8_t **data) {
  uintptr_t result = 0;
  uintptr_t shift = 0;
  unsigned char byte;
  const uint8_t *p = *data;

  do {
    byte = *p++;
    result |= (byte & 0x7f) << shift;
    shift += 7;
  }
  while (byte & 0x80);

  *data = p;

  if ((byte & 0x40) && (shift < (sizeof(result) << 3))) {
    result |= (~0 << shift);
  }

  return result;
}

}

