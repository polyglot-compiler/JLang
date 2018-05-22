SRC := src
OUT := out
CLASSES := $(OUT)/classes

CLANG := clang++
PLC := ../bin/polyllvmc
JDK7 ?= /Library/Java/JavaVirtualMachines/jdk1.7.0_80.jdk/Contents/Home
JAVAC := $(JDK7)/bin/javac

# Hack to allow dependency on PolyLLVM code.
PLC_SRC := $(shell find ../compiler/src -name "*.java")

JAVA_SRC := $(shell find src -name "*.java")
JAVA_CLS := $(JAVA_SRC:$(SRC)/%.java=$(CLASSES)/%.class)
JAVA_LL := $(JAVA_SRC:$(SRC)/%.java=$(OUT)/%.ll)
JAVA_OBJ := $(JAVA_LL:%.ll=%.o)

NATIVE_SRC := $(shell find native -name "*.cpp")
NATIVE_OBJ := $(NATIVE_SRC:%.cpp=$(OUT)/%.o)

ALL_OBJ := $(JAVA_OBJ) $(NATIVE_OBJ)

LIBJVM := ../runtime/out/libjvm.dylib
LIBJDK := $(OUT)/libjdk.dylib

PLC_FLAGS := -assert

JNI_INCLUDES := \
	-I"$(JDK7)/include" \
	-I"$(JDK7)/include/darwin" \
	-I"$(JDK7)/include/linux"

JAVA_FLAGS := -glldb -Wno-override-module
NATIVE_FLAGS := -glldb -std=c++14 $(JNI_INCLUDES) -Wall -pthread
LINK_FLAGS := -glldb -lgc

all: $(LIBJDK)

# Runtime Java code (.java --> .class)
$(JAVA_CLS): $(JAVA_SRC)
	@echo "Compiling $(words $(JAVA_SRC)) Java files down to Java bytecode"
	@mkdir -p $(CLASSES)
	@$(JAVAC) -d $(CLASSES) $(JAVA_SRC)

# Compile JDK Java code (.java --> .ll)
$(JAVA_LL): $(JAVA_CLS) $(PLC_SRC)
	@echo "Compiling $(words $(JAVA_SRC)) Java files down to LLVM IR"
	@# We compile java.lang.Object separately, since otherwise
	@# we get duplicate class errors from Polyglot. (TODO)
	@$(PLC) -cp $(CLASSES) -c -d $(OUT) $(filter %Object.java,$(JAVA_SRC))
	@$(PLC) -cp $(CLASSES) -c -d $(OUT) $(filter-out %Object.java,$(JAVA_SRC))

# Compile LLVM IR (.ll --> .o).
$(JAVA_OBJ): %.o: %.ll
	@echo "Compiling $<"
	@$(CLANG) $(JAVA_FLAGS) -c -o $@ $<

# Native code (.cpp --> .o).
$(NATIVE_OBJ): $(OUT)/%.o: %.cpp
	@mkdir -p $(dir $@)
	@echo "Compiling $<"
	@$(CLANG) $(NATIVE_FLAGS) -c -o $@ $<

# Create a shared library for compiled JDK classes.
$(LIBJDK): $(LIBJVM) $(ALL_OBJ)
	@echo "Creating libjdk"
	@$(CLANG) -glldb -lgc -shared -o $@ $^

clean:
	rm -rf $(OUT)

.PHONY: all clean
