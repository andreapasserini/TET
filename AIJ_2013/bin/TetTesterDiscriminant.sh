if [ $# -lt 7 ]
then
  echo "Usage: `basename $0` <options> <dbname> <dbuser> <dbpwd> <tetfile> <datafile> <outfile>"
  exit 1
fi

classpath="`dirname $0`:/usr/share/java/mysql.jar"
class="Tetpackage.discriminant_Tet"
javaoptions="-XX:-UseGCOverheadLimit -Xmx6G"

echo "java -classpath $classpath $javaoptions $class $@"
java -classpath $classpath $javaoptions $class $@
