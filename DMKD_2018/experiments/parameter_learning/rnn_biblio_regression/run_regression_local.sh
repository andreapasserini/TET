if [ $# -lt 8 ]
then
  echo "Usage: `basename $0` <tetfile> <trainfile> <testfile> <trainlabel> <testlabel> <outputfile> <iterations> <seed>"
  exit 1
fi

# Set the classpath and working directory
rootdir=../../..
srcdir=$rootdir/src/java
classpath="$srcdir:$rootdir/lib/mysql.jar:$rootdir/lib/guava.jar"
javalibs="-Djava.library.path=$rootdir/lib:"                                 
javaoptions="-XX:-UseGCOverheadLimit -Xmx6G $javalibs"

# TET file
tetfile=$1

# Training and test files
trainfile=$2
testfile=$3

# Labels
trainlabel=$4
testlabel=$5

# Output log file
output=$6

class="experiments.rnnTetBiblioRegression"

# Number of iterations and seed
options="-i $7 -s $8"

echo "java -classpath $classpath $javaoptions $class $options $tetfile $trainfile $testfile $trainlabel $testlabel $output" 
java -classpath $classpath $javaoptions $class $options $tetfile $trainfile $testfile $trainlabel $testlabel $output
