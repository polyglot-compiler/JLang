package polyllvm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.Job;
import polyglot.frontend.Pass;
import polyglot.frontend.Scheduler;
import polyglot.frontend.goals.EndGoal;
import polyglot.frontend.goals.Goal;
import polyglot.frontend.goals.SourceFileGoal;
import polyllvm.ast.PolyLLVMLang_c;
import polyllvm.visit.LLVMPrinter;

public class LLVMCodeGenerated extends SourceFileGoal implements EndGoal {
    public static Goal create(Scheduler scheduler, Job job) {
        return scheduler.internGoal(new LLVMCodeGenerated(job));
    }

    /**
     * @param job The job to compile.
     */
    protected LLVMCodeGenerated(Job job) {
        super(job);
    }

    @Override
    public Pass createPass(ExtensionInfo extInfo) {
        return new LLVMOutputPass(this,
                                  new LLVMPrinter(job,
                                                  PolyLLVMLang_c.instance,
                                                  extInfo.targetFactory()));
    }

    @Override
    public Collection<Goal> prerequisiteGoals(Scheduler scheduler) {
        List<Goal> l = new ArrayList<>();
        l.add(scheduler.Serialized(job));
        l.addAll(super.prerequisiteGoals(scheduler));
        return l;
    }

}
