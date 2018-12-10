rm Parser.java Lexer.java
rm *.class
jflex minijava.jflex
../../bin/cup.sh -locations -interface -parser Parser -xmlactions minijava.cup
javac -cp ../../dist/java-cup-11b-runtime.jar:. *.java
java -cp ../../dist/java-cup-11b-runtime.jar:. Parser simple.minijava simple.xml /
basex codegen.sq output.xml > simple.minijvm
