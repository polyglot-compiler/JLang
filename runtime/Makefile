SRC := src
OUT := out
CLASSES := $(OUT)/classes

# Hack to allow dependency on PolyLLVM code.
PLC_SRC := $(shell find ../compiler/src -name "*.java")

PLC := ../bin/polyllvmc
JDK7 ?= /Library/Java/JavaVirtualMachines/jdk1.7.0_80.jdk/Contents/Home
JAVAC := $(JDK7)/bin/javac

JAVA_SRC := $(shell find $(SRC) -name "*.java")
NATIVE_SRC := $(shell find native -name "*.cpp")

JAVA_CLS := $(JAVA_SRC:$(SRC)/%.java=$(CLASSES)/%.class)
JAVA_LL := $(JAVA_SRC:$(SRC)/%.java=$(OUT)/%.ll)

JAVA_OBJ := $(JAVA_SRC:$(SRC)/%.java=$(OUT)/%.o)
NATIVE_OBJ := $(NATIVE_SRC:%.cpp=$(OUT)/%.o)
ALL_OBJ := $(JAVA_OBJ) $(NATIVE_OBJ)

JNI_INCLUDES := \
	-I"$(JAVA_HOME)/include" \
	-I"$(JAVA_HOME)/include/darwin" \
	-I"$(JAVA_HOME)/include/linux"

CLANG := clang++
JAVA_FLAGS := \
	-glldb -Wno-override-module
NATIVE_FLAGS := \
	-glldb -std=c++14 -Inative $(JNI_INCLUDES) \
	-Wall -Wno-unused-private-field -MMD -pthread
LINK_FLAGS := \
	-glldb -lgc -undefined dynamic_lookup # TODO: Ideally would would not need dynamic_lookup

# We use the name "libjvm" because that is expected by JDK native code.
LIBJVM := libjvm.dylib
LIBJVM_VERSION := -current_version 1.0.0 -compatibility_version 1.0.0

all: $(OUT)/$(LIBJVM)

# Native code (.cpp --> .o).
$(NATIVE_OBJ): $(OUT)/%.o: %.cpp
	@mkdir -p $(dir $@)
	@echo "Compiling $<"
	@$(CLANG) $(NATIVE_FLAGS) -c -o $@ $<

# Runtime Java code (.java --> .class)
$(JAVA_CLS): $(JAVA_SRC)
	@echo "Compiling $(words $(JAVA_SRC)) Java files down to Java bytecode"
	@mkdir -p $(CLASSES)
	@$(JAVAC) -d $(CLASSES) $(JAVA_SRC)

# Runtime Java code (.java --> .ll)
$(JAVA_LL): $(JAVA_CLS) $(PLC_SRC)
	@echo "Compiling $(words $(JAVA_SRC)) Java files down to LLVM IR"
	@$(PLC) -c -d $(OUT) $(JAVA_SRC)

# Runtime Java code (.ll --> .o).
$(JAVA_OBJ): %.o: %.ll
	@echo "Compiling $<"
	@$(CLANG) $(JAVA_FLAGS) -c -o $@ $<

# Link into library (.o --> .dylib).
$(OUT)/$(LIBJVM): $(ALL_OBJ)
	@echo "Creating libjvm"
	@$(CLANG) $(LINK_FLAGS) -shared $(LIBJVM_VERSION) -o $@ $^
	@install_name_tool -id "@rpath/$(LIBJVM)" $@

.PHONY: clean
clean:
	rm -rf $(OUT)

# Auto-generated header dependencies for native code.
NATIVE_DEP := $(NATIVE_OBJ:%.o=%.d)
-include $(NATIVE_DEP)
