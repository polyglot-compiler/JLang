package polyllvm;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.*;
import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.goals.CodeGenerated;
import polyglot.frontend.goals.EmptyGoal;
import polyglot.frontend.goals.Goal;
import polyglot.frontend.goals.VisitorGoal;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.visit.ExpressionFlattener;
import polyglot.visit.LoopNormalizer;
import polyglot.visit.MakeNarrowingAssignmentsExplicit;
import polyglot.visit.TypeChecker;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.visit.*;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMScheduler extends JLScheduler {

    public PolyLLVMScheduler(ExtensionInfo extInfo) {
        super(extInfo);
    }

    public Goal StringLiteralRemover(Job job) {
        ExtensionInfo extInfo = job.extensionInfo();
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = new VisitorGoal(job, new StringLiteralRemover(ts, nf));
        try {
            g.addPrerequisiteGoal(Disambiguated(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);
    }

    public Goal LoopNormalizer(Job job) {
        ExtensionInfo extInfo = job.extensionInfo();
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = new VisitorGoal(job, new LoopNormalizer(job, ts, nf));
        try {
            // Make sure we have type information before we translate things.
            g.addPrerequisiteGoal(StringLiteralRemover(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);
    }

    @Override
    public Goal TypeChecked(Job job) {
        ExtensionInfo extInfo = job.extensionInfo();
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = new VisitorGoal(job, new TypeChecker(job, ts, nf));
        try {
            g.addPrerequisiteGoal(LoopNormalizer(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);
    }

    @Override
    public Goal Serialized(Job job) {
        Goal g = new EmptyGoal(job, "Serialized");
        try {
            g.addPrerequisiteGoal(ExitPathsChecked(job), this);
            g.addPrerequisiteGoal(ConstructorCallsChecked(job), this);
            g.addPrerequisiteGoal(ReachabilityChecked(job), this);
            g.addPrerequisiteGoal(InitializationsChecked(job), this);
            g.addPrerequisiteGoal(ForwardReferencesChecked(job), this);
            g.addPrerequisiteGoal(ExceptionsChecked(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);
    }

    public Goal ExpressionFlattener(Job job) {
        ExtensionInfo extInfo = job.extensionInfo();
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = new VisitorGoal(job, new ExpressionFlattener(job, ts, nf));
        try {
            // Make sure we have type information before we translate things.
            g.addPrerequisiteGoal(Serialized(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);
    }

    public Goal FlattenVisitor(Job job) {
        ExtensionInfo extInfo = job.extensionInfo();
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = new EmptyGoal(job, "fv");//new VisitorGoal(job, new FlattenVisitor(ts, nf));
        try {
            // Make sure we have type information before we translate things.
            g.addPrerequisiteGoal(ExpressionFlattener(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);
    }

    public Goal MakeNarrowingAssignmentsExplicit(Job job) {
        ExtensionInfo extInfo = job.extensionInfo();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = new VisitorGoal(job, new MakeNarrowingAssignmentsExplicit(nf));
        try {
            g.addPrerequisiteGoal(FlattenVisitor(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);
    }

    public Goal AddPrimitiveWideningCasts(Job job) {
        ExtensionInfo extInfo = job.extensionInfo();
        PolyLLVMNodeFactory nf = (PolyLLVMNodeFactory) extInfo.nodeFactory();
        TypeSystem ts = extInfo.typeSystem();
        Goal g = new VisitorGoal(job,
                                 new AddPrimitiveWideningCastsVisitor(nf, ts));
        try {
            g.addPrerequisiteGoal(MakeNarrowingAssignmentsExplicit(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);
    }

    public Goal CodeCleaner(Job job) {
        ExtensionInfo extInfo = job.extensionInfo();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = new EmptyGoal(job, "CodeCleaner");//new VisitorGoal(job, new CodeCleaner(nf)); //
        try {
            // Make sure we have type information before we translate things.
            g.addPrerequisiteGoal(AddPrimitiveWideningCasts(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);
    }

    private static class LLVMOutputPass extends AbstractPass {
        LLVMOutputPass(Goal goal) {
            super(goal);
        }

        @Override
        public boolean run() {
            ExtensionInfo extInfo = goal.job().extensionInfo();
            PolyLLVMNodeFactory nf = (PolyLLVMNodeFactory) extInfo.nodeFactory();
            TypeSystem ts = extInfo.typeSystem();

            LLVMModuleRef mod = LLVMModuleCreateWithName(goal.job().source().name());
            LLVMBuilderRef builder = LLVMCreateBuilder();
            PseudoLLVMTranslator translator = new PseudoLLVMTranslator(mod, builder, nf, ts);

            Node ast = goal.job().ast();
            ast.visit(translator);

            // Verify.
            BytePointer error = new BytePointer((Pointer) null);
            LLVMVerifyModule(mod, LLVMPrintMessageAction, error);
            LLVMDisposeMessage(error);
            error.setNull();

            // TODO: Make this more robust.
            // Emit.
            String srcName = goal.job().source().name();
            String srcExt = extInfo.defaultFileExtension();
            String outExt = extInfo.getOptions().output_ext;
            String outName = srcName.substring(0, srcName.length() - srcExt.length()) + outExt;
            LLVMPrintModuleToFile(mod, outName, error);
            LLVMDisposeMessage(error);
            error.setNull();
            LLVMDisposeBuilder(builder);
            LLVMDisposeModule(mod);

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
        Goal g = new LLVMOutputGoal(job);
        try {
            // Make sure we have type information before we translate things.
            g.addPrerequisiteGoal(CodeCleaner(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);
    }
}
