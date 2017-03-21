package polyllvm.util;

import org.bytedeco.javacpp.PointerPointer;
import polyglot.ext.jl5.types.*;
import polyglot.ext.jl5.types.inference.LubType;
import polyglot.ext.param.types.Subst;
import polyglot.types.*;
import polyglot.types.reflect.Method;
import polyglot.util.InternalCompilerError;
import polyglot.util.Pair;
import polyllvm.visit.LLVMTranslator;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.bytedeco.javacpp.LLVM.*;

public class LLVMUtils {
    private final LLVMTranslator v;

    public LLVMUtils(LLVMTranslator v) {
        this.v = v;
    }

    public ReferenceType getArrayType() {
        try {
            return (ReferenceType) v.typeSystem().typeForName("support.Array");
        } catch (SemanticException|ClassCastException e) {
            throw new InternalCompilerError("Could not load array type");
        }
    }

    public LLVMTypeRef llvmPtrSizedIntType() {
        return LLVMIntTypeInContext(v.context, llvmPtrSize());
    }

    public static int llvmPtrSize() {
        return 64;
    }

    public LLVMTypeRef llvmBytePtr() {
        return ptrTypeRef(LLVMInt8TypeInContext(v.context));
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

    private LLVMTypeRef structTypeRef(ReferenceType rt) {
        return structTypeRef(rt, true);
    }


    private LLVMTypeRef structTypeRef(ReferenceType rt, boolean fillInStruct) {
        rt = v.jl5Utils.translateType(rt);
        String mangledName = v.mangler.classTypeName(rt);
        LLVMTypeRef structType = structTypeRefOpaque(mangledName);
        if (LLVMIsOpaqueStruct(structType) != 0 && fillInStruct) {
            setStructBody(structType); // Set the struct to be empty, so it is not opaque
            setStructBody(structType, objectFieldTypes(rt));
        }
        dvTypeRef(rt); //Make sure DV is added to module if class is being used
        return ptrTypeRef(structType);
    }

    public LLVMTypeRef dvTypeRef(ReferenceType rt) {
        rt = v.jl5Utils.translateType(rt);
        String mangledDVName = v.mangler.dispatchVectorTypeName(rt);
        LLVMTypeRef dvType = structTypeRefOpaque(mangledDVName);
        if (LLVMIsOpaqueStruct(dvType) != 0) {
            setStructBody(dvType); // Set the struct to be empty, so it is not opaque
            setStructBody(dvType, dvMethodTypes(rt));
        }
        return dvType;
    }


    public LLVMTypeRef typeRef(Type t) {
        return typeRef(t, true);
    }

    private LLVMTypeRef typeRef(Type t, boolean fillInStruct) {
        t = v.jl5Utils.translateType(t);

        if      (t.isBoolean())    return LLVMInt1TypeInContext(v.context);
        else if (t.isLongOrLess()) return LLVMIntTypeInContext(v.context, numBitsOfIntegralType(t));
        else if (t.isVoid())       return LLVMVoidTypeInContext(v.context);
        else if (t.isFloat())      return LLVMFloatType();
        else if (t.isDouble())     return LLVMDoubleType();
        else if (t.isClass())      return structTypeRef(t.toReference(), fillInStruct);
        else if (t.isNull())       return ptrTypeRef(LLVMInt8TypeInContext(v.context));
        else if (t.isArray()) {
            structTypeRef(getArrayType(), fillInStruct);
            return ptrTypeRef(structTypeRefOpaque(Constants.ARR_CLASS));
        }
        else if (t.isReference())  return structTypeRef(t.toReference(), fillInStruct);
        else {
            throw new InternalCompilerError("Invalid type");
        }
    }


    public LLVMTypeRef functionType(LLVMTypeRef ret, LLVMTypeRef ...args) {
        return LLVMFunctionType(ret, new PointerPointer<>(args), args.length, /* isVarArgs */ 0);
    }

    // TODO: Just make one that takes in a procedure decl.
    public LLVMTypeRef functionType(Type returnType, List<? extends Type> formalTypes) {
        LLVMTypeRef[] args = formalTypes.stream()
                .map(t -> typeRef(t, false))
                .toArray(LLVMTypeRef[]::new);
        return functionType(typeRef(returnType, false), args);
    }

    public LLVMTypeRef methodType(ReferenceType type,
                                  Type returnType,
                                  List<? extends Type> formalTypes) {
        LLVMTypeRef[] args = Stream.concat(
                    Stream.of(typeRef(type, false)),
                    formalTypes.stream().map(t -> typeRef(t, false)))
                .toArray(LLVMTypeRef[]::new);
        return functionType(typeRef(returnType, false), args);
    }



    public LLVMValueRef buildProcedureCall(LLVMValueRef func, LLVMValueRef... args) {
        if (v.inTry() && !Constants.NON_INVOKE_FUNCTIONS.contains(LLVMGetValueName(func).getString())) {
            LLVMBasicBlockRef invokeCont = LLVMAppendBasicBlockInContext(v.context, v.currFn(), "invoke.cont");
            LLVMValueRef invoke = LLVMBuildInvoke(v.builder, func, new PointerPointer<>(args), args.length, invokeCont, v.currLpad(), "");
            LLVMPositionBuilderAtEnd(v.builder, invokeCont);
            return invoke;
        }
        return LLVMBuildCall(v.builder, func, new PointerPointer<>(args), args.length, "");
    }

    public LLVMValueRef buildMethodCall(LLVMValueRef func, LLVMValueRef... args) {
        if (v.inTry() && !Constants.NON_INVOKE_FUNCTIONS.contains(LLVMGetValueName(func).getString())) {
            LLVMBasicBlockRef invokeCont = LLVMAppendBasicBlockInContext(v.context, v.currFn(), "invoke.cont");
            LLVMValueRef invoke = LLVMBuildInvoke(v.builder, func, new PointerPointer<>(args), args.length, invokeCont, v.currLpad(), "call");
            LLVMPositionBuilderAtEnd(v.builder, invokeCont);
            return invoke;
        }
        return LLVMBuildCall(v.builder, func, new PointerPointer<>(args), args.length, "call");
    }

    /**
     * If the function is already in the module, return it, otherwise add it to the module and return it.
     */
    public LLVMValueRef getFunction(LLVMModuleRef mod, String functionName, LLVMTypeRef functionType) {
        LLVMValueRef func = LLVMGetNamedFunction(mod, functionName);
        if (func == null)
            func = LLVMAddFunction(mod, functionName, functionType);
        return func;
    }

    public LLVMValueRef funcRef(LLVMModuleRef mod,
                                       ProcedureInstance pi,
                                       LLVMTypeRef funcType) {
        return getFunction(mod, v.mangler.mangleProcedureName(pi), funcType);
    }

    public LLVMTypeRef structType(LLVMTypeRef... types) {
        return LLVMStructType(new PointerPointer<>(types), types.length, /*Packed*/ 0);
    }

    /**
     * If the global is already in the module, return it, otherwise add it to the module and return it.
     */
    public LLVMValueRef getGlobal(LLVMModuleRef mod, String globalName, LLVMTypeRef globalType) {
        LLVMValueRef global = LLVMGetNamedGlobal(mod,globalName);
        if (global == null)
            global = LLVMAddGlobal(mod, globalType, globalName);
        return global;
    }

    public LLVMValueRef buildGEP(LLVMValueRef ptr, LLVMValueRef... indices) {
        // TODO: If safe to do so, might be better to use LLVMBuildInBoundsGEP.
        return LLVMBuildGEP(v.builder, ptr, new PointerPointer<>(indices), indices.length, "gep");
    }

    /**
     * Create a constant GEP using i32 indices from indices
     */
    public LLVMValueRef constGEP(LLVMValueRef ptr, int ...indices) {
        LLVMValueRef[] llvmIndices = Arrays.stream(indices)
                .mapToObj(i -> LLVMConstInt(LLVMInt32TypeInContext(v.context), i, /*sign-extend*/ 0))
                .toArray(LLVMValueRef[]::new);
        return LLVMConstGEP(ptr, new PointerPointer<>(llvmIndices), llvmIndices.length);
    }


    public LLVMValueRef buildStructGEP(LLVMValueRef ptr, int... intIndices) {
        // LLVM suggests using i32 offsets for struct GEP instructions.
        LLVMValueRef[] indices = IntStream.of(intIndices)
                .mapToObj(i -> LLVMConstInt(LLVMInt32TypeInContext(v.context), i, /* sign-extend */ 0))
                .toArray(LLVMValueRef[]::new);
        return LLVMBuildGEP(v.builder, ptr, new PointerPointer<>(indices), indices.length, "gep");
    }

    /**
     * Return a pointer to the first element in a Java array.
     */
    public LLVMValueRef buildJavaArrayBase(LLVMValueRef arr, Type elemType) {
        LLVMValueRef baseRaw = v.utils.buildStructGEP(arr, 0, Constants.ARR_ELEM_OFFSET);
        LLVMTypeRef ptrType = v.utils.ptrTypeRef(v.utils.typeRef(elemType, false));
        return LLVMBuildCast(v.builder, LLVMBitCast, baseRaw, ptrType, "arr_cast");
    }

    private void setStructBody(LLVMTypeRef struct, LLVMTypeRef... types) {
        LLVMStructSetBody(struct, new PointerPointer<>(types), types.length, /* packed */ 0);
    }

    private LLVMTypeRef[] objectFieldTypes(ReferenceType rt) {
        Pair<List<MethodInstance>, List<FieldInstance>> layouts = v.layouts(rt);
        LLVMTypeRef dvType = structTypeRefOpaque(v.mangler.dispatchVectorTypeName(rt));
        LLVMTypeRef dvPtrType = LLVMPointerType(dvType, Constants.LLVM_ADDR_SPACE);
        return Stream.concat(
                Stream.of(dvPtrType),
                layouts.part2().stream().map(fi -> typeRef(fi.type(), false))
        ).toArray(LLVMTypeRef[]::new);
    }

    public void setupArrayType() {
        LLVMTypeRef[] fieldTypes = objectFieldTypes(getArrayType());
        fieldTypes = Arrays.copyOf(fieldTypes, fieldTypes.length + 1);
        fieldTypes[fieldTypes.length-1] = ptrTypeRef(LLVMInt8TypeInContext(v.context));

        String mangledName = v.mangler.classTypeName(getArrayType());
        LLVMTypeRef structType = structTypeRefOpaque(mangledName);
        setStructBody(structType, fieldTypes);
        dvTypeRef(getArrayType());
    }

    private LLVMTypeRef[] dvMethodTypes(ReferenceType rt) {
        List<MethodInstance> layout = v.layouts(rt).part1();
        List<LLVMTypeRef> typeList = new ArrayList<>();
        typeList.add(ptrTypeRef(LLVMInt8TypeInContext(v.context)));

        // Class dispatch vectors and interface tables currently differ in their second entry.
        if (v.isInterface(rt)) {
            typeList.add(ptrTypeRef(LLVMInt8TypeInContext(v.context)));
        } else {
            typeList.add(ptrTypeRef(v.classObjs.classObjTypeRef(rt)));
        }

        layout.stream().map(mi -> ptrTypeRef(methodType(rt, mi.returnType(), mi.formalTypes())))
                .forEach(typeList::add);

        LLVMTypeRef[] types = new LLVMTypeRef[typeList.size()];
        return typeList.toArray(types);

    }

    public LLVMValueRef getDvGlobal(ReferenceType classtype) {
        classtype = v.jl5Utils.translateType(classtype);
        return getGlobal(v.mod, v.mangler.dispatchVectorVariable(classtype), dvTypeRef(classtype));
    }

    public LLVMValueRef getItGlobal(ReferenceType it, ReferenceType usingClass) {
        it = v.jl5Utils.translateType(it);
        usingClass = v.jl5Utils.translateType(usingClass);

        String interfaceTableVar = v.mangler.InterfaceTableVariable(usingClass, it);
        LLVMTypeRef interfaceTableType = dvTypeRef(it);
        return getGlobal(v.mod, interfaceTableVar, interfaceTableType);
    }

    public LLVMValueRef[] dvMethods(ReferenceType rt, LLVMValueRef next) {
        rt = v.jl5Utils.translateType(rt);

        List<MethodInstance> layout = v.layouts(rt).part1();
        ReferenceType finalRt = rt;
        LLVMValueRef[] methods = Stream.concat(
                Stream.of(next, v.classObjs.classObjRef(v.mod, rt)),
                IntStream.range(0, layout.size()).mapToObj(i -> {
                    MethodInstance mi = layout.get(i);
                    LLVMValueRef function = getFunction(v.mod, v.mangler.mangleProcedureName(mi),
                            methodType(mi.container(), mi.returnType(), mi.formalTypes()));
                    return LLVMConstBitCast(function, ptrTypeRef(methodType(finalRt, mi.returnType(), mi.formalTypes())));
                })).toArray(LLVMValueRef[]::new);
        return methods;
    }

    public LLVMValueRef[] itMethods(ReferenceType it, ReferenceType usingClass, LLVMValueRef next) {
        it = v.jl5Utils.translateType(it);
        usingClass = v.jl5Utils.translateType(usingClass);

        List<MethodInstance> layout = v.layouts(it).part1();
        for (int i=0; i< layout.size(); i++) {
            List<MethodInstance> classLayout = v.layouts(usingClass).part1();
            MethodInstance mi = layout.get(i);
            MethodInstance miClass = v.methodInList(mi, classLayout);
            layout.set(i, miClass);
        }
        ReferenceType finalIt = it;
        LLVMValueRef[] methods = Stream.concat(
                Stream.of(next, v.classObjs.classObjRef(v.mod, it)),
                IntStream.range(0, layout.size()).mapToObj(i -> {
                    MethodInstance mi = layout.get(i);
                    LLVMValueRef function = getFunction(v.mod, v.mangler.mangleProcedureName(mi),
                            methodType(mi.container(), mi.returnType(), mi.formalTypes()));
                    return LLVMConstBitCast(function, ptrTypeRef(methodType(finalIt, mi.returnType(), mi.formalTypes())));
                })).toArray(LLVMValueRef[]::new);
        return methods;
    }

    public LLVMValueRef[] dvMethods(ReferenceType rt) {
        rt = v.jl5Utils.translateType(rt);

        return dvMethods(rt, LLVMConstNull(ptrTypeRef(LLVMInt8TypeInContext(v.context))));
    }

    public LLVMValueRef buildConstArray(LLVMTypeRef type, LLVMValueRef ...values) {
        return LLVMConstArray(type, new PointerPointer<>(values), values.length);
    }

    public LLVMValueRef buildConstStruct(LLVMValueRef ...values) {
        PointerPointer<LLVMValueRef> valArr = new PointerPointer<>(values);
        return LLVMConstStructInContext(v.context, valArr, values.length, /*packed*/ 0);
    }

    private int numBitsOfIntegralType(Type t) {
        if      (t.isByte())  return 8;
        else if (t.isShort()) return 16;
        else if (t.isChar())  return 16;
        else if (t.isInt())   return 32;
        else if (t.isLong())  return 64;
        throw new InternalCompilerError("Type " + t + " is not an integral type");
    }

    /**
     * Return the number of bytes needed to store type {@code t}
     */
    public int sizeOfType(Type t) {
        if      (t.isBoolean()) return 1;
        else if (t.isFloat())   return 4; // Specified by Java.
        else if (t.isDouble())  return 8; // Specified by Java.
        else if (t.isArray())   return llvmPtrSize();
        else if (t.isClass())   return llvmPtrSize();
        else if (t.isNull())    return llvmPtrSize();
        else if (t.isLongOrLess()) {
            assert numBitsOfIntegralType(t) % 8 == 0 : "integer bit count must be multiple of 8";
            return numBitsOfIntegralType(t) / 8;
        } else {
            throw new InternalCompilerError("Invalid type");
        }
    }
}
