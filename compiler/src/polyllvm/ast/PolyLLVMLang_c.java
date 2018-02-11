package polyllvm.ast;

import polyglot.ast.*;
import polyglot.ext.jl7.ast.J7Lang_c;
import polyglot.util.InternalCompilerError;
import polyllvm.visit.LLVMTranslator;

import static org.bytedeco.javacpp.LLVM.LLVMBasicBlockRef;
import static org.bytedeco.javacpp.LLVM.LLVMValueRef;

public class PolyLLVMLang_c extends J7Lang_c implements PolyLLVMLang {
    public static final PolyLLVMLang_c instance = new PolyLLVMLang_c();

    public static PolyLLVMLang lang(NodeOps n) {
        while (n != null) {
            Lang lang = n.lang();
            if (lang instanceof PolyLLVMLang) return (PolyLLVMLang) lang;
            if (n instanceof Ext)
                n = ((Ext) n).pred();
            else return null;
        }
        throw new InternalCompilerError("Impossible to reach");
    }

    protected PolyLLVMLang_c() {
    }

    protected static PolyLLVMExt polyllvmExt(Node n) {
        return PolyLLVMExt.ext(n);
    }

    @Override
    protected NodeOps NodeOps(Node n) {
        return polyllvmExt(n);
    }

    protected PolyLLVMOps PolyLLVMOps(Node n) {
        return polyllvmExt(n);
    }

    @Override
    public LLVMTranslator enterTranslateLLVM(Node n,
                                             LLVMTranslator v) {
        return PolyLLVMOps(n).enterTranslateLLVM(v);
    }

    @Override
    public Node leaveTranslateLLVM(Node n, LLVMTranslator v) {
        return PolyLLVMOps(n).leaveTranslateLLVM(v);
    }

    @Override
    public Node overrideTranslateLLVM(Node parent, Node n, LLVMTranslator v) {
        return PolyLLVMOps(n).overrideTranslateLLVM(parent, v);
    }

    @Override
    public Node overrideTranslateLLVM(Node n, LLVMTranslator v) {
        return PolyLLVMOps(n).overrideTranslateLLVM(v);
    }

    @Override
    public void translateLLVMConditional(Node n, LLVMTranslator v,
                                         LLVMBasicBlockRef trueBlock,
                                         LLVMBasicBlockRef falseBlock) {
        PolyLLVMOps(n).translateLLVMConditional(v, trueBlock, falseBlock);
    }

    @Override
    public LLVMValueRef translateAsLValue(Node n, LLVMTranslator v) {
        return PolyLLVMOps(n).translateAsLValue(v);
    }
}
