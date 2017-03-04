#include <stdexcept>
#include <cstring>

#include <unwind.h>
#include <inttypes.h>
#include <gc.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>

#define __STDC_LIMIT_MACROS
#define __STDC_CONSTANT_MACROS
#include "llvm/Support/Dwarf.h"


#include "types.h"
#include "reflect.h"


typedef struct _Unwind_Exception UnwindException;

extern "C" {

struct JavaException_t {
  void* jexception;

  // Note: This is properly aligned in unwind.h
  UnwindException unwindException;
};

typedef struct _Unwind_Context *_Unwind_Context_t;

//Global Variables
const unsigned char polyLLVMExceptionClassChars[] =
        {'p', 'g', 'l', 't', 'j', 'a', 'v', 'a'};

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

void deleteJavaException(_Unwind_Reason_Code reason, UnwindException *expToDelete) {
  return; // The exception will be deleted by the garbage collector
}

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

/// Read a pointer encoded value and advance pointer
/// See Variable Length Data in:
/// @link http://dwarfstd.org/Dwarf3.pdf @unlink
/// @param data reference variable holding memory pointer to decode from
/// @param encoding dwarf encoding type
/// @returns decoded value
static uintptr_t readEncodedPointer(const uint8_t **data, uint8_t encoding) {
    uintptr_t result = 0;
    const uint8_t *p = *data;

    if (encoding == llvm::dwarf::DW_EH_PE_omit)
        return(result);

    // first get value
    switch (encoding & 0x0F) {
        case llvm::dwarf::DW_EH_PE_absptr:
            result = *((uintptr_t*)p);
            p += sizeof(uintptr_t);
            break;
        case llvm::dwarf::DW_EH_PE_uleb128:
            result = readULEB128(&p);
            break;
            // Note: This case has not been tested
        case llvm::dwarf::DW_EH_PE_sleb128:
            result = readSLEB128(&p);
            break;
        case llvm::dwarf::DW_EH_PE_udata2:
            result = *((uint16_t*)p);
            p += sizeof(uint16_t);
            break;
        case llvm::dwarf::DW_EH_PE_udata4:
            result = *((uint32_t*)p);
            p += sizeof(uint32_t);
            break;
        case llvm::dwarf::DW_EH_PE_udata8:
            result = *((uint64_t*)p);
            p += sizeof(uint64_t);
            break;
        case llvm::dwarf::DW_EH_PE_sdata2:
            result = *((int16_t*)p);
            p += sizeof(int16_t);
            break;
        case llvm::dwarf::DW_EH_PE_sdata4:
            result = *((int32_t*)p);
            p += sizeof(int32_t);
            break;
        case llvm::dwarf::DW_EH_PE_sdata8:
            result = *((int64_t*)p);
            p += sizeof(int64_t);
            break;
        default:
            // not supported
            abort();
            break;
    }

    // then add relative offset
    switch (encoding & 0x70) {
        case llvm::dwarf::DW_EH_PE_absptr:
            // do nothing
            break;
        case llvm::dwarf::DW_EH_PE_pcrel:
            result += (uintptr_t)(*data);
            break;
        case llvm::dwarf::DW_EH_PE_textrel:
        case llvm::dwarf::DW_EH_PE_datarel:
        case llvm::dwarf::DW_EH_PE_funcrel:
        case llvm::dwarf::DW_EH_PE_aligned:
        default:
            // not supported
            abort();
            break;
    }

    // then apply indirection
    if (encoding & llvm::dwarf::DW_EH_PE_indirect) {
        result = *((uintptr_t*)result);
    }

    *data = p;

    return result;
}

unsigned getEncodingSize(uint8_t Encoding) {
    if (Encoding == llvm::dwarf::DW_EH_PE_omit)
        return 0;

    switch (Encoding & 0x0F) {
        case llvm::dwarf::DW_EH_PE_absptr:
            return sizeof(uintptr_t);
        case llvm::dwarf::DW_EH_PE_udata2:
            return sizeof(uint16_t);
        case llvm::dwarf::DW_EH_PE_udata4:
            return sizeof(uint32_t);
        case llvm::dwarf::DW_EH_PE_udata8:
            return sizeof(uint64_t);
        case llvm::dwarf::DW_EH_PE_sdata2:
            return sizeof(int16_t);
        case llvm::dwarf::DW_EH_PE_sdata4:
            return sizeof(int32_t);
        case llvm::dwarf::DW_EH_PE_sdata8:
            return sizeof(int64_t);
        default:
            // not supported
            abort();
    }
}

/// Deals with Dwarf actions matching our type infos
/// (OurExceptionType_t instances). Returns whether or not a dwarf emitted
/// action matches the supplied exception type. If such a match succeeds,
/// the resultAction argument will be set with > 0 index value. Only
/// corresponding llvm.eh.selector type info arguments, cleanup arguments
/// are supported. Filters are not supported.
/// See Variable Length Data in:
/// @link http://dwarfstd.org/Dwarf3.pdf @unlink
/// Also see @link http://mentorembedded.github.com/cxx-abi/abi-eh.html @unlink
/// @param resultAction reference variable which will be set with result
/// @param classInfo our array of type info pointers (to globals)
/// @param actionEntry index into above type info array or 0 (clean up).
///        We do not support filters.
/// @param exceptionClass exception class (_Unwind_Exception::exception_class)
///        of thrown exception.
/// @param exceptionObject thrown _Unwind_Exception instance.
/// @returns whether or not a type info was found. False is returned if only
///          a cleanup was found
static bool handleActionValue(int64_t *resultAction,
                              uint8_t TTypeEncoding,
                              const uint8_t *ClassInfo,
                              uintptr_t actionEntry,
                              uint64_t exceptionClass,
                              struct _Unwind_Exception *exceptionObject) {
    bool ret = false;

    if (!resultAction ||
        !exceptionObject ||
        (exceptionClass != genClass(polyLLVMExceptionClassChars, 8)))
        return(ret);

    struct JavaException_t dummyException;

    int64_t baseFromUnwindOffset = ((uintptr_t) &dummyException) -
                              ((uintptr_t) &(dummyException.unwindException));

    struct JavaException_t *excp = (struct JavaException_t*)
            (((char*) exceptionObject) + baseFromUnwindOffset);
    jobject *jobj = reinterpret_cast<jobject *>(excp->jexception);

    const uint8_t *actionPos = (uint8_t*) actionEntry,
            *tempActionPos;
    int64_t typeOffset = 0,
            actionOffset;

    for (int i = 0; true; ++i) {
        // Each emitted dwarf action corresponds to a 2 tuple of
        // type info address offset, and action offset to the next
        // emitted action.
        typeOffset = readSLEB128(&actionPos);
        tempActionPos = actionPos;
        actionOffset = readSLEB128(&tempActionPos);

        // Note: A typeOffset == 0 implies that a cleanup llvm.eh.selector
        //       argument has been matched.
        if (typeOffset > 0) {
            unsigned EncSize = getEncodingSize(TTypeEncoding);
            const uint8_t *EntryP = ClassInfo - typeOffset * EncSize;
            uintptr_t P = readEncodedPointer(&EntryP, TTypeEncoding);
            void *ThisClassInfo = reinterpret_cast<void *>(P);
            if (instanceof(jobj, ThisClassInfo)) {
                *resultAction = i + 1;
                ret = true;
                break;
            }
        }

        if (!actionOffset)
            break;

        actionPos += actionOffset;
    }

    return(ret);
}

/// Deals with the Language specific data portion of the emitted dwarf code.
/// See @link http://mentorembedded.github.com/cxx-abi/abi-eh.html @unlink
/// @param version unsupported (ignored), unwind version
/// @param lsda language specific data area
/// @param _Unwind_Action actions minimally supported unwind stage
///        (forced specifically not supported)
/// @param exceptionClass exception class (_Unwind_Exception::exception_class)
///        of thrown exception.
/// @param exceptionObject thrown _Unwind_Exception instance.
/// @param context unwind system context
/// @returns minimally supported unwinding control indicator
static _Unwind_Reason_Code handleLsda(int version,
                                      uintptr_t lsda_in,
                                      _Unwind_Action actions,
                                      uint64_t exceptionClass,
                                      _Unwind_Exception* exceptionObject,
                                      _Unwind_Context* context) {
    _Unwind_Reason_Code ret = _URC_CONTINUE_UNWIND;

    const uint8_t *lsda = (uint8_t *) lsda_in;

    if (!lsda)
        return(ret);


    // Get the current instruction pointer and offset it before next
    // instruction in the current frame which threw the exception.
    uintptr_t pc = _Unwind_GetIP(context)-1;

    // Get beginning current frame's code (as defined by the
    // emitted dwarf code)
    uintptr_t funcStart = _Unwind_GetRegionStart(context);
    uintptr_t pcOffset = pc - funcStart;
    const uint8_t *ClassInfo = NULL;

    // Note: See JITDwarfEmitter::EmitExceptionTable(...) for corresponding
    //       dwarf emission

    // Parse LSDA header.
    uint8_t lpStartEncoding = *lsda++;

    if (lpStartEncoding != llvm::dwarf::DW_EH_PE_omit) {
        readEncodedPointer(&lsda, lpStartEncoding);
    }

    uint8_t ttypeEncoding = *lsda++;
    uintptr_t classInfoOffset;

    if (ttypeEncoding != llvm::dwarf::DW_EH_PE_omit) {
        // Calculate type info locations in emitted dwarf code which
        // were flagged by type info arguments to llvm.eh.selector
        // intrinsic
        classInfoOffset = readULEB128(&lsda);
        ClassInfo = lsda + classInfoOffset;
    }

    // Walk call-site table looking for range that
    // includes current PC.

    uint8_t         callSiteEncoding = *lsda++;
    uint32_t        callSiteTableLength = readULEB128(&lsda);
    const uint8_t   *callSiteTableStart = lsda;
    const uint8_t   *callSiteTableEnd = callSiteTableStart +
                                        callSiteTableLength;
    const uint8_t   *actionTableStart = callSiteTableEnd;
    const uint8_t   *callSitePtr = callSiteTableStart;

    while (callSitePtr < callSiteTableEnd) {
        uintptr_t start = readEncodedPointer(&callSitePtr,
                                             callSiteEncoding);
        uintptr_t length = readEncodedPointer(&callSitePtr,
                                              callSiteEncoding);
        uintptr_t landingPad = readEncodedPointer(&callSitePtr,
                                                  callSiteEncoding);

        // Note: Action value
        uintptr_t actionEntry = readULEB128(&callSitePtr);

        if (exceptionClass != genClass(polyLLVMExceptionClassChars, 8)) {
            // We have been notified of a foreign exception being thrown,
            // and we therefore need to execute cleanup landing pads
            actionEntry = 0;
        }

        if (landingPad == 0) {
            continue; // no landing pad for this entry
        }

        if (actionEntry) {
            actionEntry += ((uintptr_t) actionTableStart) - 1;
        }
        else {
        }

        bool exceptionMatched = false;

        if ((start <= pcOffset) && (pcOffset < (start + length))) {
            int64_t actionValue = 0;

            if (actionEntry) {
                exceptionMatched = handleActionValue(&actionValue,
                                                     ttypeEncoding,
                                                     ClassInfo,
                                                     actionEntry,
                                                     exceptionClass,
                                                     exceptionObject);
            }

            if (!(actions & _UA_SEARCH_PHASE)) {

                // Found landing pad for the PC.
                // Set Instruction Pointer to so we re-enter function
                // at landing pad. The landing pad is created by the
                // compiler to take two parameters in registers.
                _Unwind_SetGR(context,
                              __builtin_eh_return_data_regno(0),
                              (uintptr_t)exceptionObject);

                // Note: this virtual register directly corresponds
                //       to the return of the llvm.eh.selector intrinsic
                if (!actionEntry || !exceptionMatched) {
                    // We indicate cleanup only
                    _Unwind_SetGR(context,
                                  __builtin_eh_return_data_regno(1),
                                  0);
                }
                else {
                    // Matched type info index of llvm.eh.selector intrinsic
                    // passed here.
                    _Unwind_SetGR(context,
                                  __builtin_eh_return_data_regno(1),
                                  actionValue);
                }

                // To execute landing pad set here
                _Unwind_SetIP(context, funcStart + landingPad);
                ret = _URC_INSTALL_CONTEXT;
            }
            else if (exceptionMatched) {
                ret = _URC_HANDLER_FOUND;
            }
            else {
                // Note: Only non-clean up handlers are marked as
                //       found. Otherwise the clean up handlers will be
                //       re-found and executed during the clean up
                //       phase.
            }

            break;
        }
    }

    return(ret);
}

_Unwind_Reason_Code __jxx_personality_v0 (
        int version,
        _Unwind_Action actions,
        uint64_t exceptionClass,
        _Unwind_Exception* unwind_exception,
        _Unwind_Context* context)
{
    uintptr_t lsda = _Unwind_GetLanguageSpecificData(context);

    // The real work of the personality function is captured here
    return(handleLsda(version,
                      lsda,
                      actions,
                      exceptionClass,
                      unwind_exception,
                      context));
}

}
