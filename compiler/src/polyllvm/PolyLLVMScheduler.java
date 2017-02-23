package polyllvm;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import polyglot.ast.NewArray;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.*;
import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.goals.CodeGenerated;
import polyglot.frontend.goals.EmptyGoal;
import polyglot.frontend.goals.Goal;
import polyglot.frontend.goals.VisitorGoal;
import polyglot.types.TypeSystem;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.LoopNormalizer;
import polyglot.visit.TypeChecker;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.util.LLVMUtils;
import polyllvm.util.PolyLLVMMangler;
import polyllvm.visit.*;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMScheduler extends JLScheduler {

    public PolyLLVMScheduler(ExtensionInfo extInfo) {
        super(extInfo);
    }

    public Goal LoopNormalizer(Job job) {
        ExtensionInfo extInfo = job.extensionInfo();
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = new VisitorGoal(job, new LoopNormalizer(job, ts, nf));
        try {
            g.addPrerequisiteGoal(Disambiguated(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);
    }

    public Goal AssignmentsDesugared(Job job) {
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = new VisitorGoal(job, new AssignmentDesugarVisitor(nf));
        try {
            g.addPrerequisiteGoal(LoopNormalizer(job), this);
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
            g.addPrerequisiteGoal(AssignmentsDesugared(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);
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

    public Goal StringConverter(Job job) {
        ExtensionInfo extInfo = job.extensionInfo();
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = new VisitorGoal(job, new StringConversionVisitor(job, ts, nf));
        try {
            g.addPrerequisiteGoal(AssignmentsDesugared(job), this);
            g.addPrerequisiteGoal(TypeChecked(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);
    }

    public Goal ReTypeChecked(Job job) {
        ExtensionInfo extInfo = job.extensionInfo();
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = new VisitorGoal(job, new TypeChecker(job, ts, nf));
        try {
            g.addPrerequisiteGoal(StringConverter(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);
    }

    public Goal MakeCastsExplicit(Job job) {
        ExtensionInfo extInfo = job.extensionInfo();
        PolyLLVMNodeFactory nf = (PolyLLVMNodeFactory) extInfo.nodeFactory();
        TypeSystem ts = extInfo.typeSystem();
        Goal g = new VisitorGoal(job, new MakeCastsExplicitVisitor(job, ts, nf));
        try {
            g.addPrerequisiteGoal(ReTypeChecked(job), this);
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

            //Setup class and dv types for arrays
            LLVMUtils.setupArrayType(translator);

            Node ast = goal.job().ast();
            ast.visit(translator);

            // Verify.
            BytePointer error = new BytePointer((Pointer) null);
            LLVMVerifyModule(mod, LLVMPrintMessageAction, error);
            LLVMDisposeMessage(error);
            error.setNull();

            // TODO: This data layout is likely only accurate for x86_64, Mac OS X.
            LLVMSetDataLayout(mod, "e-m:o-i64:64-f80:128-n8:16:32:64-S128");

            // Run passes.
            LLVMPassManagerRef pass = LLVMCreatePassManager();
            LLVMAddStripDeadPrototypesPass(pass);
            LLVMRunPassManager(pass, mod);
            LLVMDisposePassManager(pass);

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
        NodeFactory nf = extInfo.nodeFactory();
        Goal translate = new LLVMOutputGoal(job);
        try {
            translate.addPrerequisiteGoal(MakeCastsExplicit(job), this);

        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(translate);
    }
}
