package polyllvm.visit;

import polyglot.ast.*;
import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.Job;
import polyglot.frontend.Pass;
import polyglot.frontend.goals.AbstractGoal;
import polyglot.frontend.goals.Goal;
import polyglot.frontend.goals.VisitorGoal;
import polyglot.types.*;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.types.PolyLLVMTypeSystem;
import polyllvm.util.MultiGoal;

import java.util.*;
import java.util.stream.Collectors;

import static polyllvm.visit.DeclareCaptures.CAPTURE_PREFIX;

/** Converts captured local variable accesses to field accesses. */
public class DesugarLocalClasses extends AbstractGoal {
    private final Goal declare, substitute;

    public DesugarLocalClasses(Job job, PolyLLVMTypeSystem ts, PolyLLVMNodeFactory nf) {
        super(job, "Desugar local classes");
        CaptureContext captures = new CaptureContext(); // Share capture context.
        declare = new VisitorGoal(job, new DeclareCaptures(job, ts, nf, captures));
        substitute = new VisitorGoal(job, new SubstituteCaptures(job, ts, nf, captures));
    }

    @Override
    public Pass createPass(ExtensionInfo extInfo) {
        return new MultiGoal(job, declare, substitute).createPass(job.extensionInfo());
    }
}

/** A capture context maps class types to captured local variable instances. */
class CaptureContext {
    private final Map<ClassType, Set<LocalInstance>> captures = new HashMap<>();

    Set<LocalInstance> get(ClassType ct) {
        return captures.computeIfAbsent(ct, (key) -> new LinkedHashSet<>());
    }

    void add(ClassType ct, LocalInstance li) {
        get(ct).add(li);
    }
}

class DeclareCaptures extends DesugarVisitor {
    static final String CAPTURE_PREFIX = "capture$";
    private final List<ClassType> localClasses = new ArrayList<>();
    private final Map<LocalInstance, Integer> captureNestingLevels = new HashMap<>();
    private final CaptureContext captures;

    DeclareCaptures(
            Job job, PolyLLVMTypeSystem ts, PolyLLVMNodeFactory nf, CaptureContext captures) {
        super(job, ts, nf);
        this.captures = captures;
    }

    /**
     * Capture the given local variable in the outermost local class possible,
     * relative to the current position of the visitor in the AST.
     */
    private void tryCapture(LocalInstance li) {
        int nestingLevel = captureNestingLevels.get(li);
        if (nestingLevel < localClasses.size()) {
            ClassType captureClass = localClasses.get(nestingLevel);
            captures.add(captureClass, li);
        }
    }

    /** Capture all variables captured by the given class and its superclasses. */
    private void captureTransitive(ClassType classType) {
        captures.get(classType.toClass()).forEach(this::tryCapture);

        // Worried about querying captures on an unvisited class? There are two cases.
        // (1) If classType is a local class, then it is already visited (due to scoping rules).
        // (2) Otherwise, classType might not have been visited yet (e.g., non-static member classes
        //     can be referenced before they're declared). But then it has no captures of its
        //     own---only its superclasses may. So we traverse its superclasses here just in case,
        //     possibly duplicating work.
        Type superType = classType.superType();
        if (superType != null) {
            captureTransitive(superType.toClass());
        }
    }

    @Override
    public NodeVisitor enterDesugar(Node n) throws SemanticException {

        if (n instanceof ClassDecl) {
            ClassDecl cd = (ClassDecl) n;
            if (cd.type().isLocal()) {

                // Push local class.
                localClasses.add(cd.type());

                // Transitively capture the captures of all superclasses.
                captureTransitive(cd.type());
            }
        }

        // Map variable declarations to local class nesting level.
        if (n instanceof VarDecl) {
            VarDecl vd = (VarDecl) n;
            captureNestingLevels.put(vd.localInstance().orig(), localClasses.size());
        }

        // Capture local variables when applicable.
        if (n instanceof Local) {
            Local l = (Local) n;
            LocalInstance li = l.localInstance().orig();
            tryCapture(li);
        }

        // Capture variables needed to instantiate a new class that may have captures of its own.
        if (n instanceof New) {
            New nw = (New) n;
            ReferenceType container = nw.constructorInstance().container();
            if (container.isClass()) {
                captureTransitive(container.toClass());
            }
        }

        return super.enterDesugar(n);
    }

