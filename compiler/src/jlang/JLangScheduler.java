//Copyright (C) 2018 Cornell University

package jlang;

import jlang.util.DesugarBarrier;
import jlang.util.JLangDesugared;
import polyglot.ast.ClassDecl;
import polyglot.ast.Lang;
import polyglot.ast.Node;
import polyglot.ext.jl7.JL7Scheduler;
import polyglot.frontend.CyclicDependencyException;
import polyglot.frontend.JLExtensionInfo;
import polyglot.frontend.Job;
import polyglot.frontend.MissingDependencyException;
import polyglot.frontend.goals.EmptyGoal;
import polyglot.frontend.goals.Goal;
import polyglot.frontend.goals.VisitorGoal;
import polyglot.main.Options;
import polyglot.types.ParsedClassType;
import polyglot.util.InternalCompilerError;
import polyglot.visit.NodeVisitor;

/**
 * Schedules the passes necessary to translate Java down to LLVM IR.
 */
public class JLangScheduler extends JL7Scheduler {

    public JLangScheduler(JLExtensionInfo extInfo) {
        super(extInfo);
    }

    @Override
    protected int maxRunCount() {
    	JLangOptions options = (JLangOptions) Options.global;
    	return (options.maxPasses > 0) ? options.maxPasses : super.maxRunCount();
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

    /**
     * Eagerly resolves the signatures for the classes declared in the given job.
     * Used to avoid missing dependencies during desugar transformations.
     */
    public Goal AllSignaturesResolved(Job job) {
        Lang lang = job.extensionInfo().nodeFactory().lang();
        Goal resolveAll = new VisitorGoal(job, new NodeVisitor(lang) {
            @Override
            public Node leave(Node old, Node n, NodeVisitor v) {
                if (n instanceof ClassDecl) {
                    ClassDecl cd = (ClassDecl) n;
                    ParsedClassType ct = cd.type();
                    if (!ct.signaturesResolved()) {
                        throw new MissingDependencyException(SignaturesResolved(ct));
                    }
                }
                return super.leave(old, n, v);
            }
        });
        try {
            resolveAll.addPrerequisiteGoal(Serialized(job), this);
        } catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(resolveAll);
    }

    /** Desugar passes which simplify LLVM translation. */
    public Goal LLVMDesugared(Job job) {
        Goal desugar = new JLangDesugared(job);
        try {
            desugar.addPrerequisiteGoal(AllSignaturesResolved(job), this);
        } catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(desugar);
    }

    /** 
     * Ensure that all desugar passes have completed.
     * The DesugarBarrier will call LLVMDesugared(Job)
     * for all jobs to generate its own prerequisites.
     */
    public Goal AllLLVMDesugared() {
    	return internGoal(DesugarBarrier.create(this));
    }

    @Override
    public Goal CodeGenerated(Job job) {
        Goal translate = new LLVMEmitted(job);
        try {
        	/* 
        	 * This is necessary since code generation of one job may
        	 * depend on the desugaring of another.
        	 */
            translate.addPrerequisiteGoal(AllLLVMDesugared(), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(translate);
    }

    @Override
    public Goal SignaturesResolved(ParsedClassType ct) {
        Goal g = super.SignaturesResolved(ct);
        try {
            if (ct.superType() != null) {
                ParsedClassType superType =
                        (ParsedClassType) ct.superType().toClass().declaration();
                g.addPrerequisiteGoal(SignaturesResolved(superType), this);
            }
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return g;
    }
}
