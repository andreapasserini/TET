list=$1
outfile=$2
h=$3

../../thirdparties/graph-kernels/src/cc/gkernel -k WL -p $h -i $list -g ./ -o $outfile 

