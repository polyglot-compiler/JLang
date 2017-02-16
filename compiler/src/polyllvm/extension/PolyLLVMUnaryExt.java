package polyllvm.extension;

import com.sun.tools.javac.util.List;
import polyglot.ast.*;
import polyglot.ast.Unary.*;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.util.LLVMUtils;
import polyllvm.visit.PseudoLLVMTranslator;

import static org.bytedeco.javacpp.LLVM.*;
import static polyglot.ast.Unary.*;

public class PolyLLVMUnaryExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        Unary n = (Unary) node();
        Type t = n.type();
        NodeFactory nf = v.nodeFactory();
        Operator op = n.operator();
        Expr expr = n.expr();
        LLVMValueRef exprRef = v.getTranslation(expr);
        LLVMTypeRef exprTypeRef = LLVMUtils.typeRef(expr.type(), v);

        LLVMValueRef translation;
        if (op.equals(BIT_NOT)) {
            LLVMValueRef negOne = LLVMConstInt(exprTypeRef, -1, /* sign-extend */ 0);
            translation = LLVMBuildXor(v.builder, exprRef, negOne, "bit_not");
        }
        else if (op.equals(NEG)) {
            translation = t.isLongOrLess()
                    ? LLVMBuildNeg (v.builder, exprRef, "neg")
                    : LLVMBuildFNeg(v.builder, exprRef, "neg");
        }
        else if (op.equals(POS)) {
            translation = exprRef;
        }
        else if (op.equals(NOT)) {
            assert t.typeEquals(v.typeSystem().Boolean());
            translation = LLVMBuildNot(v.builder, exprRef, "not");
        }
        else if (List.of(PRE_INC, PRE_DEC, POST_INC, POST_DEC).contains(op)) {
            // De-sugar increment operation into a vanilla assignment.
            // This is done here rather than in a separate visitor pass in order to avoid
            // the need for an ESeq node.
            boolean pre = op.equals(PRE_INC) || op.equals(PRE_DEC);
            boolean inc = op.equals(PRE_INC) || op.equals(POST_INC);
            Binary.Operator binop = inc ? Binary.ADD : Binary.SUB;
            Position pos = Position.COMPILER_GENERATED;
            Expr delta = expr.type().isLongOrLess()
                    ? nf.IntLit(pos, IntLit.LONG, 1)
                    : nf.FloatLit(pos, FloatLit.DOUBLE, 1.);
            TypeNode exprTypeNode = nf.CanonicalTypeNode(pos, expr.type());
            Expr castDelta = nf.Cast(pos, exprTypeNode, delta);
            Expr newValue = nf.Binary(pos, expr, binop, castDelta);

            Assign assign;
            if (expr instanceof Local) {
                assign = nf.LocalAssign(pos, (Local) expr, Assign.ASSIGN, newValue);
            } else if (expr instanceof Field) {
                assign = nf.FieldAssign(pos, (Field) expr, Assign.ASSIGN, newValue);
            } else if (expr instanceof ArrayAccess) {
                assign = nf.ArrayAccessAssign(pos, (ArrayAccess) expr, Assign.ASSIGN, newValue);
            } else {
                throw new InternalCompilerError("Invalid operand to increment operation");
            }

            v.visitEdge(n, assign);
            translation = pre ? v.getTranslation(newValue) : exprRef;
        }
        else {
            throw new InternalCompilerError("Invalid unary operation");
        }

        v.addTranslation(n, translation);
        return super.translatePseudoLLVM(v);
    }

}
