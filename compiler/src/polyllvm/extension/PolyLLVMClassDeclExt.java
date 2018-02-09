package polyllvm.extension;

import org.bytedeco.javacpp.LLVM.*;
import polyglot.ast.ClassDecl;
import polyglot.ast.Node;
import polyglot.types.ClassType;
import polyglot.types.ParsedClassType;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;
import java.util.List;

import static org.bytedeco.javacpp.LLVM.*;

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
        assert !n.type().flags().isInterface(); // cannot be an interface

        // Initialize the type identity
        v.classObjs.toTypeIdentity(n.type(), /* extern */ false);
        v.classObjs.classObjRef(n.type());

        List<ClassType> interfaces = v.allInterfaces(n.type());

        if (!n.flags().isAbstract()) {
            // Initialize the CDV global
            LLVMValueRef cdvGlobal = v.utils.toCDVGlobal(n.type());
            LLVMValueRef[] cdvSlots = v.utils.toCDVSlots(n.type());
            LLVMTypeRef cdvType = v.utils.toCDVTy(n.type());
            LLVMValueRef init = v.utils.buildNamedConstStruct(cdvType, cdvSlots);
            LLVMSetInitializer(cdvGlobal, init);
        }

        if (!n.type().flags().isAbstract() && !interfaces.isEmpty()) {
            int numOfIntfs = interfaces.size();
            LLVMValueRef[] intf_id_hashes = new LLVMValueRef[numOfIntfs];
            LLVMValueRef[] intf_ids = new LLVMValueRef[numOfIntfs];
            LLVMValueRef[] intfTables = new LLVMValueRef[numOfIntfs];

            // Initialize the IDV globals
            for (int i = 0; i < numOfIntfs; ++i) {
                ClassType it = interfaces.get(i);

                int hash = v.utils.intfHash(it);
                intf_id_hashes[i] = LLVMConstInt(
                        LLVMInt32TypeInContext(v.context), hash,
                        /* sign-extend */ 0);

                LLVMValueRef intf_id_global = v.classObjs.toTypeIdentity(it);
                intf_ids[i] = intf_id_global;

                LLVMTypeRef idvType = v.utils.toIDVTy(it);
                LLVMValueRef idvGlobal = v.utils.toIDVGlobal(it, n.type());
                LLVMValueRef[] idvMethods = v.utils.toIDVSlots(it, n.type());
                LLVMValueRef init = v.utils.buildNamedConstStruct(idvType, idvMethods);
                LLVMSetInitializer(idvGlobal, init);
                intfTables[i] = LLVMBuildBitCast(
                        v.builder, idvGlobal, v.utils.llvmBytePtr(), "cast");
            }

            // Set up the hash table that points to the interface dispatch
            // vectors
            LLVMValueRef cdv_global = v.utils.toCDVGlobal(n.type());
            LLVMValueRef idv_arr_global = v.utils.toIDVArrGlobal(n.type(),
                    numOfIntfs);
            LLVMValueRef idv_id_arr_global = v.utils.toIDVIdArrGlobal(n.type(),
                    numOfIntfs);
            LLVMValueRef idv_id_hash_arr_global = v.utils
                    .toIDVIdHashArrGlobal(n.type(), numOfIntfs);

            LLVMSetInitializer(idv_arr_global, v.utils
                    .buildConstArray(v.utils.llvmBytePtr(), intfTables));
            LLVMSetInitializer(idv_id_arr_global,
                    v.utils.buildConstArray(v.utils.llvmBytePtr(), intf_ids));
            LLVMSetInitializer(idv_id_hash_arr_global, v.utils.buildConstArray(
                    LLVMInt32TypeInContext(v.context), intf_id_hashes));

            LLVMTypeRef create_idv_ht_func_type = v.utils.functionType(
                    LLVMVoidTypeInContext(v.context), // void return type
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
                v.utils.buildProcCall(create_idv_ht_func,
                        v.utils.buildCastToBytePtr(cdv_global),
                        LLVMConstInt(LLVMInt32TypeInContext(v.context),
                                capacity, /* sign-extend */ 0),
                        LLVMConstInt(LLVMInt32TypeInContext(v.context),
                                numOfIntfs, /* sign-extend */ 0),
                        LLVMConstBitCast(idv_id_hash_arr_global,
                                v.utils.ptrTypeRef(
                                        LLVMInt32TypeInContext(v.context))),
                        v.utils.buildCastToBytePtr(idv_id_arr_global),
                        v.utils.buildCastToBytePtr(idv_arr_global));
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
        if (ty.flags().isInterface()) {
            // An interface need only establish its identity.
            v.classObjs.toTypeIdentity(ty, /* extern */ false);
            return n;
        }
        return super.overrideTranslateLLVM(v);
    }
}
