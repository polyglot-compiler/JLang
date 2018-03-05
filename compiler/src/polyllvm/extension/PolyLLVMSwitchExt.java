package polyllvm.extension;

import polyglot.ast.*;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.DesugarLocally;
import polyllvm.visit.LLVMTranslator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMSwitchExt extends PolyLLVMExt {
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
    //     default:
    //         ...
    //     case sn:
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
    //     default:
    //         ...
    //     case n:
    //         ...
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
                stmts.add(v.tnf.If(equal, v.tnf.EvalAssign(pos, copy(idx), copy(val))));

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
        LLVMBasicBlockRef prevBlock = LLVMGetInsertBlock(v.builder);

        LLVMBasicBlockRef end = v.utils.buildBlock("switch.end");
        v.pushSwitch(end);

        // Build switch blocks and map cases to blocks.
        List<Case> cases = new ArrayList<>();
        List<LLVMBasicBlockRef> blocks = new ArrayList<>();
        List<Integer> blockMap = new ArrayList<>();
        for (SwitchElement elem : n.elements()) {
            if (elem instanceof Case) {
                cases.add((Case) elem);
                blockMap.add(blocks.size()); // Map to the next block encountered.
            }
            else if (elem instanceof SwitchBlock) {
                LLVMBasicBlockRef block =
                        v.utils.buildBlock("switch.case");
                LLVMPositionBuilderAtEnd(v.builder, block);
                elem.visit(v);
                blocks.add(block);
            }
            else {
                throw new InternalCompilerError("Unhandled switch element");
            }
        }
        blocks.add(end);

        // Implement fall-through.
        for (int i = 0; i < blocks.size() - 1; ++i) {
            LLVMBasicBlockRef before = blocks.get(i);
            LLVMBasicBlockRef after = blocks.get(i + 1);
            if (LLVMGetBasicBlockTerminator(before) == null)  {
                LLVMPositionBuilderAtEnd(v.builder, before);
                LLVMBuildBr(v.builder, after);
            }
        }

        // Set the default block.
        LLVMBasicBlockRef defaultBlock = end;
        for (int i = 0; i < cases.size(); ++i) {
            if (cases.get(i).isDefault()) {
                defaultBlock = blocks.get(blockMap.get(i));
                break;
            }
        }

        LLVMPositionBuilderAtEnd(v.builder, prevBlock);
        n.expr().visit(v);

        LLVMValueRef exprRef = v.getTranslation(n.expr());
        int numNormalCases = (int) cases.stream().filter(c -> !c.isDefault()).count();
        LLVMValueRef switchRef = LLVMBuildSwitch(v.builder, exprRef, defaultBlock, numNormalCases);

        // Add all cases.
        for (int i = 0; i < cases.size(); ++i) {
            Case c = cases.get(i);
            if (c.isDefault())
                continue;
            LLVMTypeRef type = v.utils.toLL(c.expr().type());
            LLVMValueRef val = LLVMConstInt(type, c.value(), /*sign-extend*/ 0);
            LLVMBasicBlockRef block = blocks.get(blockMap.get(i));
            LLVMAddCase(switchRef, val, block);
        }

        LLVMPositionBuilderAtEnd(v.builder, end);
        v.popSwitch();
        return n;
    }
}
