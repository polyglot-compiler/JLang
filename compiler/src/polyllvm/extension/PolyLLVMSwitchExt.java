package polyllvm.extension;

import polyglot.ast.*;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMSwitchExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node overrideTranslateLLVM(LLVMTranslator v) {
        Switch n = (Switch) node();
        LLVMBasicBlockRef prevBlock = LLVMGetInsertBlock(v.builder);

        LLVMBasicBlockRef end = LLVMAppendBasicBlockInContext(v.context, v.currFn(), "switch_end");
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
                        LLVMAppendBasicBlockInContext(v.context, v.currFn(), "switch_case");
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

        v.debugInfo.emitLocation(n);
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
