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
BASE_DIR := $(realpath ../..)
OUT_DIR := out
BIN_DIR := bin
EXECUTABLE := $(BIN_DIR)/Main.o
SRC_DIR := src/java

ifndef CLANG
CLANG := clang++
endif

ifndef TOP_LEVEL_MAKEFILE_INVOKED
	JDK ?= jdk-lite
	JDK := $(BASE_DIR)/$(JDK)
	JDK_CLASSES := $(JDK)/out/classes
	JDK7 ?= /Library/Java/JavaVirtualMachines/jdk1.7.0_80.jdk/Contents/Home/
	ifdef CLANG_VERSION
		CLANG := $(CLANG)-$(CLANG_VERSION)
	endif
	RUNTIME := $(BASE_DIR)/runtime/out
	SHARED_LIB_FLAGS := -g -lgc -shared -rdynamic
endif

CLANG_FLAGS := \
	-Wno-override-module -lgc -g \
	-L$(RUNTIME)/out -ljvm \
	-L$(JDK)/out -ljdk \
	-Wl,-rpath,$(JDK7_LIB_PATH) \
	-Wl,-rpath,$(JDK)/out -Wl,-rpath,$(RUNTIME)/out \
	-rdynamic

LIBCUP := $(OUT_DIR)/libcup.$(SHARED_LIB_EXT)

all: cup runtime

# Compiling Java with a Makefile is never ideal, but this is sufficient.
# Recall that $? expands to all dependencies newer than the target.

ll:
	@echo "Compiling Java file(s) down to LLVM IR"
	@$(BASE_DIR)/bin/jlangc -cp $(JDK_CLASSES) -sourcepath $(SRC_DIR) -d $(OUT_DIR) -entry-point java_cup.Main $(SRC_DIR)/java_cup/Main.java $(SRC_DIR)/java_cup/Lexer.java $(SRC_DIR)/java_cup/sym.java $(SRC_DIR)/java_cup/parser.java

classes:
	@echo "Compiling Java runtime to class files"
	@mkdir -p dist/classes
	@find $(SRC_DIR)/java_cup/runtime -name "*.java" > all.txt
	@$(JAVAC) @all.txt -Xlint:none -d dist/classes
	@rm all.txt
	@jar cf dist/java-cup-11b-runtime.jar -C dist/classes/ java_cup

objs: ll
	@echo "Compiling LLVM IR to Objects for Lib Creation"
	@$(MAKE) -f Makefile.obj -j4

cup: ll
	@echo "Creating CUP executable"
	@find $(OUT_DIR) -name "*.ll" | xargs $(CLANG) $(CLANG_FLAGS) -o $(EXECUTABLE) 


runtime: classes objs
	@echo "Compiling Cup Java runtime files to libcup"
	@find $(OUT_DIR)/java_cup/runtime -name "*.o" | xargs $(CLANG) $(SHARED_LIB_FLAGS) $(CLANG_FLAGS) -o $(LIBCUP)

debug: cup phony
	@JAVA_HOME=$(JDK7) bash -c 'gdb $(EXECUTABLE)'

clean: phony
	@echo "Removing files generated for CUP"
	@rm -rf $(EXECUTABLE) $(BIN_DIR)/*.dSYM $(OUT_DIR)

.PHONY: phony
