nns="1 2 3 5 10 25 50 100 250 500 1000"
bindir=../../../../bin

for nn in $nns
do
    (cat test.sure_negatives | awk '{print int($3)"\t-1"}'; cat knn.log.$nn  | grep Pred | awk '{print $3,$2}';)\
     | $bindir/fmeasure | awk -v nn=$nn '{printf("%d\t%.1f\n",nn,$1*100)}'    
done > test.f1

