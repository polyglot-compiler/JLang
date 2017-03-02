package polyllvm.util;

import org.bytedeco.javacpp.PointerPointer;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;
import polyglot.util.Pair;
import polyllvm.extension.ClassObjects;
import polyllvm.visit.PseudoLLVMTranslator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.bytedeco.javacpp.LLVM.*;

public class LLVMUtils {

    public static LLVMTypeRef llvmPtrSizedIntType() {
        return LLVMIntType(llvmPtrSize());
    }

    public static int llvmPtrSize() {
        return 64;
    }

    public static LLVMTypeRef llvmBytePtr() {
        return ptrTypeRef(LLVMInt8Type());
    }


    public static LLVMTypeRef ptrTypeRef(LLVMTypeRef elemType) {
        return LLVMPointerType(elemType, Constants.LLVM_ADDR_SPACE);
    }

    private static LLVMTypeRef structTypeRefOpaque(String mangledName, LLVMModuleRef mod) {
        LLVMTypeRef res = LLVMGetTypeByName(mod, mangledName);
        if (res == null)
            res = LLVMStructCreateNamed(LLVMGetGlobalContext(), mangledName);
        return res;
    }

    private static LLVMTypeRef structTypeRef(ReferenceType rt, PseudoLLVMTranslator v) {
        String mangledName = PolyLLVMMangler.classTypeName(rt);
        LLVMTypeRef structType = structTypeRefOpaque(mangledName, v.mod);
        if(LLVMIsOpaqueStruct(structType) != 0){
            setStructBody(structType); // Set the struct to be empty, so it is not opaque
            setStructBody(structType, objectFieldTypes(v, rt));
        }
        dvTypeRef(rt,v); //Make sure DV is added to module if class is being used
        return ptrTypeRef(structType);
    }

    public static LLVMTypeRef dvTypeRef(ReferenceType rt, PseudoLLVMTranslator v) {
        String mangledDVName = PolyLLVMMangler.dispatchVectorTypeName(rt);
        LLVMTypeRef dvType = structTypeRefOpaque(mangledDVName, v.mod);
        if(LLVMIsOpaqueStruct(dvType) != 0){
            setStructBody(dvType); // Set the struct to be empty, so it is not opaque
            setStructBody(dvType, dvMethodTypes(v, rt));
        }
        return dvType;
    }


    public static LLVMTypeRef typeRef(Type t, PseudoLLVMTranslator v) {
        if (t.isBoolean()) {
            return LLVMInt1Type();
        } else if (t.isLongOrLess()) {
            return LLVMIntType(numBitsOfIntegralType(t));
        } else if (t.isVoid()) {
            return LLVMVoidType();
        } else if (t.isFloat()) {
            return LLVMFloatType();
        } else if (t.isDouble()) {
            return LLVMDoubleType();
        } else if (t.isArray()) {
            structTypeRef(v.getArrayType(), v);
            return ptrTypeRef(structTypeRefOpaque(Constants.ARR_CLASS, v.mod));
        } else if (t.isClass()) {
            return structTypeRef(t.toReference(), v);
        } else if (t.isNull()) {
            return ptrTypeRef(LLVMInt8Type());
        } else throw new InternalCompilerError("Invalid type");
    }

    public static LLVMValueRef defaultValue(Type t, PseudoLLVMTranslator v) {
        if (t.isBoolean()) {
            return LLVMConstInt(LLVMInt1Type(), 0, 0);
        } else if (t.isLongOrLess()) {
            return LLVMConstInt(typeRef(t, v), 0, 0);
        } else if (t.isFloat()) {
            return LLVMConstReal(LLVMFloatType(), 0);
        } else if (t.isDouble()) {
            return LLVMConstReal(LLVMDoubleType(), 0);
        } else if (t.isClass() || t.isNull()) {
            return LLVMConstNull(typeRef(t,v));
        } else throw new InternalCompilerError("Invalid type");
    }

    public static LLVMTypeRef functionType(LLVMTypeRef ret, LLVMTypeRef ...args) {
        return LLVMFunctionType(ret, new PointerPointer<>(args), args.length, /* isVarArgs */ 0);
    }

