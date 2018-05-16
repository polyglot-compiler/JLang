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
	@$(MAKE) -C jdk
	@echo

clean:
	ant -q clean
	$(MAKE) -C runtime clean
	$(MAKE) -C jdk clean

.PHONY: compiler runtime jdk
