db=$1 #localhost/imdb
dbuser=$2 #db username
dbpwd=$3 #db password

datadir=input
train=data.all
tetdir=tet_files

outdir_tet=RESULTS_TET
outdir_plain=RESULTS_BCOUNT

test -d $outdir_tet || mkdir $outdir_tet
test -d $outdir_plain || mkdir $outdir_plain

while read line
do

testfile=`echo $line | awk '{print $1}'`
tet=`echo $line | awk '{print $2}'`
movie=`echo $line | awk '{print $3}'`

m=`echo $movie | awk '{printf "movie0="$1"\n"}'`
y1=`cat $datadir/$testfile | tr '=' ' ' | awk '{printf "year"$NF"="$NF"\n"}'`
y2=`cat $datadir/$testfile | tr '=' ' ' | awk '{printf "year"($NF-20)"="($NF-20)"\n"}'`
cat $datadir/$train | awk -v y1="$y1" -v y2="$y2" -v m="$m" '{printf y1","y2","m","$0"\n"}' > $datadir/data.train
cat $datadir/$testfile | tr ',' ' ' | awk -v y1="$y1" -v y2="$y2" -v m="$m" '{printf y1","y2","m","$1"\n"}' > $datadir/data.test

rootdir=../..
srcdir=$rootdir/src/java
classpath="$srcdir:/usr/share/java/mysql.jar:/usr/share/java/guava.jar"
javalibs="-Djava.library.path=$rootdir/lib:"                                 
javaoptions="-XX:-UseGCOverheadLimit -Xmx64G $javalibs"

tetfile=$tetdir/$tet

testname=$testfile

trainfile=$datadir/data.train
testfile=$datadir/data.test
id2namesfile=id2names.txt


class="experiments.ActorRetrieval"

options="-b 5 -w 1 -n 3 -k 10000 -M -C -S 10000 -D 1"
suffix=`echo $options | tr ' ' '_'`
startemd=$SECONDS
echo "java -classpath $classpath $javaoptions $class $options $db $dbuser $dbpwd $tetfile $trainfile $testfile $id2namesfile $class.output.$suffix"
java -classpath $classpath $javaoptions $class $options $db $dbuser $dbpwd $tetfile $trainfile $testfile $id2namesfile $class.output.$suffix > $datadir/log.$suffix
echo "time retrieval: $((SECONDS - startemd))"
mv $datadir/log.$suffix $outdir_tet/log_$testname

options="-b 5 -w 1 -n 3 -k 10000 -M -C -S 10000 -D 1 -P"
suffix=`echo $options | tr ' ' '_'`
startemd=$SECONDS
echo "java -classpath $classpath $javaoptions $class $options $db $dbuser $dbpwd $tetfile $trainfile $testfile $id2namesfile $class.output.$suffix"
java -classpath $classpath $javaoptions $class $options $db $dbuser $dbpwd $tetfile $trainfile $testfile $id2namesfile $class.output.$suffix > $datadir/log.$suffix
echo "time retrieval: $((SECONDS - startemd))"
mv $datadir/log.$suffix $outdir_plain/log_$testname

done < LIST.txt

rm $datadir/data.train $datadir/data.test

