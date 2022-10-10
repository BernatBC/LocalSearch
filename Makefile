all: Main.java
	javac Main.java

clean:
	rm -f Main.class
	rm -f IA/Energia/Energy*.class
	rm -f aima/*/*.class
	rm -f aima/*/*/*.class
