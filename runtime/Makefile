BASE_DIR = ..
POLYLLVMC_SRC = $(shell find $(BASE_DIR)/compiler/src -name "*.java")

OUT_DIR = out

JAVA_SRC = $(shell find src -name "*.java")
NATIVE_SRC = $(shell find native -name "*.cpp")
JVM_SRC = jvm

NATIVE_LL = $(NATIVE_SRC:%.cpp=$(OUT_DIR)/%.ll)
JAVA_LL = $(JAVA_SRC:src/%.java=$(OUT_DIR)/%.ll)
ALL_LL = $(NATIVE_LL) $(JAVA_LL)

JNI_INCLUDES = \
	-I"$(JAVA_HOME)/include" \
	-I"$(JAVA_HOME)/include/darwin" \
	-I"$(JAVA_HOME)/include/linux"

NATIVE_FLAGS = -g -Inative -Wall -Wno-unused-private-field -MMD -pthread

all: runtime.o $(JVM_SRC)/libjvm.dylib

$(NATIVE_LL): $(OUT_DIR)/%.ll: %.cpp
	@mkdir -p $(dir $@)
	@echo "Compiling $<"
	@clang++ $(JNI_INCLUDES) $(NATIVE_FLAGS) -S -emit-llvm -std=c++11 -o $@ $<

$(JVM_SRC)/libjvm.dylib: $(JVM_SRC)/jvm.cpp
	@echo "Creating libjvm"
	@g++ $(JNI_INCLUDES) -Inative -std=c++14 -shared -current_version 1.0.0 -compatibility_version 1.0.0 -o $@ $<
	@install_name_tool -id '@rpath/libjvm.dylib' $@

# Mixing Ant and Make is tricky; this rule isn't perfect,
# but will work in almost all cases. Note that build.xml
# defines PREBUILT before executing this Makefile.
$(JAVA_LL): $(JAVA_SRC) $(POLYLLVMC_SRC)
ifndef PREBUILT
	@echo "Building polyllvmc"
	@ant -S -f "$(BASE_DIR)"
endif
	@echo "Compiling $(words $(JAVA_SRC)) Java files down to IR"
	@$(BASE_DIR)/bin/polyllvmc -c -d $(OUT_DIR) $(JAVA_SRC)

runtime.ll: $(ALL_LL)
	@echo "Linking $(words $(ALL_LL)) IR files into $@"
	@llvm-link -S -o $@ $(ALL_LL)

runtime.o: runtime.ll
	@clang++ -g -c -o $@ $<

.PHONY: clean
clean:
	rm -rf classes $(OUT_DIR) runtime.{ll,o} $(JVM_SRC)/libjvm.dylib

# Auto-generated dependencies for native code.
-include $(NATIVE_SRC:%.cpp=out/%.d)
