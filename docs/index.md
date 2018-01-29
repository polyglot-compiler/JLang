---
title: "An LLVM backend for the Polyglot compiler"
layout: default
---

Overview
--------

PolyLLVM adds an LLVM backend to the [Polyglot](https://www.cs.cornell.edu/projects/polyglot/) compiler, translating Java ASTs into LLVM IR. See the [user manual](user-manual.html) to try it out, and the [developer guide](developer-guide.html) for an overview of the codebase.

Since Polyglot translates extended Java code into vanilla Java ASTs, PolyLLVM should be interoperable with other Polyglot extensions by default. However, it also aims to be extensible itself, so that one can write optimized LLVM translations for language extensions when needed.

The backend is in development for Java 1.4, with future support planned for the most recent version of Java. The project covers the key features of Java for ahead of time compilation and static linking of programs. It provides an easy way to integrate projects with LLVM code, and allows for the full suite of LLVM tools to be used.

Supported Features
------------------

The project currently supports the basic arithmetic operations, variables, loops, arrays, class dispatch, interface dispatch, and instanceof checks. We are currently working on implementing threading and exception support.

We do not currently plan to support dynamic class loading or reflection.
