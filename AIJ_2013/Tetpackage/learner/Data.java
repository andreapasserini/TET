package Tetpackage.learner;

import Tetpackage.*;

import java.util.*;
import java.io.*;
import mymath.*;

public class Data{

    float target;
    HashMap<String, RelObject> relobjs;

    public Data(TreeSet<Variable> vars, String buf) throws Exception
    {	
	this(vars, new StringTokenizer(buf," ",false));
    }
    
    public Data(TreeSet<Variable> vars, StringTokenizer tokenizer) throws Exception
    {   
	relobjs = new HashMap<String, RelObject>();
	
	// parse rel object values
	for(Iterator<Variable> i = vars.iterator(); i.hasNext();){
	    if(!tokenizer.hasMoreTokens())
		throw new Exception("Malformed data string, expected " + (vars.size()+1) + " elements");
	    relobjs.put(i.next().name(), new RelObject(tokenizer.nextToken()));
	}

	// parse final target value
	if(!tokenizer.hasMoreTokens())
	    throw new Exception("Malformed data string, expected " + (vars.size()+1) + " elements");
	target = Float.parseFloat(tokenizer.nextToken());
    }    
}
