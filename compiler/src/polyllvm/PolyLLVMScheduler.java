package polyllvm;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import polyglot.ast.Node;
import polyglot.ast.SourceFile;
import polyglot.ext.jl5.visit.AutoBoxer;
import polyglot.ext.jl5.visit.RemoveExtendedFors;
import polyglot.ext.jl7.JL7Scheduler;
import polyglot.ext.jl7.types.JL7TypeSystem;
import polyglot.frontend.*;
import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.goals.CodeGenerated;
import polyglot.frontend.goals.EmptyGoal;
import polyglot.frontend.goals.Goal;
import polyglot.frontend.goals.VisitorGoal;
import polyglot.util.InternalCompilerError;
import polyglot.visit.InnerClassRemover;
import polyglot.visit.TypeChecker;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.util.MultiGoal;
import polyllvm.visit.*;

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

    public Goal PrepareForLLVMOutput(Job job) {
        ExtensionInfo extInfo = job.extensionInfo();
        JL7TypeSystem ts = (JL7TypeSystem) extInfo.typeSystem();
        PolyLLVMNodeFactory nf = (PolyLLVMNodeFactory) extInfo.nodeFactory();
        Goal prep = new MultiGoal(
                job,
                new VisitorGoal(job, new AutoBoxer(job, ts, nf)),
                //TODO: Translate some of these directly.
                new VisitorGoal(job, new InnerClassRemover(job, ts, nf)),
                new VisitorGoal(job, new TypeChecker(job, ts, nf)), // Re-type-check classes.
                new VisitorGoal(job, new ClassInitializerVisitor(job, ts, nf)),
                new VisitorGoal(job, new AssertionVisitor(job, ts, nf)),
                new VisitorGoal(job, new TypeChecker(job, ts, nf)), // Re-type-check assignments.
                new VisitorGoal(job, new StringConversionVisitor(job, ts, nf)),
                new VisitorGoal(job, new TypeChecker(job, ts, nf)), // Re-type-check string ops.
                // TODO: Review RemoveExtendedFors, which has questionable behavior on debug info.
                new VisitorGoal(job, new RemoveExtendedFors(job, ts, nf)),
                new VisitorGoal(job, new MakeCastsExplicitVisitor(job, ts, nf))
        );
        try {
            prep.addPrerequisiteGoal(Serialized(job), this);
        } catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(prep);
    }

    private static class LLVMOutputPass extends AbstractPass {

        LLVMOutputPass(Goal goal) {
            super(goal);
        }

        @Override
        public boolean run() {
            ExtensionInfo extInfo = goal.job().extensionInfo();
            PolyLLVMNodeFactory nf = (PolyLLVMNodeFactory) extInfo.nodeFactory();
            JL7TypeSystem ts = (JL7TypeSystem) extInfo.typeSystem();
            Node ast = goal.job().ast();
            if (!(ast instanceof SourceFile))
                throw new InternalCompilerError("AST root should be a SourceFile");
            SourceFile sf = (SourceFile) ast;

            LLVMContextRef context = LLVMContextCreate();
            LLVMModuleRef mod = LLVMModuleCreateWithNameInContext(sf.source().name(), context);
            LLVMBuilderRef builder = LLVMCreateBuilderInContext(context);
            LLVMTranslator translator = new LLVMTranslator(sf.source().path(), context, mod, builder, nf, ts);

            ast.visit(translator);

            LLVMDIBuilderFinalize(translator.debugInfo.diBuilder);

            // Verify.
            BytePointer error = new BytePointer((Pointer) null);
            LLVMVerifyModule(mod, LLVMAbortProcessAction, error);
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

            return true;
        }
    }

    private static class LLVMOutputGoal extends CodeGenerated {
        LLVMOutputGoal(Job job) {
            super(job);
        }

        @Override
        public Pass createPass(ExtensionInfo extInfo) {
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
