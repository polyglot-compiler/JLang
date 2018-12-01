#!/bin/bash
SRC=src/java
OUT=out
BASE=~/poly/JLang
RUNTIME=$BASE/runtime/out
JDK_OUT=$BASE/$JDK/out
JDK_LIB=$JDK7/jre/lib
$BASE/bin/jlangc -cp "$JDK_OUT"/classes -sourcepath $SRC -d $OUT -entry-point java_cup.Main $SRC/java_cup/Main.java $SRC/java_cup/Lexer.java $SRC/java_cup/sym.java $SRC/java_cup/parser.java
FILES=$(find $OUT -name "*.ll")
#jflex lexer.flex
clang++ -Wno-override-module -lgc -g -L$RUNTIME -ljvm -L$JDK_OUT -ljdk -Wl,-rpath,$JDK_LIB -Wl,-rpath,$JDK_OUT -Wl,-rpath,$RUNTIME -rdynamic -o bin/Main.o out/java_cup/sym.ll out/java_cup/parser.ll out/java_cup/lexer.ll out/java_cup/parse_action_table.ll out/java_cup/symbol.ll out/java_cup/internal_error.ll out/java_cup/lalr_state.ll out/java_cup/ErrorManager.ll out/java_cup/terminal_set.ll out/java_cup/runtime/Symbol.ll out/java_cup/runtime/virtual_parse_stack.ll out/java_cup/runtime/Scanner.ll out/java_cup/runtime/ComplexSymbolFactory.ll out/java_cup/runtime/SymbolFactory.ll out/java_cup/runtime/lr_parser.ll out/java_cup/runtime/DefaultSymbolFactory.ll out/java_cup/version.ll out/java_cup/lalr_item.ll out/java_cup/action_production.ll out/java_cup/terminal.ll out/java_cup/parse_reduce_row.ll out/java_cup/nonassoc_action.ll out/java_cup/emit.ll out/java_cup/Main.ll out/java_cup/symbol_set.ll out/java_cup/production.ll out/java_cup/parse_action.ll out/java_cup/lalr_item_set.ll out/java_cup/parse_reduce_table.ll out/java_cup/action_part.ll out/java_cup/production_part.ll out/java_cup/parse_action_row.ll out/java_cup/lalr_transition.ll out/java_cup/symbol_part.ll out/java_cup/shift_action.ll out/java_cup/assoc.ll out/java_cup/reduce_action.ll out/java_cup/lr_item_core.ll out/java_cup/non_terminal.ll
