package Tetpackage;

import java.util.*;

public class Pair<First, Second>
    extends java.lang.Object{

    First first;
    Second second;

    public Pair(){
    }
    
    public Pair(First _first, Second _second){
	first = _first;
	second = _second;
    }
    
    public First first(){
	return first;
    }

    public Second second(){
	return second;
    }

    public String toString(){
	return "<" + first.toString() + "," + second.toString() + ">";
    }
}
