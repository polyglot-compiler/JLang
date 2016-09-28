Functional Tests
================

The [tests/functional-isolated](../tests/functional-isolated) directory contains single-file Java programs, and the [tests/functional-group](../tests/functional-group) directory contains multi-file Java programs. The JUnit test suite will compile and run these programs with both `javac` and `polyllvm`, then compare the results. A Makefile is used to speed up compile times (particularly for `javac`, since `javac` will not change often).
