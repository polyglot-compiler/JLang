package polyllvm;

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

import java.io.Reader;
import java.util.Set;

/**
 * Extension information for polyllvm extension.
 */
public class ExtensionInfo extends polyglot.frontend.JLExtensionInfo {
    static {
        // force Topics to load
        @SuppressWarnings("unused")
        Topics t = new Topics();
    }

    @Override
    public String defaultFileExtension() {
        return "java";
    }

    @Override
    public String compilerName() {
        return "polyllvmc";
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
        return new PolyLLVMNodeFactory_c(PolyLLVMLang_c.instance,
                                         new PolyLLVMExtFactory_c());
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
