package polyllvm.extension;

import polyglot.ast.Formal;
import polyglot.ast.MethodDecl;
import polyglot.ast.Node;
import polyglot.ast.ProcedureDecl;
import polyglot.types.*;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.util.LLVMUtils;
import polyllvm.visit.PseudoLLVMTranslator;

import java.util.List;
import java.util.stream.Collectors;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMProcedureDeclExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public PseudoLLVMTranslator enterTranslatePseudoLLVM(PseudoLLVMTranslator v) {
        ProcedureDecl n = (ProcedureDecl) node();
        TypeSystem ts = v.typeSystem();
        ProcedureInstance pi = n.procedureInstance();

        // Build function type.
        Type retType = n instanceof MethodDecl ? ((MethodDecl) n).returnType().type() : ts.Void();
        List<Type> formalTypes = n.formals().stream()
                .map(Formal::declType)
                .collect(Collectors.toList());
        ReferenceType target = v.getCurrentClass().type().toReference();
        LLVMTypeRef funcType = pi.flags().isStatic()
                ? LLVMUtils.functionType(retType, formalTypes, v)
                : LLVMUtils.methodType(target, retType, formalTypes, v);

        // Add function to module.
        LLVMValueRef funcRef = LLVMUtils.funcRef(v.mod, pi, funcType);
        if (!pi.flags().contains(Flags.NATIVE) && !pi.flags().contains(Flags.ABSTRACT)) {
            // TODO: Add alloca instructions for local variables here.
            LLVMBasicBlockRef entry = LLVMAppendBasicBlock(funcRef, "allocs_entry");
            LLVMBasicBlockRef body_entry = LLVMAppendBasicBlock(funcRef, "body_entry");
            LLVMPositionBuilderAtEnd(v.builder, entry);

            for (int i=0;i<n.formals().size(); i++) {
                Formal formal = n.formals().get(i);
                LLVMTypeRef typeRef = LLVMUtils.typeRef(formal.type().type(), v);
                LLVMValueRef alloc = LLVMBuildAlloca(v.builder, typeRef, "arg_"+formal.name());
                LLVMValueRef cast = LLVMBuildBitCast(v.builder, LLVMGetParam(funcRef, i), typeRef, "cast");
                LLVMBuildStore(v.builder, cast, alloc);
                v.addAllocation(formal.name(), alloc);
            }

            LLVMBuildBr(v.builder, body_entry);
            LLVMPositionBuilderAtEnd(v.builder,body_entry);


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

        v.clearAllocations();
        v.popFn();
        return super.translatePseudoLLVM(v);
    }
}
