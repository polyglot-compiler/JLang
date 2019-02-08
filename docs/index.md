---
title: "An LLVM backend for the Polyglot compiler"
layout: default
---

Overview
--------

JLang adds an LLVM backend to the [Polyglot](https://www.cs.cornell.edu/projects/polyglot/) compiler, translating Java into LLVM IR. See the [Getting Started Guide](user-manual.html) to try it out, and the [developer guide](developer-guide.html) for an overview of the codebase.

JLang supports Java 7, as specified by the [JLS](https://docs.oracle.com/javase/specs/jls/se7/html/index.html). Since Polyglot additionally supports Java language extensions, and since it can generally translate these extensions down to vanilla Java, JLang should be interoperable with other Polyglot extensions by default. However, JLang also aims to be extensible, so that one can write direct translations to LLVM for language extensions when needed.

To be more concrete, here's what Polyglot + JLang will allow you to do:

(1) Translate Java source files (`.java`) down to LLVM IR (`.ll`), and then from there down to object files (`.o`) using the the [LLVM](https://llvm.org) toolchain.

(2) Link object files together along with JLang-compiled [OpenJDK](http://openjdk.java.net/projects/jdk7/) classes to create a standalone executable. JLang implements a runtime to provide the JVM functionality that the JDK expects, such as reflection and cross-language method calls. Native code in the JDK (e.g., low-level networking and I/O code) can be linked directly from your local JDK installation.

(3) (Optional) Extend Java with custom language features or type annotations using Polyglot, and either translate these features back into vanilla Java, or write a direct translation into LLVM IR by extending JLang.

Status
------

JLang currently supports all Java 7 language features, except concurrency and some reflection support. This includes expressions, control flow, exceptions, method dispatch, switch statements, try-with-resources, initializer blocks, implicit type conversions, etc. OpenJDK 7 support is still a work in progress but enough is supported for many standard use cases (such as printing and accessing files).

All unit tests currently pass with OpenJDK 7, except for a number of advanced reflection features, primarily related to generics and array types.

See the [README](https://github.com/polyglot-compiler/JLang) in the repository for the most up-to-date status.


Contributing
------------
If you would like to contribute to JLang, you should fork the repo and create a new branch whose name is related to the [issue](https://github.com/polyglot-compiler/JLang/issues) that you would like to address. You can submit a pull-request in order to have your changes mreged.

If you are interested in becoming more involved in JLang as, please follow and communicate via the <a href="mailto:{{ site.contact }}">jlang users list </a> for more information, discussion and updates. To join the mailing list, send a request e-mail using this link and no e-mail body: <a href="mailto:{{ site.contact-join }}?subject=join">Join JLang</a>.

