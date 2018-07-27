dbuser=$1
dbpwd=$2

for dir in TET_AMiner_authors TET_AMiner_coauthors
do
	echo $dir
	dbname="AMiner"
	cd $dir
	./run_author_retrieval.sh $dbname $dbuser "$dbpwd" > log_hashing.txt
	./run_distance_stats.sh $dbname $dbuser "$dbpwd" > log_no_hashing.txt
	cd ..
done

for dir in TET_IMDb_business TET_IMDb_genre
do
	echo $dir
	dbname="imdb"
	cd $dir
	./run_actor_retrieval.sh $dbname $dbuser "$dbpwd" > log_hashing.txt
	./run_distance_stats.sh $dbname $dbuser "$dbpwd" > log_no_hashing.txt
	cd ..
done


