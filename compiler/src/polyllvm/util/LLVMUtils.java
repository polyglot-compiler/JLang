package polyllvm.util;

import org.bytedeco.javacpp.PointerPointer;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.RawClass;
import polyglot.ext.jl5.types.inference.LubType;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;
import polyllvm.visit.LLVMTranslator;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.bytedeco.javacpp.LLVM.*;

/**
 * Helper methods for building common LLVM types and IR instructions. This
 * includes methods for retrieving, e.g., object layout and interface methods
 * for a class.
 */
public class LLVMUtils {
    private final LLVMTranslator v;

    public LLVMUtils(LLVMTranslator v) {
        this.v = v;
    }

    public ParsedClassType getArrayType() {
        try {
            return (ParsedClassType) v.typeSystem()
                    .typeForName("support.Array");
        } catch (SemanticException | ClassCastException e) {
            throw new InternalCompilerError("Could not load array type");
        }
    }

    /**
     * Returns the {@link JL5TypeSystem#erasureType(Type) erasure} of type
     * {@code t}, except
     * <ul>
     * <li>that it returns the class of the LLVM representation of Java arrays
     * when the type is an array type, and</li>
     * <li>that it in addition {@link LubType#calculateLub() forces} LUB types
     * and computes the erasure of the forced LUB type.</li>
     * </ul>
     */
    public Type erasureLL(Type t) {
        if (t instanceof LubType) {
            t = ((LubType) t).calculateLub();
        } else if (t.isArray()) {
            t = getArrayType();
        }
        Type jErasure = v.typeSystem().erasureType(t);
        assert jErasure.isPrimitive() || jErasure.isNull()
                || jErasure instanceof ParsedClassType
                || jErasure instanceof RawClass;
        return jErasure;
    }

    /**
     * Same as {@link #erasureLL(Type)} but asserting that the "erasure" of a
     * {@code ReferenceType} is either a {@code ParsedClassType} or a
     * {@code RawClass}.
     */
    public ClassType erasureLL(ReferenceType t) {
        return erasureLL((Type) t).toClass();
    }

    /**
     * Returns the {@link #erasureLL(Type) erasure} of the formal parameter
     * types of the <em>non-substituted version</em> of a method.
     *
     * @param m
     *            the method, possibly with substitution (for its type
     *            parameters and its enclosing classes' type parameters) already
     *            applied
     */
    public List<? extends Type> formalsErasureLL(MethodInstance m) {
        return m.orig().formalTypes().stream().map(this::erasureLL)
                .collect(Collectors.toList());
    }

    /**
     * Returns the {@link #erasureLL(Type) erasure} of the return type of the
     * <em>non-substituted version</em> of a method.
     *
     * @param m
     *            the method, possibly with substitution (for its type
     *            parameters and its enclosing classes' type parameters) applied
     */
    public Type retErasureLL(MethodInstance m) {
        return erasureLL(m.orig().returnType());
    }

    /**
     * Returns the {@link #erasureLL(Type) erasure} of the formal parameter
     * types of the <em>non-substituted version</em> of a constructor.
     *
     * @param c
     *            the constructor, possibly with substitution (for its type
     *            parameters and its enclosing classes' type parameters) already
     *            applied
     */
    public List<? extends Type> formalsErasureLL(ConstructorInstance c) {
        return c.orig().formalTypes().stream().map(this::erasureLL)
                .collect(Collectors.toList());
    }

    public LLVMTypeRef llvmPtrSizedIntType() {
        return LLVMIntTypeInContext(v.context, llvmPtrSize() * 8);
    }

    public static int llvmPtrSize() {
        return 8;
    }

    public LLVMTypeRef llvmBytePtr() {
        return ptrTypeRef(LLVMInt8TypeInContext(v.context));
    }

    public LLVMTypeRef intType(int numBytes) {
        return LLVMIntTypeInContext(v.context, numBytes);
    }

    public LLVMTypeRef ptrTypeRef(LLVMTypeRef elemType) {
        return LLVMPointerType(elemType, Constants.LLVM_ADDR_SPACE);
    }

    private LLVMTypeRef structTypeRefOpaque(String mangledName) {
        LLVMTypeRef res = LLVMGetTypeByName(v.mod, mangledName);
        if (res == null)
            res = LLVMStructCreateNamed(v.context, mangledName);
        return res;
    }

