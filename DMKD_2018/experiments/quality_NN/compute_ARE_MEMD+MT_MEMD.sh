for tet in TET_AMiner_authors TET_AMiner_coauthors TET_IMDb_genre TET_IMDb_business
do
	cat $tet/log.-b_5_-w_1_-N_1_-k_3_-M_-C_-S_30_-D_12 | grep NEI | tr ':,' ' ' | awk '{print $2}' > z1
	cat $tet/log.-b_5_-w_1_-N_1_-k_3_-M_-C_-S_30_-D_12 | grep ALL | tr ':,' ' ' | awk '{print $2}' > z2
	paste z1 z2 | awk '{d=$1-$2; if (d>0) print d/$2 ; else print 0}' | awk '{s+=$1}END{print s/NR}'

done

for tet in TET_AMiner_coauthors
do
	cat $tet/log.-b_5_-w_1_-N_1_-k_3_-C_-S_30_-D_12 | grep NEI | tr ':,' ' ' | awk '{print $2}' > z1
	cat $tet/log.-b_5_-w_1_-N_1_-k_3_-C_-S_30_-D_12 | grep ALL | tr ':,' ' ' | awk '{print $2}' > z2
	paste z1 z2 | awk '{d=$1-$2; if (d>0) print d/$2 ; else print 0}' | awk '{s+=$1}END{print s/NR}'

done


