if [ $# -lt 4 ]
then
  echo "Usage: `basename $0` <options> <dbname> <dbuser> <dbpwd>"
  exit 1
fi

options=$1
db=$2
dbuser=$3
dbpwd=$4

classpath="`dirname $0`:/usr/share/java/mysql.jar"
class="Tetpackage.learner.TetLearnerPolicyDiscriminantTet"
javaoptions="-XX:-UseGCOverheadLimit -Xmx6G"

configfile=configfile
datafile=datafile
tetfile=tetfile

echo "java -classpath $classpath $javaoptions $class $options $configfile $datafile $tetfile $db $dbuser $dbpwd" 
java -classpath $classpath $javaoptions $class $options $configfile $datafile $tetfile $db $dbuser $dbpwd
