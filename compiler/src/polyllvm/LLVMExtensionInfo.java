package polyllvm;

import java.io.Reader;
import java.util.Set;

import polyglot.ast.NodeFactory;
import polyglot.frontend.Parser;
import polyglot.frontend.Scheduler;
import polyglot.frontend.Source;
import polyglot.types.TypeSystem;
import polyglot.util.ErrorQueue;
import polyllvm.ast.PolyLLVMExtFactory_c;
import polyllvm.ast.PolyLLVMLang_c;
import polyllvm.ast.PolyLLVMNodeFactory_c;
import polyllvm.parse.Lexer_c;
import polyllvm.types.PolyLLVMTypeSystem_c;

/**
 * Extension information for polyllvm extension.
 */
public class LLVMExtensionInfo extends polyglot.frontend.JLExtensionInfo {
    static {
        // force Topics to load
        @SuppressWarnings("unused")
        Topics t = new Topics();
    }

    @Override
    public String defaultFileExtension() {
        return "ll";
    }

    @Override
    public String compilerName() {
        return "polyllvmc";
    }

    @Override
    public Parser parser(Reader reader, Source source, ErrorQueue eq) {
        throw new UnsupportedOperationException("There is no LLVM parser implemented");
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
        return new LLVMScheduler(this);
    }

}