    // TODO: Just make one that takes in a procedure decl.
    public static LLVMTypeRef functionType(Type returnType, List<? extends Type> formalTypes,
                                           PseudoLLVMTranslator v) {
        LLVMTypeRef[] args = formalTypes.stream()
                .map(t -> typeRef(t, v))
                .toArray(LLVMTypeRef[]::new);
        return functionType(typeRef(returnType, v), args);
    }

    public static LLVMTypeRef methodType(ReferenceType type,
                                         Type returnType,
                                         List<? extends Type> formalTypes,
                                         PseudoLLVMTranslator v) {
        LLVMTypeRef[] args = Stream.concat(
                    Stream.of(typeRef(type, v)),
                    formalTypes.stream().map(t -> typeRef(t, v)))
                .toArray(LLVMTypeRef[]::new);
        return functionType(typeRef(returnType, v), args);
    }

    public static LLVMValueRef buildProcedureCall(PseudoLLVMTranslator v,
                                                  LLVMValueRef func,
                                                  LLVMValueRef ...args) {
        if(v.inTry() && !Constants.NON_INVOKE_FUNCTIONS.contains(LLVMGetValueName(func).getString())){
            LLVMBasicBlockRef invokeCont = LLVMAppendBasicBlock(v.currFn(), "invoke.cont");
            LLVMValueRef invoke = LLVMBuildInvoke(v.builder, func, new PointerPointer<>(args), args.length, invokeCont, v.currLpad(), "");
            LLVMPositionBuilderAtEnd(v.builder, invokeCont);
            return invoke;
        }
        return LLVMBuildCall(v.builder, func, new PointerPointer<>(args), args.length, "");
    }

    public static LLVMValueRef buildMethodCall(PseudoLLVMTranslator v,
                                               LLVMValueRef func,
                                               LLVMValueRef ...args) {
        if(v.inTry() && !Constants.NON_INVOKE_FUNCTIONS.contains(LLVMGetValueName(func).getString())){
            LLVMBasicBlockRef invokeCont = LLVMAppendBasicBlock(v.currFn(), "invoke.cont");
            LLVMValueRef invoke = LLVMBuildInvoke(v.builder, func, new PointerPointer<>(args), args.length, invokeCont, v.currLpad(), "call");
            LLVMPositionBuilderAtEnd(v.builder, invokeCont);
            return invoke;
        }
        return LLVMBuildCall(v.builder, func, new PointerPointer<>(args), args.length, "call");
    }

    /**
     * If the function is already in the module, return it, otherwise add it to the module and return it.
     */
    public static LLVMValueRef getFunction(LLVMModuleRef mod, String functionName, LLVMTypeRef functionType) {
        LLVMValueRef func = LLVMGetNamedFunction(mod, functionName);
        if (func == null) {
            func = LLVMAddFunction(mod, functionName, functionType);
        }
        return func;
    }

    public static LLVMValueRef funcRef(LLVMModuleRef mod,
                                       ProcedureInstance pi,
                                       LLVMTypeRef funcType) {
        return getFunction(mod, PolyLLVMMangler.mangleProcedureName(pi), funcType);
    }

    public static LLVMTypeRef structType(LLVMTypeRef... types) {
        return LLVMStructType(new PointerPointer<>(types), types.length, /*Packed*/ 0);
    }

    /**
     * If the global is already in the module, return it, otherwise add it to the module and return it.
     */
    public static LLVMValueRef getGlobal(LLVMModuleRef mod, String globalName, LLVMTypeRef globalType) {
        LLVMValueRef global = LLVMGetNamedGlobal(mod,globalName);
        if (global == null) {
            global = LLVMAddGlobal(mod, globalType, globalName);
        }
        return global;
    }

    public static LLVMValueRef buildGEP(LLVMBuilderRef builder,
                                        LLVMValueRef ptr,
                                        LLVMValueRef ...indices) {
        // TODO: If safe to do so, might be better to use LLVMBuildInBoundsGEP.
        return LLVMBuildGEP(builder, ptr, new PointerPointer<>(indices), indices.length, "gep");
    }

