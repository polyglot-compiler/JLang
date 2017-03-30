package polyllvm.visit;

import polyglot.ast.ClassDecl;
import polyglot.ast.Labeled;
import polyglot.ast.Loop;
import polyglot.ast.Node;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;
import polyglot.util.ListUtil;
import polyglot.util.Pair;
import polyglot.visit.NodeVisitor;
import polyllvm.ast.PolyLLVMLang;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.extension.ClassObjects;
import polyllvm.extension.PolyLLVMLocalDeclExt;
import polyllvm.util.Constants;
import polyllvm.util.DebugInfo;
import polyllvm.util.LLVMUtils;
import polyllvm.util.Triple;

import java.lang.Override;
import java.util.*;

import static org.bytedeco.javacpp.LLVM.*;

// TODO: Can simplify things by extending ContextVisitor.
public class LLVMTranslator extends NodeVisitor {

    private PolyLLVMNodeFactory nf;
    private TypeSystem ts;

    private Map<Node, Object> translations = new LinkedHashMap<>();
    private Deque<ClassDecl> classes = new ArrayDeque<>();

    public final LLVMContextRef context;
    public final LLVMModuleRef mod;
    public final LLVMBuilderRef builder;
    public final DebugInfo debugInfo;
    public final LLVMUtils utils;
    public final ClassObjects classObjs;

    private int ctorCounter;
    public int incCtorCounter() {
        return ctorCounter++;
    }

    private int stringLitCounter;
    public int incStringLitCounter() { return stringLitCounter++; }

    /**
     * A stack of all enclosing functions.
     */
    private Deque<LLVMValueRef> functions = new ArrayDeque<>();
    public void pushFn(LLVMValueRef fn) { functions.push(fn); }
    public void popFn()                 { functions.pop(); }
    public LLVMValueRef currFn()        { return functions.peek(); }

    /**
     * A list of all potential entry points (i.e., Java main functions).
     */
    private List<LLVMValueRef> entryPoints = new ArrayList<>();
    public void addEntryPoint(LLVMValueRef entryPoint) { entryPoints.add(entryPoint); }
    public List<LLVMValueRef> getEntryPoints() { return ListUtil.copy(entryPoints, false); }

    /**
     * A list of ctor functions to be added to the module.
     * Each value in the list is a struct of the form {priority, ctorFunction, data}
     */
    private List<LLVMValueRef> ctors = new ArrayList<>();
    public void addCtor(LLVMValueRef ctor) { ctors.add(ctor); }
    public List<LLVMValueRef> getCtors() { return ListUtil.copy(ctors, false); }

    /**
     * Flag to determine if in Try, and landingpad to jump to if in a Try
     */
    private boolean inTry = false;
    private LLVMBasicBlockRef lpad = null;
    private LLVMBasicBlockRef tryFinally = null;
    private LLVMValueRef retFlag = null;
    private LLVMValueRef ret = null;
    private boolean retIsVoid = false;
    private boolean isRet = false;

    public LLVMTranslator(String filePath, LLVMContextRef context, LLVMModuleRef mod, LLVMBuilderRef builder,
                          PolyLLVMNodeFactory nf, TypeSystem ts) {
        super(nf.lang());
        this.context = context;
        this.mod = mod;
        this.builder = builder;
        this.debugInfo = new DebugInfo(this, mod, builder, filePath);
        this.utils = new LLVMUtils(this);
        this.classObjs = new ClassObjects(this);
        this.nf = nf;
        this.ts = ts;
    }

    @Override
    public PolyLLVMLang lang() {
        return (PolyLLVMLang) super.lang();
    }

    @Override
    public Node leave(Node old, Node n, NodeVisitor v) {
        return lang().translatePseudoLLVM(n, this);
    }

    @Override
    public NodeVisitor enter(Node n) {
        return lang().enterTranslatePseudoLLVM(n, this);
    }

    @Override
    public Node override(Node n) {
        return lang().overrideTranslatePseudoLLVM(n, this);
    }

    /**
     * Get the node factory used by the compiler to create new nodes
     */
    public PolyLLVMNodeFactory nodeFactory() {
        return nf;
    }

    /**
     * Get the Type System used by the compiler
     */
    public TypeSystem typeSystem() {
        return ts;
    }

    /**
     * Add the translation from n -> lln
     */
    public void addTranslation(Node n, Object lln) {
        translations.put(n, lln);
    }

    /**
     * Return the translation for {@code n}.
     */
    @SuppressWarnings("unchecked")
    public <T> T getTranslation(Node n) {
        T res = (T) translations.get(n);
        if (res == null)
            throw new InternalCompilerError("Null translation of " + n.getClass() + ": " + n);
        return res;
    }

