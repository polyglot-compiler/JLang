package polyllvm.extension;

import static org.bytedeco.javacpp.LLVM.*;

import java.util.List;

import org.bytedeco.javacpp.LLVM.LLVMTypeRef;
import org.bytedeco.javacpp.LLVM.LLVMValueRef;

import polyglot.ast.ClassDecl;
import polyglot.ast.Node;
import polyglot.types.ParsedClassType;
import polyglot.types.ReferenceType;
import polyglot.types.Type;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

public class PolyLLVMClassDeclExt extends PolyLLVMExt {
	private static final long serialVersionUID = SerialVersionUID.generate();

	@Override
	public LLVMTranslator enterTranslateLLVM(LLVMTranslator v) {
		v.enterClass((ClassDecl) node());
		return super.enterTranslateLLVM(v);
	}

	@Override
	public Node leaveTranslateLLVM(LLVMTranslator v) {
		ClassDecl n = (ClassDecl) node();

		// External class object declarations.
		Type superType = n.type().superType();
		while (superType != null) {
			v.classObjs.classIdDeclRef(superType.toReference(),
					/* extern */ true);
			superType = superType.toReference().superType();
		}

		n.interfaces().stream().map(tn -> tn.type().toClass())
				.map(rt -> v.classObjs.classIdDeclRef(rt, /* extern */ true));

		// Class object for this class.
		v.classObjs.classIdDeclRef(n.type(), /* extern */ false);
		v.classObjs.classObjRef(n.type());

		List<? extends ReferenceType> interfaces = v.allInterfaces(n.type());

		if (!n.flags().isAbstract()) {
			// Set up the class dispatch vector
			LLVMValueRef dvGlobal = v.utils.getDvGlobal(n.type());
			LLVMValueRef[] dvMethods = v.utils.dvMethods(n.type());
			LLVMValueRef initStruct = v.utils.buildConstStruct(dvMethods);
			LLVMSetInitializer(dvGlobal, initStruct);
		}

		if (!n.type().flags().isAbstract() && !interfaces.isEmpty()) {
			int numOfIntfs = interfaces.size();
			LLVMValueRef[] intf_id_hashes = new LLVMValueRef[numOfIntfs];
			LLVMValueRef[] intf_ids = new LLVMValueRef[numOfIntfs];
			LLVMValueRef[] intf_tables = new LLVMValueRef[numOfIntfs];

			// Set up the interface dispatch vectors
			for (int i = 0; i < numOfIntfs; i++) {
				ReferenceType it = interfaces.get(i);

				int hash = v.utils.intfHash(it);
				intf_id_hashes[i] = LLVMConstInt(
						LLVMInt32TypeInContext(v.context), hash,
						/* sign-extend */ 0);

				LLVMValueRef intf_id_global = v.classObjs.classIdVarRef(it);
				intf_ids[i] = intf_id_global;

				LLVMValueRef idv_global = v.utils.getItGlobal(it, n.type());
				LLVMValueRef[] idv_methods = v.utils.itMethods(it, n.type());
				LLVMValueRef idv_value = v.utils.buildConstStruct(idv_methods);
				LLVMSetInitializer(idv_global, idv_value);
				intf_tables[i] = LLVMBuildBitCast(v.builder, idv_global,
						v.utils.llvmBytePtr(), "cast_for_idv");
			}

			// Set up the hash table that points to the interface dispatch
			// vectors
			LLVMValueRef cdv_global = v.utils.getDvGlobal(n.type());
			LLVMValueRef idv_arr_global = v.utils.getIdvArrGlobal(n.type(),
					numOfIntfs);
			LLVMValueRef idv_id_arr_global = v.utils.getIdvIdArrGlobal(n.type(),
					numOfIntfs);
			LLVMValueRef idv_id_hash_arr_global = v.utils
					.getIdvIdHashArrGlobal(n.type(), numOfIntfs);

			LLVMSetInitializer(idv_arr_global, v.utils
					.buildConstArray(v.utils.llvmBytePtr(), intf_tables));
			LLVMSetInitializer(idv_id_arr_global,
					v.utils.buildConstArray(v.utils.llvmBytePtr(), intf_ids));
			LLVMSetInitializer(idv_id_hash_arr_global, v.utils.buildConstArray(
					LLVMInt32TypeInContext(v.context), intf_id_hashes));

			LLVMTypeRef create_idv_ht_func_type = v.utils.functionType(
					LLVMVoidType(), // void return type
					v.utils.llvmBytePtr(), // dv*
					LLVMInt32TypeInContext(v.context), // int
					LLVMInt32TypeInContext(v.context), // int
					v.utils.ptrTypeRef(LLVMInt32TypeInContext(v.context)), // int[]
					v.utils.llvmBytePtr(), // void*[]
					v.utils.llvmBytePtr() // it*[]
			);
			LLVMValueRef create_idv_ht_func = v.utils.getFunction(v.mod,
					"__createInterfaceTables", create_idv_ht_func_type);
			int capacity = v.utils.idvCapacity(numOfIntfs);
			v.utils.buildCtor(node, () -> {
				v.utils.buildProcedureCall(create_idv_ht_func,
						v.utils.llvmConstBitCastToBytePtr(cdv_global),
						LLVMConstInt(LLVMInt32TypeInContext(v.context),
								capacity, /* sign-extend */ 0),
						LLVMConstInt(LLVMInt32TypeInContext(v.context),
								numOfIntfs, /* sign-extend */ 0),
						LLVMConstBitCast(idv_id_hash_arr_global,
								v.utils.ptrTypeRef(
										LLVMInt32TypeInContext(v.context))),
						v.utils.llvmConstBitCastToBytePtr(idv_id_arr_global),
						v.utils.llvmConstBitCastToBytePtr(idv_arr_global));
				return null;
			});
		}

		v.leaveClass();
		return super.leaveTranslateLLVM(v);
	}

	@Override
	public Node overrideTranslateLLVM(LLVMTranslator v) {
		ClassDecl n = (ClassDecl) node();
		ParsedClassType ty = n.type();
		if (v.isInterface(ty)) {
			// Interfaces need only declare a class id.
			v.classObjs.classIdDeclRef(ty, /* extern */ false);
			return n;
		}
		return super.overrideTranslateLLVM(v);
	}
}
