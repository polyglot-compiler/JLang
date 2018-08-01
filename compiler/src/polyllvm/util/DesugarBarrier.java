package polyllvm.util;

import polyglot.frontend.Job;
import polyglot.frontend.goals.Barrier;
import polyglot.frontend.goals.Goal;
import polyllvm.PolyLLVMScheduler;

public class DesugarBarrier extends Barrier {

	private final PolyLLVMScheduler polyllvmScheduler;

	public static DesugarBarrier create(PolyLLVMScheduler s) {
		return new DesugarBarrier(s);
	}

	protected DesugarBarrier(PolyLLVMScheduler scheduler) {
		super(scheduler);
		this.polyllvmScheduler = scheduler;
	}

	@Override
	public Goal goalForJob(Job job) {
		return this.polyllvmScheduler.LLVMDesugared(job);
	}

}
