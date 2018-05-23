package polyllvm.visit;

import org.bytedeco.javacpp.LLVM.*;
import polyglot.ast.Node;
import polyglot.ext.jl5.types.*;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;
import polyglot.util.ListUtil;
import polyglot.visit.NodeVisitor;
import polyllvm.ast.PolyLLVMLang;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.extension.PolyLLVMTryExt.ExceptionFrame;
import polyllvm.structures.*;
import polyllvm.types.PolyLLVMTypeSystem;
import polyllvm.util.DebugInfo;
import polyllvm.util.LLVMUtils;
import polyllvm.util.PolyLLVMMangler;
import polyllvm.util.TypedNodeFactory;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.bytedeco.javacpp.LLVM.*;

/** Translates Java into LLVM IR. */
public class LLVMTranslator extends NodeVisitor {
    public final PolyLLVMNodeFactory nf;
    public final PolyLLVMTypeSystem ts;
    public final TypedNodeFactory tnf;

    // Note: using pointer equality here!
    private Map<Object, Object> translations = new IdentityHashMap<>();

    public final LLVMContextRef context;
    public final LLVMModuleRef mod;
    public final LLVMBuilderRef builder;
    public final DebugInfo debugInfo;
    public final LLVMUtils utils;
    public final ClassObjects classObjs;
    public final PolyLLVMMangler mangler;
    public final ObjectStruct obj;
    public final DispatchVector dv;

    private int ctorCounter;

    public int incCtorCounter() {
        return ctorCounter++;
    }

    /**
     * A function context stores data such as exception frames
     * and label maps for the current function.
     */
    private static class FnCtxt {
        final LLVMValueRef fn;

        /** Stack of nested exception frames, innermost first. */
        final Deque<ExceptionFrame> exceptionFrames = new ArrayDeque<>();

        /**
         * Map from labels to IR locations.
         * Labels cannot be shadowed within a function,
         * so no need to worry about overwriting previous mappings.
         */
        final Map<String, LabeledStmtLocs> labelMap = new HashMap<>();

        FnCtxt(LLVMValueRef fn) {
            this.fn = fn;
        }
    }

    /**
     * A stack of all enclosing function contexts.
     * A stack is needed to handle nested classes.
     */
    private Deque<FnCtxt> fnCtxts = new ArrayDeque<>();

    private FnCtxt fnCtxt() { return fnCtxts.getFirst(); }

    /** Must be called when entering a function. */
    public void pushFn(LLVMValueRef fn) { fnCtxts.push(new FnCtxt(fn)); }

    /** Must be called when leaving a function. */
    public void popFn() { fnCtxts.pop(); }

    public LLVMValueRef currFn() { return fnCtxts.peek().fn; }

    /** A list of all potential entry points (i.e., Java main functions). */
    private Map<String, LLVMValueRef> entryPoints = new HashMap<>();

    public void addEntryPoint(LLVMValueRef entryPoint, String className) {
        entryPoints.put(className, entryPoint);
    }

    public Map<String, LLVMValueRef> getEntryPoints() {
        return new HashMap<>(entryPoints);
    }

    /**
     * A list of ctor functions to be added to the module. Each value in the
     * list is a struct of the form {priority, ctorFunction, data}
     */
    private List<LLVMValueRef> ctors = new ArrayList<>();

    public void addCtor(LLVMValueRef ctor) {
        ctors.add(ctor);
    }

    public List<LLVMValueRef> getCtors() {
        return ListUtil.copy(ctors, false);
    }

    public LLVMTranslator(
            String filePath, LLVMContextRef context,
            LLVMModuleRef mod, LLVMBuilderRef builder,
            PolyLLVMTypeSystem ts, PolyLLVMNodeFactory nf) {
        super(nf.lang());
        this.context = context;
        this.mod = mod;
        this.builder = builder;
        this.debugInfo = new DebugInfo(this, mod, filePath);
        this.utils = new LLVMUtils(this);
        this.classObjs = new ClassObjects(this);
        this.mangler = new PolyLLVMMangler(this);
        this.obj = createObjectStruct();
        this.dv = createDispatchVector();
        this.nf = nf;
        this.ts = ts;
        this.tnf = new TypedNodeFactory(ts, nf);
    }

