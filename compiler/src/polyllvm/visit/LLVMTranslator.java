package polyllvm.visit;

import org.bytedeco.javacpp.LLVM.*;

import static org.bytedeco.javacpp.LLVM.*;

import java.util.*;
import java.util.stream.Collectors;

import polyglot.ast.ClassDecl;
import polyglot.ast.Node;
import polyglot.ext.jl5.types.*;
import polyglot.ext.jl7.types.JL7TypeSystem;
import polyglot.types.ArrayType;
import polyglot.types.ClassType;
import polyglot.types.FieldInstance;
import polyglot.types.MemberInstance;
import polyglot.types.MethodInstance;
import polyglot.types.ParsedClassType;
import polyglot.types.ReferenceType;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.util.ListUtil;
import polyglot.visit.NodeVisitor;
import polyllvm.ast.PolyLLVMLang;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.extension.ClassObjects;
import polyllvm.extension.PolyLLVMLocalDeclExt;
import polyllvm.util.Constants;
import polyllvm.util.DebugInfo;
import polyllvm.util.LLVMUtils;
import polyllvm.util.PolyLLVMMangler;

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

	private int ctorCounter;

	public int incCtorCounter() {
		return ctorCounter++;
	}

	/**
	 * A stack of all enclosing functions.
	 */
	private Deque<LLVMValueRef> functions = new ArrayDeque<>();

	public void pushFn(LLVMValueRef fn) {
		functions.push(fn);
	}

	public void popFn() {
		functions.pop();
	}

	public LLVMValueRef currFn() {
		return functions.peek();
	}

	/**
	 * A list of all potential entry points (i.e., Java main functions).
	 */
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

	public LLVMTranslator(String filePath, LLVMContextRef context,
			LLVMModuleRef mod, LLVMBuilderRef builder, PolyLLVMNodeFactory nf,
			JL7TypeSystem ts) {
		super(nf.lang());
		this.context = context;
		this.mod = mod;
		this.builder = builder;
		this.debugInfo = new DebugInfo(this, mod, builder, filePath);
		this.utils = new LLVMUtils(this);
		this.classObjs = new ClassObjects(this);
		this.mangler = new PolyLLVMMangler(this);
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
	public JL7TypeSystem typeSystem() {
		return (JL7TypeSystem) ts;
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
			throw new InternalCompilerError(
					"Null translation of " + n.getClass() + ": " + n);
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

	/**
	 * @param mi
	 * @return
	 *         <ul>
	 *         <li>the greatest uninstantiated superclass type that declares
	 *         method {@code mi} if such a type exists;</li>
	 *         <li>the {@link container MemberInstance#container()} of
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
					if (m2 == null)
						throw new InternalCompilerError(
								"Could not find a method implementing \"" + m
										+ "\" in " + recvTy);
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
	 * @param types
	 * @param t
	 * @return true if {@code t} is appended to {@code types}, or false
	 *         otherwise
	 */
	private <T extends Type> boolean addIfNonExistent(List<T> types, T t) {
		if (types.stream().anyMatch(t0 -> t.typeEquals(t0))) {
			return false;
		} else {
			types.add(t);
			return true;
		}
	}

	/*
	 * Functions for generating loads and stores from stack allocated variables
	 * (including arguments)
	 */
	private Map<String, LLVMValueRef> allocations = new HashMap<>();

	public void addAllocation(String var, LLVMValueRef v) {
		allocations.put(var, v);
	}

	public LLVMValueRef getLocalVariable(String var) {
		LLVMValueRef allocation = allocations.get(var);
		if (allocation == null)
			throw new InternalCompilerError(
					"Local variable " + var + " has no allocation");
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
		return label == null ? continueBlocks.getLast()
				: loopLabelMap.get(label).head;
	}

	public LLVMBasicBlockRef getBreakBlock(String label) {
		return label == null ? breakBlocks.getLast()
				: loopLabelMap.get(label).end;
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
			int index = fieldIndexInObj(recvTy, fi);
			return index;

		} else if (recvTy instanceof TypeVariable
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
			tryFinally = LLVMAppendBasicBlockInContext(context, currFn(),
					"try_finally");
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
			currTry.retFlag = PolyLLVMLocalDeclExt.createLocal(this, "ret_flag",
					LLVMInt1TypeInContext(context));
		}
		LLVMBuildStore(builder, LLVMConstInt(LLVMInt1TypeInContext(context), 1,
				/* sign-extend */ 0), currTry.retFlag);
	}

	public void setTryRet(LLVMValueRef v) {
		ExceptionRecord currTry = exceptionRecords.peek();
		currTry.isRet = true;
		currTry.retIsVoid = false;
		if (currTry.ret == null) {
			currTry.ret = PolyLLVMLocalDeclExt.createLocal(this, "ret",
					LLVMTypeOf(v));
		}
		if (currTry.retFlag == null) {
			currTry.retFlag = PolyLLVMLocalDeclExt.createLocal(this, "ret_flag",
					LLVMInt1TypeInContext(context));
		}
		LLVMBuildStore(builder, LLVMConstInt(LLVMInt1TypeInContext(context), 1,
				/* sign-extend */ 0), currTry.retFlag);
		LLVMBuildStore(builder, v, currTry.ret);
	}

	public void emitTryRet() {
		ExceptionRecord currTry = exceptionRecords.pop();
		if (currTry.isRet) {
			LLVMBasicBlockRef doRet = LLVMAppendBasicBlockInContext(context,
					currFn(), "do_ret");
			LLVMBasicBlockRef noRet = LLVMAppendBasicBlockInContext(context,
					currFn(), "no_ret");

			LLVMBuildCondBr(builder,
					LLVMBuildLoad(builder, currTry.retFlag, "ret_flag_load"),
					doRet, noRet);

			LLVMPositionBuilderAtEnd(builder, doRet);
			if (currTry.retIsVoid) {
				LLVMBuildRetVoid(builder);
			} else {
				LLVMBuildRet(builder,
						LLVMBuildLoad(builder, currTry.ret, "ret_load"));
			}

			LLVMPositionBuilderAtEnd(builder, noRet);
		}
	}
}
