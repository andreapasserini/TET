SRCPATH=Tetpackage
SRCFILES=`find $(SRCPATH) -name "*.java"`
CLASSFILES=`find $(SRCPATH) -name "*.class"`
TMPFILES=`find $(SRCPATH) -name "*~"`
BINDIR=bin/
TARGETBIN=knn adjustFalseCounts 
JAVAC=javac

all: clean
	$(JAVAC) $(SRCFILES)
	cd TetValuesDistance ; make clean ; make ; cd ..

install: all
	cd TetValuesDistance && cp $(TARGETBIN) ../$(BINDIR)
	ln -fs ../$(SRCPATH) $(BINDIR) 
	 
clean:
	rm -f $(CLASSFILES) $(TMPFILES)
