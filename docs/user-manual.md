---
title: "User Manual"
layout: default
---

Contents
--------
{:.no_toc}

* [table of contents goes here]
{:toc}


Installation
------------

Note: JLang is not tested on Windows.

Clone the [repo](https://github.com/polyglot-compiler/JLang) and build using the "Quick start guide" section of the [README](https://github.com/polyglot-compiler/JLang/blob/master/README.md).

Don't forget to set your `JDK7` and `CLANG` environment variables before building!


Compiling Hello World
---------------------

Once you've built JLang and the JDK runtime using `make` in the top-level directory you can test out JLang with a sample program.

Create a simple `HelloWorld.java` file, printing to stdout using `System.out.println()`. Compile using

```
$ bin/jlangc -cp jdk-lite/out/classes HelloWorld.java
```

This will output a file called `HelloWorld.ll`, which will contain human-readable LLVM IR.

Compiling HelloWorld.ll 
--------------------------------------

To compile a `*.ll` file to an executable, it will need to be linked with
1. The compiled OpenJDK java sources (via libjdk)
2. The compiled JLang JVM (via libjvm)
3. The OpenJDK native code found in the OpenJDK shared libraries.

All of the necessary flags for linking these runtimes are specified in the `tests/isolated/Makefile` under the `CLANG_FLAGS` variable; some of these are system dependent and will therefore be defined in a `defs.*` file in the top-level directory of the repo. `defs.Linux` supports Linux configurations and `defs.Darwin` supports OS_X configurations.

Running a Binary
----------------

Once clang++ generates an output binary, your binary only needs one more runtime configuration. The `JAVA_HOME` environment variable must point to the `JDK7` directory. If you currently have Java installed on your machine and don't want to overwrite your system's `JAVA_HOME` variable, you can execute the binary like so:

```
$ JAVA_HOME=$JDK7 bash -c './HelloWorld.binary'
```
