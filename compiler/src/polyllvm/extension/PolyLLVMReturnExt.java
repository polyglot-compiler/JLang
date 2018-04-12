package polyllvm.extension;

import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.Return;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMReturnExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        Return n = (Return) node();
        Expr e = n.expr();

        LLVMValueRef retVal = null; // Return value.
        LLVMValueRef retSlot = null; // Stack slot to store return value during finally blocks.
        if (e != null) {
            retVal = v.getTranslation(e);
            if (v.needsFinallyBlockChain()) {
                retSlot = v.utils.buildAlloca("ret.finally.slot", LLVMTypeOf(retVal));
                LLVMBuildStore(v.builder, retVal, retSlot);
            }
        }

        // Detour through finally blocks if necessary.
        v.buildFinallyBlockChain(/*tryCatchNestingLevel*/ 0);

        // Reload return value if necessary.
        if (retSlot != null) {
            retVal = LLVMBuildLoad(v.builder, retSlot, "load.ret.finally");
        }

        LLVMValueRef res = retVal != null
                ? LLVMBuildRet(v.builder, retVal)
                : LLVMBuildRetVoid(v.builder);

        v.addTranslation(n, res);
        return super.leaveTranslateLLVM(v);
    }
}
