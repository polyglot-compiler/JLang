package polyllvm.extension;

import polyglot.ast.Binary;
import polyglot.ast.Binary.*;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMLabel;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMTypedOperand;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.util.PolyLLVMStringUtils;
import polyllvm.util.PolyLLVMTypeUtils;
import polyllvm.visit.AddPrimitiveWideningCastsVisitor;
import polyllvm.visit.PseudoLLVMTranslator;
import polyllvm.visit.StringLiteralRemover;

import static org.bytedeco.javacpp.LLVM.*;
import static polyglot.ast.Binary.*;

public class PolyLLVMBinaryExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node removeStringLiterals(StringLiteralRemover v) {
        Binary n = (Binary) node();
        NodeFactory nf = v.nodeFactory();
        TypeSystem ts = v.typeSystem();
        if (n.left().type().isSubtype(ts.String()) && !n.left().type().isNull()
                || n.right().type().isSubtype(ts.String())
                        && !n.right().type().isNull()) {
            Expr left = n.left();
            Expr right = n.right();
            if (left.toString().equals("null")) {
                left = (Expr) PolyLLVMStringUtils.stringToConstructor(nf.StringLit(Position.compilerGenerated(),
                                                                                   left.toString()),
                                                                      nf,
                                                                      ts);
            }
            else if (!n.left().type().isSubtype(ts.String())) {
                left = nf.Call(left.position(),
                               nf.Id(Position.compilerGenerated(),
                                     "java.lang.String.valueOf"),
                               left)
                         .type(ts.String());
            }
            if (right.toString().equals("null")) {
                right = (Expr) PolyLLVMStringUtils.stringToConstructor(nf.StringLit(Position.compilerGenerated(),
                                                                                    right.toString()),
                                                                       nf,
                                                                       ts);
            }
            else if (!n.right().type().isSubtype(ts.String())) {
                right = nf.Call(right.position(),
                                nf.Id(Position.compilerGenerated(),
                                      "java.lang.String.valueOf"),
                                right)
                          .type(ts.String());
            }

            return nf.Call(n.position(),
                           left,
                           nf.Id(Position.compilerGenerated(), "concat"),
                           right)
                     .type(ts.String());
        }

