The bibliometrics regression experiment on the AMiner data set can be used on both a reduced and a full data set, which have two corresponding subfolders.

The two folders contain training/test splits and labels (the values of h-index, g-index, e-index, and i10-index for all the authors).

For example, to run the experiment on the reduced data set, go to the reduced_data folder and run:

bash run_biblio_regression.sh localhost/imdb YOUR_MYSQL_USER YOUR_MYSQL_PASSWORD

or change the run_biblio_regression.sh accordingly.

* The TET used in this experiment is in the tet-apc file. It is the author-paper-citation single branch TET. The parametrization is different from the classification case; in fact, in the reduced_data folder you can also find a second TET, named tet-apc.class which contains the parametrized TET used for the classification task.


