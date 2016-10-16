package polyllvm.extension;

import polyglot.ast.*;
import polyglot.types.Flags;
import polyglot.types.ProcedureInstance;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.LLVMArgDecl;
import polyllvm.ast.PseudoLLVM.LLVMBlock;
import polyllvm.ast.PseudoLLVM.LLVMNode;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMPointerType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;
import polyllvm.util.PolyLLVMConstants;
import polyllvm.util.PolyLLVMMangler;
import polyllvm.util.PolyLLVMTypeUtils;
import polyllvm.visit.AddPrimitiveWideningCastsVisitor;
import polyllvm.visit.PseudoLLVMTranslator;

import java.util.ArrayList;
import java.util.List;

public class PolyLLVMProcedureDeclExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public AddPrimitiveWideningCastsVisitor enterAddPrimitiveWideningCasts(
            AddPrimitiveWideningCastsVisitor v) {
        v.setCurrentMethod((ProcedureDecl) node());
        return super.enterAddPrimitiveWideningCasts(v);
    }

    @Override
    public Node addPrimitiveWideningCasts(AddPrimitiveWideningCastsVisitor v) {
        v.popCurrentMethod();
        return super.addPrimitiveWideningCasts(v);
    }

    @Override
    public PseudoLLVMTranslator enterTranslatePseudoLLVM(
            PseudoLLVMTranslator v) {
        ProcedureDecl n = (ProcedureDecl) node();
        List<Formal> parameters = n.formals();
        for (Formal formal : parameters) {
            lang().translatePseudoLLVM(formal.type(), v);
            LLVMTypeNode tn = (LLVMTypeNode) v.getTranslation(formal.type());
            v.addArgument(formal.name(), tn);
        }
        return super.enterTranslatePseudoLLVM(v);
    }

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        ProcedureDecl n = (ProcedureDecl) node();
        ProcedureInstance pi = n.procedureInstance();
        PolyLLVMNodeFactory nf = v.nodeFactory();

        List<LLVMArgDecl> args = new ArrayList<>();
        if (!pi.flags().isStatic()) {
            LLVMTypeNode objType =
                    PolyLLVMTypeUtils.polyLLVMObjectVariableType(v,
                                                                 v.getCurrentClass()
                                                                  .type());
            LLVMPointerType objPointerType = nf.LLVMPointerType(objType);
            args.add(nf.LLVMArgDecl(objPointerType,
                                    PolyLLVMConstants.THISSTRING));
        }
        for (Formal t : n.formals()) {
            args.add((LLVMArgDecl) v.getTranslation(t));
        }

        LLVMTypeNode retType = nf.LLVMVoidType();
        if (n instanceof MethodDecl) {
            retType =
                    (LLVMTypeNode) v.getTranslation(((MethodDecl) n).returnType());
        }
        String name = PolyLLVMMangler.mangleProcedureName(pi);
        LLVMNode f;
        if (pi.flags().contains(Flags.NATIVE)) {
            f = nf.LLVMFunctionDeclaration(name, args, retType);
        }
        else {
            LLVMBlock code = (LLVMBlock) v.getTranslation(n.body());

            List<LLVMInstruction> instrs = code.instructions();
            instrs.addAll(0, v.allocationInstructions());

            if (n instanceof ConstructorDecl) {
                instrs.add(nf.LLVMRet());
            }

            code = code.instructions(instrs);

            f = nf.LLVMFunction(name, args, retType, code);
        }
        v.addTranslation(node(), f);

        v.clearArguments();
        v.clearAllocations();
        return super.translatePseudoLLVM(v);

    }

}
