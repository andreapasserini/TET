LIBPATH=../../lib
SRCPATH=.
SRCFILES=`find $(SRCPATH) -name "*.java"`
CLASSFILES=`find $(SRCPATH) -name "*.class"`
TMPFILES=`find $(SRCPATH) -name "*~"`
CLASSPATH=".:$(LIBPATH)/guava.jar"
JAVAC=javac


all: clean
	$(JAVAC) -classpath $(CLASSPATH) $(SRCFILES)
	cd FastEMD && make

clean:
	rm -f $(CLASSFILES) $(TMPFILES)
	cd FastEMD && make clean
