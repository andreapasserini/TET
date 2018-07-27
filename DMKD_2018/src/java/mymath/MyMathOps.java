package mymath;

public class MyMathOps extends java.lang.Object{

    public static double[] arrayAdd(double[] a1, double[] a2){
	if (a1.length != a2.length)
	    throw new ArithmeticException("cannot add arrays of unequal length");
	else {
	    double[] result = new double[a1.length];
	    for (int i=0;i<result.length;i++)
		result[i]=a1[i]+a2[i];
	    return result;
	}
    }

    public static void arrayNormalize(double[] a){
	double sum = 0;
	for (int i=0;i<a.length;i++)
	    sum = sum + a[i];
	for (int i=0;i<a.length;i++)
	    a[i] = a[i]/sum;
    }

    public static void arrayNormalizeEuclid(double[] a){
	double sum = 0;
	for (int i=0;i<a.length;i++)
	    sum = sum + a[i]*a[i];
	sum = Math.sqrt(sum);
	for (int i=0;i<a.length;i++)
	    a[i] = a[i]/sum;
    }

    public static double[] arrayScalMult(double[] a, double l){
	double[] result = new double[a.length];
	for (int i=0;i<result.length;i++)
	    result[i]=l*a[i];
	return result;
    }

    public static double[] arrayConcat(double[] a, double[] b){
	double[] result = new double[a.length + b.length];
	for (int i=0;i<a.length;i++)
	    result[i]= a[i];
	for (int i=0;i<b.length;i++)
	    result[a.length + i]= b[i];
	return result;
    }


    public static double ce(double[] p, double[] q){
	// returns -1 to indicate infinite CE
	double result = 0;
	if (p.length != q.length){
	    System.out.println("Cannot compute Cross-Entropy for Vectors of different length!");
	    return 0;
	}
	for (int i=0;i<p.length;i++){
	    if (q[i]==0){
		if (p[i]>0)
		    return -1;
	    }
	    if (p[i]>0)
		result = result + p[i]*Math.log(p[i]/q[i]);
	}
	return result;
    }

    public static double innerProduct(double[] a, double[] b){
	double result = 0;
	if (a.length != b.length){
	    System.out.println("Cannot compute inner product for Vectors of different length!");
	    return 0;
	}
	for (int i=0;i<a.length;i++)
	    result = result + a[i]*b[i];
	return result;
    }

       public static int intPow(int k, int l)
        // returns k to the power of l
        {
            int result =1;
            for (int i =0 ; i<l; i++) result = result*k;
            return result;
        }

    public static int[] intarrayadd(int[] a, int[] b){
	if (a.length != b.length) 
	    throw new IllegalArgumentException("Attempting to add vectors of unequal length");
	int[] result = new int[a.length];
	for (int i = 0; i<a.length; i++) result[i] = a[i]+ b[i];
	return result;
    } 

    public static int[] indexto01array(int ind,int size){
	// computes the 0/1-array with index ind in enumeration
	// of all 0/1 arrays of length size (i.e. binary representation
	// of ind with leading zeros
	int[] result = new int[size];
	int rem;
	for (int i=0;i<size;i++){
	    rem = ind % 2;
	    ind = ind/2;
	    result[size-1-i]=rem;
	}
	return result;
    }

    public static double roundDouble(double roundthis, int digits){
	//System.out.println("Rounding " + roundthis );
	double result = roundthis;
	result = Math.floor(result*Math.pow(10,digits)+0.5);
	result = result/ Math.pow(10,digits);
	//System.out.println("  ... returning " + result );
	return result;
    }
}
