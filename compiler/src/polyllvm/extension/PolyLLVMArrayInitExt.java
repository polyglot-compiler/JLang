package polyllvm.extension;

import polyglot.ast.*;
import polyglot.types.Type;
import polyglot.util.CollectionUtil;
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

        Expr one = nf.IntLit(n.position(), IntLit.INT, elements.size())
                         .type(v.typeSystem().Int());
        one.visit(v);
        List<Expr> dims = CollectionUtil.list(one);

        v.debugInfo.emitLocation(n);

        New newArray = PolyLLVMNewArrayExt.translateNewArray(v, nf, dims, elemType, n.position());
        LLVMValueRef array = v.getTranslation(newArray);

        LLVMValueRef base = v.utils.buildJavaArrayBase(array, elemType);
        int idx = 0;
        for (Expr expr : elements) {
            LLVMValueRef val = v.getTranslation(expr);
            LLVMValueRef gep = v.utils.buildStructGEP(base, idx);
            LLVMBuildStore(v.builder, val, gep);
            ++idx;
        }

        v.addTranslation(n, array);
        return super.translatePseudoLLVM(v);
    }
}
