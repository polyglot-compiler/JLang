package polyllvm.extension;

import org.bytedeco.javacpp.PointerPointer;
import polyglot.ast.Formal;
import polyglot.ast.MethodDecl;
import polyglot.ast.Node;
import polyglot.ast.ProcedureDecl;
import polyglot.types.Flags;
import polyglot.types.ProcedureInstance;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.util.PolyLLVMMangler;
import polyllvm.util.PolyLLVMTypeUtils;
import polyllvm.visit.AddPrimitiveWideningCastsVisitor;
import polyllvm.visit.PseudoLLVMTranslator;

import java.util.ArrayList;

import static org.bytedeco.javacpp.LLVM.*;

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
    public PseudoLLVMTranslator enterTranslatePseudoLLVM(PseudoLLVMTranslator v) {
        ProcedureDecl n = (ProcedureDecl) node();
        ProcedureInstance pi = n.procedureInstance();

        // Build function type.
        ArrayList<LLVMTypeRef> argTypes = new ArrayList<>();
        if (!pi.flags().isStatic()) {
            argTypes.add(LLVMVoidType()); // TODO
            // LLVMTypeNode objType = PolyLLVMTypeUtils.polyLLVMObjectVariableType(v, v.getCurrentClass().type());
        }
        for (Formal formal : n.formals()) {
            argTypes.add(PolyLLVMTypeUtils.llvmType(formal.type()));
            // v.addArgument(formal.name(), tn); // TODO
        }
        LLVMTypeRef retType = n instanceof MethodDecl
                ? PolyLLVMTypeUtils.llvmType(((MethodDecl) n).returnType())
                : LLVMVoidType();
        @SuppressWarnings("unchecked")
        PointerPointer argTypesArr = new PointerPointer(argTypes.toArray(new LLVMTypeRef[0]));
        LLVMTypeRef funcType = LLVMFunctionType(
                retType, argTypesArr, argTypes.size(), /* isVarArg */ 0);

        // Add function to module.
        LLVMValueRef res;
        String name = PolyLLVMMangler.mangleProcedureName(pi);
        if (pi.flags().contains(Flags.NATIVE) || pi.flags().contains(Flags.ABSTRACT)) {
            res = LLVMAddGlobal(v.mod, funcType, name);
        } else {
            res = LLVMAddFunction(v.mod, name, funcType);
            v.pushFn(res);
            LLVMBasicBlockRef entry = LLVMAppendBasicBlock(v.currFn(), "entry");
            LLVMPositionBuilderAtEnd(v.builder, entry);
            // TODO: Add alloca instructions for local variables here.
        }

        v.addTranslation(n, res);
        return super.enterTranslatePseudoLLVM(v);
    }

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        v.clearArguments();
        v.clearAllocations();
        v.popFn();
        return super.translatePseudoLLVM(v);
    }

    // TODO: Delete this
//    @Override
//    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
//        ProcedureDecl n = (ProcedureDecl) node();
//        ProcedureInstance pi = n.procedureInstance();
//        PolyLLVMNodeFactory nf = v.nodeFactory();
//
//        LLVMNode f;
//        else {
//            LLVMBasicBlockRef code = v.getTranslation(n.body());
//
//            List<LLVMInstruction> instrs = code.instructions();
//            instrs.addAll(0, v.allocationInstructions());
//
//            if (n instanceof ConstructorDecl) {
//                instrs.add(nf.LLVMRet());
//            }
//
//            code = code.instructions(instrs);
//
//            f = nf.LLVMFunction(name, args, retType,v.makeBlockCanonical(code));
//        }
//        v.addTranslation(node(), f);
//
//        v.clearArguments();
//        v.clearAllocations();
//        v.currFn = null;
//        return super.translatePseudoLLVM(v);
//    }
}
