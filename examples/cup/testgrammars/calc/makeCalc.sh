../../bin/cup.sh -interface -parser Parser calc.cup
javac -cp ../../dist/java-cup-11b-runtime.jar:. *.java
java -cp ../../dist/java-cup-11b-runtime.jar:. Main
