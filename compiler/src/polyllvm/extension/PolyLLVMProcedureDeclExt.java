package polyllvm.extension;

import polyglot.ast.Formal;
import polyglot.ast.MethodDecl;
import polyglot.ast.Node;
import polyglot.ast.ProcedureDecl;
import polyglot.types.*;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;
import java.util.List;
import java.util.stream.Collectors;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMProcedureDeclExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    private static boolean containsCode(ProcedureInstance pi) {
        return !pi.flags().contains(Flags.NATIVE) && !pi.flags().contains(Flags.ABSTRACT);
    }

    @Override
    public LLVMTranslator enterTranslatePseudoLLVM(LLVMTranslator v) {
        ProcedureDecl n = (ProcedureDecl) node();
        TypeSystem ts = v.typeSystem();
        ProcedureInstance pi = n.procedureInstance();
        if (!containsCode(pi))
            return super.enterTranslatePseudoLLVM(v); // Ignore native methods.

        // Build function type.
        Type retType = n instanceof MethodDecl ? ((MethodDecl) n).returnType().type() : ts.Void();
        List<Type> formalTypes = n.formals().stream()
                .map(Formal::declType)
                .collect(Collectors.toList());
        ReferenceType target = v.getCurrentClass().type().toReference();
        LLVMTypeRef funcType = pi.flags().isStatic()
                ? v.utils.functionType(retType, formalTypes)
                : v.utils.methodType(target, retType, formalTypes);

        LLVMValueRef funcRef = v.utils.funcRef(v.mod, pi, funcType);
        v.debugInfo.funcDebugInfo(v, n, funcRef);
        v.debugInfo.emitLocation(n);

        LLVMBasicBlockRef entry = LLVMAppendBasicBlock(funcRef, "allocs_entry");
        LLVMBasicBlockRef body_entry = LLVMAppendBasicBlock(funcRef, "body_entry");
        LLVMPositionBuilderAtEnd(v.builder, entry);

        for (int i = 0; i < n.formals().size(); ++i) {
            Formal formal = n.formals().get(i);
            LLVMTypeRef typeRef = v.utils.typeRef(formal.type().type());

            LLVMValueRef alloc = LLVMBuildAlloca(v.builder, typeRef, "arg_" + formal.name());
            int idx = i + (pi.flags().isStatic() ? 0 : 1);
            LLVMBuildStore(v.builder, LLVMGetParam(funcRef, idx), alloc);
            v.addAllocation(formal.name(), alloc);

            v.debugInfo.createParamVariable(v, formal, i, alloc);
        }

        v.debugInfo.emitLocation(n);
        LLVMBuildBr(v.builder, body_entry);
        LLVMPositionBuilderAtEnd(v.builder,body_entry);

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
    public Node translatePseudoLLVM(LLVMTranslator v) {
        ProcedureDecl n = (ProcedureDecl) node();
        ProcedureInstance pi = n.procedureInstance();
        if (!containsCode(pi))
            return super.translatePseudoLLVM(v); // Ignore native methods.

        // Add void return if necessary.
        LLVMBasicBlockRef block = LLVMGetInsertBlock(v.builder);
        if (LLVMGetBasicBlockTerminator(block) == null) {
            LLVMBuildRetVoid(v.builder);
        }

        v.clearAllocations();
        v.popFn();
        v.debugInfo.popScope();
        return super.translatePseudoLLVM(v);
    }
}
