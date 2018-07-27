package Tetpackage;

import java.util.*;
import mymath.*;


public class VariableType implements Comparable,Cloneable{

    private final String DEFAULTTYPENAME = "type";
    private String name;

    public VariableType(){
	name = DEFAULTTYPENAME;
    }
    public VariableType(String t){
	name = t;
    }

    /** Returns the type string */
    public String name(){
        return name;
    }

    public String toString(){
        return name;
    }

    public boolean equals(VariableType t)
    {
	return name.equals(t.name());
    }
    
    public int hashCode()
    {	
	return name.hashCode();
    }

    public VariableType clone(){

	return new VariableType(name);
    }

    public int compareTo(Object o){

	int result = 0;
	try{
	    result = name.compareTo(((VariableType)o).name());
	}catch (ClassCastException e){
	     System.out.println("ClassCastException:" + e + " in function VariableType.compareTo");
	     System.exit(1);
	}
	return result;
    }
}
