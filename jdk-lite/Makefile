ifndef TOP_LEVEL_MAKEFILE_INVOKED
$(error Please invoke the top-level Makefile)
endif

SRC := src
OUT := out
CLASSES := $(OUT)/classes

# File indicating whether .class files have been produced.
CLS_STAMP := $(CLASSES)/cls_stamp

# Hack to allow dependency on PolyLLVM code.
PLC_SRC := $(shell find ../compiler/src -name "*.java")

JAVA_SRC := $(shell find src -name "*.java")
JAVA_CLS := $(JAVA_SRC:$(SRC)/%.java=$(CLASSES)/%.class)
JAVA_LL := $(JAVA_SRC:$(SRC)/%.java=$(OUT)/%.ll)
JAVA_OBJ := $(JAVA_LL:%.ll=%.o)

NATIVE_SRC := $(shell find native -name "*.cpp")
NATIVE_OBJ := $(NATIVE_SRC:%.cpp=$(OUT)/%.o)

ALL_OBJ := $(JAVA_OBJ) $(NATIVE_OBJ)

LIBJVM := $(RUNTIME)/out/libjvm.dylib
LIBJDK := $(OUT)/libjdk.dylib

PLC_FLAGS := -assert

JAVA_FLAGS := -glldb -Wno-override-module
NATIVE_FLAGS := -glldb -std=c++14 $(JNI_INCLUDES) -Wall -pthread
LINK_FLAGS := -glldb -lgc -shared -install_name "$(realpath .)/$(LIBJDK)"

all: classes $(LIBJDK)

classes: $(CLS_STAMP)

# Runtime Java code (.java --> .class)
$(CLS_STAMP): $(JAVA_SRC)
	@echo "Compiling $(words $(JAVA_SRC)) Java files down to Java bytecode"
	@mkdir -p $(CLASSES)
	@$(JAVAC) -d $(CLASSES) $(JAVA_SRC)
	@date > $@

# Compile JDK Java code (.java --> .ll)
$(JAVA_LL): $(PLC_SRC)
	@echo "Compiling $(words $(JAVA_SRC)) Java files down to LLVM IR"
	@# We compile java.lang.Object separately, since otherwise
	@# we get duplicate class errors from Polyglot. (TODO)
	@$(PLC) -c -d $(OUT) $(filter %Object.java,$(JAVA_SRC))
	@$(PLC) -c -d $(OUT) $(filter-out %Object.java,$(JAVA_SRC))

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
	@$(CLANG) $(LINK_FLAGS) -o $@ $^

clean:
	rm -rf $(OUT)

.PHONY: all clean
