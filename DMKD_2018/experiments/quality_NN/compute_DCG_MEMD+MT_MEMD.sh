for tet in TET_AMiner_authors TET_AMiner_coauthors TET_IMDb_genre TET_IMDb_business
do
	norm=`cat $tet/log.-b_5_-w_1_-N_1_-k_3_-M_-C_-S_30_-D_12 | grep ALL | tr ':,' ' ' | awk '{print $2,$3,$4}' | awk '{if ($1==$2 && $2==$3) print "1,1,1"; else {if ($1<$2 && $2<$3) print "1,2,3"; else print "1,1,3"}}' | tr ',' ' ' | awk '{print 1/$1/log(2)*log(2)+1/$2/log(3)*log(2)+1/$3/log(4)*log(2)}'`

	cat $tet/log.-b_5_-w_1_-N_1_-k_3_-M_-C_-S_30_-D_12 | grep RANK | tr ',' ' ' | awk '{print 1/$2/log(2)*log(2)+1/$3/log(3)*log(2)+1/$4/log(4)*log(2)}' | awk -v n="$norm" '{s+=($1/n)}END{print s/NR}'


done

for tet in TET_AMiner_coauthors 
do
	norm=`cat $tet/log.-b_5_-w_1_-N_1_-k_3_-C_-S_30_-D_12 | grep ALL | tr ':,' ' ' | awk '{print $2,$3,$4}' | awk '{if ($1==$2 && $2==$3) print "1,1,1"; else {if ($1<$2 && $2<$3) print "1,2,3"; else print "1,1,3"}}' | tr ',' ' ' | awk '{print 1/$1/log(2)*log(2)+1/$2/log(3)*log(2)+1/$3/log(4)*log(2)}'`

	cat $tet/log.-b_5_-w_1_-N_1_-k_3_-C_-S_30_-D_12 | grep RANK | tr ',' ' ' | awk '{print 1/$2/log(2)*log(2)+1/$3/log(3)*log(2)+1/$4/log(4)*log(2)}' | awk -v n="$norm" '{s+=($1/n)}END{print s/NR}'

done



