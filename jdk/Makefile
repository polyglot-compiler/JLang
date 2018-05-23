ifndef TOP_LEVEL_MAKEFILE_INVOKED
$(error Please invoke the top-level Makefile)
endif

SRC := src
PATCHES := patches
OUT := out
CLASSES := $(OUT)/classes

# PolyLLVM flags when compiling the JDK.
PLC_FLAGS := -assert -method-filter jdk-method-filter.txt -sourcepath $(SRC)

all: $(LIBJDK)

.PHONY: classes
classes: $(CLASSES)/cstamp

# Extract OpenJDK 7 source.
# The zip file comes from https://sourceforge.net/projects/jdk7src/
$(SRC): | $(SRC).zip
	@echo "Unzipping JDK sources"
	@unzip -q -d $@ $(SRC).zip
	@cp -r $(SRC) $(SRC).orig

# Path JDK sources to work around PolyLLVM unimplemented features.
$(SRC)/patchstamp: | $(SRC)
	@echo "Patching JDK source files"
	@for f in `find $(PATCHES) -name "*.patch"`; do patch -p0 < "$$f"; done
	@date > $@

$(CLASSES)/cstamp: | $(SRC)
	@echo "Creating JDK class files"
	@mkdir -p $(CLASSES)
	@find $(SRC) -name "*.java" > all.txt
	@$(JAVAC) @all.txt -Xlint:none -d $(CLASSES)
	@rm all.txt
	@date > $@

# Compile JDK source files (.java --> .ll)
# Use a Hello World program to compile only the most critical slice of the JDK.
$(OUT)/llstamp: $(SRC)/patchstamp $(PLC_SRC)
	@echo "Compiling JDK sources"
	@$(PLC) $(PLC_FLAGS) -d $(OUT) -entry-point Main Main.java
	@rm $(OUT)/Main.ll
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
$(LIBJDK): $(OUT)/ostamp $(OUT)/stubs.o $(LIBJVM)
	@echo "Creating libjdk"
	@$(CLANG) $(LIBJDK_FLAGS) \
		`find $(OUT) -name "*.o"` \
		$(JDK7)/jre/lib/{libjava,libnio,libnet,libzip}.dylib \
		-headerpad_max_install_names \
		-o $@

	@# Path to system JDK. Needed for libjava.
	@install_name_tool -add_rpath $(JDK7)/jre/lib $@

	@# Path to runtime (libjvm). Needed because libjava depends on @rpath/libjvm.
	@install_name_tool -add_rpath $(RUNTIME)/out $@

clean:
	rm -rf $(OUT) $(MAIN).binary $(SRC) $(SRC).orig

.PHONY: all
