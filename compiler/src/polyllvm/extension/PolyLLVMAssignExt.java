package polyllvm.extension;

import polyglot.ast.*;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.DesugarLocally;
import polyllvm.visit.LLVMTranslator;

import java.util.Arrays;

import static org.bytedeco.javacpp.LLVM.LLVMBuildStore;
import static org.bytedeco.javacpp.LLVM.LLVMValueRef;

public class PolyLLVMAssignExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node desugar(DesugarLocally v) {
        Assign n = (Assign) node();
        Position pos = n.position();

        // We desugar all assigns into Ambiguous assigns so that the children are
        // not constrained to specific node types when desugaring.
        if (!(n instanceof AmbAssign))
            return v.nf.AmbAssign(pos, n.left(), n.operator(), n.right()).type(n.type());

        // Desugar to simple assignment.
        if (!n.operator().equals(Assign.ASSIGN))
            return desugarToSimpleAssignment(n, v);

        return super.desugar(v);
    }

    protected Node desugarToSimpleAssignment(Assign n, DesugarLocally v) {
        Position pos = n.position();

        LocalDecl leftPtrFlat = v.tnf.TempSSA("lvalue", v.tnf.AddressOf(n.left()));
        LocalDecl rightFlat = v.tnf.TempSSA("rvalue", n.right());
        Local leftPtr = v.tnf.Local(pos, leftPtrFlat);
        Local right = v.tnf.Local(pos, rightFlat);

        Binary.Operator binop = convertAssignOpToBinop(n.operator());
        Expr leftLoaded = v.tnf.Load(copy(leftPtr));
        Binary res = (Binary) v.nf.Binary(pos, leftLoaded, binop, copy(right)).type(n.type());
        n = n.left(copy(leftPtr)).operator(Assign.ASSIGN).right(res);

        return v.tnf.ESeq(Arrays.asList(leftPtrFlat, rightFlat), n);
    }

    @Override
    public Node overrideTranslateLLVM(Node parent, LLVMTranslator v) {
        Assign n = (Assign) node();
        Assign.Operator op = n.operator();

        if (!op.equals(Assign.ASSIGN))
            throw new InternalCompilerError("Non-simple assignments should have been desugared");

        LLVMValueRef ptr = lang().translateAsLValue(n.left(), v);
        n.visitChild(n.right(), v);
        LLVMValueRef val = v.getTranslation(n.right());
        LLVMBuildStore(v.builder, val, ptr);
        v.addTranslation(n, val);
        return n;
    }

    protected Binary.Operator convertAssignOpToBinop(Assign.Operator op) {
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
