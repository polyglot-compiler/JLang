//Copyright (C) 2017 Cornell University

package jlang.util;

import java.util.HashMap;
import java.util.Map;

public class JLangFreshGen {

    private static JLangFreshGen instance = null;

    Map<String, Integer> prefixMap;

    private JLangFreshGen() {
        prefixMap = new HashMap<>();
    }

    private static JLangFreshGen instance() {
        if (instance == null) {
            instance = new JLangFreshGen();
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

    public static String fresh() {
        return instance().freshString("fresh");
    }


}
