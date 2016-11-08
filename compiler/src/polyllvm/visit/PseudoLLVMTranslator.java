package polyllvm.visit;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;
import polyglot.util.ListUtil;
import polyglot.util.Pair;
import polyglot.visit.NodeVisitor;
import polyllvm.ast.PolyLLVMLang;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.*;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable.VarKind;
import polyllvm.ast.PseudoLLVM.*;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMArrayType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMPointerType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.Statements.*;
import polyllvm.util.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PseudoLLVMTranslator extends NodeVisitor {

    private PolyLLVMNodeFactory nf;
    private TypeSystem ts;

    private Map<Node, LLVMNode> translations;
    private Deque<ClassDecl> classes;

    private List<ClassDecl> classesVisited;

    private HashMap<String, LLVMTypeNode> classTypes;
    private List<LLVMGlobalVarDeclaration> globalSizes;
    private List<LLVMFunction> ctorsFunctions;

    public PseudoLLVMTranslator(PolyLLVMNodeFactory nf, TypeSystem ts) {
        super(nf.lang());
        this.nf = nf;
        this.ts = ts;
        translations = new LinkedHashMap<>();
        classes = new ArrayDeque<>();
        classesVisited = new ArrayList<>();
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
     * @return
     */
    public PolyLLVMNodeFactory nodeFactory() {
        return nf;
    }

    /**
     * Get the Type System used by the compiler
     * @return
     */
    public TypeSystem typeSystem() {
        return ts;
    }

    /**
     * Add the translation from n -> lln
     * @param n
     * @param lln
     */
    public void addTranslation(Node n, LLVMNode lln) {
        translations.put(n, lln);
    }

    /**
     * Return the translation for {@code n}, if none exists return null
     * @param n
     */
    public LLVMNode getTranslation(Node n) {
        return translations.get(n);
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
        classesVisited.add(n);
    }

    /**
     * Return the current class
     * @return
     */
    public ClassDecl getCurrentClass() {
        return classes.peek();
    }

    private boolean isOverridden(MethodDecl m) {
        return isOverridden(m.methodInstance());
    }

    public ReferenceType declaringType(MethodInstance methodInstance){
        List<MethodInstance> overrides = methodInstance.overrides();
        List<ReferenceType> refTypes = overrides.stream().map(MemberInstance::container).collect(Collectors.toList());
        ReferenceType highestSuperType =Collections.max(refTypes, (o1, o2) -> o1.descendsFrom(o2) ? -1 : 1);
        return highestSuperType;
    }

    private boolean isOverridden(MethodInstance methodInstance) {
        return !methodInstance.container().typeEquals(declaringType(methodInstance));
    }

    public boolean isInterfaceCall(MethodInstance m){
        ReferenceType declaringType = declaringType(m);
        return isInterface(declaringType);
    }

    public boolean isInterface(ReferenceType rt){
        if(rt instanceof ParsedClassType){
            return ((ParsedClassType) rt).flags().isInterface();
        }
        return false;
    }

    private void setupClassData() {
        if (classTypes != null && globalSizes != null
                && ctorsFunctions != null) {
            return;
        }
        classTypes = new HashMap<>();
        globalSizes = new ArrayList<>();
        ctorsFunctions = new ArrayList<>();

        setupClassTypes();
        setupClassSizes();
        setupCtorsFunctions();

        setupItables();

    }

    /**
    * Stores the layouts for classes.
    */
    private HashMap<String, Pair<List<MethodInstance>, List<FieldInstance>>> layouts =
            new HashMap<>();

    /**
     * Get the field and method layout for a given class
     */
    public Pair<List<MethodInstance>, List<FieldInstance>> layouts(
            ReferenceType rt) {
        //TODO: for interfaces add super interfaces in BFS (OR Something better)

        if (layouts.containsKey(rt.toString())) {
            return layouts.get(rt.toString());
        }

        List<MethodInstance> dvLayout = new ArrayList<>();
        List<FieldInstance> objLayout = new ArrayList<>();

        ReferenceType superClass = rt;
        while (superClass != null) {
//            if (superClass.toString().equals("java.lang.Object")) {
//                break;
//            }
            Pair<List<MethodInstance>, List<FieldInstance>> pair =
                    nonOverriddenClassMembers(superClass);
            dvLayout.addAll(0, pair.part1());
            objLayout.addAll(0, pair.part2());

            superClass = (ReferenceType) superClass.superType();
        }

        Pair<List<MethodInstance>, List<FieldInstance>> result =
                new Pair<>(dvLayout, objLayout);
        layouts.put(rt.toString(), result);
//        System.out.println("Layout of " + rt + ":");
//        for (MethodInstance mi : dvLayout) {
//            System.out.println("    • " + mi);
//        }
        return result;
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
                new Comparator<FieldInstance>() {
                    @Override
                    public int compare(FieldInstance o1, FieldInstance o2) {
                        return o1.name().compareTo(o2.name());
                    }
                };

        dvMethods.sort(methodComparator);
        dvOverridenMethods.sort(methodComparator);
        fields.sort(fieldComparator);
        return new Triple<>(dvMethods, dvOverridenMethods, fields);
    }

    private void setupItables(){
        LLVMPointerType bytePointerType = nf.LLVMPointerType(nf.LLVMIntType(8));
        HashSet<ReferenceType> interfacesAdded = new HashSet<>();
        for (ClassDecl cd : classesVisited){
            if(cd.flags().isInterface()) return;
            ParsedClassType rt = cd.type();
            List<LLVMInstruction> instrs = new ArrayList<>();
            List<MethodInstance> classLayout = layouts(rt).part1();

            /*
             * Call the init function of the class first
             */
            LLVMTypeNode tn = nf.LLVMFunctionType(new ArrayList<LLVMTypeNode>(), nf.LLVMVoidType());
            LLVMVariable function = nf.LLVMVariable(PolyLLVMMangler.classInitFunction(rt), tn, VarKind.GLOBAL);
            List<Pair<LLVMTypeNode, LLVMOperand>> args = CollectionUtil.list();
            LLVMCall callClassInit = nf.LLVMCall(function, args, nf.LLVMVoidType());
            instrs.add(callClassInit);
            calls.add(callClassInit);


            for (int j = 0; j< rt.interfaces().size(); j++){
                ReferenceType it = rt.interfaces().get(j);
                String interfaceTableVar = PolyLLVMMangler.InterfaceTableVariable(rt, it);
                LLVMTypeNode itType =
                        PolyLLVMTypeUtils.polyLLVMDispatchVectorVariableType(this, it);
                LLVMVariable llvmItVar =
                        nf.LLVMVariable(interfaceTableVar,
                                nf.LLVMPointerType(itType),
                                LLVMVariable.VarKind.GLOBAL);

                String dvVar = PolyLLVMMangler.dispatchVectorVariable(rt);
                LLVMTypeNode dvType = PolyLLVMTypeUtils.polyLLVMDispatchVectorVariableType(this, rt);
                LLVMVariable llvmDvVar = nf.LLVMVariable(dvVar, nf.LLVMPointerType(dvType), VarKind.GLOBAL);

                // Add list to Class DV
                if(j==0) {
                    LLVMVariable iListPtr = PolyLLVMFreshGen.freshLocalVar(nf, nf.LLVMPointerType(bytePointerType));
                    LLVMGetElementPtr gepIList = PolyLLVMFreshGen.freshGetElementPtr(nf, iListPtr, llvmDvVar, 0, 0);

                    LLVMVariable headITableCast = PolyLLVMFreshGen.freshLocalVar(nf, bytePointerType);
                    LLVMConversion bitcast =
                            nf.LLVMConversion(LLVMConversion.BITCAST, headITableCast,
                                    nf.LLVMPointerType(itType), llvmItVar, bytePointerType);

                    LLVMStore store = nf.LLVMStore(bytePointerType, headITableCast, iListPtr);

                    instrs.add(gepIList);
                    instrs.add(bitcast);
                    instrs.add(store);
                }


                //Setup as Linked List
                LLVMVariable iTableNextPtr = PolyLLVMFreshGen.freshLocalVar(nf, nf.LLVMPointerType(bytePointerType));
                LLVMGetElementPtr gep = PolyLLVMFreshGen.freshGetElementPtr(nf, iTableNextPtr, llvmItVar, 0, 0);
                if(j != rt.interfaces().size()-1){
                    ReferenceType nextIt = rt.interfaces().get(j + 1);
                    String nextITableVar = PolyLLVMMangler.InterfaceTableVariable(rt, nextIt);
                    LLVMTypeNode nextItType = PolyLLVMTypeUtils.polyLLVMDispatchVectorVariableType(this, nextIt);
                    LLVMVariable llvmNextItVar = nf.LLVMVariable(nextITableVar,
                                    nf.LLVMPointerType(nextItType),
                                    LLVMVariable.VarKind.GLOBAL);

                    LLVMVariable nextItCast = PolyLLVMFreshGen.freshLocalVar(nf, bytePointerType);
                    LLVMConversion bitcast =
                            nf.LLVMConversion(LLVMConversion.BITCAST, nextItCast,
                                    nf.LLVMPointerType(nextItType), llvmNextItVar, bytePointerType);


                    LLVMStore store = nf.LLVMStore(bytePointerType, nextItCast, iTableNextPtr);

                    instrs.add(bitcast);
                    instrs.add(gep);
                    instrs.add(store);
                } else {
                    LLVMStore store = nf.LLVMStore(bytePointerType, nf.LLVMNullLiteral(bytePointerType), iTableNextPtr);
                    instrs.add(gep);
                    instrs.add(store);
                }

                //Load type into second slot
                String s = it.toString();
                LLVMArrayType stringType = nf.LLVMArrayType(nf.LLVMIntType(8), s.length() + 1);
                if(!interfacesAdded.contains(it)) {
                    LLVMCStringLiteral init = nf.LLVMCStringLiteral(stringType, s);
                    LLVMGlobalVarDeclaration typeNameString = nf.LLVMGlobalVarDeclaration(
                            PolyLLVMMangler.interfaceStringVariable(it),
                            /* isExtern */ false,
                            LLVMGlobalVarDeclaration.CONSTANT,
                            stringType,
                            init);
                    globalSizes.add(typeNameString);
                    interfacesAdded.add(it);
                }

                LLVMVariable stringTypeGlobal = nf.LLVMVariable(
                            PolyLLVMMangler.interfaceStringVariable(it),
                            nf.LLVMPointerType(stringType), VarKind.GLOBAL);

                LLVMVariable interfaceStringBytePointer =
                        PolyLLVMFreshGen.freshNamedLocalVar(nf, "interface_string", bytePointerType);
                LLVMInstruction toBytePtr =
                        PolyLLVMFreshGen.freshGetElementPtr(nf,interfaceStringBytePointer,stringTypeGlobal,0,0);

                LLVMVariable res = PolyLLVMFreshGen.freshLocalVar(nf, nf.LLVMPointerType(bytePointerType));
                LLVMGetElementPtr gepITable = PolyLLVMFreshGen.freshGetElementPtr(nf, res, llvmItVar, 0, 1);

                LLVMStore storeTypeString = nf.LLVMStore(bytePointerType, interfaceStringBytePointer, res);

                instrs.add(toBytePtr);
                instrs.add(gepITable);
                instrs.add(storeTypeString);


                for (int k=0; k< layouts(it).part1().size(); k++) {
                    MethodInstance mi = layouts(it).part1().get(k);
                    int index = getMethodIndex(rt, mi);
                    MethodInstance miClass = methodInList(mi, classLayout);

                    LLVMTypeNode classFuncType =
                            PolyLLVMTypeUtils.polyLLVMMethodTypeNode(nf,
                                    rt,
                                    miClass.formalTypes(),
                                    miClass.returnType());

                    LLVMTypeNode interfaceFuncType =
                            PolyLLVMTypeUtils.polyLLVMMethodTypeNode(nf,
                                    it,
                                    mi.formalTypes(),
                                    mi.returnType());

                    LLVMVariable gepResultVal =
                            PolyLLVMFreshGen.freshLocalVar(nf, nf.LLVMPointerType(classFuncType));
                    LLVMGetElementPtr gepClassDV =
                            PolyLLVMFreshGen.freshGetElementPtr(nf, gepResultVal, llvmDvVar, 0, index);

                    LLVMVariable gepResultStore =
                            PolyLLVMFreshGen.freshLocalVar(nf, nf.LLVMPointerType(interfaceFuncType));
                    LLVMGetElementPtr gepInterfaceDV =
                            PolyLLVMFreshGen.freshGetElementPtr(nf, gepResultStore, llvmItVar,
                                    0, k + PolyLLVMConstants.DISPATCH_VECTOR_OFFSET);


                    LLVMESeq eseqVal = nf.LLVMESeq(gepClassDV, gepResultVal);
                    LLVMVariable loadResult = PolyLLVMFreshGen.freshLocalVar(nf, classFuncType);
                    LLVMLoad load = nf.LLVMLoad(loadResult, classFuncType, eseqVal);

                    LLVMVariable castResult = PolyLLVMFreshGen.freshLocalVar(nf, interfaceFuncType);
                    LLVMConversion bitcast =
                            nf.LLVMConversion(LLVMConversion.BITCAST,
                                    castResult,
                                    classFuncType,
                                    loadResult,
                                    interfaceFuncType);

                    LLVMESeq eseqStore = nf.LLVMESeq(gepInterfaceDV, gepResultStore);
                    LLVMStore store =
                            nf.LLVMStore(interfaceFuncType, castResult, eseqStore);

                    instrs.add(load);
                    instrs.add(bitcast);
                    instrs.add(store);
                }
            }
            instrs.add(nf.LLVMRet());
            LLVMFunction ctor =
                    nf.LLVMFunction(PolyLLVMMangler.interfacesInitFunction(rt),
                            new ArrayList<LLVMArgDecl>(),
                            nf.LLVMVoidType(),
                            nf.LLVMBlock(instrs));
            ctorsFunctions.add(ctor);
        }
    }

    private void setupCtorsFunctions() {
        for (ClassDecl cd : classesVisited) {

            List<LLVMInstruction> instrs = new ArrayList<>();

            List<MethodInstance> overridenMethods =
                    classMembers(cd.type()).part2();
            List<MethodInstance> newMethods = classMembers(cd.type()).part1();

            Pair<List<MethodInstance>, List<FieldInstance>> layouts =
                    layouts(cd.type());

            String dvVar = PolyLLVMMangler.dispatchVectorVariable(cd.type().toReference());
            LLVMTypeNode dvType =
                    PolyLLVMTypeUtils.polyLLVMDispatchVectorVariableType(this,
                                                                         cd.type());

            LLVMVariable llvmDvVar =
                    nf.LLVMVariable(dvVar,
                                    nf.LLVMPointerType(dvType),
                                    LLVMVariable.VarKind.GLOBAL);

            LLVMTypedOperand index0 = nf.LLVMTypedOperand(
                                                          nf.LLVMIntLiteral(nf.LLVMIntType(64),
                                                                            0),
                                                          nf.LLVMIntType(32));

            if (cd.type().superType() != null) {
                /*
                * Call the superclass init function
                */
                LLVMTypeNode tn =
                        nf.LLVMFunctionType(new ArrayList<LLVMTypeNode>(),
                                            nf.LLVMVoidType());
                LLVMVariable function = nf.LLVMVariable(
                                                        PolyLLVMMangler.classInitFunction((ReferenceType) cd.type()
                                                                                                            .superType()),
                                                        tn,
                                                        VarKind.GLOBAL);
                List<Pair<LLVMTypeNode, LLVMOperand>> args =
                        CollectionUtil.list();
                LLVMCall callSuperInit =
                        nf.LLVMCall(function, args, nf.LLVMVoidType());
                instrs.add(callSuperInit);
                calls.add(callSuperInit);

            }



            for (int i = 0; i < layouts.part1().size(); i++) {
                MethodInstance mi = layouts.part1().get(i);

                MethodInstance overridenMethod =
                        methodInList(mi, overridenMethods);
                MethodInstance newMethod = methodInList(mi, newMethods);

                if (overridenMethod != null) {
                    List<LLVMTypedOperand> l =
                            CollectionUtil.list(index0,
                                                nf.LLVMTypedOperand(nf.LLVMIntLiteral(nf.LLVMIntType(64),
                                                                                      i + PolyLLVMConstants.DISPATCH_VECTOR_OFFSET),
                                                                    nf.LLVMIntType(32)));

                    LLVMGetElementPtr gep = nf.LLVMGetElementPtr(llvmDvVar, l);
                    LLVMVariable gepResult =
                            PolyLLVMFreshGen.freshLocalVar(nf,
                                                           nf.LLVMPointerType(nf.LLVMPointerType(nf.LLVMIntType(8))));
                    LLVMESeq eseq =
                            nf.LLVMESeq(gep.result(gepResult), gepResult);

                    LLVMTypeNode classTypePointer =
                            nf.LLVMPointerType(nf.LLVMVariableType(PolyLLVMMangler.classTypeName(cd)));
                    LLVMTypeNode funcType =
                            PolyLLVMTypeUtils.polyLLVMFunctionTypeNode(nf,
                                                                       overridenMethod.formalTypes(),
                                                                       overridenMethod.returnType())
                                             .prependFormalTypeNode(classTypePointer);

                    LLVMStore store =
                            nf.LLVMStore(funcType,
                                         nf.LLVMVariable(PolyLLVMMangler.mangleProcedureName(overridenMethod),
                                                         funcType,
                                                         VarKind.GLOBAL),
                                         eseq);
                    instrs.add(store);

                }
                else if (newMethod != null) {

                    List<LLVMTypedOperand> l =
                            CollectionUtil.list(index0,
                                                nf.LLVMTypedOperand(nf.LLVMIntLiteral(nf.LLVMIntType(64),
                                                                                      i + PolyLLVMConstants.DISPATCH_VECTOR_OFFSET),
                                                                    nf.LLVMIntType(32)));
                    LLVMGetElementPtr gep = nf.LLVMGetElementPtr(llvmDvVar, l);
                    LLVMVariable gepResult =
                            PolyLLVMFreshGen.freshLocalVar(nf,
                                                           nf.LLVMPointerType(nf.LLVMPointerType(nf.LLVMIntType(8))));
                    LLVMESeq eseq =
                            nf.LLVMESeq(gep.result(gepResult), gepResult);
                    LLVMTypeNode funcType =
                            PolyLLVMTypeUtils.polyLLVMMethodTypeNode(nf,
                                                                     cd.type(),
                                                                     newMethod.formalTypes(),
                                                                     newMethod.returnType());
                    LLVMStore store =
                            nf.LLVMStore(funcType,
                                         nf.LLVMVariable(PolyLLVMMangler.mangleProcedureName(newMethod),
                                                         funcType,
                                                         LLVMVariable.VarKind.GLOBAL),
                                         eseq);
                    instrs.add(store);
                }
                else {
                    ReferenceType superType =
                            (ReferenceType) cd.type().superType();
                    String superDvVar =
                            PolyLLVMMangler.dispatchVectorVariable(superType);
                    LLVMTypeNode superDvType =
                            PolyLLVMTypeUtils.polyLLVMDispatchVectorVariableType(this,
                                                                                 superType);

                    LLVMVariable llvmSuperDvVar =
                            nf.LLVMVariable(superDvVar,
                                            nf.LLVMPointerType(superDvType),
                                            LLVMVariable.VarKind.GLOBAL);

                    List<LLVMTypedOperand> l =
                            CollectionUtil.list(index0,
                                                nf.LLVMTypedOperand(nf.LLVMIntLiteral(nf.LLVMIntType(64),
                                                                                      i + PolyLLVMConstants.DISPATCH_VECTOR_OFFSET),
                                                                    nf.LLVMIntType(32)));

                    LLVMGetElementPtr gepVal =
                            nf.LLVMGetElementPtr(llvmSuperDvVar, l);
                    LLVMGetElementPtr gepStore =
                            nf.LLVMGetElementPtr(llvmDvVar, l);

                    LLVMTypeNode classFuncType =
                            PolyLLVMTypeUtils.polyLLVMMethodTypeNode(nf,
                                                                     cd.type(),
                                                                     mi.formalTypes(),
                                                                     mi.returnType());
                    LLVMTypeNode superClassFuncType =
                            PolyLLVMTypeUtils.polyLLVMMethodTypeNode(nf,
                                                                     superType,
                                                                     mi.formalTypes(),
                                                                     mi.returnType());

                    LLVMVariable gepResultVal =
                            PolyLLVMFreshGen.freshLocalVar(nf,
                                                           nf.LLVMPointerType(superClassFuncType));
                    LLVMVariable gepResultStore =
                            PolyLLVMFreshGen.freshLocalVar(nf,
                                                           nf.LLVMPointerType(classFuncType));
                    LLVMESeq eseqVal = nf.LLVMESeq(gepVal.result(gepResultVal),
                                                   gepResultVal);
                    LLVMESeq eseqStore =
                            nf.LLVMESeq(gepStore.result(gepResultStore),
                                        gepResultStore);

                    LLVMVariable loadResult =
                            PolyLLVMFreshGen.freshLocalVar(nf,
                                                           superClassFuncType);
                    LLVMVariable castResult =
                            PolyLLVMFreshGen.freshLocalVar(nf, classFuncType);

                    LLVMLoad load = nf.LLVMLoad(loadResult,
                                                superClassFuncType,
                                                eseqVal);

                    LLVMConversion bitcast =
                            nf.LLVMConversion(LLVMConversion.BITCAST,
                                              castResult,
                                              superClassFuncType,
                                              loadResult,
                                              classFuncType);

                    LLVMStore store =
                            nf.LLVMStore(classFuncType, castResult, eseqStore);

                    instrs.add(load);
                    instrs.add(bitcast);
                    instrs.add(store);

                }

            }
            instrs.add(nf.LLVMRet());
            LLVMFunction ctor =
                    nf.LLVMFunction(PolyLLVMMangler.classInitFunction(cd),
                                    new ArrayList<LLVMArgDecl>(),
                                    nf.LLVMVoidType(),
                                    nf.LLVMBlock(instrs));
            ctorsFunctions.add(ctor);

        }

    }

    private MethodInstance methodInList(MethodInstance mi,
            List<MethodInstance> methods) {
        for (MethodInstance m : methods) {
            if (m.isSameMethod(mi)) {//m.name().equals(mi.name())) {
                return m;
            }
        }
        return null;
    }

    private void setupClassSizes() {
        Set<String> classesAdded = new HashSet<>();

        // Arrays.
        ReferenceType arrayType = getArrayType();
        LLVMGlobalVarDeclaration arrayDvSize = nf.LLVMGlobalVarDeclaration(
                PolyLLVMMangler.dispatchVectorVariable(arrayType),
                /* isExtern */ true,
                LLVMGlobalVarDeclaration.GLOBAL,
                nf.LLVMVariableType(PolyLLVMMangler.dispatchVectorTypeName(arrayType)),
                null);
        globalSizes.add(arrayDvSize);
        classesAdded.add(arrayType.toString());

        // Helper for setting up size globals.
        BiConsumer<ReferenceType, Boolean> addSizeGlobals = (rt, isExtern) -> {

            // Ignore array types--they're handled separately.
            if (rt.isArray()) {
                return;
            }

            // Avoid adding globals more than once.
            String name = rt.toString();
            if (classesAdded.contains(name)) {
                return;
            }
            classesAdded.add(name);

            // Do not add globals for interfaces
            if(rt instanceof ParsedClassType && ((ParsedClassType) rt).flags().isInterface()){
                return;
            }

            // Class size.
            LLVMGlobalVarDeclaration classSizeDecl = nf.LLVMGlobalVarDeclaration(
                    PolyLLVMMangler.sizeVariable(rt),
                    isExtern,
                    LLVMGlobalVarDeclaration.GLOBAL,
                    nf.LLVMIntType(64),
                    /* initValue */ null);
            globalSizes.add(classSizeDecl);

            // DV size.
            LLVMTypeNode dvTypeVariable =
                    PolyLLVMTypeUtils.polyLLVMDispatchVectorVariableType(this, rt);
            LLVMGlobalVarDeclaration dvSizeDecl = nf.LLVMGlobalVarDeclaration(
                    PolyLLVMMangler.dispatchVectorVariable(rt),
                    isExtern,
                    LLVMGlobalVarDeclaration.GLOBAL,
                    dvTypeVariable,
                    /* initValue */ null);
            globalSizes.add(dvSizeDecl);

            // ITables
            for(ReferenceType i :rt.interfaces()){
                if (classesAdded.contains(i.toString() + rt.toString())) {
                    return;
                }
                classesAdded.add(i.toString() + "_" +  rt.toString());

                LLVMTypeNode interfaceTypeVariable =
                        PolyLLVMTypeUtils.polyLLVMDispatchVectorVariableType(this, i);
                LLVMGlobalVarDeclaration itableDecl = nf.LLVMGlobalVarDeclaration(
                        PolyLLVMMangler.InterfaceTableVariable(rt, i),
                        isExtern,
                        LLVMGlobalVarDeclaration.GLOBAL,
                        interfaceTypeVariable,
                    /* initValue */ null);
                globalSizes.add(itableDecl);
            }

        };

        // Parsed classes and their superclasses.
        for (ClassDecl cd : classesVisited) {
            addSizeGlobals.accept(cd.type().toReference(), false);

            ReferenceType superClass = (ReferenceType) cd.type().superType();
            while (superClass != null) {
                addSizeGlobals.accept(superClass, /* isExtern */ true);
                superClass = (ReferenceType) superClass.superType();
            }
        }

        // Referenced classes.
        for (ReferenceType rt : classesUsed) {
            addSizeGlobals.accept(rt, /* isExtern */ true);
        }
    }

    private void setupClassTypes() {
        // Arrays.
        ReferenceType arrayType = getArrayType();
        String arrayDvName = PolyLLVMMangler.dispatchVectorTypeName(arrayType);
        List<LLVMTypeNode> arrayStruct = Arrays.asList(
                nf.LLVMPointerType(nf.LLVMVariableType(arrayDvName)),
                nf.LLVMIntType(32),
                nf.LLVMPointerType(nf.LLVMIntType(8)));
        classTypes.putIfAbsent(
                PolyLLVMMangler.classTypeName(arrayType),
                nf.LLVMStructureType(arrayStruct));
        classTypes.putIfAbsent(
                PolyLLVMMangler.dispatchVectorTypeName(arrayType),
                PolyLLVMTypeUtils.polyLLVMDispatchVectorType(this, arrayType));

        // Helper for adding structure type and dv type.
        Consumer<ReferenceType> addClassTypes = rt -> {
            classTypes.putIfAbsent(
                    PolyLLVMMangler.classTypeName(rt),
                    PolyLLVMTypeUtils.polyLLVMObjectType(this, rt));
            classTypes.putIfAbsent(
                    PolyLLVMMangler.dispatchVectorTypeName(rt),
                    PolyLLVMTypeUtils.polyLLVMDispatchVectorType(this, rt));
        };

        // Parsed classes and their superclasses.
        for (ClassDecl cd : classesVisited) {
            addClassTypes.accept(cd.type().toReference());

            ReferenceType superClass = (ReferenceType) cd.type().superType();
            while (superClass != null) {
                addClassTypes.accept(superClass);
                superClass = (ReferenceType) superClass.superType();
            }
        }

        // Referenced classes.
        for (ReferenceType rt : classesUsed) {
            addClassTypes.accept(rt);
        }

        // Opaque class type.
        classTypes.putIfAbsent("class.java_lang_String", null);
        classTypes.putIfAbsent("class.java_lang_Class", null);
    }

    private HashSet<ReferenceType> classesUsed = new HashSet<>();

    public void addClassType(ReferenceType rt) {
        classesUsed.add(rt);
    }

    /**
     * Return a list of global declarations for all classes defined.
     * @return
     */
    public List<LLVMGlobalDeclaration> globalDeclarations() {
        setupClassData();
        List<LLVMGlobalDeclaration> typeDecls = new ArrayList<>();
        for (Entry<String, LLVMTypeNode> e : classTypes.entrySet()) {
            typeDecls.add(nf.LLVMTypeDeclaration(e.getKey(), e.getValue()));
        }
        typeDecls.addAll(globalSizes);
        return typeDecls;
    }

    /**
     * Return a list of ctor functions to run before application is started.
     * @return
     */
    public List<LLVMFunction> ctorFunctions() {
        return ListUtil.copy(ctorsFunctions, false);
    }

    /*
     * Functions for generating loads and stores from stack allocated variables
     */
    private Map<String, LLVMTypeNode> allocations = new HashMap<>();
    private Map<String, LLVMTypeNode> arguments = new HashMap<>();

    public void addArgument(String arg, LLVMTypeNode tn) {
        arguments.put(arg, tn);
    }

    public boolean isArg(String name) {
        return arguments.containsKey(name);
    }

    public void clearArguments() {
        arguments.clear();
    }

    public void addAllocation(String var, LLVMTypeNode tn) {
        allocations.put(var, tn);
    }

    public void clearAllocations() {
        allocations.clear();
    }

    public String varName(String var) {
        if (isArg(var)) {
            return "arg_" + var;
        }
        return var;
    }

    public Pair<LLVMLoad, LLVMVariable> variableLoad(String name,
            LLVMTypeNode tn) {
        LLVMVariable temp = PolyLLVMFreshGen.freshLocalVar(nf, tn);
        LLVMPointerType varPtrType = nf.LLVMPointerType(tn);
        LLVMVariable var =
                nf.LLVMVariable(varName(name), varPtrType, LLVMVariable.VarKind.LOCAL);
        LLVMLoad load = nf.LLVMLoad(temp, tn, var);
        return new Pair<>(load, temp);
    }

    public List<LLVMInstruction> allocationInstructions() {
        List<LLVMInstruction> allocs = new ArrayList<>();
        for (Entry<String, LLVMTypeNode> e : allocations.entrySet()) {
            LLVMAlloca a = nf.LLVMAlloca(e.getValue());
            LLVMPointerType allocResultType = nf.LLVMPointerType(e.getValue());
            allocs.add(a.result(nf.LLVMVariable(varName(e.getKey()),
                                                allocResultType,
                                                LLVMVariable.VarKind.LOCAL)));
        }
        for (Entry<String, LLVMTypeNode> e : arguments.entrySet()) {
            LLVMAlloca a = nf.LLVMAlloca(e.getValue());
            LLVMPointerType allocResultType = nf.LLVMPointerType(e.getValue());
            allocs.add(a.result(nf.LLVMVariable(varName(e.getKey()),
                                                allocResultType,
                                                LLVMVariable.VarKind.LOCAL)));
            LLVMTypeNode valueType = null;
            LLVMTypeNode ptrType = nf.LLVMPointerType(allocResultType);
            allocs.add(nf.LLVMStore(e.getValue(),
                                    nf.LLVMVariable(e.getKey(),
                                                    valueType,
                                                    VarKind.LOCAL),
                                    nf.LLVMVariable(varName(e.getKey()),
                                                    ptrType,
                                                    LLVMVariable.VarKind.LOCAL)));
        }
        return allocs;
    }

    /*
     * Functions for translating loops
     */

    /**
     * loops is a list of (String * (String * String)), where the first String
     * is a label (or null), and the pair of strings is the head label and end
     * label for that loop.
     */
    private LinkedList<Pair<String, Pair<String, String>>> loops =
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
            String head =
                    PolyLLVMFreshGen.freshNamedLabel(nf, "loop.head").name();
            String end =
                    PolyLLVMFreshGen.freshNamedLabel(nf, "loop.end").name();
            Pair<String, Pair<String, String>> pair =
                    new Pair<>("", new Pair<>(head, end));
            loops.push(pair);
        }

    }

    public Pair<String, String> leaveLoop() {
        return loops.pop().part2();
    }

    private Labeled label;
    private String labelhead;
    private String labelend;

    public void enterLabeled(Labeled n) {
        label = n;
        labelhead =
                PolyLLVMFreshGen.freshNamedLabel(nf,
                                                 "loop." + n.label() + ".head")
                                .name();
        labelend =
                PolyLLVMFreshGen.freshNamedLabel(nf,
                                                 "loop." + n.label() + ".end")
                                .name();
    }

    public String getLoopEnd(String label) {
        for (Pair<String, Pair<String, String>> pair : loops) {
            if (pair.part1().equals(label)) {
                return pair.part2().part2();
            }
        }
        throw new InternalCompilerError("Loop labeled " + label
                + " is not on loop stack");
    }

    public String getLoopHead(String label) {
        for (Pair<String, Pair<String, String>> pair : loops) {
            if (pair.part1().equals(label)) {
                return pair.part2().part1();
            }
        }

        throw new InternalCompilerError("Loop labeled " + label
                + " is not on loop stack");
    }

    public String getLoopHead() {
        Pair<String, Pair<String, String>> pair = loops.get(0);
        return pair.part2().part1();
    }

    public String getLoopEnd() {
        Pair<String, Pair<String, String>> pair = loops.get(0);
        return pair.part2().part2();
    }

    public int getMethodIndex(ReferenceType type, MethodInstance methodInstance) {
        List<MethodInstance> methodLayout = layouts(type).part1();
        for (int i = 0; i < methodLayout.size(); i++) {
            if (methodLayout.get(i).isSameMethod(methodInstance)) {
                return i + PolyLLVMConstants.DISPATCH_VECTOR_OFFSET;
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
     * Keep track of which static methods are called
     */
    private List<LLVMCall> calls = new ArrayList<>();

    public void addStaticCall(LLVMCall llvmCall) {
        calls.add(llvmCall);
    }

    public List<LLVMCall> getStaticCalls() {
        return ListUtil.copy(calls, false);
    }

    /* Keep track of which static variables are referenced. */
    private Map<String, LLVMGlobalVarDeclaration> referencedStaticVars = new HashMap<>();

    public void addStaticVarReferenced(String name, LLVMGlobalVarDeclaration var) {
        referencedStaticVars.put(name, var);
    }

    public Collection<LLVMGlobalVarDeclaration> getReferencedStaticVars() {
        return referencedStaticVars.values();
    }

    /**
     * Get the reference type of the runtime class {@code Array}
     */
    public ReferenceType getArrayType() {
        ReferenceType arrayType;
        try {
            arrayType = (ReferenceType) typeSystem().typeForName("support.Array");
        }
        catch (SemanticException e) {
            throw new InternalCompilerError("Could not load array type");
        }
        catch (ClassCastException e) {
            throw new InternalCompilerError("Could not load array type");
        }
        return arrayType;
    }

}