    /**
     * Create a constant GEP using i32 indices from indices
     */
    public static LLVMValueRef constGEP(LLVMValueRef ptr,
                                        int ...indices) {
        LLVMValueRef[] llvmIndices = Arrays.stream(indices)
                .mapToObj(i -> LLVMConstInt(LLVMInt32Type(), i, /*sign-extend*/ 0))
                .toArray(LLVMValueRef[]::new);
        return LLVMConstGEP(ptr, new PointerPointer<>(llvmIndices), llvmIndices.length);
    }


    public static LLVMValueRef buildStructGEP(LLVMBuilderRef builder,
                                              LLVMValueRef ptr,
                                              long ...longIndices) {
        // LLVM suggests using i32 offsets for struct GEP instructions.
        LLVMValueRef[] indices = LongStream.of(longIndices)
                .mapToObj(i -> LLVMConstInt(LLVMInt32Type(), i, /* sign-extend */ 0))
                .toArray(LLVMValueRef[]::new);
        return LLVMBuildGEP(builder, ptr, new PointerPointer<>(indices), indices.length, "gep");
    }

    private static void setStructBody(LLVMTypeRef struct, LLVMTypeRef... types) {
        LLVMStructSetBody(struct, new PointerPointer<>(types), types.length, /* packed */ 0);
    }

    private static LLVMTypeRef[] objectFieldTypes(PseudoLLVMTranslator v, ReferenceType rt) {
        Pair<List<MethodInstance>, List<FieldInstance>> layouts = v.layouts(rt);
        LLVMTypeRef dvType = structTypeRefOpaque(PolyLLVMMangler.dispatchVectorTypeName(rt), v.mod);
        LLVMTypeRef dvPtrType = LLVMPointerType(dvType, Constants.LLVM_ADDR_SPACE);
        return Stream.concat(
                Stream.of(dvPtrType),
                layouts.part2().stream().map(fi -> typeRef(fi.type(), v))
        ).toArray(LLVMTypeRef[]::new);
    }

    public static void setupArrayType(PseudoLLVMTranslator v){
        LLVMTypeRef[] fieldTypes = objectFieldTypes(v, v.getArrayType());
        fieldTypes = Arrays.copyOf(fieldTypes, fieldTypes.length + 1);
        fieldTypes[fieldTypes.length-1] = ptrTypeRef(LLVMInt8Type());

        String mangledName = PolyLLVMMangler.classTypeName(v.getArrayType());
        LLVMTypeRef structType = structTypeRefOpaque(mangledName, v.mod);
        setStructBody(structType, fieldTypes);
        dvTypeRef(v.getArrayType(),v);
    }

    private static LLVMTypeRef[] dvMethodTypes(PseudoLLVMTranslator v, ReferenceType rt) {
        List<MethodInstance> layout = v.layouts(rt).part1();
        List<LLVMTypeRef> typeList = new ArrayList<>();
        typeList.add(ptrTypeRef(LLVMInt8Type()));

        // Class dispatch vectors and interface tables currently differ in their second entry.
        if (v.isInterface(rt)) {
            typeList.add(ptrTypeRef(LLVMInt8Type()));
        } else {
            typeList.add(ptrTypeRef(ClassObjects.classObjTypeRef(rt)));
        }

        layout.stream().map(mi -> ptrTypeRef(LLVMUtils.methodType(rt, mi.returnType(), mi.formalTypes(), v)))
                .forEach(typeList::add);

        LLVMTypeRef[] types = new LLVMTypeRef[typeList.size()];
        return typeList.toArray(types);

    }

    public static LLVMValueRef getDvGlobal(PseudoLLVMTranslator v, ReferenceType classtype) {
        return LLVMUtils.getGlobal(v.mod, PolyLLVMMangler.dispatchVectorVariable(classtype), LLVMUtils.dvTypeRef(classtype,v));
    }

