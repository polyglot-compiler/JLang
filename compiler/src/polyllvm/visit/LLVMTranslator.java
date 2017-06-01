package polyllvm.visit;

import polyglot.ast.ClassDecl;
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
import polyllvm.util.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.bytedeco.javacpp.LLVM.*;

/**
 * Translates Java into LLVM IR.
 */
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
    public final PolyLLVMMangler mangler;
    public final JL5TypeUtils jl5Utils;

    private int ctorCounter;
    public int incCtorCounter() {
        return ctorCounter++;
    }

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
    private Map<String, LLVMValueRef> entryPoints = new HashMap<>();
    public void addEntryPoint(LLVMValueRef entryPoint, String className) { entryPoints.put(className, entryPoint); }
    public Map<String, LLVMValueRef> getEntryPoints() { return new HashMap<>(entryPoints); }

    /**
     * A list of ctor functions to be added to the module.
     * Each value in the list is a struct of the form {priority, ctorFunction, data}
     */
    private List<LLVMValueRef> ctors = new ArrayList<>();
    public void addCtor(LLVMValueRef ctor) { ctors.add(ctor); }
    public List<LLVMValueRef> getCtors() { return ListUtil.copy(ctors, false); }

    public LLVMTranslator(String filePath, LLVMContextRef context, LLVMModuleRef mod, LLVMBuilderRef builder,
                          PolyLLVMNodeFactory nf, TypeSystem ts) {
        super(nf.lang());
        this.context = context;
        this.mod = mod;
        this.builder = builder;
        this.debugInfo = new DebugInfo(this, mod, builder, filePath);
        this.utils = new LLVMUtils(this);
        this.classObjs = new ClassObjects(this);
        this.mangler = new PolyLLVMMangler(this);
        this.jl5Utils = new JL5TypeUtils(ts, nf);
        this.nf = nf;
        this.ts = ts;
    }

    @Override
    public PolyLLVMLang lang() {
        return (PolyLLVMLang) super.lang();
    }

    @Override
    public Node leave(Node old, Node n, NodeVisitor v) {
        return lang().leaveTranslateLLVM(n, this);
    }

    @Override
    public NodeVisitor enter(Node n) {
        return lang().enterTranslateLLVM(n, this);
    }

    @Override
    public Node override(Node n) {
        return lang().overrideTranslateLLVM(n, this);
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
        return highestSuperType.orElse(methodInstance.container());
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
        return rt instanceof MemberInstance && ((MemberInstance) rt).flags().isInterface();
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

        List<MethodInstance> dvLayout = new LinkedList<>();
        List<FieldInstance> objLayout = new LinkedList<>();
        List<MethodInstance> overridenMethods =  new LinkedList<>();

        if (isInterface(rt)) {
            dvLayout = interfaceLayout(rt);
        } else {
            ReferenceType superClass = rt;
            while (superClass != null) {
                Triple<List<MethodInstance>, List<MethodInstance>, List<FieldInstance>> classMembers = classMembers(superClass);
                dvLayout.addAll(0, classMembers.part1());
                objLayout.addAll(0, classMembers.part3());
                classMembers.part2().forEach(mi -> overridenMethods.add(0, mi));
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
        rt = jl5Utils.translateType(rt);

        List<? extends MemberInstance> classMembers = rt.members();
        List<MethodInstance> dvMethods = new ArrayList<>();
        List<MethodInstance> dvOverridenMethods = new ArrayList<>();
        List<FieldInstance> fields = new ArrayList<>();

        for (MemberInstance mi : classMembers) {
            if (mi.flags().isStatic()) {
                continue;
            }
            if (mi instanceof MethodInstance) {
                mi = jl5Utils.translateMemberInstance(mi);
                if (isOverridden((MethodInstance) mi)) {
                    dvOverridenMethods.add((MethodInstance) mi);
                }
                else {
                    dvMethods.add((MethodInstance) mi);
                }
            }
            else if (mi instanceof FieldInstance) {
                mi = jl5Utils.translateMemberInstance(mi);
                fields.add((FieldInstance) mi);
            }
        }
        Comparator<MethodInstance> methodComparator =
                (o1, o2) -> o1.name().compareTo(o2.name());
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
        LLVMValueRef allocation = allocations.get(var);
        if (allocation == null)
            throw new InternalCompilerError("Local variable " +var + " has no allocation");
        return allocation;
    }

    public void clearAllocations() {
        allocations.clear();
    }

    /*
     * Loops
     */

    // Note that these stacks may have different sizes because switch statements
    // only create a break-block.
    private Deque<LLVMBasicBlockRef> continueBlocks = new ArrayDeque<>();
    private Deque<LLVMBasicBlockRef> breakBlocks = new ArrayDeque<>();

    private static class Loop {
        LLVMBasicBlockRef head;
        LLVMBasicBlockRef end;
        Loop(LLVMBasicBlockRef head, LLVMBasicBlockRef end) {
            this.head = head;
            this.end = end;
        }
    }

    private Map<String, Loop> loopLabelMap = new HashMap<>();

    private String currLoopLabel; // Null if none.
    public void pushLoopLabel(String label) {
        currLoopLabel = label;
    }

    public void pushLoop(LLVMBasicBlockRef head, LLVMBasicBlockRef end) {
        continueBlocks.addLast(head);
        breakBlocks.addLast(end);
        if (currLoopLabel != null) {
            loopLabelMap.put(currLoopLabel, new Loop(head, end));
            currLoopLabel = null;
        }
    }

    public void pushSwitch(LLVMBasicBlockRef end) {
        breakBlocks.addLast(end);
    }

    public void popLoop() {
        continueBlocks.removeLast();
        breakBlocks.removeLast();
    }

    public void popSwitch() {
        breakBlocks.removeLast();
    }

    public LLVMBasicBlockRef getContinueBlock(String label) {
        return label == null ? continueBlocks.getLast() : loopLabelMap.get(label).head;
    }

    public LLVMBasicBlockRef getBreakBlock(String label) {
        return label == null ? breakBlocks.getLast() : loopLabelMap.get(label).end;
    }

    /*
     * Functions for accessing methods and fields
     */

    public int getMethodIndex(ReferenceType type, MethodInstance methodInstance) {
        return getMethodIndex(type, methodInstance, layouts(type).part1());
    }

    public int getMethodIndex(ReferenceType type, MethodInstance methodInstance, List<MethodInstance> methodLayout) {
        type = jl5Utils.translateType(type);
        MethodInstance old = methodInstance;
        methodInstance = (MethodInstance) jl5Utils.translateMemberInstance(methodInstance);
        methodLayout = methodLayout.stream()
                .map(mi -> (MethodInstance) jl5Utils.translateMemberInstance(mi))
                .collect(Collectors.toList());
        for (int i = 0; i < methodLayout.size(); i++) {
            if (methodLayout.get(i).isSameMethod(methodInstance)) {
                return i + Constants.DISPATCH_VECTOR_OFFSET;
            }
        }

        throw new InternalCompilerError("The method " + methodInstance
                + " is not in the class " + type);
    }


    public int getFieldIndex(ReferenceType type, FieldInstance fieldInstance) {
        type = jl5Utils.translateType(type);
        fieldInstance = (FieldInstance) jl5Utils.translateMemberInstance(fieldInstance);
        List<FieldInstance> objectLayout = layouts(type).part2();
        for (int i = 0; i < objectLayout.size(); i++) {
            if (objectLayout.get(i).equals(fieldInstance)) {
                return i + Constants.OBJECT_HEADER_SIZE;
            }
        }
        throw new InternalCompilerError("The field " + fieldInstance
                + " is not in the class " + type);

    }

    // Stack of landing pads, returns, etc.
    private static Deque<ExceptionRecord> exceptionRecords = new ArrayDeque<>();

    private class ExceptionRecord {
        private boolean inTry = true;
        private LLVMBasicBlockRef lpad = null;
        private LLVMBasicBlockRef tryFinally = null;
        private LLVMValueRef retFlag = null;
        private LLVMValueRef ret = null;
        private boolean retIsVoid = false;
        private boolean isRet = false;

        ExceptionRecord() {
            lpad = LLVMAppendBasicBlockInContext(context, currFn(), "lpad");
            tryFinally = LLVMAppendBasicBlockInContext(context, currFn(), "try_finally");
        }
    }

    /*
     * Methods for implementing exceptions
     */
    public void enterTry() {
        exceptionRecords.push(new ExceptionRecord());
    }

    public void exitTry() {
        ExceptionRecord currTry = exceptionRecords.peek();
        currTry.inTry = false;
        currTry.lpad = null;
    }

    public boolean inTry() {
        return exceptionRecords.peek() != null && exceptionRecords.peek().inTry;
    }

    public LLVMBasicBlockRef currLpad() {
        assert inTry();
        return exceptionRecords.peek().lpad;
    }

    public void setLpad(LLVMBasicBlockRef lpad) {
        assert inTry();
        exceptionRecords.peek().lpad = lpad;
    }

    public LLVMBasicBlockRef currFinally() {
        assert inTry();
        return exceptionRecords.peek().tryFinally;
    }

    public void setTryRet() {
        ExceptionRecord currTry = exceptionRecords.peek();
        currTry.isRet = true;
        currTry.retIsVoid = true;
        if (currTry.retFlag == null) {
            currTry.retFlag = PolyLLVMLocalDeclExt.createLocal(this, "ret_flag", LLVMInt1TypeInContext(context));
        }
        LLVMBuildStore(builder, LLVMConstInt(LLVMInt1TypeInContext(context), 1, /*sign-extend*/ 0), currTry.retFlag);
    }

    public void setTryRet(LLVMValueRef v) {
        ExceptionRecord currTry = exceptionRecords.peek();
        currTry.isRet = true;
        currTry.retIsVoid = false;
        if (currTry.ret == null) {
            currTry.ret = PolyLLVMLocalDeclExt.createLocal(this, "ret", LLVMTypeOf(v));
        }
        if (currTry.retFlag == null) {
            currTry.retFlag = PolyLLVMLocalDeclExt.createLocal(this, "ret_flag", LLVMInt1TypeInContext(context));
        }
        LLVMBuildStore(builder, LLVMConstInt(LLVMInt1TypeInContext(context), 1, /*sign-extend*/ 0), currTry.retFlag);
        LLVMBuildStore(builder, v, currTry.ret);
    }

    public void emitTryRet() {
        ExceptionRecord currTry = exceptionRecords.pop();
        if (currTry.isRet) {
            LLVMBasicBlockRef doRet = LLVMAppendBasicBlockInContext(context, currFn(), "do_ret");
            LLVMBasicBlockRef noRet = LLVMAppendBasicBlockInContext(context, currFn(), "no_ret");

            LLVMBuildCondBr(builder, LLVMBuildLoad(builder, currTry.retFlag, "ret_flag_load"), doRet, noRet);

            LLVMPositionBuilderAtEnd(builder, doRet);
            if (currTry.retIsVoid) {
                LLVMBuildRetVoid(builder);
            } else {
                LLVMBuildRet(builder, LLVMBuildLoad(builder, currTry.ret, "ret_load"));
            }

            LLVMPositionBuilderAtEnd(builder, noRet);
        }
    }
}
