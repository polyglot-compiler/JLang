BASE_DIR = ..
RUNTIME = $(BASE_DIR)/runtime
LLFOLDER = $(BASE_DIR)/runtime/ll

JAVA_SRCS = $(shell find . -name "*.java" -not -path ./src/placeholder/*)
JNI_SRCS = $(shell find jni -name "*.cpp")

JNI_RES = $(JNI_SRCS:.cpp=.ll)
JAVA_RES = $(JAVA_SRCS:.java=.ll)

all: jni java linkall

jni: $(JNI_RES)

java: $(JAVA_RES)

.PHONY: polyllvmc
polyllvmc:
	ant -f "../build.xml" jar > /dev/null

%.ll: %.cpp
	clang++ $< -S -emit-llvm -std=c++11 -o $(LLFOLDER)/$@

%.ll: %.java polyllvmc
	$(BASE_DIR)/bin/polyllvmc -c -d $(LLFOLDER)/$(dir $(subst src/,,$@)) $<

linkall: jni java
	/usr/local/bin/llvm-link -o runtime.ll $(shell find $(LLFOLDER) -name "*.ll") -S