package polyllvm.extension;

import polyglot.ast.FloatLit;
import polyglot.ast.IntLit;
import polyglot.ast.MethodDecl;
import polyglot.ast.Node;
import polyglot.types.MethodInstance;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.visit.AddVoidReturnVisitor;

public class PolyLLVMMethodDeclExt extends PolyLLVMProcedureDeclExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

//    @Override
//    public AddPrimitiveWideningCastsVisitor enterAddPrimitiveWideningCasts(
//            AddPrimitiveWideningCastsVisitor v) {
//        v.setCurrentMethod((MethodDecl) node());
//        return super.enterAddPrimitiveWideningCasts(v);
//    }
//
//    @Override
//    public Node addPrimitiveWideningCasts(AddPrimitiveWideningCastsVisitor v) {
//        v.popCurrentMethod();
//        return super.addPrimitiveWideningCasts(v);
//    }

//    @Override
//    public PseudoLLVMTranslator enterTranslatePseudoLLVM(
//            PseudoLLVMTranslator v) {
//        MethodDecl n = (MethodDecl) node();
//        List<Formal> parameters = n.formals();
//        for (Formal formal : parameters) {
//            lang().translatePseudoLLVM(formal.type(), v);
//            LLVMTypeNode tn = (LLVMTypeNode) v.getTranslation(formal.type());
//            v.addArgument(formal.name(), tn);
//        }
//        return super.enterTranslatePseudoLLVM(v);
//    }

//    @Override
//    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
//        MethodDecl n = (MethodDecl) node();
//        MethodInstance mi = n.methodInstance();
//        PolyLLVMNodeFactory nf = v.nodeFactory();
//
//        List<LLVMArgDecl> args = new ArrayList<>();
//        if (!mi.flags().isStatic()) {
//            LLVMTypeNode objType =
//                    PolyLLVMTypeUtils.polyLLVMObjectVariableType(v,
//                                                                 v.getCurrentClass()
//                                                                  .type());
//            LLVMPointerType objPointerType = nf.LLVMPointerType(objType);
//            args.add(nf.LLVMArgDecl(objPointerType,
//                                    PolyLLVMConstants.THISSTRING));
//        }
//        for (Formal t : n.formals()) {
//            args.add((LLVMArgDecl) v.getTranslation(t));
//        }
//        LLVMTypeNode retType = (LLVMTypeNode) v.getTranslation(n.returnType());
//        String name = PolyLLVMMangler.mangleMethodName(mi);
//        LLVMNode f;
//        if (mi.flags().contains(Flags.NATIVE)) {
//            f = nf.LLVMFunctionDeclaration(name, args, retType);
//        }
//        else {
//            LLVMBlock code = (LLVMBlock) v.getTranslation(n.body());
//
//            List<LLVMInstruction> instrs = code.instructions();
//            instrs.addAll(0, v.allocationInstructions());
//            code = code.instructions(instrs);
//
//            f = nf.LLVMFunction(name, args, retType, code);
//        }
//        v.addTranslation(n, f);
//
//        v.clearArguments();
//        v.clearAllocations();
//        return super.translatePseudoLLVM(v);
//    }

    @Override
    public Node addVoidReturn(AddVoidReturnVisitor v) {
        MethodDecl n = (MethodDecl) node();
        MethodInstance mi = n.methodInstance();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        if (mi.returnType().isVoid() && n.body() != null) {
            return n.body(n.body()
                           .append(nf.Return(Position.compilerGenerated())));
        }
        else if (mi.returnType().isLongOrLess() && n.body() != null) {
            return n.body(n.body()
                           .append(nf.Return(Position.compilerGenerated(),
                                             nf.IntLit(Position.compilerGenerated(),
                                                       IntLit.INT,
                                                       0)
                                               .type(mi.returnType()))));

        }
        else if (mi.returnType().isBoolean() && n.body() != null) {
            return n.body(n.body()
                           .append(nf.Return(Position.compilerGenerated(),
                                             nf.BooleanLit(Position.compilerGenerated(),
                                                           false)
                                               .type(mi.returnType()))));
        }
        else if ((mi.returnType().isDouble() || mi.returnType().isFloat())
                && n.body() != null) {
            return n.body(n.body()
                           .append(nf.Return(Position.compilerGenerated(),
                                             nf.FloatLit(Position.compilerGenerated(),
                                                         FloatLit.DOUBLE,
                                                         0)
                                               .type(mi.returnType()))));
        }
        else if ((mi.returnType().isClass() || mi.returnType().isArray())
                && n.body() != null) {
            return n.body(n.body()
                           .append(nf.Return(Position.compilerGenerated(),
                                             nf.NullLit(Position.compilerGenerated())
                                               .type(mi.returnType()))));
        }
        return super.addVoidReturn(v);
    }
}
