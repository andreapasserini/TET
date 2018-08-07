if [ $# -lt 3 ]
then
  echo "Usage: `basename $0` <dbname> <dbuser> <dbpwd> <tetfile> <outputfile>"
  exit 1
fi

db=localhost/$1
dbuser=$2
dbpwd=$3

rootdir=../..
srcdir=$rootdir/src/java
classpath="$srcdir:../../lib/mysql.jar:../../lib/guava.jar"
#classpath="$srcdir:$rootdir/tet/lib/mysql-connector-java-5.0.8-bin.jar:$rootdir/tet/lib/guava-19.0.jar"
javalibs="-Djava.library.path=$rootdir/lib:"                                 
javaoptions="-XX:-UseGCOverheadLimit -Xmx6G $javalibs"

tetfile=$4

trainfile=data.train
testfile=data.test

trainvalfile=labels.train
testvalfile=labels.test

output=$5
# count_distance + EMD version
# class="experiments.BiblioClassification"
# options="-b 5 -w 1 -n 3 -k 10 -C -S 30 -D 12"
# suffix=`echo $options | tr ' ' '_'`
# startemd=$SECONDS
# echo "java -classpath $classpath $javaoptions $class $options $db $dbuser $dbpwd $tetfile $trainfile $testfile $class.output.$suffix"
# java -classpath $classpath $javaoptions $class $options $db $dbuser $dbpwd $tetfile $trainfile $testfile $class.output.$suffix > log.$suffix
# echo "time regression: $((SECONDS - startemd))"

# count_distance + MEMD version
class="experiments.BiblioClassification"
options="-b 5 -w 1 -n 3 -k 10 -M -C -S 30 -D 12"
suffix=`echo $options | tr ' ' '_'`
startemd=$SECONDS
echo "java -classpath $classpath $javaoptions $class $options $db $dbuser $dbpwd $tetfile $trainfile $testfile $trainvalfile $testvalfile $output"
java -classpath $classpath $javaoptions $class $options $db $dbuser $dbpwd $tetfile $trainfile $testfile $trainvalfile $testvalfile $output > $output
echo "time regression: $((SECONDS - startemd))"
