package polyllvm.types;

import polyglot.ext.jl5.types.reflect.JL5ClassFileLazyClassInitializer;
import polyglot.types.ClassType;
import polyglot.types.MethodInstance;
import polyglot.types.reflect.ClassFile;
import polyglot.types.reflect.Method;

public class PolyLLVMClassFileLazyClassInitializer extends JL5ClassFileLazyClassInitializer {

	PolyLLVMTypeSystem ts;

	public PolyLLVMClassFileLazyClassInitializer(ClassFile file, PolyLLVMTypeSystem ts) {
		super(file, ts);
		this.ts = ts;
	}

	@Override
	protected MethodInstance methodInstance(Method m, ClassType ct) {
		MethodInstance mi = super.methodInstance(m, ct);
		PolyLLVMParsedClassType_c pct = (PolyLLVMParsedClassType_c) ct;
		if (pct.isNewMethod(mi)) {
			return mi;
		} else {
			return null;
		}
	}
}
