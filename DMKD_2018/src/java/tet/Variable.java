package tet;

import java.util.*;
import mymath.*;

public class Variable implements Comparable,Cloneable{

    private final String DEFAULTVARNAME = "var";
    String name;
    private VariableType type;

    public Variable(){	
	name = DEFAULTVARNAME;
	type = new VariableType();
    }
  

    public Variable(String n){
	name = n;
	/* assume variable names are made of variable type + variable identifier starting with a digit */
	type = new VariableType(TetUtilities.trimUpToDigit(n));
    }
  
    public Variable(String n, String t)
    {
	name = n;
	type = new VariableType(t);
    }

    public Variable(String n, VariableType t)
    {
	name = n;
	type = t;
    }

    public String type()
    {
	return type.name();
    }

    /** Returns the name of this variable */
    public String name(){
	return name;
    }

    /** Tests whether obj is a variable with the same name as this variable */
    public boolean equals(Object obj){
	if (obj instanceof Variable && ((Variable)obj).name().equals(name))
	    return true;
	else
	    return false;
    }
    
    public int compareTo(Object o){

	int result = 0;
	try{
	    result = name.compareTo(((Variable)o).name());
	}catch (ClassCastException e){
	     System.out.println("ClassCastException:" + e + " in function Variable.compareTo");
	     System.exit(1);
	}
	return result;
    }

    public String toString(){
	
	return name;
    }    

    public Variable clone(){
	
	return new Variable(name, type.clone());
    }
}
