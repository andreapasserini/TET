FLAGS=-O3 -Wall -Wno-deprecated -I.
#FLAGS=-g -Wall -Wno-deprecated -I.

%.o:%.cpp
	g++ $(FLAGS) -c $< -o $@

TARGETS=computeDistance knn knn-regression adjustFalseCounts buildMatrix fmeasure

all: $(TARGETS)

clean:
	rm -f *~ *.o $(TARGETS)

computeDistance: Value.o ValueWithCount.o Multiset.o StringTokenizer.o computeDistance.o transportSimplex.h
	g++ $(FLAGS) -o computeDistance Value.o ValueWithCount.o Multiset.o StringTokenizer.o computeDistance.o

knn: Value.o ValueWithCount.o Multiset.o StringTokenizer.o knn.o transportSimplex.h
	g++ $(FLAGS) -o knn Value.o ValueWithCount.o Multiset.o StringTokenizer.o knn.o

knn-regression: Value.o ValueWithCount.o Multiset.o StringTokenizer.o knn-regression.o transportSimplex.h
	g++ $(FLAGS) -o knn-regression Value.o ValueWithCount.o Multiset.o StringTokenizer.o knn-regression.o

adjustFalseCounts: Value.o ValueWithCount.o Multiset.o StringTokenizer.o adjustFalseCounts.o transportSimplex.h
	g++ $(FLAGS) -o adjustFalseCounts Value.o ValueWithCount.o Multiset.o StringTokenizer.o adjustFalseCounts.o

buildMatrix: Value.o ValueWithCount.o Multiset.o StringTokenizer.o buildMatrix.o transportSimplex.h
	g++ $(FLAGS) -o buildMatrix Value.o ValueWithCount.o Multiset.o StringTokenizer.o buildMatrix.o

fmeasure: Exception.o fmeasure.cpp
	g++ $(FLAGS) $+ -o $@
