package polyllvm;

<<<<<<<HEAD

import java.io.Reader;
import java.util.Set;

import polyglot.ast.NodeFactory;
import polyglot.frontend.CupParser;
import polyglot.frontend.Parser;
import polyglot.frontend.Scheduler;
import polyglot.frontend.Source;
import polyglot.lex.Lexer;
import polyglot.main.Options;
import polyglot.types.TypeSystem;
import polyglot.util.ErrorQueue;
import polyllvm.ast.PolyLLVMExtFactory_c;
import polyllvm.ast.PolyLLVMLang_c;
import polyllvm.ast.PolyLLVMNodeFactory_c;
import polyllvm.parse.Grm;
import polyllvm.parse.Lexer_c;
import polyllvm.types.PolyLLVMTypeSystem_c;

/**
 * Extension information for polyllvm extension.
=======
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
>>>>>>> 92481ea628bc4270d08ad51a6decb42ec21671b3
 */
public class ExtensionInfo extends polyglot.frontend.JLExtensionInfo {
    static {
        // force Topics to load
        @SuppressWarnings("unused")
        Topics t = new Topics();
    }

    @Override
    public String defaultFileExtension() {
<<<<<<< HEAD
        return "java";
=======
        return "pll";
>>>>>>> 92481ea628bc4270d08ad51a6decb42ec21671b3
    }

    @Override
    public String compilerName() {
<<<<<<< HEAD
        return "polyllvmc";
=======
        return "PolyLLVMc";
>>>>>>> 92481ea628bc4270d08ad51a6decb42ec21671b3
    }

    @Override
    public Parser parser(Reader reader, Source source, ErrorQueue eq) {
        Lexer lexer = new Lexer_c(reader, source, eq);
        Grm grm = new Grm(lexer, ts, nf, eq);
        return new CupParser(grm, source, eq);
    }

    @Override
    public Set<String> keywords() {
<<<<<<< HEAD
        return new Lexer_c(null).keywords();
=======
	return new Lexer_c(null).keywords();
>>>>>>> 92481ea628bc4270d08ad51a6decb42ec21671b3
    }

    @Override
    protected NodeFactory createNodeFactory() {
<<<<<<< HEAD
        return new PolyLLVMNodeFactory_c(PolyLLVMLang_c.instance,
                                         new PolyLLVMExtFactory_c());
=======
        return new PolyLLVMNodeFactory_c(PolyLLVMLang_c.instance, new PolyLLVMExtFactory_c());
>>>>>>> 92481ea628bc4270d08ad51a6decb42ec21671b3
    }

    @Override
    protected TypeSystem createTypeSystem() {
        return new PolyLLVMTypeSystem_c();
    }

    @Override
    protected Scheduler createScheduler() {
        return new PolyLLVMScheduler(this);
    }

    @Override
    public Options getOptions() {
        // TODO Auto-generated method stub
        Options o = super.getOptions();
        o.output_ext = "ll";
        return o;
    }

//    @Override
//    public polyglot.frontend.ExtensionInfo outputExtensionInfo() {
//        return new LLVMExtensionInfo();
//    }
}
