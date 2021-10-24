all: 
	compile
	run

compile:
	java -jar ../jtb132di.jar -te minijava.jj
	java -jar ../javacc5.jar minijava-jtb.jj
	javac Main.java
	javac Pair.java
	javac symbolTable.java
	javac myOffsets.java
	

run:
	java Main QuickSort.java MiniExample.java QuickSort.java

clean:
	rm -f *.class *~

