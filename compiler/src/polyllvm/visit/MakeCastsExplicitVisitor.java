package polyllvm.visit;

import org.bytedeco.javacpp.LLVM;
import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.types.*;
import polyglot.types.reflect.Method;
import polyglot.util.Position;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.NodeVisitor;
import polyllvm.util.JL5TypeUtils;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Turn all implicit casts and type promotions into explicit casts.
 * Examples:
 * {@code char c = 1} -> {@code char c = (char) 1}.
 * {@code 1 + 2l} -> {@code ((long) 1) + 2l}.
 */
public class MakeCastsExplicitVisitor extends AscriptionVisitor {

    private final JL5TypeUtils jl5Utils;

    public MakeCastsExplicitVisitor(Job job, TypeSystem ts, NodeFactory nf, JL5TypeUtils jl5Utils) {
        super(job, ts, nf);
        this.jl5Utils = jl5Utils;
    }

    @Override
    protected Node leaveCall(Node parent, Node old, Node n, NodeVisitor v) throws SemanticException {
        if (parent instanceof Cast) {
            // We already have a cast; no need to add another.
            return n;
        } else if (n instanceof ArrayInit && parent instanceof NewArray) {
            // Tries to cast ArrayInit to Cast, need to propagate type
            return ((ArrayInit) n).type(((NewArray) parent).type());
        } else if(n instanceof Field && parent instanceof FieldAssign){
            //Tries to cast Cast to Field if ascribe is called
            return n;
        } else if (n instanceof FieldAssign){
            FieldAssign fieldAssign = (FieldAssign) n;
            FieldInstance origFi = fieldAssign.left().fieldInstance();
            FieldInstance fi = (FieldInstance) jl5Utils.translateMemberInstance(origFi);
            if(!origFi.equals(fi)){
                Cast sourceCast = nf.Cast(fieldAssign.right().position(),
                        nf.CanonicalTypeNode(fieldAssign.right().position(), fi.type()),
                        fieldAssign.right());
                return nf.FieldAssign(n.position(), fieldAssign.left(), fieldAssign.operator(), sourceCast);
            }
            return n;
        } else {
            return super.leaveCall(parent, old, n, v);
        }
    }

    @Override
    public Expr ascribe(Expr e, Type toType) throws SemanticException {
        //First need to check cases where generics could need casts
        if (e instanceof New) {
            New n = (New) e;
            ConstructorInstance origCi = n.constructorInstance();
            e = n.arguments(argumentCasts(origCi, n.arguments()));
        } else if (e instanceof Call) {
            Call n = (Call) e;
            MethodInstance origMi = n.methodInstance();
            MethodInstance mi = (MethodInstance) jl5Utils.translateMemberInstance(origMi);
            e = (Expr) n.arguments(argumentCasts(origMi, n.arguments()));
            if(!mi.returnType().equals(origMi.returnType()) && !toType.isVoid()){
                return nf.Cast(n.position(), nf.CanonicalTypeNode(n.position(),toType), n).type(toType);
            }
        } else if (e instanceof Field){
            FieldInstance origFi = ((Field) e).fieldInstance();
            FieldInstance fi = (FieldInstance) jl5Utils.translateMemberInstance(origFi);
            if(!origFi.equals(fi) && !toType.isVoid()){
                return nf.Cast(e.position(), nf.CanonicalTypeNode(e.position(),toType), e).type(toType);
            }
        }


        // Avoid adding casts to void.
        if (e.type().typeEquals(toType) || toType.isVoid()) {
            return super.ascribe(e, toType);
        } else {
            NodeFactory nf = nodeFactory();
            Position pos = e.position();
            TypeNode typeNode = nf.CanonicalTypeNode(pos, toType);
            return nf.Cast(pos, typeNode, e).type(toType);
        }
    }

    private List<Expr> argumentCasts(ProcedureInstance origPi, List<Expr> arguments) {
        ProcedureInstance pi = (ProcedureInstance) jl5Utils.translateMemberInstance((MemberInstance) origPi);
        List<Expr> args = new ArrayList<>(arguments);
        if (!origPi.equals(pi)) {
            List<? extends Type> origFormals = origPi.formalTypes();
            List<? extends Type> formals = pi.formalTypes();
            for (int i = 0; i < origPi.formalTypes().size(); i++) {
                Type type = formals.get(i);
                if (!origFormals.get(i).equals(type)) {
                    Expr expr = args.get(i);
                    args.set(i, nf.Cast(expr.position(),
                            nf.CanonicalTypeNode(expr.position(), type),
                            expr).type(type));
                }
            }

        }
        return args;
    }
}
