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

BASE_DIR = ../..
RUNTIME = $(BASE_DIR)/runtime
POLYLLVMC_SRC = $(shell find $(BASE_DIR)/compiler/src -name "*.java")
RUNTIME_SRC = $(shell find $(RUNTIME) -name "*.java") $(shell find $(RUNTIME)/native -name "*.cpp")

SRCS = $(shell find * -name "*.java")

CLASSES = $(SRCS:.java=.class)
SOL = $(SRCS:.java=.sol)
LLVMIR = $(SRCS:.java=.ll)
LINKED_LLVMIR = $(SRCS:.java=.lll)
ASM = $(SRCS:.java=.s)
BINARY = $(SRCS:.java=.binary)
OUTPUT = $(SRCS:.java=.output)


all: sol output

sol: $(SOL)

ir: $(LLVMIR)

asm: $(ASM)

binary: $(BINARY)

output: $(OUTPUT)

$(CLASSES): $(SRCS)
	@echo "Compiling $(words $?) tests with javac"
	@javac -source 7 -cp $(RUNTIME)/classes $?
	@touch $(CLASSES)

%.sol: %.class
	@echo "Generating solution for $*"
	@java -ea -cp .:$(RUNTIME)/classes $(subst /,.,$*) > $@

# Mixing Ant and Make is tricky; this rule isn't perfect,
# but will work in almost all cases.
ifdef BATCHED
$(LLVMIR): $(SRCS) $(POLYLLVMC_SRC) $(RUNTIME_SRC)
	@echo "Building polyllvmc"
	@ant -f $(BASE_DIR)
	@echo "Compiling $(words $(SRCS)) Java files down to IR"
	@$(BASE_DIR)/bin/polyllvmc -assert $(SRCS)
else
%.ll: %.java $(POLYLLVMC_SRC) $(RUNTIME_SRC)
	@echo "Building polyllvmc"
	@ant -f $(BASE_DIR)
	@echo "Compiling $<"
	@$(BASE_DIR)/bin/polyllvmc -assert $<
endif

.PHONY: phony
%.desugar: %.java $(POLYLLVMC_SRC) $(RUNTIME_SRC)
	@echo "Desugaring $<"
	@ant -f $(BASE_DIR)
	@$(BASE_DIR)/bin/polyllvmc -assert -dump-desugared $<


%.binary: %.ll
	@echo "Creating binary for $<"
	@clang++ -Wno-override-module -L/usr/local/lib/ -lgc -g -o $@ $(RUNTIME)/runtime.o $<

.PHONY: phony
%.output: %.binary phony
	@echo "Generating output for $*"
	@./$< | tee $@


EXT = sol ll binary output dSYM class
clean: $(addprefix clean-, $(EXT))
clean-%:
	find . -name "*.$*" -delete
clean-dSYM:
	rm -rf *.dSYM
