package polyllvm;

import polyglot.ext.jl7.JL7Scheduler;
import polyglot.frontend.CyclicDependencyException;
import polyglot.frontend.JLExtensionInfo;
import polyglot.frontend.Job;
import polyglot.frontend.goals.EmptyGoal;
import polyglot.frontend.goals.Goal;
import polyglot.util.InternalCompilerError;
import polyllvm.util.PolyLLVMDesugared;

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

    @Override
    public Goal CodeGenerated(Job job) {
        Goal translate = new LLVMEmitted(job);
        try {
            translate.addPrerequisiteGoal(PrepareForLLVMOutput(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(translate);
    }
}
