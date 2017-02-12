package polyllvm.util;

import org.bytedeco.javacpp.PointerPointer;
import polyglot.ast.ClassDecl;
import polyglot.ast.TypeNode;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;
import polyglot.util.Pair;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMFunctionType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMStructureType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.extension.ClassObjects;
import polyllvm.visit.PseudoLLVMTranslator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.bytedeco.javacpp.LLVM.*;

public class LLVMUtils {

    public static LLVMTypeRef llvmPtrSizedIntType() {
        return LLVMInt64Type();
    }

    public static LLVMTypeRef ptrTypeRef(LLVMTypeRef elemType) {
        return LLVMPointerType(elemType, Constants.LLVM_ADDR_SPACE);
    }

    private static LLVMTypeRef structTypeRef(String mangledName, LLVMModuleRef mod) {
        LLVMTypeRef res = LLVMGetTypeByName(mod, mangledName);
        if (res == null)
            res = LLVMStructCreateNamed(LLVMGetGlobalContext(), mangledName);
        return res;
    }

    public static LLVMTypeRef typeRef(Type t, LLVMModuleRef mod) {
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
            return ptrTypeRef(structTypeRef(Constants.ARR_CLASS, mod));
        } else if (t.isClass()) {
            String mangledName = PolyLLVMMangler.classTypeName(t.toReference());
            return ptrTypeRef(structTypeRef(mangledName, mod));
        } else if (t.isNull()) {
            return ptrTypeRef(LLVMInt8Type());
        } else throw new InternalCompilerError("Invalid type");
    }

    // TODO: Delete this
    public static LLVMTypeNode polyLLVMTypeNode(PolyLLVMNodeFactory nf, Type t) {
        if (t.isByte()) {
            return nf.LLVMIntType(8);
        }
        else if (t.isChar() || t.isShort()) {
            return nf.LLVMIntType(16);
        }
        else if (t.isInt()) {
            return nf.LLVMIntType(32);
        }
        else if (t.isLong()) {
            return nf.LLVMIntType(64);
        }
        else if (t.isVoid()) {
            return nf.LLVMVoidType();
        }
        else if (t.isBoolean()) {
            return nf.LLVMIntType(1);
        }
        else if (t.isFloat()) {
            return nf.LLVMFloatType();
        }
        else if (t.isDouble()) {
            return nf.LLVMDoubleType();
        }
        else if (t.isArray()) {
            ArrayType arrayType = t.toArray();
            if (arrayType.base().isReference()) {
                String classTypeName = Constants.ARR_CLASS;
                return nf.LLVMPointerType(nf.LLVMVariableType(classTypeName));
            }
            else if (arrayType.base().isPrimitive()) {
                //TODO : Change to depend on primitive type
                String classTypeName = Constants.ARR_CLASS;
                return nf.LLVMPointerType(nf.LLVMVariableType(classTypeName));

            }
            else {
                throw new InternalCompilerError("Array Type not handled : "
                        + arrayType);
            }
        }
        else if (t.isClass()) {
            String classTypeName = PolyLLVMMangler.classTypeName(t.toReference());
            return nf.LLVMPointerType(nf.LLVMVariableType(classTypeName));
        }
        else if (t.isNull()) {
            //TODO: Figure out something better
            return nf.LLVMPointerType(nf.LLVMIntType(8));
        }
        else {
            try {
                throw new InternalCompilerError("Only integral types,"
                        + " Boolean types, float, double,"
                        + " void, and classes currently supported, not \"" + t
                        + "\".");
            }
            catch (InternalCompilerError e) {
                System.out.println(e
                        + "\n    (For more info go to LLVMUtils"
                        + " and print the stack trace)");
            }
            return null;
        }
    }

    public static LLVMTypeRef functionType(LLVMTypeRef ret, LLVMTypeRef ...args) {
        return LLVMFunctionType(ret, new PointerPointer<>(args), args.length, /* isVarArgs */ 0);
    }

    // TODO: Just make one that takes in a procedure decl.
    public static LLVMTypeRef functionType(Type returnType, List<? extends Type> formalTypes,
                                           LLVMModuleRef mod) {
        LLVMTypeRef[] args = formalTypes.stream()
                .map(t -> typeRef(t, mod))
                .toArray(LLVMTypeRef[]::new);
        return functionType(typeRef(returnType, mod), args);
    }

    public static LLVMTypeRef methodType(ReferenceType type,
                                         Type returnType,
                                         List<? extends Type> formalTypes,
                                         LLVMModuleRef mod) {
        LLVMTypeRef[] args = Stream.concat(
                    Stream.of(typeRef(type, mod)),
                    formalTypes.stream().map(t -> typeRef(t, mod)))
                .toArray(LLVMTypeRef[]::new);
        return functionType(typeRef(returnType, mod), args);
    }

    public static LLVMValueRef buildProcedureCall(LLVMBuilderRef builder,
                                                  LLVMValueRef func,
                                                  LLVMValueRef ...args) {
        return LLVMBuildCall(builder, func, new PointerPointer<>(args), args.length, "");
    }

    public static LLVMValueRef buildMethodCall(LLVMBuilderRef builder,
                                               LLVMValueRef func,
                                               LLVMValueRef ...args) {
        return LLVMBuildCall(builder, func, new PointerPointer<>(args), args.length, "call");
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

    // TODO
    public static LLVMFunctionType polyLLVMFunctionTypeNode(
            PolyLLVMNodeFactory nf, List<? extends Type> formalTypes,
            Type returnType) {
        List<LLVMTypeNode> formals = new ArrayList<>();
        for (Type type : formalTypes) {
            formals.add(polyLLVMTypeNode(nf, type));
        }
        return nf.LLVMFunctionType(formals, polyLLVMTypeNode(nf, returnType));
    }

    public static LLVMTypeNode polyLLVMMethodTypeNode(PolyLLVMNodeFactory nf,
            ReferenceType type, List<? extends Type> formalTypes,
            Type returnType) {
        LLVMTypeNode classTypePointer =
                nf.LLVMPointerType(nf.LLVMVariableType(PolyLLVMMangler.classTypeName(type)));
        return LLVMUtils.polyLLVMFunctionTypeNode(nf,
                                                          formalTypes,
                                                          returnType)
                                .prependFormalTypeNode(classTypePointer);
    }

    public static LLVMTypeNode polyLLVMObjectType(PseudoLLVMTranslator v,
            ReferenceType rt) {
        Pair<List<MethodInstance>, List<FieldInstance>> layouts = v.layouts(rt);
        List<LLVMTypeNode> typeList = new ArrayList<>();
        typeList.add(v.nodeFactory()
                      .LLVMPointerType(polyLLVMDispatchVectorVariableType(v,
                                                                          rt)));
        for (FieldInstance f : layouts.part2()) {
            typeList.add(polyLLVMTypeNode(v.nodeFactory(), f.type()));
        }
        LLVMStructureType structureType =
                v.nodeFactory().LLVMStructureType(typeList);

        return structureType;
    }

    public static LLVMTypeNode polyLLVMObjectType(PseudoLLVMTranslator v,
            ClassDecl cd) {
        return polyLLVMObjectType(v, cd.type());
    }

    public static LLVMTypeNode polyLLVMObjectType(PseudoLLVMTranslator v,
            TypeNode superClass) {
        return polyLLVMObjectType(v, (ReferenceType) superClass.type());
    }

    public static LLVMTypeNode polyLLVMDispatchVectorType(
            PseudoLLVMTranslator v, TypeNode superClass) {
        return polyLLVMDispatchVectorType(v, (ReferenceType) superClass.type());

    }

    public static LLVMTypeNode polyLLVMDispatchVectorType(
            PseudoLLVMTranslator v, ClassDecl cd) {
        return polyLLVMDispatchVectorType(v, cd.type());
    }

    public static LLVMTypeNode polyLLVMDispatchVectorType(PseudoLLVMTranslator v,
                                                          ReferenceType type) {
        PolyLLVMNodeFactory nf = v.nodeFactory();
        List<MethodInstance> layout = v.layouts(type).part1();
        List<LLVMTypeNode> typeList = new ArrayList<>();
        typeList.add(nf.LLVMPointerType(nf.LLVMIntType(8)));

        // Class dispatch vectors and interface tables currently differ in their second entry.
        if (v.isInterface(type)) {
            typeList.add(nf.LLVMPointerType(nf.LLVMIntType(8)));
        } else {
            typeList.add(nf.LLVMPointerType(ClassObjects.classObjType(nf, type)));
        }

        for (MethodInstance mi : layout) {
            LLVMTypeNode classTypePointer =
                    nf.LLVMPointerType(nf.LLVMVariableType(PolyLLVMMangler.classTypeName(type)));
            LLVMTypeNode funcType =
                    LLVMUtils.polyLLVMFunctionTypeNode(nf, mi.formalTypes(), mi.returnType())
                                     .prependFormalTypeNode(classTypePointer);

            typeList.add(funcType);
        }

        return nf.LLVMStructureType(typeList);
    }

    public static LLVMTypeNode polyLLVMDispatchVectorVariableType(
            PseudoLLVMTranslator v, ReferenceType rt) {
        return v.nodeFactory()
                .LLVMVariableType(PolyLLVMMangler.dispatchVectorTypeName(rt));
    }

    public static LLVMTypeNode polyLLVMObjectVariableType(
            PseudoLLVMTranslator v, ReferenceType rt) {
        return v.nodeFactory()
                .LLVMVariableType(PolyLLVMMangler.classTypeName(rt));
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

    public static LLVMTypeNode polyLLVMMethodTypeNode(PolyLLVMNodeFactory nf,
            ReferenceType container, List<? extends Type> formalTypes) {
        List<LLVMTypeNode> formals = new ArrayList<>();
        for (Type type : formalTypes) {
            formals.add(polyLLVMTypeNode(nf, type));
        }
        LLVMTypeNode classTypePointer =
                nf.LLVMPointerType(nf.LLVMVariableType(PolyLLVMMangler.classTypeName(container)));
        return nf.LLVMFunctionType(formals, nf.LLVMVoidType())
                 .prependFormalTypeNode(classTypePointer);

    }
}
