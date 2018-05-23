# Indicates to sub-Makefiles that it was invoked by this one.
export TOP_LEVEL_MAKEFILE_INVOKED := true

# PolyLLVM.
export PLC := $(realpath bin/polyllvmc)

# Hack to allow dependency on PolyLLVM source.
export PLC_SRC := $(realpath $(shell find compiler/src -name "*.java"))

# System JDK.
ifndef JDK7
$(error Please point the JDK7 environment variable to your system JDK 7)
endif
export JDK7 := $(realpath $(JDK7))
export JAVAC := $(JDK7)/bin/javac
export JNI_INCLUDES := \
	-I"$(JDK7)/include" \
	-I"$(JDK7)/include/darwin" \
	-I"$(JDK7)/include/linux"

# PolyLLVM JDK.
# Use the bare-bones JDK by default.
JDK ?= jdk-lite
export JDK := $(realpath $(JDK))
export JDK_CLASSES := $(JDK)/out/classes

# PolyLLVM runtime.
export RUNTIME := $(realpath runtime)
export RUNTIME_CLASSES := $(RUNTIME)/out/classes

# Clang.
export CLANG := clang++


all: compiler runtime jdk

# Compiler.
compiler:
	@echo "--- Building compiler ---"
	@ant -S
	@echo

runtime: compiler jdk-classes
	@echo "--- Building runtime ---"
	@$(MAKE) -s -C $(RUNTIME)
	@echo

jdk-classes:
	@echo "--- Building $(notdir $(JDK)) classes ---"
	@$(MAKE) -s -C $(JDK) classes
	@echo

jdk: compiler runtime
	@echo "--- Building $(notdir $(JDK)) ---"
	@$(MAKE) -s -C $(JDK)
	@echo

clean:
	@echo "Cleaning compiler, runtime, and jdk"
	@ant -q -S clean
	@$(MAKE) -s -C $(RUNTIME) clean
	@$(MAKE) -s -C $(JDK) clean

.PHONY: compiler runtime jdk-classes jdk
