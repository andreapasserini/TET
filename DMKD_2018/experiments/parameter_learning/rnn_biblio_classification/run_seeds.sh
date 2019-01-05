<<<<<<< HEAD
mkdir -p logs
mkdir -p logs/full logs/reduced
=======
size=$1
if [ $size != "full"] || [$size != "reduced"]
then
    echo "Usage: `basename $0` <full,reduced>"
    exit 1
fi
>>>>>>> fcca4d4d00af2fc30b628c4e7eb170718577a10d
for s in 1 2 3 5 8 13 21 34 55 89 144 233 377 610 987 1597 2584 4181 6765 10946
do
   ./run_classification_local.sh tet-def data/$size/dataset.train data/$size/dataset.test data/$size/labels.train data/$size/labels.test logs/$size/$s.log 200 $s 
done
for s in 1 2 3 5 8 13 21 34 55 89 144 233 377 610 987 1597 2584 4181 6765 10946
do
   ./run_classification_local.sh tet-def data/reduced/dataset.train data/reduced/dataset.test data/reduced/labels.train data/reduced/labels.test logs/$s\_cl.log $s 
done
