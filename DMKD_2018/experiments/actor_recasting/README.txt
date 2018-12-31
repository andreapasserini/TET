To run the recasting experiment, please use the following command:

bash actor_recasting.sh localhost/imdb YOUR_MYSQL_USER YOUR_MYSQL_PASSWORD

or change the actor_recasting.sh accordingly.

* The tet_files folder contains all the TET files used in the experiments: they are all identical in the structure, but change just in the years used to identify time windows.

* The input folder contains all the input files used in the experiments, which basically just identify the constants associated to actors and years.

* In the LIST.txt there is one test example per row, with four columns: (1) data input file, (2) TET input file, (3) movie id, (4) target actor. 

* The LIST_MOVIES.txt file contains the list of test movies, each with the associated id.

