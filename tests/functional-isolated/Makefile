# `make sol` to generate solution (.sol) files.
# `make llvmir` to compile all programs down to LLVM IR (.ll) files.
# `make output` to interpret LLVM IR and produce program output (.output) files.
# `make <file>` to produce any individual generated file.
# `make all` creates solution files, llvm ir, and output files.

SRCS = $(shell find . -name "*.java")
CLASSES = $(SRCS:.java=.class)
SOL = $(SRCS:.java=.sol)
LLVMIR = $(SRCS:.java=.ll)
OUTPUT = $(SRCS:.java=.output)

all: sol llvmir output

sol: $(SOL)

llvmir: $(LLVMIR)

output: $(OUTPUT)

%.class: %.java
	javac $<

%.sol: %.class
	java -cp $(dir $@) $(notdir $*) > $@

# Assume polyllvmc has changed.
.PHONY: polyllvmc
%.ll: %.java polyllvmc
	../../bin/polyllvmc -c -d $(dir $@) $<

%.output: %.ll
	lli $< > $@

clean:
	find . -name "*.class" -delete
	find . -name "*.sol" -delete
	find . -name "*.ll" -delete
	find . -name "*.output" -delete
