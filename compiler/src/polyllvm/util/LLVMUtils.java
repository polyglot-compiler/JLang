package polyllvm.util;

import org.bytedeco.javacpp.PointerPointer;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.RawClass;
import polyglot.ext.jl5.types.inference.LubType;
import polyglot.ext.jl7.types.DiamondType;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;
import polyllvm.visit.LLVMTranslator;

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
        }
        else if (t instanceof DiamondType) {
            t = ((DiamondType) t).base();
        }
        else if (t.isArray()) {
            t = v.ts.ArrayObject();
        }

        Type jErasure = v.ts.erasureType(t);
        assert jErasure.isPrimitive()
                || jErasure.isNull()
                || jErasure instanceof ParsedClassType
                || jErasure instanceof RawClass
                : jErasure.getClass();
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

    public FieldInstance erasureLL(FieldInstance fi) {
        return erasureLL(fi.container()).fieldNamed(fi.name());
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
        return m.orig().formalTypes().stream().map(this::erasureLL).collect(Collectors.toList());
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
        return LLVMIntTypeInContext(v.context, 8 * llvmPtrSize());
    }

    public int llvmPtrSize() {
        return 8;
    }

    public LLVMTypeRef llvmBytePtr() {
        return ptrTypeRef(LLVMInt8TypeInContext(v.context));
    }

    public LLVMTypeRef intType(int numBits) {
        return LLVMIntTypeInContext(v.context, numBits);
    }

    public LLVMTypeRef ptrTypeRef(LLVMTypeRef elemType) {
        return LLVMPointerType(elemType, Constants.LLVM_ADDR_SPACE);
    }

    public LLVMTypeRef getOrCreateNamedOpaqueStruct(String mangledName) {
        LLVMTypeRef res = LLVMGetTypeByName(v.mod, mangledName);
        if (res == null)
            res = LLVMStructCreateNamed(v.context, mangledName);
        return res;
    }

    public LLVMTypeRef functionType(LLVMTypeRef ret, LLVMTypeRef... args) {
        return LLVMFunctionType(ret, new PointerPointer<>(args), args.length, /* isVarArgs */ 0);
    }

    public void setStructBody(LLVMTypeRef struct, LLVMTypeRef... types) {
        LLVMStructSetBody(struct, new PointerPointer<>(types), types.length, /* packed */ 0);
    }

    public void fillStructIfNeeded(LLVMTypeRef struct, Supplier<LLVMTypeRef[]> f) {
        if (LLVMIsOpaqueStruct(struct) != 0) {
            setStructBody(struct, f.get());
        }
    }

    public LLVMValueRef buildCastToBytePtr(LLVMValueRef val) {
        return LLVMConstBitCast(val, llvmBytePtr());
    }

    /** Builds a call, or an invoke if the translator is currently within an exception frame. */
    private LLVMValueRef buildCall(
            String label, LLVMBasicBlockRef lpad, LLVMValueRef func, LLVMValueRef... args) {
        String funcName = LLVMGetValueName(func).getString();
        if (lpad != null && !Constants.NON_INVOKE_FUNCTIONS.contains(funcName)) {
            // Invoke instruction which unwinds to the current landing pad.
            LLVMBasicBlockRef invokeCont = v.utils.buildBlock("invoke.cont");
            LLVMValueRef invoke = LLVMBuildInvoke(
                    v.builder, func, new PointerPointer<>(args),
                    args.length, invokeCont, lpad, label);
            LLVMPositionBuilderAtEnd(v.builder, invokeCont);
            return invoke;
        }
        else {
            // Simple call instruction with no landing pad.
            return LLVMBuildCall(v.builder, func, new PointerPointer<>(args), args.length, label);
        }
    }

    // LLVM requires that void-returning functions have the empty string for their label.
    public LLVMValueRef buildProcCall(LLVMValueRef p, LLVMValueRef... a) {
        return buildCall("", v.currLandingPad(), p, a);
    }

    // Same as above, but allows custom landing pad.
    public void buildProcCall(LLVMBasicBlockRef lpad, LLVMValueRef p, LLVMValueRef... a) {
        buildCall("", lpad, p, a);
    }

    public LLVMValueRef buildFunCall(LLVMValueRef fun, LLVMValueRef... args) {
        return buildCall("call", v.currLandingPad(), fun, args);
    }

    /**
     * The ctor supplier should build the body of the ctor and return a pointer
     * to the data that it initializes, or return null if not applicable. (If
     * the associated data is never used in the resulting program, then LLVM
     * knows to prevent the ctor from running.) Ctor functions will run in the
     * order that they are built.
     */
    public void buildCtor(Supplier<LLVMValueRef> ctor) {
        LLVMBasicBlockRef prevBlock = LLVMGetInsertBlock(v.builder);
        LLVMTypeRef funcType = v.utils.functionType(LLVMVoidTypeInContext(v.context));
        LLVMTypeRef voidPtr = v.utils.llvmBytePtr();

        int counter = v.incCtorCounter();
        String name = "ctor." + counter;
        LLVMValueRef func = v.utils.getFunction(name, funcType);
        LLVMSetLinkage(func, LLVMPrivateLinkage);
        LLVMMetadataRef typeArray = LLVMDIBuilderGetOrCreateTypeArray(
                v.debugInfo.diBuilder, new PointerPointer<>(), /* length */ 0);
        LLVMMetadataRef funcDiType = LLVMDIBuilderCreateSubroutineType(
                v.debugInfo.diBuilder, v.debugInfo.debugFile, typeArray);
        v.debugInfo.funcDebugInfo(func, name, name, funcDiType, 0);

        LLVMBasicBlockRef entry = LLVMAppendBasicBlockInContext(v.context, func, "entry");
        LLVMBasicBlockRef body = LLVMAppendBasicBlockInContext(v.context, func, "body");
        LLVMPositionBuilderAtEnd(v.builder, body);

        // We use `counter` as the ctor priority to help ensure that static initializers
        // are executed in textual order, per the JLS.
        LLVMTypeRef i32 = LLVMInt32TypeInContext(v.context);
        LLVMValueRef priority = LLVMConstInt(i32, counter, /* sign-extend */ 0);

        v.pushFn(func);
        LLVMValueRef data = ctor.get(); // Calls supplier lambda to build ctor body.
        v.popFn();

        if (data == null)
            data = LLVMConstNull(voidPtr);
        LLVMValueRef castData = LLVMConstBitCast(data, voidPtr);
        LLVMValueRef res = v.utils.buildConstStruct(priority, func, castData);
        v.addCtor(res);

        LLVMBuildRetVoid(v.builder);

        // Connect entry to body after all alloca instructions.
        LLVMPositionBuilderAtEnd(v.builder, entry);
        LLVMBuildBr(v.builder, body);

        v.debugInfo.popScope();
        LLVMPositionBuilderAtEnd(v.builder, prevBlock);
    }

    /**
     * Allocates space for a new variable on the stack, and returns the pointer to this space.
     * Does not change the position of the instruction builder.
     */
    public LLVMValueRef buildAlloca(String name, LLVMTypeRef t) {
        LLVMBasicBlockRef prevBlock = LLVMGetInsertBlock(v.builder);
        LLVMPositionBuilderAtEnd(v.builder, LLVMGetEntryBasicBlock(v.currFn()));
        LLVMValueRef res = LLVMBuildAlloca(v.builder, t, name);
        LLVMPositionBuilderAtEnd(v.builder, prevBlock);
        return res;
    }

    /** Convenience function for appending basic blocks to the current function. */
    public LLVMBasicBlockRef buildBlock(String name) {
        return LLVMAppendBasicBlockInContext(v.context, v.currFn(), name);
    }

    /**
     *  If the current block has no terminator, then branch to [block].
     */
    public void branchUnlessTerminated(LLVMBasicBlockRef block) {
        LLVMBasicBlockRef curr = LLVMGetInsertBlock(v.builder);
        if (LLVMGetBasicBlockTerminator(curr) == null) {
            LLVMBuildBr(v.builder, block);
        }
    }

    /**
     * Returns the pointer to the function with the given name and type in the
     * given module. If the function is not in the module, it gets declared in
     * the module before returned.
     */
    public LLVMValueRef getFunction(String functionName, LLVMTypeRef functionType) {
        LLVMValueRef func = LLVMGetNamedFunction(v.mod, functionName);
        if (func == null)
            func = LLVMAddFunction(v.mod, functionName, functionType);
        return func;
    }

    public LLVMTypeRef structType(LLVMTypeRef... types) {
        return LLVMStructTypeInContext(
                v.context, new PointerPointer<>(types), types.length, /*packed*/ 0);
    }

    /**
     * If the global is already in the module, return it, otherwise add it to
     * the module and return it.
     */
    public LLVMValueRef getGlobal(String globalName, LLVMTypeRef globalType) {
        LLVMValueRef global = LLVMGetNamedGlobal(v.mod, globalName);
        if (global == null)
            global = LLVMAddGlobal(v.mod, globalType, globalName);
        return global;
    }

    public LLVMValueRef buildGEP(LLVMValueRef ptr, LLVMValueRef... indices) {
        // TODO: If safe to do so, might be better to use LLVMBuildInBoundsGEP.
        return LLVMBuildGEP(v.builder, ptr, new PointerPointer<>(indices), indices.length, "gep");
    }

    public LLVMValueRef buildStructGEP(LLVMValueRef ptr, int... intIndices) {
        // LLVM suggests using i32 offsets for struct GEP instructions.
        LLVMValueRef[] indices = IntStream.of(intIndices)
                .mapToObj(i -> LLVMConstInt(LLVMInt32TypeInContext(v.context),
                        i, /* sign-extend */ 0))
                .toArray(LLVMValueRef[]::new);
        return LLVMBuildGEP(v.builder, ptr, new PointerPointer<>(indices), indices.length, "gep");
    }

    /**
     * Obtains the LLVM type that Java values of type {@code t} have after
     * translated to LLVM.
     *
     * @param t the Java type
     * @return an LLVM type reference
     */
    public LLVMTypeRef toLL(final Type t) {
        if (t.isBoolean()) {
            return LLVMInt1TypeInContext(v.context);
        }
        else if (t.isLongOrLess()) {
            return LLVMIntTypeInContext(v.context, numBitsOfIntegralType(t.toPrimitive()));
        }
        else if (t.isVoid()) {
            return LLVMVoidTypeInContext(v.context);
        }
        else if (t.isFloat()) {
            return LLVMFloatTypeInContext(v.context);
        }
        else if (t.isDouble()) {
            return LLVMDoubleTypeInContext(v.context);
        }
        else if (t.isNull()) {
            return llvmBytePtr();
        }
        else if (t.isReference()) {
            return ptrTypeRef(v.obj.structTypeRef(t.toReference()));
        }
        else {
            throw new InternalCompilerError("Unhandled type " + t.getClass());
        }
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
        return getGlobal(v.mangler.idvGlobalId(intf, clazz), toIDVTy(intf));
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
        LLVMTypeRef idv_ty = getOrCreateNamedOpaqueStruct(mangledDVName);
        if (LLVMIsOpaqueStruct(idv_ty) != 0)
            setStructBody(idv_ty, toIDVTySlots(intf));
        return idv_ty;
    }

    /**
     * The LLVM types of each slot in the LLVM structure representation of the
     * interface dispatch vector for Java interface type {@code intf}.
     *
     * @param intf The Java interface type (not required to be erasure)
     * @return an array of LLVM types that correspond to the slots in the
     *         interface dispatch vector for Java type {@code jt}.
     */
    private LLVMTypeRef[] toIDVTySlots(ClassType intf) {
        List<MethodInstance> methods = v.idvMethods(erasureLL(intf));
        LLVMTypeRef[] res = new LLVMTypeRef[Constants.INTF_DISP_VEC_OFFSET + methods.size()];
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
        return getGlobal(v.mangler.idvArrGlobalId(clazz),
                LLVMArrayType(llvmBytePtr(), length));
    }

    public LLVMValueRef toIDVIdArrGlobal(ReferenceType clazz, int length) {
        return getGlobal(v.mangler.idvIdArrGlobalId(clazz),
                LLVMArrayType(llvmBytePtr(), length));
    }

    public LLVMValueRef toIDVIdHashArrGlobal(ReferenceType clazz, int length) {
        return getGlobal(v.mangler.idvIdHashArrGlobalId(clazz),
                LLVMArrayType(LLVMInt32TypeInContext(v.context), length));
    }

    /**
     * Returns the LLVM representation of each slot in the interface dispatch
     * vector of Java class type {@code clazz} for Java interface type
     * {@code intf}.
     *
     * @param intf a Java interface type
     *             {@link LLVMTranslator#allInterfaces(ClassType) implemented} by
     *             {@code clazz}
     * @param clazz the non-abstract Java class type
     */
    public LLVMValueRef[] toIDVSlots(ClassType intf, ClassType clazz) {
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
            LLVMValueRef funcVal = getFunction(
                    v.mangler.mangleProcName(cdvM), cdvM_LLTy);
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
     * @return The LLVM function type whose argument types are the translation
     *         of {@code recvTy} followed by the translation of
     *         {@code formalTys}, and whose return type is the translation of
     *         {@code retTy};
     */
    public LLVMTypeRef toLLFuncTy(
            ReferenceType recvTy, Type retTy, List<? extends Type> formalTys) {
        LLVMTypeRef[] arg_tys = Stream
                .of(CollectUtils.toArray(recvTy, formalTys, Type.class))
                .map(this::toLL)
                .toArray(LLVMTypeRef[]::new);
        LLVMTypeRef ret_ty = toLL(retTy);
        return functionType(ret_ty, arg_tys);
    }

    /**
     * @return The LLVM function type whose argument types are the translation
     *         of {@code formalTys} and whose return type is the translation of
     *         {@code retTy};
     */
    public LLVMTypeRef toLLFuncTy(Type retTy, List<? extends Type> formalTys) {
        LLVMTypeRef[] arg_tys = formalTys.stream()
                .map(this::toLL)
                .toArray(LLVMTypeRef[]::new);
        LLVMTypeRef ret_ty = toLL(retTy);
        return functionType(ret_ty, arg_tys);
    }

    /**
     * Returns the bitcast to the LLVM translation of a Java type. It is used to
     * eliminate any potential mismatch between the LLVM type of the translation
     * of a Java expression and the LLVM translation of the Java expression's
     * type.
     *
     * @param val the LLVM translation of a Java expression (an l-value)
     * @param jty the Java type of the Java expression
     */
    public LLVMValueRef toBitcastL(LLVMValueRef val, Type jty) {
        return LLVMBuildBitCast(v.builder, val, ptrTypeRef(toLL(jty)), "cast_l");
    }

    public LLVMValueRef buildConstArray(LLVMTypeRef type,
            LLVMValueRef... values) {
        return LLVMConstArray(type, new PointerPointer<>(values), values.length);
    }

    /** Returns an anonymous constant struct. */
    public LLVMValueRef buildConstStruct(LLVMValueRef... values) {
        PointerPointer<LLVMValueRef> valArr = new PointerPointer<>(values);
        return LLVMConstStructInContext(v.context, valArr, values.length, /* packed */ 0);
    }

    /**
     * Returns a named constant struct.
     * This is necessary for building initializer structs for variables with a named type, since
     * otherwise the type of the variable and the type of the value will be considered distinct.
     */
    public LLVMValueRef buildNamedConstStruct(LLVMTypeRef type, LLVMValueRef... values) {
        PointerPointer<LLVMValueRef> valArr = new PointerPointer<>(values);
        return LLVMConstNamedStruct(type, valArr, values.length);
    }

    /**
     * Returns the number of bytes occupied by a value of Java type {@code t}.
     * @param t the Java type (not required to be erasure)
     */
    public int sizeOfType(Type t) {
        Type erased = erasureLL(t);
        if (erased.isBoolean()) {
            return 1;
        }
        else if (erased.isFloat()) {
            return 4; // Specified by Java.
        }
        else if (erased.isDouble()) {
            return 8; // Specified by Java.
        }
        else if (erased.isLongOrLess()) {
            PrimitiveType integral = erased.toPrimitive();
            assert numBitsOfIntegralType(integral) % 8 == 0 : "integer bits must be multiple of 8";
            return numBitsOfIntegralType(integral) / 8;
        }
        else if (erased.isNull() || erased.isArray() || erased.isClass()) {
            return llvmPtrSize();
        }
        else {
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
        throw new InternalCompilerError("Type " + t + " is not an integral type");
    }

}
