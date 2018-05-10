MAIN = Main
PLC := ../bin/polyllvmc
JDK = /Library/Java/JavaVirtualMachines/jdk1.7.0_80.jdk/Contents/Home
JDK_SRC := openjdk-7-src
OUT := out

# PolyLLVM flags when compiling the JDK.
PLC_FLAGS := -assert -method-filter jdk-method-filter.txt -sourcepath $(JDK_SRC)

all: $(MAIN).binary

# Extract OpenJDK 7 source.
# The zip file comes from https://sourceforge.net/projects/jdk7src/
$(JDK_SRC): | $(JDK_SRC).zip
	unzip -d $@ $<

# Compile JDK source files (.java --> .ll)
$(OUT)/llstamp: $(MAIN).java | $(JDK_SRC)
	@echo "Compiling JDK sources"
	@$(PLC) $(PLC_FLAGS) -d $(OUT) $(MAIN).java
	@date > $(OUT)/llstamp
	@echo "Successfully compiled `find $(OUT) -name '*.ll' | wc -l | awk '{print $1}'` files"

# Compile LLVM IR (.ll --> .o).
# Uses a separate makefile so that we can run `find $(OUT) -name "*.ll"`
# only after the LLVM IR files are created.
$(OUT)/ostamp: $(OUT)/llstamp
	@echo "Compiling LLVM IR"
	@$(MAKE) -f Makefile.obj
	@date > $(OUT)/ostamp

# Compile temporary stubs for missing symbols.
# These are due to methods that the JDK normally registers at runtime.
$(OUT)/stubs.o: stubs.cpp
	@echo "Compiling missing symbol stubs"
	@clang++ -std=c++14 -c -o $@ $<

# Link everything together into a binary.
$(MAIN).binary: $(OUT)/llstamp $(OUT)/ostamp $(OUT)/stubs.o ../runtime/runtime.o
	@echo "Linking everything together"
	@clang++ -L/usr/local/lib/ -lgc -o \
	    `find $(OUT) -name "*.o"` \
	    ../runtime/runtime.o \
	    $(JDK)/jre/lib/{libjava,libnio,libnet,libzip}.dylib \
	    -o $@

	@# Add system JDK libraries.
	@install_name_tool -add_rpath $(JDK)/jre/lib/ $@

	@# Add custom JVM stubs.
	@install_name_tool -add_rpath ../runtime/jvm $@

clean:
	rm -rf $(OUT) $(MAIN).binary

.PHONY: all
