package polyllvm;

import polyglot.ast.NodeFactory;
import polyglot.frontend.CyclicDependencyException;
import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.JLScheduler;
import polyglot.frontend.Job;
import polyglot.frontend.goals.EmptyGoal;
import polyglot.frontend.goals.Goal;
import polyglot.frontend.goals.VisitorGoal;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.visit.CodeCleaner;
import polyglot.visit.FlattenVisitor;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.visit.AddVoidReturnVisitor;
import polyllvm.visit.PseudoLLVMTranslator;
import polyllvm.visit.StringLiteralRemover;

public class PolyLLVMScheduler extends JLScheduler {

    public PolyLLVMScheduler(ExtensionInfo extInfo) {
        super(extInfo);
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

    public Goal StringLiteralRemover(Job job) {
        ExtensionInfo extInfo = job.extensionInfo();
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = new VisitorGoal(job, new StringLiteralRemover(ts, nf));
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
        Goal g = new VisitorGoal(job, new FlattenVisitor(ts, nf));
        try {
            // Make sure we have type information before we translate things.
            g.addPrerequisiteGoal(StringLiteralRemover(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);
    }

    public Goal AddVoidReturn(Job job) {
        ExtensionInfo extInfo = job.extensionInfo();
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = new VisitorGoal(job,
                                 new AddVoidReturnVisitor(ts,
                                                          (PolyLLVMNodeFactory) nf));
        try {
            // Make sure we have type information before we translate things.
            g.addPrerequisiteGoal(FlattenVisitor(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);
    }

    public Goal CodeCleaner(Job job) {
        ExtensionInfo extInfo = job.extensionInfo();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = new VisitorGoal(job, new CodeCleaner(nf));
        try {
            // Make sure we have type information before we translate things.
            g.addPrerequisiteGoal(AddVoidReturn(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);
    }

    public Goal LLVMTranslate(Job job) {
        ExtensionInfo extInfo = job.extensionInfo();
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();

        Goal g = new VisitorGoal(job,
                                 new PseudoLLVMTranslator(ts,
                                                          (PolyLLVMNodeFactory) nf));
        try {
            // Make sure we have type information before we translate things.
            g.addPrerequisiteGoal(CodeCleaner(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);
    }

    @Override
    public Goal CodeGenerated(Job job) {
        Goal g = LLVMCodeGenerated.create(this, job);
//        Goal g = new EmptyGoal(job, "CodeGenerated");
        // Add a prerequisite goal to translate CArray features.
        try {
            g.addPrerequisiteGoal(LLVMTranslate(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);
    }

//    @Override
//    public boolean runToCompletion() {
//        boolean complete = super.runToCompletion();
//        if (complete) {
//            // Call the compiler for output files to compile our translated
//            // code.
//            ExtensionInfo outExtInfo = extInfo.outputExtensionInfo();
//            Scheduler outScheduler = outExtInfo.scheduler();
//
//            // Create a goal to compile every source file.
//            for (Job job : outScheduler.jobs()) {
//                Job newJob = outScheduler.addJob(job.source(), job.ast());
//                outScheduler.addGoal(outExtInfo.getCompileGoal(newJob));
//            }
//            return outScheduler.runToCompletion();
//        }
//        return complete;
//    }
}
