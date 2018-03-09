package polyllvm.structures;

import org.bytedeco.javacpp.LLVM;
import org.bytedeco.javacpp.LLVM.LLVMTypeRef;
import polyglot.types.ClassType;
import polyglot.types.ProcedureInstance;
import polyllvm.visit.LLVMTranslator;

public class DispatchVector_c implements DispatchVector {
    protected final LLVMTranslator v;

    public DispatchVector_c(LLVMTranslator v) {
        this.v = v;
    }

    /** The high-level layout of a dispatch vector. */
    private enum Layout {
        IT_HT {
            @Override
            LLVMTypeRef toTypeRef(DispatchVector_c o, ClassType ct) {
                return null; // TODO
            }
        },

        TYPE_INFO {
            @Override
            LLVMTypeRef toTypeRef(DispatchVector_c o, ClassType ct) {
                return null; // TODO
            }
        },

        METHODS {
            @Override
            LLVMTypeRef toTypeRef(DispatchVector_c o, ClassType ct) {
                return null; // TODO
            }
        };

        abstract LLVMTypeRef toTypeRef(DispatchVector_c o, ClassType ct);
    }

    @Override
    public LLVMTypeRef buildTypeRef(ClassType ct) {
        // return v.utils.toCDVTy(ct);
        return null; // TODO
    }

    @Override
    public LLVM.LLVMValueRef buildFuncElementPtr(LLVM.LLVMValueRef dv, ProcedureInstance pi) {
        return null; // TODO
    }
}