    protected ObjectStruct createObjectStruct() {
        return new ObjectStruct_c(this);
    }

    protected DispatchVector createDispatchVector() {
        return new DispatchVector_c(this);
    }

    @Override
    public <N extends Node> N visitEdge(Node parent, N child) {
        // Switch the debug location to that of the child node.
        LLVMValueRef prevDebugLoc = debugInfo.getLocation();
        debugInfo.setLocation(child);

        child = super.visitEdge(parent, child);

        // Restore debug location.
        debugInfo.setLocation(prevDebugLoc);
        return child;
    }

    @Override
    public PolyLLVMLang lang() {
        return (PolyLLVMLang) super.lang();
    }

    @Override
    public Node override(Node parent, Node n) {
        return lang().overrideTranslateLLVM(parent, n, this);
    }

    @Override
    public NodeVisitor enter(Node n) {
        return lang().enterTranslateLLVM(n, this);
    }

    @Override
    public Node leave(Node old, Node n, NodeVisitor v) {
        return lang().leaveTranslateLLVM(n, this);
    }

    /** Add the translation from a Polyglot node to LLVM IR. */
    public void addTranslation(Object n, Object ll) {
        if (translations.containsKey(n))
            throw new InternalCompilerError("Already translated " + n.getClass() + ": " + n + "\n" +
                    "This may indicate an AST node appearing twice in the AST without copy(),\n" +
                    "or a node visited twice during translation.");
        translations.put(n, ll);
    }

    /** Return the translation for a Polyglot node. */
    @SuppressWarnings("unchecked")
    public <T> T getTranslation(Object n) {
        T res = (T) translations.get(n);
        if (res == null)
            throw new InternalCompilerError("Null translation of " + n.getClass() + ": " + n);
        return res;
    }

    /**
     * @return
     *         <ul>
     *         <li>the greatest uninstantiated superclass type that declares
     *         method {@code mi} if such a type exists;</li>
     *         <li>the {@link MemberInstance#container()} of
     *         {@code mi} otherwise.</li>
     *         </ul>
     */
    public ReferenceType declaringType(MethodInstance mi) {
        List<MethodInstance> overrides = mi.overrides();
        Optional<ReferenceType> highestSuperType = overrides.stream()
                .map(MemberInstance::container)
                .max((o1, o2) -> o1.descendsFrom(o2) ? -1 : 1);
        return highestSuperType.orElse(mi.container());
    }

    /**
     * @param mi
     * @return true if method {@code mi} does not override any method declared
     *         in a superclass.
     */
    public boolean isNonOverriding(MethodInstance mi) {
        return !mi.flags().isStatic()
                && mi.container().typeEquals(declaringType(mi));
    }

    /**
     * @param rt
     *            Should be an array type or a
     *            "{@link LLVMTranslator#isArrayOrPlainClass plain}" class type.
     * @return The type hierarchy as defined by
     *         {@link ReferenceType#superType()}. The last element is
     *         {@code rt}, and the first element is {@code java.lang.Object}.
     */
    public List<ReferenceType> classHierarchy(ReferenceType rt) {
        assert isArrayOrPlainClass(rt);

        LinkedList<ReferenceType> res = new LinkedList<>();
        ReferenceType curClass = rt;
        do {
            res.addFirst(curClass);
            curClass = curClass.superType() == null ? null
                    : curClass.superType().toReference();
        } while (curClass != null);
        return res;
    }

    /** Caches class-dispatch-vector methods. */
    private Map<ReferenceType, List<MethodInstance>> cdvMethodsCache = new IdentityHashMap<>();

    /** Caches interface-dispatch-vector methods. */
    private HashMap<ClassType, List<MethodInstance>> idvMethodsCache = new HashMap<>();

