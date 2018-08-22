package jlang.visit;

import polyglot.ast.*;
import polyglot.frontend.AbstractPass;
import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.Job;
import polyglot.frontend.Pass;
import polyglot.frontend.goals.AbstractGoal;
import polyglot.frontend.goals.Goal;
import polyglot.frontend.goals.VisitorGoal;
import polyglot.types.*;
import polyglot.util.Position;

import static jlang.visit.DeclareCaptures.CAPTURE_PREFIX;

import java.util.*;
import java.util.stream.Collectors;

import jlang.ast.JLangNodeFactory;
import jlang.types.JLangTypeSystem;

/**
 * Converts captured local variable accesses to field accesses.
 * This occurs in two passes:
 *
 * (1) The {@link DeclareCaptures} visitor
 *     - updates the names of local classes to ensure that qualified names are unique,
 *     - determines which local variables are captured by which classes,
 *     - declares "capture fields" to store these captures, and
 *     - initializes these fields using "capture formals" prepended to each constructor.
 *
 * (2) The {@link SubstituteCaptures} visitor
 *     - translates captured local variable accesses to field accesses, and
 *     - prepends capture arguments to super constructor calls and {@code new} expressions.
 *
 * Passes (1) and (2) could not be combined into one pass, because there could be an
 * expression of the form {@code new C(...)} inside the body of C, and we can't know
 * which captures to prepend as arguments until we've finished visiting C in its entirety.
 */
public class DesugarLocalClasses extends AbstractGoal {
    private final Goal declare, substitute;

    public DesugarLocalClasses(Job job, JLangTypeSystem ts, JLangNodeFactory nf) {
        super(job, "Desugar local classes");
        CaptureContext captures = new CaptureContext(); // Shared capture context.
        declare = new VisitorGoal(job, new DeclareCaptures(job, ts, nf, captures));
        substitute = new VisitorGoal(job, new SubstituteCaptures(job, ts, nf, captures));
    }

    @Override
    public Pass createPass(ExtensionInfo extInfo) {
        Pass declarePass = declare.createPass(extInfo);
        Pass substitutePass = substitute.createPass(extInfo);
        return new AbstractPass(this) {
            @Override
            public boolean run() {
                return declarePass.run() && substitutePass.run();
            }
        };
    }
}

/**
 * A capture context maps class types to captured local variable instances.
 * Uses {@link LocalInstance#orig()} to ensure that equality works as expected.
 */
class CaptureContext {
    private final Map<ClassType, Set<LocalInstance>> captures = new HashMap<>();

    Set<LocalInstance> get(ClassType ct) {
        return captures.computeIfAbsent(ct, (key) -> new LinkedHashSet<>());
    }

    void add(ClassType ct, LocalInstance li) {
        get(ct).add(li.orig());
    }
}

/**
 * Collects captured local variables and declares constructor-initialized capture fields.
 * A variable might be captured by a class C if it is
 * - referenced directly within C,
 * - captured by another class that is being instantiated within C through {@code new}, or
 * - captured by a superclass of C.
 *
 * Collecting captures in the face of arbitrary nesting and inheritance can get complicated.
 * We maintain a couple invariants:
 *
 * - Local variables are captured by the outermost class possible.
 *   That is, if a class C is nested within a class O, and C accesses a captured local variable x
 *   declared before O, then C will not capture x directly; instead, it will access x through its
 *   enclosing instance of O.
 *
 * - A corollary of the above is that *only* local classes can have captures.
 *   (Recall that local classes are those immediately enclosed by a code block.)
 *   Member classes nested inside local classes will always defer to their enclosing instance.
 */
class DeclareCaptures extends DesugarVisitor {
    static final String CAPTURE_PREFIX = "capture$";
    private final CaptureContext captures;

    /** Stack of local classes enclosing the current position of the visitor. */
    private final List<ClassType> localClasses = new ArrayList<>();

    /**
     * Maps local variable declarations to the number of enclosing local classes.
     * This is used to enforce that local variables are captured by the outermost class possible.
     */
    private final Map<LocalInstance, Integer> localClassNestingLevels = new HashMap<>();

    DeclareCaptures(
            Job job, JLangTypeSystem ts, JLangNodeFactory nf, CaptureContext captures) {
        super(job, ts, nf);
        this.captures = captures;
    }

    /**
     * Capture the given local variable in the outermost local class possible (if any),
     * relative to the current position of the visitor in the AST.
     */
    private void tryCapture(LocalInstance li) {
        int nestingLevel = localClassNestingLevels.get(li);
        if (nestingLevel < localClasses.size()) {
            ClassType captureClass = localClasses.get(nestingLevel);
            captures.add(captureClass, li);
        }
    }

    /** Capture all variables captured by the given class. */
    private void captureTransitive(ClassType classType) {
        // Worried about querying captures on an unvisited class? There are three cases.
        // (1) If classType is a local class not enclosing this one, then it is already visited
        //     (due to scoping rules).
        // (2) If classType is a local class enclosing this one, then we don't need its captures;
        //     they're already available through nesting.
        // (3) Otherwise, classType is not a local class, and it might not have been visited yet
        //     (e.g., non-static member classes can be referenced before they're declared). But
        //     then it has no captures of its own---only its superclasses may. So we traverse its
        //     superclasses below just in case.
        captures.get(classType).forEach(this::tryCapture);
        Type superType = classType.superType();
        if (superType != null) {
            captureTransitive(superType.toClass());
        }
    }

