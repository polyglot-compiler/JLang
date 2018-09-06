//Copyright (C) 2018 Cornell University

package jlang.ast;

import polyglot.ast.Expr;

/**
 * A load dereferences its child.
 * This is useful in conjunction with {@link AddressOf}.
 */
public interface Load extends Expr {

    Expr expr();
}
