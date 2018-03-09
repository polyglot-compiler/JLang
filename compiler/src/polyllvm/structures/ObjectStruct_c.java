package polyllvm.structures;

import org.bytedeco.javacpp.LLVM.*;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;
import java.util.*;
import java.util.stream.Stream;

import static org.bytedeco.javacpp.LLVM.*;

public class ObjectStruct_c implements ObjectStruct {
    protected final LLVMTranslator v;
    protected final Map<ClassType, LLVMTypeRef> typeCache = new HashMap<>();
    protected final Map<ReferenceType, List<FieldInstance>> fieldCache = new HashMap<>();

    public ObjectStruct_c(LLVMTranslator v) {
        this.v = v;
    }

    /** The high-level layout of an object instance. */
    protected enum Layout {
        DV {
            @Override
            LLVMTypeRef buildTypeRef(ObjectStruct_c o, ClassType erased) {
                return o.v.utils.ptrTypeRef(o.v.utils.toCDVTy(erased));
            }
        },

        SYNC_VARS {
            @Override
            LLVMTypeRef buildTypeRef(ObjectStruct_c o, ClassType erased) {
                // TODO: This will eventually be needed for implementing synchronized blocks.
                return o.v.utils.llvmBytePtr();
            }
        },

        FIELDS {
            @Override
            LLVMTypeRef buildTypeRef(ObjectStruct_c o, ClassType erased) {
                List<FieldInstance> instanceFields = o.getOrComputeInstanceFields(erased);
                LLVMTypeRef[] fieldTypeRefs = instanceFields.stream()
                        .map((fi) -> o.v.utils.toLL(fi.type()))
                        .toArray(LLVMTypeRef[]::new);
                return o.v.utils.structType(fieldTypeRefs);
            }
        };

        // Important:
        // Arrays implicitly hold their elements right after their length field (see below).

        /** Convert a component to an LLVM type reference. */
        abstract LLVMTypeRef buildTypeRef(ObjectStruct_c o, ClassType erased);

        /** Returns an array of type references for all components of this struct. */
        static LLVMTypeRef[] buildComponentTypeRefs(ObjectStruct_c o, ClassType erased) {

            LLVMTypeRef[] baseComponents = Stream.of(Layout.values())
                    .map((c) -> c.buildTypeRef(o, erased))
                    .toArray(LLVMTypeRef[]::new);

            if (erased.typeEquals(o.v.ts.ArrayObject())) {
                // Append a zero-length LLVM array [0 x i8] onto Java array types so that
                // we can access the underlying array elements. This is the LLVM-approved
                // way of implementing dynamically sized arrays.
                LLVMTypeRef i8 = LLVMInt8TypeInContext(o.v.context);
                return Stream.concat(
                        Stream.of(baseComponents),
                        Stream.of(LLVMArrayType(i8, 0)))
                        .toArray(LLVMTypeRef[]::new);
            }
            else if (erased.isClass()) {
                // Standard class instance.
                return baseComponents;
            }
            else {
                throw new InternalCompilerError("Unhandled reference type");
            }
        }
    }

    @Override
    public LLVMTypeRef structTypeRef(ReferenceType rt) {
        ClassType erased = v.utils.erasureLL(rt); // Erase generic types!
        return typeCache.computeIfAbsent(erased, (key) -> {
            String mangledName = v.mangler.classTypeName(erased);
            return v.utils.getOrCreateNamedOpaqueStruct(mangledName);
        });
    }

    /**
     * Same as {@link ObjectStruct#structTypeRef(ReferenceType)}, but ensures that
     * the struct is non-opaque.
     */
    protected LLVMTypeRef structTypeRefNonOpaque(ReferenceType rt) {
        ClassType erased = v.utils.erasureLL(rt); // Erase generic types!
        LLVMTypeRef res = structTypeRef(erased);
        v.utils.fillStructIfNeeded(res, () -> Layout.buildComponentTypeRefs(this, erased));
        return res;
    }

    @Override
    public LLVMValueRef sizeOf(ReferenceType rt) {
        return LLVMSizeOf(structTypeRefNonOpaque(rt));
    }

    @Override
    public LLVMValueRef buildDispatchVectorElementPtr(LLVMValueRef instance, ReferenceType rt) {
        structTypeRefNonOpaque(rt); // Ensure non-opaque type.
        return v.utils.buildStructGEP(instance, 0, Layout.DV.ordinal());
    }

    @Override
    public LLVMValueRef buildFieldElementPtr(LLVMValueRef instance, FieldInstance fi) {
        fi = v.utils.erasureLL(fi); // Erase generic types!
        structTypeRefNonOpaque(fi.container()); // Ensure non-opaque type.

        int idx = getOrComputeInstanceFields(fi.container()).indexOf(fi);
        if (idx < 0)
            throw new InternalCompilerError("Field " + fi + " not found in " + fi.container());
        return v.utils.buildStructGEP(instance, 0, Layout.FIELDS.ordinal(), idx);
    }

    @Override
    public LLVMValueRef buildArrayBaseElementPtr(LLVMValueRef instance, ArrayType at) {
        structTypeRefNonOpaque(at); // Ensure non-opaque type.

        // Go one past the end of the object header (see Layout#buildComponentTypeRefs).
        int idx = Layout.values().length;
        LLVMValueRef baseRaw = v.utils.buildStructGEP(instance, 0, idx);
        LLVMTypeRef ptrType = v.utils.ptrTypeRef(v.utils.toLL(at.base()));
        return LLVMBuildBitCast(v.builder, baseRaw, ptrType, "cast");
    }

    /** Returns an ordered list of all type-erased fields in the given reference type. */
    protected List<FieldInstance> getOrComputeInstanceFields(ReferenceType rt) {
        ClassType erased = v.utils.erasureLL(rt);
        return fieldCache.computeIfAbsent(erased, (key) -> {

            // Add fields from super type.
            List<FieldInstance> res = new ArrayList<>();
            if (erased.superType() != null)
                res.addAll(getOrComputeInstanceFields(erased.superType().toReference()));

            // Add own fields.
            erased.fields().stream()
                    .filter(fi -> !fi.flags().isStatic()) // Non-static.
                    .sorted(Comparator.comparing(VarInstance::name))
                    .forEach(res::add);

            return res;
        });
    }
}
