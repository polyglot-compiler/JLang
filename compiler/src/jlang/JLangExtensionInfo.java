//Copyright (C) 2017 Cornell University

package jlang;

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

import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import jlang.ast.JLangExtFactory_c;
import jlang.ast.JLangLang_c;
import jlang.ast.JLangNodeFactory_c;
import jlang.types.JLangTypeSystem_c;

import java.io.IOException;

/**
 * Extension information for JLang extension.
 */
public class JLangExtensionInfo extends JL7ExtensionInfo {
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
        return "jlangc";
    }

    @Override
    protected NodeFactory createNodeFactory() {
        return new JLangNodeFactory_c(JLangLang_c.instance,
                new JLangExtFactory_c(
                        new JL7ExtFactory_c(
                                new JL5ExtFactory_c())));
    }
    
    @Override
    protected TypeSystem createTypeSystem() {
        return new JLangTypeSystem_c();
    }

    @Override
    public Scheduler createScheduler() {
        return new JLangScheduler(this);
    }

    @Override
    protected Options createOptions() {
        return new JLangOptions(this);
    }

    @Override
    public Version version() {
        return new jlang.Version();
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
