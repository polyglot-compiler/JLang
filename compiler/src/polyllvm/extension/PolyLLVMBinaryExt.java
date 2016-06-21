package polyllvm.extension;

import polyglot.ast.Binary;
import polyglot.ast.Binary.Operator;
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
import polyllvm.ast.PseudoLLVM.LLVMNode;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Statements.LLVMICmp;
import polyllvm.util.PolyLLVMStringUtils;
import polyllvm.visit.PseudoLLVMTranslator;
import polyllvm.visit.StringLiteralRemover;

public class PolyLLVMBinaryExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node removeStringLiterals(StringLiteralRemover v) {
        Binary n = (Binary) node();
        NodeFactory nf = v.nodeFactory();
        TypeSystem ts = v.typeSystem();
        if (n.left().type().isSubtype(ts.String())
                || n.right().type().isSubtype(ts.String())) {
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
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        Binary n = (Binary) node();
        PolyLLVMNodeFactory nf = v.nodeFactory();
        LLVMOperand left = (LLVMOperand) v.getTranslation(n.left());
        LLVMOperand right = (LLVMOperand) v.getTranslation(n.right());
        Operator op = n.operator();
        if (n.left().type().isLongOrLess() && n.right().type().isLongOrLess()) {
            int intSize = Math.max(numBitsOfIntegralType(n.left().type()),
                                   numBitsOfIntegralType(n.right().type()));
            LLVMNode translation;
            if (op == Binary.ADD) {
                translation =
                        nf.LLVMAdd(Position.compilerGenerated(),
                                   nf.LLVMIntType(Position.compilerGenerated(),
                                                  intSize),
                                   left,
                                   right,
                                   nf.PolyLLVMExtFactory().extLLVMAdd());
            }
//            else if (op == Binary.SUB) {
//            }
//            else if (op == Binary.MUL) {
//            }
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
                translation = nf.LLVMICmp(Position.compilerGenerated(),
                                          LLVMICmp.sgt,
                                          nf.LLVMIntType(Position.compilerGenerated(),
                                                         intSize),
                                          left,
                                          right);

            }
            else if (op == Binary.LT) {
                translation = nf.LLVMICmp(Position.compilerGenerated(),
                                          LLVMICmp.slt,
                                          nf.LLVMIntType(Position.compilerGenerated(),
                                                         intSize),
                                          left,
                                          right);
            }
            else if (op == Binary.EQ) {
                translation = nf.LLVMICmp(Position.compilerGenerated(),
                                          LLVMICmp.eq,
                                          nf.LLVMIntType(Position.compilerGenerated(),
                                                         intSize),
                                          left,
                                          right);
            }
            else if (op == Binary.LE) {
                translation = nf.LLVMICmp(Position.compilerGenerated(),
                                          LLVMICmp.sle,
                                          nf.LLVMIntType(Position.compilerGenerated(),
                                                         intSize),
                                          left,
                                          right);
            }
            else if (op == Binary.GE) {
                translation = nf.LLVMICmp(Position.compilerGenerated(),
                                          LLVMICmp.sge,
                                          nf.LLVMIntType(Position.compilerGenerated(),
                                                         intSize),
                                          left,
                                          right);
            }
            else if (op == Binary.NE) {
                translation = nf.LLVMICmp(Position.compilerGenerated(),
                                          LLVMICmp.ne,
                                          nf.LLVMIntType(Position.compilerGenerated(),
                                                         intSize),
                                          left,
                                          right);
            }
//            else if (op == Binary.COND_AND) {
//            }
//            else if (op == Binary.COND_OR) {
//            }
            else {
                throw new InternalCompilerError("Only add operator currently supported");
            }
            v.addTranslation(n, translation);
        }
        else if (n.type().isFloat()) {
            throw new InternalCompilerError("Adding floats temporarily not supported");
        }
        else if (n.type().isDouble()) {
            throw new InternalCompilerError("Adding doubles temporarily not supported");
        }
        return super.translatePseudoLLVM(v);
    }

    private int numBitsOfIntegralType(Type t) {
        if (t.isByte())
            return 8;
        else if (t.isShort() || t.isChar())
            return 16;
        else if (t.isInt())
            return 32;
        else if (t.isLong()) return 64;
        throw new InternalCompilerError("Type " + t
                + " is not an integral type");
    }

}
