//Copyright (C) 2018 Cornell University

package jlang.types;

import polyglot.ext.jl5.types.reflect.JL5ClassFileLazyClassInitializer;
import polyglot.types.ClassType;
import polyglot.types.MethodInstance;
import polyglot.types.reflect.ClassFile;
import polyglot.types.reflect.Method;

public class JLangClassFileLazyClassInitializer extends JL5ClassFileLazyClassInitializer {

	JLangTypeSystem ts;

	public JLangClassFileLazyClassInitializer(ClassFile file, JLangTypeSystem ts) {
		super(file, ts);
		this.ts = ts;
	}

	@Override
	protected MethodInstance methodInstance(Method m, ClassType ct) {
		MethodInstance mi = super.methodInstance(m, ct);
		JLangParsedClassType_c pct = (JLangParsedClassType_c) ct;
		if (pct.isNewMethod(mi)) {
			return mi;
		} else {
			return null;
		}
	}
}
