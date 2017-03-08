package polyllvm;

import polyglot.ast.NodeFactory;
import polyglot.ext.jl5.ast.JL5ExtFactory_c;
import polyglot.ext.jl7.JL7ExtensionInfo;
import polyglot.ext.jl7.ast.JL7ExtFactory_c;
import polyglot.frontend.Scheduler;
import polyglot.main.Options;
import polyllvm.ast.PolyLLVMExtFactory_c;
import polyllvm.ast.PolyLLVMLang_c;
import polyllvm.ast.PolyLLVMNodeFactory_c;

/**
 * Extension information for polyllvm extension.
 */
public class ExtensionInfo extends JL7ExtensionInfo {
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
    protected NodeFactory createNodeFactory() {
        return new PolyLLVMNodeFactory_c(PolyLLVMLang_c.instance,
                new PolyLLVMExtFactory_c(
                        new JL7ExtFactory_c(
                                new JL5ExtFactory_c())));
    }

    @Override
    public Scheduler createScheduler() {
        return new PolyLLVMScheduler(this);
    }

    @Override
    public Options getOptions() {
        Options o = super.getOptions();
        o.output_ext = "ll";
        return o;
    }
}