    /**
     * Returns the list of methods in the class dispatch
     * vector (CDV) of the reference type {@param rt}.
     *
     * The actual dispatch vector methods may differ due to
     * erasure. We preserve type variable substitutions here
     * because they're needed to determine whether methods
     * are override-equivalent. For example, consider the following.
     *
     * <code>
     * class A<T> {
     *     void f(T t) {};
     * }
     *
     * class B extends A<String> {
     *     void f(Object o) {};
     *     void f(String s) {}; // Overrides A<T>#f(T).
     * }
     * </code>
     *
     * If we eagerly erased the supertype of B, then we could not
     * know which method overrides {@code A<T>#f(T)}.
     */
    public List<MethodInstance> cdvMethods(ReferenceType rt) {

        if (cdvMethodsCache.containsKey(rt))
            return cdvMethodsCache.get(rt);

        // Start with all methods from the supertype.
        List<MethodInstance> supMethods = rt.superType() != null
                ? cdvMethods(rt.superType().toClass())
                : Collections.emptyList();
        List<MethodInstance> res = new ArrayList<>(supMethods);

        // Look for overriding methods.
        for (ListIterator<MethodInstance> it = res.listIterator(); it.hasNext(); ) {
            MethodInstance supMi = it.next();

            if (!ts.isAccessible(supMi, rt))
                continue; // If not visible, cannot override.

            List<? extends MethodInstance> declared = rt.methodsNamed(supMi.name());
            for (MethodInstance mi : declared) {
                if (ts.areOverrideEquivalent((JL5MethodInstance) mi, (JL5MethodInstance) supMi)) {
                    it.set(mi);
                    break;
                }
            }
        }

        // Add remaining methods from this class.
        rt.methods().stream()
                .filter(mi -> !mi.flags().isStatic())
                .filter(mi -> !res.contains(mi))
                .sorted(Comparator.comparing(MethodInstance::name))
                .forEach(res::add);

        cdvMethodsCache.put(rt, res);
        return res;
    }

    /**
     * Returns the list of methods in the interface
     * dispatch vector (IDV) for interface type {@code ct}.
     */
    public List<MethodInstance> idvMethods(ClassType ct) {
        assert ct.flags().isInterface();

        if (idvMethodsCache.containsKey(ct))
            return idvMethodsCache.get(ct);

        List<MethodInstance> res = new ArrayList<>();
        for (ClassType it : allInterfaces(ct)) {
            for (MethodInstance mj : it.methods()) {
                if (mj.flags().isStatic())
                    continue; // Interfaces may have static methods after certain desugar passes.
                boolean novel = res.stream()
                        .noneMatch(mi -> ts.areOverrideEquivalent(
                                (JL5MethodInstance) mi,
                                (JL5MethodInstance) mj));
                if (novel) {
                    res.add(mj);
                }
            }
        }

        idvMethodsCache.put(ct, res);
        return res;
    }