    @Override
    public Node leaveDesugar(Node n) throws SemanticException {

        if (n instanceof ClassDecl) {
            ClassDecl cd = (ClassDecl) n;
            ParsedClassType ct = cd.type();
            if (ct.isLocal()) {

                // Pop local class.
                localClasses.remove(localClasses.size() - 1);

                // Declare and initialize captures.
                List<FieldDecl> fields = captures.get(ct).stream()
                        .map((li) -> tnf.FieldDecl(
                                li.position(), CAPTURE_PREFIX + li.name(), li.type(),
                                ct, /*init*/ null, Flags.FINAL))
                        .collect(Collectors.toList());
                cd = prependConstructorInitializedFields(cd, fields);
            }
            n = cd;
        }

        return super.leaveDesugar(n);
    }
}

class SubstituteCaptures extends DesugarVisitor {
    private final List<ClassType> localClasses = new ArrayList<>();
    private final CaptureContext captures;

    SubstituteCaptures(
            Job job, PolyLLVMTypeSystem ts, PolyLLVMNodeFactory nf, CaptureContext captures) {
        super(job, ts, nf);
        this.captures = captures;
    }

    /** Translate a local variable to a capture field, if necessary. */
    private Expr translateLocal(Local l) {
        LocalInstance li = l.localInstance().orig();
        for (ClassType ct : localClasses) {
            if (captures.get(ct).contains(li)) {
                Position pos = l.position();
                Special receiver = tnf.This(pos, ct);
                return tnf.Field(pos, receiver, CAPTURE_PREFIX + li.name());
            }
        }
        return l;
    }

    /** Returns a list of expressions to initialize the capture fields of the given class. */
    private List<Expr> buildCaptureArgs(ClassType container) {
        return captures.get(container.toClass()).stream()
                .map((li) -> tnf.Local(li.position(), li))
                .map(this::translateLocal)
                .collect(Collectors.toList());
    }

    @Override
    public NodeVisitor enterDesugar(Node n) throws SemanticException {

        if (n instanceof ClassDecl) {
            ClassDecl cd = (ClassDecl) n;
            if (cd.type().isLocal()) {
                // Push local class.
                localClasses.add(cd.type());
            }
        }
        return super.enterDesugar(n);
    }

    @Override
    public Node leaveDesugar(Node n) throws SemanticException {

        if (n instanceof ClassDecl) {
            ClassDecl cd = (ClassDecl) n;
            if (cd.type().isLocal()) {
                // Pop local class.
                localClasses.remove(localClasses.size() - 1);
            }
        }

        // Map local variables to capture fields when possible.
        if (n instanceof Local) {
            n = translateLocal((Local) n);
        }

        // Supply extra arguments to object instantiations in order to initialize capture fields.
        if (n instanceof New) {
            New nw = (New) n;
            ReferenceType container = nw.constructorInstance().container();
            if (container.isClass() && container.toClass().isLocal()) {
                List<Expr> args = new ArrayList<>();
                args.addAll(buildCaptureArgs(container.toClass()));
                args.addAll(nw.arguments());
                n = tnf.New(nw.position(), container.toClass(), args);
            }
        }

        // Supply extra arguments to super constructor calls as well.
        if (n instanceof ConstructorCall) {
            ConstructorCall cc = (ConstructorCall) n;
            if (cc.kind() == ConstructorCall.SUPER) {
                ClassType container = cc.constructorInstance().container().toClass();
                if (container.isLocal()) {
                    List<Expr> args = new ArrayList<>();
                    args.addAll(buildCaptureArgs(container));
                    args.addAll(cc.arguments());
                    n = tnf.ConstructorCall(cc.position(), cc.kind(), container, args);
                }
            }
        }

        return super.leaveDesugar(n);
    }
}
