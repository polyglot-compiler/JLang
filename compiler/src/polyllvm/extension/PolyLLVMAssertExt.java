package polyllvm.extension;

import polyglot.ast.Assert;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.main.Options;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.DesugarLocally;
import polyllvm.visit.LLVMTranslator;

import java.util.Collections;
import java.util.List;

public class PolyLLVMAssertExt extends PolyLLVMExt {

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        throw new InternalCompilerError("Assert statements should have been desugared");
    }

    @Override
    public Node desugar(DesugarLocally v) {
        Assert n = (Assert) node();
        Position pos = n.position();

        if (!Options.global.assertions) {
            // Assertions are disabled.
            return v.nf.Empty(pos);
        }

        List<Expr> args = n.errorMessage() != null
                ? Collections.singletonList(n.errorMessage())
                : Collections.emptyList();

        return v.tnf.If(v.tnf.Not(n.cond()), v.tnf.Throw(pos, v.ts.AssertionError(), args));
    }
}
