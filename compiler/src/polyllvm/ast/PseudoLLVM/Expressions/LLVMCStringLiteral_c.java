package polyllvm.ast.PseudoLLVM.Expressions;

import polyglot.ast.Ext;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMArrayType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;

import java.util.List;

/**
 * Created by Daniel on 10/31/16.
 */
public class LLVMCStringLiteral_c extends LLVMOperand_c implements LLVMCStringLiteral {

    private final String string;
    private final LLVMTypeNode typeNode;


    public LLVMCStringLiteral_c(Position pos, LLVMArrayType tn, String s, Ext e) {
        super(pos, e);
        this.string = s;
        this.typeNode = tn;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write("c\"");
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c >= 128){
                throw new InternalCompilerError("C String literals can only contain ASCII characters");
            }
            w.write("\\" + Integer.toHexString(c));
        }
        w.write("\\00\"");
    }


    @Override
    public LLVMTypeNode typeNode() {
        return typeNode;
    }
}