    public LLVMTypeRef functionType(LLVMTypeRef ret, LLVMTypeRef... args) {
        return LLVMFunctionType(ret, new PointerPointer<>(args), args.length,
                /* isVarArgs */ 0);
    }

    private void setStructBody(LLVMTypeRef struct, LLVMTypeRef... types) {
        LLVMStructSetBody(struct, new PointerPointer<>(types), types.length,
                /* packed */ 0);
    }

    public LLVMValueRef buildCastToBytePtr(LLVMValueRef val) {
        return LLVMConstBitCast(val, llvmBytePtr());
    }

    public LLVMValueRef buildProcedureCall(LLVMValueRef func,
            LLVMValueRef... args) {
        if (v.inTry() && !Constants.NON_INVOKE_FUNCTIONS
                .contains(LLVMGetValueName(func).getString())) {
            LLVMBasicBlockRef invokeCont = LLVMAppendBasicBlockInContext(
                    v.context, v.currFn(), "invoke.cont");
            LLVMValueRef invoke = LLVMBuildInvoke(v.builder, func,
                    new PointerPointer<>(args), args.length, invokeCont,
                    v.currLpad(), "");
            LLVMPositionBuilderAtEnd(v.builder, invokeCont);
            return invoke;
        }
        return LLVMBuildCall(v.builder, func, new PointerPointer<>(args),
                args.length, "");
    }

    public LLVMValueRef buildMethodCall(LLVMValueRef func,
            LLVMValueRef... args) {
        if (v.inTry() && !Constants.NON_INVOKE_FUNCTIONS
                .contains(LLVMGetValueName(func).getString())) {
            LLVMBasicBlockRef invokeCont = LLVMAppendBasicBlockInContext(
                    v.context, v.currFn(), "invoke.cont");
            LLVMValueRef invoke = LLVMBuildInvoke(v.builder, func,
                    new PointerPointer<>(args), args.length, invokeCont,
                    v.currLpad(), "call");
            LLVMPositionBuilderAtEnd(v.builder, invokeCont);
            return invoke;
        }
        return LLVMBuildCall(v.builder, func, new PointerPointer<>(args),
                args.length, "call");
    }

    /**
     * The ctor supplier should build the body of the ctor and return a pointer
     * to the data that it initializes, or return null if not applicable. (If
     * the associated data is never used in the resulting program, then LLVM
     * knows to prevent the ctor from running.) Ctor functions will run in the
     * order that they are built.
     */
    public void buildCtor(Node n, Supplier<LLVMValueRef> ctor) {
        LLVMTypeRef funcType = v.utils
                .functionType(LLVMVoidTypeInContext(v.context));
        LLVMTypeRef voidPtr = v.utils
                .ptrTypeRef(LLVMInt8TypeInContext(v.context));

        int counter = v.incCtorCounter();
        String name = "ctor" + counter;
        LLVMValueRef func = v.utils.getFunction(v.mod, name, funcType);
        LLVMSetLinkage(func, LLVMPrivateLinkage);
        LLVMMetadataRef typeArray = LLVMDIBuilderGetOrCreateTypeArray(
                v.debugInfo.diBuilder, new PointerPointer<>(), /* length */ 0);
        LLVMMetadataRef funcDiType = LLVMDIBuilderCreateSubroutineType(
                v.debugInfo.diBuilder, v.debugInfo.createFile(), typeArray);
        v.debugInfo.funcDebugInfo(0, name, name, funcDiType, func);
        v.debugInfo.emitLocation(n);

        LLVMBasicBlockRef entry = LLVMAppendBasicBlockInContext(v.context, func,
                "entry");
        LLVMBasicBlockRef body = LLVMAppendBasicBlockInContext(v.context, func,
                "body");
        LLVMPositionBuilderAtEnd(v.builder, entry);
        LLVMBuildBr(v.builder, body);
        LLVMPositionBuilderAtEnd(v.builder, body);

        // We use `counter` as the ctor priority to help ensure that static
        // initializers
        // are executed in textual order, per the JLS.
        LLVMTypeRef i32 = LLVMInt32TypeInContext(v.context);
        LLVMValueRef priority = LLVMConstInt(i32, counter, /* sign-extend */ 0);

        v.pushFn(func);
        LLVMValueRef data = ctor.get(); // Calls supplier lambda to build ctor
                                        // body.
        v.popFn();

        if (data == null)
            data = LLVMConstNull(voidPtr);
        LLVMValueRef castData = LLVMConstBitCast(data, voidPtr);
        LLVMValueRef res = v.utils.buildConstStruct(priority, func, castData);
        v.addCtor(res);

        LLVMBuildRetVoid(v.builder);
        v.debugInfo.popScope();
    }

