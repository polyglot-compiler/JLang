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

    private static boolean noImplementation(ProcedureInstance pi) {
        return pi.flags().contains(Flags.NATIVE) || pi.flags().contains(Flags.ABSTRACT);
    }

    @Override
    public Node overrideTranslateLLVM(LLVMTranslator v) {
        ProcedureDecl n = (ProcedureDecl) node();
        TypeSystem ts = v.typeSystem();
        ProcedureInstance pi = n.procedureInstance();
        if (noImplementation(pi))
            return super.overrideTranslateLLVM(v); // Ignore native and abstract methods.

        // Build function type.
        Type retType = n instanceof MethodDecl
                ? ((MethodDecl) n).returnType().type()
                : ts.Void();
        List<Type> formalTypes = n.formals().stream()
                .map(Formal::declType)
                .collect(Collectors.toList());
        ReferenceType target = v.getCurrentClass().type().toReference();
        LLVMTypeRef funcType = pi.flags().isStatic()
                ? v.utils.toLLFuncTy(retType, formalTypes)
                : v.utils.toLLFuncTy(target, retType, formalTypes);

        LLVMValueRef funcRef = v.utils.getFunction(v.mod, v.mangler.mangleProcName(pi), funcType);
        v.debugInfo.funcDebugInfo(n, funcRef);

        // Note that the entry block is reserved exclusively for alloca instructions
        // and parameter initialization. Children translations will insert alloca instructions
        // into this block as needed.
        LLVMBasicBlockRef entry = LLVMAppendBasicBlockInContext(v.context, funcRef, "entry");
        LLVMBasicBlockRef body = LLVMAppendBasicBlockInContext(v.context, funcRef, "body");
        LLVMPositionBuilderAtEnd(v.builder, entry);

        for (int i = 0; i < n.formals().size(); ++i) {
            Formal formal = n.formals().get(i);
            LLVMTypeRef typeRef = v.utils.toLL(formal.declType());

            LLVMValueRef alloc = LLVMBuildAlloca(v.builder, typeRef, "arg." + formal.name());
            int idx = i + (pi.flags().isStatic() ? 0 : 1);
            LLVMBuildStore(v.builder, LLVMGetParam(funcRef, idx), alloc);
            v.addAllocation(formal.name(), alloc);

            v.debugInfo.createParamVariable(v, formal, i, alloc);
        }

        // Register as entry point if applicable.
        boolean isEntryPoint = n.name().equals("main")
                && n.flags().isPublic()
                && n.formals().size() == 1
                && n.formals().iterator().next().declType().equals(ts.arrayOf(ts.String()));
        if (isEntryPoint) {
            String className = (n.procedureInstance()).container().toString();
            v.addEntryPoint(funcRef, className);
        }

        v.pushFn(funcRef);
        v.addTranslation(n, funcRef);

        // Recurse to children.
        LLVMPositionBuilderAtEnd(v.builder, body);
        n.visitChildren(v);

        // Add void return if necessary.
        LLVMBasicBlockRef block = LLVMGetInsertBlock(v.builder);
        if (LLVMGetBasicBlockTerminator(block) == null) {
            if (retType.isVoid()) {
                LLVMBuildRetVoid(v.builder);
            } else {
                LLVMBuildUnreachable(v.builder);
            }
        }

        // We build this branch at the end since child translations need to be able
        // to insert into the entry block before its terminator. (LLVMPositionBuilderBefore
        // is inconvenient because it changes the debug location.)
        LLVMPositionBuilderAtEnd(v.builder, entry);
        LLVMBuildBr(v.builder, body);

        v.clearAllocations();
        v.popFn();
        v.debugInfo.popScope();
        return n;
    }
}
