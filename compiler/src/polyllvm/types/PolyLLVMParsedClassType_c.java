package polyllvm.types;

import polyglot.ext.jl5.types.JL5ParsedClassType_c;
import polyglot.frontend.Source;
import polyglot.types.LazyClassInitializer;
import polyglot.types.TypeSystem;

public class PolyLLVMParsedClassType_c extends JL5ParsedClassType_c {

    public PolyLLVMParsedClassType_c(TypeSystem ts, LazyClassInitializer init, Source fromSource) {
        super(ts, init, fromSource);
    }

    @Override
    public void name(String name) {
        // Override in order to avoid error when setting name for anonymous classes.
        this.name = name;
    }
}