    /**
     * Remove the current class from the stack of classes being visited
     */
    public void leaveClass() {
        classes.pop();
    }

    /**
     * Set {@code n} as the new current class
     */
    public void enterClass(ClassDecl n) {
        classes.push(n);
    }

    /**
     * Return the current class
     */
    public ClassDecl getCurrentClass() {
        return classes.peek();
    }

    public ReferenceType declaringType(MethodInstance methodInstance) {
        List<MethodInstance> overrides = methodInstance.overrides();
        Optional<ReferenceType> highestSuperType = overrides.stream()
                .map(MemberInstance::container)
                .max((o1, o2) -> o1.descendsFrom(o2) ? -1 : 1);
        return highestSuperType.get();
    }

    public boolean isOverridden(MethodInstance methodInstance) {
        return !methodInstance.container().typeEquals(declaringType(methodInstance));
    }

    /**
     * Return true if {@code m} was declared in an interface Interface, false otherwise.
     */
    public boolean isInterfaceCall(MethodInstance m) {
        ReferenceType declaringType = declaringType(m);
        return isInterface(declaringType);
    }

    /**
     * Return true if {@code rt} is a Interface, false otherwise.
     */
    public boolean isInterface(ReferenceType rt) {
        return rt instanceof ParsedClassType && ((ParsedClassType) rt).flags().isInterface();
    }

    /**
    * Stores the layouts for classes.
    */
    private HashMap<String, Pair<List<MethodInstance>, List<FieldInstance>>> layouts =
            new HashMap<>();

    /**
     * Get the field and method layout for a given class
     */
    public Pair<List<MethodInstance>, List<FieldInstance>> layouts(ReferenceType rt) {
        if (layouts.containsKey(rt.toString())) {
            return layouts.get(rt.toString());
        }

        List<MethodInstance> dvLayout = new ArrayList<>();
        List<FieldInstance> objLayout = new ArrayList<>();
        HashSet<MethodInstance> overridenMethods =  new HashSet<>();

        if (isInterface(rt)) {
            dvLayout = interfaceLayout(rt);
        } else {
            ReferenceType superClass = rt;
            while (superClass != null) {
                Triple<List<MethodInstance>, List<MethodInstance>, List<FieldInstance>> classMembers = classMembers(superClass);
                dvLayout.addAll(0, classMembers.part1());
                objLayout.addAll(0, classMembers.part3());
                classMembers.part2().stream().forEach(overridenMethods::add);

                superClass = (ReferenceType) superClass.superType();
            }
        }

        List<MethodInstance> dvLayoutCopy = dvLayout;

        overridenMethods.forEach(mi ->
                dvLayoutCopy.set(getMethodIndex(rt, mi, dvLayoutCopy) - Constants.DISPATCH_VECTOR_OFFSET, mi));

        Pair<List<MethodInstance>, List<FieldInstance>> result =
                new Pair<>(dvLayout, objLayout);
        layouts.put(rt.toString(), result);
        return result;
    }

    public List<MethodInstance> interfaceLayout(ReferenceType rt) {
        List<MethodInstance> dvLayout = new ArrayList<>(nonOverriddenClassMembers(rt).part1());
        List<? extends ReferenceType> superTypes = rt.interfaces();
        for (ReferenceType superType: superTypes) {
             dvLayout.addAll(0, interfaceLayout(superType));
        }
        return dvLayout;
    }


    private Pair<List<MethodInstance>, List<FieldInstance>> nonOverriddenClassMembers(
            ReferenceType rt) {
        Triple<List<MethodInstance>, List<MethodInstance>, List<FieldInstance>> triple =
                classMembers(rt);
        return new Pair<>(triple.part1(), triple.part3());

    }

