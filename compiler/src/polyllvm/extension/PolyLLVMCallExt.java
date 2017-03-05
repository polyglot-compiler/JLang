package polyllvm.extension;

import polyglot.ast.Call;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.Special;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.util.SerialVersionUID;
import polyllvm.util.PolyLLVMMangler;
import polyllvm.visit.LLVMTranslator;

import java.util.stream.Stream;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMCallExt extends PolyLLVMProcedureCallExt {
	private static final long serialVersionUID = SerialVersionUID.generate();

	@Override public Node translatePseudoLLVM(LLVMTranslator v) {
		Call n = (Call) node();

		if (n.target() instanceof Special
				&& ((Special) n.target()).kind().equals(Special.SUPER)) {
			translateSuperCall(v);
		} else if (n.target() instanceof Expr
				&& v.isInterfaceCall(n.methodInstance())) {
			translateInterfaceMethodCall(v);
		} else if (n.methodInstance().flags().isStatic()) {
			translateStaticCall(v);
		} else {
			translateMethodCall(v);
		}

		return super.translatePseudoLLVM(v);
	}

	private void translateStaticCall(LLVMTranslator v) {
		Call n = (Call) node();

		String mangledFuncName = PolyLLVMMangler
				.mangleProcedureName(n.methodInstance());

		LLVMTypeRef tn = v.utils.functionType(n.methodInstance().returnType(),
				n.methodInstance().formalTypes());

		LLVMValueRef[] args = n.arguments().stream().map(v::getTranslation)
				.toArray(LLVMValueRef[]::new);

		LLVMValueRef func = v.utils.getFunction(v.mod, mangledFuncName, tn);

		v.debugInfo.emitLocation(n);
		if (n.methodInstance().returnType().isVoid()) {
			v.addTranslation(n, v.utils.buildProcedureCall(func, args));
		} else {
			v.addTranslation(n, v.utils.buildMethodCall(func, args));
		}

	}

	private void translateSuperCall(LLVMTranslator v) {
		Call n = (Call) node();
		MethodInstance superMethod = n.methodInstance().overrides().get(0);

		LLVMTypeRef toType = v.utils.methodType(v.getCurrentClass().type(),
				n.methodInstance().returnType(),
				n.methodInstance().formalTypes());

		LLVMTypeRef superMethodType = v.utils.methodType(
				superMethod.container(), superMethod.returnType(),
				superMethod.formalTypes());

		LLVMValueRef superFunc = v.utils.getFunction(v.mod,
				PolyLLVMMangler.mangleProcedureName(superMethod),
				superMethodType);

		LLVMValueRef superBitCast = LLVMBuildBitCast(v.builder, superFunc,
				toType, "bitcast_super");

		LLVMValueRef thisArg = LLVMGetParam(v.currFn(), 0);

		LLVMValueRef[] args = Stream
				.concat(Stream.of(thisArg),
						n.arguments().stream().map(v::getTranslation))
				.toArray(LLVMValueRef[]::new);

		if (n.methodInstance().returnType().isVoid()) {
			v.addTranslation(n, v.utils.buildProcedureCall(superBitCast, args));
		} else {
			v.addTranslation(n, v.utils.buildMethodCall(superBitCast, args));
		}

	}

	private void translateMethodCall(LLVMTranslator v) {
		Call n = (Call) node();

		ReferenceType referenceType = (ReferenceType) n.target().type();
		LLVMValueRef thisTranslation = v.getTranslation(n.target());

		LLVMValueRef dvDoublePtr = v.utils.buildGEP(v.builder, thisTranslation,
				LLVMConstInt(LLVMInt32TypeInContext(v.context), 0,
						/* sign-extend */ 0),
				LLVMConstInt(LLVMInt32TypeInContext(v.context), 0,
						/* sign-extend */ 0));

		LLVMValueRef dvPtr = LLVMBuildLoad(v.builder, dvDoublePtr, "dv_ptr");

		int methodIndex = v.getMethodIndex(referenceType, n.methodInstance());

		LLVMTypeRef res = LLVMGetTypeByName(v.mod,
				PolyLLVMMangler.dispatchVectorTypeName(referenceType));
		LLVMTypeRef methodType = LLVMStructGetTypeAtIndex(res, methodIndex);
		int i = LLVMGetPointerAddressSpace(LLVMTypeOf(dvPtr));
		LLVMValueRef funcDoublePtr = v.utils.buildGEP(v.builder, dvPtr,
				LLVMConstInt(LLVMInt32TypeInContext(v.context), 0,
						/* sign-extend */ 0),
				LLVMConstInt(LLVMInt32TypeInContext(v.context), methodIndex,
						/* sign-extend */ 0));

		LLVMValueRef methodPtr = LLVMBuildLoad(v.builder, funcDoublePtr,
				"load_method_ptr");

		LLVMValueRef[] args = Stream
				.concat(Stream.of(thisTranslation),
						n.arguments().stream()
								.map(arg -> v.getTranslation(arg)))
				.toArray(LLVMValueRef[]::new);

		if (n.methodInstance().returnType().isVoid()) {
			v.addTranslation(n, v.utils.buildProcedureCall(methodPtr, args));
		} else {
			v.addTranslation(n, v.utils.buildMethodCall(methodPtr, args));

		}
	}

	private void translateInterfaceMethodCall(LLVMTranslator v) {
		Call n = (Call) node();
		ReferenceType rt = (ReferenceType) n.target().type();
		MethodInstance mi = n.methodInstance();
		LLVMValueRef thisTranslation = v.getTranslation(n.target());

		LLVMTypeRef methodType = v.utils.ptrTypeRef(
				v.utils.methodType(rt, mi.returnType(), mi.formalTypes()));

		int methodIndex = v.getMethodIndex(rt, mi);

		LLVMValueRef interfaceStringPtr = v.utils.getGlobal(v.mod,
				PolyLLVMMangler.interfaceStringVariable(rt),
				LLVMArrayType(LLVMInt8TypeInContext(v.context),
						rt.toString().length() + 1));
		LLVMValueRef interfaceStringBytePtr = v.utils.buildGEP(v.builder,
				interfaceStringPtr,
				LLVMConstInt(LLVMInt32TypeInContext(v.context), 0, 0),
				LLVMConstInt(LLVMInt32TypeInContext(v.context), 0, 0));

		LLVMValueRef obj_bitcast = LLVMBuildBitCast(v.builder, thisTranslation,
				v.utils.ptrTypeRef(LLVMInt8TypeInContext(v.context)),
				"cast_for_interface_call");

		LLVMTypeRef getInterfaceMethodType = v.utils.functionType(
				v.utils.ptrTypeRef(LLVMInt8TypeInContext(v.context)), // void*
																		// return
																		// type
				v.utils.ptrTypeRef(LLVMInt8TypeInContext(v.context)), // jobject*
				v.utils.ptrTypeRef(LLVMInt8TypeInContext(v.context)), // char*
				LLVMInt32TypeInContext(v.context) // int
		);
		LLVMValueRef getInterfaceMethod = v.utils.getFunction(v.mod,
				"__getInterfaceMethod", getInterfaceMethodType);
		LLVMValueRef interfaceMethod = v.utils.buildMethodCall(
				getInterfaceMethod, obj_bitcast, interfaceStringBytePtr,
				LLVMConstInt(LLVMInt32TypeInContext(v.context), methodIndex,
						/* sign-extend */ 0));

		LLVMValueRef cast = LLVMBuildBitCast(v.builder, interfaceMethod,
				methodType, "cast_interface_method");

		LLVMValueRef[] args = Stream
				.concat(Stream.of(thisTranslation),
						n.arguments().stream().map(v::getTranslation))
				.toArray(LLVMValueRef[]::new);

		if (n.methodInstance().returnType().isVoid()) {
			v.addTranslation(n, v.utils.buildProcedureCall(cast, args));
		} else {
			v.addTranslation(n, v.utils.buildMethodCall(cast, args));
		}
	}

}
