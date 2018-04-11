package polyllvm.types;

import polyglot.ext.jl5.types.JL5MethodInstance;
import polyglot.ext.jl5.types.JL5Subst;
import polyglot.ext.jl7.types.JL7TypeSystem_c;
import polyglot.frontend.Source;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;

import static polyllvm.util.Constants.RUNTIME_ARRAY;
import static polyllvm.util.Constants.RUNTIME_HELPER;

public class PolyLLVMTypeSystem_c extends JL7TypeSystem_c implements PolyLLVMTypeSystem {

    @Override
    public SubstMethodInstance substMethodInstance(
            JL5MethodInstance postSubst, JL5MethodInstance preSubst, JL5Subst subst) {
        return new PolyLLVMSubstMethodInstance_c(postSubst, preSubst, subst);
    }

    @Override
    public ParsedClassType ArrayObject() {
        try {
            return (ParsedClassType) typeForName(RUNTIME_ARRAY);
        } catch (SemanticException | ClassCastException e) {
            throw new InternalCompilerError("Could not load array type");
        }
    }

    @Override
    public ParsedClassType RuntimeHelper() {
        try {
            return (ParsedClassType) typeForName(RUNTIME_HELPER);
        } catch (SemanticException e) {
            throw new InternalCompilerError("Could not load runtime helper class");
        }
    }

    @Override
    public PolyLLVMLocalInstance localInstance(Position pos, Flags flags, Type type, String name) {
        return localInstance(pos, flags, type, name, /*isTemp*/ false, /*isSSA*/ false);
    }

    @Override
    public PolyLLVMLocalInstance localInstance(
            Position pos, Flags flags, Type type, String name, boolean isTemp, boolean isSSA) {
        return new PolyLLVMLocalInstance_c(this, pos, flags, type, name, isTemp, isSSA);
    }

    @Override
    public ParsedClassType createClassType(LazyClassInitializer init, Source fromSource) {
        return new PolyLLVMParsedClassType_c(this, init, fromSource);
    }
}
