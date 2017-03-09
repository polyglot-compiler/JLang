package polyllvm.ast;

import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.Stmt;

public interface ESeq extends Expr {
    public Stmt stmt();

    public ESeq stmt(Stmt s);

    public Expr expr();

    public ESeq expr(Expr e);

}
