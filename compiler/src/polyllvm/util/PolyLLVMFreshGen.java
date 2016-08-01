package polyllvm.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMLabel;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMTypedOperand;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMVariable_c.VarType;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;
import polyllvm.ast.PseudoLLVM.Statements.LLVMGetElementPtr;

public class PolyLLVMFreshGen {

    private static PolyLLVMFreshGen instance = null;

    Map<String, Integer> prefixMap;

    private PolyLLVMFreshGen() {
        prefixMap = new HashMap<>();
    }

    private static PolyLLVMFreshGen instance() {
        if (instance == null) {
            instance = new PolyLLVMFreshGen();
        }
        return instance;
    }

    private String freshString(String string) {
        String ret;
        if (prefixMap.containsKey(string)) {
            ret = string + prefixMap.get(string);
            prefixMap.put(string, prefixMap.get(string) + 1);
        }
        else {
            ret = string + "0";
            prefixMap.put(string, 1);
        }
        return ret;
    }

    public static LLVMLabel freshLabel(PolyLLVMNodeFactory nf) {
        return nf.LLVMLabel(instance().freshString("label."));
    }

    public static LLVMLabel freshNamedLabel(PolyLLVMNodeFactory nf,
            String prefix) {
        return nf.LLVMLabel(instance().freshString(prefix + "."));
    }

    public static LLVMVariable freshLocalVar(PolyLLVMNodeFactory nf,
            LLVMTypeNode tn) {
        return nf.LLVMVariable(instance().freshString("_temp."),
                               tn,
                               VarType.LOCAL);
    }

    public static LLVMVariable freshNamedLocalVar(PolyLLVMNodeFactory nf,
            String name, LLVMTypeNode tn) {
        return nf.LLVMVariable(instance().freshString("_" + name + "."),
                               tn,
                               VarType.LOCAL);
    }

    public static LLVMGetElementPtr freshGetElementPtr(PolyLLVMNodeFactory nf,
            LLVMVariable result, LLVMOperand o, int... is) {
        List<LLVMTypedOperand> operands = new ArrayList<>();
        for (int index : is) {
            LLVMTypeNode tn = nf.LLVMIntType(32);
            LLVMOperand op = nf.LLVMIntLiteral(tn, index);
            operands.add(nf.LLVMTypedOperand(op, tn));
        }
        return nf.LLVMGetElementPtr(o, operands).result(result);
    }

}
