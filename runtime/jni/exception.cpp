#include <stdexcept>

#include <inttypes.h>

extern "C" {

  typedef enum {
    _URC_NO_REASON = 0,
    _URC_FOREIGN_EXCEPTION_CAUGHT = 1,
    _URC_FATAL_PHASE2_ERROR = 2,
    _URC_FATAL_PHASE1_ERROR = 3,
    _URC_NORMAL_STOP = 4,
    _URC_END_OF_STACK = 5,
    _URC_HANDLER_FOUND = 6,
    _URC_INSTALL_CONTEXT = 7,
    _URC_CONTINUE_UNWIND = 8
  } _Unwind_Reason_Code;

  typedef enum {
    _UA_SEARCH_PHASE = 1,
    _UA_CLEANUP_PHASE = 2,
    _UA_HANDLER_FRAME = 4,
    _UA_FORCE_UNWIND = 8,
    _UA_END_OF_STACK = 16
  } _Unwind_Action;

  struct _Unwind_Exception;

  typedef void (*_Unwind_Exception_Cleanup_Fn) (_Unwind_Reason_Code,
                                                struct _Unwind_Exception *);

  struct _Unwind_Exception {
    uint64_t exception_class;
    _Unwind_Exception_Cleanup_Fn exception_cleanup;

    uintptr_t private_1;
    uintptr_t private_2;

    // @@@ The IA-64 ABI says that this structure must be double-word aligned.
    //  Taking that literally does not make much sense generically.  Instead
    //  we provide the maximum alignment required by any type for the machine.
  } __attribute__((__aligned__));

  struct _Unwind_Context;
  typedef struct _Unwind_Context *_Unwind_Context_t;

  extern const uint8_t *_Unwind_GetLanguageSpecificData (_Unwind_Context_t c);
  extern uintptr_t _Unwind_GetGR (_Unwind_Context_t c, int i);
  extern void _Unwind_SetGR (_Unwind_Context_t c, int i, uintptr_t n);
  extern void _Unwind_SetIP (_Unwind_Context_t, uintptr_t new_value);
  extern uintptr_t _Unwind_GetIP (_Unwind_Context_t context);
  extern uintptr_t _Unwind_GetRegionStart (_Unwind_Context_t context);

} // extern "C"

typedef struct _Unwind_Exception UnwindException;


struct JavaException_t {
  void* jexception;

  // Note: This is properly aligned in unwind.h
  struct _Unwind_Exception unwindException;
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
  void* allocate k.ug
  JavaException_t *ret = (JavaException_t*) memset(malloc(size), 0, size);
  (ret->jexception) = jexception;
  (ret->unwindException).exception_class = genClass(polyLLVMExceptionClassChars);
  (ret->unwindException).exception_cleanup = deleteJavaException;

  return(&(ret->unwindException));
}

void throwJavaException(UnwindException* exception){
  _Unwind_RaiseException(exception);
  std::abort();
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

