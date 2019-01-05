<<<<<<< HEAD
mkdir -p logs
mkdir -p logs/full logs/reduced
for i in 1 2 3 5 8 13 21 34 55 89 144 233 377 610 987 1597 2584 4181 6765 10946
do 
    ./run_regression_local.sh tet-def data/full/dataset.train data/full/dataset.test data/full/eindex.train data/full/eindex.test logs/full/eindex_$i.log $i
    ./run_regression_local.sh tet-def data/full/dataset.train data/full/dataset.test data/full/gindex.train data/full/gindex.test logs/full/gindex_$i.log $i
    ./run_regression_local.sh tet-def data/full/dataset.train data/full/dataset.test data/full/hindex.train data/full/hindex.test logs/full/hindex_$i.log $i
    ./run_regression_local.sh tet-def data/full/dataset.train data/full/dataset.test data/full/i10index.train data/full/i10index.test logs/full/i10index_$i.log $i
done
for i in 1 2 3 5 8 13 21 34 55 89 144 233 377 610 987 1597 2584 4181 6765 10946
do 
    ./run_regression_local.sh tet-def data/reduced/dataset.train data/reduced/dataset.test data/reduced/eindex.train data/reduced/eindex.test logs/reduced/eindex_$i.log $i
    ./run_regression_local.sh tet-def data/reduced/dataset.train data/reduced/dataset.test data/reduced/gindex.train data/reduced/gindex.test logs/reduced/gindex_$i.log $i
    ./run_regression_local.sh tet-def data/reduced/dataset.train data/reduced/dataset.test data/reduced/hindex.train data/reduced/hindex.test logs/reduced/hindex_$i.log $i
    ./run_regression_local.sh tet-def data/reduced/dataset.train data/reduced/dataset.test data/reduced/i10index.train data/reduced/i10index.test logs/reduced/i10index_$i.log $i
=======
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
>>>>>>> fcca4d4d00af2fc30b628c4e7eb170718577a10d
done