        return super.removeStringLiterals(v);
    }

    @Override
    public Node addPrimitiveWideningCasts(AddPrimitiveWideningCastsVisitor v) {
        Binary n = (Binary) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        Expr left = n.left();
        Expr right = n.right();

        Operator op = n.operator();
        // Rules for Binary Numeric Promotion found in Java Lang. Spec 5.6.2
        // All binary operands except for shifts, or, and are subject to the rules
        if (!(op == Binary.SHL || op == Binary.SHR || op == Binary.USHR
                || op == Binary.COND_OR || op == Binary.COND_AND)
                && left.type().isPrimitive() && right.type().isPrimitive()) {
            //"If either operand is of type double, the other is converted to double."
            if (left.type().isDouble() && !right.type().isDouble()) {
                right = nf.Cast(Position.compilerGenerated(),
                                nf.CanonicalTypeNode(Position.compilerGenerated(),
                                                     left.type()),
                                right)
                          .type(left.type());
            }
            else if (!left.type().isDouble() && right.type().isDouble()) {
                left = nf.Cast(Position.compilerGenerated(),
                               nf.CanonicalTypeNode(Position.compilerGenerated(),
                                                    right.type()),
                               left)
                         .type(right.type());
            }
            else if (left.type().isDouble() && right.type().isDouble()) {
                //Both are doubles -- do nothing
            }
            //Otherwise, if either operand is of type float, the other is converted to float.
            else if (left.type().isFloat() && !right.type().isFloat()) {
                right = nf.Cast(Position.compilerGenerated(),
                                nf.CanonicalTypeNode(Position.compilerGenerated(),
                                                     left.type()),
                                right)
                          .type(left.type());
            }
            else if (!left.type().isFloat() && right.type().isFloat()) {
                left = nf.Cast(Position.compilerGenerated(),
                               nf.CanonicalTypeNode(Position.compilerGenerated(),
                                                    right.type()),
                               left)
                         .type(right.type());
            }
            else if (left.type().isFloat() && right.type().isFloat()) {
                //Both are floats -- do nothing

            }
            //Otherwise, if either operand is of type long, the other is converted to long
            else if (left.type().isLong() && !right.type().isLong()) {
                right = nf.Cast(Position.compilerGenerated(),
                                nf.CanonicalTypeNode(Position.compilerGenerated(),
                                                     left.type()),
                                right)
                          .type(left.type());
            }
            else if (!left.type().isLong() && right.type().isLong()) {
                left = nf.Cast(Position.compilerGenerated(),
                               nf.CanonicalTypeNode(Position.compilerGenerated(),
                                                    right.type()),
                               left)
                         .type(right.type());
            }
            else if (left.type().isLong() && right.type().isLong()) {
                //Both are longs -- do nothing

            }
            //Otherwise, both operands are converted to type int
            else if (!left.type().isInt() && right.type().isInt()) {
                left = nf.Cast(Position.compilerGenerated(),
                               nf.CanonicalTypeNode(Position.compilerGenerated(),
                                                    right.type()),
                               left)
                         .type(right.type());

            }
            else if (left.type().isInt() && !right.type().isInt()) {
                right = nf.Cast(Position.compilerGenerated(),
                                nf.CanonicalTypeNode(Position.compilerGenerated(),
                                                     left.type()),
                                right)
                          .type(left.type());
            }
            else if (left.type().isInt() && right.type().isInt()) {
                //Do nothing: they are both already ints
            }
            else {
                TypeSystem ts = v.typeSystem();
                left = nf.Cast(Position.compilerGenerated(),
                               nf.CanonicalTypeNode(Position.compilerGenerated(),
                                                    ts.Int()),
                               left)
                         .type(ts.Int());
                right = nf.Cast(Position.compilerGenerated(),
                                nf.CanonicalTypeNode(Position.compilerGenerated(),
                                                     ts.Int()),
                                right)
                          .type(ts.Int());
            }
            n = n.left(left);
            n = n.right(right);
            return n;
        }

        return super.addPrimitiveWideningCasts(v);
    }

    private static boolean isUnsigned(Type t) {
        return t.isChar();
    }

    private static int llvmComparisonKind(Operator op, Type t) {
        if      (op == LT) return isUnsigned(t) ? LLVMIntULT : LLVMIntSLT;
        else if (op == LE) return isUnsigned(t) ? LLVMIntULE : LLVMIntSLE;
        else if (op == EQ) return LLVMIntEQ;
        else if (op == NE) return LLVMIntNE;
        else if (op == GE) return isUnsigned(t) ? LLVMIntUGE : LLVMIntSGE;
        else if (op == GT) return isUnsigned(t) ? LLVMIntUGT : LLVMIntSGT;
        else {
            throw new InternalCompilerError("This operation is not a comparison");
        }
    }

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        Binary n = (Binary) node();
        Type type = n.type();
        LLVMValueRef left = (LLVMValueRef) v.getTranslation(n.left());
        LLVMValueRef right = (LLVMValueRef) v.getTranslation(n.right());
        Operator op = n.operator();
        LLVMValueRef res;

        // TODO: Will need to add widening casts here.

        if (type.isLongOrLess()) {
            // Integer arithmetic.
            if (op == Binary.ADD) {
                res = LLVMBuildAdd(v.builder, left, right, "add");
            } else if (op == SUB) {
                res = LLVMBuildSub(v.builder, left, right, "sub");
            } else if (op == MUL) {
                res = LLVMBuildMul(v.builder, left, right, "mul");
            } else if (op == DIV && type.isChar()) {
                res = LLVMBuildUDiv(v.builder, left, right, "udiv");
            } else if (op == DIV) {
                res = LLVMBuildSDiv(v.builder, left, right, "div");
            } else if (op == MOD && type.isChar()) {
                res = LLVMBuildURem(v.builder, left, right, "umod");
            } else if (op == MOD) {
                res = LLVMBuildSRem(v.builder, left, right, "mod");
            } else if (op == BIT_OR) {
                res = LLVMBuildOr(v.builder, left, right, "or");
            } else if (op == BIT_AND) {
                res = LLVMBuildAnd(v.builder, left, right, "and");
            } else if (op == BIT_XOR) {
                res = LLVMBuildXor(v.builder, left, right, "xor");
            } else if (op == SHL) {
                res = LLVMBuildShl(v.builder, left, right, "shl");
            } else if (op == USHR || (op == SHR && type.isChar())) {
                res = LLVMBuildLShr(v.builder, left, right, "lshr");
            } else if (op == SHR) {
                res = LLVMBuildAShr(v.builder, left, right, "ashr");
            } else {
                throw new InternalCompilerError("Invalid integer operation");
            }
        }
        else if (type.isBoolean()) {
            // Comparison.
            res = LLVMBuildICmp(v.builder, llvmComparisonKind(op, type), left, right, "cmp");
        }
        else if (type.isFloat() || type.isDouble()) {
            // Floating point arithmetic.
            if (op == ADD) {
                res = LLVMBuildFAdd(v.builder, left, right, "fadd");
            } else if (op == SUB) {
                res = LLVMBuildFSub(v.builder, left, right, "fsub");
            } else if (op == MUL) {
                res = LLVMBuildFMul(v.builder, left, right, "fmul");
            } else if (op == DIV) {
                res = LLVMBuildFDiv(v.builder, left, right, "fdiv");
            } else {
                throw new InternalCompilerError("Invalid floating point operation");
            }
        } else {
            throw new InternalCompilerError("Invalid binary operation result type");
        }

        v.addTranslation(n, res);
        return super.translatePseudoLLVM(v);
    }

    @Override
    public Node translatePseudoLLVMConditional(PseudoLLVMTranslator v,
                                               LLVMLabel trueLabel, LLVMLabel falseLabel) {
        Binary n = (Binary) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        Operator op = n.operator();
        if (op == Binary.COND_AND || op == Binary.COND_OR)
            throw new InternalCompilerError("Short-circuiting AND/OR not supported as binop");

        LLVMOperand translation = (LLVMOperand) v.getTranslation(n);
        LLVMTypeNode tn = PolyLLVMTypeUtils.polyLLVMTypeNode(nf, n.type());
        LLVMTypedOperand cond = nf.LLVMTypedOperand(translation, tn);
        return  nf.LLVMBr(cond, trueLabel, falseLabel);
    }
}
