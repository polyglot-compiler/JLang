package jlang.ast;

import polyglot.ast.Expr;
import polyglot.ast.Stmt;

import java.util.List;

public interface ESeq extends Expr {

    List<Stmt> statements();

    ESeq statements(List<Stmt> statements);

    Expr expr();

    ESeq expr(Expr expr);
}
