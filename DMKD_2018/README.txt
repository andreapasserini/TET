To configure and compile the software, please do the following:

./install_dependencies.sh (this will download libs)
cd src ; make ; cd ..

To download and prepare the AMiner data in SQL format, please do the following:

cd data/AMiner ; ./prepare_data.sh > data.sql ; cat AMiner_structure.sql data.sql > AMiner.sql ; cd ../..

Then, load the AMiner.sql file into your MySQL database.

The experiments folder contains all the scripts needed to reproduce results.

