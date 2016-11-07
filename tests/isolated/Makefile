# `make all` creates solution files, llvm ir, and output files. Add the -k flag
#            to make all targets regardless of errors.
# `make <file>` to produce any individual generated file, such as .ll (LLVM IR),
#               .lll (linked LLVM IR), .s (ASM), .binary (executable), etc.
# `make sol` to generate solution (.sol) files.
# `make llvmir` to compile all programs down to LLVM IR (.ll) files.
# `make output` to compile LLVM IR to executable files and produce program
#               output (.output) files.

# Uses absolute paths to llvm commands, because Java's ProcessBuilder will
# otherwise not be able to find them.

BASE_DIR = ../..
RUNTIME = $(BASE_DIR)/runtime

SRCS = $(shell find . -name "*.java")
LL = $(shell find $(RUNTIME)/ll -name "*.ll")

CLASSES = $(SRCS:.java=.class)
SOL = $(SRCS:.java=.sol)
LLVMIR = $(SRCS:.java=.ll)
LINKED_LLVMIR = $(SRCS:.java=.lll)
ASM = $(SRCS:.java=.s)
BINARY = $(SRCS:.java=.binary)
OUTPUT = $(SRCS:.java=.output)
CHECK = $(SRCS:.java=.check)


all: sol llvmir output

sol: $(SOL)

llvmir: $(LLVMIR)

linked-llvmir: $(LINKED_LLVMIR)

asm: $(ASM)

binary: $(BINARY)

output: $(OUTPUT)

check: $(CHECK)

polyllvmc:
	ant -f "../../build.xml" jar

%.class: %.java
	javac -classpath $(RUNTIME)/classes $<

%.sol: %.class
	java -cp $(dir $@):$(RUNTIME)/classes $(notdir $*) | tee $@

# Assume polyllvmc has changed.
.PHONY: polyllvmc
%.ll: %.java polyllvmc
	$(BASE_DIR)/bin/polyllvmc -c -d $(dir $@) $<

%.lll: %.ll
	/usr/local/bin/llvm-link -o $@ -S $(LL) $<

%.s: %.lll
	/usr/local/bin/llc -o $@ $<

%.binary: %.s
	clang -o $@ $<

%.loutput: %.lll
	/usr/local/bin/lli $< | tee $@

%.output: %.binary
	$< | tee $@

%.check: %.output %.sol
	diff $^


EXT = class sol ll lll s binary output
clean: $(addprefix clean-, $(EXT))
clean-%:
	find . -name "*.$*" -delete
