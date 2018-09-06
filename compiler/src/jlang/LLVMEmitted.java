//Copyright (C) 2017 Cornell University

package jlang;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;

import jlang.ast.JLangNodeFactory;
import jlang.types.JLangTypeSystem;
import jlang.visit.LLVMTranslator;
import polyglot.ast.Node;
import polyglot.ast.SourceFile;
import polyglot.frontend.*;
import polyglot.frontend.goals.CodeGenerated;
import polyglot.frontend.goals.Goal;
import polyglot.main.Options;
import polyglot.util.InternalCompilerError;
import polyglot.util.OptimalCodeWriter;
import polyglot.visit.PrettyPrinter;

import java.io.File;
import java.lang.Override;
import java.nio.file.Paths;

import static org.bytedeco.javacpp.LLVM.*;

class LLVMEmitted extends CodeGenerated {
    private boolean attempted = false;

    LLVMEmitted(Job job) {
        super(job);
    }

    @Override
    public Pass createPass(ExtensionInfo extInfo) {
        if (attempted)
            throw new InternalCompilerError("Cannot attempt to emit LLVM output twice");
        attempted = true;
        return new LLVMOutputPass(this);
    }

    private static class LLVMOutputPass extends AbstractPass {

        private LLVMOutputPass(Goal goal) {
            super(goal);
        }

        @Override
        public boolean run() {
            ExtensionInfo extInfo = goal.job().extensionInfo();
            JLangNodeFactory nf = (JLangNodeFactory) extInfo.nodeFactory();
            JLangTypeSystem ts = (JLangTypeSystem) extInfo.typeSystem();
            Node ast = goal.job().ast();
            if (!(ast instanceof SourceFile))
                throw new InternalCompilerError("AST root should be a SourceFile");
            SourceFile sf = (SourceFile) ast;

            if (((JLangOptions) Options.global).printDesugar) {
                new PrettyPrinter(lang()).printAst(ast, new OptimalCodeWriter(System.out, 80));
            }

            LLVMContextRef context = LLVMContextCreate();
            LLVMModuleRef mod = LLVMModuleCreateWithNameInContext(sf.source().name(), context);
            LLVMBuilderRef builder = LLVMCreateBuilderInContext(context);
            LLVMTranslator v = new LLVMTranslator(
                    sf.source().path(), context, mod, builder, ts, nf);

            try {
                v = (LLVMTranslator) v.begin();
                ast.visit(v);
                v.finish();
                goal.job().ast(ast);
            } catch (MissingDependencyException e) {
                throw new InternalCompilerError(
                        "Cannot handle missing dependencies during LLVM translation for "
                                + goal.job(), e);
            }

            LLVMDIBuilderFinalize(v.debugInfo.diBuilder);

            // Verify.
            BytePointer error = new BytePointer((Pointer) null);
            boolean verifySuccess = LLVMVerifyModule(mod, LLVMPrintMessageAction, error) == 0;
            LLVMDisposeMessage(error);
            error.setNull();

            // TODO: This data layout is likely only accurate for x86_64, Mac OS X.
            LLVMSetDataLayout(mod, "e-m:o-i64:64-f80:128-n8:16:32:64-S128");

            // Run passes.
            LLVMPassManagerRef pass = LLVMCreatePassManager();
            LLVMAddStripDeadPrototypesPass(pass);
            LLVMRunPassManager(pass, mod);
            LLVMDisposePassManager(pass);

            // Emit.
            String pkg = sf.package_() == null ? "" : sf.package_().toString();
            String outPath = extInfo.targetFactory().outputFileObject(pkg, sf.source()).getName();
            File dir = Paths.get(outPath).getParent().toFile();
            if (!dir.exists() && !dir.mkdirs()) {
                System.err.println("Failed to make output directory " + dir);
                System.exit(1);
            }
            LLVMPrintModuleToFile(mod, outPath, error);
            LLVMDisposeMessage(error);
            error.setNull();

            LLVMDIBuilderDestroy(v.debugInfo.diBuilder);
            LLVMDisposeBuilder(builder);
            LLVMDisposeModule(mod);
            LLVMContextDispose(context);

            if (!verifySuccess)
                throw new InternalCompilerError("The LLVM verifier found an issue in " + outPath);
            return true;
        }
    }
}
