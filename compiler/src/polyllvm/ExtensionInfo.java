package polyllvm;

import polyglot.lex.Lexer;
import polyllvm.parse.Lexer_c;
import polyllvm.parse.Grm;
import polyllvm.ast.*;
import polyllvm.types.*;
import polyglot.ast.*;
import polyglot.frontend.*;
import polyglot.main.*;
import polyglot.types.*;
import polyglot.util.*;

import java.io.*;
import java.util.Set;

/**
 * Extension information for PolyLLVM extension.
 */
public class ExtensionInfo extends polyglot.frontend.JLExtensionInfo {
    static {
        // force Topics to load
        @SuppressWarnings("unused")
        Topics t = new Topics();
    }

    @Override
    public String defaultFileExtension() {
        return "pll";
    }

    @Override
    public String compilerName() {
        return "PolyLLVMc";
    }

    @Override
    public Parser parser(Reader reader, Source source, ErrorQueue eq) {
        Lexer lexer = new Lexer_c(reader, source, eq);
        Grm grm = new Grm(lexer, ts, nf, eq);
        return new CupParser(grm, source, eq);
    }

    @Override
    public Set<String> keywords() {
	return new Lexer_c(null).keywords();
    }

    @Override
    protected NodeFactory createNodeFactory() {
        return new PolyLLVMNodeFactory_c(PolyLLVMLang_c.instance, new PolyLLVMExtFactory_c());
    }

    @Override
    protected TypeSystem createTypeSystem() {
        return new PolyLLVMTypeSystem_c();
    }

}
