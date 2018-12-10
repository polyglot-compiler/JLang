mkdir -p out
../bin/cup.sh -destdir out -interface -parser Parser -xmlactions test2.cup
javac -cp ../dist/java-cup-11b-runtime.jar:.:out/ *.java out/*.java
java -cp ../dist/java-cup-11b-runtime.jar:.:out/ Parser input.minijava out/test_xml.xml
