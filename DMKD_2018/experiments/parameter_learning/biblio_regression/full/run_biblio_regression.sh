if [ $# -lt 2 ]
then
  echo "Usage: `basename $0` <dbname> <dbuser> <dbpwd> <tetfile> <outputfile>"
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

tetfile=$4

trainfile=data/full/data.train
testfile=data/full/data.test

output=$5
# count_distance + EMD version
#class="experiments.BiblioRegression"
#options="-b 5 -w 1 -n 3 -k 10 -C -S 30 -D 12"
#suffix=`echo $options | tr ' ' '_'`
#startemd=$SECONDS
#echo "java -classpath $classpath $javaoptions $class $options $db $dbuser $dbpwd $tetfile $trainfile $testfile $class.output.$suffix"
#java -classpath $classpath $javaoptions $class $options $db $dbuser $dbpwd $tetfile $trainfile $testfile $class.output.$suffix
#echo "time regression: $((SECONDS - startemd))"

# count_distance + MEMD version
class="experiments.BiblioRegression"
options="-b 5 -w 1 -n 3 -k 10 -M -C -S 30 -D 12"
suffix=`echo $options | tr ' ' '_'`
startemd=$SECONDS
echo "java -classpath $classpath $javaoptions $class $options $db $dbuser $dbpwd $tetfile $trainfile $testfile $output"
java -classpath $classpath $javaoptions $class $options $db $dbuser $dbpwd $tetfile $trainfile $testfile $output > $output
echo "time regression: $((SECONDS - startemd))"
