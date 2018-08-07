if [ $# -lt 3 ]
then
  echo "Usage: `basename $0` <tetfile> <trainfile> <testfile> <trainlabel> <testlabel> <outputfile> <seed>"
  exit 1
fi

#make -C ../../../src/java

srcdir=../../../src/java
classpath="$srcdir:/usr/share/java/mysql.jar:/usr/share/java/guava.jar"
javalibs="-Djava.library.path=../../lib"                                 
javaoptions="-XX:-UseGCOverheadLimit -Xmx6G $javalibs"

tetfile=$1

trainfile=$2
testfile=$3

trainlabel=$4
testlabel=$5

output=$6

class="experiments.rnnTetBiblioClassification"
options="-i 200 -s $7"

echo "java -classpath $classpath $javaoptions $class $options $tetfile $trainfile $testfile $trainlabel $testlabel $output" 
java -classpath $classpath $javaoptions $class $options $tetfile $trainfile $testfile $trainlabel $testlabel $output
