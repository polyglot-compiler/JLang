package polyllvm.extension;

import org.bytedeco.javacpp.LLVM.*;
import polyglot.ast.*;
import polyglot.types.ClassType;
import polyglot.types.FieldInstance;
import polyglot.types.ParsedClassType;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.util.Constants;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.bytedeco.javacpp.LLVM.*;
import static polyllvm.util.Constants.REGISTER_CLASS_FUNC;

public class PolyLLVMClassDeclExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        ClassDecl n = (ClassDecl) node();
        ParsedClassType ct = n.type();
        initClassDataStructures(v, ct, n.body());
        return super.leaveTranslateLLVM(v);
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

        // Declare class object.
        LLVMTypeRef classType = v.utils.toLL(v.ts.Class());
        LLVMValueRef classObjectGlobal = v.utils.getClassObjectGlobal(ct);
        LLVMSetInitializer(classObjectGlobal, LLVMConstNull(classType));

        List<FieldInstance> instanceFields = ct.fields().stream()
                .filter(fi -> !fi.flags().isStatic())
                .collect(Collectors.toList());

        // This layout must precisely mirror the layout defined in the runtime (class.cpp).
        LLVMValueRef classInfo = v.utils.buildConstStruct(

                // Class name, char*
                v.utils.buildGlobalCStr(ct.fullName()),

                // Number of instance fields, i32
                LLVMConstInt(v.utils.i32(), instanceFields.size(), /*sign-extend*/ 0),

                // Instance field names, char**
                v.utils.buildGlobalArrayAsPtr(v.utils.i8Ptr(), instanceFields.stream()
                        .map(fi -> v.utils.buildGlobalCStr(fi.name()))
                        .toArray(LLVMValueRef[]::new)),

                // Instance field offsets, i32*
                v.utils.buildGlobalArrayAsPtr(v.utils.i32(), instanceFields.stream()
                        .map(fi -> {
                            LLVMValueRef nullPtr = LLVMConstNull(v.utils.toLL(fi.container()));
                            LLVMValueRef offset = v.obj.buildFieldElementPtr(nullPtr, fi);
                            return LLVMConstPtrToInt(offset, v.utils.i32());
                        })
                        .toArray(LLVMValueRef[]::new))
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

            // Call into runtime to register this class.
            LLVMTypeRef regClassFuncType = v.utils.functionType(
                    v.utils.voidType(), classType, v.utils.ptrTypeRef(LLVMTypeOf(classInfo)));
            LLVMValueRef regClass = v.utils.getFunction(REGISTER_CLASS_FUNC, regClassFuncType);
            v.utils.buildProcCall(regClass, clazz, classInfoGlobal);

            // Load super class if necessary.
            if (!ct.flags().isInterface() && ct.superType() != null) {
                v.utils.buildClassLoadCheck(ct.superType().toClass());
            }

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

            // Return the loaded class.
            LLVMBuildRet(v.builder, clazz);
        };

        v.utils.buildFunc(
                ct.position(),
                funcName, debugName,
                v.ts.Class(), Collections.emptyList(),
                buildBody);
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
