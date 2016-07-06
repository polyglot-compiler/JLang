package polyllvm.ast.PseudoLLVM.LLVMTypes;

import java.util.List;

import polyglot.ast.Ext;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;

public class LLVMFunctionType_c extends LLVMTypeNode_c
        implements LLVMFunctionType {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected List<LLVMTypeNode> formalTypes;
    protected LLVMTypeNode returnType;

    public LLVMFunctionType_c(Position pos, List<LLVMTypeNode> formalTypes,
            LLVMTypeNode returnType, Ext e) {
        super(pos, e);
        this.formalTypes = formalTypes;
        this.returnType = returnType;
    }

}
