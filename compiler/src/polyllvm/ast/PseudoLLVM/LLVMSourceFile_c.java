package polyllvm.ast.PseudoLLVM;

import java.util.ArrayList;
import java.util.List;

import polyglot.ast.Node;
import polyglot.frontend.Source;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

public class LLVMSourceFile_c extends LLVMNode_c implements LLVMSourceFile {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected List<LLVMFunction> funcs;
    protected List<LLVMFunctionDeclaration> funcdecls;
    protected List<LLVMGlobalDeclaration> globals;

    protected Source source;
    protected String name;

    public LLVMSourceFile_c(Position pos, String name, Source s,
            List<LLVMFunction> funcs, List<LLVMFunctionDeclaration> funcdecls,
            List<LLVMGlobalDeclaration> globals) {
        super(pos, null);
        source = s;
        this.name = name;
        this.funcs = funcs == null ? new ArrayList<LLVMFunction>() : funcs;
        this.funcdecls = funcdecls == null
                ? new ArrayList<LLVMFunctionDeclaration>() : funcdecls;
        this.globals = globals == null
                ? new ArrayList<LLVMGlobalDeclaration>() : globals;
    }

    @Override
    public LLVMSourceFile merge(LLVMSourceFile s) {
        if (s == null) return this;
        s = copyIfNeeded(s);

        if (s.fileName() == null) s = s.fileName(name);
        if (s.source() == null) s = s.source(source);
        for (LLVMFunction f : funcs) {
            s = s.appendFunction(f);
        }
        for (LLVMFunctionDeclaration fd : funcdecls) {
            s = s.appendFunctionDeclaration(fd);
        }
        for (LLVMGlobalDeclaration g : globals) {
            s = s.appendGlobal(g);
        }
        return s;
    }

    @Override
    public LLVMSourceFile fileName(String s) {
        return fileName(this, s);
    }

    @Override
    public LLVMSourceFile source(Source source) {
        return source(this, source);
    }

    @Override
    public LLVMSourceFile functions(List<LLVMFunction> functions) {
        return functions(this, functions);
    }

    @Override
    public LLVMSourceFile appendFunction(LLVMFunction f) {
        List<LLVMFunction> l = new ArrayList<>(funcs.size() + 1);
        l.addAll(funcs);
        l.add(f);
        return functions(this, l);
    }

    @Override
    public LLVMSourceFile functionDeclarations(
            List<LLVMFunctionDeclaration> funcdecls) {
        return functionDeclarations(this, funcdecls);
    }

    @Override
    public LLVMSourceFile appendFunctionDeclaration(
            LLVMFunctionDeclaration fd) {
        List<LLVMFunctionDeclaration> l = new ArrayList<>(funcdecls.size() + 1);
        l.addAll(funcdecls);
        l.add(fd);
        return functionDeclarations(this, l);
    }

    @Override
    public LLVMSourceFile globals(List<LLVMGlobalDeclaration> gs) {
        return globals(this, gs);
    }

    @Override
    public LLVMSourceFile appendGlobal(LLVMGlobalDeclaration g) {
        List<LLVMGlobalDeclaration> l = new ArrayList<>(globals.size() + 1);
        l.addAll(globals);
        l.add(g);
        return globals(this, l);
    }

    protected <N extends LLVMSourceFile_c> N functions(N n,
            List<LLVMFunction> fs) {
        if (n.funcs == fs) return n;
        n = copyIfNeeded(n);
        n.funcs = fs;
        return n;
    }

    protected <N extends LLVMSourceFile_c> N functionDeclarations(N n,
            List<LLVMFunctionDeclaration> fds) {
        if (n.funcdecls == fds) return n;
        n = copyIfNeeded(n);
        n.funcdecls = fds;
        return n;
    }

    protected <N extends LLVMSourceFile_c> N globals(N n,
            List<LLVMGlobalDeclaration> gs) {
        if (n.globals == gs) return n;
        n = copyIfNeeded(n);
        n.globals = gs;
        return n;
    }

    protected <N extends LLVMSourceFile_c> N fileName(N n, String name) {
        if (n.name == name) return n;
        n = copyIfNeeded(n);
        n.name = name;
        return n;
    }

    protected <N extends LLVMSourceFile_c> N source(N n, Source s) {
        if (n.source == s) return n;
        n = copyIfNeeded(n);
        n.source = s;
        return n;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        for (LLVMGlobalDeclaration g : globals) {
            print(g, w, pp);
            w.write("\n");
        }
        for (LLVMFunctionDeclaration fd : funcdecls) {
            print(fd, w, pp);
            w.write("\n");
        }
        for (LLVMFunction f : funcs) {
            print(f, w, pp);
            w.write("\n\n");
        }
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        List<LLVMFunction> fs = visitList(funcs, v);
        List<LLVMFunctionDeclaration> fds = visitList(funcdecls, v);
        List<LLVMGlobalDeclaration> gds = visitList(globals, v);
        return reconstruct(this, fs, fds, gds);
    }

    /** Reconstruct the LLVM SourceFile. */
    protected <N extends LLVMSourceFile_c> N reconstruct(N n,
            List<LLVMFunction> fs, List<LLVMFunctionDeclaration> fds,
            List<LLVMGlobalDeclaration> gds) {
        n = functions(n, fs);
        n = functionDeclarations(n, fds);
        n = globals(n, gds);
        return n;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (LLVMGlobalDeclaration g : globals) {
            s.append(g.toString());
            s.append("\n");
        }
        for (LLVMFunctionDeclaration fd : funcdecls) {
            s.append(fd.toString());
            s.append("\n");
        }
        for (LLVMFunction f : funcs) {
            s.append(f.toString());
            s.append("\n\n");
        }
        return s.toString();
    }

    @Override
    public Source source() {
        return source;
    }

    @Override
    public String fileName() {
        return name;
    }

}
