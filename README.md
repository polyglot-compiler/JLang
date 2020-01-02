JLang
========

JLang supports ahead-of-time compilation of Java. It works by adding an LLVM back end to the [Polyglot](https://www.cs.cornell.edu/projects/polyglot/) compiler, allowing Java to be translated down to LLVM IR.
From there, a back end can translate to the architecture of choice.

Since Polyglot already translates extended Java code into vanilla Java ASTs, JLang should be interoperable with other Polyglot extensions by default. However, JLang aims to be extensible itself, so that one can write optimized LLVM translations for language extensions when needed.

A user manual and developer guide can be found on the [JLang website](https://polyglot-compiler.github.io/JLang/).

Contributing
------------

Before contributing, please do the following.

(1) Read through the rest of this README.<br>
(2) Read through all GitHub issues carefully, to get the most up-to-date picture of the current state of the project.<br>
(3) Read through the [developer guide](http://polyglot-compiler.github.io/JLang/developer-guide.html) on the website, to get technical details on the most critical subcomponents of JLang.<br>
(4) If you need to work on compiler translations, get familiar with [LLVM IR](https://llvm.org/docs/LangRef.html).<br>
(5) If you need to work on native runtime code, get familiar with [JNI](https://docs.oracle.com/javase/7/docs/technotes/guides/jni/spec/design.html).

Quick start guide
-----------------

JLang has the following dependencies, which you will need to download and install prior to use.

- [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html) and [Ant](http://ant.apache.org) are required to build the compiler. Ant is best installed through your preferred package manager. Be sure that the `JAVA_HOME` environment variable is defined and points to the JDK 8 installation (e.g., `/Library/Java/JavaVirtualMachines/jdk1.8.0_162.jdk/Contents/Home/`).

- [JDK 7](http://www.oracle.com/technetwork/java/javase/downloads/java-archive-downloads-javase7-521261.html) is required to compile programs with JLang, since we target Java 7. **This specific, linked version of the JDK native code is required to build the JDK source that we have provided with this repo. At the moment, no other distribution of JDK native code is compatible with this source on all platforms**. Also be sure that the `JDK7` environment variable is defined and points to the JDK 7 home directory. When trying to run programs compiled with JLang you will need to set the `JAVA_HOME` environment variable to this value as well (see the test suite Makefile for an example of how to execute JLang-compiled binaries).

- [LLVM](http://llvm.org) and [Clang](https://clang.llvm.org) are needed to build the runtime and create binaries from Java programs. JLang is tested with version 5.0.1, which you can download [here](http://releases.llvm.org/download.html#5.0.1). It may be possible to install through a package manager (e.g., `sudo apt install llvm && sudo apt install clang`). After installation be sure that `llc --version` (for example) and `clang++ --version` report consistent versions. You may have to alter your PATH to pick the right version, especially on a Mac for which a version of `Clang` comes bundled with the command line developer tools. If your clang binary is named `clang++-VERSION`, then you must define its version with the `CLANG_VERSION` environment variable. For example, if you are running `clang++-5.0` then you should set `CLANG_VERSION=5.0`.

- The [Boehm-Demers-Weiser garbage collector](http://www.hboehm.info/gc/) is also required for creating binaries. JLang is tested with version 7.6.4, which you can download [here](http://www.hboehm.info/gc/gc_source/) or install through a package manager (`brew install boehmgc`). A typical install from source looks like this: `./configure && make && make install`. Note that the garbage collector depends on [libatomic_ops](https://github.com/ivmai/libatomic_ops), which is often available through a package manager.

- [Git LFS](https://git-lfs.github.com) is required to use JLang with OpenJDK 7. We use Git LFS to track a zip file containing all OpenJDK 7 Java source files. (The alternative is requiring a checkout and build of the full OpenJDK tree, which is notoriously difficult to configure.) Be sure to do a `git lfs pull` so that `jdk/src.zip` is downloaded; otherwise you will see strange error messages when trying to build.

Note that [Polyglot](https://github.com/polyglot-compiler/polyglot/) is also required, but is tracked as a git submodule and will be built automatically.

Finally, build JLang by running `make` at the top level of the repository. By default this will build only a "bare-bones" JDK, which is enough to run the [unit tests](tests/isolated). Note that JLang is usually tested on OS X; see issue #55 for updates on whether the build system supports Linux.

To run the test-suite you can execute the `make tests` at the top level of the repository. In order to run specific tests, the test Makefile can also be run from the `tests/isolated` directory; however, some environment variables may not be appropriately set depending upon your operating system.

To open this project in IntelliJ, simply open the top level directory of the repository after running `make` once. If you want to run unit tests from IntelliJ, run the `TestAll` class, with the top level of the repository as the working directory and all necessary environment variables. *Caveat*: IntelliJ does not support `$PATH` syntax. You must explicitly write out all paths if you want to append a new one.


High-level project structure
----------------------------

- Compiler source files are in the [compiler/src/jlang](compiler/src/jlang) directory. Most translation code resides in the [extension](compiler/src/jlang/extension) subdirectory.

- [runtime](runtime) contains [Java code](runtime/src) and [native code](runtime/native) needed to implement Java semantics at runtime (think: exceptions, JNI, reflection, etc.).

- [tests/isolated](tests/isolated) contains unit tests for translations from Java to LLVM IR. The JUnit test suite compiles and runs these programs with both `javac` and `jlang`, then compares the results.

- [jdk](jdk) provides a way to compile and link OpenJDK 7 with JLang, and [jdk-lite](jdk-lite) provides a "bare-bones" JDK implementation used for unit testing.

- [docs](docs) contains documentation for the various subcomponents of JLang, in the form of Markdown files. These files also provide the content for the [JLang website](http://polyglot-compiler.github.io/jlang/).

- [lib](lib) contains Polyglot (the frontend for JLang); a fork of [JavaCPP Presets](https://github.com/bytedeco/javacpp-presets) to generate Java stubs from LLVM headers; and various supporting `.jar` files.

- [examples](examples) contains full Java 7 projects which can be compiled with JLang and executed. Currently, we have included only the CUP parser generator, which can be built with the `make cup` command in the top-level JLang directory and is executable with the script [cup.sh](examples/cup/bin/cup.sh).

Status (December 2019)
-----------------

All translations from Java to LLVM IR are complete. This means that all Java 7 language features---expressions, control flow, exceptions, method dispatch, switch statements, try-with-resources, initializer blocks, implicit type conversions, etc.---are translated robustly and as specified by the [JLS](https://docs.oracle.com/javase/specs/jls/se7/html/index.html). 

All unit tests currently pass with OpenJDK 7, except for a number of advanced reflection features, primarily related to generic types.

All other loose ends (minor bugs, build system issues, etc.) are tracked as GitHub issues as well. If you would like to contribute, please read through these tracked issues to find a feature to add or a bug to fix!