    /**
     * Returns the pointer to the function with the given name and type in the
     * given module. If the function is not in the module, it gets declared in
     * the module before returned.
     */
    public LLVMValueRef getFunction(LLVMModuleRef mod, String functionName,
            LLVMTypeRef functionType) {
        LLVMValueRef func = LLVMGetNamedFunction(mod, functionName);
        if (func == null)
            func = LLVMAddFunction(mod, functionName, functionType);
        return func;
    }

    /**
     * Creates a bitcast that casts a pointer to a function to a given type.
     *
     * @param funcPtr
     * @param castToTy
     *            the cast-to function type (not a pointer type)
     * @return
     */
    public LLVMValueRef bitcastFunc(LLVMValueRef funcPtr,
            LLVMTypeRef castToTy) {
        return LLVMBuildBitCast(v.builder, funcPtr, ptrTypeRef(castToTy),
                "func_cast");
    }

    public LLVMTypeRef structType(LLVMTypeRef... types) {
        return LLVMStructType(new PointerPointer<>(types), types.length,
                /* Packed */ 0);
    }

    /**
     * If the global is already in the module, return it, otherwise add it to
     * the module and return it.
     */
    public LLVMValueRef getGlobal(LLVMModuleRef mod, String globalName,
            LLVMTypeRef globalType) {
        LLVMValueRef global = LLVMGetNamedGlobal(mod, globalName);
        if (global == null)
            global = LLVMAddGlobal(mod, globalType, globalName);
        return global;
    }

    public LLVMValueRef buildGEP(LLVMValueRef ptr, LLVMValueRef... indices) {
        // TODO: If safe to do so, might be better to use LLVMBuildInBoundsGEP.
        return LLVMBuildGEP(v.builder, ptr, new PointerPointer<>(indices),
                indices.length, "gep");
    }

    /**
     * Create a constant GEP using i32 indices from indices
     */
    public LLVMValueRef constGEP(LLVMValueRef ptr, int... indices) {
        LLVMValueRef[] llvmIndices = Arrays.stream(indices)
                .mapToObj(i -> LLVMConstInt(LLVMInt32TypeInContext(v.context),
                        i, /* sign-extend */ 0))
                .toArray(LLVMValueRef[]::new);
        return LLVMConstGEP(ptr, new PointerPointer<>(llvmIndices),
                llvmIndices.length);
    }

    public LLVMValueRef buildStructGEP(LLVMValueRef ptr, int... intIndices) {
        // LLVM suggests using i32 offsets for struct GEP instructions.
        LLVMValueRef[] indices = IntStream.of(intIndices)
                .mapToObj(i -> LLVMConstInt(LLVMInt32TypeInContext(v.context),
                        i, /* sign-extend */ 0))
                .toArray(LLVMValueRef[]::new);
        return LLVMBuildGEP(v.builder, ptr, new PointerPointer<>(indices),
                indices.length, "gep");
    }

    /**
     * Return a pointer to the first element in a Java array.
     */
    public LLVMValueRef buildJavaArrayBase(LLVMValueRef arr, Type elemType) {
        LLVMValueRef baseRaw = v.utils.buildStructGEP(arr, 0,
                Constants.ARR_ELEM_OFFSET);
        LLVMTypeRef ptrType = v.utils.ptrTypeRef(v.utils.toLL(elemType, false));
        return LLVMBuildCast(v.builder, LLVMBitCast, baseRaw, ptrType,
                "arr_cast");
    }

    /**
     * Obtains the LLVM type that Java values of type {@code jty} have after
     * translated to LLVM while always populating the structure-type body when
     * the translation is an LLVM structure type.
     *
     * @param jty
     *            the Java type (not required to be erasure)
     */
    public LLVMTypeRef toLL(Type jty) {
        return toLL(jty, true);
    }

