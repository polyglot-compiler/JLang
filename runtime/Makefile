BASE_DIR = ..
POLYLLVMC_SRC = $(shell find $(BASE_DIR)/compiler/src -name "*.java")

JAVA_SRC = $(shell find src -name "*.java" -not -path src/placeholder/*)
JAVA_EXN_SRC = $(shell find ExceptionSrc -name "*.java" -not -path src/placeholder/*)
JAVA_ERR_SRC = $(shell find ErrorSrc -name "*.java" -not -path src/placeholder/*)
JNI_SRC = $(shell find jni -name "*.cpp")

JNI_LL = $(JNI_SRC:%.cpp=ll/%.ll)
JAVA_LL = $(JAVA_SRC:src/%.java=ll/%.ll)
JAVA_EXN_LL = $(JAVA_EXN_SRC:ExceptionSrc/%.java=ll/%.ll)
JAVA_ERR_LL = $(JAVA_ERR_SRC:ErrorSrc/%.java=ll/%.ll)
ALL_LL = $(JNI_LL) $(JAVA_LL) $(JAVA_EXN_LL) $(JAVA_ERR_LL)

all: runtime.o

$(JNI_LL): ll/%.ll: %.cpp
	@mkdir -p $(dir $@)
	@echo "Compiling $<"
	@clang++ -S -emit-llvm -std=c++11 -o $@ $<

# Mixing Ant and Make is tricky; this rule isn't perfect,
# but will work in almost all cases. Note that build.xml
# defines PREBUILT before executing this Makefile.
$(JAVA_LL): $(JAVA_SRC) $(JAVA_EXN_SRC) $(JAVA_ERR_SRC) $(POLYLLVMC_SRC)
ifndef PREBUILT
	@echo "Building polyllvmc"
	@ant -S -f "$(BASE_DIR)"
endif
	@echo "Compiling $(words $(JAVA_SRC) $(JAVA_EXN_SRC) $(JAVA_ERR_SRC)) Java files down to IR"
	@$(BASE_DIR)/bin/polyllvmc -c -d ll $(JAVA_SRC) 
	@$(BASE_DIR)/bin/polyllvmc -c -d ll $(JAVA_EXN_SRC)
	@$(BASE_DIR)/bin/polyllvmc -c -d ll $(JAVA_ERR_SRC)

runtime.ll: $(ALL_LL)
	@echo "Linking $(words $(ALL_LL)) IR files into $@"
	@llvm-link -S -o $@ $(ALL_LL)

runtime.o: runtime.ll
	clang++ -c -o $@ $<

.PHONY: clean
clean:
	rm -rf ll runtime.{ll,o}
