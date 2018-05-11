#include <cstdio>
#include <cstdlib>

[[noreturn]] static void unlinked(const char* name) {
  fprintf(stderr,
    "- - - - - - - - - - - - - - - - - - - - - - - - - - -\n"
    "The following native method is currently unlinked:\n"
    "  %s\n"
    "This stub is defined in " __FILE__ ".\n"
    "Aborting for now.\n"
    "- - - - - - - - - - - - - - - - - - - - - - - - - - -\n"
    , name);
  fflush(stderr);
  abort();
}

extern "C" {

// Begin weird anomalies.

void Java_java_lang_Enum_compareTo__Ljava_lang_Object_2() {
    // For some reason Polyglot adds this method to java.lang.Enum with
    // the wrong argument type, in addition to the correct version.
    // This should be fixed, because this likely breaks
    // method dispatch for enums.
    unlinked("Java_java_lang_Enum_compareTo__Ljava_lang_Object_2");
}

// Begin missing shared library methods.

void Java_sun_security_krb5_Credentials_acquireDefaultNativeCreds() {
    unlinked("Java_sun_security_krb5_Credentials_acquireDefaultNativeCreds");
}

void Java_sun_security_krb5_SCDynamicStoreConfig_getKerberosConfig() {
    unlinked("Java_sun_security_krb5_SCDynamicStoreConfig_getKerberosConfig");
}

void Java_sun_security_krb5_SCDynamicStoreConfig_installNotificationCallback() {
    unlinked("Java_sun_security_krb5_SCDynamicStoreConfig_installNotificationCallback");
}

// Begin methods presumably registered at runtime through resgisterNatives().

void Java_java_lang_Enum_readObject__Ljava_lang_Object_2() {
    unlinked("Java_java_lang_Enum_readObject__Ljava_lang_Object_2");
}

void Java_java_lang_Object_clone__() {
    unlinked("Java_java_lang_Object_clone__");
}

void Java_java_lang_Object_notifyAll__() {
    unlinked("Java_java_lang_Object_notifyAll__");
}

void Java_java_lang_Object_notify__() {
    unlinked("Java_java_lang_Object_notify__");
}

void Java_java_lang_Object_wait__J() {
    unlinked("Java_java_lang_Object_wait__J");
}

void Java_java_lang_Object_hashCode() {
    unlinked("Java_java_lang_Object_hashCode");
}

void Java_java_lang_System_PrintStream_println__Ljava_lang_String_2() {
    unlinked("Java_java_lang_System_PrintStream_println__Ljava_lang_String_2");
}

void Java_java_lang_System_currentTimeMillis() {
    unlinked("Java_java_lang_System_currentTimeMillis");
}

void Java_java_lang_System_nanoTime() {
    unlinked("Java_java_lang_System_nanoTime");
}

void Java_java_io_FileDescriptor_set() {
    unlinked("Java_java_io_FileDescriptor_set");
}

void Java_java_io_FileInputStream_read() {
    unlinked("Java_java_io_FileInputStream_read");
}

void Java_java_io_ObjectInputStream_latestUserDefinedLoader() {
    unlinked("Java_java_io_ObjectInputStream_latestUserDefinedLoader");
}

void Java_java_lang_ClassLoader_NativeLibrary_find() {
    unlinked("Java_java_lang_ClassLoader_NativeLibrary_find");
}

void Java_java_lang_ClassLoader_NativeLibrary_load() {
    unlinked("Java_java_lang_ClassLoader_NativeLibrary_load");
}

void Java_java_lang_ClassLoader_NativeLibrary_unload() {
    unlinked("Java_java_lang_ClassLoader_NativeLibrary_unload");
}

void Java_java_lang_ClassLoader_getCaller() {
    unlinked("Java_java_lang_ClassLoader_getCaller");
}

void Java_java_lang_ClassLoader_retrieveDirectives() {
    unlinked("Java_java_lang_ClassLoader_retrieveDirectives");
}

void Java_java_lang_Class_desiredAssertionStatus0() {
    unlinked("Java_java_lang_Class_desiredAssertionStatus0");
}

void Java_java_lang_Class_getClassLoader0() {
    unlinked("Java_java_lang_Class_getClassLoader0");
}

void Java_java_lang_Class_getComponentType() {
    unlinked("Java_java_lang_Class_getComponentType");
}

void Java_java_lang_Class_getConstantPool() {
    unlinked("Java_java_lang_Class_getConstantPool");
}

void Java_java_lang_Class_getDeclaredClasses0() {
    unlinked("Java_java_lang_Class_getDeclaredClasses0");
}

void Java_java_lang_Class_getDeclaredConstructors0() {
    unlinked("Java_java_lang_Class_getDeclaredConstructors0");
}

void Java_java_lang_Class_getDeclaredFields0() {
    unlinked("Java_java_lang_Class_getDeclaredFields0");
}

void Java_java_lang_Class_getDeclaredMethods0() {
    unlinked("Java_java_lang_Class_getDeclaredMethods0");
}

void Java_java_lang_Class_getDeclaringClass() {
    unlinked("Java_java_lang_Class_getDeclaringClass");
}

void Java_java_lang_Class_getEnclosingMethod0() {
    unlinked("Java_java_lang_Class_getEnclosingMethod0");
}

void Java_java_lang_Class_getGenericSignature() {
    unlinked("Java_java_lang_Class_getGenericSignature");
}

void Java_java_lang_Class_getInterfaces() {
    unlinked("Java_java_lang_Class_getInterfaces");
}

void Java_java_lang_Class_getModifiers() {
    unlinked("Java_java_lang_Class_getModifiers");
}

void Java_java_lang_Class_getName0() {
    unlinked("Java_java_lang_Class_getName0");
}

void Java_java_lang_Class_getProtectionDomain0() {
    unlinked("Java_java_lang_Class_getProtectionDomain0");
}

void Java_java_lang_Class_getRawAnnotations() {
    unlinked("Java_java_lang_Class_getRawAnnotations");
}

void Java_java_lang_Class_getSigners() {
    unlinked("Java_java_lang_Class_getSigners");
}

void Java_java_lang_Class_getSuperclass() {
    unlinked("Java_java_lang_Class_getSuperclass");
}

void Java_java_lang_Class_isArray() {
    unlinked("Java_java_lang_Class_isArray");
}

void Java_java_lang_Class_isInterface() {
    unlinked("Java_java_lang_Class_isInterface");
}

void Java_java_lang_Class_isPrimitive() {
    unlinked("Java_java_lang_Class_isPrimitive");
}

void Java_java_lang_Class_setProtectionDomain0() {
    unlinked("Java_java_lang_Class_setProtectionDomain0");
}

void Java_java_lang_Class_setSigners() {
    unlinked("Java_java_lang_Class_setSigners");
}

void Java_java_lang_Object_clone() {
    unlinked("Java_java_lang_Object_clone");
}

void Java_java_lang_Object_notify() {
    unlinked("Java_java_lang_Object_notify");
}

void Java_java_lang_Object_notifyAll() {
    unlinked("Java_java_lang_Object_notifyAll");
}

void Java_java_lang_Object_wait() {
    unlinked("Java_java_lang_Object_wait");
}

void Java_java_lang_ProcessEnvironment_environmentBlock() {
    unlinked("Java_java_lang_ProcessEnvironment_environmentBlock");
}

void Java_java_lang_ProcessImpl_closeHandle() {
    unlinked("Java_java_lang_ProcessImpl_closeHandle");
}

void Java_java_lang_ProcessImpl_create() {
    unlinked("Java_java_lang_ProcessImpl_create");
}

void Java_java_lang_ProcessImpl_getExitCodeProcess() {
    unlinked("Java_java_lang_ProcessImpl_getExitCodeProcess");
}

void Java_java_lang_ProcessImpl_getStillActive() {
    unlinked("Java_java_lang_ProcessImpl_getStillActive");
}

void Java_java_lang_ProcessImpl_openForAtomicAppend() {
    unlinked("Java_java_lang_ProcessImpl_openForAtomicAppend");
}

void Java_java_lang_ProcessImpl_terminateProcess() {
    unlinked("Java_java_lang_ProcessImpl_terminateProcess");
}

void Java_java_lang_ProcessImpl_waitForInterruptibly() {
    unlinked("Java_java_lang_ProcessImpl_waitForInterruptibly");
}

void Java_java_lang_System_arraycopy() {
    unlinked("Java_java_lang_System_arraycopy");
}

void Java_java_lang_Thread_countStackFrames() {
    unlinked("Java_java_lang_Thread_countStackFrames");
}

void Java_java_lang_Thread_currentThread() {
    unlinked("Java_java_lang_Thread_currentThread");
}

void Java_java_lang_Thread_dumpThreads() {
    unlinked("Java_java_lang_Thread_dumpThreads");
}

void Java_java_lang_Thread_getThreads() {
    unlinked("Java_java_lang_Thread_getThreads");
}

void Java_java_lang_Thread_holdsLock() {
    unlinked("Java_java_lang_Thread_holdsLock");
}

void Java_java_lang_Thread_interrupt0() {
    unlinked("Java_java_lang_Thread_interrupt0");
}

void Java_java_lang_Thread_isAlive() {
    unlinked("Java_java_lang_Thread_isAlive");
}

void Java_java_lang_Thread_isInterrupted() {
    unlinked("Java_java_lang_Thread_isInterrupted");
}

void Java_java_lang_Thread_resume0() {
    unlinked("Java_java_lang_Thread_resume0");
}

void Java_java_lang_Thread_setNativeName() {
    unlinked("Java_java_lang_Thread_setNativeName");
}

void Java_java_lang_Thread_setPriority0() {
    unlinked("Java_java_lang_Thread_setPriority0");
}

void Java_java_lang_Thread_sleep() {
    unlinked("Java_java_lang_Thread_sleep");
}

void Java_java_lang_Thread_start0() {
    unlinked("Java_java_lang_Thread_start0");
}

void Java_java_lang_Thread_stop0() {
    unlinked("Java_java_lang_Thread_stop0");
}

void Java_java_lang_Thread_suspend0() {
    unlinked("Java_java_lang_Thread_suspend0");
}

void Java_java_lang_Thread_yield() {
    unlinked("Java_java_lang_Thread_yield");
}

void Java_java_net_DualStackPlainDatagramSocketImpl_socketBind() {
    unlinked("Java_java_net_DualStackPlainDatagramSocketImpl_socketBind");
}

void Java_java_net_DualStackPlainDatagramSocketImpl_socketClose() {
    unlinked("Java_java_net_DualStackPlainDatagramSocketImpl_socketClose");
}

void Java_java_net_DualStackPlainDatagramSocketImpl_socketConnect() {
    unlinked("Java_java_net_DualStackPlainDatagramSocketImpl_socketConnect");
}

void Java_java_net_DualStackPlainDatagramSocketImpl_socketCreate() {
    unlinked("Java_java_net_DualStackPlainDatagramSocketImpl_socketCreate");
}

void Java_java_net_DualStackPlainDatagramSocketImpl_socketDisconnect() {
    unlinked("Java_java_net_DualStackPlainDatagramSocketImpl_socketDisconnect");
}

void Java_java_net_DualStackPlainDatagramSocketImpl_socketGetIntOption() {
    unlinked("Java_java_net_DualStackPlainDatagramSocketImpl_socketGetIntOption");
}

void Java_java_net_DualStackPlainDatagramSocketImpl_socketLocalAddress() {
    unlinked("Java_java_net_DualStackPlainDatagramSocketImpl_socketLocalAddress");
}

void Java_java_net_DualStackPlainDatagramSocketImpl_socketLocalPort() {
    unlinked("Java_java_net_DualStackPlainDatagramSocketImpl_socketLocalPort");
}

void Java_java_net_DualStackPlainDatagramSocketImpl_socketReceiveOrPeekData() {
    unlinked("Java_java_net_DualStackPlainDatagramSocketImpl_socketReceiveOrPeekData");
}

void Java_java_net_DualStackPlainDatagramSocketImpl_socketSend() {
    unlinked("Java_java_net_DualStackPlainDatagramSocketImpl_socketSend");
}

void Java_java_net_DualStackPlainDatagramSocketImpl_socketSetIntOption() {
    unlinked("Java_java_net_DualStackPlainDatagramSocketImpl_socketSetIntOption");
}

void Java_java_net_DualStackPlainSocketImpl_accept0() {
    unlinked("Java_java_net_DualStackPlainSocketImpl_accept0");
}

void Java_java_net_DualStackPlainSocketImpl_available0() {
    unlinked("Java_java_net_DualStackPlainSocketImpl_available0");
}

void Java_java_net_DualStackPlainSocketImpl_bind0() {
    unlinked("Java_java_net_DualStackPlainSocketImpl_bind0");
}

void Java_java_net_DualStackPlainSocketImpl_close0() {
    unlinked("Java_java_net_DualStackPlainSocketImpl_close0");
}

void Java_java_net_DualStackPlainSocketImpl_configureBlocking() {
    unlinked("Java_java_net_DualStackPlainSocketImpl_configureBlocking");
}

void Java_java_net_DualStackPlainSocketImpl_connect0() {
    unlinked("Java_java_net_DualStackPlainSocketImpl_connect0");
}

void Java_java_net_DualStackPlainSocketImpl_getIntOption() {
    unlinked("Java_java_net_DualStackPlainSocketImpl_getIntOption");
}

void Java_java_net_DualStackPlainSocketImpl_initIDs() {
    unlinked("Java_java_net_DualStackPlainSocketImpl_initIDs");
}

void Java_java_net_DualStackPlainSocketImpl_listen0() {
    unlinked("Java_java_net_DualStackPlainSocketImpl_listen0");
}

void Java_java_net_DualStackPlainSocketImpl_localAddress() {
    unlinked("Java_java_net_DualStackPlainSocketImpl_localAddress");
}

void Java_java_net_DualStackPlainSocketImpl_localPort0() {
    unlinked("Java_java_net_DualStackPlainSocketImpl_localPort0");
}

void Java_java_net_DualStackPlainSocketImpl_sendOOB() {
    unlinked("Java_java_net_DualStackPlainSocketImpl_sendOOB");
}

void Java_java_net_DualStackPlainSocketImpl_setIntOption() {
    unlinked("Java_java_net_DualStackPlainSocketImpl_setIntOption");
}

void Java_java_net_DualStackPlainSocketImpl_shutdown0() {
    unlinked("Java_java_net_DualStackPlainSocketImpl_shutdown0");
}

void Java_java_net_DualStackPlainSocketImpl_socket0() {
    unlinked("Java_java_net_DualStackPlainSocketImpl_socket0");
}

void Java_java_net_DualStackPlainSocketImpl_waitForConnect() {
    unlinked("Java_java_net_DualStackPlainSocketImpl_waitForConnect");
}

void Java_java_net_DualStackPlainSocketImpl_waitForNewConnection() {
    unlinked("Java_java_net_DualStackPlainSocketImpl_waitForNewConnection");
}

void Java_java_net_TwoStacksPlainDatagramSocketImpl_bind0() {
    unlinked("Java_java_net_TwoStacksPlainDatagramSocketImpl_bind0");
}

void Java_java_net_TwoStacksPlainDatagramSocketImpl_connect0() {
    unlinked("Java_java_net_TwoStacksPlainDatagramSocketImpl_connect0");
}

void Java_java_net_TwoStacksPlainDatagramSocketImpl_datagramSocketClose() {
    unlinked("Java_java_net_TwoStacksPlainDatagramSocketImpl_datagramSocketClose");
}

void Java_java_net_TwoStacksPlainDatagramSocketImpl_datagramSocketCreate() {
    unlinked("Java_java_net_TwoStacksPlainDatagramSocketImpl_datagramSocketCreate");
}

void Java_java_net_TwoStacksPlainDatagramSocketImpl_disconnect0() {
    unlinked("Java_java_net_TwoStacksPlainDatagramSocketImpl_disconnect0");
}

void Java_java_net_TwoStacksPlainDatagramSocketImpl_getTTL() {
    unlinked("Java_java_net_TwoStacksPlainDatagramSocketImpl_getTTL");
}

void Java_java_net_TwoStacksPlainDatagramSocketImpl_getTimeToLive() {
    unlinked("Java_java_net_TwoStacksPlainDatagramSocketImpl_getTimeToLive");
}

void Java_java_net_TwoStacksPlainDatagramSocketImpl_init() {
    unlinked("Java_java_net_TwoStacksPlainDatagramSocketImpl_init");
}

void Java_java_net_TwoStacksPlainDatagramSocketImpl_join() {
    unlinked("Java_java_net_TwoStacksPlainDatagramSocketImpl_join");
}

void Java_java_net_TwoStacksPlainDatagramSocketImpl_leave() {
    unlinked("Java_java_net_TwoStacksPlainDatagramSocketImpl_leave");
}

void Java_java_net_TwoStacksPlainDatagramSocketImpl_peek() {
    unlinked("Java_java_net_TwoStacksPlainDatagramSocketImpl_peek");
}

void Java_java_net_TwoStacksPlainDatagramSocketImpl_peekData() {
    unlinked("Java_java_net_TwoStacksPlainDatagramSocketImpl_peekData");
}

void Java_java_net_TwoStacksPlainDatagramSocketImpl_receive0() {
    unlinked("Java_java_net_TwoStacksPlainDatagramSocketImpl_receive0");
}

void Java_java_net_TwoStacksPlainDatagramSocketImpl_send() {
    unlinked("Java_java_net_TwoStacksPlainDatagramSocketImpl_send");
}

void Java_java_net_TwoStacksPlainDatagramSocketImpl_setTTL() {
    unlinked("Java_java_net_TwoStacksPlainDatagramSocketImpl_setTTL");
}

void Java_java_net_TwoStacksPlainDatagramSocketImpl_setTimeToLive() {
    unlinked("Java_java_net_TwoStacksPlainDatagramSocketImpl_setTimeToLive");
}

void Java_java_net_TwoStacksPlainDatagramSocketImpl_socketGetOption() {
    unlinked("Java_java_net_TwoStacksPlainDatagramSocketImpl_socketGetOption");
}

void Java_java_net_TwoStacksPlainDatagramSocketImpl_socketSetOption() {
    unlinked("Java_java_net_TwoStacksPlainDatagramSocketImpl_socketSetOption");
}

void Java_java_net_TwoStacksPlainSocketImpl_initProto() {
    unlinked("Java_java_net_TwoStacksPlainSocketImpl_initProto");
}

void Java_java_net_TwoStacksPlainSocketImpl_socketAccept() {
    unlinked("Java_java_net_TwoStacksPlainSocketImpl_socketAccept");
}

void Java_java_net_TwoStacksPlainSocketImpl_socketAvailable() {
    unlinked("Java_java_net_TwoStacksPlainSocketImpl_socketAvailable");
}

void Java_java_net_TwoStacksPlainSocketImpl_socketBind() {
    unlinked("Java_java_net_TwoStacksPlainSocketImpl_socketBind");
}

void Java_java_net_TwoStacksPlainSocketImpl_socketClose0() {
    unlinked("Java_java_net_TwoStacksPlainSocketImpl_socketClose0");
}

void Java_java_net_TwoStacksPlainSocketImpl_socketConnect() {
    unlinked("Java_java_net_TwoStacksPlainSocketImpl_socketConnect");
}

void Java_java_net_TwoStacksPlainSocketImpl_socketCreate() {
    unlinked("Java_java_net_TwoStacksPlainSocketImpl_socketCreate");
}

void Java_java_net_TwoStacksPlainSocketImpl_socketGetOption() {
    unlinked("Java_java_net_TwoStacksPlainSocketImpl_socketGetOption");
}

void Java_java_net_TwoStacksPlainSocketImpl_socketListen() {
    unlinked("Java_java_net_TwoStacksPlainSocketImpl_socketListen");
}

void Java_java_net_TwoStacksPlainSocketImpl_socketSendUrgentData() {
    unlinked("Java_java_net_TwoStacksPlainSocketImpl_socketSendUrgentData");
}

void Java_java_net_TwoStacksPlainSocketImpl_socketSetOption() {
    unlinked("Java_java_net_TwoStacksPlainSocketImpl_socketSetOption");
}

void Java_java_net_TwoStacksPlainSocketImpl_socketShutdown() {
    unlinked("Java_java_net_TwoStacksPlainSocketImpl_socketShutdown");
}

void Java_java_util_ResourceBundle_getClassContext() {
    unlinked("Java_java_util_ResourceBundle_getClassContext");
}

void Java_java_util_zip_Inflater_getBytesRead() {
    unlinked("Java_java_util_zip_Inflater_getBytesRead");
}

void Java_java_util_zip_Inflater_getBytesWritten() {
    unlinked("Java_java_util_zip_Inflater_getBytesWritten");
}

void Java_sun_io_Win32ErrorMode_setErrorMode() {
    unlinked("Java_sun_io_Win32ErrorMode_setErrorMode");
}

void Java_sun_misc_Perf_attach() {
    unlinked("Java_sun_misc_Perf_attach");
}

void Java_sun_misc_Perf_createByteArray() {
    unlinked("Java_sun_misc_Perf_createByteArray");
}

void Java_sun_misc_Perf_createLong() {
    unlinked("Java_sun_misc_Perf_createLong");
}

void Java_sun_misc_Perf_detach() {
    unlinked("Java_sun_misc_Perf_detach");
}

void Java_sun_misc_Perf_highResCounter() {
    unlinked("Java_sun_misc_Perf_highResCounter");
}

void Java_sun_misc_Perf_highResFrequency() {
    unlinked("Java_sun_misc_Perf_highResFrequency");
}

void Java_sun_misc_Perf_registerNatives() {
    unlinked("Java_sun_misc_Perf_registerNatives");
}

void Java_sun_misc_Unsafe_addressSize() {
    unlinked("Java_sun_misc_Unsafe_addressSize");
}

void Java_sun_misc_Unsafe_allocateInstance() {
    unlinked("Java_sun_misc_Unsafe_allocateInstance");
}

void Java_sun_misc_Unsafe_allocateMemory() {
    unlinked("Java_sun_misc_Unsafe_allocateMemory");
}

void Java_sun_misc_Unsafe_arrayBaseOffset() {
    unlinked("Java_sun_misc_Unsafe_arrayBaseOffset");
}

void Java_sun_misc_Unsafe_arrayIndexScale() {
    unlinked("Java_sun_misc_Unsafe_arrayIndexScale");
}

void Java_sun_misc_Unsafe_compareAndSwapInt() {
    unlinked("Java_sun_misc_Unsafe_compareAndSwapInt");
}

void Java_sun_misc_Unsafe_compareAndSwapLong() {
    unlinked("Java_sun_misc_Unsafe_compareAndSwapLong");
}

void Java_sun_misc_Unsafe_compareAndSwapObject() {
    unlinked("Java_sun_misc_Unsafe_compareAndSwapObject");
}

void Java_sun_misc_Unsafe_copyMemory() {
    unlinked("Java_sun_misc_Unsafe_copyMemory");
}

void Java_sun_misc_Unsafe_defineAnonymousClass() {
    unlinked("Java_sun_misc_Unsafe_defineAnonymousClass");
}

void Java_sun_misc_Unsafe_defineClass__Ljava_lang_String_2_3BII() {
    unlinked("Java_sun_misc_Unsafe_defineClass__Ljava_lang_String_2_3BII");
}

void Java_sun_misc_Unsafe_defineClass__Ljava_lang_String_2_3BIILjava_lang_ClassLoader_2Ljava_security_ProtectionDomain_2() {
    unlinked("Java_sun_misc_Unsafe_defineClass__Ljava_lang_String_2_3BIILjava_lang_ClassLoader_2Ljava_security_ProtectionDomain_2");
}

void Java_sun_misc_Unsafe_ensureClassInitialized() {
    unlinked("Java_sun_misc_Unsafe_ensureClassInitialized");
}

void Java_sun_misc_Unsafe_freeMemory() {
    unlinked("Java_sun_misc_Unsafe_freeMemory");
}

void Java_sun_misc_Unsafe_getAddress() {
    unlinked("Java_sun_misc_Unsafe_getAddress");
}

void Java_sun_misc_Unsafe_getBoolean() {
    unlinked("Java_sun_misc_Unsafe_getBoolean");
}

void Java_sun_misc_Unsafe_getBooleanVolatile() {
    unlinked("Java_sun_misc_Unsafe_getBooleanVolatile");
}

void Java_sun_misc_Unsafe_getByteVolatile() {
    unlinked("Java_sun_misc_Unsafe_getByteVolatile");
}

void Java_sun_misc_Unsafe_getByte__J() {
    unlinked("Java_sun_misc_Unsafe_getByte__J");
}

void Java_sun_misc_Unsafe_getByte__Ljava_lang_Object_2J() {
    unlinked("Java_sun_misc_Unsafe_getByte__Ljava_lang_Object_2J");
}

void Java_sun_misc_Unsafe_getCharVolatile() {
    unlinked("Java_sun_misc_Unsafe_getCharVolatile");
}

void Java_sun_misc_Unsafe_getChar__J() {
    unlinked("Java_sun_misc_Unsafe_getChar__J");
}

void Java_sun_misc_Unsafe_getChar__Ljava_lang_Object_2J() {
    unlinked("Java_sun_misc_Unsafe_getChar__Ljava_lang_Object_2J");
}

void Java_sun_misc_Unsafe_getDoubleVolatile() {
    unlinked("Java_sun_misc_Unsafe_getDoubleVolatile");
}

void Java_sun_misc_Unsafe_getDouble__J() {
    unlinked("Java_sun_misc_Unsafe_getDouble__J");
}

void Java_sun_misc_Unsafe_getDouble__Ljava_lang_Object_2J() {
    unlinked("Java_sun_misc_Unsafe_getDouble__Ljava_lang_Object_2J");
}

void Java_sun_misc_Unsafe_getFloatVolatile() {
    unlinked("Java_sun_misc_Unsafe_getFloatVolatile");
}

void Java_sun_misc_Unsafe_getFloat__J() {
    unlinked("Java_sun_misc_Unsafe_getFloat__J");
}

void Java_sun_misc_Unsafe_getFloat__Ljava_lang_Object_2J() {
    unlinked("Java_sun_misc_Unsafe_getFloat__Ljava_lang_Object_2J");
}

void Java_sun_misc_Unsafe_getIntVolatile() {
    unlinked("Java_sun_misc_Unsafe_getIntVolatile");
}

void Java_sun_misc_Unsafe_getInt__J() {
    unlinked("Java_sun_misc_Unsafe_getInt__J");
}

void Java_sun_misc_Unsafe_getInt__Ljava_lang_Object_2J() {
    unlinked("Java_sun_misc_Unsafe_getInt__Ljava_lang_Object_2J");
}

void Java_sun_misc_Unsafe_getLoadAverage() {
    unlinked("Java_sun_misc_Unsafe_getLoadAverage");
}

void Java_sun_misc_Unsafe_getLongVolatile() {
    unlinked("Java_sun_misc_Unsafe_getLongVolatile");
}

void Java_sun_misc_Unsafe_getLong__J() {
    unlinked("Java_sun_misc_Unsafe_getLong__J");
}

void Java_sun_misc_Unsafe_getLong__Ljava_lang_Object_2J() {
    unlinked("Java_sun_misc_Unsafe_getLong__Ljava_lang_Object_2J");
}

void Java_sun_misc_Unsafe_getObject() {
    unlinked("Java_sun_misc_Unsafe_getObject");
}

void Java_sun_misc_Unsafe_getObjectVolatile() {
    unlinked("Java_sun_misc_Unsafe_getObjectVolatile");
}

void Java_sun_misc_Unsafe_getShortVolatile() {
    unlinked("Java_sun_misc_Unsafe_getShortVolatile");
}

void Java_sun_misc_Unsafe_getShort__J() {
    unlinked("Java_sun_misc_Unsafe_getShort__J");
}

void Java_sun_misc_Unsafe_getShort__Ljava_lang_Object_2J() {
    unlinked("Java_sun_misc_Unsafe_getShort__Ljava_lang_Object_2J");
}

void Java_sun_misc_Unsafe_monitorEnter() {
    unlinked("Java_sun_misc_Unsafe_monitorEnter");
}

void Java_sun_misc_Unsafe_monitorExit() {
    unlinked("Java_sun_misc_Unsafe_monitorExit");
}

void Java_sun_misc_Unsafe_objectFieldOffset() {
    unlinked("Java_sun_misc_Unsafe_objectFieldOffset");
}

void Java_sun_misc_Unsafe_pageSize() {
    unlinked("Java_sun_misc_Unsafe_pageSize");
}

void Java_sun_misc_Unsafe_park() {
    unlinked("Java_sun_misc_Unsafe_park");
}

void Java_sun_misc_Unsafe_putAddress() {
    unlinked("Java_sun_misc_Unsafe_putAddress");
}

void Java_sun_misc_Unsafe_putBoolean() {
    unlinked("Java_sun_misc_Unsafe_putBoolean");
}

void Java_sun_misc_Unsafe_putBooleanVolatile() {
    unlinked("Java_sun_misc_Unsafe_putBooleanVolatile");
}

void Java_sun_misc_Unsafe_putByteVolatile() {
    unlinked("Java_sun_misc_Unsafe_putByteVolatile");
}

void Java_sun_misc_Unsafe_putByte__JB() {
    unlinked("Java_sun_misc_Unsafe_putByte__JB");
}

void Java_sun_misc_Unsafe_putByte__Ljava_lang_Object_2JB() {
    unlinked("Java_sun_misc_Unsafe_putByte__Ljava_lang_Object_2JB");
}

void Java_sun_misc_Unsafe_putCharVolatile() {
    unlinked("Java_sun_misc_Unsafe_putCharVolatile");
}

void Java_sun_misc_Unsafe_putChar__JC() {
    unlinked("Java_sun_misc_Unsafe_putChar__JC");
}

void Java_sun_misc_Unsafe_putChar__Ljava_lang_Object_2JC() {
    unlinked("Java_sun_misc_Unsafe_putChar__Ljava_lang_Object_2JC");
}

void Java_sun_misc_Unsafe_putDoubleVolatile() {
    unlinked("Java_sun_misc_Unsafe_putDoubleVolatile");
}

void Java_sun_misc_Unsafe_putDouble__JD() {
    unlinked("Java_sun_misc_Unsafe_putDouble__JD");
}

void Java_sun_misc_Unsafe_putDouble__Ljava_lang_Object_2JD() {
    unlinked("Java_sun_misc_Unsafe_putDouble__Ljava_lang_Object_2JD");
}

void Java_sun_misc_Unsafe_putFloatVolatile() {
    unlinked("Java_sun_misc_Unsafe_putFloatVolatile");
}

void Java_sun_misc_Unsafe_putFloat__JF() {
    unlinked("Java_sun_misc_Unsafe_putFloat__JF");
}

void Java_sun_misc_Unsafe_putFloat__Ljava_lang_Object_2JF() {
    unlinked("Java_sun_misc_Unsafe_putFloat__Ljava_lang_Object_2JF");
}

void Java_sun_misc_Unsafe_putIntVolatile() {
    unlinked("Java_sun_misc_Unsafe_putIntVolatile");
}

void Java_sun_misc_Unsafe_putInt__JI() {
    unlinked("Java_sun_misc_Unsafe_putInt__JI");
}

void Java_sun_misc_Unsafe_putInt__Ljava_lang_Object_2JI() {
    unlinked("Java_sun_misc_Unsafe_putInt__Ljava_lang_Object_2JI");
}

void Java_sun_misc_Unsafe_putLongVolatile() {
    unlinked("Java_sun_misc_Unsafe_putLongVolatile");
}

void Java_sun_misc_Unsafe_putLong__JJ() {
    unlinked("Java_sun_misc_Unsafe_putLong__JJ");
}

void Java_sun_misc_Unsafe_putLong__Ljava_lang_Object_2JJ() {
    unlinked("Java_sun_misc_Unsafe_putLong__Ljava_lang_Object_2JJ");
}

void Java_sun_misc_Unsafe_putObject() {
    unlinked("Java_sun_misc_Unsafe_putObject");
}

void Java_sun_misc_Unsafe_putObjectVolatile() {
    unlinked("Java_sun_misc_Unsafe_putObjectVolatile");
}

void Java_sun_misc_Unsafe_putOrderedInt() {
    unlinked("Java_sun_misc_Unsafe_putOrderedInt");
}

void Java_sun_misc_Unsafe_putOrderedLong() {
    unlinked("Java_sun_misc_Unsafe_putOrderedLong");
}

void Java_sun_misc_Unsafe_putOrderedObject() {
    unlinked("Java_sun_misc_Unsafe_putOrderedObject");
}

void Java_sun_misc_Unsafe_putShortVolatile() {
    unlinked("Java_sun_misc_Unsafe_putShortVolatile");
}

void Java_sun_misc_Unsafe_putShort__JS() {
    unlinked("Java_sun_misc_Unsafe_putShort__JS");
}

void Java_sun_misc_Unsafe_putShort__Ljava_lang_Object_2JS() {
    unlinked("Java_sun_misc_Unsafe_putShort__Ljava_lang_Object_2JS");
}

void Java_sun_misc_Unsafe_reallocateMemory() {
    unlinked("Java_sun_misc_Unsafe_reallocateMemory");
}

void Java_sun_misc_Unsafe_registerNatives() {
    unlinked("Java_sun_misc_Unsafe_registerNatives");
}

void Java_sun_misc_Unsafe_setMemory() {
    unlinked("Java_sun_misc_Unsafe_setMemory");
}

void Java_sun_misc_Unsafe_staticFieldBase() {
    unlinked("Java_sun_misc_Unsafe_staticFieldBase");
}

void Java_sun_misc_Unsafe_staticFieldOffset() {
    unlinked("Java_sun_misc_Unsafe_staticFieldOffset");
}

void Java_sun_misc_Unsafe_throwException() {
    unlinked("Java_sun_misc_Unsafe_throwException");
}

void Java_sun_misc_Unsafe_tryMonitorEnter() {
    unlinked("Java_sun_misc_Unsafe_tryMonitorEnter");
}

void Java_sun_misc_Unsafe_unpark() {
    unlinked("Java_sun_misc_Unsafe_unpark");
}

void Java_sun_net_dns_ResolverConfigurationImpl_init0() {
    unlinked("Java_sun_net_dns_ResolverConfigurationImpl_init0");
}

void Java_sun_net_dns_ResolverConfigurationImpl_loadDNSconfig0() {
    unlinked("Java_sun_net_dns_ResolverConfigurationImpl_loadDNSconfig0");
}

void Java_sun_net_dns_ResolverConfigurationImpl_notifyAddrChange0() {
    unlinked("Java_sun_net_dns_ResolverConfigurationImpl_notifyAddrChange0");
}

void Java_sun_nio_ch_FileDispatcherImpl_duplicateHandle() {
    unlinked("Java_sun_nio_ch_FileDispatcherImpl_duplicateHandle");
}

void Java_sun_nio_ch_Iocp_close0() {
    unlinked("Java_sun_nio_ch_Iocp_close0");
}

void Java_sun_nio_ch_Iocp_createIoCompletionPort() {
    unlinked("Java_sun_nio_ch_Iocp_createIoCompletionPort");
}

void Java_sun_nio_ch_Iocp_getErrorMessage() {
    unlinked("Java_sun_nio_ch_Iocp_getErrorMessage");
}

void Java_sun_nio_ch_Iocp_getQueuedCompletionStatus() {
    unlinked("Java_sun_nio_ch_Iocp_getQueuedCompletionStatus");
}

void Java_sun_nio_ch_Iocp_initIDs() {
    unlinked("Java_sun_nio_ch_Iocp_initIDs");
}

void Java_sun_nio_ch_Iocp_postQueuedCompletionStatus() {
    unlinked("Java_sun_nio_ch_Iocp_postQueuedCompletionStatus");
}

void Java_sun_nio_ch_Net_remoteInetAddress() {
    unlinked("Java_sun_nio_ch_Net_remoteInetAddress");
}

void Java_sun_nio_ch_Net_remotePort() {
    unlinked("Java_sun_nio_ch_Net_remotePort");
}

void Java_sun_nio_ch_SocketDispatcher_close0() {
    unlinked("Java_sun_nio_ch_SocketDispatcher_close0");
}

void Java_sun_nio_ch_SocketDispatcher_preClose0() {
    unlinked("Java_sun_nio_ch_SocketDispatcher_preClose0");
}

void Java_sun_nio_ch_SocketDispatcher_read0() {
    unlinked("Java_sun_nio_ch_SocketDispatcher_read0");
}

void Java_sun_nio_ch_SocketDispatcher_readv0() {
    unlinked("Java_sun_nio_ch_SocketDispatcher_readv0");
}

void Java_sun_nio_ch_SocketDispatcher_write0() {
    unlinked("Java_sun_nio_ch_SocketDispatcher_write0");
}

void Java_sun_nio_ch_SocketDispatcher_writev0() {
    unlinked("Java_sun_nio_ch_SocketDispatcher_writev0");
}

void Java_sun_nio_ch_WindowsAsynchronousFileChannelImpl_close0() {
    unlinked("Java_sun_nio_ch_WindowsAsynchronousFileChannelImpl_close0");
}

void Java_sun_nio_ch_WindowsAsynchronousFileChannelImpl_lockFile() {
    unlinked("Java_sun_nio_ch_WindowsAsynchronousFileChannelImpl_lockFile");
}

void Java_sun_nio_ch_WindowsAsynchronousFileChannelImpl_readFile() {
    unlinked("Java_sun_nio_ch_WindowsAsynchronousFileChannelImpl_readFile");
}

void Java_sun_nio_ch_WindowsAsynchronousFileChannelImpl_writeFile() {
    unlinked("Java_sun_nio_ch_WindowsAsynchronousFileChannelImpl_writeFile");
}

void Java_sun_nio_ch_WindowsAsynchronousServerSocketChannelImpl_accept0() {
    unlinked("Java_sun_nio_ch_WindowsAsynchronousServerSocketChannelImpl_accept0");
}

void Java_sun_nio_ch_WindowsAsynchronousServerSocketChannelImpl_closesocket0() {
    unlinked("Java_sun_nio_ch_WindowsAsynchronousServerSocketChannelImpl_closesocket0");
}

void Java_sun_nio_ch_WindowsAsynchronousServerSocketChannelImpl_initIDs() {
    unlinked("Java_sun_nio_ch_WindowsAsynchronousServerSocketChannelImpl_initIDs");
}

void Java_sun_nio_ch_WindowsAsynchronousServerSocketChannelImpl_updateAcceptContext() {
    unlinked("Java_sun_nio_ch_WindowsAsynchronousServerSocketChannelImpl_updateAcceptContext");
}

void Java_sun_nio_ch_WindowsAsynchronousSocketChannelImpl_closesocket0() {
    unlinked("Java_sun_nio_ch_WindowsAsynchronousSocketChannelImpl_closesocket0");
}

void Java_sun_nio_ch_WindowsAsynchronousSocketChannelImpl_connect0() {
    unlinked("Java_sun_nio_ch_WindowsAsynchronousSocketChannelImpl_connect0");
}

void Java_sun_nio_ch_WindowsAsynchronousSocketChannelImpl_initIDs() {
    unlinked("Java_sun_nio_ch_WindowsAsynchronousSocketChannelImpl_initIDs");
}

void Java_sun_nio_ch_WindowsAsynchronousSocketChannelImpl_read0() {
    unlinked("Java_sun_nio_ch_WindowsAsynchronousSocketChannelImpl_read0");
}

void Java_sun_nio_ch_WindowsAsynchronousSocketChannelImpl_updateConnectContext() {
    unlinked("Java_sun_nio_ch_WindowsAsynchronousSocketChannelImpl_updateConnectContext");
}

void Java_sun_nio_ch_WindowsAsynchronousSocketChannelImpl_write0() {
    unlinked("Java_sun_nio_ch_WindowsAsynchronousSocketChannelImpl_write0");
}

void Java_sun_nio_ch_WindowsSelectorImpl_SubSelector_poll0() {
    unlinked("Java_sun_nio_ch_WindowsSelectorImpl_SubSelector_poll0");
}

void Java_sun_nio_ch_WindowsSelectorImpl_discardUrgentData() {
    unlinked("Java_sun_nio_ch_WindowsSelectorImpl_discardUrgentData");
}

void Java_sun_nio_ch_WindowsSelectorImpl_resetWakeupSocket0() {
    unlinked("Java_sun_nio_ch_WindowsSelectorImpl_resetWakeupSocket0");
}

void Java_sun_nio_ch_WindowsSelectorImpl_setWakeupSocket0() {
    unlinked("Java_sun_nio_ch_WindowsSelectorImpl_setWakeupSocket0");
}

void Java_sun_nio_fs_RegistryFileTypeDetector_queryStringValue() {
    unlinked("Java_sun_nio_fs_RegistryFileTypeDetector_queryStringValue");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_AddAccessAllowedAceEx() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_AddAccessAllowedAceEx");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_AddAccessDeniedAceEx() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_AddAccessDeniedAceEx");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_AdjustTokenPrivileges() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_AdjustTokenPrivileges");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_BackupRead0() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_BackupRead0");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_BackupSeek() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_BackupSeek");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_BuildTrusteeWithSid() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_BuildTrusteeWithSid");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_CloseHandle() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_CloseHandle");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_ConvertSidToStringSid() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_ConvertSidToStringSid");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_ConvertStringSidToSid0() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_ConvertStringSidToSid0");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_CopyFileEx0() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_CopyFileEx0");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_CreateDirectory0() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_CreateDirectory0");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_CreateFile0() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_CreateFile0");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_CreateHardLink0() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_CreateHardLink0");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_CreateIoCompletionPort() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_CreateIoCompletionPort");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_CreateSymbolicLink0() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_CreateSymbolicLink0");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_DeleteFile0() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_DeleteFile0");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_DeviceIoControlGetReparsePoint() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_DeviceIoControlGetReparsePoint");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_DeviceIoControlSetSparse() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_DeviceIoControlSetSparse");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_DuplicateTokenEx() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_DuplicateTokenEx");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_FindClose() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_FindClose");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_FindFirstFile0() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_FindFirstFile0");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_FindFirstFile1() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_FindFirstFile1");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_FindFirstStream0() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_FindFirstStream0");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_FindNextFile() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_FindNextFile");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_FindNextStream() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_FindNextStream");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_FormatMessage() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_FormatMessage");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_GetAce() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_GetAce");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_GetAclInformation0() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_GetAclInformation0");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_GetCurrentProcess() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_GetCurrentProcess");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_GetCurrentThread() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_GetCurrentThread");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_GetDiskFreeSpaceEx0() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_GetDiskFreeSpaceEx0");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_GetDriveType0() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_GetDriveType0");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_GetEffectiveRightsFromAcl() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_GetEffectiveRightsFromAcl");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_GetFileAttributes0() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_GetFileAttributes0");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_GetFileAttributesEx0() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_GetFileAttributesEx0");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_GetFileInformationByHandle() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_GetFileInformationByHandle");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_GetFileSecurity0() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_GetFileSecurity0");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_GetFinalPathNameByHandle() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_GetFinalPathNameByHandle");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_GetFullPathName0() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_GetFullPathName0");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_GetLengthSid() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_GetLengthSid");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_GetLogicalDrives() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_GetLogicalDrives");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_GetQueuedCompletionStatus0() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_GetQueuedCompletionStatus0");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_GetSecurityDescriptorDacl() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_GetSecurityDescriptorDacl");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_GetSecurityDescriptorOwner() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_GetSecurityDescriptorOwner");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_GetTokenInformation() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_GetTokenInformation");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_GetVolumeInformation0() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_GetVolumeInformation0");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_GetVolumePathName0() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_GetVolumePathName0");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_InitializeAcl() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_InitializeAcl");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_InitializeSecurityDescriptor() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_InitializeSecurityDescriptor");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_LocalFree() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_LocalFree");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_LookupAccountName0() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_LookupAccountName0");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_LookupAccountSid0() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_LookupAccountSid0");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_LookupPrivilegeValue0() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_LookupPrivilegeValue0");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_MoveFileEx0() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_MoveFileEx0");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_OpenProcessToken() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_OpenProcessToken");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_OpenThreadToken() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_OpenThreadToken");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_PostQueuedCompletionStatus() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_PostQueuedCompletionStatus");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_ReadDirectoryChangesW() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_ReadDirectoryChangesW");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_RemoveDirectory0() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_RemoveDirectory0");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_SetEndOfFile() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_SetEndOfFile");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_SetFileAttributes0() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_SetFileAttributes0");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_SetFileSecurity0() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_SetFileSecurity0");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_SetFileTime() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_SetFileTime");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_SetSecurityDescriptorDacl() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_SetSecurityDescriptorDacl");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_SetSecurityDescriptorOwner() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_SetSecurityDescriptorOwner");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_SetThreadToken() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_SetThreadToken");
}

void Java_sun_nio_fs_WindowsNativeDispatcher_initIDs() {
    unlinked("Java_sun_nio_fs_WindowsNativeDispatcher_initIDs");
}

void Java_sun_security_krb5_Config_getWindowsDirectory() {
    unlinked("Java_sun_security_krb5_Config_getWindowsDirectory");
}

void Java_sun_security_provider_NativeSeedGenerator_nativeGenerateSeed() {
    unlinked("Java_sun_security_provider_NativeSeedGenerator_nativeGenerateSeed");
}

} // extern "C"
