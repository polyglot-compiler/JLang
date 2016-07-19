package polyllvm.util;

import java.util.ArrayList;
import java.util.List;

import polyglot.ast.ClassDecl;
import polyglot.ast.TypeNode;
import polyglot.types.FieldInstance;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;
import polyglot.util.Pair;
import polyglot.util.Position;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMFunctionType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMStructureType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMTypeUtils {

    public static LLVMTypeNode polyLLVMTypeNode(PolyLLVMNodeFactory nf,
            Type t) {
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
        else if (t.isClass()) {
            return nf.LLVMVariableType("class." + t.toString());
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
                        + "\n    (For more info go to PolyLLVMTypeUtil"
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
        return nf.LLVMFunctionType(Position.compilerGenerated(),
                                   formals,
                                   polyLLVMTypeNode(nf, returnType));
    }

    private static LLVMTypeNode polyLLVMObjectType(PseudoLLVMTranslator v,
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

        System.out.println("\n\nHere is a structure Type!");
        structureType.prettyPrint(v.nodeFactory().lang(), System.out);
        System.out.println("\n\n");
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

    /**
     *
     */
    private static LLVMTypeNode polyLLVMDispatchVectorType(
            PseudoLLVMTranslator v, ReferenceType type) {
        List<MethodInstance> layout = v.layouts(type).part1();
        List<LLVMTypeNode> typeList = new ArrayList<>();
        typeList.add(v.nodeFactory()
                      .LLVMPointerType(v.nodeFactory().LLVMIntType(8)));
        for (int i = 0; i < layout.size(); i++) {
            PolyLLVMNodeFactory nf = v.nodeFactory();
            LLVMTypeNode classTypePointer =
                    nf.LLVMPointerType(nf.LLVMVariableType(PolyLLVMMangler.classTypeName(type)));
            LLVMTypeNode funcType =
                    PolyLLVMTypeUtils.polyLLVMFunctionTypeNode(nf,
                                                               layout.get(i)
                                                                     .formalTypes(),
                                                               layout.get(i)
                                                                     .returnType())
                                     .prependFormalTypeNode(classTypePointer);

            typeList.add(funcType);
        }
        LLVMStructureType structureType =
                v.nodeFactory().LLVMStructureType(typeList);
        return structureType;
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

}
