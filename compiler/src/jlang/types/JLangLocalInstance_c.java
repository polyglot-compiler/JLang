//Copyright (C) 2017 Cornell University

package jlang.types;

import polyglot.ext.jl5.types.JL5LocalInstance_c;
import polyglot.types.Flags;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.Position;

public class JLangLocalInstance_c
        extends JL5LocalInstance_c
        implements JLangLocalInstance {
    private final boolean isTemp, isSSA;

    public JLangLocalInstance_c(
            TypeSystem ts, Position pos, Flags flags, Type type, String name,
            boolean isTemp, boolean isSSA) {
        super(ts, pos, flags, type, name);
        this.isTemp = isTemp;
        this.isSSA = isSSA;
    }

    @Override
    public boolean isTemp() {
        return isTemp;
    }

    @Override
    public boolean isSSA() {
        return isSSA;
    }
}
