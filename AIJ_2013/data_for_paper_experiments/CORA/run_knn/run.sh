
if [ $# -lt 2 ]
then
  echo "Usage `basename $0` <dbuser> <dbpwd>" 
  exit 1
fi

$dbuser=$1
$dbpwd=$2

testoptions="-v -f"

db=cora
bindir=../../../../bin/
nn="1 2 3 5 10 25 50 100 250 500 1000"

for x in `seq 1 5`
do
  echo "cd $x"
  cd $x
  echo "../valuecommand.sh \"$testoptions\" $db $dbuser $dbpwd > value.log"
  ../valuecommand.sh "$testoptions" $db $dbuser $dbpwd > value.log
  $bindir/adjustFalseCounts train.knn.input -avg yfile > train.knn.input.rebalanced
  $bindir/adjustFalseCounts test.knn.input -avg yfile > test.knn.input.rebalanced
  echo "$bindir/knn train.knn.input.rebalanced test.knn.input.rebalanced \"$nn\"  knn.log"
  $bindir/knn train.knn.input.rebalanced test.knn.input.rebalanced "$nn"  knn.log
  echo "../results.sh"
  ../results.sh
  echo "cd .."
  cd ..
done

