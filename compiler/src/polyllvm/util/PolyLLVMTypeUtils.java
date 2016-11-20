package polyllvm.util;

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

public class PolyLLVMTypeUtils {

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
                String classTypeName = "class.support_Array";
                return nf.LLVMPointerType(nf.LLVMVariableType(classTypeName));
            }
            else if (arrayType.base().isPrimitive()) {
                //TODO : Change to depend on primitive type
                String classTypeName = "class.support_Array";
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
                        + "\n    (For more info go to PolyLLVMTypeUtils"
                        + " and print the stack trace)");
            }
            return null;
        }
    }

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
        return PolyLLVMTypeUtils.polyLLVMFunctionTypeNode(nf,
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
                    PolyLLVMTypeUtils.polyLLVMFunctionTypeNode(nf, mi.formalTypes(), mi.returnType())
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
        else if (t.isLong()) return 64;
        throw new InternalCompilerError("Type " + t
                + " is not an integral type");
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
