package polyllvm.types;

import polyglot.ext.jl5.types.JL5MethodInstance;
import polyglot.ext.jl5.types.JL5Subst;
import polyglot.ext.jl7.types.JL7TypeSystem;
import polyglot.types.ParsedClassType;

public interface PolyLLVMTypeSystem extends JL7TypeSystem {

    ParsedClassType Array();

    SubstMethodInstance substMethodInstance(JL5MethodInstance postSubst,
            JL5MethodInstance preSubst, JL5Subst subst);

}
