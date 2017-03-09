package polyllvm.extension;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import java.util.ArrayList;
import java.util.Arrays;

import static org.bytedeco.javacpp.LLVM.LLVMBuildLoad;
import static org.bytedeco.javacpp.LLVM.LLVMValueRef;

public class PolyLLVMArrayAccessExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node overrideTranslatePseudoLLVM(LLVMTranslator v) {
        ArrayAccess n = (ArrayAccess) node();
        LLVMValueRef ptr = translateAsLValue(v); // Emits debug location.
        LLVMValueRef load = LLVMBuildLoad(v.builder, ptr, "arr_load");
        v.addTranslation(n, load);
        return n;
    }

    @Override
    public LLVMValueRef translateAsLValue(LLVMTranslator v) {
        // Return a pointer to the appropriate element in the array.
        ArrayAccess n = (ArrayAccess) node();
        NodeFactory nf = v.nodeFactory();
        TypeSystem ts = v.typeSystem();
        Position pos = n.position();

        n.array().visit(v);
        n.index().visit(v);
        v.debugInfo.emitLocation(n);

        //TODO: implement this logic in LLVM so index isn't translated multiple times
//        FieldInstance lengthFi;
//        try {
//            lengthFi = ts.findField(n.array().type().toReference(), "length", v.getCurrentClass().type(), true);
//        } catch (SemanticException se){
//            throw new InternalCompilerError(se);
//        }
//        Expr length = nf.Field(pos, n.array(), nf.Id(pos, "length")).fieldInstance(lengthFi).type(ts.Int());
//        Expr right = nf.Binary(pos, n.index(), Binary.GE, length).type(ts.Boolean());
//        Expr left = nf.Binary(pos, n.index(), Binary.LT, nf.IntLit(pos, IntLit.INT, 0).type(ts.Int())).type(ts.Boolean());
//        Expr condition = nf.Binary(pos, left, Binary.COND_OR, right).type(ts.Boolean());
//
//        ClassType arrayIndexOutOfBoundsExceptionType = getArrayIndexOutOfBoundsExceptionType(ts);
//
//        ConstructorInstance constructor;
//        try {
//            constructor = ts.findConstructor(arrayIndexOutOfBoundsExceptionType, Arrays.asList(ts.Int()), v.getCurrentClass().type(), true);
//        } catch (SemanticException e){
//            throw new InternalCompilerError(e);
//        }
//        Expr outOfBounds = nf.New(pos, nf.CanonicalTypeNode(pos, arrayIndexOutOfBoundsExceptionType), Arrays.asList(index()))
//                .constructorInstance(constructor)
//                .type(arrayIndexOutOfBoundsExceptionType);
//        Throw throwOutOfBounds = nf.Throw(pos, outOfBounds);
//
//        If anIf = nf.If(pos, condition, throwOutOfBounds);
//        anIf.visit(v);

        LLVMValueRef arr = v.getTranslation(n.array());
        LLVMValueRef base = v.utils.buildJavaArrayBase(arr, n.type());
        LLVMValueRef offset = v.getTranslation(n.index());
        return v.utils.buildGEP(base, offset);
    }

    private ClassType getArrayIndexOutOfBoundsExceptionType(TypeSystem ts) {
        ClassType type;
        try {
            type = (ClassType) ts.typeForName("java.lang.ArrayIndexOutOfBoundsException");
        } catch (SemanticException se){
            throw new InternalCompilerError(se);
        }
        return type;
    }
}
