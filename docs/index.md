---
title: "An LLVM backend for the Polyglot compiler"
layout: default
---

Overview
--------

PolyLLVM adds an LLVM backend to the [Polyglot](https://www.cs.cornell.edu/projects/polyglot/) compiler, translating Java ASTs into LLVM IR. See the [user manual](user-manual.html) to try it out, and the [developer guide](developer-guide.html) for an overview of the codebase.

Since Polyglot translates extended Java code into vanilla Java ASTs, PolyLLVM should be interoperable with other Polyglot extensions by default. However, it also aims to be extensible itself, so that one can write optimized LLVM translations for language extensions when needed.

To be more concrete, here's what Polyglot + PolyLLVM will allow you to do:

(1) (Optional) Extend Java with custom language features or type annotations using Polyglot, and write a pass to translate these features back into standard Java.
(2) Translate Java source files (`.java`) down to LLVM IR (`.ll`), and then from there down to object files (`.o`) using the the [LLVM](https://llvm.org) toolchain.
(3) Link object files together along with PolyLLVM-compiled [OpenJDK](http://openjdk.java.net/projects/jdk7/) classes to create a standalone executable. PolyLLVM implements a runtime to provide the JVM functionality that the JDK expects, such as reflection and cross-language method calls.

Status
------

PolyLLVM currently supports all Java 7 language features, minus concurrency support. This includes expressions, control flow, exceptions, method dispatch, switch statements, try-with-resources, initializer blocks, implicit type conversions, etc. OpenJDK 7 support is still a work in progress.

See the [README](https://github.com/gharrma/polyllvm) in the repository for the most up-to-date status.
