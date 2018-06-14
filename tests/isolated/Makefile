# `make all` creates solution files, llvm ir, and output files. Add the -k flag
#            to make all targets regardless of errors.
# `make <file>` to produce any individual generated file, such as .ll (LLVM IR),
#               .binary (executable), etc.
# `make sol` to generate solution (.sol) files.
# `make ir` to compile all programs down to LLVM IR (.ll) files.
# `make output` to compile LLVM IR to executable files and produce program
#               output (.output) files.

# Uses absolute paths to llvm commands, because Java's ProcessBuilder will
# otherwise not be able to find them.

BASE_DIR := ../..
RUNTIME := $(BASE_DIR)/runtime

# Use bare-bones JDK by default.
JDK ?= jdk-lite
JDK := $(BASE_DIR)/$(JDK)
JDK_CLS := $(JDK)/out/classes

SRC := $(shell find * -name "*.java")

CLASSES := $(SRC:.java=.class)
SOL := $(SRC:.java=.sol)
LL := $(SRC:.java=.ll)
BINARY := $(SRC:.java=.binary)
OUTPUT := $(SRC:.java=.output)

CLANG := clang++
CLANG_FLAGS := \
	-Wno-override-module -lgc -g \
	-L$(RUNTIME)/out -ljvm \
	-L$(JDK)/out -ljdk \
	-Wl,-rpath,$(JDK)/out -Wl,-rpath,$(RUNTIME)/out \
	-rdynamic

all: output

check: sol output
	@for i in $(ls *.sol | awk -F'.' '{print $1}'); do \
	 diff -q $i.sol $i.output; \
	done > check

sol: $(SOL)

ir: $(LL)

ll: $(LL)

binary: $(BINARY)

output: $(OUTPUT)

# Compiling Java with a Makefile is never ideal, but this is sufficient.
# Recall that $? expands to all dependencies newer than the target.
$(SOL): $(SRC)
	@echo "Compiling $(words $?) test(s) with javac"
	@javac $?
	@echo "Generating solutions for $(words $?) test(s)"
	@for f in $?; do \
		java -ea -cp . $(subst /,.,$${f%.java}) > $${f%.java}.sol; \
	done
	@touch $(SOL)

$(LL): $(SRC)
	@echo "Compiling $(words $?) Java file(s) down to LLVM IR"
	@$(BASE_DIR)/bin/polyllvmc -cp $(JDK_CLS) -assert $?
	@touch $(LL)

%.ll: %.java
	@echo "Compiling file to LLVM IR"
	@$(BASE_DIR)/bin/polyllvmc -cp $(JDK_CLS) -assert $?

%.binary: %.ll
	@echo "Creating binary for $<"
	@$(CLANG) $(CLANG_FLAGS) -o $@ $<

%.output: %.binary phony
	@echo "Generating output for $*"
	@./$< | tee $@

clean: phony
	@echo "Removing generated files"
	@rm -rf *.dSYM *.sol *.ll *.binary *.output *.class check

.PHONY: phony