    /**
     * Obtains the LLVM type that Java values of type {@code jt} have after
     * translated to LLVM.
     *
     * @param jt
     *            the Java type
     * @param fillInStruct
     *            whether to populate the structure-type body when the
     *            translation is an LLVM structure type
     * @return the LLVM type
     */
    private LLVMTypeRef toLL(final Type jt, boolean fillInStruct) {
        if (jt.isBoolean())
            return LLVMInt1TypeInContext(v.context);
        else if (jt.isLongOrLess())
            return LLVMIntTypeInContext(v.context,
                    numBitsOfIntegralType(jt.toPrimitive()));
        else if (jt.isVoid())
            return LLVMVoidTypeInContext(v.context);
        else if (jt.isFloat())
            return LLVMFloatType();
        else if (jt.isDouble())
            return LLVMDoubleType();
        else if (jt.isNull())
            return llvmBytePtr();
        else if (jt.isReference())
            return toObj(jt.toReference(), fillInStruct);
        else
            throw new InternalCompilerError("Cannot handle " + jt.getClass());
    }

    /**
     * Obtains the LLVM type that Java values of reference type {@code jt} have
     * after translated to LLVM.
     *
     * @param jt
     *            the Java reference type (not required to be erasure)
     * @param fillInStruct
     *            whether to populate the structure-type body when the structure
     *            type is opaque
     * @return the LLVM type
     */
    private LLVMTypeRef toObj(ReferenceType jt, boolean fillInStruct) {
        String mangledName = v.mangler.classTypeName(jt);
        LLVMTypeRef structType = structTypeRefOpaque(mangledName);
        if (LLVMIsOpaqueStruct(structType) != 0 && fillInStruct) {
            if (erasureLL(jt).flags().isInterface())
                setStructBody(structType);
            else
                setStructBody(structType, toObjSlots(jt));
        }
        return ptrTypeRef(structType);
    }

    /**
     * The LLVM types of each slot in the LLVM structure representation of a
     * Java object of Java type {@code jt}.
     *
     * @param jt
     *            The Java non-interface reference type (not required to be
     *            erasure).
     * @return an array of LLVM types that correspond to the slots in the object
     *         layout for Java type {@code jt}.
     */
    private LLVMTypeRef[] toObjSlots(ReferenceType jt) {
        List<FieldInstance> fields = v.objFields(erasureLL(jt));
        LLVMTypeRef ptr_cdv_ty = ptrTypeRef(toCDVTy(jt));

        int numOfSlots = Constants.OBJECT_FIELDS_OFFSET + fields.size();
        boolean isArray = jt.isArray()
                || (jt.isClass() && jt.toClass().typeEquals(getArrayType()));
        if (isArray)
            numOfSlots += 1;
        LLVMTypeRef[] res = new LLVMTypeRef[numOfSlots];
        int idx = 0;
        res[idx++] = /* pointer to CDV */ ptr_cdv_ty;
        res[idx++] = /* pointer to struct of sync vars */ llvmBytePtr();
        for (FieldInstance fi : fields)
            res[idx++] = /* field */ toLL(fi.type(), false);
        if (isArray)
            res[idx++] = /* extra i8* slot */ llvmBytePtr();
        return res;
    }

    /**
     * Obtains the LLVM global variable that denotes the class dispatch vector
     * for Java reference type {@code jt}.
     *
     * @param jt
     *            The Java type (not required to be erasure)
     */
    public LLVMValueRef toCDVGlobal(ReferenceType jt) {
        return getGlobal(v.mod, v.mangler.cdvGlobalId(jt), toCDVTy(jt));
    }

    /**
     * Obtains the LLVM type of the LLVM structure representation of the class
     * dispatch vector for Java reference type {@code jt}.
     *
     * @param jt
     *            The Java type (not required to be erasure)
     */
    public LLVMTypeRef toCDVTy(ReferenceType jt) {
        String mangledDVName = v.mangler.cdvTyName(erasureLL(jt));
        LLVMTypeRef cdv_ty = structTypeRefOpaque(mangledDVName);
        if (LLVMIsOpaqueStruct(cdv_ty) != 0)
            setStructBody(cdv_ty, toCDVTySlots(jt));
        return cdv_ty;
    }

