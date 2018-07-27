
*** Tutorial example on Type Extension Trees (TETs) ***


To run a complete example on TET structure learning, computation of TET values
and classification using KNN, just prompt:

	./run.sh <results_directory>

The script will run through the following phases:

1) Learning a TET from data

	Considering a bibliometrics scenario, a TET will be learned from data,
	where the target predicate discriminates author with high H-index.
	In this part, a small MySQL database will be used, so make sure that
	you change your MySQL user/passwd in traincommand.sh (the db will
	be automatically loaded during the execution of the script).

	traincommand.sh

	The training procedure requires i) a configfile describing the domain
	in a prolog-like syntax, and ii) a datafile containing the ids of
	the examples, followed by their labels (+1/-1)

2) Computing TET values

	Using the TET learned during phase 1, TET values will be computed for
	some instances, in order to run a KNN algorithm using WK-distance.
	Also in this phase the MySQL database will be used, so user/passwd
	should be set accordingly in testcommand.sh.

	testcommand.sh
	

3) Adjusting false counts

	Before running KNN and/or computing WK-distance between any pair of
	TET values, a re-normalization of false counts might be run.
	This can be done using the adjustFalseCounts tool:

	../TetValuesDistance/adjustFalseCounts values -avg yfile

	In yfile you must put the y values (one per line) which multiply
	the product with the c_avg of false counts (see the example).

4) Running KNN using WK-distance between TET-values

	Finally, a KNN can be run using the Wasserstein-Kantorowich distance
	to measure similarities between two TET values:

	../TetValuesDistance/knn values.train values.train "1 2 3" out

	The first two arguments are training and test set, where labels and
	values are separated by a TAB (see example files).
	The third argument is a string describing the range of K parameter
	for KNN algorithm (neighborhood width).
	The fourth argument is the suffix to be used for the output files.


*** Other useful tools ***

Computing WK-distance between TET-values

	To compute the WK-distance between two TET-values, just put them into
	a single file, with no labels (as in example/values), and run
	
	../TetValuesDistance/computeDistance values


