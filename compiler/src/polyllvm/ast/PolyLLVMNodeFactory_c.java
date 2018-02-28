package polyllvm.ast;

import polyglot.ast.*;
import polyglot.ext.jl7.ast.JL7NodeFactory_c;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyllvm.types.PolyLLVMLocalInstance;

import java.util.List;

/** NodeFactory for PolyLLVM. */
public class PolyLLVMNodeFactory_c extends JL7NodeFactory_c implements PolyLLVMNodeFactory {

    public PolyLLVMNodeFactory_c(PolyLLVMLang lang, PolyLLVMExtFactory extFactory) {
        super(lang, extFactory);
    }

    @Override
    public ESeq ESeq(Position pos, List<Stmt> statements, Expr expr) {
        return new ESeq_c(pos, statements, expr, extFactory().extESeq());
    }

    @Override
    public PolyLLVMExtFactory extFactory() {
        return (PolyLLVMExtFactory) super.extFactory();
    }

    @Override
    public LocalAssign LocalAssign(Position pos, Local left, Assign.Operator op, Expr right) {
        // Check that the left expression is not an SSA value.
        PolyLLVMLocalInstance li = (PolyLLVMLocalInstance) left.localInstance().orig();
        if (li.isSSA())
            throw new InternalCompilerError("Trying to assign to SSA value");
        return super.LocalAssign(pos, left, op, right);
    }
}
