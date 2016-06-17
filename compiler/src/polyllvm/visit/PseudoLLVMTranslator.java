package polyllvm.visit;

import java.util.LinkedHashMap;
import java.util.Map;

import polyglot.ast.ClassDecl;
import polyglot.ast.Node;
import polyglot.types.TypeSystem;
import polyglot.visit.NodeVisitor;
import polyllvm.ast.PolyLLVMLang;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.LLVMNode;

public class PseudoLLVMTranslator extends NodeVisitor {

    private TypeSystem ts;
    private PolyLLVMNodeFactory nf;

    private Map<Node, LLVMNode> translations;
    private ClassDecl currentClass;

    public PseudoLLVMTranslator(TypeSystem ts, PolyLLVMNodeFactory nf) {
        super(nf.lang());
        this.ts = ts;
        this.nf = nf;
        translations = new LinkedHashMap<>();
        currentClass = null;
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

    public PolyLLVMNodeFactory nodeFactory() {
        return nf;
    }

    public void addTranslation(Node n, LLVMNode lln) {
        translations.put(n, lln);
    }

    public LLVMNode getTranslation(Node n) {
        return translations.get(n);
    }

    /**
     * Set the current class to null
     */
    public void clearCurrentClass() {
        currentClass = null;
    }

    /**
     * Set {@code n} as the new current class, return the old one.
     */
    public ClassDecl setCurrentClass(ClassDecl n) {
        ClassDecl old = currentClass;
        currentClass = n;
        return old;
    }

    /**
     * Return the current class
     * @return
     */
    public ClassDecl getCurrentClass() {
        return currentClass;
    }
}
