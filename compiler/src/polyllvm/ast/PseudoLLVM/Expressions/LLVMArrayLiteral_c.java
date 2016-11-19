package polyllvm.ast.PseudoLLVM.Expressions;

import polyglot.ast.Ext;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMArrayType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;

import java.util.List;

public class LLVMArrayLiteral_c extends LLVMOperand_c implements LLVMArrayLiteral {
    private final LLVMTypeNode typeNode;
    private final List<LLVMTypedOperand> exprs;

    public LLVMArrayLiteral_c(Position pos, LLVMArrayType tn,
                              List<LLVMTypedOperand> entries, Ext e) {
        super(pos, e);
        this.typeNode = tn;
        this.exprs = entries;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write("[");
        int i = 0;
        for (LLVMExpr e : exprs) {
            if (i > 0) w.write(", ");
            print(e, w, pp);
            ++i;
        }
        w.write("]");
    }

    @Override
    public LLVMTypeNode typeNode() {
        return typeNode;
    }
}
