---
title: "User Manual"
layout: default
---

Contents
=============
{:.no_toc}

* [table of contents goes here]
{:toc}


Installation
=============

Note: _JLang is not yet tested or built for use on Windows._

Clone the [repo](https://github.com/polyglot-compiler/JLang) and build using the "Quick start guide" section of the [README](https://github.com/polyglot-compiler/JLang/blob/master/README.md).

Dependencies
----------------
There are a fair number of dependencies you'll need to install before building JLang:
> * ANT - Polyglot and JLang are built with ant `build.xml` specifications.
> * JDK native libraries - Requires a manual download from [Oracle's](https://www.oracle.com/technetwork/java/javase/downloads/java-archive-downloads-javase7-521261.html) website. (Select Java SE Development Kit 7u80 for your target platform).
> * LLVM + Clang 5.0 - Used to compile the runtime C++ and make executables from *.ll files.
> * Boehm-Demers-Weiser garbage collector - This implements garbage collection for LLVM and must be linked with the generated code.
> * Git LFS - used to download the JDK7 java source which is distributed with this repo.

More details on how to install all of these dependencies are included in the [README](https://github.com/polyglot-compiler/JLang/blob/master/README.md).

Configuration
-----------------


Below is a list of the environment variables used by JLang (an `N/a` default means it is not optional):

Var | Default | Meaning
----|---------|----------
JDK7 | N/a | The absolute path pointing to your JDK7 installation (e.g. /usr/lib/jvm/jdk1.7.0_80/)
JDK | jdk | Which set of JDK java libraries to compile. For small unit tests that don't require the full openjdk, use `jdk-lite`.
CLANG_VERSION |  | If you have multiple versions of clang/llvm installed, this can be used to select them. CLANG_VERSION=5.0 means that we will look for the executables `clang++-5.0` and  `llc-5.0` instead of `clang++` and `llc`.


Building JLang
------------------

To build the JLang compiler and runtime, simply type `make` from the top-level directory!

```
$ /home/user/JLang> printf "$JDK7\n"
/usr/lib/jvm/jdk1.7.0_80/
$ /home/user/JLang> printf "$JDK\n"
jdk
$ /home/user/JLang> printf "$CLANG_VERSION\n"
5.0
$ /home/user/JLang> make
--- Checking setup ---
+ JDK 7 found
+ git lfs installed
+ LLVM version is up to date: 5.0.0
Setup looks good
--- Building Polyglot ---
...

--- Building compiler ---
...

--- Building jdk classes ---
...

--- Building runtime ---
...

--- Building jdk ---
...
$ /home/user/JLang>
```

Before running the build, your configuration will be checked for correctness.
If you get an error message, be sure the appropriate dependency is installed (and on your shell's `PATH`) and your environment variables are correctly configured.

Running Example Programs
=======================

Compiling the Test Suite
---------------------

You can run the `make tests` command from the *top-level directory* to run our unit test suite, which will report all failed unit tests as either expected or regressions.

You should see an output similar to the following

```
$ /home/user/JLang> make tests
...
...
Generating output for Unary
Generating output for UnaryOperations
Generating output for VarArgs
******** CHECKING CORRECTNESS *********
==============
No regressions found!
==============
==============
==============
The following expected failures occurred:
FieldReflection
MethodReflection
ThreadingTest
$ /home/user/JLang>
```

Compiling the CUP Parser Generator Example
------------------

In the `examples/cup` directory we have included a distribution of the source code for the [CUP](http://www2.cs.tum.edu/projects/cup/) parser generator.

You can build CUP from source with JLang using the `make cup` command from the *top-level* directory.

```
$ /home/user/JLang> make cup
...
--- Building the CUP Parser Generator ---
Compiling Java file(s) down to LLVM IR
Creating CUP executable
Compiling Java runtime to class files
Note: Some input files use unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.
Compiling LLVM IR to Objects for Lib Creation
Compiling out/java_cup/runtime/virtual_parse_stack.ll
Compiling out/java_cup/runtime/lr_parser.ll
Compiling out/java_cup/runtime/ComplexSymbolFactory.ll
Compiling out/java_cup/runtime/Scanner.ll
Compiling out/java_cup/runtime/SymbolFactory.ll
Compiling out/java_cup/runtime/Symbol.ll
Compiling out/java_cup/runtime/DefaultSymbolFactory.ll
Compiling Cup Java runtime files to libcup
$ /home/user/JLang>
```

This will build both a CUP executable (accessible via the `examples/cup/bin/cup.sh` script) and a CUP runtime library which can be used by JLang generated code.

Once you've compiled CUP you can try out any of the example languages supplied with the distribution in `examples/cup/testgrammars` or `examples/cup/test`.

```
$ /home/user/JLang> cd examples/cup/test
$ /home/user/JLang/examples/cup/test> ../bin/cup.sh example.cup
------- CUP v0.11b 20160615 (GIT 3d0ae71) Parser Generation Summary -------
0 errors and 0 warnings
10 terminals, 2 non-terminals, and 9 productions declared,
producing 19 unique parse states.
0 terminals declared but not used.
0 non-terminals declared but not used.
0 productions never reduced.
0 conflicts detected (0 expected).
Code written to "parser.java", and "sym.java".
---------------------------------------------------- (CUP v0.11b 20160615 (GIT 3d0ae71))
$ /home/user/JLang/examples/cup/test> ls
example.cup parser.java sym.java
$ /home/user/JLang/examples/cup/test>
```

Hello World with JLang
=================

Compiling Hello World.java
----------------------

Once you've built JLang and the JDK runtime using `make` in the top-level directory you can test out JLang with a sample program.

Create a simple `HelloWorld.java` file, printing to stdout using `System.out.println()`. Compile using

```
$ /home/user/JLang> ./bin/jlangc -cp "$JDK"/out/classes HelloWorld.java
```

This will generate a file called `HelloWorld.ll`, which contains 'human-readable' LLVM IR.

Compiling HelloWorld.ll
-------------------

To compile a `*.ll` file to an executable, it will need to be linked with
1. The compiled OpenJDK java sources (via libjdk)
2. The compiled JLang JVM (via libjvm)
3. The OpenJDK native code found in the OpenJDK shared libraries.

We have provided a script for compiling with the appropriate linking flags in `bin/compile_ll.sh`.

```
$ /home/user/JLang> ./bin/compile_ll.sh Hello.ll
Wrote compiled binary to Hello.o
$ /home/user/JLang>
```

Running Hello World.o
-------------

Once clang++ generates an output binary, your binary only needs one more runtime configuration. The `JAVA_HOME` environment variable must point to the `JDK7` directory. If you currently have Java installed on your machine and don't want to overwrite your system's `JAVA_HOME` variable, you can execute the binary like so:

```
$ /home/user/JLang> JAVA_HOME=$JDK7 bash -c './HelloWorld.o'
```

We have provided a simple wrapper script which will do this for you in `bin/execute.sh`

```
$ /home/user/JLang> ./bin/execute.sh Hello.o
hello world!
$ /home/user/JLang>
```

Building Larger Projects With JLang
===================================

In general, building larger projects which are distributed over more source files requires a slightly different build process.

Compiling *.java files
---------------------
JLang requires the user to specify a single entry point since that will become the final top-level LLVM module and executable.
If only 1 source file is provided, such as in the [HelloWorld](#hello-world-with-jlang) example, then that java file's `static main` method will be used as the entry point.

In the case where multiple source files are present, you can use the `--entry-point` option to specify the entry class by full class name. The `-sourcepath` option can be used to search for any other *.java files required to compile the main class. If `--entry-point` is not specified, then JLang will not search for other *.java files.

For example, the following command could be used to compile a large number of java classes into a single application:
```
$ /home/user/JLang/example_app> ../bin/jlangc -cp ../"$JDK"/out/classes -sourcepath src -d out --entry-point org.startup.app.Main src/org/startup/app/Main.java
```

In this command,

* `-cp ../"$JDK"/out/classes` specifies all of the class files which the compiler may assume already exist as a JLang library. In this case, we are just speciyfying the JDK on our classpath.
* The `-sourcepath src` option says to start looking for *.java files under the `src` directory.
* The `-d out` specifies that the output *.ll files should be placed in the `out` directory.
* `--entry-point` says that the file in `src/org/startup/app/Main.java` is the entry point (as per java package & path naming convention)

Compiling *.ll files
---------------------

The `compile_ll.sh` script provided in the `bin` directory can still be used to compile many \*.ll files into a single executable.
As long as your classpath is not dependent upon any pre-built libraries *other than the JDK*, you can simply run this script with all of the \*.ll files produced by JLang as the arguments.

From our example above:
```
$ /home/user/JLang/example_app> find out -name "*.ll" | xargs ../bin/compile_ll.sh AppExec
Wrote compiled binary to AppExec.o
$ /home/user/JLang/example_app>
```

The compilation script's first argument is the name of the executable to produce and all other arguments are the list of *.ll files to compile. It expects exactly 1 of these files to be a "top-level" module (which should be `out/org/startup/app/Main.ll` in our example).

Running MyApp
------------

Executing this app should be exactly the same as the HelloWorld case! Nothing changes between single file and large project compilations at this phase.

```
$ /home/user/JLang/example_app> ../bin/execute.sh AppExec.o arg1 arg2 -o1 optarg...
...
PROFIT
$ /home/user/JLang/example_app>
```


Building Shared Libraries with JLang
==================================
Documentation coming soon

(TLDR; compile each *.ll file into a *.o file individually and then use the `-shared` flag for clang to export them as a single shared library)