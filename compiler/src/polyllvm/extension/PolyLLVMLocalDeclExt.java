package polyllvm.extension;

import polyglot.ast.LocalDecl;
import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.LLVMNode;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable_c.VarType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.Statements.LLVMStore;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMLocalDeclExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        LocalDecl n = (LocalDecl) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();

        LLVMTypeNode typeNode = (LLVMTypeNode) v.getTranslation(n.type());
        v.addAllocation(n.name(), typeNode);

        if (n.init() == null) {
            return super.translatePseudoLLVM(v);
        }

        LLVMNode decl = v.getTranslation(n.init());

        if (!(decl instanceof LLVMOperand)) {
            throw new InternalCompilerError("Initializer " + n.init() + " ("
                    + n.init().getClass() + ") was not translated to an"
                    + " LLVMOperand, it was translated: " + decl);
        }
        LLVMVariable ptr =
                nf.LLVMVariable(Position.compilerGenerated(),
                                v.varName(n.name()),
                                typeNode,
                                VarType.LOCAL);
        LLVMStore store = nf.LLVMStore(Position.compilerGenerated(),
                                       typeNode,
                                       (LLVMOperand) decl,
                                       ptr);
        v.addTranslation(n, store);

//        if (decl instanceof LLVMInstruction) {
//            LLVMInstruction translation =
//                    ((LLVMInstruction) decl).result(nf.LLVMVariable(Position.compilerGenerated(),
//                                                                    n.name(),
//                                                                    VarType.LOCAL));
//            v.addTranslation(node(), translation);
//        } /*else  if (decl instanceof LLVMIntLiteral){
//          } */
//        else {
//            throw new InternalCompilerError("Cannot translate java expression -- "
//                    + n.init() + " -- as a part of a local decl.");
//        }
//        LLVMInstruction expr = (LLVMInstruction) v.getTranslation(n.init());
//        LLVMInstruction translation =
//                expr.result(nf.LLVMVariable(Position.compilerGenerated(),
//                                            n.name(),
//                                            VarType.LOCAL));
//        v.addTranslation(node(), translation);
        return super.translatePseudoLLVM(v);
    }
}
