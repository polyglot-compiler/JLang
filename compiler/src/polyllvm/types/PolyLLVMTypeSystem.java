package polyllvm.types;

import polyglot.ext.jl5.types.JL5MethodInstance;
import polyglot.ext.jl5.types.JL5Subst;
import polyglot.ext.jl7.types.JL7TypeSystem;
import polyglot.types.ClassType;
import polyglot.types.Flags;
import polyglot.types.ParsedClassType;
import polyglot.types.Type;
import polyglot.util.Position;

public interface PolyLLVMTypeSystem extends JL7TypeSystem {

    ParsedClassType ArrayObject();

    ClassType RuntimeHelper();

    @Override
    PolyLLVMLocalInstance localInstance(Position pos, Flags flags, Type type, String name);

    /**
     * Creates a local instance with the option of making it a temp variable, which
     * is hidden from the user when debugging.
     */
    PolyLLVMLocalInstance localInstance(
            Position pos, Flags flags, Type type, String name, boolean isTemp, boolean isSSA);

    SubstMethodInstance substMethodInstance(JL5MethodInstance postSubst,
            JL5MethodInstance preSubst, JL5Subst subst);
}