    public static LLVMValueRef getItGlobal(PseudoLLVMTranslator v, ReferenceType it, ReferenceType usingClass){
        String interfaceTableVar = PolyLLVMMangler.InterfaceTableVariable(usingClass, it);
        LLVMTypeRef interfaceTableType = LLVMUtils.dvTypeRef(it, v);
        return LLVMUtils.getGlobal(v.mod, interfaceTableVar, interfaceTableType);
    }


    public static LLVMValueRef[] dvMethods(PseudoLLVMTranslator v, ReferenceType rt, LLVMValueRef next) {
        List<MethodInstance> layout = v.layouts(rt).part1();
        LLVMValueRef[] methods = Stream.concat(
                Stream.of(next, ClassObjects.classObjRef(v.mod, rt)),
                IntStream.range(0, layout.size()).mapToObj(i -> {
                    MethodInstance mi = layout.get(i);
                    LLVMValueRef function = getFunction(v.mod, PolyLLVMMangler.mangleProcedureName(mi),
                            methodType(mi.container(), mi.returnType(), mi.formalTypes(), v));
                    return LLVMConstBitCast(function, ptrTypeRef(methodType(rt, mi.returnType(), mi.formalTypes(), v)));
                })).toArray(LLVMValueRef[]::new);
        return methods;
    }

    public static LLVMValueRef[] itMethods(PseudoLLVMTranslator v, ReferenceType it, ReferenceType usingClass, LLVMValueRef next) {
        List<MethodInstance> layout = v.layouts(it).part1();
        for (int i=0; i< layout.size(); i++){
            List<MethodInstance> classLayout = v.layouts(usingClass).part1();
            MethodInstance mi = layout.get(i);
            MethodInstance miClass = v.methodInList(mi, classLayout);
            layout.set(i, miClass);

        }
        LLVMValueRef[] methods = Stream.concat(
                Stream.of(next, ClassObjects.classObjRef(v.mod, it)),
                IntStream.range(0, layout.size()).mapToObj(i -> {
                    MethodInstance mi = layout.get(i);
                    LLVMValueRef function = getFunction(v.mod, PolyLLVMMangler.mangleProcedureName(mi),
                            methodType(mi.container(), mi.returnType(), mi.formalTypes(), v));
                    return LLVMConstBitCast(function, ptrTypeRef(methodType(it, mi.returnType(), mi.formalTypes(), v)));
                })).toArray(LLVMValueRef[]::new);
        return methods;
    }



    public static LLVMValueRef[] dvMethods(PseudoLLVMTranslator v, ReferenceType rt) {
        return dvMethods(v, rt, LLVMConstNull(ptrTypeRef(LLVMInt8Type())));
    }

    public static LLVMValueRef buildConstArray(LLVMTypeRef type, LLVMValueRef ...values) {
        return LLVMConstArray(type, new PointerPointer<>(values), values.length);
    }

    public static LLVMValueRef buildConstStruct(LLVMValueRef ...values) {
        return LLVMConstStruct(new PointerPointer<>(values), values.length, /*Unpacked struct*/0);
    }


    public static int numBitsOfIntegralType(Type t) {
        if (t.isByte())
            return 8;
        else if (t.isShort() || t.isChar())
            return 16;
        else if (t.isInt())
            return 32;
        else if (t.isLong())
            return 64;
        throw new InternalCompilerError("Type " + t + " is not an integral type");
    }

    /**
     * Return the number of bytes needed to store type {@code t}
     */
    public static int sizeOfType(Type t) {
        if (t.isBoolean()) {
            return 1;
        } else if (t.isLongOrLess()) {
            assert numBitsOfIntegralType(t) % 8 == 0 : "numBitsOfIntegralType must return a multiple of 8";
            return numBitsOfIntegralType(t)/8;
        } else if (t.isFloat()) {
            return 4; //Specified by java
        } else if (t.isDouble()) {
            return 8; //Specified by Java
        } else if (t.isArray()) {
            return LLVMUtils.llvmPtrSize();
        } else if (t.isClass()) {
            return LLVMUtils.llvmPtrSize();
        } else if (t.isNull()) {
            return LLVMUtils.llvmPtrSize();
        } else throw new InternalCompilerError("Invalid type");

    }

}
