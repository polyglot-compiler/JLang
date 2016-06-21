package polyllvm.util;

import java.util.HashMap;
import java.util.Map;

import polyglot.util.Position;
import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Expressions.LLVMLabel;

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
        return nf.LLVMLabel(Position.compilerGenerated(),
                            instance().freshString("label."));
    }

}