    private Triple<List<MethodInstance>, List<MethodInstance>, List<FieldInstance>> classMembers(
            ReferenceType rt) {
        List<? extends MemberInstance> classMembers = rt.members();
        List<MethodInstance> dvMethods = new ArrayList<>();
        List<MethodInstance> dvOverridenMethods = new ArrayList<>();
        List<FieldInstance> fields = new ArrayList<>();

        for (MemberInstance mi : classMembers) {
            if (mi.flags().isStatic()) {
                continue;
            }
            if (mi instanceof MethodInstance) {
                if (isOverridden((MethodInstance) mi)) {
                    dvOverridenMethods.add((MethodInstance) mi);
                }
                else {
                    dvMethods.add((MethodInstance) mi);
                }
            }
            else if (mi instanceof FieldInstance) {
                fields.add((FieldInstance) mi);
            }
        }
        Comparator<MethodInstance> methodComparator =
                new Comparator<MethodInstance>() {
                    @Override
                    public int compare(MethodInstance o1, MethodInstance o2) {
                        if (visibilityInt(o1) != visibilityInt(o2)) {
                            return visibilityInt(o2) - visibilityInt(o1);
                        }
                        else {
                            return o1.name().compareTo(o2.name());
                        }
                    }

                    private int visibilityInt(MethodInstance md) {
                        int mdVisibility;
                        if (md.flags().isPublic()) {
                            mdVisibility = 3;
                        }
                        else if (md.flags().isPackage()) {
                            mdVisibility = 2;
                        }
                        else if (md.flags().isProtected()) {
                            mdVisibility = 1;
                        }
                        else if (md.flags().isPrivate()) {
                            mdVisibility = 0;
                        }
                        else {
                            throw new InternalCompilerError("Method " + md
                                    + " does not have a know visibility modifier");
                        }
                        return mdVisibility;

                    }
                };
        Comparator<FieldInstance> fieldComparator =
                (o1, o2) -> o1.name().compareTo(o2.name());

        dvMethods.sort(methodComparator);
        dvOverridenMethods.sort(methodComparator);
        fields.sort(fieldComparator);
        return new Triple<>(dvMethods, dvOverridenMethods, fields);
    }

    public List<ReferenceType> allInterfaces(ReferenceType rt) {
        List<ReferenceType> interfaces = new ArrayList<>();
        for (ReferenceType superType : rt.interfaces()) {
            interfaces.addAll(allInterfaces(superType));
            interfaces.add(superType);
        }
        return interfaces;
    }



    public MethodInstance methodInList(MethodInstance mi,
            List<MethodInstance> methods) {
        for (MethodInstance m : methods) {
            if (m.isSameMethod(mi)) {
                return m;
            }
        }
        return null;
    }

    /*
     * Functions for generating loads and stores from stack allocated variables (including arguments)
     */
    private Map<String, LLVMValueRef> allocations = new HashMap<>();

    public void addAllocation(String var, LLVMValueRef v) {
        allocations.put(var, v);
    }

    public LLVMValueRef getLocalVariable(String var) {
        return allocations.get(var);
    }

    public void clearAllocations() {
        allocations.clear();
    }


    /*
     * Functions for translating loops
     */

    /**
     * loops is a list of (String * (LLVMBasicBlockRef * LLVMBasicBlockRef)),
     * where the String is a label (or null), and the pair of basic blocks is the
     * head block and end block for that loop.
     */
    private LinkedList<Pair<String, Pair<LLVMBasicBlockRef, LLVMBasicBlockRef>>> loops =
            new LinkedList<>();

    public void enterLoop(Loop n) {
        //If the loop is labeled, use the stored info to push to loops
        if (label != null && labelhead != null && labelend != null) {
            loops.push(new Pair<>(label.label(),
                                  new Pair<>(labelhead, labelend)));
            label = null;
            labelhead = null;
            labelend = null;
        }
        //Else the loop is unlabled to generate a fresh label
        else {
            LLVMBasicBlockRef head = LLVMAppendBasicBlockInContext(context, currFn(), "loop_head");
            LLVMBasicBlockRef end = LLVMAppendBasicBlockInContext(context, currFn(), "loop_end");

            Pair<String, Pair<LLVMBasicBlockRef, LLVMBasicBlockRef>> pair =
                    new Pair<>("", new Pair<>(head, end));
            loops.push(pair);
        }

    }

    public Pair<LLVMBasicBlockRef, LLVMBasicBlockRef> leaveLoop() {
        return loops.pop().part2();
    }
    public Pair<LLVMBasicBlockRef, LLVMBasicBlockRef> peekLoop() {
        return loops.peek().part2();
    }


    private Labeled label;
    private LLVMBasicBlockRef labelhead;
    private LLVMBasicBlockRef labelend;

    public void enterLabeled(Labeled n) {
        label = n;
        labelhead = LLVMAppendBasicBlockInContext(context, currFn(), n.label() + "_head");
        labelend = LLVMAppendBasicBlockInContext(context, currFn(), n.label() + "_end");
    }

    public LLVMBasicBlockRef getLoopEnd(String label) {
        for (Pair<String, Pair<LLVMBasicBlockRef, LLVMBasicBlockRef>> pair : loops) {
            if (pair.part1().equals(label)) {
                return pair.part2().part2();
            }
        }
        throw new InternalCompilerError("Loop labeled " + label
                + " is not on loop stack");
    }

