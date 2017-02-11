package polyllvm.extension;

import polyglot.ast.MethodDecl;
import polyglot.ast.Node;
import polyglot.ast.ProcedureDecl;
import polyglot.types.Flags;
import polyglot.types.ProcedureInstance;
import polyglot.types.TypeSystem;
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

    /**
     * Returns true iff the class member has the signature `public static void main(String[] args)`.
     */
    private boolean isEntryPoint(TypeSystem ts) {
        ProcedureDecl n = (ProcedureDecl) node();
        return n.name().equals("main")
                && n.flags().isStatic()
                && n.flags().isPublic()
                && n.formals().size() == 1
                && n.formals().iterator().next().declType().equals(ts.arrayOf(ts.String()));
    }

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
        TypeSystem ts = v.typeSystem();
        ProcedureInstance pi = n.procedureInstance();

        // Build function type.
        ArrayList<LLVMTypeRef> argTypes = new ArrayList<>();
        if (!pi.flags().isStatic())
            argTypes.add(LLVMUtils.typeRef(v.getCurrentClass().type(), v.mod));
        n.formals().stream()
                .map(f -> LLVMUtils.typeRef(f.declType(), v.mod))
                .forEach(argTypes::add);
        LLVMTypeRef retType = n instanceof MethodDecl
                ? LLVMUtils.typeRef(((MethodDecl) n).returnType().type(), v.mod)
                : LLVMVoidType();
        LLVMTypeRef[] argTypesArr = argTypes.toArray(new LLVMTypeRef[0]);
        LLVMTypeRef funcType = LLVMUtils.functionType(retType, argTypesArr);

        // Add function to module.
        LLVMValueRef funcRef;
        String name = PolyLLVMMangler.mangleProcedureName(pi);
        funcRef = LLVMAddFunction(v.mod, name, funcType);
        if (!pi.flags().contains(Flags.NATIVE) && !pi.flags().contains(Flags.ABSTRACT)) {
            // TODO: Add alloca instructions for local variables here.
            LLVMBasicBlockRef entry = LLVMAppendBasicBlock(v.currFn(), "entry_alloca_instrs");
            LLVMPositionBuilderAtEnd(v.builder, entry);
        }

        // Register as entry point if applicable.
        boolean isEntryPoint = n.name().equals("main")
                && n.flags().isPublic()
                && n.formals().size() == 1
                && n.formals().iterator().next().declType().equals(ts.arrayOf(ts.String()));
        if (isEntryPoint) {
            v.addEntryPoint(funcRef);
        }

        v.pushFn(funcRef);
        v.addTranslation(n, funcRef);
        return super.enterTranslatePseudoLLVM(v);
    }

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        // Add void return if necessary.
        LLVMBasicBlockRef block = LLVMGetInsertBlock(v.builder);
        if (LLVMGetBasicBlockTerminator(block) == null) {
            LLVMBuildRetVoid(v.builder);
        }

        v.clearArguments();
        v.clearAllocations();
        v.popFn();
        return super.translatePseudoLLVM(v);
    }
}
