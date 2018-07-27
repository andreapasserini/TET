Data for this experiment:  files data.train and data.test containing actor id's.
Finds for each actor in data.test the nearest neighbors in data.train.

To run the experiment:

./run_actor_retrieval.sh imdb <mysqluser> <mysqlpassword>

(takes some minutes!)

The results are printed to a logfile. The last part of the logfile contains the lists of the
nearest neighbors in data.train for the actors in data.test. For example:

test actor Humphrey Bogart   neighbors: Eddie I Graham , Tom I Quinn , Lynton Brent , Carl Stockdale , Francis I Ford , Joe I King , Luis Alberni , Forrest I Taylor , Clarence Muse , David I Newell 
