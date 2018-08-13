package tet.rnn;

import tet.EmptyValueException;
import java.util.StringTokenizer;
import java.util.Vector;

public class FloatMultiset {
    private class ValueWithCount{
        FloatValue val;
        int count;

        private ValueWithCount(FloatValue v, int c){
            val = v;
            count = c;
        }

        private FloatValue value(){
            return val;
        }

        private int count(){
            return count;
        }
    }

    Vector<ValueWithCount> elements; // Vector of ValueWithCount

    public FloatMultiset(){
        elements = new Vector<ValueWithCount>();
    }

    /* A Multiset String is something like:
       [(T,[T:4],[T:3]):2,(T,[],[T:3]):3]
    */
    public FloatMultiset(StringTokenizer tokenizer) throws Exception{

        this(); // initialize multiset

        if(!tokenizer.nextToken().equals("[")) // check parenthesis
            throw new Exception("Malformed Multiset string");

        do{ // value:count recursion
            try{
                FloatValue val = new FloatValue(tokenizer); // recover value
                if(!tokenizer.nextToken().equals(":"))  // check ':'
                    throw new Exception("Malformed Multiset string");
                Integer count = new Integer(tokenizer.nextToken()); // recover count
                add(val,count); // add value:count pair
            }catch(EmptyValueException e){ // empty value found
                continue; // simply skip
            }
        }while(!tokenizer.nextToken().equals("]")); // end of multiset recursion
    }

    public void add(FloatValue v, int c){
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

    public FloatValue getValueAt(int i){
        return elements.elementAt(i).value();
    }

    public int getCountAt(int i){
        return elements.elementAt(i).count();
    }

}