    @Override
    public void enterClassBody(ParsedClassType ct, ClassBody body) {
        super.enterClassBody(ct, body);

        if (ct.isLocal() || ct.isAnonymous()) {
            // Push local class.
            localClasses.add(ct);
        }

        // Transitively capture the captures of all superclasses.
        captureTransitive(ct);
    }

    @Override
    public void enterDesugar(Node n) throws SemanticException {
        super.enterDesugar(n);

        // Map variable declarations to local class nesting level.
        if (n instanceof VarDecl) {
            VarDecl vd = (VarDecl) n;
            localClassNestingLevels.put(vd.localInstance().orig(), localClasses.size());
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
    }

    @Override
    public ClassBody leaveClassBody(ParsedClassType ct, ClassBody cb) {

        if (ct.isLocal() || ct.isAnonymous()) {

            // Pop local class.
            localClasses.remove(localClasses.size() - 1);

            // Declare and initialize capture fields.
            List<FieldDecl> fields = captures.get(ct).stream()
                    .map((li) -> tnf.FieldDecl(
                            li.position(), ct,
                            Flags.FINAL, li.type(), CAPTURE_PREFIX + li.name(), /*init*/ null))
                    .collect(Collectors.toList());
            cb = prependConstructorInitializedFields(ct, cb, fields);
        }

        return super.leaveClassBody(ct, cb);
    }
}

/**
 * Translates captures to field accesses, and updates super constructor calls and {@code new}
 * expressions in order to initialize capture fields.
 */
class SubstituteCaptures extends DesugarVisitor {
    private final CaptureContext captures;

    /** Stack of local classes enclosing the current position of the visitor. */
    private final Deque<ClassType> localClasses = new ArrayDeque<>();

    SubstituteCaptures(
            Job job, JLangTypeSystem ts, JLangNodeFactory nf, CaptureContext captures) {
        super(job, ts, nf);
        this.captures = captures;
    }

    /** Translate a local variable to a capture field, if necessary. */
    private Expr translateLocal(Local l) {
        // Recall that we've maintained that l is captured by at most one enclosing class.
        LocalInstance li = l.localInstance().orig();
        for (ClassType ct : localClasses) {
            if (captures.get(ct).contains(li)) {
                String captureName = CAPTURE_PREFIX + li.name();

                // If we are inside a constructor, try to use a capture formal rather than the
                // capture field. This ensures that capture fields are not accessed in the
                // constructor before they are initialized.
                if (!constructors.isEmpty()) {
                    ConstructorDecl ctor = constructors.peek();
                    if (ts.typeEqualsErased(ctor.constructorInstance().container(), ct)) {
                        List<Formal> captureFormals = ctor.formals().stream()
                                .filter((f) -> f.name().equals(captureName))
                                .collect(Collectors.toList());
                        assert captureFormals.size() == 1;
                        Formal captureFormal = captureFormals.get(0);
                        return tnf.Local(l.position(), captureFormal);
                    }
                }

                // Otherwise, use the capture field.
                Special receiver = tnf.This(l.position(), ct);
                return tnf.Field(l.position(), receiver, captureName);
            }
        }
        return l;
    }

    /** Returns a list of expressions to initialize the capture fields of the given class. */
    private List<Expr> buildCaptureArgs(ClassType container) {
        return captures.get(container).stream()
                .map((li) -> tnf.Local(li.position(), li))
                .map(this::translateLocal)
                .collect(Collectors.toList());
    }

    @Override
    public void enterClassBody(ParsedClassType ct, ClassBody body) {
        // Push local class.
        super.enterClassBody(ct, body);
        if (ct.isLocal() || ct.isAnonymous()) {
            localClasses.push(ct);
        }
    }

    @Override
    public ClassBody leaveClassBody(ParsedClassType ct, ClassBody cb) {
        // Pop local class.
        if (ct.isLocal() || ct.isAnonymous())
            localClasses.remove();
        return super.leaveClassBody(ct, cb);
    }

    @Override
    public Node leaveDesugar(Node n) throws SemanticException {

        // Map local variables to capture fields when possible.
        if (n instanceof Local) {
            n = translateLocal((Local) n);
        }

        // Supply extra arguments to object instantiations in order to initialize capture fields.
        if (n instanceof New) {
            New nw = (New) n;
            ReferenceType rt = nw.constructorInstance().container();
            if (rt.isClass()) {
                ClassType container = rt.toClass();
                if (container.isLocal() || container.isAnonymous()) {
                    Position pos = nw.position();
                    List<Expr> args = concat(buildCaptureArgs(container), nw.arguments());
                    n = tnf.New(pos, nw.type().toClass(), nw.qualifier(), args, nw.body());
                }
            }
        }

        // Supply extra arguments to super constructor calls as well.
        if (n instanceof ConstructorCall) {
            ConstructorCall cc = (ConstructorCall) n;
            if (cc.kind().equals(ConstructorCall.SUPER)) {
                ClassType container = cc.constructorInstance().container().toClass();
                if (container.isLocal() || container.isAnonymous()) {
                    List<Expr> args = concat(buildCaptureArgs(container), cc.arguments());
                    n = tnf.ConstructorCall(cc.position(), cc.kind(), container, args);
                }
            }
        }

        return super.leaveDesugar(n);
    }
}
