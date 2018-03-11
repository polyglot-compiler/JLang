package polyllvm.util;

import polyglot.frontend.*;
import polyglot.frontend.goals.AbstractGoal;
import polyglot.frontend.goals.Goal;
import polyglot.frontend.goals.VisitorGoal;
import polyglot.types.ParsedClassType;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.types.PolyLLVMTypeSystem;
import polyllvm.visit.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Runs all PolyLLVM desugar passes so that the AST is
 * fully desugared and ready for emitting LLVM IR.
 */
public class PolyLLVMDesugared extends AbstractGoal {
    private boolean attempted = false;
    private final Goal[] goals;

    public PolyLLVMDesugared(Job job) {
        super(job);

        ExtensionInfo extInfo = job.extensionInfo();
        PolyLLVMTypeSystem ts = (PolyLLVMTypeSystem) extInfo.typeSystem();
        PolyLLVMNodeFactory nf = (PolyLLVMNodeFactory) extInfo.nodeFactory();

        goals = new Goal[] {
                // Visitor passes running after type checking must preserve type information.
                // However, running type check again after these passes can fail, for example
                // because the type checker could complain about visibility issues. That's ok.

                // Future desugar passes assume that anonymous classes have constructors and names.
                new VisitorGoal(job, new NameLocalClasses(job, ts, nf)),
                new VisitorGoal(job, new DeclareExplicitAnonCtors(job, ts, nf)),

                // Future desugar passes assume that class initialization code exists
                // within class constructors.
                new VisitorGoal(job, new DesugarClassInitializers(job, ts, nf)),

                // Translate enums to normal classes.
                new VisitorGoal(job, new DesugarEnums(job, ts, nf)),

                // Translate captures to field accesses.
                new DesugarLocalClasses(job, ts, nf),

                // Translate accesses to enclosing instances. Future desugar passes
                // should not create qualified Special nodes.
                new DesugarInnerClasses(job, ts, nf),

                // Declare static fields to hold class objects,
                new VisitorGoal(job, new DeclareClassObjects(job, ts, nf)),

                // Local desugar transformations should be applied last.
                new VisitorGoal(job, new DesugarLocally(job, ts, nf))
        };
    }

    @Override
    public Collection<Goal> prerequisiteGoals(Scheduler scheduler) {
        TypeSystem ts = job.extensionInfo().typeSystem();
        List<Goal> prereqs = new ArrayList<>(super.prerequisiteGoals(scheduler));

        // Resolve signatures for any class that we might reference in a
        // desugar transformation.
        String[] neededTypes = {
                "java.lang.Class",
                "java.lang.ClassCastException",
                "java.lang.NullPointerException",
                "java.lang.IndexOutOfBoundsException",
                "java.lang.ArrayIndexOutOfBoundsException",
                "java.lang.AssertionError",
                "java.lang.Exception",
                "java.lang.Error",
                Constants.RUNTIME_ARRAY,
                Constants.RUNTIME_ARRAY_TYPE,
                Constants.RUNTIME_HELPER
        };
        try {
            for (String t : neededTypes) {
                prereqs.add(scheduler.SignaturesResolved((ParsedClassType) ts.typeForName(t)));
            }
        } catch (SemanticException e) {
            throw new InternalCompilerError(e);
        }

        return prereqs;
    }

    @Override
    public Pass createPass(ExtensionInfo extInfo) {
        // Make sure we have not already run the desugar passes.
        if (attempted)
            throw new InternalCompilerError(
                    "Desugaring passes should only be run once per job.\n" +
                            "They are not idempotent!\n" + job.toString());
        attempted = true;

        Pass[] passes = Stream.of(goals)
                .map(g -> g.createPass(extInfo))
                .toArray(Pass[]::new);

        return new AbstractPass(this) {

            @Override
            public boolean run() {

                // Run all sub-passes in sequence.
                for (Pass p : passes) {
                    try {
                        if (!p.run()) {
                            throw new InternalCompilerError("Desugar pass did not succeed: " + p);
                        }
                    } catch (MissingDependencyException e) {
                        // Intercept missing dependency exceptions, since these would
                        // lead to rerunning all desugar passes.
                        throw new InternalCompilerError(
                                "Missing dependency while running desugar pass: " + p + "." +
                                        "\nMay need to add the dependency explicitly.", e);
                    }
                }

                return true;
            }
        };
    }
}
