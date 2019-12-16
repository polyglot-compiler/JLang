//Copyright (C) 2018 Cornell University

package jlang.extension;

import jlang.types.JLangParsedClassType_c;
import org.bytedeco.javacpp.LLVM;
import org.bytedeco.javacpp.LLVM.*;

import jlang.ast.JLangExt;
import jlang.util.Constants;
import jlang.visit.LLVMTranslator;
import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.SerialVersionUID;

import java.lang.Override;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static jlang.extension.JLangSynchronizedExt.buildMonitorFunc;
import static jlang.extension.JLangSynchronizedExt.buildMonitorFuncWithGlobalMutex;
import static jlang.util.Constants.REGISTER_CLASS_FUNC;
import static jlang.util.Constants.RUNTIME_ARRAY;
import static org.bytedeco.javacpp.LLVM.*;

public class JLangClassDeclExt extends JLangExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        ClassDecl n = (ClassDecl) node();
        ParsedClassType ct = n.type();
        initClassDataStructures(v, ct, n.body());
        return super.leaveTranslateLLVM(v);
    }

    private static LLVMValueRef getTypePointer(LLVMTranslator v, Type t) {
        Type erasedType = v.utils.erasureLL(t);
        if (erasedType.isPrimitive()) {
            LLVMValueRef primPtr = v.utils.getGlobal("Polyglot_native_"+erasedType.toString(), v.utils.toLL(v.ts.Class()));
            return v.utils.buildCastToBytePtr(primPtr);
        } else if (erasedType.equalsImpl(v.ts.ArrayObject())) {
            return LLVMConstNull(v.utils.i8Ptr());
        } else {
            return v.utils.buildCastToBytePtr(v.utils.getClassObjectGlobal(erasedType.toClass()));
        }
    }

    /**
     * Builds a function that runs static initializers, and
     * creates the java.lang.Class object representing this class.
     *
     * Note that this serves the same role as class loading in the JVM.
     * Whereas the JVM reads a .class file to build backing data structures
     * for this class, we statically emit these data structures in LLVM IR.
     */
    public static void buildClassLoadingFunc(LLVMTranslator v, ClassType ct, ClassBody cb) {

//        System.out.println("building class loading function for "+ct);

        // Declare class object.
        LLVMTypeRef classType = v.utils.toLL(v.ts.Class());
        LLVMValueRef classObjectGlobal = v.utils.getClassObjectGlobal(ct);
        LLVMSetInitializer(classObjectGlobal, LLVMConstNull(classType));

        // Instance field info.
        LLVMTypeRef fieldInfoType = v.utils.structType(
                v.utils.i8Ptr(), // char* name
                v.utils.i32(),    // int32_t offset
                v.utils.i32(),    // int32_t modifiers
                v.utils.i8Ptr(), // type_ptr*
                v.utils.i8Ptr() // char* sig
        );
        LLVMValueRef[] fieldInfoElems = ct.fields().stream().filter(fi -> !fi.flags().isStatic())
            	.map(fi -> {
            		LLVMValueRef name = v.utils.buildGlobalCStr(fi.name());
            		LLVMValueRef nullPtr = LLVMConstNull(v.utils.toLL(ct));
            		LLVMValueRef gep = v.obj.buildFieldElementPtr(nullPtr, fi);
            		LLVMValueRef offset = LLVMConstPtrToInt(gep, v.utils.i32());
                    LLVMValueRef modifiers = LLVMConstInt(v.utils.i32(), fi.flags().toModifiers(), 1);
                    LLVMValueRef typeClass = getTypePointer(v, fi.type());
                    LLVMValueRef signature = v.utils.buildGlobalCStr(v.mangler.jniUnescapedSignature(fi.type()));
                    return v.utils.buildConstStruct(name, offset, modifiers, typeClass, signature);
            	})
            	.toArray(LLVMValueRef[]::new);

        // Static field info.
        LLVMTypeRef staticFieldType = v.utils.structType(
        		v.utils.i8Ptr(), // char* name
        		v.utils.i8Ptr(), // char* sig (field type)
        		v.utils.i8Ptr(), //  void* to field, stored as global var
                v.utils.i32(),   // int32_t modifiers
                v.utils.i8Ptr() // type_ptr*
        );

        LLVMValueRef[] staticFieldElems = ct.fields().stream().filter(fi -> fi.flags().isStatic())
            	.map(fi -> {
            		LLVMValueRef name = v.utils.buildGlobalCStr(fi.name());
            		LLVMValueRef signature = v.utils.buildGlobalCStr(v.mangler.jniUnescapedSignature(fi.type()));
            		LLVMValueRef ptr = v.utils.getStaticField(fi);
            		LLVMValueRef staticPtr = LLVMConstBitCast(ptr, v.utils.i8Ptr());
                    LLVMValueRef modifiers = LLVMConstInt(v.utils.i32(), fi.flags().toModifiers(), 1);
                    LLVMValueRef typeClass = getTypePointer(v, fi.type());
            		return v.utils.buildConstStruct(name, signature, staticPtr, modifiers, typeClass);
            	})
            	.toArray(LLVMValueRef[]::new);

        // Method info. Includes constructors. Does not include inherited methods.
        LLVMTypeRef methodInfoType = v.utils.structType(
                v.utils.i8Ptr(), // char* name
                v.utils.i8Ptr(), // char* signature
                v.utils.i32(),   // int32_t offset into dispatch vector
                v.utils.i8Ptr(), // void* function pointer
                v.utils.i8Ptr() , // void* trampoline pointer
                v.utils.i8Ptr(),  // void* for interface methods, the interface method id.
                v.utils.i32(),     // int32_t A precomputed hash of the intf_id.
                v.utils.i32(),      // modifiers
                v.utils.i8Ptr(),     // return type
                v.utils.i32(),      // number of arg types
                v.utils.ptrTypeRef(v.utils.i8Ptr())     // argTypes void**
        );
        LLVMValueRef[] methodInfoElems = Stream.concat(
                ct.methods().stream(), ct.constructors().stream())
                .map(pi -> buildMethodInfo(v, ct, pi))
                .toArray(LLVMValueRef[]::new);

        //Info about implemented interfaces. Needed by runtime reflection
        LLVMValueRef[] interfaceInfoElems = v.allInterfaces(ct).stream()
        		.map(intf -> v.utils.getClassObjectGlobal(intf))
        		.toArray(LLVMValueRef[]::new);

        // This layout must precisely mirror the layout defined in the runtime (class.cpp).
        LLVMValueRef classInfo = v.utils.buildConstStruct(

                // Class name, char*
                v.utils.buildGlobalCStr(v.mangler.userVisibleClassName(ct)),

                // Super class pointer, jclass*
                ct.superType() != null
                        ? v.utils.getClassObjectGlobal(ct.superType().toClass())
                        : LLVMConstNull(v.utils.ptrTypeRef(classType)),

                // Dispatch Vector pointer (NULL if not an instantiatable class), void*
                ct.flags().isInterface() || ct.flags().isAbstract() ?
                		LLVMConstNull(v.utils.i8Ptr()) :
                		v.utils.buildCastToBytePtr(v.dv.getDispatchVectorFor(ct)),

                // Object size (Base Object Rep + all fields) in bytes, i64
                v.obj.sizeOf(ct),

                // Boolean isInterface, jboolean (i8)
                LLVMConstInt(v.utils.i8(), ct.flags().isInterface() ? 1 : 0, /*sign-extend*/ 0),

                // Number of implemented interfaces, i32
                LLVMConstInt(v.utils.i32(), interfaceInfoElems.length, /*sign-extend*/ 0),

                // Implemented Interface Pointers, jclass**
                v.utils.buildGlobalConstArrayAsPtr(v.utils.ptrTypeRef(classType),
                		interfaceInfoElems),

                // Number of instance fields, i32
                LLVMConstInt(v.utils.i32(), fieldInfoElems.length, /*sign-extend*/ 0),

                // Instance fields, { char* name, int32_t offset }
                v.utils.buildGlobalArrayAsPtr(fieldInfoType, fieldInfoElems),

                // Number of static fields, i32
                LLVMConstInt(v.utils.i32(), staticFieldElems.length, /*sign-extend*/ 0),

                // Static fields, { char* name, void* ptr }
                v.utils.buildGlobalArrayAsPtr(staticFieldType, staticFieldElems),

                // Number of methods, i32
                LLVMConstInt(v.utils.i32(), methodInfoElems.length, /*sign-extend*/ 0),

                // Methods, { char* name, char* sig, int32_t offset, void* fnPtr, void* trampoline }
                v.utils.buildGlobalArrayAsPtr(methodInfoType, methodInfoElems)


        );

        // Emit class info as a global variable.
        String classInfoMangled = v.mangler.classInfoGlobal(ct);
        LLVMValueRef classInfoGlobal = v.utils.getGlobal(classInfoMangled, LLVMTypeOf(classInfo));
        LLVMSetInitializer(classInfoGlobal, classInfo);
        LLVMSetGlobalConstant(classInfoGlobal, 1);
        LLVMSetLinkage(classInfoGlobal, LLVMPrivateLinkage);

        // Begin class loading function.
        String funcName = v.mangler.classLoadingFunc(ct);
        String debugName = "load_" + ct.fullName();

        Runnable buildBody = () -> {
            // Synchronize the class loading function.
            buildMonitorFuncWithGlobalMutex(v, Constants.MONITOR_ENTER);

            // Allocate and store a new java.lang.Class instance.
            // Note that we do not call any constructors for the allocated class objects.
            LLVMValueRef calloc = LLVMGetNamedFunction(v.mod, Constants.CALLOC);
            LLVMValueRef memory = v.utils.buildFunCall(calloc, v.obj.sizeOf(v.ts.Class()));
            LLVMValueRef clazz = LLVMBuildBitCast(v.builder, memory, classType, "cast");
            LLVMValueRef classGlobal = v.utils.getGlobal(v.mangler.classObj(ct), classType);
            LLVMBuildStore(v.builder, clazz, classGlobal);

            // Set the dispatch vector of the class object.
            LLVMValueRef dvGep = v.obj.buildDispatchVectorElementPtr(clazz, v.ts.Class());
            LLVMValueRef dvGlobal = v.dv.getDispatchVectorFor(v.ts.Class());
            LLVMBuildStore(v.builder, dvGlobal, dvGep);

            // Load super class if necessary.
            // Technically interfaces do not need to initialize their
            // super classes (per the JLS), but we do so anyway for simplicity.
            if (ct.superType() != null) {
                v.utils.buildClassLoadCheck(ct.superType().toClass());
            }

            // We actually also need to load interfaces to
            // Since we need their information registers for runtime interface calls.
            for (ClassType intf : v.allInterfaces(ct)) {
            	v.utils.buildClassLoadCheck(intf);
            }

            // Call into runtime to register this class.
            LLVMTypeRef regClassFuncType = v.utils.functionType(
                    v.utils.voidType(), classType, v.utils.ptrTypeRef(LLVMTypeOf(classInfo)));
            LLVMValueRef regClass = v.utils.getFunction(REGISTER_CLASS_FUNC, regClassFuncType);
            v.utils.buildProcCall(regClass, clazz, classInfoGlobal);

            // Run static initializers.
            for (ClassMember m : cb.members()) {

                // Run static field initializers.
                if (m instanceof FieldDecl) {
                    FieldDecl fd = (FieldDecl) m;
                    FieldInstance fi = fd.fieldInstance();
                    if (fi.flags().isStatic() && fd.init() != null) {
                        LLVMValueRef var = v.utils.getStaticField(fi);
                        fd.visitChild(fd.init(), v);
                        LLVMValueRef val = v.getTranslation(fd.init());
                        LLVMBuildStore(v.builder, val, var);
                    }
                }

                // Run static initializer blocks.
                if (m instanceof Initializer) {
                    Initializer init = (Initializer) m;
                    if (init.flags().isStatic()) {
                        init.visitChild(init.body(), v);
                    }
                }
            }

            buildMonitorFuncWithGlobalMutex(v, Constants.MONITOR_EXIT);

            // Return the loaded class.
            LLVMBuildRet(v.builder, clazz);
        };

        v.utils.buildFunc(
                ct.position(),
                funcName, debugName,
                v.ts.Class(), Collections.emptyList(),
                buildBody);
    }

    /**
     * Builds the following constant struct for {@code pi}.
     *
     * struct {
     *   char* name;       // Name (without signature). {@code "<init>"} for constructors.
     *   char* sig;        // JNI-specified signature encoding.
     *   int32_t offset;   // Offset into dispatch vector. -1 for static methods and constructors.
     *   void* fnPtr;      // Direct function pointer. Null for abstract/interface methods.
     *   void* trampoline; // Trampoline for casting the function pointer to the correct type.
     *   void* intf_id;    // For interface methods, the interface method id.
     *   int32_t intf_id_hash; // A precomputed hash of the intf_id.
     };
     */
    protected static LLVMValueRef buildMethodInfo(
            LLVMTranslator v, ClassType ct, ProcedureInstance pi) {

        assert pi instanceof ConstructorInstance || pi instanceof MethodInstance;
        LLVMValueRef name = pi instanceof ConstructorInstance
                ? v.utils.buildGlobalCStr("<init>")
                : v.utils.buildGlobalCStr(((MethodInstance) pi).name());
        LLVMValueRef sig = v.utils.buildGlobalCStr(v.mangler.jniUnescapedSignature(pi));

        LLVMValueRef offset;
        LLVMValueRef intfPtr = LLVMConstNull(v.utils.i8Ptr());
        LLVMValueRef hash = LLVMConstNull(v.utils.i32());
        if (pi.flags().isStatic()) {
            offset = LLVMConstInt(v.utils.i32(), Constants.STATIC_METHOD_INFO_OFFSET, /*sign-extend*/ 1);
        } else if (pi instanceof ConstructorInstance) {
        	offset = LLVMConstInt(v.utils.i32(), Constants.CTOR_METHOD_INFO_OFFSET, /*sign-extend*/ 1);
        } else {
            MethodInstance mi = (MethodInstance) pi;
            LLVMValueRef nullPtr = LLVMConstNull(v.utils.ptrTypeRef(v.dv.structTypeRef(ct)));
            LLVMValueRef gep = v.dv.buildFuncElementPtr(nullPtr, ct, mi);
            offset = LLVMConstPtrToInt(gep, v.utils.i32());
            ClassType intf = v.getImplementingInterface(mi);
            if (intf != null) {
            	intfPtr = v.classObjs.toTypeIdentity(intf);
            	hash = LLVMConstInt(v.utils.i32(), v.utils.intfHash(intf), 0);
            }
        }

        LLVMValueRef fnPtr = pi.flags().isAbstract() || ct.flags().isInterface()
                ? LLVMConstNull(v.utils.ptrTypeRef(v.utils.toLL(pi)))
                : v.utils.getFunction(v.mangler.proc(pi), v.utils.toLL(pi));
        LLVMValueRef fnPtrCast = LLVMConstBitCast(fnPtr, v.utils.i8Ptr());
        LLVMValueRef trampoline = v.utils.getFunction(
                v.mangler.procJniTrampoline(pi), v.utils.toLLTrampoline(pi));
        LLVMValueRef trampolineCast = LLVMConstBitCast(trampoline, v.utils.i8Ptr());

        LLVMValueRef modifiers = LLVMConstInt(v.utils.i32(), pi.flags().toModifiers(), 1);

        LLVMValueRef returnType;

        LLVMValueRef[] argTypes = pi.formalTypes().stream()
                .map(argType -> getTypePointer(v, argType))
                .toArray(LLVMValueRef[]::new);
        LLVMValueRef argTypesPtr = v.utils.buildGlobalArrayAsPtr(v.utils.i8Ptr(),
                argTypes);
        LLVMValueRef numArgTypes = LLVMConstInt(v.utils.i32(), pi.formalTypes().size(), /*sign-extend*/ 1);

        if (pi instanceof ConstructorInstance) {
            ConstructorInstance ci = (ConstructorInstance) pi;
            returnType = getTypePointer(v, ci.container());
        } else {
            MethodInstance mi = (MethodInstance) pi;
            returnType = getTypePointer(v, mi.returnType());
        }

        return v.utils.buildConstStruct(name, sig, offset, fnPtrCast, trampolineCast, intfPtr, hash,
                modifiers, returnType, numArgTypes, argTypesPtr);
    }

    @SuppressWarnings("WeakerAccess")
    public static void initClassDataStructures(LLVMTranslator v, ClassType ct, ClassBody cb) {

        v.classObjs.toTypeIdentity(ct, /*extern*/ false);
        buildClassLoadingFunc(v, ct, cb);

        if (ct.flags().isInterface()) {
            // Interfaces don't have the remaining class data structure.
            return;
        }

        v.classObjs.classObjRef(ct);

        List<ClassType> interfaces = v.allInterfaces(ct);

        if (!ct.flags().isAbstract()) {
            // Initialize the dispatch vector for this class.
            v.dv.initializeDispatchVectorFor(ct);
        }

        if (!ct.flags().isAbstract() && !interfaces.isEmpty()) {
            int numOfIntfs = interfaces.size();
            LLVMValueRef[] intf_id_hashes = new LLVMValueRef[numOfIntfs];
            LLVMValueRef[] intf_ids = new LLVMValueRef[numOfIntfs];
            LLVMValueRef[] intfTables = new LLVMValueRef[numOfIntfs];

            // Initialize the IDV globals
            for (int i = 0; i < numOfIntfs; ++i) {
                ClassType it = interfaces.get(i);

                int hash = v.utils.intfHash(it);
                intf_id_hashes[i] = LLVMConstInt(
                        LLVMInt32TypeInContext(v.context), hash,
                        /* sign-extend */ 0);

                LLVMValueRef intf_id_global = v.classObjs.toTypeIdentity(it);
                intf_ids[i] = intf_id_global;

                LLVMTypeRef idvType = v.utils.toIDVTy(it);
                LLVMValueRef idvGlobal = v.utils.toIDVGlobal(it, ct);
                LLVMValueRef[] idvMethods = v.utils.toIDVSlots(it, ct);
                LLVMValueRef init = v.utils.buildNamedConstStruct(idvType, idvMethods);
                LLVMSetInitializer(idvGlobal, init);
                intfTables[i] = LLVMBuildBitCast(
                        v.builder, idvGlobal, v.utils.i8Ptr(), "cast");
            }

            // Set up the hash table that points to the interface dispatch
            // vectors
            LLVMValueRef cdv_global = v.dv.getDispatchVectorFor(ct);
            LLVMValueRef idv_arr_global = v.utils.toIDVArrGlobal(ct, numOfIntfs);
            LLVMValueRef idv_id_arr_global = v.utils.toIDVIdArrGlobal(ct, numOfIntfs);
            LLVMValueRef idv_id_hash_arr_global = v.utils.toIDVIdHashArrGlobal(ct, numOfIntfs);

            LLVMSetInitializer(idv_arr_global, v.utils
                    .buildConstArray(v.utils.i8Ptr(), intfTables));
            LLVMSetInitializer(idv_id_arr_global,
                    v.utils.buildConstArray(v.utils.i8Ptr(), intf_ids));
            LLVMSetInitializer(idv_id_hash_arr_global, v.utils.buildConstArray(
                    LLVMInt32TypeInContext(v.context), intf_id_hashes));

            LLVMTypeRef create_idv_ht_func_type = v.utils.functionType(
                    LLVMVoidTypeInContext(v.context), // void return type
                    v.utils.i8Ptr(), // dv*
                    LLVMInt32TypeInContext(v.context), // int
                    LLVMInt32TypeInContext(v.context), // int
                    v.utils.ptrTypeRef(LLVMInt32TypeInContext(v.context)), // int[]
                    v.utils.i8Ptr(), // void*[]
                    v.utils.i8Ptr() // it*[]
            );
            LLVMValueRef create_idv_ht_func = v.utils.getFunction(
                    "__createInterfaceTables", create_idv_ht_func_type);
            int capacity = v.utils.idvCapacity(numOfIntfs);
            v.utils.buildCtor(() -> {
                v.utils.buildProcCall(create_idv_ht_func,
                        v.utils.buildCastToBytePtr(cdv_global),
                        LLVMConstInt(LLVMInt32TypeInContext(v.context),
                                capacity, /* sign-extend */ 0),
                        LLVMConstInt(LLVMInt32TypeInContext(v.context),
                                numOfIntfs, /* sign-extend */ 0),
                        LLVMConstBitCast(idv_id_hash_arr_global,
                                v.utils.ptrTypeRef(
                                        LLVMInt32TypeInContext(v.context))),
                        v.utils.buildCastToBytePtr(idv_id_arr_global),
                        v.utils.buildCastToBytePtr(idv_arr_global));
                return null;
            });
        }
    }
}
