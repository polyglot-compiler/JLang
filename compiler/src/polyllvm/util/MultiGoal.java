package polyllvm.util;

import polyglot.frontend.AbstractPass;
import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.Job;
import polyglot.frontend.Pass;
import polyglot.frontend.goals.AbstractGoal;
import polyglot.frontend.goals.Goal;

import java.util.stream.Stream;

public class MultiGoal extends AbstractGoal {
    private Goal[] goals;

    public MultiGoal(Job job, Goal... passes) {
        super(job);
        this.goals = passes;
    }

    private static class MultiPass extends AbstractPass {
        Pass[] passes;

        MultiPass(Goal goal, Pass... passes) {
            super(goal);
            this.passes = passes;
        }

        @Override
        public boolean run() {
            for (Pass p : passes)
                if (!p.run())
                    return false;
            return true;
        }
    }

    @Override
    public Pass createPass(ExtensionInfo extInfo) {
        Pass[] passes = Stream.of(goals)
                .map(g -> g.createPass(job.extensionInfo()))
                .toArray(Pass[]::new);
        return new MultiPass(this, passes);
    }
}
