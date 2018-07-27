
if [ $# -lt 2 ]
then
  echo "Usage `basename $0` <dbuser> <dbpwd>" 
  exit 1
fi

$dbuser=$1
$dbpwd=$2

cd learn_tet_structure
./run.sh $dbuser $dbpwd
cd ..

cd run_knn
./run.sh $dbuser $dbpwd
cd ..

