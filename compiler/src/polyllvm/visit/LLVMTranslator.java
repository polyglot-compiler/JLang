package polyllvm.visit;

import org.bytedeco.javacpp.LLVM.*;
import polyglot.ast.Node;
import polyglot.ext.jl5.types.*;
import polyglot.ext.jl7.types.JL7TypeSystem;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;
import polyglot.util.ListUtil;
import polyglot.visit.NodeVisitor;
import polyllvm.ast.PolyLLVMLang;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.extension.ClassObjects;
import polyllvm.extension.PolyLLVMTryExt.ExceptionFrame;
import polyllvm.types.PolyLLVMTypeSystem;
import polyllvm.util.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Translates Java into LLVM IR.
 */
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

    private int ctorCounter;

    public int incCtorCounter() {
        return ctorCounter++;
    }

    /** A stack of all enclosing functions. */
    private Deque<LLVMValueRef> functions = new ArrayDeque<>();

    public void pushFn(LLVMValueRef fn) { functions.push(fn); }

    public void popFn() { functions.pop(); }

    public LLVMValueRef currFn() { return functions.peek(); }

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
        this.nf = nf;
        this.ts = ts;
        this.tnf = new TypedNodeFactory(ts, nf);
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

    public PolyLLVMNodeFactory nodeFactory() {
        return nf;
    }

    public PolyLLVMTypeSystem typeSystem() {
        return ts;
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
    private List<ReferenceType> classHierarchy(ReferenceType rt) {
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

    /**
     * Returns the superinterfaces of interface {@code it}, including {@code it}
     * itself. The order of interfaces in the list is not prescribed but
     * deterministic. Any two interfaces in the returned list are not equal.
     *
     * @param it
     *            should be an interface
     * @return The interfaces of interface {@code it}, including {@code it}
     *         itself.
     */
    private List<ClassType> intfHierarchy(ClassType it) {
        assert it.flags().isInterface();

        Set<ClassType> res = new LinkedHashSet<>();
        Deque<ClassType> toVisit = new LinkedList<>();
        toVisit.addLast(it);
        do {
            ClassType curIntf = toVisit.removeFirst();
            res.add(curIntf);
            curIntf.interfaces().stream()
                    .forEach(rt -> toVisit.addLast(rt.toClass()));
        } while (!toVisit.isEmpty());
        return new ArrayList<>(res);
    }

    /** Caches class-dispatch-vector methods. */
    private HashMap<ReferenceType, List<MethodInstance>> cdvMethodsCache = new HashMap<>();

    /** Caches object-layout fields. */
    private HashMap<ReferenceType, List<FieldInstance>> objFieldsCache = new HashMap<>();

    /** Caches interface-dispatch-vector methods. */
    private HashMap<ClassType, List<MethodInstance>> idvMethodsCache = new HashMap<>();

    /**
     * Returns the list of methods corresponding to those in the class dispatch
     * vector (CDV) of reference type {@code recvTy}. The returned methods are
     * not necessarily the exact methods used to create the CDV because the
     * methods used to create the CDV are methods of the erasure of
     * {@code recvTy}.
     *
     * <p>
     * In a CDV, methods declared by a superclass appear before methods added by
     * its subclasses. Override-equivalent methods appear only once in a CDV; an
     * overriding method is treated as member of the greatest superclass that
     * adds the method.
     *
     * @param recvTy
     *            Should be an array type or a
     *            "{@link LLVMTranslator#isArrayOrPlainClass plain}" class type.
     * @return The list of methods corresponding to those in the class dispatch
     *         vector of reference type {@code recvTy}.
     */
    public List<MethodInstance> cdvMethods(ReferenceType recvTy) {
        assert isArrayOrPlainClass(recvTy);

        List<MethodInstance> res = cdvMethodsCache.get(recvTy);
        if (res != null)
            return res;

        res = new ArrayList<>();
        List<ReferenceType> hierarchy = classHierarchy(recvTy);
        // Add the methods of the superclasses.
        for (int i = 0; i < hierarchy.size() - 1; ++i) {
            ClassType supc = hierarchy.get(i).toClass();
            for (MethodInstance m : nonOverridingMethods(supc)) {
                MethodInstance m2;
                if (recvTy.isClass() && ts.isAccessible(m, recvTy.toClass())) {
                    // Be precise about the signature, as a subclass can refine
                    // it.
                    m2 = typeSystem().findImplementingMethod(recvTy.toClass(),
                            m);
                    if (m2 == null) {
                        if (recvTy.toClass().flags().isAbstract())
                            m2 = m; // It's OK if we couldn't find one in an
                                    // abstract class.
                        else
                            throw new InternalCompilerError(
                                    "Could not find a method implementing \""
                                            + m + "\" in " + recvTy);
                    }
                } else {
                    m2 = m;
                }
                res.add(m2);
            }
        }
        // Add the methods of the current type.
        res.addAll(nonOverridingMethods(recvTy));

        cdvMethodsCache.put(recvTy, res);
        return res;
    }

    /**
     * Returns the list of fields corresponding to those in an object of type
     * {@code recvTy}. The returned field types are not necessarily the exact
     * field types in the object layout because the field types in the object
     * layout are the field types of the erasure of {@code recvTy}.
     *
     * @param recvTy
     *            Should be an array type or a
     *            "{@link LLVMTranslator#isArrayOrPlainClass plain}" class type.
     * @return The list of fields corresponding to those in the object layout of
     *         reference type {@code recvTy}.
     */
    public List<FieldInstance> objFields(ReferenceType recvTy) {
        assert isArrayOrPlainClass(recvTy);

        List<FieldInstance> res = objFieldsCache.get(recvTy);
        if (res != null)
            return res;

        res = new ArrayList<>();
        for (ReferenceType rt : classHierarchy(recvTy))
            res.addAll(fields(rt));

        objFieldsCache.put(recvTy, res);
        return res;
    }

    /**
     * Returns the list of methods corresponding to those in the interface
     * dispatch vector (IDV) of reference type {@code recvTy}. The returned
     * methods are not necessarily the exact methods used to create the IDV
     * because the methods used to create the IDV are the methods of the erasure
     * of {@code recvTy}.
     *
     * <p>
     * In an IDV, methods declared by a superinterface appear <em>after</em> the
     * methods added by its subinterfaces. Override-equivalent methods appear
     * only once in an IDV; a method is treated as member of the first interface
     * that declares an override-equivalent method in the list defined by
     * {@link LLVMTranslator#intfHierarchy}.
     *
     * @param recvTy
     *            should be an interface
     * @return
     */
    public List<MethodInstance> idvMethods(ClassType recvTy) {
        assert recvTy.flags().isInterface();

        List<MethodInstance> res = idvMethodsCache.get(recvTy);
        if (res != null)
            return res;

        res = new ArrayList<>();
        List<ClassType> hierarchy = intfHierarchy(recvTy);
        Iterator<ClassType> iter = hierarchy.iterator();
        // Add methods of the first interface in hierarchy
        res.addAll(iter.next().methods());
        // Add methods of the remaining interfaces in hierarchy
        while (iter.hasNext()) {
            JL7TypeSystem ts = typeSystem();
            ClassType it = iter.next();
            for (MethodInstance mj : it.methods()) {
                boolean seen = res.stream()
                        .anyMatch(mi -> ts.areOverrideEquivalent(
                                (JL5MethodInstance) mi,
                                (JL5MethodInstance) mj));
                if (!seen)
                    res.add(mj);
            }
        }

        idvMethodsCache.put(recvTy, res);
        return res;
    }

    private static Comparator<MethodInstance> methodOrdByName() {
        return new CompareMethodsByName();
    }

    private static class CompareMethodsByName
            implements Comparator<MethodInstance> {
        public int compare(MethodInstance m1, MethodInstance m2) {
            return m1.name().compareTo(m2.name());
        };
    }

    /**
     * @param rt
     * @return The list of methods that are declared in type {@code rt} but do
     *         not override any method declared in a superclass of {@code rt}.
     *         The returned list of methods are sorted by name.
     */
    private List<MethodInstance> nonOverridingMethods(ReferenceType rt) {
        return rt.methods().stream().filter(m -> isNonOverriding(m))
                .sorted(methodOrdByName()).collect(Collectors.toList());
    }

    /**
     * @param rt
     * @return The list of fields that are declared in type {@code rt}. The
     *         returned list of fields are sorted by name.
     */
    private List<FieldInstance> fields(ReferenceType rt) {
        return rt.fields().stream().sorted(new Comparator<FieldInstance>() {
            @Override
            public int compare(FieldInstance f1, FieldInstance f2) {
                return f1.name().compareTo(f2.name());
            }
        }).collect(Collectors.toList());
    }

    /**
     * @param clazz
     *            the non-interface class type
     * @return The interfaces of class {@code clazz}, including the interfaces
     *         that {@code clazz} transitively implements. The iteration order
     *         of the list is deterministic.
     */
    public List<ClassType> allInterfaces(ClassType clazz) {
        assert !clazz.flags().isInterface();
        List<ClassType> res = new ArrayList<>();
        addAllInterfacesOfClass(clazz, res);
        return res;
    }

    /**
     * Appends all of {@code clazz}'s interfaces (up to transitivity) that are
     * not already in {@code lst} to {@code lst}.
     *
     * @param clazz
     *            the non-interface class type
     * @param lst
     *            the existing list of interfaces
     */
    private void addAllInterfacesOfClass(ClassType clazz, List<ClassType> lst) {
        assert !clazz.flags().isInterface();
        for (ReferenceType intf : clazz.interfaces()) {
            addAllInterfacesOfIntf(intf.toClass(), lst);
        }
        if (clazz.superType() != null) {
            addAllInterfacesOfClass(clazz.superType().toClass(), lst);
        }
    }

    /**
     * Appends all of {@code intf}'s superinterfaces (up to transitivity and
     * reflexivity) that are not already in {@code lst} to {@code lst}.
     *
     * @param intf
     *            the interface type
     * @param lst
     *            the existing list of interfaces
     */
    private void addAllInterfacesOfIntf(ClassType intf, List<ClassType> lst) {
        assert intf.flags().isInterface();
        if (addIfNonExistent(lst, intf)) {
            for (ReferenceType superIntf : intf.interfaces()) {
                addAllInterfacesOfIntf(superIntf.toClass(), lst);
            }
        }
    }

    /**
     * Appends {@code t} to {@code types} if there does not exist a {@code t0}
     * in {@code types} such that {@code t.typeEquals(t0)} is true.
     *
     * @return true if {@code t} is appended to {@code types}, or false
     *         otherwise
     */
    private <T extends Type> boolean addIfNonExistent(List<T> types, T t) {
        if (types.stream().anyMatch(t::typeEquals)) {
            return false;
        } else {
            types.add(t);
            return true;
        }
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
        private int tryCatchNestingLevel = exceptionFrames.size();

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

    // Labels cannot be shadowed, so no need to worry about overwriting previous mappings.
    private Map<String, LabeledStmtLocs> labelMap = new HashMap<>();

    public void pushLabel(String label, LLVMBasicBlockRef before, LLVMBasicBlockRef after) {
        labelMap.put(label, new LabeledStmtLocs(before, after));
    }

    public void popLabel(String label) {
        labelMap.remove(label);
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
        return label == null ? continueLocs.getLast() : labelMap.get(label).head;
    }

    public ControlTransferLoc getBreakLoc(String label) {
        return label == null ? breakLocs.getLast() : labelMap.get(label).end;
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
     * @param recvTy
     *            should be an array type or a
     *            "{@link LLVMTranslator#isArrayOrPlainClass plain}" class type
     * @param mi
     *            a method found in type {@code recvTy} or its superclasses
     * @return the index of method {@code mi} in the class dispatch vector of
     *         type {@code recvTy}.
     */
    private int methodIndexInCDV(ReferenceType recvTy, MethodInstance mi) {
        assert isArrayOrPlainClass(recvTy);

        List<MethodInstance> cdvMethods = cdvMethods(recvTy);
        int idx = indexOfOverridingMethod(mi, cdvMethods);
        return Constants.CLASS_DISP_VEC_OFFSET + idx;
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

        List<MethodInstance> idvMethods = idvMethods(recvTy);
        int idx = indexOfOverridingMethod(mi, idvMethods);
        return Constants.INTF_DISP_VEC_OFFSET + idx;
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
    public int indexOfOverridingMethod(MethodInstance mi,
            List<MethodInstance> lst) {
        for (int j = lst.size() - 1; j >= 0; --j) {
            JL5MethodInstance mj = (JL5MethodInstance) lst.get(j);
            if (typeSystem().areOverrideEquivalent((JL5MethodInstance) mi, mj))
                return j;
        }
        throw new InternalCompilerError(
                "Could not find a method that is override-equivalent with "
                        + mi.signature());
    }

    /**
     * @param recvTy
     * @param fi
     *            a field found in {@code recvTy} or its superclasses
     * @return the index of field {@code fi} in the object layout when the
     *         receiver type is {@code recvTy}.
     */
    public int fieldInfo(ReferenceType recvTy, FieldInstance fi) {
        assert recvTy != null;

        if (isArrayOrPlainClass(recvTy)) {
            return fieldIndexInObj(recvTy, fi);
        }
        else if (recvTy instanceof TypeVariable
                || recvTy instanceof WildCardType) {
            ReferenceType container = fi.container();
            assert isArrayOrPlainClass(container);
            return fieldInfo(container, fi);

        }
        throw new InternalCompilerError("Cannot handle " + recvTy);
    }

    /**
     * @param recvTy
     *            should be an array type or a
     *            "{@link LLVMTranslator#isArrayOrPlainClass plain}" class type
     * @param fi
     *            a field found in {@code recvTy} or its superclasses
     * @return the index of field {@code fi} in the object layout of type
     *         {@code recvTy}.
     */
    private int fieldIndexInObj(ReferenceType recvTy, FieldInstance fi) {
        assert isArrayOrPlainClass(recvTy);

        List<FieldInstance> objFields = objFields(recvTy);
        int j = 0;
        for (; j < objFields.size(); ++j) {
            FieldInstance fj = objFields.get(j);
            if (fi.equals(fj)) // field found
                return j + Constants.OBJECT_FIELDS_OFFSET;
        }
        throw new InternalCompilerError("Could not find an entry for \"" + fi
                + "\" in the object layout of " + recvTy);
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
    private boolean isArrayOrPlainClass(ReferenceType refTy) {
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

    /** Stack of nested exception frames, innermost first. */
    private final Deque<ExceptionFrame> exceptionFrames = new ArrayDeque<>();

    /** Must be called when entering a try-catch-finally structure. */
    public void pushExceptionFrame(ExceptionFrame frame) {
        exceptionFrames.push(frame);
    }

    /** Must be called when leaving a try-catch finally structure. */
    public void popExceptionFrame() {
        exceptionFrames.pop();
    }

    /** Returns the landing pad in the current exception frame. */
    public LLVMBasicBlockRef currLandingPad() {
        for (ExceptionFrame frame : exceptionFrames) {
            if (frame.getLpadCatch() != null)
                return frame.getLpadCatch();
            if (frame.getLpadFinally() != null)
                return frame.getLpadFinally();
        }
        return null;
    }

    public boolean needsFinallyBlockChain() {
        return exceptionFrames.stream().anyMatch((ef) -> ef.getLpadFinally() != null);
    }

    /**
     * Runs all finally blocks between the current location of the translator and the
     * [destExceptionFrameNestingLevel], returning the first finally block to jump to.
     */
    public LLVMBasicBlockRef buildFinallyBlockChain(
            LLVMBasicBlockRef dest,
            int destExceptionFrameNestingLevel) {
         ExceptionFrame[] frames = exceptionFrames.stream()
                .limit(exceptionFrames.size() - destExceptionFrameNestingLevel)
                .toArray(ExceptionFrame[]::new);
         for (int i = frames.length - 1; i >= 0; --i)
             dest = frames[i].getFinallyBlockBranchingTo(dest);
         return dest;
    }
}
