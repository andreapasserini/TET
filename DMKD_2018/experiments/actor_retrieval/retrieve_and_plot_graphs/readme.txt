Data for this experiment: single file data.draw containing actor id's
whose graphs should be extracted/plotted.

Extract graph representations of Tet-defined relational neighborhoods:

./run_graph_extractor.sh imdb <mysqluser> <mysqlpassword>

Extracted graphs are stored in .graphml format in the directory ./graphs

In the .graphml files nodes have a numeric "nt" attribute. The output to the
console gives a key mapping the attribute names from the database to the "nt"
attribute values. 
