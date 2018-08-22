package jlang.extension;

import polyglot.ast.*;
import polyglot.ext.jl7.ast.TryWithResources;
import polyglot.types.ClassType;
import polyglot.types.Flags;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jlang.visit.DesugarLocally;
import jlang.visit.LLVMTranslator;

public class JLangTryWithResourcesExt extends JLangTryExt {

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        throw new InternalCompilerError("Try-with-resources should be desugared");
    }

    @Override
    public Node desugar(DesugarLocally v) {
        TryWithResources n = (TryWithResources) node();

        if (!n.catchBlocks().isEmpty() || n.finallyBlock() != null)
            return desugarExtended(n, v);

        return desugarSimple(n, v);
    }

    // 14.20.3.1. Basic try-with-resources (JLS SE 7).
    //
    // try (VariableModifiers R Identifier = Expression ...)
    //     Block
    //
    // -->
    //
    // final VariableModifiers_minus_final R Identifier = Expression;
    // Throwable #primaryExc = null;
    //
    // try ResourceSpecification_tail
    //     Block
    // catch (Throwable #t) {
    //     #primaryExc = #t;
    //     throw #t;
    // } finally {
    //     if (Identifier != null) {
    //         if (#primaryExc != null) {
    //             try {
    //                 Identifier.close();
    //             } catch (Throwable #suppressedExc) {
    //                 #primaryExc.addSuppressed(#suppressedExc);
    //             }
    //         } else {
    //             Identifier.close();
    //         }
    //     }
    // }
    protected Node desugarSimple(TryWithResources n, DesugarLocally v) {
        Position pos = n.position();

        assert !n.resources().isEmpty();
        LocalDecl head = n.resources().get(0);
        List<LocalDecl> tail = n.resources().subList(1, n.resources().size());

        // Declare resource.
        LocalDecl resourceDecl = head.flags(head.flags().clearFinal());
        Local resource = v.tnf.Local(pos, resourceDecl);
        assert resource.type().isClass();
        ClassType resourceType = resource.type().toClass();

        // Declare primaryExn.
        LocalDecl primaryExnDecl = v.tnf.TempVar(
                pos, "primaryExn", v.ts.Throwable(), v.tnf.NullLit(pos));
        Local primaryExn = v.tnf.Local(pos, primaryExnDecl);

        // Build catch block.
        List<Stmt> catchBlockStmts = new ArrayList<>();
        Formal tDecl = v.tnf.Formal(pos, "t", v.ts.Throwable(), Flags.NONE);
        Local t = v.tnf.Local(pos, tDecl);
        catchBlockStmts.add(v.tnf.EvalAssign(copy(primaryExn), copy(t)));
        catchBlockStmts.add(v.nf.Throw(pos, copy(t)));
        Catch catchClause = v.nf.Catch(pos, tDecl, v.nf.Block(pos, catchBlockStmts));
        List<Catch> catchClauses = Collections.singletonList(catchClause);

        // Build finally block (null checks).
        Expr resourceNotNull = v.nf.Binary(
                pos, copy(resource), Binary.NE, v.tnf.NullLit(pos)).type(v.ts.Boolean());
        Expr primaryExnNotNull = v.nf.Binary(
                pos, copy(primaryExn), Binary.NE, v.tnf.NullLit(pos)).type(v.ts.Boolean());

        // Build finally block (try close).
        Call closePrimary = v.tnf.Call(pos, copy(resource), "close", resourceType, v.ts.Void());
        Stmt evalClosePrimary = v.nf.Eval(pos, closePrimary);
        Formal suppressedExnDecl = v.tnf.Formal(pos, "suppressedExn", v.ts.Throwable(), Flags.NONE);
        Call addSuppressed = v.tnf.Call(
                pos, copy(primaryExn), "addSuppressed", v.ts.Throwable(),
                v.ts.Void(), v.tnf.Local(pos, suppressedExnDecl));
        Stmt evalAddSuppressed = v.nf.Eval(pos, addSuppressed);
        Catch catchClose = v.nf.Catch(pos, suppressedExnDecl, v.nf.Block(pos, evalAddSuppressed));
        Try tryClose = v.nf.Try(pos,
                v.nf.Block(pos, evalClosePrimary),
                Collections.singletonList(catchClose));

        // Build finally block (else branch).
        Call closeSecondary = v.tnf.Call(pos, copy(resource), "close", resourceType, v.ts.Void());
        Stmt evalCloseSecondary = v.nf.Eval(pos, closeSecondary);

        // Build finally block (if-statements).
        If switchOnPrimary = v.nf.If(
                pos, primaryExnNotNull, tryClose, v.nf.Block(pos, evalCloseSecondary));
        If switchOnResource = v.tnf.If(resourceNotNull, switchOnPrimary);
        Block finallyBlock = v.nf.Block(pos, switchOnResource);

        // Assemble new try-catch block.
        Try recurse = tail.isEmpty()
                ? v.nf.Try(pos, n.tryBlock(), catchClauses, finallyBlock)
                : v.nf.TryWithResources(pos, tail, n.tryBlock(), catchClauses, finallyBlock);

        List<Stmt> stmts = new ArrayList<>();
        stmts.add(resourceDecl);
        stmts.add(primaryExnDecl);
        stmts.add(recurse);

        return v.nf.Block(pos, stmts);
    }

    // 14.20.3.2. Extended try-with-resources (JLS SE 7).
    //
    // try ResourceSpecification
    //     Block
    // Catches
    // Finally
    //
    // -->
    //
    // try {
    //     try ResourceSpecification
    //         Block
    // }
    // Catches
    // Finally
    protected Node desugarExtended(TryWithResources n, DesugarLocally v) {
        List<Catch> catchBlocks = n.catchBlocks();
        Block finallyBlock = n.finallyBlock();
        n = (TryWithResources) n.catchBlocks(Collections.emptyList()).finallyBlock(null);
        return v.nf.Try(n.position(), v.nf.Block(n.position(), n), catchBlocks, finallyBlock);
    }
}
