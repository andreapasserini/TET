if [ $# -lt 3 ]
then
  echo "Usage: `basename $0` <dbname> <dbuser> <dbpwd>"
  exit 1
fi

db=localhost/$1
dbuser=$2
dbpwd=$3

rootdir=../../..
srcdir=$rootdir/src/java
classpath="$srcdir:$rootdir/lib/mysql.jar:$rootdir/lib/guava.jar"
javalibs="-Djava.library.path=$rootdir/lib:"                                 
javaoptions="-XX:-UseGCOverheadLimit -Xmx6G $javalibs"

# Select the one you want:
tetfile=tet.genre
#tetfile=tet.business

# extract graphs
class="experiments.graphExtractor"
options=""

datafile=data.draw
dstdir=graphs/

echo "java -classpath $classpath $javaoptions $class $options $db $dbuser $dbpwd $tetfile $datafile $dstdir"
java -classpath $classpath $javaoptions $class $options $db $dbuser $dbpwd $tetfile $datafile $dstdir 

