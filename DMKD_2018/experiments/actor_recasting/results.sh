resdir=$1

while read line
do

first=`echo $line | cut -d' ' -f1`
other=`echo $line | cut -d' ' -f4-`

cat $resdir/log_$first | grep neigh | tr ':' '\n' | tail -1 | tr ',' '\n' | grep -n "$other" | tr ':' ' ' | awk '{print $1-1,$0}'

done < LIST.txt