    /**
     * The LLVM types of each slot in the LLVM structure representation of the
     * class dispatch vector for Java type {@code jt}.
     *
     * @param jt
     *            The Java non-interface reference type (not required to be
     *            erasure).
     * @return an array of LLVM types that correspond to the slots in the class
     *         dispatch vector for Java type {@code jt}.
     */
    private LLVMTypeRef[] toCDVTySlots(ReferenceType jt) {
        List<MethodInstance> methods = v.cdvMethods(erasureLL(jt));
        LLVMTypeRef[] res = new LLVMTypeRef[Constants.CLASS_DISP_VEC_OFFSET
                + methods.size()];
        int idx = 0;
        // 1st slot points to the table of IDVs
        res[idx++] = ptrTypeRef(LLVMInt8TypeInContext(v.context));
        // 2nd slot points to RTTI
        res[idx++] = ptrTypeRef(v.classObjs.classObjTypeRef(jt));
        // remaining slots point to method codes
        for (MethodInstance m : methods) {
            LLVMTypeRef m_ty = toLLFuncTy(jt, m.returnType(), m.formalTypes());
            res[idx++] = ptrTypeRef(m_ty);
        }
        return res;
    }

    /**
     * Obtains the LLVM global variable that denotes the interface dispatch
     * vector for Java interface type {@code intf} implemented by {@code clazz}.
     *
     * @param intf
     *            The Java interface type (not required to be erasure)
     * @param clazz
     *            The Java class type (not required to be erasure)
     */
    public LLVMValueRef toIDVGlobal(ClassType intf, ReferenceType clazz) {
        return getGlobal(v.mod, v.mangler.idvGlobalId(intf, clazz),
                toIDVTy(intf));
    }

    /**
     * Obtains the LLVM type of the LLVM structure representation of the
     * interface dispatch vector for Java interface type {@code intf}.
     *
     * @param intf
     *            The Java interface type (not required to be the erasure)
     */
    public LLVMTypeRef toIDVTy(ClassType intf) {
        String mangledDVName = v.mangler.idvTyName(intf);
        LLVMTypeRef idv_ty = structTypeRefOpaque(mangledDVName);
        if (LLVMIsOpaqueStruct(idv_ty) != 0)
            setStructBody(idv_ty, toIDVTySlots(intf));
        return idv_ty;
    }

    /**
     * The LLVM types of each slot in the LLVM structure representation of the
     * interface dispatch vector for Java interface type {@code intf}.
     *
     * @param intf
     *            The Java interface type (not required to be erasure)
     * @return an array of LLVM types that correspond to the slots in the
     *         interface dispatch vector for Java type {@code jt}.
     */
    private LLVMTypeRef[] toIDVTySlots(ClassType intf) {
        List<MethodInstance> methods = v.idvMethods(erasureLL(intf));
        LLVMTypeRef[] res = new LLVMTypeRef[Constants.INTF_DISP_VEC_OFFSET
                + methods.size()];
        int idx = 0;
        for (MethodInstance m : methods) {
            LLVMTypeRef m_ty = toLLFuncTy(intf, m.returnType(),
                    m.formalTypes());
            res[idx++] = ptrTypeRef(m_ty);
        }
        return res;
    }

    /**
     * Returns the hash code used to index interface type {@code intf} in the
     * hash table of a class' IDVs.
     *
     * @param intf
     *            the Java interface type
     */
    public int intfHash(ClassType intf) {
        ParsedClassType base = (ParsedClassType) erasureLL(intf).declaration();
        int h = base.toString().hashCode();
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }

    /**
     * Returns the capacity of the hash table of IDVs. The capacity is the
     * smallest power of 2 that is at least the number of IDVs.
     *
     * @param size
     *            the number of IDVs.
     */
    public int idvCapacity(int size) {
        assert size >= 0;
        int approx = (int) Math.ceil(size * 1.5); // load factor = 0.5
        int c = 1;
        while ((approx >>>= 1) > 0)
            ++c;
        return 1 << c;
    }

    public LLVMValueRef toIDVArrGlobal(ReferenceType clazz, int length) {
        return getGlobal(v.mod, v.mangler.idvArrGlobalId(clazz),
                LLVMArrayType(llvmBytePtr(), length));
    }

    public LLVMValueRef toIDVIdArrGlobal(ReferenceType clazz, int length) {
        return getGlobal(v.mod, v.mangler.idvIdArrGlobalId(clazz),
                LLVMArrayType(llvmBytePtr(), length));
    }

    public LLVMValueRef toIDVIdHashArrGlobal(ReferenceType clazz, int length) {
        return getGlobal(v.mod, v.mangler.idvIdHashArrGlobalId(clazz),
                LLVMArrayType(LLVMInt32TypeInContext(v.context), length));
    }

