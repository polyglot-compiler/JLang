package polyllvm.ast;

import polyglot.ast.*;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

import java.util.ArrayList;
import java.util.List;

public class ESeq_c extends Expr_c implements ESeq {
    protected List<Stmt> statements;
    protected Expr expr;

    ESeq_c(Position pos, List<Stmt> statements, Expr expr, Ext ext) {
        super(pos, ext);
        this.statements = ListUtil.copy(statements, true);
        this.expr = expr;
    }

    @Override
    public Node typeCheck(TypeChecker tc) {
        return type(expr.type());
    }

    @Override
    public List<Stmt> statements() {
        return statements;
    }

    @Override
    public Expr expr() {
        return expr;
    }

    protected <N extends ESeq_c> N statements(N n, List<Stmt> statements) {
        if (CollectionUtil.equals(n.statements, statements)) return n;
        n = copyIfNeeded(n);
        n.statements = ListUtil.copy(statements, true);
        return n;
    }

    protected <N extends ESeq_c> N expr(N n, Expr expr) {
        if (n.expr == expr) return n;
        n = copyIfNeeded(n);
        n.expr = expr;
        return n;
    }

    protected <N extends ESeq_c> N reconstruct(N n, List<Stmt> statements, Expr expr) {
        n = statements(n, statements);
        n = expr(n, expr);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        List<Stmt> statements = visitList(this.statements, v);
        Expr expr = visitChild(this.expr, v);
        return reconstruct(this, statements, expr);
    }

    @Override
    public Term firstChild() {
        return listChild(statements, expr);
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        v.visitCFGList(statements, expr, ENTRY);
        v.visitCFG(expr, this, EXIT);
        return succs;
    }

    private Block blockEquivalent() {
        List<Stmt> stmtsAndExpr = new ArrayList<>(statements());
        stmtsAndExpr.add(new Eval_c(position(), expr(), ext()));
        return new Block_c(position(), stmtsAndExpr, ext());
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        blockEquivalent().prettyPrint(w, pp);
    }

    @Override
    public String toString() {
        return blockEquivalent().toString();
    }
}
