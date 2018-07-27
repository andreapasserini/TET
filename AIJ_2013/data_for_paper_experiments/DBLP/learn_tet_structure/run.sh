
if [ $# -lt 2 ]
then
  echo "Usage `basename $0` <dbuser> <dbpwd>" 
  exit 1
fi

$dbuser=$1
$dbpwd=$2

bindir=../../../bin/
learnoptions="-d 2 -s 0 -g 0 -n 3 -E 1e-4 -t -u"

db="dblp_small"
time $bindir/TetLearnerDiscriminant.sh "$learnoptions" "localhost/$db" $dbuser $dbpwd > train.log

