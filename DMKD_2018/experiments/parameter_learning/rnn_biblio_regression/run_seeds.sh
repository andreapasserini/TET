size=$1
if [ $size != "full"] || [$size != "reduced"]
then
    echo "Usage: `basename $0` <full,reduced>"
    exit 1
fi
for s in 1 2 3 5 8 13 21 34 55 89 144 233 377 610 987 1597 2584 4181 6765 10946
do
    ./run_regression_local.sh tet-def data/$size/dataset.train data/$size/dataset.test data/$size/eindex.train data/$size/eindex.test logs/$size/eindex_$s.log 200 $s
    ./run_regression_local.sh tet-def data/$size/dataset.train data/$size/dataset.test data/$size/gindex.train data/$size/gindex.test logs/$size/gindex_$s.log 200 $s
    ./run_regression_local.sh tet-def data/$size/dataset.train data/$size/dataset.test data/$size/hindex.train data/$size/hindex.test logs/$size/hindex_$s.log 200 $s
    ./run_regression_local.sh tet-def data/$size/dataset.train data/$size/dataset.test data/$size/i10index.train data/$size/i10index.test logs/$size/i10index_$s.log 200 $s
done


