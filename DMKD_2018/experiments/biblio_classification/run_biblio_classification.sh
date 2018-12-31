if [ $# -lt 3 ]
then
  echo "Usage: `basename $0` <dbname> <dbuser> <dbpwd>"
  exit 1
fi

db=localhost/$1
dbuser=$2
dbpwd=$3

# Set the classpath and working directory
rootdir=../..
srcdir=$rootdir/src/java
classpath="$srcdir:$rootdir/lib/mysql.jar:$rootdir/lib/guava.jar"
javalibs="-Djava.library.path=$rootdir/lib:"                                 
javaoptions="-XX:-UseGCOverheadLimit -Xmx6G $javalibs"

# TET file
tetfile=tet-apc

# Training and test files
trainfile=data.train
testfile=data.test

# Labels
trainvalfile=labels.train
testvalfile=labels.test

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
echo "java -classpath $classpath $javaoptions $class $options $db $dbuser $dbpwd $tetfile $trainfile $testfile $trainvalfile $testvalfile $class.output.$suffix"
java -classpath $classpath $javaoptions $class $options $db $dbuser $dbpwd $tetfile $trainfile $testfile $trainvalfile $testvalfile $class.output.$suffix > log.$suffix
echo "time regression: $((SECONDS - startemd))"
