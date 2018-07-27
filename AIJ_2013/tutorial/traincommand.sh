
if [ $# -lt 1 ]
then
  echo "Usage `basename $0` <dbname>"
  exit 1
fi

db=$1
options="-d 3 -s 0 -g 0 -n 3 -a 5e-2 -e 1e-3 -t"

classpath="../..:../../mysql-connector-java-5.0.8-bin.jar"
class="Tetpackage.learner.TetLearnerPolicyDiscriminantTet"

configfile=configfile
datafile=datafile
tetfile=tetfile

##### EDIT THIS PART #####
YOUR_MYSQL_USER="CHANGE.ME"
YOUR_MYSQL_PASSWD="CHANGE.ME"
##########################

dbuser=${YOUR_MYSQL_USER}
dbpwd=${YOUR_MYSQL_PASSWD}

if [ ${YOUR_MYSQL_USER} = "CHANGE.ME" ]
then
	echo "You should specify MySQL user in traincommand.sh"
	exit
else
	if [ ${YOUR_MYSQL_PASSWD} = "CHANGE.ME" ]
	then
		echo "You should specify MySQL password in traincommand.sh"
		exit
	fi
fi

mysql -u $dbuser -p$dbpwd < ../dblp_small.sql

echo "java -XX:-UseGCOverheadLimit -Xmx16G -classpath $classpath $class $options $configfile $datafile $tetfile $db $dbuser $dbpwd" 
java -XX:-UseGCOverheadLimit -Xmx16G -classpath $classpath $class $options $configfile $datafile $tetfile $db $dbuser $dbpwd

