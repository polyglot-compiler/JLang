package polyllvm.util;

import polyglot.ext.jl5.types.*;
import polyglot.ext.jl5.types.inference.LubType;
import polyglot.ext.param.types.Subst;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;

/**
 * Created by Daniel on 3/17/17.
 */
public class JL5TypeUtils {

    final private TypeSystem ts;

    public JL5TypeUtils(TypeSystem ts){
        this.ts = ts;
    }

     /*
     * Helper Methods for dealing with Generic types
     */

    @SuppressWarnings("unchecked")
    public <T extends Type> T translateType(T t) {
        assert t!=null : "type is null";
        t = (T) ((JL5TypeSystem) ts).erasureType(t);
        if (t instanceof LubType) {
            t = (T) ((LubType) t).calculateLub();
            t = (T) ((JL5TypeSystem) ts).erasureType(t);
        }

        if (t instanceof JL5SubstClassType) {
            // For C<T1,...,Tn>, just print C.
            JL5SubstClassType jct = (JL5SubstClassType) t;
            System.out.println("OUTER: " + jct.outer());
            return (T) translateType(jct.base());
        }
        else if (t instanceof ArrayType) {
            ArrayType at = (ArrayType) t;
            return (T) ts.arrayOf(translateType(at.base()));
        } else if (t instanceof ParsedClassType){
            ParsedClassType parsedClassType = (ParsedClassType) t;
            if (parsedClassType.outer() != null) {
                parsedClassType.outer(translateType(parsedClassType.outer()));
                return (T) parsedClassType;
            }
        }
        return t;

    }

    public MemberInstance translateMemberInstance(MemberInstance mi){
        ReferenceType container = mi.container();
        if(container instanceof JL5SubstClassType){
            JL5SubstClassType substClass = (JL5SubstClassType) container;
            Subst<TypeVariable, ReferenceType> subst = substClass.subst();
            JL5ParsedClassType base = substClass.base();

            for (MemberInstance member : base.members()){
                MemberInstance origMember = member;
                if (member instanceof MethodInstance) {
                    member = subst.substMethod((MethodInstance) member);
                } else if (member instanceof ConstructorInstance) {
                    member = subst.substConstructor((ConstructorInstance) member);
                } else if (member instanceof FieldInstance) {
                    member = subst.substField((FieldInstance) member);
                } else if (member instanceof ClassType) {
                    member = (ClassType) subst.substType((ClassType)member);
                } else if (!(member instanceof InitializerInstance)) {
                    throw new InternalCompilerError("Cannot handle Member Instance: " + mi + " (" + mi.getClass() + ")");
                }

                if(member.equals(mi)){
                    return translateMemberInstance(origMember);
                }
            }
        } else if(container instanceof JL5ParsedClassType){
            JL5TypeSystem ts = (JL5TypeSystem) this.ts;
            JL5Subst subst = ts.erasureSubst((JL5ParsedClassType) container);
            if(subst != null) {
                if (mi instanceof MethodInstance) {
                    return subst.substMethod((MethodInstance) mi);
                } else if (mi instanceof ConstructorInstance) {
                    ConstructorInstance constructorInstance = subst.substConstructor((ConstructorInstance) mi);
                    return constructorInstance;
                } else if (mi instanceof FieldInstance) {
                    return subst.substField((FieldInstance) mi);
                } else if (mi instanceof ClassType) {
                    return translateType((ClassType) mi);
                } else if (mi instanceof InitializerInstance) {
                    return mi;
                } else {
                    throw new InternalCompilerError("Cannot translate Member Instance: " + mi + " (" + mi.getClass() + ")");
                }
            }
        }
        return mi;
    }

}
