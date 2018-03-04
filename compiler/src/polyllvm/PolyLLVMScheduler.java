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
import polyglot.frontend.goals.VisitorGoal;
import polyglot.util.InternalCompilerError;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.types.PolyLLVMTypeSystem;
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

    /** Desugar passes which simplify LLVM translation. */
    public Goal PrepareForLLVMOutput(Job job) {
        ExtensionInfo extInfo = job.extensionInfo();
        PolyLLVMTypeSystem ts = (PolyLLVMTypeSystem) extInfo.typeSystem();
        PolyLLVMNodeFactory nf = (PolyLLVMNodeFactory) extInfo.nodeFactory();
        Goal[] goals = {
                // Visitor passes running after type checking must preserve type information.
                // However, running type check again after these passes can fail, for example
                // because the type checker could complain about visibility issues. That's ok.

                // Future desugar passes assume that anonymous classes have constructors and names.
                new VisitorGoal(job, new NameLocalClasses(job, ts, nf)),
                new VisitorGoal(job, new DeclareExplicitAnonCtors(job, ts, nf)),

                // Future desugar passes assume that class initialization code exists
                // within class constructors.
                new VisitorGoal(job, new DesugarClassInitializers(job, ts, nf)),

                // These desugar passes simplify various language constructs.
                // Their order should not matter, but future desugar desugar passes
                // must not create the constructs that these remove.
                new VisitorGoal(job, new DesugarEnums(job, ts, nf)),
                new VisitorGoal(job, new DesugarAsserts(job, ts, nf)),
                new VisitorGoal(job, new DesugarMultidimensionalArrays(job, ts, nf)),
                new VisitorGoal(job, new DesugarVarargs(job, ts, nf)),

                // Translates captures to field accesses.
                new DesugarLocalClasses(job, ts, nf),

                // Translates accesses to enclosing instances. Future desugar passes
                // should not create qualified Special nodes.
                new DesugarInnerClasses(job, ts, nf),

                // Local desugar transformations should be applied last.
                new VisitorGoal(job, new DesugarLocally(job, ts, nf))
        };
        Goal prep = new MultiGoal(job, goals);
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
            PolyLLVMTypeSystem ts = (PolyLLVMTypeSystem) extInfo.typeSystem();
            Node ast = goal.job().ast();
            if (!(ast instanceof SourceFile))
                throw new InternalCompilerError("AST root should be a SourceFile");
            SourceFile sf = (SourceFile) ast;

            // This is a good place to debug the output of desugar passes:
            // new PrettyPrinter(lang()).printAst(ast, new OptimalCodeWriter(System.out, 120));

            LLVMContextRef context = LLVMContextCreate();
            LLVMModuleRef mod = LLVMModuleCreateWithNameInContext(sf.source().name(), context);
            LLVMBuilderRef builder = LLVMCreateBuilderInContext(context);
            LLVMTranslator translator = new LLVMTranslator(
                    sf.source().path(), context, mod, builder, ts, nf);

            ast.visit(translator);

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
        private LLVMOutputGoal(Job job) {
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
