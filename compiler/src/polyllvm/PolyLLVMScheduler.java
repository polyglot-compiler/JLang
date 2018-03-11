package polyllvm;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import polyglot.ast.Node;
import polyglot.ast.SourceFile;
import polyglot.ext.jl7.JL7Scheduler;
import polyglot.frontend.*;
import polyglot.frontend.goals.CodeGenerated;
import polyglot.frontend.goals.EmptyGoal;
import polyglot.frontend.goals.Goal;
import polyglot.main.Options;
import polyglot.util.InternalCompilerError;
import polyglot.util.OptimalCodeWriter;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.types.PolyLLVMTypeSystem;
import polyllvm.util.PolyLLVMDesugared;
import polyllvm.visit.LLVMTranslator;

import java.io.File;
import java.lang.Override;
import java.nio.file.Paths;

import static org.bytedeco.javacpp.LLVM.*;

/**
 * Schedules the passes necessary to translate Java down to LLVM IR.
 */
public class PolyLLVMScheduler extends JL7Scheduler {

    public PolyLLVMScheduler(JLExtensionInfo extInfo) {
        super(extInfo);
    }

    @Override
    public Goal Serialized(Job job) {
        // Avoid serializing classes.
        Goal g = new EmptyGoal(job, "SerializedPlaceholder");
        try {
            g.addPrerequisiteGoal(Validated(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);
    }

    /** Desugar passes which simplify LLVM translation. */
    public Goal PrepareForLLVMOutput(Job job) {
        Goal desugar = new PolyLLVMDesugared(job);
        try {
            desugar.addPrerequisiteGoal(Serialized(job), this);
        } catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(desugar);
    }

    private static class LLVMOutputPass extends AbstractPass {

        LLVMOutputPass(Goal goal) {
            super(goal);
        }

        @Override
        public boolean run() {
            ExtensionInfo extInfo = goal.job().extensionInfo();
            PolyLLVMNodeFactory nf = (PolyLLVMNodeFactory) extInfo.nodeFactory();
            PolyLLVMTypeSystem ts = (PolyLLVMTypeSystem) extInfo.typeSystem();
            Node ast = goal.job().ast();
            if (!(ast instanceof SourceFile))
                throw new InternalCompilerError("AST root should be a SourceFile");
            SourceFile sf = (SourceFile) ast;

            if (((PolyLLVMOptions) Options.global).printDesugar) {
                new PrettyPrinter(lang()).printAst(ast, new OptimalCodeWriter(System.out, 80));
            }

            LLVMContextRef context = LLVMContextCreate();
            LLVMModuleRef mod = LLVMModuleCreateWithNameInContext(sf.source().name(), context);
            LLVMBuilderRef builder = LLVMCreateBuilderInContext(context);
            LLVMTranslator translator = new LLVMTranslator(
                    sf.source().path(), context, mod, builder, ts, nf);

            try {
                ast.visit(translator);
            } catch (MissingDependencyException e) {
                throw new InternalCompilerError(
                        "Cannot handle missing dependencies during LLVM translation", e);
            }

            LLVMDIBuilderFinalize(translator.debugInfo.diBuilder);

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

            LLVMDIBuilderDestroy(translator.debugInfo.diBuilder);
            LLVMDisposeBuilder(builder);
            LLVMDisposeModule(mod);
            LLVMContextDispose(context);

            if (!verifySuccess)
                throw new InternalCompilerError("The LLVM verifier found an issue in " + outPath);
            return true;
        }
    }

    private static class LLVMOutputGoal extends CodeGenerated {
        private boolean attempted = false;

        private LLVMOutputGoal(Job job) {
            super(job);
        }

        @Override
        public Pass createPass(ExtensionInfo extInfo) {
            if (attempted)
                throw new InternalCompilerError("Cannot attempt to emit LLVM output twice");
            attempted = true;
            return new LLVMOutputPass(this);
        }
    }

    @Override
    public Goal CodeGenerated(Job job) {
        Goal translate = new LLVMOutputGoal(job);
        try {
            translate.addPrerequisiteGoal(PrepareForLLVMOutput(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(translate);
    }
}
