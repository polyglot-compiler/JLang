JLang
========

JLang adds an LLVM back end to the [Polyglot](https://www.cs.cornell.edu/projects/polyglot/) compiler, translating Java down to LLVM IR.

Since Polyglot already translates extended Java code into vanilla Java ASTs, JLang should be interoperable with other Polyglot extensions by default. However, JLang aims to be extensible itself, so that one can write optimized LLVM translations for language extensions when needed.

A user manual and developer guide can be found on the [JLang website](https://dz333.github.io/JLang/).


Contributing
------------

Before contributing, please do the following.

(1) Read through the rest of this README.<br>
(2) Read through all GitHub issues carefully, to get the most up-to-date picture of the current state of the project.<br>
(3) Read through the [developer guide](http://dz333.github.io/JLang/developer-guide.html) on the website, to get technical details on the most critical subcomponents of JLang.<br>
(4) If you need to work on compiler translations, get familiar with [LLVM IR](https://llvm.org/docs/LangRef.html).<br>
(5) If you need to work on native runtime code, get familiar with [JNI](https://docs.oracle.com/javase/7/docs/technotes/guides/jni/spec/design.html).


Quick start guide
-----------------

JLang has the following dependencies, which you will need to download and install prior to use.

- [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html) and [Ant](http://ant.apache.org) are required to build the compiler. Ant is best installed through your preferred package manager. Be sure that the `JAVA_HOME` environment variable is defined and points to the JDK 8 installation (e.g., `/Library/Java/JavaVirtualMachines/jdk1.8.0_162.jdk/Contents/Home/`).

- [JDK 7](http://www.oracle.com/technetwork/java/javase/downloads/java-archive-downloads-javase7-521261.html) is required to compile programs with JLang, since we target Java 7. Be sure that the `JDK7` environment variable is defined and points to the JDK 7 home directory. When trying to run programs compiled with JLang you will need to set the `JAVA_HOME` environment variable to this value as well (see the test suite Makefile for an example of how to execute JLang-compiled binaries).

- [LLVM](http://llvm.org) and [Clang](https://clang.llvm.org) are needed to build the runtime and create binaries from Java programs. JLang is tested with version 5.0.1, which you can download [here](http://releases.llvm.org/download.html#5.0.1). It may be possible to install through a package manager (e.g., `sudo apt install llvm && sudo apt install clang`). After installation be sure that `llc --version` (for example) and `clang++ --version` report consistent versions. You may have to alter your PATH to pick the right version, especially on a Mac for which a version of `Clang` comes bundled with the command line developer tools.

- The [Boehm-Demers-Weiser garbage collector](http://www.hboehm.info/gc/) is also required for creating binaries. JLang is tested with version 7.6.4, which you can download [here](http://www.hboehm.info/gc/gc_source/) or install through a package manager (`brew install boehmgc`). A typical install from source looks like this: `./configure && make && make install`. Note that the garbage collector depends on [libatomic_ops](https://github.com/ivmai/libatomic_ops), which is often available through a package manager.

- [Git LFS](https://git-lfs.github.com) is required to use JLang with OpenJDK 7. We use Git LFS to track a zip file containing all OpenJDK 7 Java source files. (The alternative is requiring a checkout and build of the full OpenJDK tree, which is notoriously difficult to configure.) Be sure to do a `git lfs pull` so that `jdk/src.zip` is downloaded; otherwise you will see strange error messages when trying to build.

Note that [Polyglot](https://github.com/polyglot-compiler/polyglot/) is also required, but is tracked as a git submodule and will be built automatically.

Finally, build JLang by running `make` at the top level of the repository. By default this will build only a "bare-bones" JDK, which is enough to run the [unit tests](tests/isolated). Note that JLang is usually tested on OS X; see issue #55 for updates on whether the build system supports Linux.

If you want to run unit tests from IntelliJ, run the `TestAll` class, using the top level of the repository as the working directory.


High-level project structure
----------------------------

- Compiler source files are in the [compiler/src/jlang](compiler/src/jlang) directory. Most translation code resides in the [extension](compiler/src/jlang/extension) subdirectory.

- [runtime](runtime) contains [Java code](runtime/src) and [native code](runtime/native) needed to implement Java semantics at runtime (think: exceptions, JNI, reflection, etc.).

- [tests/isolated](tests/isolated) contains unit tests for translations from Java to LLVM IR. The JUnit test suite compiles and runs these programs with both `javac` and `jlang`, then compares the results.

- [jdk](jdk) provides a way to compile and link OpenJDK 7 with JLang, and [jdk-lite](jdk-lite) provides a "bare-bones" JDK implementation used for unit testing.

- [docs](docs) contains documentation for the various subcomponents of JLang, in the form of Markdown files. These files also provide the content for the [JLang website](http://dz333.github.io/jlang/).

- [lib](lib) contains Polyglot (the frontend for JLang); a fork of [JavaCPP Presets](https://github.com/bytedeco/javacpp-presets) to generate Java stubs from LLVM headers; and various supporting `.jar` files.


Status (May 2018)
-----------------

All translations from Java to LLVM IR are complete, with the exception of the `synchronized` keyword (see below on concurrency support). This means that all Java 7 language features---expressions, control flow, exceptions, method dispatch, switch statements, try-with-resources, initializer blocks, implicit type conversions, etc.---are translated robustly and as specified by the [JLS](https://docs.oracle.com/javase/specs/jls/se7/html/index.html). All of our [unit tests](tests/isolated) pass.

However, JLang is still a work in progress for two important reasons.

(1) JDK support. The JDK includes thousands of classes that are critical to the Java ecosystem, such as `java.lang.Class`, `java.util.ArrayList`, `java.io.File`, and `java.net.Socket`. Thus it is important to be able to compile programs that rely on the JDK. So far we can fully support a hand-written "bare-bones" JDK that includes only the JDK classes that are necessary for unit tests (`java.lang.Object`, `java.lang.Class`, `java.lang.System`, etc.). This has allowed us to test the compiler before trying to compile the entire JDK. Support for the full JDK is close, but not finished. Please see [issue #54](https://github.com/dz333/JLang/issues/54) for more information.

(2) Concurrency support. Support for multiple threads and synchronization has not been started, as JDK support took priority. JLang will ignore the Java `synchronized` keyword, and the [native runtime code](runtime/native) is generally not thread-safe. Please see [issue #5](https://github.com/dz333/JLang/issues/5) for more information.

All other loose ends (minor bugs, build system issues, etc.) are tracked as GitHub issues as well. If you would like to contribute, please read through all of these tracked issues carefully!

