PolyLLVM
========

PolyLLVM adds an LLVM back end to the [Polyglot](https://www.cs.cornell.edu/projects/polyglot/) compiler, translating Java ASTs into LLVM IR.

Since Polyglot translates extended Java code into vanilla Java ASTs, PolyLLVM should be interoperable with other Polyglot extensions by default. However, it also aims to be extensible itself, so that one can write optimized LLVM translations for language extensions when needed.

A user manual and developer guide can be found on the PolyLLVM [website](http://dbd64.github.io/PolyLLVM/).


Quick Start Guide
-----------------

PolyLLVM has the following dependencies, which you will need to download and install prior to use.

- [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html) and [Ant](http://ant.apache.org) are required to build the compiler. Ant is best installed through your preferred package manager. Be sure that JAVA_HOME is defined and points to the JDK 8 installation (e.g., `/Library/Java/JavaVirtualMachines/jdk1.8.0_162.jdk/Contents/Home/`).

- [LLVM](http://llvm.org) and [Clang](https://clang.llvm.org) are needed to build the runtime and create binaries from Java programs. PolyLLVM is tested with version 5.0.1, which you can download [here](http://releases.llvm.org/download.html#5.0.1). It may be possible to install through a package manager (e.g., `sudo apt install llvm && sudo apt install clang`). After installation be sure that `llc --version` (for example) and `clang++ --version` report consistent versions. You may have to alter your PATH pick to the right version, especially on a Mac for which a version of `Clang` comes preinstalled with the command line developer tools.

- The [Boehm-Demers-Weiser garbage collector](http://www.hboehm.info/gc/) is also required for creating binaries. PolyLLVM is tested with version 7.6.0, which you can download [here](http://www.hboehm.info/gc/gc_source/) or install through a package manager. A typical install from source looks like this: `./configure && make && make install`. Note that the garbage collector depends on [libatomic_ops](https://github.com/ivmai/libatomic_ops), which is often available through a package manager.

- [Polyglot](https://github.com/polyglot-compiler/polyglot/) is the required front end for PolyLLVM, and exists as a git submodule. Run `git submodule init` and then `git submodule update` at the top level of the repository. To build: `cd lib/polyglot && ant && ant jar`.

Finally, build PolyLLVM by running `ant` at the top level of the repository.


Navigating the Codebase
-----------------------

- Source files are in the [compiler/src/polyllvm](compiler/src/polyllvm) directory. The [ast](compiler/src/polyllvm/ast) subdirectory contains LLVM IR nodes. Most translation code resides in the [extension](compiler/src/polyllvm/extension) subdirectory.

- The [runtime](runtime) directory contains supporting files such as Java library source files and native code. A Makefile is used to produce LLVM IR from library files.

- The [tests/isolated](tests/isolated) directory contains single-file Java programs, and the [tests/group](tests/group) directory contains multi-file Java programs. The JUnit test suite compiles and runs these programs with both `javac` and `polyllvm`, then compares the results. A Makefile is used to speed up compile times.
