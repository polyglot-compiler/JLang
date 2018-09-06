//Copyright (C) 2017 Cornell University

package jlang.ast;

import polyglot.ast.Expr;

/**
 * An AddressOf translates its child as an lvalue even in an rvalue context.
 * This is useful for temporarily storing the result of an expression before
 * later using it in an lvalue context.
 */
public interface AddressOf extends Expr {

    Expr expr();
}
