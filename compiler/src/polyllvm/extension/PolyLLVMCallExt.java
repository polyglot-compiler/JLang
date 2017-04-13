package polyllvm.extension;

import polyglot.ast.Call;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.Special;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.util.SerialVersionUID;
import polyllvm.visit.LLVMTranslator;

import java.util.stream.Stream;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMCallExt extends PolyLLVMProcedureCallExt {
	private static final long serialVersionUID = SerialVersionUID.generate();

	@Override public Node leaveTranslateLLVM(LLVMTranslator v) {
		Call n = (Call) node();
		MethodInstance mi = n.methodInstance();

		if (n.target() instanceof Special
				&& ((Special) n.target()).kind().equals(Special.SUPER)) {
			translateSuperCall(v);
		} else if (n.target() instanceof Expr && v.isInterface(n.target().type().toReference())) {
			translateInterfaceMethodCall(v);
		} else if (mi.flags().isStatic()) {
			translateStaticCall(v);
		} else if (mi.flags().isPrivate() || mi.flags().isFinal()) {
			translateFinalMethodCall(v);
		} else {
			translateMethodCall(v);
		}

		return super.leaveTranslateLLVM(v);
	}

	private void translateStaticCall(LLVMTranslator v) {
		Call n = (Call) node();

		String mangledFuncName = v.mangler
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

		LLVMTypeRef toType = v.utils.ptrTypeRef(
				v.utils.methodType(v.getCurrentClass().type(),
					n.methodInstance().returnType(),
					n.methodInstance().formalTypes()));

		LLVMTypeRef superMethodType = v.utils.methodType(
				superMethod.container(), superMethod.returnType(),
				superMethod.formalTypes());

		LLVMValueRef superFunc = v.utils.getFunction(v.mod,
				v.mangler.mangleProcedureName(superMethod),
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

		ReferenceType referenceType = (ReferenceType) v.jl5Utils.translateType(n.target().type());
		MethodInstance methodInstance = (MethodInstance) v.jl5Utils.translateMemberInstance(n.methodInstance());
		v.utils.typeRef(referenceType); // Ensure the target type body and dv type body (with generics stripped) are set before GEP

		LLVMValueRef thisTranslation = v.getTranslation(n.target());

		LLVMValueRef dvDoublePtr = v.utils.buildStructGEP(thisTranslation, 0, 0);

		LLVMValueRef dvPtr = LLVMBuildLoad(v.builder, dvDoublePtr, "dv_ptr");

		int methodIndex = v.getMethodIndex(referenceType, methodInstance);

		LLVMTypeRef res = LLVMGetTypeByName(v.mod,
				v.mangler.dispatchVectorTypeName(referenceType));

		LLVMValueRef funcDoublePtr = v.utils.buildStructGEP(dvPtr, 0, methodIndex);

		LLVMValueRef methodPtr = LLVMBuildLoad(v.builder, funcDoublePtr,
				"load_method_ptr");

		LLVMValueRef[] args = Stream
				.concat(Stream.of(thisTranslation),
						n.arguments().stream()
								.map(v::getTranslation))
				.toArray(LLVMValueRef[]::new);

		if (methodInstance.returnType().isVoid()) {
			v.addTranslation(n, v.utils.buildProcedureCall(methodPtr, args));
		} else {
			v.addTranslation(n, v.utils.buildMethodCall(methodPtr, args));

		}
	}

	private void translateFinalMethodCall(LLVMTranslator v) {
		Call n = (Call) node();

		String mangledFuncName = v.mangler
				.mangleProcedureName(n.methodInstance());

		ReferenceType referenceType = (ReferenceType) n.target().type();
		LLVMTypeRef tn = v.utils.methodType(referenceType,
				n.methodInstance().returnType(),
				n.methodInstance().formalTypes());

		LLVMValueRef thisTranslation = v.getTranslation(n.target());
		LLVMValueRef[] args = Stream
				.concat(Stream.of(thisTranslation),
						n.arguments().stream()
								.map(v::getTranslation))
				.toArray(LLVMValueRef[]::new);

		LLVMValueRef func = v.utils.getFunction(v.mod, mangledFuncName, tn);

		v.debugInfo.emitLocation(n);
		if (n.methodInstance().returnType().isVoid()) {
			v.addTranslation(n, v.utils.buildProcedureCall(func, args));
		} else {
			v.addTranslation(n, v.utils.buildMethodCall(func, args));
		}

	}


	private void translateInterfaceMethodCall(LLVMTranslator v) {
		Call n = (Call) node();
		ReferenceType rt = (ReferenceType) n.target().type();
		MethodInstance mi = (MethodInstance) v.jl5Utils.translateMemberInstance(n.methodInstance());
		LLVMValueRef thisTranslation = v.getTranslation(n.target());

		LLVMTypeRef methodType = v.utils.ptrTypeRef(
				v.utils.methodType(rt, mi.returnType(), mi.formalTypes()));

		int methodIndex = v.getMethodIndex(rt, mi);

		LLVMValueRef interfaceStringPtr = v.utils.getGlobal(v.mod,
				v.mangler.interfaceStringVariable(rt),
				LLVMArrayType(LLVMInt8TypeInContext(v.context),
						rt.toString().length() + 1));
		LLVMValueRef interfaceStringBytePtr
				= v.utils.buildStructGEP(interfaceStringPtr, 0, 0);

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
