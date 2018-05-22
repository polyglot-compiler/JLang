JDK ?= jdk-lite # Use the bare-bones JDK by default.

all: compiler runtime jdk

compiler:
	@echo "--- Building compiler ---"
	@ant -q
	@echo

runtime: compiler
	@echo "--- Building runtime ---"
	@$(MAKE) -C runtime
	@echo

jdk: compiler runtime
	@echo "--- Building JDK ---"
	@$(MAKE) -C $(JDK)
	@echo

clean:
	ant -q clean
	$(MAKE) -C runtime clean
	$(MAKE) -C $(JDK) clean

.PHONY: compiler runtime jdk
