PolyLLVM
========

PolyLLVM adds an LLVM back end to the [Polyglot](https://www.cs.cornell.edu/projects/polyglot/) compiler, translating Java down to LLVM IR.

Since Polyglot already translates extended Java code into vanilla Java ASTs, PolyLLVM should be interoperable with other Polyglot extensions by default. However, PolyLLVM aims to be extensible itself, so that one can write optimized LLVM translations for language extensions when needed.

A user manual and developer guide can be found on the PolyLLVM [website](http://gharrma.github.io/polyllvm/).


Quick start guide
-----------------

PolyLLVM has the following dependencies, which you will need to download and install prior to use.

- [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html) and [Ant](http://ant.apache.org) are required to build the compiler. Ant is best installed through your preferred package manager. Be sure that JAVA_HOME is defined and points to the JDK 8 installation (e.g., `/Library/Java/JavaVirtualMachines/jdk1.8.0_162.jdk/Contents/Home/`).

- [LLVM](http://llvm.org) and [Clang](https://clang.llvm.org) are needed to build the runtime and create binaries from Java programs. PolyLLVM is tested with version 5.0.1, which you can download [here](http://releases.llvm.org/download.html#5.0.1). It may be possible to install through a package manager (e.g., `sudo apt install llvm && sudo apt install clang`). After installation be sure that `llc --version` (for example) and `clang++ --version` report consistent versions. You may have to alter your PATH to pick the right version, especially on a Mac for which a version of `Clang` comes bundled with the command line developer tools.

- The [Boehm-Demers-Weiser garbage collector](http://www.hboehm.info/gc/) is also required for creating binaries. PolyLLVM is tested with version 7.6.4, which you can download [here](http://www.hboehm.info/gc/gc_source/) or install through a package manager. A typical install from source looks like this: `./configure && make && make install`. Note that the garbage collector depends on [libatomic_ops](https://github.com/ivmai/libatomic_ops), which is often available through a package manager.

- [Polyglot](https://github.com/polyglot-compiler/polyglot/) is the required front end for PolyLLVM, and exists as a git submodule. Run `git submodule init` and then `git submodule update` at the top level of the repository. To build: `cd lib/polyglot && ant && ant jar`.

- [Git LFS](https://git-lfs.github.com) is required to use PolyLLVM with OpenJDK 7. We use Git LFS to track a zip file containing all OpenJDK 7 Java source files. The alternative is requiring a checkout and build of the full OpenJDK tree, which is notoriously difficult to configure.

Finally, build PolyLLVM by running `ant` at the top level of the repository.

If you want to run unit tests from IntelliJ, run the `TestAll` class, using the top level of the repository as the working directory.


Status (May 2018)
-----------------

All translations from Java to LLVM IR are complete, with the exception of the `synchronized` keyword (see below on concurrency support). This means that all Java 7 language features---expressions, control flow, exceptions, method dispatch, switch statements, try-with-resources, initializer blocks, implicit type conversions, etc.---are translated robustly and as specified by the [JLS](https://docs.oracle.com/javase/specs/jls/se7/html/index.html). All of our [unit tests](tests/isolated) pass.

However, PolyLLVM is still a work in progress for two important reasons.

(1) JDK support. The JDK includes thousands of classes that are critical to the Java ecosystem, such as `java.lang.Class`, `java.util.ArrayList`, `java.io.File`, and `java.net.Socket`. Thus it is important to be able to compile programs that rely on the JDK. So far we can fully support a hand-written "bare-bones" JDK that includes only the JDK classes that are necessary for unit tests (`java.lang.Object`, `java.lang.Class`, `java.lang.System`, etc.). This has allowed us to test the compiler before trying to compile the entire JDK. Support for the full JDK is close, but not finished. Please see issue #54 for more information.

(2) Concurrency support. Support for multiple threads and synchronization has not been started, as JDK support took priority. PolyLLVM will ignore the Java `synchronized` keyword, and the [native runtime code](runtime/native) is generally not thread-safe. Please see issue #5 for more information.

All other loose ends (minor bugs, build system issues, etc.) are tracked as GitHub issues as well.


Project structure
-----------------

- Source files are in the [compiler/src/polyllvm](compiler/src/polyllvm) directory. Most translation code resides in the [extension](compiler/src/polyllvm/extension) subdirectory.

- The [runtime](runtime) directory contains Java code and native code needed to implement Java semantics at runtime (think: exceptions, JNI, reflection, etc.).

- The [tests/isolated](tests/isolated) directory contains unit tests for translations from Java to LLVM IR. The JUnit test suite compiles and runs these programs with both `javac` and `polyllvm`, then compares the results.