    /**
     * Returns the LLVM representation of each slot in the class dispatch vector
     * of Java type {@code jt} except for the first slot. The first slot is left
     * null, and will be initialized later to point to the IDV hash table.
     *
     * @param jt
     *            The Java non-abstract class type.
     * @return
     */
    public LLVMValueRef[] toCDVSlots(ReferenceType jt) {
        return toCDVSlots(jt, LLVMConstNull(llvmBytePtr()));
    }

    private LLVMValueRef[] toCDVSlots(ReferenceType jt, LLVMValueRef next) {
        // The method signatures are obtained using the erasure of the Java
        // class type.
        List<MethodInstance> jms = v.cdvMethods(erasureLL(jt));

        LLVMValueRef[] res = new LLVMValueRef[Constants.CLASS_DISP_VEC_OFFSET
                + jms.size()];
        int idx = 0;
        // 1st slot points to the hash table of IDVs
        res[idx++] = next;
        // 2nd slot points to RTTI
        res[idx++] = v.classObjs.classObjRef(jt);
        // remaining slots point to method codes
        for (MethodInstance jm : jms) {
            LLVMTypeRef llm_ty_cast_from = toLLFuncTy(jm.container(),
                    jm.returnType(), jm.formalTypes());
            LLVMTypeRef llm_ty_cast_to = toLLFuncTy(jt, jm.returnType(),
                    jm.formalTypes());
            LLVMValueRef llm = getFunction(v.mod,
                    v.mangler.mangleProcedureName(jm), llm_ty_cast_from);
            LLVMValueRef cast = LLVMConstBitCast(llm,
                    ptrTypeRef(llm_ty_cast_to));
            res[idx++] = cast;
        }
        return res;
    }

    /**
     * Returns the LLVM representation of each slot in the interface dispatch
     * vector of Java class type {@code clazz} for Java interface type
     * {@code intf}.
     *
     * @param intf
     *            a Java interface type
     *            {@link LLVMTranslator#allInterfaces(ClassType) implemented} by
     *            {@code clazz}
     * @param clazz
     *            the non-abstract Java class type
     * @return
     */
    public LLVMValueRef[] toIDVSlots(ClassType intf, ParsedClassType clazz) {
        List<MethodInstance> cdvMethods = v.cdvMethods(erasureLL(clazz));
        List<MethodInstance> idvMethods = v.idvMethods(erasureLL(intf));
        int idvSize = idvMethods.size();
        LLVMValueRef[] res = new LLVMValueRef[idvSize];
        // For each method in IDV, find the corresponding method in the CDV so
        // as to get the right mangled name and signature.
        // However, method signatures in cdvMethods do not in general (due to
        // erasure) override those in idvMethods.
        List<MethodInstance> clazzMethods = v.cdvMethods(clazz);
        List<MethodInstance> intfMethods = v.idvMethods(intf);
        // Fortunately, method signatures in clazzMethods do override those in
        // intfMethods.
        for (int idxI = 0; idxI < idvSize; ++idxI) {
            MethodInstance intfM = intfMethods.get(idxI);
            int idxC = v.indexOfOverridingMethod(intfM, clazzMethods);
            // The idxI-th method in IDV is the idxC-th method in CDV.
            MethodInstance cdvM = cdvMethods.get(idxC);
            LLVMTypeRef cdvM_LLTy = toLLFuncTy(clazz, cdvM.returnType(),
                    cdvM.formalTypes());
            LLVMValueRef funcVal = getFunction(v.mod,
                    v.mangler.mangleProcedureName(cdvM), cdvM_LLTy);
            // Cast funcVal to the method signature used by IDV
            MethodInstance idvM = idvMethods.get(idxI);
            LLVMTypeRef idvM_LLTy = toLLFuncTy(intf, idvM.returnType(),
                    idvM.formalTypes());
            LLVMValueRef cast = LLVMConstBitCast(funcVal,
                    ptrTypeRef(idvM_LLTy));
            res[idxI] = cast;
        }
        return res;
    }

    /**
     * @param recvTy
     * @param retTy
     * @param formalTys
     * @return The LLVM function type whose argument types are the translation
     *         of {@code recvTy} followed by the translation of
     *         {@code formalTys}, and whose return type is the translation of
     *         {@code retTy};
     */
    public LLVMTypeRef toLLFuncTy(ReferenceType recvTy, Type retTy,
            List<? extends Type> formalTys) {
        LLVMTypeRef[] arg_tys = Stream
                .of(CollectUtils.toArray(recvTy, formalTys, Type.class))
                .map(jt -> toLL(jt, false)).toArray(LLVMTypeRef[]::new);
        LLVMTypeRef ret_ty = toLL(retTy, false);
        return functionType(ret_ty, arg_tys);
    }

