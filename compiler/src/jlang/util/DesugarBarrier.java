//Copyright (C) 2018 Cornell University

package jlang.util;

import jlang.JLangScheduler;
import polyglot.frontend.Job;
import polyglot.frontend.goals.Barrier;
import polyglot.frontend.goals.Goal;

public class DesugarBarrier extends Barrier {

	private final JLangScheduler jlangScheduler;

	public static DesugarBarrier create(JLangScheduler s) {
		return new DesugarBarrier(s);
	}

	protected DesugarBarrier(JLangScheduler scheduler) {
		super(scheduler);
		this.jlangScheduler = scheduler;
	}

	@Override
	public Goal goalForJob(Job job) {
		return this.jlangScheduler.LLVMDesugared(job);
	}

}
