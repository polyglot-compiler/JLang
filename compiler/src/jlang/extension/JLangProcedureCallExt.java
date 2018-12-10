//Copyright (C) 2018 Cornell University

package jlang.extension;

import org.bytedeco.javacpp.LLVM.LLVMTypeRef;
import org.bytedeco.javacpp.LLVM.LLVMValueRef;

import jlang.ast.JLangExt;
import jlang.visit.DesugarLocally;
import jlang.visit.LLVMTranslator;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.ProcedureCall;
import polyglot.ext.jl5.types.JL5Flags;
import polyglot.types.MethodInstance;
import polyglot.types.ProcedureInstance;
import polyglot.types.Type;
import polyglot.util.SerialVersionUID;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.javacpp.LLVM.LLVMBuildBitCast;

abstract class JLangProcedureCallExt extends JLangExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node desugar(DesugarLocally v) {
        ProcedureCall n = node();

        // Is it a variable-argument procedure call?
        ProcedureInstance pi = n.procedureInstance();
        if (!JL5Flags.isVarArgs(pi.flags()))
            return super.desugar(v);

        // Is the variable argument already in proper form? Read the JLS! (15.12.4.2)
        int lastFormalIdx = pi.formalTypes().size() - 1;
        Type lastFormalT = pi.formalTypes().get(lastFormalIdx);
        assert lastFormalT.isArray();
        if (n.arguments().size() == pi.formalTypes().size()) {
            Type lastArgT = n.arguments().get(lastFormalIdx).type();
            if (v.ts.isImplicitCastValid(lastArgT, lastFormalT)) {
                return super.desugar(v);
            }
        }

        // Extract the variable arguments.
        List<Expr> args = n.arguments();
        List<Expr> varargs = new ArrayList<>(args.subList(lastFormalIdx, args.size()));
        args = new ArrayList<>(args.subList(0, lastFormalIdx));

        // Append the new vararg array.
        Expr varargArr = v.nf.ArrayInit(n.position(), varargs).type(lastFormalT);
        args.add(varargArr);
        return n.arguments(args);
    }

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        ProcedureCall n = node();
        ProcedureInstance pi = n.procedureInstance();

        LLVMTypeRef retType = v.utils.toLL(v.utils.erasedReturnType(pi));
        LLVMTypeRef[] paramTypes = v.utils.toLLParamTypes(pi);
        LLVMTypeRef funcType = v.utils.functionType(retType, paramTypes);

        LLVMValueRef[] args = buildErasedArgs(v, paramTypes);
        LLVMValueRef funcPtr = buildFuncPtr(v, funcType);

        Type returnType = pi instanceof MethodInstance
                ? ((MethodInstance) pi).returnType()
                : v.ts.Void();

        if (returnType.isVoid()) {
            // Procedure call.
            v.utils.buildProcCall(funcPtr, args);
        }
        else {
            // Function call; bitcast result to handle erasure.
            LLVMValueRef call = v.utils.buildFunCall(funcPtr, args);
            LLVMTypeRef resType = v.utils.toLL(returnType);
            LLVMValueRef erasureCast = LLVMBuildBitCast(v.builder, call, resType, "cast.erasure");
            v.addTranslation(n, erasureCast);
        }

        return super.leaveTranslateLLVM(v);
    }

    /**
     * Returns the LLVM arguments for this call, including implicit receiver and JNI arguments.
     * Casts each argument to the type that the callee expects (due to erasure).
     */
    protected LLVMValueRef[] buildErasedArgs(LLVMTranslator v, LLVMTypeRef[] paramTypes) {
        ProcedureCall n = node();
        ProcedureInstance pi = n.procedureInstance();
        List<LLVMValueRef> args = new ArrayList<>();

        // Add receiver argument.
        if (!pi.flags().isStatic()) {
            args.add(buildReceiverArg(v));
        }

        // Add normal arguments.
        n.arguments().stream()
                .map(a -> (LLVMValueRef) v.getTranslation(a))
                .forEach(args::add);

        // Cast all arguments to erased types.
        LLVMValueRef[] res = new LLVMValueRef[args.size()];
        assert res.length == paramTypes.length;
        for (int i = 0; i < args.size(); ++i)
            res[i] = LLVMBuildBitCast(v.builder, args.get(i), paramTypes[i], "cast.erasure");
        return res;
    }

    /** Returns the receiver argument for this call. Only called if one exists. */
    protected abstract LLVMValueRef buildReceiverArg(LLVMTranslator v);

    /**
     * Returns the function pointer for this call.
     * Takes in a pre-computed function type to avoid having to recompute it.
     * Uses direct dispatch unless overridden.
     */
    protected LLVMValueRef buildFuncPtr(LLVMTranslator v, LLVMTypeRef funcType) {
        ProcedureCall n = node();
        String funcName = v.mangler.proc(n.procedureInstance());
        return v.utils.getFunction(funcName, funcType);
    }

    @Override
    public ProcedureCall node() {
        return (ProcedureCall) super.node();
    }
}
