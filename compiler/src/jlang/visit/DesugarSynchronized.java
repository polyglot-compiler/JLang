package jlang.visit;

import jlang.ast.JLangNodeFactory;
import jlang.ast.SynchronizedEnter;
import jlang.ast.SynchronizedExit;
import jlang.types.JLangTypeSystem;
import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.types.ParsedClassType;
import polyglot.types.SemanticException;
import polyglot.util.Position;

import java.util.Collections;

public class DesugarSynchronized extends DesugarVisitor {

    private ParsedClassType currentClass;

    public DesugarSynchronized(Job job, JLangTypeSystem ts, JLangNodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    protected void enterClassBody(ParsedClassType ct, ClassBody body) {
        currentClass = ct;
        super.enterClassBody(ct, body);
    }

    @Override
    protected Node leaveDesugar(Node n) throws SemanticException {

        if (n instanceof Synchronized) {
            return desugarSynchronizedNode((Synchronized) n);
        } else if (n instanceof MethodDecl) {
            MethodDecl node = (MethodDecl) n;
            if (node.flags().isSynchronized() && node.body() != null) {
                // TODO: how to handle public synchronized native void foo()
                // Remove && node.body() != null after figure out how to handle it
                Block body = node.body();
                Expr synchronizedObj;
                if (node.flags().isStatic()) {
                    synchronizedObj = tnf.ClassLit(body.position(), currentClass);
                } else {
                    synchronizedObj = tnf.UnqualifiedThis(body.position(), currentClass);
                }
                Synchronized synchronizedBody = nf.Synchronized(body.position(), synchronizedObj, body);
                return node.body(desugarSynchronizedNode(synchronizedBody));
            }
        }

        return n;
    }

    /**
     * Desugar
     * synchronized(o) {...}
     * To
     * {
     * Object temp = o;
     * try { synchronized_enter(o); ... }
     * finally {synchronized_exit(o); }
     * }
     *
     * @param node the desugared input
     * @return the desugared output
     */
    private Block desugarSynchronizedNode(Synchronized node) {
        Position pos = node.position();

        LocalDecl declObj = tnf.TempSSA("syncObj", node.expr());
        Local obj = tnf.Local(pos, declObj);

        SynchronizedEnter syncEnter = nf.SynchronizedEnter(pos, copy(obj));
        SynchronizedExit syncExit = nf.SynchronizedExit(pos, copy(obj));

        Block tryBlock = nf.Block(pos, syncEnter, node.body());
        Block finallyBlock = nf.Block(pos, syncExit);

        Try tryFinally = nf.Try(pos, tryBlock, Collections.emptyList(), finallyBlock);

        return nf.Block(pos, declObj, tryFinally);
    }
}
