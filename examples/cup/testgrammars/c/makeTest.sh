rm *.java
rm *.class
jflex c.jflex
../../bin/cup.sh -locations -interface -parser Parser -xmlactions c.cup
javac -cp ../../dist/java-cup-11b-runtime.jar:. *.java
java -cp ../../dist/java-cup-11b-runtime.jar:. Parser input.c simple.xml
java -cp ../../dist/java-cup-11b-runtime.jar:. Parser complicated.c complicated.xml
