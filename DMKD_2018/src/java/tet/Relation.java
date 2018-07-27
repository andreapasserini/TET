package tet;

import java.util.*;
import mymath.*;

public class Relation implements Cloneable{

    private String name;
    private int arity;
    private VariableType[] argstype;

    public Relation(String n, int a){
	name = n;
	arity = a;
	argstype = null;
    } 

    public Relation(String n, int a, VariableType[] atype){
	name = n;
	arity = a;
	argstype = atype;
    } 

    public Relation(String relstring) throws Exception{
	this(new StringTokenizer(relstring,"(,)",true));	
    }
    
    public Relation(StringTokenizer tokenizer) throws Exception{

	String buf;
	int i = 0;

	name = tokenizer.nextToken();

	if(!(tokenizer.nextToken().equals("(")))
	    throw new Exception("Malformed Relation string: missing starting '('");
	
	arity = tokenizer.countTokens() / 2;

	argstype = new VariableType[arity];	
	do{
	    buf = tokenizer.nextToken();
	    if(!buf.equals(")") && !buf.equals(","))  
		argstype[i++] = new VariableType(buf);
	} while(!(buf.equals(")"))); // end of edge labels

	if(tokenizer.hasMoreTokens()) 
	    throw new Exception("Malformed Relation string: found characters after ')'");
    }

    public String name(){
	return name;
    }

    public int arity(){
	return arity;
    }
    
    public String toString(){
	
	return name;
    }

    public String toTypeString(){
	
	StringBuffer buf = new StringBuffer(1024);
        
        buf.append(name + "(");
        
        for (int i=0;i<arity; i++)
            buf.append((argstype[i]).toString()  + ",");
        if(arity > 0)
            buf.setCharAt(buf.length()-1,')');
        else
            buf.append(")");
        
        return buf.toString();  
    }

    public VariableType argType(int i)  throws Exception{
	
	if(i >= arity)
	    throw new Exception("Index out of bounds in trying to access argstype"); 

	if(argstype == null)
	    throw new Exception("ArgsType not defined for this relation"); 

	return argstype[i];
    }    

    public Relation clone()
    {	
	VariableType[] clone_argstype = null;

	if(argstype != null){
	    clone_argstype = new VariableType[arity];
	    for (int i=0;i<arity; i++)
		clone_argstype[i] = argstype[i].clone();
	}

	return new Relation(name, arity, clone_argstype);
    }

    public boolean hasArgsType()
    {
	return (argstype != null);
    }

    public boolean equals(Relation rel)
    {
	/* name differs */
	if(!this.name.equals(rel.name()))
	    return false;

	/* arity differs */
	if(this.arity != rel.arity())
	    return false;

	/* no argstype defined for at least one relation */
	if(!hasArgsType() || !rel.hasArgsType())
	    return true;
	
	/* argstype differ */
	try{
	    for (int i=0;i<arity; i++)
		if(!argstype[i].equals(rel.argType(i)))
		    return false;
	}catch(Exception e){ 
	    /* exception should never happen, exit */
	    e.printStackTrace();
            System.exit(1);
	}

	return true;
    }

    public boolean isOperator()
    {
	return OperatorSet.isOperator(name);
    }

    public boolean isSymmetric()
    {
	/* must be binary */
	if(arity != 2)
	    return false;

	/* must start with sym or same or be <> or = */
	if(name.startsWith("sym") || 
	   name.startsWith("same") ||  
	   name.startsWith("Same") ||  
	   name.equals("<>") || 
	   name.equals("="))
	    return true;	    

	return false;
    }
}
