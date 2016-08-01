package polyllvm.extension;

import polyglot.ast.Binary;
import polyglot.ast.Binary.Operator;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMLabel;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMTypedOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMIntType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.Statements.LLVMBr;
import polyllvm.ast.PseudoLLVM.Statements.LLVMICmp;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;
import polyllvm.util.PolyLLVMFreshGen;
import polyllvm.util.PolyLLVMStringUtils;
import polyllvm.util.PolyLLVMTypeUtils;
import polyllvm.visit.AddPrimitiveWideningCastsVisitor;
import polyllvm.visit.PseudoLLVMTranslator;
import polyllvm.visit.StringLiteralRemover;

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

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        Binary n = (Binary) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        LLVMOperand left = (LLVMOperand) v.getTranslation(n.left());
        LLVMOperand right = (LLVMOperand) v.getTranslation(n.right());
        Operator op = n.operator();
        if (n.left().type().isLongOrLess() && n.right().type().isLongOrLess()) {
            int intSize =
                    Math.max(PolyLLVMTypeUtils.numBitsOfIntegralType(n.left()
                                                                      .type()),
                             PolyLLVMTypeUtils.numBitsOfIntegralType(n.right()
                                                                      .type()));
            LLVMInstruction translation;
            LLVMTypeNode tn;
            if (op == Binary.ADD) {
                tn = nf.LLVMIntType(intSize);
                translation = nf.LLVMAdd((LLVMIntType) tn, left, right);
            }
            else if (op == Binary.SUB) {
                tn = nf.LLVMIntType(intSize);
                translation = nf.LLVMSub((LLVMIntType) tn, left, right);

            }
            else if (op == Binary.MUL) {
                tn = nf.LLVMIntType(intSize);
                translation = nf.LLVMMul((LLVMIntType) tn, left, right);

            }
//            else if (op == Binary.DIV) {
//            }
//            else if (op == Binary.MOD) {
//            }
//            else if (op == Binary.BIT_OR) {
//            }
//            else if (op == Binary.BIT_AND) {
//            }
//            else if (op == Binary.BIT_XOR) {
//            }
//            else if (op == Binary.SHL) {
//            }
//            else if (op == Binary.SHR) {
//            }
//            else if (op == Binary.USHR) {
//            }
            else if (op == Binary.GT) {
                tn = nf.LLVMIntType(1);
                translation = nf.LLVMICmp((LLVMIntType) tn,
                                          LLVMICmp.sgt,
                                          nf.LLVMIntType(intSize),
                                          left,
                                          right);

            }
            else if (op == Binary.LT) {
                tn = nf.LLVMIntType(1);
                translation = nf.LLVMICmp((LLVMIntType) tn,
                                          LLVMICmp.slt,
                                          nf.LLVMIntType(intSize),
                                          left,
                                          right);
            }
            else if (op == Binary.EQ) {
                tn = nf.LLVMIntType(1);
                translation = nf.LLVMICmp((LLVMIntType) tn,
                                          LLVMICmp.eq,
                                          nf.LLVMIntType(intSize),
                                          left,
                                          right);
            }
            else if (op == Binary.LE) {
                tn = nf.LLVMIntType(1);
                translation = nf.LLVMICmp((LLVMIntType) tn,
                                          LLVMICmp.sle,
                                          nf.LLVMIntType(intSize),
                                          left,
                                          right);
            }
            else if (op == Binary.GE) {
                tn = nf.LLVMIntType(1);
                translation = nf.LLVMICmp((LLVMIntType) tn,
                                          LLVMICmp.sge,
                                          nf.LLVMIntType(intSize),
                                          left,
                                          right);
            }
            else if (op == Binary.NE) {
                tn = nf.LLVMIntType(1);
                translation = nf.LLVMICmp((LLVMIntType) tn,
                                          LLVMICmp.ne,
                                          nf.LLVMIntType(intSize),
                                          left,
                                          right);
            }
//            else if (op == Binary.COND_AND) {
//            }
//            else if (op == Binary.COND_OR) {
//            }
            else {
                throw new InternalCompilerError("Operator " + op
                        + " is not currently supported for integral types");
            }

            LLVMVariable result = PolyLLVMFreshGen.freshLocalVar(nf, tn);
            translation = translation.result(result);
            v.addTranslation(n, nf.LLVMESeq(translation, result));
        }
        else if (n.type().isFloat()) {
            throw new InternalCompilerError("Adding floats temporarily not supported");
        }
        else if (n.type().isDouble()) {
            LLVMInstruction translation;
            LLVMTypeNode tn;
            if (op == Binary.ADD) {
                tn = PolyLLVMTypeUtils.polyLLVMTypeNode(nf, n.type());
                translation = nf.LLVMFAdd(tn, left, right);
            }
            // else if (op == Binary.SUB) {

            // }
            // else if (op == Binary.MUL) {
            // }
//            else if (op == Binary.DIV) {
//            }
//            else if (op == Binary.MOD) {
//            }
//            else if (op == Binary.BIT_OR) {
//            }
//            else if (op == Binary.BIT_AND) {
//            }
//            else if (op == Binary.BIT_XOR) {
//            }
//            else if (op == Binary.SHL) {
//            }
//            else if (op == Binary.SHR) {
//            }
//            else if (op == Binary.USHR) {
//            }
            // else if (op == Binary.GT) {
            // }
            // else if (op == Binary.LT) {
            // }
            // else if (op == Binary.EQ) {
            // }
            // else if (op == Binary.LE) {
            // }
            // else if (op == Binary.GE) {
            // }
            // else if (op == Binary.NE) {
            // }
//            else if (op == Binary.COND_AND) {
//            }
//            else if (op == Binary.COND_OR) {
//            }
            else {
                throw new InternalCompilerError("Operator " + op
                        + " is not currently supported for doubles");
            }

            LLVMVariable result = PolyLLVMFreshGen.freshLocalVar(nf, tn);
            translation = translation.result(result);
            v.addTranslation(n, nf.LLVMESeq(translation, result));
        }
        return super.translatePseudoLLVM(v);
    }

    @Override
    public Node translatePseudoLLVMConditional(PseudoLLVMTranslator v,
            LLVMLabel trueLabel, LLVMLabel falseLabel) {
        Binary n = (Binary) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
//        LLVMOperand left = (LLVMOperand) v.getTranslation(n.left());
//        LLVMOperand right = (LLVMOperand) v.getTranslation(n.right());
        Operator op = n.operator();

//        if (op == Binary.GT) {
//        }
//        else if (op == Binary.LT) {
//        }
//        else if (op == Binary.EQ) {
//        }
//        else if (op == Binary.LE) {
//        }
//        else if (op == Binary.GE) {
//        }
//        else if (op == Binary.NE) {
//        }
        if (op == Binary.COND_AND) {
            throw new InternalCompilerError("Conditional translation of AND not supported");
        }
        else if (op == Binary.COND_OR) {
            throw new InternalCompilerError("Conditional translation of OR not supported");
        }
        else {
            if (!(v.getTranslation(n) instanceof LLVMOperand)) {
                throw new InternalCompilerError("Binary " + n
                        + " is not translated to an LLVMOperand");
            }
            LLVMOperand translation = (LLVMOperand) v.getTranslation(n);
//            LLVMInstruction cmp = (LLVMInstruction) translation;
//            LLVMVariable tmp = PolyLLVMFreshGen.freshLocalVar(nf);
//            cmp = cmp.result(tmp);
            LLVMTypeNode tn = PolyLLVMTypeUtils.polyLLVMTypeNode(nf, n.type());
            LLVMTypedOperand cond = nf.LLVMTypedOperand(translation, tn);
            LLVMBr br = nf.LLVMBr(cond, trueLabel, falseLabel);
//            List<LLVMInstruction> instructions = new ArrayList<>();
//            instructions.add(br);
            return br;//nf.LLVMSeq( instructions);
        }
//        return super.translatePseudoLLVMConditional(v, trueLabel, falseLabel);
    }

}
