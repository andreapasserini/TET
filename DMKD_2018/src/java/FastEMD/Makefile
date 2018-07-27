#########################################################
# class and executable name
#########################################################
PACKAGE= FastEMD
CLASS= emd_hat
EXE= $(CLASS)_main
NATIVE_ADDITION= native
#########################################################

#########################################################
# compiler stuff
#########################################################
UNAME = $(shell uname)
ifeq ($(UNAME),Darwin)
    ## Under MacOS, clang is preferred if installed
    CXX = $(shell which clang++)
    ifeq ($(CXX),)
        CXX = $(shell which g++)
    endif
    CC = clang
    CXXFLAGS = -dynamic -Wall  -O -DNDEBUG
    JAVA_HOME = $(shell /usr/libexec/java_home -v 1.7)
    JAVA_INCLUDES = -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/darwin
    CJOK = 1
endif
ifeq ($(UNAME),Linux)
    ## At least under Ubuntu 11.04, clang++ include path is broken...
    CXX = g++
    CC = gcc
    CXXFLAGS = -shared -fPIC -Wall  -O -DNDEBUG
    JAVA_HOME = /usr/lib/jvm/java-8-openjdk-amd64
    JAVA_INCLUDES = -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux
    CJOK = 1
endif
ifeq ($(CJOK),)
    $(error Fatal: OS not recognized. Edit Makefile and fix manually)
endif


JAVA= javac
LIBDIR= ../../../lib

#########################################################

all: lib$(CLASS)_$(NATIVE_ADDITION).so 
 
#########################################################
# Creating the shared library
#########################################################
lib$(CLASS)_$(NATIVE_ADDITION).so:
	@echo If this compliation fails, you\'ll need to find out how to compile a shared library that includes jni.h to your specific computer architectecture.
	@echo
	$(CXX) $(CXXFLAGS) $(JAVA_INCLUDES) -Iinclude -shared $(CLASS)_$(NATIVE_ADDITION).cpp -o $@
	cp $@ $(LIBDIR) 


#########################################################

#########################################################
# For creating the interface
#########################################################
$(CLASS).java:
	@touch $@
	@echo class $(CLASS) { >> $@
	@echo     static {         >> $@
	@echo       // We used here loadLibrary which means that you\'ll have to run your main function with -Djava.library.path=. >> $@
	@echo       // For example if your main function is in demo_FastEMD.java like here. You\'ll run it with: >> $@
	@echo       // java -Djava.library.path=. demo_FastEMD >> $@
	@echo       // Another option is to use here load instead of loadLibrary with the full path. Something like this: >> $@
	@echo       // System.load\(\"/cs/vis/ofirpele/emd/FastEMD/FastEMD/JAVA/lib$(CLASS)_$(NATIVE_ADDITION).so\"\)\; >> $@
	@echo       // Use System.out.println\(System.mapLibraryName\(\"emd_hat_native\"\)\)\; to see how your specific architectecture >> $@
	@echo       // names shared libraries. >> $@
	@echo      try{ >> $@
	@echo        System.loadLibrary\(\"$(CLASS)\"\)\; >> $@
	@echo      } catch \(java.lang.UnsatisfiedLinkError e\) { >> $@
	@echo        System.out.println\(\"====================================================\"\)\; >> $@
	@echo        System.out.println\(\"Could not find $(CLASS)_$(NATIVE_ADDITION) library. Run with:\"\)\; >> $@
	@echo        System.out.println\(\"java -Djava.library.path=.\"\)\; >> $@
	@echo	     System.out.println\(\"If it\'s not the problem, you\'ll need to copy a compiled library that suites your architectecture from compiled_$(CLASS)_$(NATIVE_ADDITION)_libs/\"\)\; >> $@
	@echo        System.out.println\(\"Or compile the library \(from emd_hat_native.cpp\)\"\)\; >> $@
	@echo        System.out.println\(\"====================================================\"\)\; >> $@
	@echo        System.out.println\(\"\"\)\; >> $@
	@echo        throw e\; >> $@
	@echo      } >> $@
	@echo     }                >> $@
	@echo } >> $@
	@echo ============================================
	@echo Now implement $@
	@echo ============================================

$(CLASS).class:
	javac $(CLASS).java

$(CLASS)_$(NATIVE_ADDITION).hpp:
	javah -jni -classpath .. -o $@ $(PACKAGE).$(CLASS) 

$(CLASS)_$(NATIVE_ADDITION).cpp: $(CLASS)_$(NATIVE_ADDITION).hpp 
	@touch $@
	@echo \#include \"$<\" >> $@
	@echo >> $@
	@grep JNICALL $< >> $@
	@echo ============================================
	@echo Now implement $@
	@echo ============================================
#########################################################

clean:
	rm -f *.so *.class

.PHONY: all clean depend
