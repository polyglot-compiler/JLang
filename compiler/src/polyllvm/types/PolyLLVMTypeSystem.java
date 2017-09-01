package polyllvm.types;

import polyglot.ext.jl5.types.JL5ConstructorInstance;
import polyglot.ext.jl5.types.JL5MethodInstance;
import polyglot.ext.jl5.types.JL5Subst;
import polyglot.ext.jl7.types.JL7TypeSystem;

public interface PolyLLVMTypeSystem extends JL7TypeSystem {

	SubstMethodInstance substMethodInstance(JL5MethodInstance postSubst,
			JL5MethodInstance preSubst, JL5Subst subst);

}
