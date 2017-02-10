package polyllvm.extension;

import polyglot.ast.Node;
import polyglot.ast.ProcedureDecl;
import polyglot.types.Flags;
import polyglot.types.ProcedureInstance;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.util.LLVMUtils;
import polyllvm.util.PolyLLVMMangler;
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

    protected LLVMTypeRef llvmRetType(LLVMModuleRef mod) {
        // Override in subclasses.
        return LLVMVoidType();
    }

    @Override
    public PseudoLLVMTranslator enterTranslatePseudoLLVM(PseudoLLVMTranslator v) {
        ProcedureDecl n = (ProcedureDecl) node();
        ProcedureInstance pi = n.procedureInstance();

        // Build function type.
        ArrayList<LLVMTypeRef> argTypes = new ArrayList<>();
        if (!pi.flags().isStatic()) {
            argTypes.add(LLVMVoidType()); // TODO
            // LLVMTypeNode objType = LLVMUtils.polyLLVMObjectVariableType(v, v.getCurrentClass().type());
        }
        n.formals().stream().map(f -> LLVMUtils.typeRef(f.type(), v.mod)).forEach(argTypes::add);
        LLVMTypeRef retType = llvmRetType(v.mod);
        LLVMTypeRef[] argTypesArr = argTypes.toArray(new LLVMTypeRef[0]);
        LLVMTypeRef funcType = LLVMUtils.functionType(retType, argTypesArr);

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
}
