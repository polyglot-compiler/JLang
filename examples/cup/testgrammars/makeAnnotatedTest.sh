mkdir -p out
../bin/cup.sh -destdir out -interface -parser Parser -xmlactions test-correctannotations.cup
javac -cp ../dist/java-cup-11b-runtime.jar:.:out/ -d out *.java out/*.java
java -cp ../dist/java-cup-11b-runtime.jar:.:out/ Parser input.minijava out/test.xml
