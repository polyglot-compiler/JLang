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

Clone the [repo](https://github.com/dbd64/PolyLLVM) and build:

```
$ git clone https://github.com/dbd64/PolyLLVM.git
$ cd PolyLLVM
$ ant
```

Please note the dependencies listed in the README.


Compiling Hello World
---------------------

Create a simple `HelloWorld.java` file, printing to stdout using `placeholder.Print.println()`. (`System.out` and other library classes are not yet available.) Compile using

```
$ bin/polyllvmc HelloWorld.java
```

This will output a file called `HelloWorld.ll`, which will contain human-readable LLVM IR. From here you can use `llvm-link`, `llc`, and `clang++` to link `HelloWorld.ll` with the runtime and create an executable. For convenience you can execute

```
$ bin/link HelloWorld.ll
```

which creates an assembly file `HelloWorld.s` and an executable file `HelloWorld.binary`.


Using PolyLLVM With Another Extension
-------------------------------------

Coming soon.
