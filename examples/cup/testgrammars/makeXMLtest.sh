java -jar ../dist/java-cup-11b.jar -interface -parser Parser -xmlactions test2.cup
javac -cp ../dist/java-cup-11b-runtime.jar:. *.java
java -cp ../dist/java-cup-11b-runtime.jar:. Parser input.minijava test.xml
