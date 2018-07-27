package tet;

import java.util.*;
import mymath.*;
import myio.*;

public class Multiset{
    

    private class ValueWithCount{
	Value val;
	int count;
	
	private ValueWithCount(Value v, int c){
	    val = v;
	    count = c;
	}
	
	private Value value(){
	    return val;
	}

	private int count(){
	    return count;
	}
    }

    Vector<ValueWithCount> elements; // Vector of ValueWithCount

    public Multiset(){
	elements = new Vector<ValueWithCount>();
    } 

    /* A Multiset String is something like:
       [(T,[T:4],[T:3]):2,(T,[],[T:3]):3]
    */
    public Multiset(StringTokenizer tokenizer) throws Exception{
	
	this(); // initialize multiset

	if(!tokenizer.nextToken().equals("[")) // check parenthesis
	    throw new Exception("Malformed Multiset string"); 
	
	do{ // value:count recursion
	    try{
		Value val = new Value(tokenizer); // recover value
		if(!tokenizer.nextToken().equals(":"))  // check ':'
		    throw new Exception("Malformed Multiset string"); 
		Integer count = new Integer(tokenizer.nextToken()); // recover count
		add(val,count); // add value:count pair
	    }catch(EmptyValueException e){ // empty value found 
		continue; // simply skip		
	    }
	}while(!tokenizer.nextToken().equals("]")); // end of multiset recursion	
    }

    public void add(Value v, int c){  
	/* Note: it is not checked whether 'v' already exists
	 * in multiset, and only count needs to be incremented!
	 */
	elements.add(new ValueWithCount(v,c));
    }

    public String toString(){

	if(elements.size() == 0)
	    return "[ ]";

	String result = "[";
	for (int i=0;i<elements.size();i++){
	    ValueWithCount wc = elements.elementAt(i);
	    result = result + wc.value().toString() + ":" + wc.count() + ",";
	}
	return result.substring(0,result.length()-1) + "]";
    }


//     public int getLengthOfPhi(){
// 	/* Multisets contain values of the same type that 
// 	 * have phi-values of identical length
// 	 */
// 	Value anyvalue = ((ValueWithCount)elements.elementAt(0)).value();
// 	return anyvalue.phi().length;
//     }

    public int getNumberOfElements(){
	return elements.size();
    }

    public int getSizeOfValueSpace(){
	/* All values in the multiset should come from the
	 * same value space
	 */
	Value anyvalue = elements.elementAt(0).value();
	return anyvalue.getValSpSize();
    }

    public Value getValueAt(int i){
	return elements.elementAt(i).value();
    }

    public int getCountAt(int i){
	return elements.elementAt(i).count();
    }

    public String writeRDK(int level, 
			   boolean affine, 
			   boolean normalize_inside,
			   String count_operator){	

	String data = " T: " + RDKTypes.RecursiveUnsortedMultiSparseVect 
	    + " C: 0 N: 0 S: " + elements.size() + " Co: " + RDKTypes.CombineSum; 
	
	for (int i=0;i<elements.size();i++){
	    data += " T: " + RDKTypes.RecursiveDenseVect 
		+ " C: " + elements.elementAt(i).value().topValueID() // top value is selector
		+ " N: 0 S: 2 Co: " + RDKTypes.CombineProd  // combine count and value by their product
		+ " T: " + RDKTypes.TerminalDenseVect + " C: 0 N: 0 S: 1 Co: "; // count part
	    if(elements.elementAt(i).value().isLeaf())
		data += RDKTypes.CombineSum + " Eo: " + count_operator + " "; // user defined operator
	    else
		data += RDKTypes.CombineSum + " Eo: " + RDKTypes.ElementProd + " "; // product for internal nodes
	    data += elements.elementAt(i).count() // count part
		+ elements.elementAt(i).value().writeRDKNode(level, affine, normalize_inside,count_operator); // value part
	}

	return data;
    }

    public int[] countvector(){
	int[] result = new int[this.getSizeOfValueSpace()];
	for (int i=0;i<elements.size();i++){
	    ValueWithCount wc = elements.elementAt(i);
	    result[wc.value().getIndex()]=wc.count();
	}
// 	System.out.println(StringOps.arrayToString(result));
	return result;
    }

    public double[] sumOfPhi1s(){
	ValueWithCount wc = elements.elementAt(0);
	Value val = wc.value();
	int c = wc.count();
	double[] result = MyMathOps.arrayScalMult(val.phi1(), c);

	for (int i=1;i<elements.size();i++){
	    wc = elements.elementAt(i);
	    val = wc.value();
	    c = wc.count();
	    result = MyMathOps.arrayAdd(result,MyMathOps.arrayScalMult(val.phi1(), c));
	}
	return result;
    }

    public double[] sumOfPhis(){
	ValueWithCount wc = elements.elementAt(0);
	Value val = wc.value();
	int c = wc.count();
	double[] result = MyMathOps.arrayScalMult(val.phi(), c);

	for (int i=1;i<elements.size();i++){
	    wc = elements.elementAt(i);
	    val = wc.value();
	    c = wc.count();
	    result = MyMathOps.arrayAdd(result,MyMathOps.arrayScalMult(val.phi(), c));
	}
	return result;
    }

    public Histogram computeHistogramPhi(int total){	
	
	Histogram histo = new Histogram();

	for (int i=0;i<elements.size();i++){
	    Histogram child_histo = elements.elementAt(i).value().computeHistogramPhi(total);
	    child_histo.replicate_histogram(elements.elementAt(i).count(), total);
	    histo.add_histogram(child_histo);
	}
	return histo;
    }

}