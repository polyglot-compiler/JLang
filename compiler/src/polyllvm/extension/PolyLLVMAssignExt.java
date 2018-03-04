package polyllvm.extension;

import polyglot.ast.AmbAssign;
import polyglot.ast.Assign;
import polyglot.ast.Binary;
import polyglot.ast.Node;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.DesugarLocally;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMAssignExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node desugar(DesugarLocally v) {
        // We desugar all assigns into Ambiguous assigns so that the children are
        // not constrained to specific node types when desugaring.
        Assign n = (Assign) node();
        if (!(n instanceof AmbAssign))
            return v.nf.AmbAssign(n.position(), n.left(), n.operator(), n.right());
        return super.desugar(v);
    }

    @Override
    public Node overrideTranslateLLVM(Node parent, LLVMTranslator v) {
        Assign n = (Assign) node();
        Assign.Operator op = n.operator();
        Type rhsTy = n.right().type();

        LLVMValueRef ptr = lang().translateAsLValue(n.left(), v);
        n.right().visit(v);
        LLVMValueRef x_rhs = v.getTranslation(n.right());

        if (op.equals(Assign.ASSIGN)) {
            // Simple assignment.
            LLVMBuildStore(v.builder, v.getTranslation(n.right()), ptr);
            v.addTranslation(n, x_rhs);
        } else {
            // Update assignment.
            LLVMValueRef prevVal = LLVMBuildLoad(v.builder, ptr, "load");
            Binary.Operator binop = convertAssignOpToBinop(n.operator());
            LLVMValueRef newVal
                    = PolyLLVMBinaryExt.computeBinop(v.builder, binop, prevVal, x_rhs, rhsTy, rhsTy);
            LLVMBuildStore(v.builder, newVal, ptr);
            v.addTranslation(n, newVal);
        }

        return n;
    }

    private static Binary.Operator convertAssignOpToBinop(Assign.Operator op) {
        if      (op.equals(Assign.ADD_ASSIGN))     return Binary.ADD;
        else if (op.equals(Assign.SUB_ASSIGN))     return Binary.SUB;
        else if (op.equals(Assign.MUL_ASSIGN))     return Binary.MUL;
        else if (op.equals(Assign.DIV_ASSIGN))     return Binary.DIV;
        else if (op.equals(Assign.MOD_ASSIGN))     return Binary.MOD;
        else if (op.equals(Assign.BIT_AND_ASSIGN)) return Binary.BIT_AND;
        else if (op.equals(Assign.BIT_OR_ASSIGN))  return Binary.BIT_OR;
        else if (op.equals(Assign.BIT_XOR_ASSIGN)) return Binary.BIT_XOR;
        else if (op.equals(Assign.SHL_ASSIGN))     return Binary.SHL;
        else if (op.equals(Assign.SHR_ASSIGN))     return Binary.SHR;
        else if (op.equals(Assign.USHR_ASSIGN))    return Binary.USHR;
        else throw new InternalCompilerError("Invalid assignment-to-binop conversion");
    }
}