    public LLVMBasicBlockRef getLoopHead(String label) {
        for (Pair<String, Pair<LLVMBasicBlockRef, LLVMBasicBlockRef>> pair : loops) {
            if (pair.part1().equals(label)) {
                return pair.part2().part1();
            }
        }

        throw new InternalCompilerError("Loop labeled " + label
                + " is not on loop stack");
    }

    public LLVMBasicBlockRef getLoopHead() {
        Pair<String, Pair<LLVMBasicBlockRef, LLVMBasicBlockRef>> pair = loops.get(0);
        return pair.part2().part1();
    }

    public LLVMBasicBlockRef getLoopEnd() {
        Pair<String, Pair<LLVMBasicBlockRef, LLVMBasicBlockRef>> pair = loops.get(0);
        return pair.part2().part2();
    }

    public int getMethodIndex(ReferenceType type, MethodInstance methodInstance) {
        return getMethodIndex(type, methodInstance, layouts(type).part1());
    }

    /*
     * Functions for accessing methods and fields
     */

    public int getMethodIndex(ReferenceType type, MethodInstance methodInstance, List<MethodInstance> methodLayout) {
        for (int i = 0; i < methodLayout.size(); i++) {
            if (methodLayout.get(i).isSameMethod(methodInstance)) {
                return i + Constants.DISPATCH_VECTOR_OFFSET;
            }
        }
        throw new InternalCompilerError("The method " + methodInstance
                + " is not in the class " + type);
    }


    public int getFieldIndex(ReferenceType type, FieldInstance fieldInstance) {
        List<FieldInstance> objectLayout = layouts(type).part2();
        for (int i = 0; i < objectLayout.size(); i++) {
            if (objectLayout.get(i).equals(fieldInstance)) {
                return i + 1;
            }
        }
        throw new InternalCompilerError("The field " + fieldInstance
                + " is not in the class " + type);

    }

    /*
     * Methods for implementing exceptions
     */
    public void enterTry() {
        inTry = true;
        lpad = LLVMAppendBasicBlockInContext(context, currFn(), "lpad");
        tryFinally = LLVMAppendBasicBlockInContext(context, currFn(), "try_finally");
    }

    public void exitTry() {
        inTry = false;
        lpad = null;
    }

    public boolean inTry() {
        return inTry;
    }

    public LLVMBasicBlockRef currLpad() {
        assert inTry;
        return lpad;
    }

    public void setLpad(LLVMBasicBlockRef lpad) {
        assert inTry;
        this.lpad = lpad;
    }

    public LLVMBasicBlockRef currFinally() {
        assert inTry;
        return tryFinally;
    }

    public void setTryFinally(LLVMBasicBlockRef tryFinally) {
        assert inTry;
        this.tryFinally = tryFinally;
    }


    public void setTryRet() {
        isRet = true;
        retIsVoid = true;
        if (retFlag == null) {
            retFlag = PolyLLVMLocalDeclExt.createLocal(this, "ret_flag", LLVMInt1TypeInContext(context));
        }
        LLVMBuildStore(builder, LLVMConstInt(LLVMInt1TypeInContext(context), 1, /*sign-extend*/ 0), retFlag);
    }

    public void setTryRet(LLVMValueRef v) {
        isRet = true;
        retIsVoid = false;
        if (ret == null) {
            ret = PolyLLVMLocalDeclExt.createLocal(this, "ret", LLVMTypeOf(v));
        }
        if (retFlag == null) {
            retFlag = PolyLLVMLocalDeclExt.createLocal(this, "ret_flag", LLVMInt1TypeInContext(context));
        }
        LLVMBuildStore(builder, LLVMConstInt(LLVMInt1TypeInContext(context), 1, /*sign-extend*/ 0), retFlag);
        LLVMBuildStore(builder, v, ret);
    }

    public void emitTryRet() {
        if (isRet) {
            LLVMBasicBlockRef doRet = LLVMAppendBasicBlockInContext(context, currFn(), "do_ret");
            LLVMBasicBlockRef noRet = LLVMAppendBasicBlockInContext(context, currFn(), "no_ret");

            LLVMBuildCondBr(builder, LLVMBuildLoad(builder, retFlag, "ret_flag_load"), doRet, noRet);

            LLVMPositionBuilderAtEnd(builder, doRet);
            if (retIsVoid) {
                LLVMBuildRetVoid(builder);
            } else {
                LLVMBuildRet(builder, LLVMBuildLoad(builder, ret, "ret_load"));
            }

            LLVMPositionBuilderAtEnd(builder, noRet);
        }


        tryFinally = null;
        retFlag = null;
        ret = null;
        retIsVoid = false;
        isRet = false;
        inTry = false;
        lpad = null;

    }
}
