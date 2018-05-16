MAIN = Main
PLC := ../bin/polyllvmc
JDK = /Library/Java/JavaVirtualMachines/jdk1.7.0_80.jdk/Contents/Home
JDK_SRC := openjdk-7-src
PATCHES := patches
OUT := out

# PolyLLVM flags when compiling the JDK.
PLC_FLAGS := -assert -method-filter jdk-method-filter.txt -sourcepath $(JDK_SRC)

JNI_INCLUDES = \
	-I"$(JAVA_HOME)/include" \
	-I"$(JAVA_HOME)/include/darwin" \
	-I"$(JAVA_HOME)/include/linux"

all: $(MAIN).binary

# Extract OpenJDK 7 source.
# The zip file comes from https://sourceforge.net/projects/jdk7src/
$(JDK_SRC): | $(JDK_SRC).zip
	unzip -d $@ $(JDK_SRC).zip
	cp -r $(JDK_SRC) $(JDK_SRC).orig

$(JDK_SRC)/patchstamp: | $(JDK_SRC)
	@echo "Patching JDK source files"
	@for f in `find $(PATCHES) -name "*.patch"`; do patch -p0 < "$$f"; done
	@date > $@

# Compile JDK source files (.java --> .ll)
$(OUT)/llstamp: $(MAIN).java $(JDK_SRC)/patchstamp
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
	@clang++ -g -std=c++14 $(JNI_INCLUDES) -c -o $@ $<

# Link everything together into a binary.
$(MAIN).binary: $(OUT)/llstamp $(OUT)/ostamp $(OUT)/stubs.o ../runtime/runtime.o ../runtime/jvm/libjvm.dylib
	@echo "Linking everything together"
	@clang++ -g \
	    -L/usr/local/lib/ -lgc \
	    `find $(OUT) -name "*.o"` \
	    ../runtime/runtime.o \
	    ../runtime/jvm/libjvm.dylib \
	    $(JDK)/jre/lib/{libjava,libnio,libnet,libzip}.dylib \
	    -headerpad_max_install_names \
	    -o $@

	@# Add system JDK libraries.
	@install_name_tool -add_rpath $(JDK)/jre/lib/ $@

	@# Add custom JVM stubs.
	@install_name_tool -add_rpath ../runtime/jvm $@

$(OUT)/classes/cstamp: | $(JDK_SRC)
	@echo "Creating JDK class files"
	@mkdir -p $(OUT)/classes
	@find $(JDK_SRC) -name "*.java" > all.txt
	@javac @all.txt -source 7 -target 7 -Xlint:none -d $(OUT)/classes
	@rm all.txt
	@date > $@

clean:
	rm -rf $(OUT) $(MAIN).binary $(JDK_SRC)

.PHONY: all