    /**
     * Returns a list of all interfaces that {@code rt} implements,
     * including itself if applicable. No duplicates.
     */
    public List<ClassType> allInterfaces(ReferenceType rt) {
        List<ClassType> res = new ArrayList<>();
        if (rt.isClass() && rt.toClass().flags().isInterface())
            res.add(rt.toClass());
        for (ReferenceType it : rt.interfaces())
            res.addAll(allInterfaces(it));
        if (rt.superType() != null)
            res.addAll(allInterfaces(rt.superType().toReference()));

        return res;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Loops, labels, break, and continue.
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Represents the target of a break or continue statement.
     * Usually targets the beginning or end of a loop, the end of a switch statement, or
     * the end of a labeled statement.
     */
    public class ControlTransferLoc {
        private LLVMBasicBlockRef block;

        // The try-catch nesting level is used to determine how many try-catch finally blocks
        // we need to run before jumping to this location.
        private int tryCatchNestingLevel = exceptionFrames().size();

        ControlTransferLoc(LLVMBasicBlockRef block) {
            this.block = block;
        }

        public LLVMBasicBlockRef getBlock() {
            return block;
        }

        public int getTryCatchNestingLevel() {
            return tryCatchNestingLevel;
        }
    }

    private Map<String, LabeledStmtLocs> labelMap() {
        return fnCtxt().labelMap;
    }

    // The last items in these stacks hold the destination of unlabeled continue/break statements.
    // Note that these stacks may have different sizes because switch statements
    // only create a break-block.
    private Deque<ControlTransferLoc> continueLocs = new ArrayDeque<>();
    private Deque<ControlTransferLoc> breakLocs = new ArrayDeque<>();

    /**
     * Holds the beginning and end of a labeled statement.
     */
    private class LabeledStmtLocs {
        ControlTransferLoc head, end;

        LabeledStmtLocs(LLVMBasicBlockRef head, LLVMBasicBlockRef end) {
            this.head = new ControlTransferLoc(head);
            this.end = new ControlTransferLoc(end);
        }
    }

    public void pushLabel(String label, LLVMBasicBlockRef before, LLVMBasicBlockRef after) {
        labelMap().put(label, new LabeledStmtLocs(before, after));
    }

    public void popLabel(String label) {
        labelMap().remove(label);
    }

    public void pushLoop(LLVMBasicBlockRef head, LLVMBasicBlockRef end) {
        continueLocs.addLast(new ControlTransferLoc(head));
        breakLocs.addLast(new ControlTransferLoc(end));
    }

    public void popLoop() {
        continueLocs.removeLast();
        breakLocs.removeLast();
    }

    public void pushSwitch(LLVMBasicBlockRef end) {
        breakLocs.addLast(new ControlTransferLoc(end));
    }

    public void popSwitch() {
        breakLocs.removeLast();
    }

    public ControlTransferLoc getContinueLoc(String label) {
        return label == null ? continueLocs.getLast() : labelMap().get(label).head;
    }

    public ControlTransferLoc getBreakLoc(String label) {
        return label == null ? breakLocs.getLast() : labelMap().get(label).end;
    }

    /**
     * Encapsulates the information necessary for translating method calls.
     */
    public static class DispatchInfo {
        private boolean isClassDisp;
        private int methodIndex;
        private ClassType intfErasure;

        static DispatchInfo createClassDispInfo(int methodIndex) {
            return new DispatchInfo(true, methodIndex, null);
        }

        static DispatchInfo createIntfDispInfo(int methodIndex,
                ClassType recvErasure) {
            return new DispatchInfo(false, methodIndex, recvErasure);
        }

        private DispatchInfo(boolean isClassDisp, int methodIndex,
                ClassType recvErasure) {
            this.isClassDisp = isClassDisp;
            this.methodIndex = methodIndex;
            this.intfErasure = recvErasure;
        }

        /**
         * @return true if this is a class method dispatch, or false if this is
         *         an interface method dispatch.
         */
        public boolean isClassDisp() {
            return isClassDisp;
        }

        /**
         * @return the index of the method in the (class or interface) dispatch
         *         vector
         */
        public int methodIndex() {
            return methodIndex;
        }

        /**
         * @return the erasure of the interface if this is an interface method
         *         dispatch; undefined otherwise.
         */
        public ClassType intfErasure() {
            assert !isClassDisp;
            return intfErasure;
        }
    }

    /**
     * @param recvTy
     * @param mi
     *            the method <em>without</em> its type parameters substituted
     * @return the dispatch information needed to translate a method call to
     *         {@code mi} when the receiver type is {@code recvTy}.
     */
    public DispatchInfo dispatchInfo(ReferenceType recvTy, MethodInstance mi) {
        assert recvTy != null;

        if (isArrayOrPlainClass(recvTy)) {
            int index = methodIndexInCDV(recvTy, mi);
            return DispatchInfo.createClassDispInfo(index);

        } else if (recvTy.isClass() && recvTy.toClass().flags().isInterface()) {
            int index = methodIndexInIDV(recvTy.toClass(), mi);
            ClassType erasure = utils.erasureLL(recvTy);
            return DispatchInfo.createIntfDispInfo(index, erasure);

        } else if (recvTy instanceof TypeVariable
                || recvTy instanceof WildCardType) {
            ReferenceType container = mi.container();
            assert isArrayOrPlainClass(container) || (container.isClass()
                    && container.toClass().flags().isInterface());
            return dispatchInfo(container, mi);

        }
        throw new InternalCompilerError("Cannot handle " + recvTy);
    }

    /**
     * @param rt  should be an array type or a
     *            "{@link LLVMTranslator#isArrayOrPlainClass plain}" class type
     * @param mi  a method found in type {@code rt} or its superclasses
     * @return the index of method {@code mi} in the class dispatch vector of
     *         type {@code rt}.
     */
    private int methodIndexInCDV(ReferenceType rt, MethodInstance mi) {
        assert isArrayOrPlainClass(rt);
        // TODO: Verify that this is correct.
        List<MethodInstance> cdvMethods = cdvMethods(rt).stream()
                .map(MethodInstance::orig)
                .collect(Collectors.toList());
        int idx = cdvMethods.indexOf(mi.orig());
        if (idx == -1)
            throw new InternalCompilerError("Method not found in CDV: " + mi.orig());
        return idx;
    }

    /**
     * @param recvTy
     *            should be an interface type
     * @param mi
     *            a method found in type {@code recvTy} or its superinterfaces
     * @return the index of method {@code mi} in the interface dispatch vector
     *         of type {@code recvTy}.
     */
    private int methodIndexInIDV(ClassType recvTy, MethodInstance mi) {
        assert recvTy.flags().isInterface();

        List<MethodInstance> idvMethods = idvMethods(recvTy).stream()
                .map(MethodInstance::orig)
                .collect(Collectors.toList());
        int idx = idvMethods.indexOf(mi.orig());
        if (idx == -1)
            throw new InternalCompilerError("Could not find method for IDV");
        return idx;
    }

    /**
     * Returns the index of the <em>last</em> method in {@code lst} that is
     * {@link JL5TypeSystem#areOverrideEquivalent override-equivalent} with
     * {@code mi}. It is a failure that there is not such a method in
     * {@code lst}.
     *
     * @param mi
     * @param lst
     */
    public int indexOfOverridingMethod(MethodInstance mi, List<MethodInstance> lst) {
        for (int j = lst.size() - 1; j >= 0; --j)
            if (ts.areOverrideEquivalent((JL5MethodInstance) mi, (JL5MethodInstance) lst.get(j)))
                return j;
        throw new InternalCompilerError(
                "Could not find a method that is override-equivalent with " + mi);
    }

    /**
     * @param refTy
     *            should be non-null
     * @return true if {@code refTy} is one of the following:
     *         <ul>
     *         <li>an {@link ArrayType}</li>
     *         <li>a non-interface {@link ParsedClassType}</li>
     *         <li>a non-interface {@link JL5SubstClassType}</li>
     *         <li>a non-interface {@link RawClass}</li>
     *         </ul>
     */
    public boolean isArrayOrPlainClass(ReferenceType refTy) {
        assert refTy != null;
        if (refTy.isArray())
            return true;
        if (refTy instanceof ParsedClassType
                || refTy instanceof JL5SubstClassType
                || refTy instanceof RawClass)
            return !refTy.toClass().flags().isInterface();
        return false;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Exception handling.
    ////////////////////////////////////////////////////////////////////////////

    private Deque<ExceptionFrame> exceptionFrames() {
        return fnCtxt().exceptionFrames;
    }

    /** Must be called when entering a try-catch-finally structure. */
    public void pushExceptionFrame(ExceptionFrame frame) {
        exceptionFrames().push(frame);
    }

    /** Must be called when leaving a try-catch-finally structure. */
    public void popExceptionFrame() {
        exceptionFrames().pop();
    }

    /** Returns the landing pad in the current exception frame. */
    public LLVMBasicBlockRef currLandingPad() {
        for (ExceptionFrame frame : exceptionFrames()) {
            if (frame.getLpadCatch() != null)
                return frame.getLpadCatch();
            if (frame.getLpadFinally() != null)
                return frame.getLpadFinally();
        }
        return null;
    }

    public boolean needsFinallyBlockChain() {
        return exceptionFrames().stream().anyMatch((ef) -> ef.getLpadFinally() != null);
    }

    /**
     * Runs all finally blocks between the current location of the
     * translator and [destExceptionFrameNestingLevel].
     */
    public void buildFinallyBlockChain(int destExceptionFrameNestingLevel) {

        List<ExceptionFrame> frames = exceptionFrames().stream()
                .limit(exceptionFrames().size() - destExceptionFrameNestingLevel)
                .filter(f -> f.getLpadFinally() != null)
                .collect(Collectors.toList());

        for (ExceptionFrame frame : frames) {
            LLVMBasicBlockRef next = utils.buildBlock("finally.chain");
            frame.buildFinallyBlockBranchingTo(next);
            LLVMPositionBuilderAtEnd(builder, next);
        }
    }
}
