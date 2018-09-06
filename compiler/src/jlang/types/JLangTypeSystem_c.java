//Copyright (C) 2018 Cornell University

package jlang.types;

import static jlang.util.Constants.RUNTIME_ARRAY;
import static jlang.util.Constants.RUNTIME_HELPER;

import polyglot.ext.jl5.types.JL5MethodInstance;
import polyglot.ext.jl5.types.JL5Subst;
import polyglot.ext.jl7.types.JL7TypeSystem_c;
import polyglot.frontend.Source;
import polyglot.types.*;
import polyglot.types.reflect.ClassFile;
import polyglot.types.reflect.ClassFileLazyClassInitializer;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;

public class JLangTypeSystem_c extends JL7TypeSystem_c implements JLangTypeSystem {



	@Override
	public ClassFileLazyClassInitializer classFileLazyClassInitializer(ClassFile clazz) {
		return new JLangClassFileLazyClassInitializer(clazz, this);
	}

    @Override
    public SubstMethodInstance substMethodInstance(
            JL5MethodInstance postSubst, JL5MethodInstance preSubst, JL5Subst subst) {
        return new JLangSubstMethodInstance_c(postSubst, preSubst, subst);
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
    public JLangLocalInstance localInstance(Position pos, Flags flags, Type type, String name) {
        return localInstance(pos, flags, type, name, /*isTemp*/ false, /*isSSA*/ false);
    }

    @Override
    public JLangLocalInstance localInstance(
            Position pos, Flags flags, Type type, String name, boolean isTemp, boolean isSSA) {
        return new JLangLocalInstance_c(this, pos, flags, type, name, isTemp, isSSA);
    }

    @Override
    public ParsedClassType createClassType(LazyClassInitializer init, Source fromSource) {
        return new JLangParsedClassType_c(this, init, fromSource);
    }

    @Override
    public boolean typeEqualsErased(Type a, Type b) {
        if (a.isClass() && b.isClass())
            return a.toClass().declaration().equals(b.toClass().declaration());
        return a.typeEquals(b);
    }

    @Override
    public boolean isSubtypeErased(Type a, Type b) {

        // Check bottom and top.
        if (a.isNull() || b.typeEquals(Object()))
            return true;

        // Check if equal.
        if (typeEqualsErased(a, b))
            return true;

        // Check class hierarchy.
        if (a.isReference()) {
            ReferenceType rt = a.toReference();

            // Check supertype.
            if (rt.superType() != null)
                if (isSubtypeErased(rt.superType(), b))
                    return true;

            // Check interfaces.
            for (ReferenceType it : rt.interfaces())
                if (isSubtypeErased(it, b))
                    return true;
        }

        return false;
    }
}
