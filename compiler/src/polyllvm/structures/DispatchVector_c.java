package polyllvm.structures;

import org.bytedeco.javacpp.LLVM;
import org.bytedeco.javacpp.LLVM.*;
import polyglot.types.ClassType;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyllvm.util.Constants;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.bytedeco.javacpp.LLVM.*;

// TODO
// There's still some cleanup to do here, namely, moving all dispatch vector code
// from LLVMUtils and LLVMTranslator into this class. May also want to create new classes
// to specifically manage the methods array and interface method hash table.

public class DispatchVector_c implements DispatchVector {
    protected final LLVMTranslator v;
    protected final Map<ClassType, LLVMTypeRef> typeCache = new HashMap<>();
    protected final Map<ClassType, List<MethodInstance>> methodCache = new HashMap<>();

    public DispatchVector_c(LLVMTranslator v) {
        this.v = v;
    }

    /**
     * The high-level layout of a dispatch vector.
     * This should match the layout defined in the native runtime.
     */
    private enum Layout {

        CLASS_OBJECT {
            // A pointer to the java.lang.Class representing this class type.
            // These class objects are declared as static fields.
            @Override
            LLVMValueRef buildValueRef(DispatchVector_c o, ClassType erased) {
                String fieldName = Constants.CLASS_OBJECT;
                String mangledName = o.v.mangler.mangleStaticFieldName(erased, fieldName);
                LLVMTypeRef elemType = o.v.utils.toLL(o.v.ts.Class());
                return o.v.utils.getGlobal(mangledName, elemType);
            }
        },

        INTERFACE_METHOD_HASH_TABLE {
            // A hash table for interface method dispatch.
            @Override
            LLVMValueRef buildValueRef(DispatchVector_c o, ClassType erased) {
                // The pointer will be initialized at runtime.
                return LLVMConstNull(o.v.utils.i8Ptr());
            }
        },

        SUPER_TYPES {
            // A contiguous array of all super types for cache-efficient instanceof checks.
            @Override
            LLVMValueRef buildValueRef(DispatchVector_c o, ClassType erased) {
                return o.v.classObjs.classObjRef(erased);
            }
        },

        CLASS_METHODS {
            // Method pointers for class method dispatch.
            @Override
            LLVMValueRef buildValueRef(DispatchVector_c o, ClassType erased) {

                // Convert a method instance into a function pointer.
                Function<MethodInstance, LLVMValueRef> getFuncPtr = mi -> {
                    String name = o.v.mangler.mangleProcName(mi);
                    LLVMTypeRef type = o.v.utils.toLL(mi);
                    return o.v.utils.getFunction(name, type);
                };

                // Collect all function pointers.
                LLVMValueRef[] funcPtrs = o.v.cdvMethods(erased).stream()
                        .map(getFuncPtr)
                        .toArray(LLVMValueRef[]::new);

                return o.v.utils.buildConstStruct(funcPtrs);
            }
        };

        abstract LLVMValueRef buildValueRef(DispatchVector_c o, ClassType erased);

        static LLVMValueRef[] buildComponentValueRefs(DispatchVector_c o, ClassType erased) {
            return Stream.of(Layout.values())
                    .map((c) -> c.buildValueRef(o, erased))
                    .toArray(LLVMValueRef[]::new);
        }
    }

    @Override
    public LLVMTypeRef structTypeRef(ReferenceType rt) {
        ClassType erased = v.utils.erasureLL(rt); // Erase generic types!
        return typeCache.computeIfAbsent(erased, (key) -> {
            String mangledName = v.mangler.cdvTyName(erased);
            return v.utils.getOrCreateNamedOpaqueStruct(mangledName);
        });
    }

    /**
     * Same as {@link DispatchVector#structTypeRef(ReferenceType)}, but ensures that
     * the struct is non-opaque.
     */
    protected LLVMTypeRef structTypeRefNonOpaque(ReferenceType rt) {
        ClassType erased = v.utils.erasureLL(rt); // Erase generic types!
        LLVMTypeRef res = structTypeRef(erased);
        v.utils.fillStructIfNeeded(res, () ->
                Stream.of(Layout.buildComponentValueRefs(this, erased))
                        .map(LLVM::LLVMTypeOf)
                        .toArray(LLVMTypeRef[]::new));
        return res;
    }

    @Override
    public void initializeDispatchVectorFor(ReferenceType rt) {
        ClassType erased = v.utils.erasureLL(rt); // Erase generic types!
        LLVMTypeRef typeRef = structTypeRefNonOpaque(erased); // Ensure non-opaque type.
        LLVMValueRef global = getDispatchVectorFor(erased);
        LLVMValueRef[] body = Layout.buildComponentValueRefs(this, erased);
        LLVMValueRef init = v.utils.buildNamedConstStruct(typeRef, body);
        LLVMSetInitializer(global, init);
    }

    @Override
    public LLVMValueRef getDispatchVectorFor(ReferenceType rt) {
        return v.utils.getGlobal(v.mangler.cdvGlobalId(rt), structTypeRef(rt));
    }

    @Override
    public LLVMValueRef buildFuncElementPtr(
            LLVMValueRef dvPtr, ReferenceType recvTy, MethodInstance mi) {
        structTypeRefNonOpaque(recvTy); // Ensure non-opaque type.
        int idx = v.dispatchInfo(recvTy, mi).methodIndex();
        return v.utils.buildStructGEP(dvPtr, 0, Layout.CLASS_METHODS.ordinal(), idx);
    }
}
