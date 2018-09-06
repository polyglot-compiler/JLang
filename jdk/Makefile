ifndef TOP_LEVEL_MAKEFILE_INVOKED
$(error Please invoke the top-level Makefile)
endif

SHELL=/bin/bash

SRC := src
SRC2 := src2
PATCHES := patches
OUT := out
CLASSES := $(OUT)/classes
NUM_JAVA_FILES := $(shell find $(JDK)/$(SRC) -name "*.java" | wc -l)
#Magic Multiplier for now. TODO calculate this a better way
RUN_COUNT := $(shell echo "2 * $(NUM_JAVA_FILES)" | bc)
# JLANG flags when compiling the JDK.
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

# Path JDK sources to work around JLang unimplemented features.
$(SRC)/patchstamp: | $(SRC)
	@echo "Patching JDK source files"
	@for f in `find $(PATCHES) -name "*.patch"`; do \
			patch -p0 < "$$f"; \
			NEW_FILE=$(SRC)$$(echo "$${f#patches}" | sed "s/patch/java/g"); \
			echo "$$NEW_FILE"; \
			sed "s/XXDATEXX/$$(date +%Y-%m-%d)/g" modification_notice.txt > tmp.txt; \
			cat "$$NEW_FILE" >> tmp.txt; \
			cat tmp.txt > "$$NEW_FILE"; \
		done
	@rm -f tmp.txt;
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
	@$(PLC) $(PLC_FLAGS) -d $(OUT) -max-runs $(RUN_COUNT) -entry-point Main Main.java $(SRC)/sun/nio/cs/UTF_8.java $(SRC)/java/io/UnixFileSystem.java
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
	@$(CLANG) -g -std=c++14 -fPIC $(JNI_INCLUDES) -c -o $@ $<

# Create a shared library for compiled JDK classes.
# Notice that we link directly with the system JDK's native code.
$(LIBJDK): $(OUT)/ostamp $(OUT)/stubs.o $(LIBJVM)
	@echo "Creating libjdk"
	@$(CLANG) $(LIBJDK_FLAGS) \
		`find $(OUT) -name "*.o"` \
		$(LIBJVM) \
		$(JDK7_LIB_PATH)/{libjava,libnio,libnet,libzip}.$(SHARED_LIB_EXT) \
		-Wl,-rpath,$(JDK7_LIB_PATH) \
		-o $@

clean:
	rm -rf $(OUT) $(MAIN).binary $(SRC) $(SRC).orig

.PHONY: all
