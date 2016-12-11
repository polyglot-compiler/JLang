PolyLLVM
========

PolyLLVM adds an LLVM back end to the [Polyglot](https://www.cs.cornell.edu/projects/polyglot/) compiler, translating Java ASTs into LLVM IR.

Since Polyglot translates extended Java code into vanilla Java ASTs, PolyLLVM should be interoperable with other Polyglot extensions by default. However, it also aims to be extensible itself, so that one can write optimized LLVM translations for language extensions when needed.

A user manual and developer guide can be found on the PolyLLVM [website](http://dbd64.github.io/PolyLLVM/).

Navigating the Codebase
-----------------------

- Source files are in the [compiler/src/polyllvm](compiler/src/polyllvm) directory. The [ast](compiler/src/polyllvm/ast) subdirectory contains LLVM IR nodes. Most translation code resides in the [extension](compiler/src/polyllvm/extension) subdirectory.

- The [runtime](runtime) directory contains supporting files such as Java library source files and native code. A Makefile is used to produce LLVM IR from library files.

- The [tests/isolated](tests/isolated) directory contains single-file Java programs, and the [tests/group](tests/group) directory contains multi-file Java programs. The JUnit test suite compiles and runs these programs with both `javac` and `polyllvm`, then compares the results. A Makefile is used to speed up compile times.
