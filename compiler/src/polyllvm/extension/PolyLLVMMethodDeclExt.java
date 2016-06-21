package polyllvm.extension;

import java.util.ArrayList;
import java.util.List;

import polyglot.ast.Formal;
import polyglot.ast.MethodDecl;
import polyglot.ast.Node;
import polyglot.types.Flags;
import polyglot.types.MethodInstance;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.LLVMArgDecl;
import polyllvm.ast.PseudoLLVM.LLVMBlock;
import polyllvm.ast.PseudoLLVM.LLVMNode;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.util.PolyLLVMMangler;
import polyllvm.visit.AddVoidReturnVisitor;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMMethodDeclExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        MethodDecl n = (MethodDecl) node();
        MethodInstance mi = n.methodInstance();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        if (mi.flags().contains(Flags.STATIC)) {
            List<LLVMArgDecl> args = new ArrayList<>();
            for (Formal t : n.formals()) {
                args.add((LLVMArgDecl) v.getTranslation(t));
            }
            LLVMTypeNode retType =
                    (LLVMTypeNode) v.getTranslation(n.returnType());
            String name =
                    PolyLLVMMangler.mangleMethodName(v.getCurrentClass().name(),
                                                     n.name());
            LLVMNode f;
            if (mi.flags().contains(Flags.NATIVE)) {
                f = nf.LLVMFunctionDeclaration(Position.compilerGenerated(),
                                               name,
                                               args,
                                               retType);
            }
            else {
                LLVMBlock code = (LLVMBlock) v.getTranslation(n.body());
                f = nf.LLVMFunction(Position.compilerGenerated(),
                                    name,
                                    args,
                                    retType,
                                    code);
            }
            v.addTranslation(node(), f);

        }
        else {
            throw new InternalCompilerError("Cannot compile non-static methods");
        }

        return super.translatePseudoLLVM(v);
    }

    @Override
    public Node addVoidReturn(AddVoidReturnVisitor v) {
        MethodDecl n = (MethodDecl) node();
        MethodInstance mi = n.methodInstance();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        if (mi.returnType().isVoid() && n.body() != null) {
            return n.body(n.body()
                           .append(nf.Return(Position.compilerGenerated())));
        }
        return super.addVoidReturn(v);
    }
}