    /**
     * @param retTy
     * @param formalTys
     * @return The LLVM function type whose argument types are the translation
     *         of {@code formalTys} and whose return type is the translation of
     *         {@code retTy};
     */
    public LLVMTypeRef toLLFuncTy(Type retTy, List<? extends Type> formalTys) {
        LLVMTypeRef[] arg_tys = formalTys.stream().map(t -> toLL(t, false))
                .toArray(LLVMTypeRef[]::new);
        LLVMTypeRef ret_ty = toLL(retTy, false);
        return functionType(ret_ty, arg_tys);
    }

    /**
     * Returns the bitcast that casts the translation of a Java expression to
     * the LLVM translation of the expression's expected type. (For example, the
     * expected type of the initializer of a local variable declaration is the
     * type of the variable.)
     * <p>
     * This method is different from {@link #toBitcastR}.
     *
     * @param e
     * @param expectedTy
     * @return
     */
    public LLVMValueRef bitcastToLHS(Expr e, Type expectedTy) {
        return LLVMBuildSExtOrBitCast(v.builder, v.getTranslation(e),
                toLL(expectedTy), "cast_with_expected");
    }

    /**
     * Returns the bitcast to the LLVM translation of a Java type. It is used to
     * eliminate any potential mismatch between the LLVM type of the translation
     * of a Java expression and the LLVM translation of the Java expression's
     * type.
     * <p>
     * This method is different from {@link #bitcastToLHS}.
     *
     * @param val
     *            the LLVM translation of a Java expression (an r-value)
     * @param jty
     *            the Java type of the Java expression
     * @return
     */
    public LLVMValueRef toBitcastR(LLVMValueRef val, Type jty) {
        return LLVMBuildBitCast(v.builder, val, toLL(jty), "cast_r");
    }

    /**
     * Returns the bitcast to the LLVM translation of a Java type. It is used to
     * eliminate any potential mismatch between the LLVM type of the translation
     * of a Java expression and the LLVM translation of the Java expression's
     * type.
     *
     * @param val
     *            the LLVM translation of a Java expression (an l-value)
     * @param jty
     *            the Java type of the Java expression
     * @return
     */
    public LLVMValueRef toBitcastL(LLVMValueRef val, Type jty) {
        return LLVMBuildBitCast(v.builder, val, ptrTypeRef(toLL(jty)),
                "cast_l");
    }

    public LLVMValueRef buildConstArray(LLVMTypeRef type,
            LLVMValueRef... values) {
        return LLVMConstArray(type, new PointerPointer<>(values),
                values.length);
    }

    public LLVMValueRef buildConstStruct(LLVMValueRef... values) {
        PointerPointer<LLVMValueRef> valArr = new PointerPointer<>(values);
        return LLVMConstStructInContext(v.context, valArr, values.length,
                /* packed */ 0);
    }

    /**
     * Returns the number of bytes occupied by a value of Java type {@code t}.
     *
     * @param t
     *            the Java type (not required to be erasure)
     * @return
     */
    public int sizeOfType(Type t) {
        Type erased = erasureLL(t);
        if (erased.isBoolean()) {
            return 1;
        } else if (erased.isFloat()) {
            return 4; // Specified by Java.
        } else if (erased.isDouble()) {
            return 8; // Specified by Java.
        } else if (erased.isLongOrLess()) {
            PrimitiveType integral = erased.toPrimitive();
            assert numBitsOfIntegralType(integral)
                    % 8 == 0 : "integer bit count must be multiple of 8";
            return numBitsOfIntegralType(integral) / 8;
        } else if (erased.isNull()) {
            return llvmPtrSize();
        } else if (erased.isArray()) {
            return llvmPtrSize();
        } else if (erased.isClass()) {
            return llvmPtrSize();
        } else {
            throw new InternalCompilerError("Invalid type");
        }
    }

    private int numBitsOfIntegralType(PrimitiveType t) {
        if (t.isByte())
            return 8;
        else if (t.isShort())
            return 16;
        else if (t.isChar())
            return 16;
        else if (t.isInt())
            return 32;
        else if (t.isLong())
            return 64;
        throw new InternalCompilerError(
                "Type " + t + " is not an integral type");
    }

}
