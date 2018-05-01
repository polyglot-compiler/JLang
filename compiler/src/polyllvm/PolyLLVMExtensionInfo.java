package polyllvm;

import polyglot.ast.NodeFactory;
import polyglot.ext.jl5.ast.JL5ExtFactory_c;
import polyglot.ext.jl7.JL7ExtensionInfo;
import polyglot.ext.jl7.ast.JL7ExtFactory_c;
import polyglot.filemanager.ExtFileManager;
import polyglot.filemanager.FileManager;
import polyglot.frontend.Scheduler;
import polyglot.main.Options;
import polyglot.main.Version;
import polyglot.types.TypeSystem;
import polyglot.util.StringUtil;
import polyllvm.ast.PolyLLVMExtFactory_c;
import polyllvm.ast.PolyLLVMLang_c;
import polyllvm.ast.PolyLLVMNodeFactory_c;
import polyllvm.types.PolyLLVMTypeSystem_c;

import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import java.io.IOException;

/**
 * Extension information for polyllvm extension.
 */
public class PolyLLVMExtensionInfo extends JL7ExtensionInfo {
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
    public String[] defaultFileExtensions() {
        String ext = defaultFileExtension();
        return new String[] { ext };
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
    protected TypeSystem createTypeSystem() {
        return new PolyLLVMTypeSystem_c();
    }

    @Override
    public Scheduler createScheduler() {
        return new PolyLLVMScheduler(this);
    }

    @Override
    protected Options createOptions() {
        return new PolyLLVMOptions(this);
    }

    @Override
    public Version version() {
        return new polyllvm.Version();
    }

    @Override
    public FileManager createFileManager() {
        return new ExtFileManager(this) {
            @Override
            public JavaFileObject getJavaFileForInput(
                    Location location, String className, Kind kind) throws IOException {
                // Explicitly avoid using reflection---as
                // super.getJavaFileForInput sometimes does---because
                // Polyglot runs with JDK 8, but compiles Java 7 code.
                // XXX This is a workaround; there may be a cleaner solution.
                String pkg = StringUtil.getPackageComponent(className);
                String name = StringUtil.getShortNameComponent(className);
                String relativeName = name + kind.extension;
                return (JavaFileObject) getFileForInput(location, pkg, relativeName);
            }
        };
    }
}
