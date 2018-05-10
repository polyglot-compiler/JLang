#include <cstdio>
#include <cstdlib>
#include <execinfo.h>

[[noreturn]] static void jvm_Unimplemented(const char* name) {
    fprintf(stderr,
        "- - - - - - - - - - - - - - - - - - - - - - - - - - -\n"
        "The following JVM method is currently unimplemented:\n"
        "  %s\n"
        "It is defined in " __FILE__ ".\n"
        "Note that the signature of this stub may not be correct;\n"
        "you can find the correct signature in jvm.h within the JDK.\n"
        "Aborting for now.\n"
        "- - - - - - - - - - - - - - - - - - - - - - - - - - -\n"
        , name);

    // Dump stack trace.
    constexpr int max_frames = 256;
    void* callstack[max_frames];
    int frames = backtrace(callstack, max_frames);
    backtrace_symbols_fd(callstack, frames, fileno(stderr));

    fflush(stderr);
    abort();
}

extern "C" {

void AsyncGetCallTrace() {
    jvm_Unimplemented("AsyncGetCallTrace");
}

void JNI_CreateJavaVM() {
    jvm_Unimplemented("JNI_CreateJavaVM");
}

void JNI_GetCreatedJavaVMs() {
    jvm_Unimplemented("JNI_GetCreatedJavaVMs");
}

void JNI_GetDefaultJavaVMInitArgs() {
    jvm_Unimplemented("JNI_GetDefaultJavaVMInitArgs");
}

void JVM_Accept() {
    jvm_Unimplemented("JVM_Accept");
}

void JVM_ActiveProcessorCount() {
    jvm_Unimplemented("JVM_ActiveProcessorCount");
}

void JVM_AllocateNewArray() {
    jvm_Unimplemented("JVM_AllocateNewArray");
}

void JVM_AllocateNewObject() {
    jvm_Unimplemented("JVM_AllocateNewObject");
}

void JVM_ArrayCopy() {
    jvm_Unimplemented("JVM_ArrayCopy");
}

void JVM_AssertionStatusDirectives() {
    jvm_Unimplemented("JVM_AssertionStatusDirectives");
}

void JVM_Available() {
    jvm_Unimplemented("JVM_Available");
}

void JVM_Bind() {
    jvm_Unimplemented("JVM_Bind");
}

void JVM_CX8Field() {
    jvm_Unimplemented("JVM_CX8Field");
}

void JVM_ClassDepth() {
    jvm_Unimplemented("JVM_ClassDepth");
}

void JVM_ClassLoaderDepth() {
    jvm_Unimplemented("JVM_ClassLoaderDepth");
}

void JVM_Clone() {
    jvm_Unimplemented("JVM_Clone");
}

void JVM_Close() {
    jvm_Unimplemented("JVM_Close");
}

void JVM_CompileClass() {
    jvm_Unimplemented("JVM_CompileClass");
}

void JVM_CompileClasses() {
    jvm_Unimplemented("JVM_CompileClasses");
}

void JVM_CompilerCommand() {
    jvm_Unimplemented("JVM_CompilerCommand");
}

void JVM_Connect() {
    jvm_Unimplemented("JVM_Connect");
}

void JVM_ConstantPoolGetClassAt() {
    jvm_Unimplemented("JVM_ConstantPoolGetClassAt");
}

void JVM_ConstantPoolGetClassAtIfLoaded() {
    jvm_Unimplemented("JVM_ConstantPoolGetClassAtIfLoaded");
}

void JVM_ConstantPoolGetDoubleAt() {
    jvm_Unimplemented("JVM_ConstantPoolGetDoubleAt");
}

void JVM_ConstantPoolGetFieldAt() {
    jvm_Unimplemented("JVM_ConstantPoolGetFieldAt");
}

void JVM_ConstantPoolGetFieldAtIfLoaded() {
    jvm_Unimplemented("JVM_ConstantPoolGetFieldAtIfLoaded");
}

void JVM_ConstantPoolGetFloatAt() {
    jvm_Unimplemented("JVM_ConstantPoolGetFloatAt");
}

void JVM_ConstantPoolGetIntAt() {
    jvm_Unimplemented("JVM_ConstantPoolGetIntAt");
}

void JVM_ConstantPoolGetLongAt() {
    jvm_Unimplemented("JVM_ConstantPoolGetLongAt");
}

void JVM_ConstantPoolGetMemberRefInfoAt() {
    jvm_Unimplemented("JVM_ConstantPoolGetMemberRefInfoAt");
}

void JVM_ConstantPoolGetMethodAt() {
    jvm_Unimplemented("JVM_ConstantPoolGetMethodAt");
}

void JVM_ConstantPoolGetMethodAtIfLoaded() {
    jvm_Unimplemented("JVM_ConstantPoolGetMethodAtIfLoaded");
}

void JVM_ConstantPoolGetSize() {
    jvm_Unimplemented("JVM_ConstantPoolGetSize");
}

void JVM_ConstantPoolGetStringAt() {
    jvm_Unimplemented("JVM_ConstantPoolGetStringAt");
}

void JVM_ConstantPoolGetUTF8At() {
    jvm_Unimplemented("JVM_ConstantPoolGetUTF8At");
}

void JVM_CountStackFrames() {
    jvm_Unimplemented("JVM_CountStackFrames");
}

void JVM_CurrentClassLoader() {
    jvm_Unimplemented("JVM_CurrentClassLoader");
}

void JVM_CurrentLoadedClass() {
    jvm_Unimplemented("JVM_CurrentLoadedClass");
}

void JVM_CurrentThread() {
    jvm_Unimplemented("JVM_CurrentThread");
}

void JVM_CurrentTimeMillis() {
    jvm_Unimplemented("JVM_CurrentTimeMillis");
}

void JVM_DTraceActivate() {
    jvm_Unimplemented("JVM_DTraceActivate");
}

void JVM_DTraceDispose() {
    jvm_Unimplemented("JVM_DTraceDispose");
}

void JVM_DTraceGetVersion() {
    jvm_Unimplemented("JVM_DTraceGetVersion");
}

void JVM_DTraceIsProbeEnabled() {
    jvm_Unimplemented("JVM_DTraceIsProbeEnabled");
}

void JVM_DTraceIsSupported() {
    jvm_Unimplemented("JVM_DTraceIsSupported");
}

void JVM_DefineClass() {
    jvm_Unimplemented("JVM_DefineClass");
}

void JVM_DefineClassWithSource() {
    jvm_Unimplemented("JVM_DefineClassWithSource");
}

void JVM_DefineClassWithSourceCond() {
    jvm_Unimplemented("JVM_DefineClassWithSourceCond");
}

void JVM_DesiredAssertionStatus() {
    jvm_Unimplemented("JVM_DesiredAssertionStatus");
}

void JVM_DisableCompiler() {
    jvm_Unimplemented("JVM_DisableCompiler");
}

void JVM_DoPrivileged() {
    jvm_Unimplemented("JVM_DoPrivileged");
}

void JVM_DumpAllStacks() {
    jvm_Unimplemented("JVM_DumpAllStacks");
}

void JVM_DumpThreads() {
    jvm_Unimplemented("JVM_DumpThreads");
}

void JVM_EnableCompiler() {
    jvm_Unimplemented("JVM_EnableCompiler");
}

void JVM_Exit() {
    jvm_Unimplemented("JVM_Exit");
}

void JVM_FillInStackTrace() {
    jvm_Unimplemented("JVM_FillInStackTrace");
}

void JVM_FindClassFromBootLoader() {
    jvm_Unimplemented("JVM_FindClassFromBootLoader");
}

void JVM_FindClassFromCaller() {
    jvm_Unimplemented("JVM_FindClassFromCaller");
}

void JVM_FindClassFromClass() {
    jvm_Unimplemented("JVM_FindClassFromClass");
}

void JVM_FindClassFromClassLoader() {
    jvm_Unimplemented("JVM_FindClassFromClassLoader");
}

void JVM_FindLibraryEntry() {
    jvm_Unimplemented("JVM_FindLibraryEntry");
}

void JVM_FindLoadedClass() {
    jvm_Unimplemented("JVM_FindLoadedClass");
}

void JVM_FindPrimitiveClass() {
    jvm_Unimplemented("JVM_FindPrimitiveClass");
}

void JVM_FindSignal() {
    jvm_Unimplemented("JVM_FindSignal");
}

void JVM_FreeMemory() {
    jvm_Unimplemented("JVM_FreeMemory");
}

void JVM_GC() {
    jvm_Unimplemented("JVM_GC");
}

void JVM_GetAllThreads() {
    jvm_Unimplemented("JVM_GetAllThreads");
}

void JVM_GetArrayElement() {
    jvm_Unimplemented("JVM_GetArrayElement");
}

void JVM_GetArrayLength() {
    jvm_Unimplemented("JVM_GetArrayLength");
}

void JVM_GetCPClassNameUTF() {
    jvm_Unimplemented("JVM_GetCPClassNameUTF");
}

void JVM_GetCPFieldClassNameUTF() {
    jvm_Unimplemented("JVM_GetCPFieldClassNameUTF");
}

void JVM_GetCPFieldModifiers() {
    jvm_Unimplemented("JVM_GetCPFieldModifiers");
}

void JVM_GetCPFieldNameUTF() {
    jvm_Unimplemented("JVM_GetCPFieldNameUTF");
}

void JVM_GetCPFieldSignatureUTF() {
    jvm_Unimplemented("JVM_GetCPFieldSignatureUTF");
}

void JVM_GetCPMethodClassNameUTF() {
    jvm_Unimplemented("JVM_GetCPMethodClassNameUTF");
}

void JVM_GetCPMethodModifiers() {
    jvm_Unimplemented("JVM_GetCPMethodModifiers");
}

void JVM_GetCPMethodNameUTF() {
    jvm_Unimplemented("JVM_GetCPMethodNameUTF");
}

void JVM_GetCPMethodSignatureUTF() {
    jvm_Unimplemented("JVM_GetCPMethodSignatureUTF");
}

void JVM_GetCallerClass() {
    jvm_Unimplemented("JVM_GetCallerClass");
}

void JVM_GetClassAccessFlags() {
    jvm_Unimplemented("JVM_GetClassAccessFlags");
}

void JVM_GetClassAnnotations() {
    jvm_Unimplemented("JVM_GetClassAnnotations");
}

void JVM_GetClassCPEntriesCount() {
    jvm_Unimplemented("JVM_GetClassCPEntriesCount");
}

void JVM_GetClassCPTypes() {
    jvm_Unimplemented("JVM_GetClassCPTypes");
}

void JVM_GetClassConstantPool() {
    jvm_Unimplemented("JVM_GetClassConstantPool");
}

void JVM_GetClassContext() {
    jvm_Unimplemented("JVM_GetClassContext");
}

void JVM_GetClassDeclaredConstructors() {
    jvm_Unimplemented("JVM_GetClassDeclaredConstructors");
}

void JVM_GetClassDeclaredFields() {
    jvm_Unimplemented("JVM_GetClassDeclaredFields");
}

void JVM_GetClassDeclaredMethods() {
    jvm_Unimplemented("JVM_GetClassDeclaredMethods");
}

void JVM_GetClassFieldsCount() {
    jvm_Unimplemented("JVM_GetClassFieldsCount");
}

void JVM_GetClassInterfaces() {
    jvm_Unimplemented("JVM_GetClassInterfaces");
}

void JVM_GetClassLoader() {
    jvm_Unimplemented("JVM_GetClassLoader");
}

void JVM_GetClassMethodsCount() {
    jvm_Unimplemented("JVM_GetClassMethodsCount");
}

void JVM_GetClassModifiers() {
    jvm_Unimplemented("JVM_GetClassModifiers");
}

void JVM_GetClassName() {
    jvm_Unimplemented("JVM_GetClassName");
}

void JVM_GetClassNameUTF() {
    jvm_Unimplemented("JVM_GetClassNameUTF");
}

void JVM_GetClassSignature() {
    jvm_Unimplemented("JVM_GetClassSignature");
}

void JVM_GetClassSigners() {
    jvm_Unimplemented("JVM_GetClassSigners");
}

void JVM_GetComponentType() {
    jvm_Unimplemented("JVM_GetComponentType");
}

void JVM_GetDeclaredClasses() {
    jvm_Unimplemented("JVM_GetDeclaredClasses");
}

void JVM_GetDeclaringClass() {
    jvm_Unimplemented("JVM_GetDeclaringClass");
}

void JVM_GetEnclosingMethodInfo() {
    jvm_Unimplemented("JVM_GetEnclosingMethodInfo");
}

void JVM_GetFieldAnnotations() {
    jvm_Unimplemented("JVM_GetFieldAnnotations");
}

void JVM_GetFieldIxModifiers() {
    jvm_Unimplemented("JVM_GetFieldIxModifiers");
}

void JVM_GetHostName() {
    jvm_Unimplemented("JVM_GetHostName");
}

void JVM_GetInheritedAccessControlContext() {
    jvm_Unimplemented("JVM_GetInheritedAccessControlContext");
}

void JVM_GetInterfaceVersion() {
    jvm_Unimplemented("JVM_GetInterfaceVersion");
}

void JVM_GetLastErrorString() {
    jvm_Unimplemented("JVM_GetLastErrorString");
}

void JVM_GetManagement() {
    jvm_Unimplemented("JVM_GetManagement");
}

void JVM_GetMethodAnnotations() {
    jvm_Unimplemented("JVM_GetMethodAnnotations");
}

void JVM_GetMethodDefaultAnnotationValue() {
    jvm_Unimplemented("JVM_GetMethodDefaultAnnotationValue");
}

void JVM_GetMethodIxArgsSize() {
    jvm_Unimplemented("JVM_GetMethodIxArgsSize");
}

void JVM_GetMethodIxByteCode() {
    jvm_Unimplemented("JVM_GetMethodIxByteCode");
}

void JVM_GetMethodIxByteCodeLength() {
    jvm_Unimplemented("JVM_GetMethodIxByteCodeLength");
}

void JVM_GetMethodIxExceptionIndexes() {
    jvm_Unimplemented("JVM_GetMethodIxExceptionIndexes");
}

void JVM_GetMethodIxExceptionTableEntry() {
    jvm_Unimplemented("JVM_GetMethodIxExceptionTableEntry");
}

void JVM_GetMethodIxExceptionTableLength() {
    jvm_Unimplemented("JVM_GetMethodIxExceptionTableLength");
}

void JVM_GetMethodIxExceptionsCount() {
    jvm_Unimplemented("JVM_GetMethodIxExceptionsCount");
}

void JVM_GetMethodIxLocalsCount() {
    jvm_Unimplemented("JVM_GetMethodIxLocalsCount");
}

void JVM_GetMethodIxMaxStack() {
    jvm_Unimplemented("JVM_GetMethodIxMaxStack");
}

void JVM_GetMethodIxModifiers() {
    jvm_Unimplemented("JVM_GetMethodIxModifiers");
}

void JVM_GetMethodIxNameUTF() {
    jvm_Unimplemented("JVM_GetMethodIxNameUTF");
}

void JVM_GetMethodIxSignatureUTF() {
    jvm_Unimplemented("JVM_GetMethodIxSignatureUTF");
}

void JVM_GetMethodParameterAnnotations() {
    jvm_Unimplemented("JVM_GetMethodParameterAnnotations");
}

void JVM_GetPrimitiveArrayElement() {
    jvm_Unimplemented("JVM_GetPrimitiveArrayElement");
}

void JVM_GetProtectionDomain() {
    jvm_Unimplemented("JVM_GetProtectionDomain");
}

void JVM_GetSockName() {
    jvm_Unimplemented("JVM_GetSockName");
}

void JVM_GetSockOpt() {
    jvm_Unimplemented("JVM_GetSockOpt");
}

void JVM_GetStackAccessControlContext() {
    jvm_Unimplemented("JVM_GetStackAccessControlContext");
}

void JVM_GetStackTraceDepth() {
    jvm_Unimplemented("JVM_GetStackTraceDepth");
}

void JVM_GetStackTraceElement() {
    jvm_Unimplemented("JVM_GetStackTraceElement");
}

void JVM_GetSystemPackage() {
    jvm_Unimplemented("JVM_GetSystemPackage");
}

void JVM_GetSystemPackages() {
    jvm_Unimplemented("JVM_GetSystemPackages");
}

void JVM_GetTemporaryDirectory() {
    jvm_Unimplemented("JVM_GetTemporaryDirectory");
}

void JVM_GetThreadStateNames() {
    jvm_Unimplemented("JVM_GetThreadStateNames");
}

void JVM_GetThreadStateValues() {
    jvm_Unimplemented("JVM_GetThreadStateValues");
}

void JVM_GetVersionInfo() {
    jvm_Unimplemented("JVM_GetVersionInfo");
}

void JVM_Halt() {
    jvm_Unimplemented("JVM_Halt");
}

void JVM_HoldsLock() {
    jvm_Unimplemented("JVM_HoldsLock");
}

void JVM_IHashCode() {
    jvm_Unimplemented("JVM_IHashCode");
}

void JVM_InitAgentProperties() {
    jvm_Unimplemented("JVM_InitAgentProperties");
}

void JVM_InitProperties() {
    jvm_Unimplemented("JVM_InitProperties");
}

void JVM_InitializeCompiler() {
    jvm_Unimplemented("JVM_InitializeCompiler");
}

void JVM_InitializeSocketLibrary() {
    jvm_Unimplemented("JVM_InitializeSocketLibrary");
}

void JVM_InternString() {
    jvm_Unimplemented("JVM_InternString");
}

void JVM_Interrupt() {
    jvm_Unimplemented("JVM_Interrupt");
}

void JVM_InvokeMethod() {
    jvm_Unimplemented("JVM_InvokeMethod");
}

void JVM_IsArrayClass() {
    jvm_Unimplemented("JVM_IsArrayClass");
}

void JVM_IsConstructorIx() {
    jvm_Unimplemented("JVM_IsConstructorIx");
}

void JVM_IsInterface() {
    jvm_Unimplemented("JVM_IsInterface");
}

void JVM_IsInterrupted() {
    jvm_Unimplemented("JVM_IsInterrupted");
}

void JVM_IsNaN() {
    jvm_Unimplemented("JVM_IsNaN");
}

void JVM_IsPrimitiveClass() {
    jvm_Unimplemented("JVM_IsPrimitiveClass");
}

void JVM_IsSameClassPackage() {
    jvm_Unimplemented("JVM_IsSameClassPackage");
}

void JVM_IsSilentCompiler() {
    jvm_Unimplemented("JVM_IsSilentCompiler");
}

void JVM_IsSupportedJNIVersion() {
    jvm_Unimplemented("JVM_IsSupportedJNIVersion");
}

void JVM_IsThreadAlive() {
    jvm_Unimplemented("JVM_IsThreadAlive");
}

void JVM_LatestUserDefinedLoader() {
    jvm_Unimplemented("JVM_LatestUserDefinedLoader");
}

void JVM_Listen() {
    jvm_Unimplemented("JVM_Listen");
}

void JVM_LoadClass0() {
    jvm_Unimplemented("JVM_LoadClass0");
}

void JVM_LoadLibrary() {
    jvm_Unimplemented("JVM_LoadLibrary");
}

void JVM_Lseek() {
    jvm_Unimplemented("JVM_Lseek");
}

void JVM_MaxMemory() {
    jvm_Unimplemented("JVM_MaxMemory");
}

void JVM_MaxObjectInspectionAge() {
    jvm_Unimplemented("JVM_MaxObjectInspectionAge");
}

void JVM_MonitorNotify() {
    jvm_Unimplemented("JVM_MonitorNotify");
}

void JVM_MonitorNotifyAll() {
    jvm_Unimplemented("JVM_MonitorNotifyAll");
}

void JVM_MonitorWait() {
    jvm_Unimplemented("JVM_MonitorWait");
}

void JVM_NanoTime() {
    jvm_Unimplemented("JVM_NanoTime");
}

void JVM_NativePath() {
    jvm_Unimplemented("JVM_NativePath");
}

void JVM_NewArray() {
    jvm_Unimplemented("JVM_NewArray");
}

void JVM_NewInstanceFromConstructor() {
    jvm_Unimplemented("JVM_NewInstanceFromConstructor");
}

void JVM_NewMultiArray() {
    jvm_Unimplemented("JVM_NewMultiArray");
}

void JVM_OnExit() {
    jvm_Unimplemented("JVM_OnExit");
}

void JVM_Open() {
    jvm_Unimplemented("JVM_Open");
}

void JVM_PrintStackTrace() {
    jvm_Unimplemented("JVM_PrintStackTrace");
}

void JVM_RaiseSignal() {
    jvm_Unimplemented("JVM_RaiseSignal");
}

void JVM_RawMonitorCreate() {
    jvm_Unimplemented("JVM_RawMonitorCreate");
}

void JVM_RawMonitorDestroy() {
    jvm_Unimplemented("JVM_RawMonitorDestroy");
}

void JVM_RawMonitorEnter() {
    jvm_Unimplemented("JVM_RawMonitorEnter");
}

void JVM_RawMonitorExit() {
    jvm_Unimplemented("JVM_RawMonitorExit");
}

void JVM_Read() {
    jvm_Unimplemented("JVM_Read");
}

void JVM_Recv() {
    jvm_Unimplemented("JVM_Recv");
}

void JVM_RecvFrom() {
    jvm_Unimplemented("JVM_RecvFrom");
}

void JVM_RegisterSignal() {
    jvm_Unimplemented("JVM_RegisterSignal");
}

void JVM_ReleaseUTF() {
    jvm_Unimplemented("JVM_ReleaseUTF");
}

void JVM_ResolveClass() {
    jvm_Unimplemented("JVM_ResolveClass");
}

void JVM_ResumeThread() {
    jvm_Unimplemented("JVM_ResumeThread");
}

void JVM_Send() {
    jvm_Unimplemented("JVM_Send");
}

void JVM_SendTo() {
    jvm_Unimplemented("JVM_SendTo");
}

void JVM_SetArrayElement() {
    jvm_Unimplemented("JVM_SetArrayElement");
}

void JVM_SetClassSigners() {
    jvm_Unimplemented("JVM_SetClassSigners");
}

void JVM_SetLength() {
    jvm_Unimplemented("JVM_SetLength");
}

void JVM_SetNativeThreadName() {
    jvm_Unimplemented("JVM_SetNativeThreadName");
}

void JVM_SetPrimitiveArrayElement() {
    jvm_Unimplemented("JVM_SetPrimitiveArrayElement");
}

void JVM_SetProtectionDomain() {
    jvm_Unimplemented("JVM_SetProtectionDomain");
}

void JVM_SetSockOpt() {
    jvm_Unimplemented("JVM_SetSockOpt");
}

void JVM_SetThreadPriority() {
    jvm_Unimplemented("JVM_SetThreadPriority");
}

void JVM_Sleep() {
    jvm_Unimplemented("JVM_Sleep");
}

void JVM_Socket() {
    jvm_Unimplemented("JVM_Socket");
}

void JVM_SocketAvailable() {
    jvm_Unimplemented("JVM_SocketAvailable");
}

void JVM_SocketClose() {
    jvm_Unimplemented("JVM_SocketClose");
}

void JVM_SocketShutdown() {
    jvm_Unimplemented("JVM_SocketShutdown");
}

void JVM_StartThread() {
    jvm_Unimplemented("JVM_StartThread");
}

void JVM_StopThread() {
    jvm_Unimplemented("JVM_StopThread");
}

void JVM_SupportsCX8() {
    jvm_Unimplemented("JVM_SupportsCX8");
}

void JVM_SuspendThread() {
    jvm_Unimplemented("JVM_SuspendThread");
}

void JVM_Sync() {
    jvm_Unimplemented("JVM_Sync");
}

void JVM_Timeout() {
    jvm_Unimplemented("JVM_Timeout");
}

void JVM_TotalMemory() {
    jvm_Unimplemented("JVM_TotalMemory");
}

void JVM_TraceInstructions() {
    jvm_Unimplemented("JVM_TraceInstructions");
}

void JVM_TraceMethodCalls() {
    jvm_Unimplemented("JVM_TraceMethodCalls");
}

void JVM_UnloadLibrary() {
    jvm_Unimplemented("JVM_UnloadLibrary");
}

void JVM_Write() {
    jvm_Unimplemented("JVM_Write");
}

void JVM_Yield() {
    jvm_Unimplemented("JVM_Yield");
}

void JVM_handle_bsd_signal() {
    jvm_Unimplemented("JVM_handle_bsd_signal");
}

void jio_fprintf() {
    jvm_Unimplemented("jio_fprintf");
}

void jio_printf() {
    jvm_Unimplemented("jio_printf");
}

void jio_snprintf() {
    jvm_Unimplemented("jio_snprintf");
}

void jio_vfprintf() {
    jvm_Unimplemented("jio_vfprintf");
}

void jio_vsnprintf() {
    jvm_Unimplemented("jio_vsnprintf");
}

} // extern "C"
