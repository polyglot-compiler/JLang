//Copyright (C) 2017 Cornell University

package jlang.types;

import jlang.visit.NameAnonClasses;
import polyglot.ext.jl5.types.JL5MethodInstance;
import polyglot.ext.jl5.types.JL5ParsedClassType_c;
import polyglot.frontend.Source;
import polyglot.types.ClassType;
import polyglot.types.LazyClassInitializer;
import polyglot.types.MethodInstance;
import polyglot.types.TypeSystem;

public class JLangParsedClassType_c extends JL5ParsedClassType_c {

	JLangTypeSystem ts;
    public JLangParsedClassType_c(JLangTypeSystem ts, LazyClassInitializer init, Source fromSource) {
        super(ts, init, fromSource);
        this.ts = ts;
    }

    @Override
    public void name(String name) {
        // Override in order to avoid error when setting name for anonymous classes.
        this.name = name;
    }

    /**
     * Similar to {@link ClassType#fullName()}, but works for anonymous classes too,
     * with the help of {@link NameAnonClasses}.
     */
    @Override
    public String fullName() {
        String name = name();
        if (outer() != null) {
            return outer().fullName() + "." + name;
        } else if (package_() != null) {
            return package_().fullName() + "." + name;
        } else {
            return name;
        }
    }

    public boolean isNewMethod(MethodInstance mi) {
		for (MethodInstance ct_mi : this.methods) {
			if (ts.areOverrideEquivalent((JL5MethodInstance) mi, (JL5MethodInstance) ct_mi)) {
				if (ts.isSubtypeErased(mi.returnType(),  ct_mi.returnType())) {
					this.methods.remove(ct_mi);
					return true;
				} else {
					return false;
				}
			}
		}
		return true;
    }

    @Override
    public void addMethod(MethodInstance mi) {
    	if (mi != null) {
    		super.addMethod(mi);
    	}
    }
}
