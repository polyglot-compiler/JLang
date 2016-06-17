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
import polyllvm.ast.PseudoLLVM.LLVMFunction;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.visit.AddVoidReturnVisitor;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMMethodDeclExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        MethodDecl n = (MethodDecl) node();
        MethodInstance mi = n.methodInstance();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        if (mi.flags().contains(Flags.NATIVE)) {
            return super.translatePseudoLLVM(v);
        }
        if (mi.flags().contains(Flags.STATIC)) {
            List<LLVMArgDecl> args = new ArrayList<>();
            for (Formal t : n.formals()) {
                args.add((LLVMArgDecl) v.getTranslation(t));
            }
            LLVMTypeNode retType =
                    (LLVMTypeNode) v.getTranslation(n.returnType());
            LLVMBlock code = (LLVMBlock) v.getTranslation(n.body());
            System.out.println("Current Class name: "
                    + v.getCurrentClass().name());
            String name;
            if (n.name().equals("main"))
                name = n.name();
            else name = "_" + v.getCurrentClass().name() + "_" + n.name();
            LLVMFunction f = nf.LLVMFunction(Position.compilerGenerated(),
                                             name,
                                             args,
                                             retType,
                                             code);
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
