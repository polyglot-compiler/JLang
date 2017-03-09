package polyllvm.extension;

import polyglot.ast.*;
import polyglot.types.Type;
import polyglot.util.CollectionUtil;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.visit.LLVMTranslator;

import java.util.List;

import static org.bytedeco.javacpp.LLVM.LLVMBuildStore;
import static org.bytedeco.javacpp.LLVM.LLVMValueRef;

public class PolyLLVMArrayInitExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(LLVMTranslator v) {
        ArrayInit n = (ArrayInit) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        List<Expr> elements = n.elements();
        Type elemType = n.type().toArray().base();

        Expr intLit1 = nf.IntLit(Position.compilerGenerated(), IntLit.INT, elements.size())
                         .type(v.typeSystem().Int());
        v.lang().translatePseudoLLVM(intLit1, v);
        List<Expr> dims = CollectionUtil.list(intLit1);

        v.debugInfo.emitLocation(n);

        New newArray = PolyLLVMNewArrayExt.translateArrayWithDims(v, nf, dims, elemType);
        LLVMValueRef array = v.getTranslation(newArray);

        LLVMValueRef base = v.utils.buildJavaArrayBase(array, elemType);
        int idx = 0;
        for (Expr expr : elements) {
            LLVMValueRef val = v.getTranslation(expr);
            LLVMValueRef gep = v.utils.buildStructGEP(v.builder, base, idx);
            LLVMBuildStore(v.builder, val, gep);
            ++idx;
        }

        v.addTranslation(n, array);
        return super.translatePseudoLLVM(v);
    }
}
