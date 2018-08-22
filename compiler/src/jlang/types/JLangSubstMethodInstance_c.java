package jlang.types;

import polyglot.ext.jl5.types.JL5MethodInstance;
import polyglot.ext.jl5.types.JL5MethodInstance_c;
import polyglot.ext.jl5.types.JL5Subst;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.MethodInstance;
import polyglot.util.SerialVersionUID;

/**
 * See {@link SubstMethodInstance}.
 */
public class JLangSubstMethodInstance_c extends JL5MethodInstance_c
        implements SubstMethodInstance {
    private static final long serialVersionUID = SerialVersionUID.generate();

    final protected JL5MethodInstance base;
    final protected JL5Subst subst;

    public JLangSubstMethodInstance_c(JL5MethodInstance postSubst,
                                         JL5MethodInstance preSubst, JL5Subst subst) {
        super((JL5TypeSystem) postSubst.typeSystem(), postSubst.position(),
                postSubst.container(), postSubst.flags(),
                postSubst.returnType(), postSubst.name(),
                postSubst.formalTypes(), postSubst.throwTypes(),
                postSubst.typeParams());
        this.decl = (MethodInstance) postSubst.declaration();

        // Type parameters should have been eliminated by substitution.
        assert postSubst.typeParams().isEmpty();

        this.base = preSubst;
        this.subst = subst;
    }

    @Override
    public JL5MethodInstance base() {
        return base;
    }

    @Override
    public JL5Subst subst() {
        return subst;
    }

}
