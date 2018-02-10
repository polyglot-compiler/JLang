package polyllvm.extension;

import polyglot.ast.*;
import polyglot.types.Type;
import polyglot.util.CollectionUtil;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.visit.LLVMTranslator;

import java.util.List;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMArrayInitExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();



    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        ArrayInit n = (ArrayInit) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        List<Expr> elements = n.elements();
        // An ArrayInit's type is an array type whose base is the
        // "least common ancestor" of the elements; it can also be
        // the Null type when there is no element.
        Type elemType;
        if (n.type().isArray()) {
            elemType = n.type().toArray().base();
        }
        else  {
            assert n.type().isNull();
            elemType = v.typeSystem().Null();
        }

        Expr one = nf.IntLit(n.position(), IntLit.INT, elements.size())
                         .type(v.typeSystem().Int());
        one.visit(v);
        List<Expr> dims = CollectionUtil.list(one);

        New newArray = PolyLLVMNewArrayExt.translateNewArray(v, nf, dims, elemType, n.position());
        LLVMValueRef array = v.getTranslation(newArray);

        if (!elements.isEmpty()) {
            LLVMValueRef base = v.utils.buildJavaArrayBase(array, elemType);
            int idx = 0;
            for (Expr expr : elements) {
                LLVMValueRef gep = v.utils.buildStructGEP(base, idx);
                LLVMBuildStore(v.builder, v.getTranslation(expr), gep);
                ++idx;
            }
        }

        v.addTranslation(n, array);
        return super.leaveTranslateLLVM(v);
    }
}
