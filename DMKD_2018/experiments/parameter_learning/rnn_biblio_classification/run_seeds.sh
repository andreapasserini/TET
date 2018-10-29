mkdir -p logs
mkdir -p logs/full logs/reduced
for s in 1 2 3 5 8 13 21 34 55 89 144 233 377 610 987 1597 2584 4181 6765 10946
do
   ./run_classification_local.sh tet-def data/full/dataset.train data/full/dataset.test data/full/labels.train data/full/labels.test logs/$s\_cl.log $s 
done
for s in 1 2 3 5 8 13 21 34 55 89 144 233 377 610 987 1597 2584 4181 6765 10946
do
   ./run_classification_local.sh tet-def data/reduced/dataset.train data/reduced/dataset.test data/reduced/labels.train data/reduced/labels.test logs/$s\_cl.log $s 
done
