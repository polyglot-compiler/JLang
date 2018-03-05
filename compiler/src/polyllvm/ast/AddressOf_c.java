package polyllvm.ast;

import polyglot.ast.*;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

import java.util.List;

public class AddressOf_c extends Expr_c implements AddressOf {
    protected Expr expr;

    AddressOf_c(Position pos, Expr expr, Ext ext) {
        super(pos, ext);
        this.expr = expr;
    }

    @Override
    public Node typeCheck(TypeChecker tc) {
        return type(expr.type());
    }

    @Override
    public Expr expr() {
        return expr;
    }

    protected <N extends AddressOf_c> N expr(N n, Expr expr) {
        if (n.expr == expr) return n;
        n = copyIfNeeded(n);
        n.expr = expr;
        return n;
    }

    protected <N extends AddressOf_c> N reconstruct(N n, Expr expr) {
        return expr(n, expr);
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Expr expr = visitChild(this.expr, v);
        return reconstruct(this, expr);
    }

    @Override
    public Term firstChild() {
        return expr;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        v.visitCFG(expr, this, EXIT);
        return succs;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write("addr(");
        expr.prettyPrint(w, pp);
        w.write(")");
    }

    @Override
    public String toString() {
        return "addr(" + expr.toString() + ")";
    }
}
