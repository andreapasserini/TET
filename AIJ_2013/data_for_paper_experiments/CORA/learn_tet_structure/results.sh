
bindir=../../../../bin

# compute f1
(cat test.sure_negatives | awk '{print $3"\t-1"}'; cat test.output | cut -f 1 | paste test.labels -;) \
   | $bindir/fmeasure | awk '{printf("%.3f\n",$1)}'> test.f1

