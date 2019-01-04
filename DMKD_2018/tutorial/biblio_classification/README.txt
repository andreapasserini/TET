As a tutorial example, we suggest to run the classification experiment for the bibliometrics data. In this folder, we provide a small sample of the AMiner data that you can find in the experiments/biblio_classification folder: 5,000 training examples and 1,000 test examples.

For all the experiments, we typically use a dedicated Java class in the src/java/experiments folder. In this case we use the BiblioClassification class. There you can find all the command-line parameters that you can select. In the bash file you can find a more detailed description of the parameters used in the experiments in our paper.

The task is addressed with the KNN algorithm for binary classification. We use the AMiner data set.

To run the experiment on the AMiner data set, please use the following command:

bash run.sh localhost/imdb YOUR_MYSQL_USER YOUR_MYSQL_PASSWORD

or change the run.sh accordingly.

* The TET used in this experiment is in the tet-apc file. It is the author-paper-citation single branch TET. Parameters have been manually set.

* The training and test data sets are in the data.train and data.test files, with corresponding labels.train and labels.test files.



