package polyllvm.util;

import polyglot.types.Type;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;

public class PolyLLVMTypeUtils {

    public static LLVMTypeNode polyLLVMTypeNode(PolyLLVMNodeFactory nf,
            Type t) {
        if (t.isByte()) {
            return nf.LLVMIntType(Position.compilerGenerated(), 8, null);
        }
        else if (t.isChar() || t.isShort()) {
            return nf.LLVMIntType(Position.compilerGenerated(), 16, null);
        }
        else if (t.isInt()) {
            return nf.LLVMIntType(Position.compilerGenerated(), 32, null);
        }
        else if (t.isLong()) {
            return nf.LLVMIntType(Position.compilerGenerated(), 64, null);
        }
        else if (t.isVoid()) {
            return nf.LLVMVoidType(Position.compilerGenerated());
        }
        else {
            try {
                throw new InternalCompilerError("Only integral types and "
                        + "void currently supported.");
            }
            catch (InternalCompilerError e) {
                System.out.println(e + " (For more info go to PolyLLVMTypeUtil"
                        + " and print the stack trace)");
            }
            return null;
        }
    }

}
