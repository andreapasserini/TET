package tet.rnn;

import tet.EmptyValueException;

import java.util.StringTokenizer;
import java.util.Vector;

public class FloatValue {
    // This class is almost the equivalent of the Value class,
    // instead of the bool value it contains a float.

    float value;
    Vector<FloatMultiset> multisets;

    int sizeOfValueSpace; // This should give the number of different values in
    // this value's value space (for a given domain size n)
    // Currently is not computed; must be set by hand to some
    // sufficiently large value;

    int indexInValueSpace; // The index of this value in some ordering of the value
    // space. Currently not computed; must be set by hand to
    // some value;

    public FloatValue(){
        this(0,10,0,0);
    }

    public FloatValue(float v){
        this(v, 10, 0,0 );
    }

    public FloatValue(float v,int dim, int s, int i){
        value = v;
        multisets = new Vector<FloatMultiset>(dim);
        sizeOfValueSpace = s;
        indexInValueSpace = i;
    }

    public FloatValue(String valuestring) throws Exception, EmptyValueException {

        this(new StringTokenizer(valuestring,"[(,:)]",true));
    }

    public FloatValue(StringTokenizer tokenizer) throws Exception,EmptyValueException{

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
            multisets.add(new FloatMultiset(tokenizer));
        }while(!tokenizer.nextToken().equals(")")); // end of multiset recursion
    }

    public float getTopValue(){
        return value;
    }

    public void setTopValue(String valstring){
        try {
            value = Float.parseFloat(valstring);
        }catch(NumberFormatException e){
            e.printStackTrace();
        }
    }

    public void setTopValue(float v) { value = v; }

    public void setMultiset(FloatMultiset ms, int pos){
        multisets.insertElementAt(ms, pos);
    }

    public FloatMultiset getMultisetAt(int i){
        return multisets.elementAt(i);
    }

    public int getNumberOfMultisets(){ return multisets.size(); }

    public boolean isLeaf(){

        return (multisets.size() == 0);
    }

    public String topValueString(){
        return String.valueOf(value);
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
}
