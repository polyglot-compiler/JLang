package polyllvm.util;

import java.util.ArrayList;
import java.util.List;

import polyglot.ast.ClassDecl;
import polyglot.ast.TypeNode;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;

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

    public static LLVMTypeNode polyLLVMFunctionTypeNode(PolyLLVMNodeFactory nf,
            List<? extends Type> formalTypes, Type returnType) {
        List<LLVMTypeNode> formals = new ArrayList<>();
        for (Type type : formalTypes) {
            formals.add(polyLLVMTypeNode(nf, type));
        }
        return nf.LLVMFunctionType(Position.compilerGenerated(),
                                   formals,
                                   polyLLVMTypeNode(nf, returnType));
    }

    public static LLVMTypeNode polyLLVMObjectType(PolyLLVMNodeFactory nf,
            ClassDecl currentClass) {
        String s = currentClass.name();
        return nf.LLVMVariableType("class." + s);
    }

    public static LLVMTypeNode polyLLVMObjectType(PolyLLVMNodeFactory nf,
            TypeNode superClass) {
        String s = superClass.name();
        return nf.LLVMVariableType("class." + s);
    }

    public static LLVMTypeNode polyLLVMDispatchVectorType(
            PolyLLVMNodeFactory nf, ClassDecl currentClass) {
        String s = currentClass.name();
        return nf.LLVMVariableType("dv." + s);
    }

    public static LLVMTypeNode polyLLVMDispatchVectorType(
            PolyLLVMNodeFactory nf, TypeNode superClass) {
        String s = superClass.name();
        return nf.LLVMVariableType("dv." + s);
    }

}
