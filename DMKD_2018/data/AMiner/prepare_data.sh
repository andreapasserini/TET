echo "Downloading data..:"
wget http://arnetminer.org/lab-datasets/aminerdataset/AMiner-Paper.rar
wget http://arnetminer.org/lab-datasets/aminerdataset/AMiner-Author.zip
wget http://arnetminer.org/lab-datasets/aminerdataset/AMiner-Author2Paper.zip
echo "Unzipping data..."
unrar e AMiner-Paper.rar
unzip AMiner-Author.zip
unzip AMiner-Author2Paper.zip
echo "Converting from txt to sql"
python txt2sql.py AMiner-Author.txt AMiner-Paper.txt Author2Paper.txt


