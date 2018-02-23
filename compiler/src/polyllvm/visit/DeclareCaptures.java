package polyllvm.visit;

import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.types.*;
import polyglot.visit.NodeVisitor;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.types.PolyLLVMTypeSystem;

import java.util.*;
import java.util.stream.Collectors;

public class DeclareCaptures extends DesugarVisitor {
    private static final String CAPTURE_STR = "capture$";
    private List<ClassType> localClasses = new ArrayList<>();
    private Map<LocalInstance, Integer> captureNestingLevels = new HashMap<>();
    private Map<ClassType, Set<LocalInstance>> captures = new HashMap<>();

    DeclareCaptures(Job job, PolyLLVMTypeSystem ts, PolyLLVMNodeFactory nf) {
        super(job, ts, nf);
    }

    private Set<LocalInstance> getCaptures(ClassType ct) {
        return captures.computeIfAbsent(ct, (key) -> new LinkedHashSet<>());
    }

    /**
     * Capture the given local variable in the outermost local class possible,
     * relative to the current position of the visitor in the AST.
     */
    private void tryCapture(LocalInstance li) {
        int nestingLevel = captureNestingLevels.get(li);
        if (nestingLevel < localClasses.size()) {
            ClassType captureClass = localClasses.get(nestingLevel);
            getCaptures(captureClass).add(li);
        }
    }

    /** Capture all variables captured by the given class and its superclasses. */
    private void captureTransitive(ClassType classType) {
        getCaptures(classType.toClass()).forEach(this::tryCapture);

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
            if (nw.type().isClass()) {
                captureTransitive(nw.type().toClass());
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
                List<FieldDecl> fields = getCaptures(ct).stream()
                        .map((li) -> tnf.FieldDecl(
                                li.position(), CAPTURE_STR + li.name(), li.type(),
                                ct, /*init*/ null, Flags.FINAL))
                        .collect(Collectors.toList());
                cd = prependConstructorInitializedFields(cd, fields);
            }
            n = cd;
        }

        return super.leaveDesugar(n);
    }
}
