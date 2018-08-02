#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <chrono>
#include <sys/time.h>
#include <unistd.h>

#include "stack_trace.h"
#include "class.h"
#include "exception.h"
#include "jni.h"
#include "rep.h"

#include "jvm.h"

[[noreturn]] static void JvmUnimplemented(const char* name) {
    fprintf(stderr,
        "- - - - - - - - - - - - - - - - - - - - - - - - - - -\n"
        "The following JVM method is currently unimplemented:\n"
        "  %s\n"
        "It is defined in " __FILE__ ".\n"
        "Aborting for now.\n"
        "- - - - - - - - - - - - - - - - - - - - - - - - - - -\n"
        , name);
    DumpStackTrace();
    abort();
}

static void JvmIgnore(const char* name) {
    fprintf(stderr,
        "WARNING: JVM method %s is unimplemented, but will not abort.\n", name);
    fflush(stderr);
}

extern "C" {

jint
JVM_GetInterfaceVersion(void) {
    JvmUnimplemented("JVM_GetInterfaceVersion");
}

jint
JVM_IHashCode(JNIEnv *env, jobject obj) {
    auto addr = reinterpret_cast<intptr_t>(obj);
    return static_cast<jint>(addr);
}

void
JVM_MonitorWait(JNIEnv *env, jobject obj, jlong ms) {
    JvmUnimplemented("JVM_MonitorWait");
}

void
JVM_MonitorNotify(JNIEnv *env, jobject obj) {
    JvmUnimplemented("JVM_MonitorNotify");
}

void
JVM_MonitorNotifyAll(JNIEnv *env, jobject obj) {
    JvmUnimplemented("JVM_MonitorNotifyAll");
}

jobject
JVM_Clone(JNIEnv *env, jobject obj) {
    JvmUnimplemented("JVM_Clone");
}

jstring
JVM_InternString(JNIEnv *env, jstring str) {
    JvmUnimplemented("JVM_InternString");
}

jlong
JVM_CurrentTimeMillis(JNIEnv *env, jclass ignored) {
  timeval t;
  if (gettimeofday( &t, NULL) == -1)
    JvmUnimplemented("CurrentTimeMillisFailed");
  return jlong(t.tv_sec) * 1000  +  jlong(t.tv_usec) / 1000;
}

jlong
JVM_NanoTime(JNIEnv *env, jclass ignored) {
  auto time = std::chrono::system_clock::now().time_since_epoch();
  return std::chrono::duration_cast<std::chrono::nanoseconds>(time).count();
}

void
JVM_ArrayCopy(
    JNIEnv *env, jclass ignored,
    jobject src, jint src_pos, jobject dst, jint dst_pos, jint length
) {
  //TODO check bounds
    jarray src_arr = reinterpret_cast<jarray>(src);
    jarray dst_arr = reinterpret_cast<jarray>(dst);
    jsize elemsize = (Unwrap(src_arr)->ElemSize());
    char* src_data = static_cast<char*>(Unwrap(src_arr)->Data());
    char* dst_data = static_cast<char*>(Unwrap(dst_arr)->Data());
    memmove(dst_data + (dst_pos * elemsize), src_data + (src_pos * elemsize), elemsize * length);
}

jobject
JVM_InitProperties(JNIEnv *env, jobject p) {
  return p;

}

void
JVM_OnExit(void (*func)(void)) {
    JvmUnimplemented("JVM_OnExit");
}

void
JVM_Exit(jint code) {
    JvmUnimplemented("JVM_Exit");
}

void
JVM_Halt(jint code) {
    JvmUnimplemented("JVM_Halt");
}

void
JVM_GC(void) {
    JvmUnimplemented("JVM_GC");
}

jlong
JVM_MaxObjectInspectionAge(void) {
    JvmUnimplemented("JVM_MaxObjectInspectionAge");
}

void
JVM_TraceInstructions(jboolean on) {
    JvmUnimplemented("JVM_TraceInstructions");
}

void
JVM_TraceMethodCalls(jboolean on) {
    JvmUnimplemented("JVM_TraceMethodCalls");
}

jlong
JVM_TotalMemory(void) {
    JvmUnimplemented("JVM_TotalMemory");
}

jlong
JVM_FreeMemory(void) {
    JvmUnimplemented("JVM_FreeMemory");
}

jlong
JVM_MaxMemory(void) {
    JvmUnimplemented("JVM_MaxMemory");
}

jint
JVM_ActiveProcessorCount(void) {
    JvmUnimplemented("JVM_ActiveProcessorCount");
}

void*
JVM_LoadLibrary(const char *name) {
    JvmUnimplemented("JVM_LoadLibrary");
}

void
JVM_UnloadLibrary(void * handle) {
    JvmUnimplemented("JVM_UnloadLibrary");
}

void*
JVM_FindLibraryEntry(void *handle, const char *name) {
    JvmUnimplemented("JVM_FindLibraryEntry");
}

jboolean
JVM_IsSupportedJNIVersion(jint version) {
    JvmUnimplemented("JVM_IsSupportedJNIVersion");
}

jboolean
JVM_IsNaN(jdouble d) {
    JvmUnimplemented("JVM_IsNaN");
}

void
JVM_FillInStackTrace(JNIEnv *env, jobject throwable) {
    JvmIgnore("JVM_FillInStackTrace");
}

void
JVM_PrintStackTrace(JNIEnv *env, jobject throwable, jobject printable) {
    JvmUnimplemented("JVM_PrintStackTrace");
}

jint
JVM_GetStackTraceDepth(JNIEnv *env, jobject throwable) {
    JvmUnimplemented("JVM_GetStackTraceDepth");
}

jobject
JVM_GetStackTraceElement(JNIEnv *env, jobject throwable, jint index) {
    JvmUnimplemented("JVM_GetStackTraceElement");
}

void
JVM_InitializeCompiler(JNIEnv *env, jclass compCls) {
    JvmUnimplemented("JVM_InitializeCompiler");
}

jboolean
JVM_IsSilentCompiler(JNIEnv *env, jclass compCls) {
    JvmUnimplemented("JVM_IsSilentCompiler");
}

jboolean
JVM_CompileClass(JNIEnv *env, jclass compCls, jclass cls) {
    JvmUnimplemented("JVM_CompileClass");
}

jboolean
JVM_CompileClasses(JNIEnv *env, jclass cls, jstring jname) {
    JvmUnimplemented("JVM_CompileClasses");
}

jobject
JVM_CompilerCommand(JNIEnv *env, jclass compCls, jobject arg) {
    JvmUnimplemented("JVM_CompilerCommand");
}

void
JVM_EnableCompiler(JNIEnv *env, jclass compCls) {
    JvmUnimplemented("JVM_EnableCompiler");
}

void
JVM_DisableCompiler(JNIEnv *env, jclass compCls) {
  return;
}

void
JVM_StartThread(JNIEnv *env, jobject thread) {
  //TODO someday there will be synchronization
  return;
}

void
JVM_StopThread(JNIEnv *env, jobject thread, jobject exception) {
    //TODO someday there will be synchronization
    return;
}

jboolean
JVM_IsThreadAlive(JNIEnv *env, jobject thread) {
  return (thread == mainThread) ? mainThreadIsAlive : JNI_FALSE;
}

void
JVM_SuspendThread(JNIEnv *env, jobject thread) {
  //TODO someday there will be synchronization
  return;
}

void
JVM_ResumeThread(JNIEnv *env, jobject thread) {
  //TODO someday there will be synchronization
  return;
}

void
JVM_SetThreadPriority(JNIEnv *env, jobject thread, jint prio) {
  //TODO someday there will be synchronization
  return;
}

void
JVM_Yield(JNIEnv *env, jclass threadClass) {
  //TODO someday there will be synchronization
  return;
}

void
JVM_Sleep(JNIEnv *env, jclass threadClass, jlong millis) {
  usleep(millis * 1000);
  return;
}

jobject
JVM_CurrentThread(JNIEnv *env, jclass threadClass) {
  //TODO real multi-threading
  return GetMainThread();
}

jint
JVM_CountStackFrames(JNIEnv *env, jobject thread) {
    JvmUnimplemented("JVM_CountStackFrames");
}

void
JVM_Interrupt(JNIEnv *env, jobject thread) {
    JvmUnimplemented("JVM_Interrupt");
}

jboolean
JVM_IsInterrupted(JNIEnv *env, jobject thread, jboolean clearInterrupted) {
    JvmUnimplemented("JVM_IsInterrupted");
}

jboolean
JVM_HoldsLock(JNIEnv *env, jclass threadClass, jobject obj) {
    JvmUnimplemented("JVM_HoldsLock");
}

void
JVM_DumpAllStacks(JNIEnv *env, jclass unused) {
    JvmUnimplemented("JVM_DumpAllStacks");
}

jobjectArray
JVM_GetAllThreads(JNIEnv *env, jclass dummy) {
    JvmUnimplemented("JVM_GetAllThreads");
}

void
JVM_SetNativeThreadName(JNIEnv *env, jobject jthread, jstring name) {
    JvmUnimplemented("JVM_SetNativeThreadName");
}

jobjectArray
JVM_DumpThreads(JNIEnv *env, jclass threadClass, jobjectArray threads) {
    JvmUnimplemented("JVM_DumpThreads");
}

jclass
JVM_CurrentLoadedClass(JNIEnv *env) {
    JvmUnimplemented("JVM_CurrentLoadedClass");
}

jobject
JVM_CurrentClassLoader(JNIEnv *env) {
    JvmUnimplemented("JVM_CurrentClassLoader");
}

jobjectArray
JVM_GetClassContext(JNIEnv *env) {
    JvmUnimplemented("JVM_GetClassContext");
}

jint
JVM_ClassDepth(JNIEnv *env, jstring name) {
    JvmUnimplemented("JVM_ClassDepth");
}

jint
JVM_ClassLoaderDepth(JNIEnv *env) {
    JvmUnimplemented("JVM_ClassLoaderDepth");
}

jstring
JVM_GetSystemPackage(JNIEnv *env, jstring name) {
    JvmUnimplemented("JVM_GetSystemPackage");
}

jobjectArray
JVM_GetSystemPackages(JNIEnv *env) {
    JvmUnimplemented("JVM_GetSystemPackages");
}

jobject
JVM_AllocateNewObject(JNIEnv *env, jobject obj, jclass currClass, jclass initClass) {
    JvmUnimplemented("JVM_AllocateNewObject");
}

jobject
JVM_AllocateNewArray(JNIEnv *env, jobject obj, jclass currClass, jint length) {
    JvmUnimplemented("JVM_AllocateNewArray");
}

jobject
JVM_LatestUserDefinedLoader(JNIEnv *env) {
    JvmUnimplemented("JVM_LatestUserDefinedLoader");
}

jclass
JVM_LoadClass0(JNIEnv *env, jobject obj, jclass currClass, jstring currClassName) {
    JvmUnimplemented("JVM_LoadClass0");
}

jint
JVM_GetArrayLength(JNIEnv *env, jobject arr) {
    JvmUnimplemented("JVM_GetArrayLength");
}

jobject
JVM_GetArrayElement(JNIEnv *env, jobject arr, jint index) {
    JvmUnimplemented("JVM_GetArrayElement");
}

jvalue
JVM_GetPrimitiveArrayElement(JNIEnv *env, jobject arr, jint index, jint wCode) {
    JvmUnimplemented("JVM_GetPrimitiveArrayElement");
}

void
JVM_SetArrayElement(JNIEnv *env, jobject arr, jint index, jobject val) {
    JvmUnimplemented("JVM_SetArrayElement");
}

void
JVM_SetPrimitiveArrayElement(JNIEnv *env, jobject arr, jint index, jvalue v, unsigned char vCode) {
    JvmUnimplemented("JVM_SetPrimitiveArrayElement");
}

jobject
JVM_NewArray(JNIEnv *env, jclass eltClass, jint length) {
    JvmUnimplemented("JVM_NewArray");
}

jobject
JVM_NewMultiArray(JNIEnv *env, jclass eltClass, jintArray dim) {
    JvmUnimplemented("JVM_NewMultiArray");
}

jclass
JVM_GetCallerClass(JNIEnv *env, int n) {
    JvmIgnore("JVM_GetCallerClass");
    return nullptr;
}

jclass
JVM_FindPrimitiveClass(JNIEnv *env, const char *utf) {
  //TODO throw ClassNotFoundException if not primitive type
  return GetPrimitiveClass(utf);
}

void
JVM_ResolveClass(JNIEnv *env, jclass cls) {
    JvmUnimplemented("JVM_ResolveClass");
}

jclass
JVM_FindClassFromBootLoader(JNIEnv *env, const char *name) {
    JvmUnimplemented("JVM_FindClassFromBootLoader");
}

jclass
JVM_FindClassFromCaller(JNIEnv *env, const char *name, jboolean init, jobject loader, jclass caller) {
  auto clazz = GetJavaClassFromPathName(name);
  if (clazz != NULL) {
    return clazz;
  } else { //try to load class from jdklib
    clazz = LoadJavaClassFromLib(name);
    if (clazz == NULL) {
      throwClassNotFoundException(env, name);
      return NULL; //to make the compiler happy
    } else {
      return clazz;
    }
  }
}

jclass
JVM_FindClassFromClassLoader(JNIEnv *env, const char *name, jboolean init, jobject loader, jboolean throwError) {
    JvmUnimplemented("JVM_FindClassFromClassLoader");
}

jclass
JVM_FindClassFromClass(JNIEnv *env, const char *name, jboolean init, jclass from) {
    JvmUnimplemented("JVM_FindClassFromClass");
}

jclass
JVM_FindLoadedClass(JNIEnv *env, jobject loader, jstring name) {
    JvmUnimplemented("JVM_FindLoadedClass");
}

jclass
JVM_DefineClass(JNIEnv *env, const char *name, jobject loader, const jbyte *buf, jsize len, jobject pd) {
    JvmUnimplemented("JVM_DefineClass");
}

jclass
JVM_DefineClassWithSource(JNIEnv *env, const char *name, jobject loader, const jbyte *buf, jsize len, jobject pd, const char *source) {
    JvmUnimplemented("JVM_DefineClassWithSource");
}

jstring
JVM_GetClassName(JNIEnv *env, jclass cls) {
    auto name = GetJavaClassInfo(cls)->name;
    return env->NewStringUTF(name);
}

jobjectArray
JVM_GetClassInterfaces(JNIEnv *env, jclass cls) {
    JvmUnimplemented("JVM_GetClassInterfaces");
}

jboolean
JVM_IsInterface(JNIEnv *env, jclass cls) {
  auto info = GetJavaClassInfo(cls);
  if (info) {
    return info->isIntf;
  } else {
    return JNI_FALSE;
  }
}

jobjectArray
JVM_GetClassSigners(JNIEnv *env, jclass cls) {
    JvmUnimplemented("JVM_GetClassSigners");
}

void
JVM_SetClassSigners(JNIEnv *env, jclass cls, jobjectArray signers) {
    JvmUnimplemented("JVM_SetClassSigners");
}

jobject
JVM_GetProtectionDomain(JNIEnv *env, jclass cls) {
    JvmUnimplemented("JVM_GetProtectionDomain");
}

void
JVM_SetProtectionDomain(JNIEnv *env, jclass cls, jobject protection_domain) {
    JvmUnimplemented("JVM_SetProtectionDomain");
}

jboolean
JVM_IsArrayClass(JNIEnv *env, jclass cls) {
    JvmUnimplemented("JVM_IsArrayClass");
}

jboolean
JVM_IsPrimitiveClass(JNIEnv *env, jclass cls) {
    JvmUnimplemented("JVM_IsPrimitiveClass");
}

jclass
JVM_GetComponentType(JNIEnv *env, jclass cls) {
    JvmUnimplemented("JVM_GetComponentType");
}

jint
JVM_GetClassModifiers(JNIEnv *env, jclass cls) {
    JvmUnimplemented("JVM_GetClassModifiers");
}

jobjectArray
JVM_GetDeclaredClasses(JNIEnv *env, jclass ofClass) {
    JvmUnimplemented("JVM_GetDeclaredClasses");
}

jclass
JVM_GetDeclaringClass(JNIEnv *env, jclass ofClass) {
    JvmUnimplemented("JVM_GetDeclaringClass");
}

jstring
JVM_GetClassSignature(JNIEnv *env, jclass cls) {
    JvmUnimplemented("JVM_GetClassSignature");
}

jbyteArray
JVM_GetClassAnnotations(JNIEnv *env, jclass cls) {
    JvmUnimplemented("JVM_GetClassAnnotations");
}

jobjectArray
JVM_GetClassDeclaredMethods(JNIEnv *env, jclass ofClass, jboolean publicOnly) {
    JvmUnimplemented("JVM_GetClassDeclaredMethods");
}

jobjectArray
JVM_GetClassDeclaredFields(JNIEnv *env, jclass ofClass, jboolean publicOnly) {
    JvmUnimplemented("JVM_GetClassDeclaredFields");
}

jobjectArray
JVM_GetClassDeclaredConstructors(JNIEnv *env, jclass ofClass, jboolean publicOnly) {
  //TODO ignore primitive and array types (return empty array for those)
  auto class_info = GetJavaClassInfo(ofClass);
  if (class_info == NULL) {
    return CreateJavaObjectArray(0);
  } else {
    return GetJavaConstructors(ofClass, class_info, publicOnly);
  }
}

jint
JVM_GetClassAccessFlags(JNIEnv *env, jclass cls) {
    JvmUnimplemented("JVM_GetClassAccessFlags");
}

jobject
JVM_InvokeMethod(JNIEnv *env, jobject method, jobject obj, jobjectArray args0) {
    JvmUnimplemented("JVM_InvokeMethod");
}

jobject
JVM_NewInstanceFromConstructor(JNIEnv *env, jobject c, jobjectArray args0) {
    JvmUnimplemented("JVM_NewInstanceFromConstructor");
}

jobject
JVM_GetClassConstantPool(JNIEnv *env, jclass cls) {
    JvmUnimplemented("JVM_GetClassConstantPool");
}

jint
JVM_ConstantPoolGetSize(JNIEnv *env, jobject unused, jobject jcpool) {
    JvmUnimplemented("JVM_ConstantPoolGetSize");
}

jclass
JVM_ConstantPoolGetClassAt(JNIEnv *env, jobject unused, jobject jcpool, jint index) {
    JvmUnimplemented("JVM_ConstantPoolGetClassAt");
}

jclass
JVM_ConstantPoolGetClassAtIfLoaded(JNIEnv *env, jobject unused, jobject jcpool, jint index) {
    JvmUnimplemented("JVM_ConstantPoolGetClassAtIfLoaded");
}

jobject
JVM_ConstantPoolGetMethodAt(JNIEnv *env, jobject unused, jobject jcpool, jint index) {
    JvmUnimplemented("JVM_ConstantPoolGetMethodAt");
}

jobject
JVM_ConstantPoolGetMethodAtIfLoaded(JNIEnv *env, jobject unused, jobject jcpool, jint index) {
    JvmUnimplemented("JVM_ConstantPoolGetMethodAtIfLoaded");
}

jobject
JVM_ConstantPoolGetFieldAt(JNIEnv *env, jobject unused, jobject jcpool, jint index) {
    JvmUnimplemented("JVM_ConstantPoolGetFieldAt");
}

jobject
JVM_ConstantPoolGetFieldAtIfLoaded(JNIEnv *env, jobject unused, jobject jcpool, jint index) {
    JvmUnimplemented("JVM_ConstantPoolGetFieldAtIfLoaded");
}

jobjectArray
JVM_ConstantPoolGetMemberRefInfoAt(JNIEnv *env, jobject unused, jobject jcpool, jint index) {
    JvmUnimplemented("JVM_ConstantPoolGetMemberRefInfoAt");
}

jint
JVM_ConstantPoolGetIntAt(JNIEnv *env, jobject unused, jobject jcpool, jint index) {
    JvmUnimplemented("JVM_ConstantPoolGetIntAt");
}

jlong
JVM_ConstantPoolGetLongAt(JNIEnv *env, jobject unused, jobject jcpool, jint index) {
    JvmUnimplemented("JVM_ConstantPoolGetLongAt");
}

jfloat
JVM_ConstantPoolGetFloatAt(JNIEnv *env, jobject unused, jobject jcpool, jint index) {
    JvmUnimplemented("JVM_ConstantPoolGetFloatAt");
}

jdouble
JVM_ConstantPoolGetDoubleAt(JNIEnv *env, jobject unused, jobject jcpool, jint index) {
    JvmUnimplemented("JVM_ConstantPoolGetDoubleAt");
}

jstring
JVM_ConstantPoolGetStringAt(JNIEnv *env, jobject unused, jobject jcpool, jint index) {
    JvmUnimplemented("JVM_ConstantPoolGetStringAt");
}

jstring
JVM_ConstantPoolGetUTF8At(JNIEnv *env, jobject unused, jobject jcpool, jint index) {
    JvmUnimplemented("JVM_ConstantPoolGetUTF8At");
}

jobject
JVM_DoPrivileged(JNIEnv *env, jclass cls, jobject action, jobject context, jboolean wrapException) {
  //TODO throw NPE if action is null
  if (action == NULL) { fprintf(stderr, "ERROR: Null privileged action"); return NULL; }
  //TODO we are just straight up calling the 'run' method, but doPrivileged
  //does a lot more
  auto pActionClazz = GetJavaClassFromName("java.security.PrivilegedAction");
  //  auto pActionClazz = jni_FindClass(env, "java/security/PrivilegedAction");
  jobject result = CallJavaInterfaceMethod<jobject>(action, pActionClazz, "run", "()Ljava/lang/Object;", NULL);
  return result;
}

jobject
JVM_GetInheritedAccessControlContext(JNIEnv *env, jclass cls) {
  return NULL;
}

jobject
JVM_GetStackAccessControlContext(JNIEnv *env, jclass cls) {
  //TODO is NULL ok here if we don't want to implement Access Control?
  // I don't know
  return NULL;
}

void*
JVM_RegisterSignal(jint sig, void *handler) {
    JvmUnimplemented("JVM_RegisterSignal");
}

jboolean
JVM_RaiseSignal(jint sig) {
    JvmUnimplemented("JVM_RaiseSignal");
}

jint
JVM_FindSignal(const char *name) {
    JvmUnimplemented("JVM_FindSignal");
}

jboolean
JVM_DesiredAssertionStatus(JNIEnv *env, jclass unused, jclass cls) {
    JvmUnimplemented("JVM_DesiredAssertionStatus");
}

jobject
JVM_AssertionStatusDirectives(JNIEnv *env, jclass unused) {
    JvmUnimplemented("JVM_AssertionStatusDirectives");
}

jboolean
JVM_SupportsCX8(void) {
    JvmUnimplemented("JVM_SupportsCX8");
}

const char*
JVM_GetClassNameUTF(JNIEnv *env, jclass cb) {
    auto name = GetJavaClassInfo(cb)->name;
    char* res = (char*) malloc(strlen(name) + 1);
    strcpy(res, name);
    return res;
}

void
JVM_GetClassCPTypes(JNIEnv *env, jclass cb, unsigned char *types) {
    JvmUnimplemented("JVM_GetClassCPTypes");
}

jint
JVM_GetClassCPEntriesCount(JNIEnv *env, jclass cb) {
    JvmUnimplemented("JVM_GetClassCPEntriesCount");
}

jint
JVM_GetClassFieldsCount(JNIEnv *env, jclass cb) {
    JvmUnimplemented("JVM_GetClassFieldsCount");
}

jint
JVM_GetClassMethodsCount(JNIEnv *env, jclass cb) {
    JvmUnimplemented("JVM_GetClassMethodsCount");
}

void
JVM_GetMethodIxExceptionIndexes(JNIEnv *env, jclass cb, jint method_index, unsigned short *exceptions) {
    JvmUnimplemented("JVM_GetMethodIxExceptionIndexes");
}

jint
JVM_GetMethodIxExceptionsCount(JNIEnv *env, jclass cb, jint method_index) {
    JvmUnimplemented("JVM_GetMethodIxExceptionsCount");
}

void
JVM_GetMethodIxByteCode(JNIEnv *env, jclass cb, jint method_index, unsigned char *code) {
    JvmUnimplemented("JVM_GetMethodIxByteCode");
}

jint
JVM_GetMethodIxByteCodeLength(JNIEnv *env, jclass cb, jint method_index) {
    JvmUnimplemented("JVM_GetMethodIxByteCodeLength");
}

void
JVM_GetMethodIxExceptionTableEntry(JNIEnv *env, jclass cb, jint method_index, jint entry_index, JVM_ExceptionTableEntryType *entry) {
    JvmUnimplemented("JVM_GetMethodIxExceptionTableEntry");
}

jint
JVM_GetMethodIxExceptionTableLength(JNIEnv *env, jclass cb, int index) {
    JvmUnimplemented("JVM_GetMethodIxExceptionTableLength");
}

jint
JVM_GetFieldIxModifiers(JNIEnv *env, jclass cb, int index) {
    JvmUnimplemented("JVM_GetFieldIxModifiers");
}

jint
JVM_GetMethodIxModifiers(JNIEnv *env, jclass cb, int index) {
    JvmUnimplemented("JVM_GetMethodIxModifiers");
}

jint
JVM_GetMethodIxLocalsCount(JNIEnv *env, jclass cb, int index) {
    JvmUnimplemented("JVM_GetMethodIxLocalsCount");
}

jint
JVM_GetMethodIxArgsSize(JNIEnv *env, jclass cb, int index) {
    JvmUnimplemented("JVM_GetMethodIxArgsSize");
}

jint
JVM_GetMethodIxMaxStack(JNIEnv *env, jclass cb, int index) {
    JvmUnimplemented("JVM_GetMethodIxMaxStack");
}

jboolean
JVM_IsConstructorIx(JNIEnv *env, jclass cb, int index) {
    JvmUnimplemented("JVM_IsConstructorIx");
}

const char*
JVM_GetMethodIxNameUTF(JNIEnv *env, jclass cb, jint index) {
    JvmUnimplemented("JVM_GetMethodIxNameUTF");
}

const char*
JVM_GetMethodIxSignatureUTF(JNIEnv *env, jclass cb, jint index) {
    JvmUnimplemented("JVM_GetMethodIxSignatureUTF");
}

const char*
JVM_GetCPFieldNameUTF(JNIEnv *env, jclass cb, jint index) {
    JvmUnimplemented("JVM_GetCPFieldNameUTF");
}

const char*
JVM_GetCPMethodNameUTF(JNIEnv *env, jclass cb, jint index) {
    JvmUnimplemented("JVM_GetCPMethodNameUTF");
}

const char*
JVM_GetCPMethodSignatureUTF(JNIEnv *env, jclass cb, jint index) {
    JvmUnimplemented("JVM_GetCPMethodSignatureUTF");
}

const char*
JVM_GetCPFieldSignatureUTF(JNIEnv *env, jclass cb, jint index) {
    JvmUnimplemented("JVM_GetCPFieldSignatureUTF");
}

const char*
JVM_GetCPClassNameUTF(JNIEnv *env, jclass cb, jint index) {
    JvmUnimplemented("JVM_GetCPClassNameUTF");
}

const char*
JVM_GetCPFieldClassNameUTF(JNIEnv *env, jclass cb, jint index) {
    JvmUnimplemented("JVM_GetCPFieldClassNameUTF");
}

const char*
JVM_GetCPMethodClassNameUTF(JNIEnv *env, jclass cb, jint index) {
    JvmUnimplemented("JVM_GetCPMethodClassNameUTF");
}

jint
JVM_GetCPFieldModifiers(JNIEnv *env, jclass cb, int index, jclass calledClass) {
    JvmUnimplemented("JVM_GetCPFieldModifiers");
}

jint
JVM_GetCPMethodModifiers(JNIEnv *env, jclass cb, int index, jclass calledClass) {
    JvmUnimplemented("JVM_GetCPMethodModifiers");
}

void
JVM_ReleaseUTF(const char *utf) {
    free(const_cast<char*>(utf));
}

jboolean
JVM_IsSameClassPackage(JNIEnv *env, jclass class1, jclass class2) {
    JvmUnimplemented("JVM_IsSameClassPackage");
}

jint
JVM_GetLastErrorString(char *buf, int len) {
    JvmUnimplemented("JVM_GetLastErrorString");
}

char*
JVM_NativePath(char *) {
    JvmUnimplemented("JVM_NativePath");
}

jint
JVM_Open(const char *fname, jint flags, jint mode) {
    JvmUnimplemented("JVM_Open");
}

jint
JVM_Close(jint fd) {
    JvmUnimplemented("JVM_Close");
}

jint
JVM_Read(jint fd, char *buf, jint nbytes) {
    JvmUnimplemented("JVM_Read");
}

jint
JVM_Write(jint fd, char *buf, jint nbytes) {
  jint result = -1;
  while (result == -1) {
    result = write(fd, buf, nbytes);
  }
  return result;
}

jint
JVM_Available(jint fd, jlong *pbytes) {
    JvmUnimplemented("JVM_Available");
}

jlong
JVM_Lseek(jint fd, jlong offset, jint whence) {
    JvmUnimplemented("JVM_Lseek");
}

jint
JVM_SetLength(jint fd, jlong length) {
    JvmUnimplemented("JVM_SetLength");
}

jint
JVM_Sync(jint fd) {
    JvmUnimplemented("JVM_Sync");
}

jint
JVM_InitializeSocketLibrary(void) {
    JvmUnimplemented("JVM_InitializeSocketLibrary");
}

jint
JVM_Socket(jint domain, jint type, jint protocol) {
    JvmUnimplemented("JVM_Socket");
}

jint
JVM_SocketClose(jint fd) {
    JvmUnimplemented("JVM_SocketClose");
}

jint
JVM_SocketShutdown(jint fd, jint howto) {
    JvmUnimplemented("JVM_SocketShutdown");
}

jint
JVM_Recv(jint fd, char *buf, jint nBytes, jint flags) {
    JvmUnimplemented("JVM_Recv");
}

jint
JVM_Send(jint fd, char *buf, jint nBytes, jint flags) {
    JvmUnimplemented("JVM_Send");
}

jint
JVM_Timeout(int fd, long timeout) {
    JvmUnimplemented("JVM_Timeout");
}

jint
JVM_Listen(jint fd, jint count) {
    JvmUnimplemented("JVM_Listen");
}

jint
JVM_Connect(jint fd, struct sockaddr *him, jint len) {
    JvmUnimplemented("JVM_Connect");
}

jint
JVM_Bind(jint fd, struct sockaddr *him, jint len) {
    JvmUnimplemented("JVM_Bind");
}

jint
JVM_Accept(jint fd, struct sockaddr *him, jint *len) {
    JvmUnimplemented("JVM_Accept");
}

jint
JVM_RecvFrom(jint fd, char *buf, int nBytes, int flags, struct sockaddr *from, int *fromlen) {
    JvmUnimplemented("JVM_RecvFrom");
}

jint
JVM_SendTo(jint fd, char *buf, int len, int flags, struct sockaddr *to, int tolen) {
    JvmUnimplemented("JVM_SendTo");
}

jint
JVM_SocketAvailable(jint fd, jint *result) {
    JvmUnimplemented("JVM_SocketAvailable");
}

jint
JVM_GetSockName(jint fd, struct sockaddr *him, int *len) {
    JvmUnimplemented("JVM_GetSockName");
}

jint
JVM_GetSockOpt(jint fd, int level, int optname, char *optval, int *optlen) {
    JvmUnimplemented("JVM_GetSockOpt");
}

jint
JVM_SetSockOpt(jint fd, int level, int optname, const char *optval, int optlen) {
    JvmUnimplemented("JVM_SetSockOpt");
}

int
JVM_GetHostName(char* name, int namelen) {
    JvmUnimplemented("JVM_GetHostName");
}

//FROM JDK (but what else would you write?
extern "C" {
  int
  jio_vsnprintf(char *str, size_t count, const char *fmt, va_list args) {
    // see bug 4399518, 4417214
    if ((intptr_t)count <= 0) return -1;
    return vsnprintf(str, count, fmt, args);
  }
  
  int jio_snprintf(char *str, size_t count, const char *fmt, ...) {
    va_list args;
    int len;
    va_start(args, fmt);
    len = jio_vsnprintf(str, count, fmt, args);
    va_end(args);
    return len;
  }

  int jio_fprintf(FILE* f, const char *fmt, ...) {
    int len;
    va_list args;
    va_start(args, fmt);
    len = jio_vfprintf(f, fmt, args);
    va_end(args);
    return len;
  }

  int jio_vfprintf(FILE* f, const char *fmt, va_list args) {
    return vfprintf(f, fmt, args);
  }
}

void*
JVM_RawMonitorCreate(void) {
    JvmUnimplemented("JVM_RawMonitorCreate");
}

void
JVM_RawMonitorDestroy(void *mon) {
    JvmUnimplemented("JVM_RawMonitorDestroy");
}

jint
JVM_RawMonitorEnter(void *mon) {
    JvmUnimplemented("JVM_RawMonitorEnter");
}

void
JVM_RawMonitorExit(void *mon) {
    JvmUnimplemented("JVM_RawMonitorExit");
}

void*
JVM_GetManagement(jint version) {
    JvmUnimplemented("JVM_GetManagement");
}

jobject
JVM_InitAgentProperties(JNIEnv *env, jobject agent_props) {
    JvmUnimplemented("JVM_InitAgentProperties");
}

jstring
JVM_GetTemporaryDirectory(JNIEnv *env) {
    JvmUnimplemented("JVM_GetTemporaryDirectory");
}

jobjectArray
JVM_GetEnclosingMethodInfo(JNIEnv* env, jclass ofClass) {
    JvmUnimplemented("JVM_GetEnclosingMethodInfo");
}

jintArray
JVM_GetThreadStateValues(JNIEnv* env, jint javaThreadState) {
    JvmUnimplemented("JVM_GetThreadStateValues");
}

jobjectArray
JVM_GetThreadStateNames(JNIEnv* env, jint javaThreadState, jintArray values) {
    JvmUnimplemented("JVM_GetThreadStateNames");
}

void
JVM_GetVersionInfo(JNIEnv* env, jvm_version_info* info, size_t info_size) {
    JvmIgnore("JVM_GetVersionInfo");
}

} // extern "C"
