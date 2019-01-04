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

tetfile=tet-apc

trainfile=gk.data.train
testfile=gk.data.test



# count_distance + MEMD version
class="experiments.ExhaustiveBiblioRegression"
options="-b 5 -w 1 -n 3 -k 10 -M -C"
suffix=`echo $options | tr ' ' '_'`
startemd=$SECONDS
echo "java -classpath $classpath $javaoptions $class $options $db $dbuser $dbpwd $tetfile $trainfile $testfile $class.output.$suffix"
java -classpath $classpath $javaoptions $class $options $db $dbuser $dbpwd $tetfile $trainfile $testfile $class.output.$suffix > log.$suffix
echo "time count distance + EMD: $((SECONDS - startemd))"


