package tet;

import java.io.*;
import mymath.*;
import java.util.*;

public class Value{

    boolean topbool;
    Vector<Multiset> multisets;

    int sizeOfValueSpace; // This should give the number of different values in
                          // this value's value space (for a given domain size n)
                          // Currently is not computed; must be set by hand to some
                          // sufficiently large value;

    int indexInValueSpace; // The index of this value in some ordering of the value
                           // space. Currently not computed; must be set by hand to
                           // some value;

    public Value(){
	this(true,10,0,0);
    }

    public Value(boolean b,int dim, int s, int i){
	topbool = b;
	multisets = new Vector<Multiset>(dim);
	sizeOfValueSpace = s;
	indexInValueSpace = i;
    }

    /* A Value String is something like:
       (T,[(T,[T:4],[T:3]):2,(T,[],[T:3]):3])
    */
    public Value(String valuestring) throws Exception,EmptyValueException{

	this(new StringTokenizer(valuestring,"[(,:)]",true));
    }

    public Value(StringTokenizer tokenizer) throws Exception,EmptyValueException{

	this(); // init value

	String buf = tokenizer.nextToken();

	if(buf.equals(" "))
	    throw new EmptyValueException(); 
	
	if(!buf.equals("(")){ // end of recursion
	    setTopValue(buf); // set top value 
	    return;
	}
	setTopValue(tokenizer.nextToken()); // set top value 

	if(!tokenizer.nextToken().equals(",")) // check comma
	    throw new Exception("Malformed Value string"); 

	do{ // multiset recursion
	    multisets.add(new Multiset(tokenizer));
	}while(!tokenizer.nextToken().equals(")")); // end of multiset recursion	
    }
	
    public boolean getTopbool(){
	return topbool;
    }
    
    public void setTopValue(String valstring) throws Exception{

	if(valstring.equals("T")){
	    topbool = true;
	    return;
	}
	else if(valstring.equals("F")){
	    topbool = false;
	    return;
	}
	throw new Exception("Only true/false value implemented for topbool, found: " + valstring);
    }

    public int getValSpSize(){
	return sizeOfValueSpace;
    }

    public Multiset getMultisetAt(int i){
	return multisets.elementAt(i);
    }

    public int getIndex(){
	return indexInValueSpace;
    }

    public void setMultiset(Multiset ms, int pos){
	multisets.insertElementAt(ms, pos);
    }

    public String topValueString(){
    
	if(topbool)
	    return "T";
	return "F";
    }   

    public String topValueID(){
    
	if(topbool)
	    return "1";
	return "0";
    }   

    public float topValue(){
    
	if(topbool)
	    return (float)1.;
	return (float)0.;
    }   


    public String toString(){

	if(multisets.size() == 0)
	    return topValueString();

	String result = "(";
	result = result + topValueString();
	for (int i=0;i<multisets.size();i++)
	    result = result + "," + multisets.elementAt(i).toString();
	result = result + ")";
	return result;
    }
    
    public boolean isLeaf(){

	return (multisets.size() == 0);
    }

    /* A Value String is something like:
       (T,[(T,[T:4],[T:3]):2,(T,[],[T:3]):3])
    */
    public String writeRDK(){
	return writeRDK(true, false, RDKTypes.ElementProd);
	//return writeRDK(true, false, RDKTypes.ElementMin);
    }

    public String writeRDK(boolean affine, 
			   boolean normalize_inside, 
			   String count_operator){

	String data = "T: " + RDKTypes.RecursiveSortedSparseVect // sparse vector with single entry
	    + " C: 0 N: 1 S: 1 Co: " + RDKTypes.CombineSum
	    + writeRDKNode(2, affine, normalize_inside, count_operator); 
	
	return data;
    }

    public void appendValue(Value v) throws Exception{

	if(v.getTopbool() != topbool)
	    throw new Exception("Value can be appended only if topbool matches");

	multisets.addAll(v.multisets);
    }

    public String writeRDKNode(int level, 
			       boolean affine, 
			       boolean normalize_inside,
			       String count_operator){
	
	if(isLeaf())
	    return writeRDKLeaf();
	
	int N = (normalize_inside) ? 1 : 0; 

	String data = " T: " + RDKTypes.RecursiveDenseVect 
	    + " C: " + topValueID(); // top value is selector
	if(affine){
	    data += " N: 0 S: 2 Co: " + RDKTypes.CombineSum // combine 1 if topvalue match and multisets part 
		+ " T: " + RDKTypes.TerminalDenseVect + " C: 0 N: 0 S: 1 Co: " // add 1 if topvalue match  
		+ RDKTypes.CombineSum + " Eo: " + RDKTypes.ElementProd + " 1 " // add 1 if topvalue match  
		+ " T: " + RDKTypes.RecursiveDenseVect // combine multisets for different branches
		+ " C: 0";
	}
	data += " N: " + N + " S: " + multisets.size() + " Co: " + RDKTypes.CombineSum;
	// increase level
	level++; 
	for (int i=0;i<multisets.size();i++)
	    data += multisets.elementAt(i).writeRDK(level, affine, normalize_inside, count_operator);
	
	return data;
    }

