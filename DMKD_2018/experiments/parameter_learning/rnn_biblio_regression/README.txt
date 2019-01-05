To run the parameter learning regression experiment on the AMiner data set,
for all the 20 seeds we tested, please use the following command:

   >bash run_seeds.sh DATASET

where DATASET can be 'full' or 'reduced'.
The best results on validation set sets the l-mse assignment.

To run a single experiment, run run_regression_local.sh, and insert the
input values for the experiment. Notice that when the <seed> field is put to
-1, the initial parameters are the one stated in the TET file definition,
otherwise the random generator initiates the parameters, according to the
specified seed.

To test a parameter assignment, write it in the TET file, set the seed to -1, and
the number of iterations to 1.

Files tet-def and tet-man contain TET with the respective parameter assignment.

* The TET used in this experiment is in the tet-def file. It is the
  author-paper-citation single branch TET.

* The log files of the computation are stored in the logs folder, the file name
  corresponds to the seed of the experiment.

* The folders of the two datasets are in the data/ folder,
  containing each the files dataset.train, dataset.test and the corresponding
  label files for each of the bibliometric index:
    - hindex.train, hindex.test
    - gindex.train, gindex.test
    - eindex.train, eindex.test
    - i10index.train, i10index.test
