
if [ $# -lt 2 ]
then
  echo "Usage `basename $0` <dbuser> <dbpwd>" 
  exit 1
fi

$dbuser=$1
$dbpwd=$2

bindir=../../../../bin/
learnoptions="-d 4 -s 0 -E 1e-2 -n 3 -g 0 -t -u"
testoptions="-v"

for x in `seq 1 5`
do
  echo "cd $x"
  cd $x
  db="cora_train_$x"
  time $bindir/TetLearnerDiscriminant.sh "$learnoptions" "localhost/$db" $dbuser $dbpwd > train.log
  db="cora_test_$x"
  time $bindir/TetTesterDiscriminant.sh "$testoptions" "localhost/$db" $dbuser $dbpwd tetfile test.input test.output > test.log
  ../results.sh
  echo "cd .."
  cd ..
done

echo "f1=`cat */test.f1 | awk '{x+=$1} END {print x/NR}'`"

