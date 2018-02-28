package polyllvm.ast;

import polyglot.ast.Expr;
import polyglot.ast.Stmt;
import polyglot.ext.jl7.ast.JL7NodeFactory;
import polyglot.util.Position;

import java.util.List;

/** NodeFactory for PolyLLVM. */
public interface PolyLLVMNodeFactory extends JL7NodeFactory {

    ESeq ESeq(Position pos, List<Stmt> statements, Expr expr);
}
