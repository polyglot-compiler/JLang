---
title: "Developer Guide"
layout: default
---

Contents
--------
{:.no_toc}

* [table of contents goes here]
{:toc}

Overview
--------

PolyLLVM is built as an extension to the
[Polyglot](https://www.cs.cornell.edu/projects/polyglot/) compiler. As
PolyLLVM is a backend only, it does not extend the parser, or the type
system built into polyglot. PolyLLVM does add a new set of AST nodes for
the LLVM source tree, and compiler passes for desugaring and translating
the code.

LLVM AST
--------

The LLVM language is described in the [Language
Reference](http://www.llvm.org/docs/LangRef.html). Not all of the LLVM
language is currently used as a translation target, so only the
statements used have associated Polyglot AST nodes. The LLVM language is
extended with ESEQ nodes to allow easier translation, which are removed
in a [compiler pass](#post-translation-passes) after translation. There are four
main kinds of Nodes for the LLVM AST: top level declarations,
statements, expressions, and types.

Top level declarations include a node to represent source files as well
as functions, function declarations, global variable declarations, and
type declarations. This also includes a node LLVMBlock which represents
a basic block of LLVM statements.

Statement nodes represent instructions in LLVM, and must all implement
the `LLVMInstruction` interface.

Expression nodes represent expressions in LLVM, and must all implement
the `LLVMExpr` interface.

Type nodes represent types in LLVM, and must all implement the
`LLVMTypeNode` interface.

Desugaring Passes
-----------------

There are currently 3 desugaring passes implemented as a part of
PolyLLVM: `StringLiteralRemover`, `AddPrimitiveWideningCastsVisitor`,
and `AddVoidReturnVisitor`. The `StringLiteralRemover` pass converts
string literals to explicit constructor calls for the `String` class.
The `AddPrimitiveWideningCastsVisitor` adds explicit casts where the
Java language implicitly casts between two types. The
`AddVoidReturnVisitor` adds an explicit return to the end of void
functions. Theses are polyglot visitors, so the logic for these
transformations is in the extension objects for the Java AST nodes.

Translation Pass
----------------

The translation pass is also implemented as a polyglot visitor, but the
visitor translator maintains additional state for translation. The main
data structure the translator uses is a map from Java `Node` objects to
`LLVMNode` objects. When translating a Java node, the translation for
sub-nodes is retrieved using the `getTranslation` method.

The translator contains contains helper functions to generate necessary code and declarations, including
the necessary type declarations, global variables, and ctor functions. There are helper methods to generate
class and interface layouts.

All local variables are allocated on the stack, as LLVM has a pass to lift allocated variables to registers.
The translator keeps track of allocations and arguments for functions, to generate prologue code to allocate
stack space for all variables with the correct names.

Object Layout
-------------

TODO

Mangling
--------

TODO

Method and Interface Calls
--------------------------

TODO

InstanceOf
----------

TODO

Arrays
------

TODO

Native Code
-----------

TODO

Post Translation Passes
-----------------------

Currently the only post translation pass is the ESEQ removal pass.
