JDK
===

This directory contains source code for OpenJDK 7, and a build system for compiling the JDK with JLang to produce a shared library.

The OpenJDK codebase is available directly from the [openjdk](http://openjdk.java.net/guide/repositories.html#clone) website, or from one of several git clones such as [this one](https://github.com/dmlloyd/openjdk). However, many JDK Java source files are script-generated, and the build system for JDK 7 is notoriously difficult to configure. So for convenience we track a zip file that contains all JDK sources, prebuilt. This zip file was obtained from [here](https://sourceforge.net/projects/jdk7src/).

Building
--------

The Makefile in this directory will:
(1) Extract JDK source files from the tracked zip file.<br>
(2) Apply JLang-specific patches to work around a few unsupported features such as reflection and threads. These patches should be removed as the corresponding features are implemented.<br>
(3) Use JLang to compile the JDK Java source files down to LLVM IR.<br>
(4) Use clang to compile LLVM IR down to object files.<br>
(5) Use clang to link object files from the JDK into a shared library.

Notes
-----

- In addition to direct source patches, there is also a file called `jdk-method-filter.txt`, which JLang uses to filter out about a dozen methods and field initializers that cause problems due to differences between JLang and javac. The file has a comment for each method explaining why the method causes issues. This takes advantage of Polyglot's `-method-filter` flag.

- At the time of writing, we use the `Main.java` file here in order to compile only the slice of the JDK that is required for a Hello World program. This slice is quite large (1000+ files), and includes many of the most important JDK classes.
