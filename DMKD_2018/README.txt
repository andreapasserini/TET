=== TYPE EXTENSION TREES ===

This package contains the software used in the paper "Counts-of-Counts Similarity for Prediction and Search in Relational Data" by Manfred Jaeger, Marco Lippi, Giovanni Pellegrini, and Andrea Passerini, Data Mining and Knowledge Discovery (2019). The tool allows to use Type Extension Trees (TETs) for classification and retrieval tasks in relational settings.

* Requirements:

- Java (tested with version 1.8)
- MySQL (tested with version 5.7)
- C++ (tested with version 5.4.0)

* Installation

To configure and compile the software, please do the following:
./install_dependencies.sh (this will download libs)
cd src ; make ; cd ..

* Data preparation (AMiner)

To download and prepare the AMiner data in SQL format, please do the following:
cd data/AMiner ; ./prepare_data.sh > data.sql ; cat AMiner_structure.sql data.sql > AMiner.sql ; cd ../..
Then, load the AMiner.sql file into your MySQL database.

* Data preparation (IMDb)

The IMDb data set used in our experiments is unfortunately no longer available, and it cannot be re-distributed. Please contact the authors if you want to obtain futher information on this data set.

* Experiments

The experiments folder contains all the scripts needed to reproduce results, together with README files providing explanations and details.

* Tutorial

The tutorial folder contains a simple reduced example for the binary classification task in the bibliometrics setting. We suggest to use this example to get familiar with the TET syntax, parameters, and experimental setup.


------------------------------------
For any question, please contact us:
------------------------------------
manfred.jaeger@cs.aau.dk
marco.lippi@unimore.it
giovanni.pellegrini@unitn.it
andrea.passerini@unitn.it
------------------------------------




