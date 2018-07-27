resdir=$1

if test $# -eq 0
then
	echo
	echo "Usage:"
	echo "        run.sh <results_directory>"
	echo
	exit
fi

test -d $resdir || mkdir $resdir
cp configfile datafile test.input yfile $resdir/

echo ""
echo "cd $resdir"
cd $resdir
echo ""

echo "1) Learn a TET from data"
echo "../traincommand.sh localhost/dblp_small > train.log"
../traincommand.sh localhost/dblp_small > train.log
echo ""

echo "2) Compute TET values"
echo "../testcommand.sh localhost/dblp_small dblp_small > test.log"
../testcommand.sh localhost/dblp_small dblp_small > test.log
echo ""

cat test.output | awk -F "\t" '{printf $1"\t"$3"\n"}' > test.values

echo "3) Adjust/normalize false counts"
echo "../../TetValuesDistance/adjustFalseCounts test.values -avg yfile"
../../TetValuesDistance/adjustFalseCounts test.values -avg yfile > test.values.adjusted
echo ""

echo "4) Run KNN on TET values using Wasserstein-Kantorovich distance"
echo "../../TetValuesDistance/knn test.values test.values "1 2 3" knnout"
../../TetValuesDistance/knn test.values test.values "1 2 3" knnout
echo ""

echo "cd .."
cd ..
echo ""
