if [ $# -lt 2 ]
then
  echo "Usage `basename $0` <dbname> <srcdb>" 
  exit 1
fi

db=$1
srcdb=$2

tetfile=tetfile
datafile=test.input
outfile=test.output

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

classpath="../..:../../mysql-connector-java-5.0.8-bin.jar"
class="Tetpackage.discriminant_Tet"

options="-v -f"

echo "java -XX:-UseGCOverheadLimit -Xmx16G -classpath $classpath $class $options $db $dbuser $dbpwd $tetfile $datafile $outfile"
time java -XX:-UseGCOverheadLimit -Xmx16G -classpath $classpath $class $options $db $dbuser $dbpwd $tetfile $datafile $outfile 

