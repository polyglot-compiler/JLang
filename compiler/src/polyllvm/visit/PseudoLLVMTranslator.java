package polyllvm.visit;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import polyglot.ast.ClassDecl;
import polyglot.ast.Labeled;
import polyglot.ast.Loop;
import polyglot.ast.MethodDecl;
import polyglot.ast.Node;
import polyglot.types.FieldInstance;
import polyglot.types.MemberInstance;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;
import polyglot.util.Pair;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;
import polyllvm.ast.PolyLLVMLang;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.LLVMFunction;
import polyllvm.ast.PseudoLLVM.LLVMGlobalDeclaration;
import polyllvm.ast.PseudoLLVM.LLVMGlobalVarDeclaration;
import polyllvm.ast.PseudoLLVM.LLVMNode;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMESeq;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMIntLiteral;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMTypedOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable_c.VarType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMIntType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMPointerType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.Statements.LLVMAlloca;
import polyllvm.ast.PseudoLLVM.Statements.LLVMGetElementPtr;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;
import polyllvm.ast.PseudoLLVM.Statements.LLVMLoad;
import polyllvm.ast.PseudoLLVM.Statements.LLVMStore;
import polyllvm.util.PolyLLVMFreshGen;
import polyllvm.util.PolyLLVMMangler;
import polyllvm.util.PolyLLVMTypeUtils;
import polyllvm.util.Triple;

public class PseudoLLVMTranslator extends NodeVisitor {

    private PolyLLVMNodeFactory nf;

    private Map<Node, LLVMNode> translations;
    private Deque<ClassDecl> classes;

    private List<ClassDecl> classesVisited;

    private List<LLVMTypeNode> classTypes;
    private List<LLVMGlobalVarDeclaration> globalSizes;
    private List<LLVMFunction> ctorsFunctions;