    public String writeRDKLeaf(){
	
	String data = " T: " + RDKTypes.TerminalDenseVect 
	    + " C: " + topValueID() 
	    + " N: 0 S: 1 Co: " // add 1 if topvalue match  
	    + RDKTypes.CombineSum + " Eo: " + RDKTypes.ElementProd + " 1 "; // add 1 if topvalue match 
	
	return data;
    }

    public double kappa(Value v2){
	/* computes experimental kernel function kappa of this value and v2;
	 * v2 must be from the same value space as this value (no error
	 * handling in the code if it doesn't!).
	 */
	boolean thistb = this.topbool;
	boolean v2tb = v2.getTopbool();

	Value tempval1;
	Value tempval2;
	int c1;
	int c2;

	double result = 0;
	if (thistb == v2tb)
	    result = 1;
	if (multisets.size() == 0)
	    return result;
	else{ 
	    for (int i=0;i<multisets.size();i++){
		Multiset ms1 = this.multisets.elementAt(i);
		Multiset ms2 = v2.getMultisetAt(i);
		for (int h=0;h<ms1.getNumberOfElements();h++){
		    tempval1 = ms1.getValueAt(h);
		    c1 = ms1.getCountAt(h);
		    for (int j=0;j<ms2.getNumberOfElements();j++){
			tempval2 = ms2.getValueAt(j);
			c2 = ms2.getCountAt(j);
			result = result + c1*c2*tempval1.kappa(tempval2);
		    }
		}
	    }
	}
	return result;
    }

    public double normalkappa(Value v2){
	double normalfactor = Math.sqrt(this.kappa(this)*v2.kappa(v2));
	return this.kappa(v2)/normalfactor;
    }

    public double[] phi1(){
	if (multisets.size() == 0){
	    double[] res = new double[1];
	    res[0]=-1;
	    if (topbool == true)
		res[0] = 1;
	    return res;
	}
	else{
	    
	    double[] firstpart = new double[1];

	    int[] cvec = multisets.firstElement().countvector();
	    if (topbool == false)
		firstpart[0]=-1;
	    else
		firstpart[0]=1;

	    double[] secondpart = new double[multisets.firstElement().getSizeOfValueSpace()];
	    for (int i=0;i<cvec.length;i++)
		secondpart[i]=cvec[i];

	    double[] thirdpart = multisets.firstElement().sumOfPhi1s();

	    double[] result = MyMathOps.arrayConcat(firstpart,secondpart);
	    result = MyMathOps.arrayConcat(result,thirdpart);

	    return result;
	}
    }

    public double[] phi(){
	if (multisets.size() == 0){
	    double[] res = new double[1];
	    res[0]=-1;
	    if (topbool == true)
		res[0] = 1;
	    return res;
	    }
	else{
	    
	    double[] firstpart = new double[1];

	    int[] cvec = multisets.firstElement().countvector();
	    if (topbool == false)
		firstpart[0]=-1;
	    else
		firstpart[0]=1;

	    double[] secondpart = new double[multisets.firstElement().getSizeOfValueSpace()];
	    for (int i=0;i<cvec.length;i++)
		secondpart[i]=cvec[i];
	    
	    MyMathOps.arrayNormalize(secondpart);

	    double[] thirdpart = multisets.firstElement().sumOfPhis();
	    MyMathOps.arrayNormalizeEuclid(thirdpart);

	    double[] result = MyMathOps.arrayConcat(firstpart,secondpart);
	    result = MyMathOps.arrayConcat(result,thirdpart);
	    MyMathOps.arrayNormalizeEuclid(result);

	    return result;
	}
    }

    public Histogram computeHistogramPhi(int total){	
	
	Histogram histo = new Histogram();

	if(isLeaf())
	    histo.put(1,new Float(topValue()));
	else{
	    for (int i=0;i<multisets.size();i++){
		Histogram child_histo = multisets.elementAt(i).computeHistogramPhi(total);
		histo.add_histogram(child_histo);
	    }
	}
	return histo;
    }

    public static void main(String[] args){
	
	if(args.length == 0 || args[0] == "--help"){
	    System.out.println("Usage: Value <total> < <values>");
	    System.exit(1);
	}

	int total =  Integer.parseInt(args[0]);

	try{
	    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	    	    
	    while(in.ready()){
		String vstring = in.readLine();
		try{
		    Value value = new Value(vstring);
		    System.out.println(value.computeHistogramPhi(total).toString());
		}catch(Exception e) {
		    e.printStackTrace();
		    System.out.println("Parsing value string " + vstring);
		    System.exit(1);
		}
	    }
	    in.close();
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
