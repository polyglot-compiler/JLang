//Copyright (C) 2017 Cornell University

package jlang.extension;

import polyglot.ast.*;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;

import java.lang.Override;
import java.util.*;

import jlang.ast.JLangExt;
import jlang.visit.DesugarLocally;
import jlang.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class JLangSwitchExt extends JLangExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node desugar(DesugarLocally v) {
        Switch n = (Switch) node();

        if (n.expr().type().isSubtype(v.ts.String()))
            return desugarStringSwitch(n, v);

        return super.desugar(v);
    }

    // switch (strExpr) {
    //     case s1:
    //         ...
    //     case sn:
    //         ...
    //     default:
    //         ...
    // }
    //
    // --->
    //
    // let s = strExpr;
    // if (s == null)
    //     throw new NullPointerException();
    //
    // var i = 0;
    // if (s.equals(s1))
    //     i = 1;
    // if (s.equals(sn))
    //     i = n;
    //
    // switch (i) {
    //     case 1:
    //         ...
    //     case n:
    //         ...
    //     default:
    //         ...
    // }
    protected Node desugarStringSwitch(Switch n, DesugarLocally v) {
        assert n.expr().type().isSubtype(v.ts.String());
        Position pos = n.position();
        List<Stmt> stmts = new ArrayList<>();

        // Save switch expression.
        LocalDecl strDecl = v.tnf.TempSSA("str", n.expr());
        Local str = v.tnf.Local(pos, strDecl);
        stmts.add(strDecl);

        // Check for null pointer.
        Expr nil = v.nf.NullLit(pos).type(v.ts.Null());
        Expr check = v.nf.Binary(pos, copy(str), Binary.EQ, nil).type(v.ts.Boolean());
        Stmt throwExn = v.tnf.Throw(pos, v.ts.NullPointerException(), Collections.emptyList());
        stmts.add(v.tnf.If(check, throwExn));

        // Declare case index.
        Expr zero = v.nf.IntLit(pos, IntLit.INT, 0).type(v.ts.Int());
        LocalDecl idxDecl = v.tnf.TempVar(pos, "idx", v.ts.Int(), zero);
        Local idx = v.tnf.Local(pos, idxDecl);
        stmts.add(idxDecl);

        // Switch on case index instead of string value.
        n = n.expr(copy(idx));

        // Assign case index based on string value.
        int counter = 0;
        List<SwitchElement> elems = new ArrayList<>(n.elements());
        for (int i = 0; i < elems.size(); ++i) {
            SwitchElement e = elems.get(i);
            if (e instanceof Case) {
                Case c = (Case) e;
                if (c.isDefault())
                    continue;

                // Assign case index if strings equal.
                assert c.expr() != null;
                Expr equal = v.tnf.Call(
                        pos, copy(str), "equals", v.ts.String(), v.ts.Boolean(), c.expr());
                IntLit val = (IntLit) v.nf.IntLit(pos, IntLit.INT, ++counter).type(v.ts.Int());
                stmts.add(v.tnf.If(equal, v.tnf.EvalAssign(copy(idx), copy(val))));

                // Update case value.
                c = c.expr(copy(val)).value(val.value());
                elems.set(i, c);
            }
        }
        n = n.elements(elems);

        stmts.add(n);
        return v.nf.Block(pos, stmts);
    }

    @Override
    public Node overrideTranslateLLVM(Node parent, LLVMTranslator v) {
        Switch n = (Switch) node();

        // Translate switch expression.
        n.expr().visit(v);
        LLVMValueRef exprRef = v.getTranslation(n.expr());
        LLVMBasicBlockRef headBlock = LLVMGetInsertBlock(v.builder);

        // Create a basic block for each switch block.
        List<LLVMBasicBlockRef> blocks = new ArrayList<>();
        for (SwitchElement e : n.elements())
            if (e instanceof SwitchBlock)
                blocks.add(v.utils.buildBlock("switch.case"));

        // Build end block.
        LLVMBasicBlockRef end = v.utils.buildBlock("switch.end");
        blocks.add(end); // Append end block for convenience.

        // Build switch blocks and case-to-block mappings.
        v.pushSwitch(end); // Allows break statements to jump to end.
        Map<Case, LLVMBasicBlockRef> blockMap = new LinkedHashMap<>(); // Excludes default case.
        LLVMBasicBlockRef defaultBlock = end;
        int nextBlockIdx = 0;
        for (SwitchElement elem : n.elements()) {
            if (elem instanceof Case) {
                // Map case to block.
                Case c = (Case) elem;
                if (c.isDefault()) {
                    defaultBlock = blocks.get(nextBlockIdx);
                } else {
                    blockMap.put(c, blocks.get(nextBlockIdx));
                }
            }
            else if (elem instanceof SwitchBlock) {
                // Build switch block and implement fall-through.
                LLVMPositionBuilderAtEnd(v.builder, blocks.get(nextBlockIdx));
                elem.visit(v);
                ++nextBlockIdx;
                v.utils.branchUnlessTerminated(blocks.get(nextBlockIdx));
            }
            else {
                throw new InternalCompilerError("Unhandled switch element");
            }
        }
        v.popSwitch();

        // Build switch.
        LLVMPositionBuilderAtEnd(v.builder, headBlock);
        LLVMValueRef switchRef = LLVMBuildSwitch(v.builder, exprRef, defaultBlock, blockMap.size());

        // Add all cases.
        for (Map.Entry<Case, LLVMBasicBlockRef> e : blockMap.entrySet()) {
            Case c = e.getKey();
            LLVMBasicBlockRef block = e.getValue();
            assert !c.isDefault() : "The default case should be handled separately";
            LLVMTypeRef type = v.utils.toLL(c.expr().type());
            LLVMValueRef label = LLVMConstInt(type, c.value(), /*sign-extend*/ 0);
            LLVMAddCase(switchRef, label, block);
        }

        LLVMPositionBuilderAtEnd(v.builder, end);
        return n;
    }
}
