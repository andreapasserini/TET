
if [ $# -lt 4 ]
then
  echo "Usage `basename $0` <options> <dbname> <dbuser> <dbpwd>" 
  exit 1
fi

options=$1
dbname=$2
dbuser=$3
dbpwd=$4
bindir=../../../../bin/

tetfile=tetfile

datafile=train.input
outfile=train.values
labelfile=train.labels
knnfile=train.knn.input

time $bindir/TetTesterDiscriminant.sh "$options" "localhost/$dbname" $dbuser $dbpwd $tetfile $datafile $outfile 
cat $outfile | cut -f 3 | paste $labelfile - > $knnfile

datafile=test.input 
outfile=test.values 
labelfile=test.labels
knnfile=test.knn.input


time $bindir/TetTesterDiscriminant.sh "$options" "localhost/$dbname" $dbuser $dbpwd $tetfile $datafile $outfile 
cat $outfile | cut -f 3 | paste $labelfile - > $knnfile
