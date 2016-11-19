package polyllvm.ast.PseudoLLVM.Expressions;

import polyglot.ast.Ext;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMStructureType;

import java.util.List;

public class LLVMStructLiteral_c extends LLVMOperand_c implements LLVMStructLiteral {
    private final LLVMStructureType typeNode;
    private final List<LLVMTypedOperand> exprs;

    public LLVMStructLiteral_c(Position pos, LLVMStructureType tn,
                               List<LLVMTypedOperand> entries, Ext e) {
        super(pos, e);
        this.typeNode = tn;
        this.exprs = entries;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write("{");
        int i = 0;
        for (LLVMExpr e : exprs) {
            if (i > 0) w.write(", ");
            print(e, w, pp);
            ++i;
        }
        w.write("}");
    }

    @Override
    public LLVMStructureType typeNode() {
        return typeNode;
    }
}
