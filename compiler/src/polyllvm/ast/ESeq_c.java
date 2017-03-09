package polyllvm.ast;

import polyglot.ast.*;
import polyglot.util.Position;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;

import java.util.List;

/**
 * Created by Daniel on 3/9/17.
 */
public class ESeq_c extends Expr_c implements ESeq {

    protected Stmt stmt;
    protected Expr expr;

    public ESeq_c(Position pos, Stmt stmt, Expr expr, Ext e) {
        super(pos, e);
        this.stmt = stmt;
        this.expr = expr;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Stmt s = visitChild(stmt, v);
        Expr e = visitChild(expr, v);
        return reconstruct(this, s, e);
    }

    /** Reconstruct the LLVM Function. */
    protected <N extends ESeq_c> N reconstruct(N n, Stmt s, Expr e) {
        n = stmt(n, s);
        n = expr(n, e);
        return n;
    }

    protected <N extends ESeq_c> N stmt(N n, Stmt s) {
        if (n.stmt == s) return n;
        n = copyIfNeeded(n);
        n.stmt = s;
        return n;
    }

    protected <N extends ESeq_c> N expr(N n, Expr e) {
        if (n.expr == e) return n;
        n = copyIfNeeded(n);
        n.expr = e;
        return n;
    }

    @Override
    public String toString() {
        return "ESEQ(" + stmt.toString() + ", "+expr.toString()+" )";
    }

    @Override
    public Stmt stmt() {
        return stmt;
    }

    @Override
    public ESeq stmt(Stmt s) {
        return stmt(this, s);
    }

    @Override
    public Expr expr() {
        return expr;
    }

    @Override
    public ESeq expr(Expr e) {
        return expr(this, e);
    }

    @Override
    public Term firstChild() {
        return stmt;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        //TODO: Check if this is correct
        v.visitCFG(stmt, expr, ENTRY);
        v.visitCFG(expr, this, EXIT);
        return succs;
    }
}
