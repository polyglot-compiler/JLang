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

Note: PolyLLVM is not tested on Windows.

Clone the [repo](https://github.com/gharrma/polyllvm) and build:

```
$ git clone https://github.com/gharrma/polyllvm.git
$ cd PolyLLVM
$ ant
```

Please note the dependencies listed in the README.


Compiling Hello World
---------------------

Create a simple `HelloWorld.java` file, printing to stdout using `System.out.println()`. Compile using

```
$ bin/plc HelloWorld.java
```

This will output a file called `HelloWorld.ll`, which will contain human-readable LLVM IR. It will also create an executable `a.out` which you can run directly!

From here you could also use `llvm-link`, `llc`, and `clang++` to link `HelloWorld.ll` with the runtime and create an executable manually.


Using PolyLLVM With Another Extension
-------------------------------------

Coming soon.
