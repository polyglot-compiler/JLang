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

Clone the [repo](https://github.com/dz333/JLang) and build using the "Quick start guide" section of the [README](https://github.com/dz333/JLang/blob/master/README.md).


Compiling Hello World
---------------------

Create a simple `HelloWorld.java` file, printing to stdout using `System.out.println()`. Compile using

```
$ bin/jlangc -cp jdk-lite/out/classes HelloWorld.java
```

This will output a file called `HelloWorld.ll`, which will contain human-readable LLVM IR.

From here you could also use `clang++` to link `HelloWorld.ll` with the runtime and create an executable manually. See the Makefile in the `tests/isolated` directory for an example of how to do this.
