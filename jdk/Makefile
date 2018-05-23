ifndef TOP_LEVEL_MAKEFILE_INVOKED
$(error Please invoke the top-level Makefile)
endif

MAIN = Main
PLC := ../bin/polyllvmc
JDK7 ?= /Library/Java/JavaVirtualMachines/jdk1.7.0_80.jdk/Contents/Home
JDK_SRC := src
PATCHES := patches
OUT := out
CLASSES := $(OUT)/classes

CLANG := clang++

LIBJVM := ../runtime/out/libjvm.dylib
LIBJDK := libjdk.dylib

# PolyLLVM flags when compiling the JDK.
PLC_FLAGS := -assert -method-filter jdk-method-filter.txt -sourcepath $(JDK_SRC)

JNI_INCLUDES = \
	-I"$(JAVA_HOME)/include" \
	-I"$(JAVA_HOME)/include/darwin" \
	-I"$(JAVA_HOME)/include/linux"

# For now we test with Main.java.
all: $(MAIN).binary

.PHONY: classes
classes: $(CLASSES)/cstamp

# Extract OpenJDK 7 source.
# The zip file comes from https://sourceforge.net/projects/jdk7src/
$(JDK_SRC): | $(JDK_SRC).zip
	@echo "Unzipping JDK sources"
	@unzip -q -d $@ $(JDK_SRC).zip
	@cp -r $(JDK_SRC) $(JDK_SRC).orig

# Path JDK sources to work around PolyLLVM unimplemented features.
$(JDK_SRC)/patchstamp: | $(JDK_SRC)
	@echo "Patching JDK source files"
	@for f in `find $(PATCHES) -name "*.patch"`; do patch -p0 < "$$f"; done
	@date > $@

$(CLASSES)/cstamp: | $(JDK_SRC)
	@echo "Creating JDK class files"
	@mkdir -p $(CLASSES)
	@find $(JDK_SRC) -name "*.java" > all.txt
	@javac @all.txt -source 7 -target 7 -Xlint:none -d $(CLASSES)
	@rm all.txt
	@date > $@

# Compile JDK source files (.java --> .ll)
# Use a Hello World program to compile only the most critical slice of the JDK.
$(OUT)/llstamp: $(MAIN).java $(JDK_SRC)/patchstamp $(PLC_SRC)
	@echo "Compiling JDK sources"
	@$(PLC) $(PLC_FLAGS) -d $(OUT) -entry-point $(MAIN) $(MAIN).java
	@date > $@
	@echo "Successfully compiled `find $(OUT) -name '*.ll' | wc -l | awk '{print $1}'` files"

# Compile LLVM IR (.ll --> .o).
# Uses a separate makefile so that we can run `find $(OUT) -name "*.ll"`
# only after the LLVM IR files are created.
$(OUT)/ostamp: $(OUT)/llstamp
	@echo "Compiling LLVM IR"
	@$(MAKE) -f Makefile.obj -j4
	@date > $@

# Compile temporary stubs for missing symbols.
# These are due to methods that the JDK normally registers at runtime.
$(OUT)/stubs.o: stubs.cpp
	@echo "Compiling missing symbol stubs"
	@$(CLANG) -glldb -std=c++14 $(JNI_INCLUDES) -c -o $@ $<

# Create a shared library for compiled JDK classes.
$(OUT)/$(LIBJDK): $(OUT)/ostamp $(OUT)/stubs.o $(LIBJVM)
	@echo "Creating libjdk"
	@$(CLANG) -glldb -lgc $(LIBJVM) -shared \
		`find $(OUT) -name "*.o"` \
		$(JDK7)/jre/lib/{libjava,libnio,libnet,libzip}.dylib \
		-o $@

# Create binary.
$(MAIN).binary: $(OUT)/$(LIBJDK)
	@# We include libjvm directly, since that's where the 'main' function is.
	@$(CLANG) -glldb $(LIBJVM) -o $@ $<
	@install_name_tool -add_rpath $(JDK7)/jre/lib/ $@ # Path to system JDK.
	@install_name_tool -add_rpath $(dir $(LIBJVM)) $@ # Path to our runtime.

clean:
	rm -rf $(OUT) $(MAIN).binary $(JDK_SRC) $(JDK_SRC).orig

.PHONY: all