    public PseudoLLVMTranslator(PolyLLVMNodeFactory nf) {
        super(nf.lang());
        this.nf = nf;
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

    /**
     * Get the node factory used by the compiler to create new nodes
     * @return
     */
    public PolyLLVMNodeFactory nodeFactory() {
        return nf;
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
        ClassDecl leaving = classes.pop();
//        ClassBody body = leaving.body();
//
//        List<MethodDecl> dvMethods = new ArrayList<>();
//        List<FieldDecl> fields = new ArrayList<>();
//
//        for (ClassMember cm : body.members()) {
//            if (cm instanceof MethodDecl) {
//                MethodDecl m = (MethodDecl) cm;
//                if (!m.methodInstance().flags().isStatic()) {
//                    dvMethods.add(m);
//                }
//            }
//            else if (cm instanceof FieldDecl) {
//                FieldDecl f = (FieldDecl) cm;
//                fields.add(f);
//            }
//
//        }
//
//        dvMethods.sort(new Comparator<MethodDecl>() {
//            @Override
//            public int compare(MethodDecl o1, MethodDecl o2) {
//                return o1.name().compareTo(o2.name());
//            }
//        });
//
//        fields.sort(new Comparator<FieldDecl>() {
//            @Override
//            public int compare(FieldDecl o1, FieldDecl o2) {
//                return o1.name().compareTo(o2.name());
//            }
//        });
//
//        System.out.println("Sorted fields: " + fields);
//        System.out.println("Sorted methods: " + dvMethods);

//        if (leaving.superClass() != null) {
//
//        }
//
//        List<LLVMBlock> blocks = null;
//
//        List<MethodDecl> dvMethods = new ArrayList<>();
//        List<FieldDecl> fields = new ArrayList<>();
//
//        for (ClassMember cm : body.members()) {
//            if (cm instanceof MethodDecl) {
//                MethodDecl m = (MethodDecl) cm;
//                if (!m.methodInstance().flags().isStatic()) {
//                    dvMethods.add(m);
//                    for (MethodInstance mi : m.methodInstance().overrides()) {
//                        System.out.println("        " + mi + " -- "
//                                + mi.container() + " -- "
//                                + mi.container().members());
//                    }
//                    System.out.println(m.methodInstance().overrides());
//                }
//                MemberInstance memberInstance = m.memberInstance();
//                System.out.println("Container: " + memberInstance.container());
//                List<MethodInstance> overrides = m.methodInstance().overrides();
//                for (MethodInstance mi : overrides) {
//
//                    Declaration d = mi.declaration();
//
//                }
//                //TODO: do something with this - m.methodInstance().overrides();
//
//            }
//            else if (cm instanceof FieldDecl) {
//                FieldDecl f = (FieldDecl) cm;
//                fields.add(f);
//            }
//        }
//
//        dvMethods.sort(new Comparator<MethodDecl>() {
//            @Override
//            public int compare(MethodDecl o1, MethodDecl o2) {
//                return o1.name().compareTo(o2.name());
//            }
//        });
//
//        fields.sort(new Comparator<FieldDecl>() {
//            @Override
//            public int compare(FieldDecl o1, FieldDecl o2) {
//                return o1.name().compareTo(o2.name());
//            }
//        });
//
//        System.out.println("Sorted fields: " + fields);
//        System.out.println("Sorted methods: " + dvMethods);
//
//        String setupFuncName = PolyLLVMMangler.classInitFunction(leaving);
//        LLVMFunction ctorFunc = nf.LLVMFunction(Position.compilerGenerated(),
//                                                setupFuncName,
//                                                new ArrayList<LLVMArgDecl>(),
//                                                nf.LLVMVoidType(),
//                                                blocks);

    }

    /**
     * Set {@code n} as the new current class
     */
    public void enterClass(ClassDecl n) {
        classes.push(n);
        classesVisited.add(n);

//        classTypes.add(PolyLLVMTypeUtils.polyLLVMObjectType(nf, n));
//
//        String name = PolyLLVMMangler.sizeVariable(n);
//        LLVMIntType i64Type = nf.LLVMIntType(Position.compilerGenerated(), 64);
//        LLVMIntLiteral sizeInit;
//        boolean isExtern;
//        if (n.body() != null) {
//            sizeInit =
//                    nf.LLVMIntLiteral(Position.compilerGenerated(), i64Type, 0);
//            isExtern = false;
//        }
//        else {
//            sizeInit = null;
//            isExtern = true;
//        }
//        globalSizes.add(nf.LLVMGlobalVarDeclaration(name,
//                                                    isExtern,
//                                                    LLVMGlobalVarDeclaration.GLOBAL,
//                                                    i64Type,
//                                                    sizeInit));

    }

    /**
     * Return the current class
     * @return
     */
    public ClassDecl getCurrentClass() {
        return classes.peek();
    }

    private boolean isOverridden(MethodDecl m) {
        MethodInstance methodInstance = m.methodInstance();
        return isOverridden(methodInstance);
    }

    private boolean isOverridden(MethodInstance methodInstance) {
        List<MethodInstance> overrides = methodInstance.overrides();
        List<ReferenceType> refTypes = new ArrayList<>();
        for (MethodInstance mi : overrides) {
            refTypes.add(mi.container());
        }
        ReferenceType highestSuperType =
                Collections.max(refTypes, new Comparator<ReferenceType>() {

                    @Override
                    public int compare(ReferenceType o1, ReferenceType o2) {
                        return o1.descendsFrom(o2) ? -1 : 1;
                    }
                });

//        System.out.println("\nStarting type: " + highestSuperType);
//        ReferenceType x = highestSuperType;
//        while (x.superType() != null) {
//            if (x.superType() instanceof ReferenceType) {
//                x = (ReferenceType) x.superType();
//                System.out.println("IS A REF TYPE: " + x + " - members - "
//                        + x.members());
//            }
//            else {
//                System.out.println("NOT A REF TYPE: " + x.superType());
//                break;
//            }
//        }
//        System.out.println("While loop done\n");

//        System.out.println();
//        refTypes.stream().forEach(new Consumer<ReferenceType>() {
//            @Override
//            public void accept(ReferenceType rt) {
//                System.out.println("Ref Type " + rt + " ----- " + rt.methods());
//            }
//        });
//        System.out.println("\nREFTYPES ---- " + refTypes);
        return !methodInstance.container().typeEquals(highestSuperType);
    }

    private void setupClassData() {
        if (classTypes != null && globalSizes != null
                && ctorsFunctions != null) {
            return;
        }
        classTypes = new ArrayList<>();
        globalSizes = new ArrayList<>();
        ctorsFunctions = new ArrayList<>();

        setupClassTypes();
        setupClassSizes();
        setupCtorsFunctions();

    }

    private Pair<List<MethodInstance>, List<FieldInstance>> layouts(
            ReferenceType rt) {
        List<MethodInstance> dvLayout = new ArrayList<>();
        List<FieldInstance> objLayout = new ArrayList<>();

        ReferenceType superClass = rt;
        while (superClass != null) {
            System.out.println(superClass.toString());
            if (superClass.toString().equals("java.lang.Object")) {
                break;
            }
            Pair<List<MethodInstance>, List<FieldInstance>> pair =
                    nonOverriddenClassMembers(superClass);
            dvLayout.addAll(0, pair.part1());
            objLayout.addAll(0, pair.part2());

            superClass = (ReferenceType) superClass.superType();
        }
        return new Pair<>(dvLayout, objLayout);
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

    private void setupCtorsFunctions() {
        for (ClassDecl cd : classesVisited) {

            List<LLVMInstruction> instrs = new ArrayList<>();

//            ClassBody body = cd.body();
//
//            Triple<List<MethodInstance>, List<MethodInstance>, List<FieldInstance>> triple =
//                    classMembers(cd.type());
//
//            List<MethodInstance> dvMethods = triple.part1();
//            List<MethodInstance> dvOverridenMethods = triple.part2();
//            List<FieldInstance> fields = triple.part3();

//            List<MethodInstance> dvLayout = new ArrayList<>();
//
//            Queue<ReferenceType> superTypes = new ArrayDeque<>();
//            ReferenceType superClass = cd.type();
//            while (superClass != null) {
//                superTypes.add(superClass);
//                superClass = (ReferenceType) superClass.superType();
//            }
//
//            ((ArrayDeque) superTypes).pollLast(); //TODO : This removes Object as a supertype, it is only temporary
//
//            for (ReferenceType rt : superTypes) {
//                Triple<List<MethodInstance>, List<MethodInstance>, List<FieldInstance>> triple =
//                        classMembers(rt);
//
//                List<MethodInstance> dvMethods = triple.part1();
//                List<MethodInstance> dvOverridenMethods = triple.part2();
//                List<FieldInstance> fields = triple.part3();
//
//                dvLayout.addAll(0, dvMethods);
//            }
//
//            System.out.println("\nSuperTypes are: " + superTypes);
//            System.out.println("DV Layout is: ");
//            dvLayout.stream().forEach(new Consumer<MethodInstance>() {
//                @Override
//                public void accept(MethodInstance t) {
//                    System.out.println("    ● " + t);
//                }
//            });

//            System.out.println("Sorted fields: " + fields);
//            System.out.println("Sorted methods: " + dvMethods);
//            System.out.println("Sorted overridden methods: "
//                    + dvOverridenMethods);

            List<MethodInstance> overridenMethods =
                    classMembers(cd.type()).part2();
            List<MethodInstance> newMethods = classMembers(cd.type()).part1();

            Pair<List<MethodInstance>, List<FieldInstance>> layouts =
                    layouts(cd.type());

            System.out.println("\nObject layout for " + cd + "is: ");
            layouts.part2().stream().forEach(new Consumer<FieldInstance>() {
                @Override
                public void accept(FieldInstance t) {
                    System.out.println("    ● " + t);
                }
            });

            System.out.println("DV layout for " + cd + "is: ");
            layouts.part1().stream().forEach(new Consumer<MethodInstance>() {
                @Override
                public void accept(MethodInstance t) {
                    System.out.println("    ● " + t);
                }
            });

            System.out.println("\nOverridden Methods are: " + overridenMethods);
            System.out.println("\nRough CTOR: ");
            String dvVar = PolyLLVMMangler.dispatchVectorVariable(cd);
            LLVMTypeNode dvType =
                    PolyLLVMTypeUtils.polyLLVMDispatchVectorType(nf, cd);
            String superDvVar =
                    PolyLLVMMangler.dispatchVectorVariable(cd.superClass());
            LLVMTypeNode superDvType =
                    PolyLLVMTypeUtils.polyLLVMDispatchVectorType(nf,
                                                                 cd.superClass());

            LLVMVariable llvmDvVar =
                    nf.LLVMVariable(Position.compilerGenerated(),
                                    dvVar,
                                    nf.LLVMPointerType(dvType),
                                    VarType.GLOBAL);

            for (int i = 0; i < layouts.part1().size(); i++) {
                MethodInstance mi = layouts.part1().get(i);

                MethodInstance overridenMethod =
                        methodInList(mi, overridenMethods);
                MethodInstance newMethod = methodInList(mi, newMethods);

                if (overridenMethod != null) {
//                    System.out.println("    " + i
//                            + ". Write new implmentation of : " + mi + " --> "
//                            + PolyLLVMMangler.mangleMethodName(mi));
                    List<LLVMTypedOperand> l =
                            CollectionUtil.list(nf.LLVMTypedOperand(nf.LLVMIntLiteral(nf.LLVMIntType(64),
                                                                                      0),
                                                                    nf.LLVMPointerType(nf.LLVMIntType(8))),
                                                nf.LLVMTypedOperand(nf.LLVMIntLiteral(nf.LLVMIntType(64),
                                                                                      i),
                                                                    nf.LLVMPointerType(nf.LLVMIntType(8))));

                    LLVMGetElementPtr gep = nf.LLVMGetElementPtr(llvmDvVar, l);
                    LLVMVariable gepResult =
                            PolyLLVMFreshGen.freshLocalVar(nf,
                                                           nf.LLVMPointerType(nf.LLVMPointerType(nf.LLVMIntType(8))));
                    LLVMESeq eseq = nf.LLVMESeq(gep, gepResult);
                    LLVMStore store =
                            nf.LLVMStore(nf.LLVMPointerType(nf.LLVMIntType(8)),
                                         nf.LLVMVariable(Position.compilerGenerated(),
                                                         PolyLLVMMangler.mangleMethodName(overridenMethod),
                                                         nf.LLVMPointerType(nf.LLVMIntType(8)),
                                                         VarType.LOCAL),
                                         eseq);
                    instrs.add(store);

//                    System.out.println("        %ptr.0 = getelementptr "
//                            + dvType + ", " + dvType + "* @" + dvVar
//                            + ", i8* 0, i8* " + i);
//                    System.out.println("        store i8* %"
//                            + PolyLLVMMangler.mangleMethodName(overridenMethod)
//                            + ", i8** %ptr.0");

                }
                else if (newMethod != null) {
//                    System.out.println("    " + i
//                            + ". Wow thats a new method of the class "
//                            + cd.name() + " : " + mi + " --> "
//                            + PolyLLVMMangler.mangleMethodName(mi));

                    List<LLVMTypedOperand> l =
                            CollectionUtil.list(nf.LLVMTypedOperand(nf.LLVMIntLiteral(nf.LLVMIntType(64),
                                                                                      0),
                                                                    nf.LLVMPointerType(nf.LLVMIntType(8))),
                                                nf.LLVMTypedOperand(nf.LLVMIntLiteral(nf.LLVMIntType(64),
                                                                                      i),
                                                                    nf.LLVMPointerType(nf.LLVMIntType(8))));

                    LLVMGetElementPtr gep = nf.LLVMGetElementPtr(llvmDvVar, l);
                    LLVMVariable gepResult =
                            PolyLLVMFreshGen.freshLocalVar(nf,
                                                           nf.LLVMPointerType(nf.LLVMPointerType(nf.LLVMIntType(8))));
                    LLVMESeq eseq = nf.LLVMESeq(gep, gepResult);
                    LLVMStore store =
                            nf.LLVMStore(nf.LLVMPointerType(nf.LLVMIntType(8)),
                                         nf.LLVMVariable(Position.compilerGenerated(),
                                                         PolyLLVMMangler.mangleMethodName(newMethod),
                                                         nf.LLVMPointerType(nf.LLVMIntType(8)),
                                                         VarType.LOCAL),
                                         eseq);
                    instrs.add(store);

//                    System.out.println("        %ptr.0 = getelementptr "
//                            + dvType + ", " + dvType + "* @" + dvVar
//                            + ", i8* 0, i8* " + i);
//                    System.out.println("        store i8* %"
//                            + PolyLLVMMangler.mangleMethodName(newMethod)
//                            + ", i8** %ptr.0");

                }
                else {
//                    System.out.println("    " + i
//                            + ". Copy old implmentation of : " + mi);
                    LLVMVariable llvmSuperDvVar =
                            nf.LLVMVariable(Position.compilerGenerated(),
                                            superDvVar,
                                            nf.LLVMPointerType(superDvType),
                                            VarType.GLOBAL);

                    List<LLVMTypedOperand> l =
                            CollectionUtil.list(nf.LLVMTypedOperand(nf.LLVMIntLiteral(nf.LLVMIntType(64),
                                                                                      0),
                                                                    nf.LLVMPointerType(nf.LLVMIntType(8))),
                                                nf.LLVMTypedOperand(nf.LLVMIntLiteral(nf.LLVMIntType(64),
                                                                                      i),
                                                                    nf.LLVMPointerType(nf.LLVMIntType(8))));

                    LLVMGetElementPtr gepVal =
                            nf.LLVMGetElementPtr(llvmSuperDvVar, l);
                    LLVMGetElementPtr gepStore =
                            nf.LLVMGetElementPtr(llvmDvVar, l);
                    LLVMVariable gepResultVal =
                            PolyLLVMFreshGen.freshLocalVar(nf,
                                                           nf.LLVMPointerType(nf.LLVMPointerType(nf.LLVMIntType(8))));
                    LLVMVariable gepResultStore =
                            PolyLLVMFreshGen.freshLocalVar(nf,
                                                           nf.LLVMPointerType(nf.LLVMPointerType(nf.LLVMIntType(8))));
                    LLVMESeq eseqVal = nf.LLVMESeq(gepVal, gepResultVal);
                    LLVMESeq eseqStore = nf.LLVMESeq(gepStore, gepResultStore);

                    LLVMVariable loadResult =
                            PolyLLVMFreshGen.freshLocalVar(nf,
                                                           nf.LLVMPointerType(nf.LLVMIntType(8)));

                    LLVMLoad load = nf.LLVMLoad(loadResult,
                                                nf.LLVMPointerType(nf.LLVMIntType(8)),
                                                eseqVal);
                    LLVMStore store =
                            nf.LLVMStore(nf.LLVMPointerType(nf.LLVMIntType(8)),
                                         loadResult,
                                         eseqStore);

//                    System.out.println("\n\n");
//                    load.prettyPrint(nf.lang(), System.out);
//                    System.out.println();
//                    store.prettyPrint(nf.lang(), System.out);
//                    System.out.println("\n\n");

                    instrs.add(load);
                    instrs.add(store);

                    System.out.println("        %ptr.0 = getelementptr "
                            + superDvType + ", " + superDvType + "* @"
                            + superDvVar + ", i8* 0, i8* " + i);
                    System.out.println("        %ptr.1 = getelementptr "
                            + dvType + ", " + dvType + "* @" + dvVar
                            + ", i8* 0, i8* " + i);

                    System.out.println("        %val.0 = load i8*, i8** %ptr.0");
                    System.out.println("        store i8* %val.0, i8** %ptr.1");

                }
            }
            System.out.println();

            System.out.println("DV is : " + dvVar + "\n");
        }
    }

    private MethodInstance methodInList(MethodInstance mi,
            List<MethodInstance> methods) {
        for (MethodInstance m : methods) {
            if (m.name().equals(mi.name())) {
                return m;
            }
        }
        return null;
    }

    private void setupClassSizes() {
        Set<String> classesAdded = new HashSet<>();
        for (ClassDecl cd : classesVisited) {
            String name = PolyLLVMMangler.sizeVariable(cd);
            LLVMIntType i64Type = nf.LLVMIntType(64);
            LLVMIntLiteral sizeInit = nf.LLVMIntLiteral(i64Type, 0);
            boolean isExtern = false;
            globalSizes.add(nf.LLVMGlobalVarDeclaration(name,
                                                        isExtern,
                                                        LLVMGlobalVarDeclaration.GLOBAL,
                                                        i64Type,
                                                        sizeInit));
            classesAdded.add(cd.name());
        }
        for (ClassDecl cd : classesVisited) {
            if (cd.superClass() != null
                    && !classesAdded.contains(cd.superClass().name())) {
                String name = PolyLLVMMangler.sizeVariable(cd.superClass());
                LLVMIntType i64Type = nf.LLVMIntType(64);
                LLVMIntLiteral sizeInit = null;
                boolean isExtern = true;
                globalSizes.add(nf.LLVMGlobalVarDeclaration(name,
                                                            isExtern,
                                                            LLVMGlobalVarDeclaration.GLOBAL,
                                                            i64Type,
                                                            sizeInit));
            }
        }
    }

    private void setupClassTypes() {
        Set<String> classesAdded = new HashSet<>();
        for (ClassDecl cd : classesVisited) {
            if (!classesAdded.contains(cd.name())) {
                classTypes.add(PolyLLVMTypeUtils.polyLLVMObjectType(nf, cd));
                classTypes.add(PolyLLVMTypeUtils.polyLLVMDispatchVectorType(nf,
                                                                            cd));
                classesAdded.add(cd.name());
            }
            if (cd.superClass() != null
                    && !classesAdded.contains(cd.superClass().name())) {
                classTypes.add(PolyLLVMTypeUtils.polyLLVMObjectType(nf,
                                                                    cd.superClass()));
                classTypes.add(PolyLLVMTypeUtils.polyLLVMDispatchVectorType(nf,
                                                                            cd.superClass()));

                classesAdded.add(cd.superClass().name());
            }
        }

    }

    /**
     * Return a list of global declarations for all classes defined.
     * @return
     */
    public List<LLVMGlobalDeclaration> globalDeclarations() {
        setupClassData();
        List<LLVMGlobalDeclaration> typeDecls = new ArrayList<>();
        for (LLVMTypeNode tn : classTypes) {
            typeDecls.add(nf.LLVMTypeDeclaration(tn));
        }
        typeDecls.addAll(globalSizes);
        return typeDecls;
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
                nf.LLVMVariable(Position.compilerGenerated(),
                                varName(name),
                                varPtrType,
                                VarType.LOCAL);
        LLVMLoad load = nf.LLVMLoad(temp, tn, var);
        return new Pair<>(load, temp);
    }

    public List<LLVMInstruction> allocationInstructions() {
        List<LLVMInstruction> allocs = new ArrayList<>();
        for (Entry<String, LLVMTypeNode> e : allocations.entrySet()) {
            LLVMAlloca a =
                    nf.LLVMAlloca(Position.compilerGenerated(), e.getValue());
            LLVMPointerType allocResultType = nf.LLVMPointerType(e.getValue());
            allocs.add(a.result(nf.LLVMVariable(Position.compilerGenerated(),
                                                varName(e.getKey()),
                                                allocResultType,
                                                VarType.LOCAL)));
        }
        for (Entry<String, LLVMTypeNode> e : arguments.entrySet()) {
            LLVMAlloca a =
                    nf.LLVMAlloca(Position.compilerGenerated(), e.getValue());
            LLVMPointerType allocResultType = nf.LLVMPointerType(e.getValue());
            allocs.add(a.result(nf.LLVMVariable(Position.compilerGenerated(),
                                                varName(e.getKey()),
                                                allocResultType,
                                                VarType.LOCAL)));
            LLVMTypeNode valueType = null;
            LLVMTypeNode ptrType = nf.LLVMPointerType(allocResultType);
            allocs.add(nf.LLVMStore(e.getValue(),
                                    nf.LLVMVariable(Position.compilerGenerated(),
                                                    e.getKey(),
                                                    valueType,
                                                    VarType.LOCAL),
                                    nf.LLVMVariable(Position.compilerGenerated(),
                                                    varName(e.getKey()),
                                                    ptrType,
                                                    VarType.LOCAL)));
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
}
