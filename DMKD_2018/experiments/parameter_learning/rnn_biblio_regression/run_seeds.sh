for i in 1 2 3 5 8 13 21 34 55 89 144 233 377 610 987 1597 2584 4181 6765 10946
do 
    ./run_regression_local.sh tet-def data/full/dataset.train data/full/dataset.test data/full/eindex.train data/full/eindex.test logs/eindex_$i.log $i
    ./run_regression_local.sh tet-def data/full/dataset.train data/full/dataset.test data/full/gindex.train data/full/gindex.test logs/gindex_$i.log $i
    ./run_regression_local.sh tet-def data/full/dataset.train data/full/dataset.test data/full/hindex.train data/full/hindex.test logs/hindex_$i.log $i
    ./run_regression_local.sh tet-def data/full/dataset.train data/full/dataset.test data/full/i10index.train data/full/i10index.test logs/i10index_$i.log $i
done


