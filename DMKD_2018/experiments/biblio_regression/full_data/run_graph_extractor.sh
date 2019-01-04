if [ $# -lt 3 ]
then
  echo "Usage: `basename $0` <dbname> <dbuser> <dbpwd>"
  exit 1
fi

db=localhost/$1
dbuser=$2
dbpwd=$3

rootdir=../..
srcdir=$rootdir/src/java
classpath="$srcdir:$rootdir/lib/mysql.jar:$rootdir/lib/guava.jar"
javalibs="-Djava.library.path=$rootdir/lib:"                                 
javaoptions="-XX:-UseGCOverheadLimit -Xmx6G $javalibs"

tetfile=tet-apc

# extract graphs
class="experiments.graphExtractor"
options=""

datafile=gk.data.train
dstdir=graphs_train
test -d $dstdir || mkdir $dstdir
echo "java -classpath $classpath $javaoptions $class $options $db $dbuser $dbpwd $tetfile $datafile $dstdir"
java -classpath $classpath $javaoptions $class $options $db $dbuser $dbpwd $tetfile $datafile $dstdir 

datafile=gk.data.test
dstdir=graphs_test
test -d $dstdir || mkdir $dstdir
echo "java -classpath $classpath $javaoptions $class $options $db $dbuser $dbpwd $tetfile $datafile $dstdir"
java -classpath $classpath $javaoptions $class $options $db $dbuser $dbpwd $tetfile $datafile $dstdir 
