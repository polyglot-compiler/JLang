package polyllvm.extension;

import polyglot.ast.FloatLit;
import polyglot.ast.IntLit;
import polyglot.ast.MethodDecl;
import polyglot.ast.Node;
import polyglot.types.MethodInstance;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.visit.AddVoidReturnVisitor;

public class PolyLLVMMethodDeclExt extends PolyLLVMProcedureDeclExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node addVoidReturn(AddVoidReturnVisitor v) {
        MethodDecl n = (MethodDecl) node();
        MethodInstance mi = n.methodInstance();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        if (mi.returnType().isVoid() && n.body() != null) {
            return n.body(n.body()
                           .append(nf.Return(Position.compilerGenerated())));
        }
        else if (mi.returnType().isLongOrLess() && n.body() != null) {
            return n.body(n.body()
                           .append(nf.Return(Position.compilerGenerated(),
                                             nf.IntLit(Position.compilerGenerated(),
                                                       IntLit.INT,
                                                       0)
                                               .type(mi.returnType()))));

        }
        else if (mi.returnType().isBoolean() && n.body() != null) {
            return n.body(n.body()
                           .append(nf.Return(Position.compilerGenerated(),
                                             nf.BooleanLit(Position.compilerGenerated(),
                                                           false)
                                               .type(mi.returnType()))));
        }
        else if ((mi.returnType().isDouble() || mi.returnType().isFloat())
                && n.body() != null) {
            return n.body(n.body()
                           .append(nf.Return(Position.compilerGenerated(),
                                             nf.FloatLit(Position.compilerGenerated(),
                                                         FloatLit.DOUBLE,
                                                         0)
                                               .type(mi.returnType()))));
        }
        else if ((mi.returnType().isClass() || mi.returnType().isArray())
                && n.body() != null) {
            return n.body(n.body()
                           .append(nf.Return(Position.compilerGenerated(),
                                             nf.NullLit(Position.compilerGenerated())
                                               .type(mi.returnType()))));
        }
        return super.addVoidReturn(v);
    }
}
