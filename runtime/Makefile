ifndef TOP_LEVEL_MAKEFILE_INVOKED
$(error Please invoke the top-level Makefile)
endif

SRC := src
OUT := out
CLASSES := $(OUT)/classes

# Files indicating whether .class and .ll files have been generated.
CLS_STAMP := $(OUT)/cls_stamp
LL_STAMP := $(OUT)/ll_stamp

JAVA_SRC := $(shell find $(SRC) -name "*.java")
JAVA_OBJ := $(JAVA_SRC:$(SRC)/%.java=$(OUT)/%.o)

NATIVE_SRC := $(shell find native -name "*.cpp")
NATIVE_OBJ := $(NATIVE_SRC:%.cpp=$(OUT)/%.o)

ALL_OBJ := $(JAVA_OBJ) $(NATIVE_OBJ)

JAVA_FLAGS := \
	-glldb -Wno-override-module
NATIVE_FLAGS := \
	-glldb -std=c++14 -Inative $(JNI_INCLUDES) \
	-Wall -Wno-unused-private-field -MMD -pthread

# We use the name "libjvm" because that is expected by JDK native code.
LIBJVM := $(OUT)/libjvm.dylib
LIBJVM_VERSION := -current_version 1.0.0 -compatibility_version 1.0.0
LINK_FLAGS := \
	-glldb -lgc -shared $(LIBJVM_VERSION) \
	-install_name "$(realpath .)/$(LIBJVM)" \
	-undefined dynamic_lookup # TODO: Ideally would would not need dynamic_lookup

all: classes $(LIBJVM)

classes: $(CLS_STAMP)

# Runtime Java classes (.java --> .class)
$(CLS_STAMP): $(JAVA_SRC)
	@echo "Compiling $(words $(JAVA_SRC)) Java files with javac (.java --> .class)"
	@mkdir -p $(CLASSES)
	@$(JAVAC) -d $(CLASSES) $(JAVA_SRC)
	@date > $@

# Native code (.cpp --> .o).
$(NATIVE_OBJ): $(OUT)/%.o: %.cpp
	@mkdir -p $(dir $@)
	@echo "Compiling $<"
	@$(CLANG) $(NATIVE_FLAGS) -c -o $@ $<

# Runtime Java IR (.java --> .ll)
$(LL_STAMP): $(CLS_STAMP) $(PLC_SRC)
	@echo "Compiling $(words $(JAVA_SRC)) Java files with PolyLLVM (.java --> .ll)"
	@$(PLC) -cp $(JDK_CLASSES) -c -d $(OUT) $(JAVA_SRC)
	@date > $@

# Runtime Java code (.ll --> .o).
$(JAVA_OBJ): %.o: $(LL_STAMP)
	@echo "Compiling $(patsubst %.o,%.ll,$@)"
	@$(CLANG) $(JAVA_FLAGS) -c -o $@ $(patsubst %.o,%.ll,$@)

# Link into library (.o --> .dylib).
$(LIBJVM): $(ALL_OBJ)
	@echo "Creating libjvm"
	@$(CLANG) $(LINK_FLAGS) -o $@ $^

.PHONY: clean
clean:
	rm -rf $(OUT)

# Auto-generated header dependencies for native code.
NATIVE_DEP := $(NATIVE_OBJ:%.o=%.d)
-include $(NATIVE_DEP)
