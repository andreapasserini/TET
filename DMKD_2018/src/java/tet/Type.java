package tet;

import java.util.*;
import mymath.*;

class LiteralByVariable extends Pair<String,Pair<Integer,Integer>>{   

    public LiteralByVariable(String s , Pair<Integer,Integer> p)
    {
	first = s;
	second = p;
    }
};
class LiteralByVariableVec extends Vector<LiteralByVariable>{};
class LiteralByVariableVecMap extends HashMap<String,LiteralByVariableVec>{};



public class Type{

    Vector<Literal> literals; /* Vector of literals */

    public Type()
    {
	literals = new Vector<Literal>();
    }

    public Type(Literal lit)
    {
	this();
	addLiteral(lit);
    }

    public Type(Type t)
    {
	literals = new Vector<Literal>(t.literals.size());
	for(int i = 0; i < t.literals.size(); i++)
	    literals.add(i, new Literal(t.literals.elementAt(i)));
    }

    public Type(String typestring) throws Exception
    {
        this(new StringTokenizer(typestring,"(,)",true));
    }

    /* Type string is something like (s(u,v),t(w))..... */
    public Type(StringTokenizer tokenizer) throws Exception
    {
	String buf;

	if(!tokenizer.nextToken().equals("("))
	    throw new Exception("Malformed Type string"); 
	
	literals = new Vector<Literal>();
	
	do{
	    buf = tokenizer.nextToken();
	    if(buf.equals(")"))
		break;
	    if(!buf.equals(","))
		literals.add(new Literal(buf,tokenizer));
	}while(!tokenizer.nextToken().equals(")")); // end of literals list
    }
    
    /* Type string is something like (s(u,v),t(w))..... */
    public String toString(){
	
	StringBuffer buf = new StringBuffer();

	buf.append("(");

	for (int i=0;i<literals.size(); i++)
	    buf.append(literals.elementAt(i).toString() + ",");
	 if(literals.size() > 0)
	     buf.setCharAt(buf.length()-1,')');
	 else
	     buf.append(")");
	 
	 return buf.toString();
    }
  
    public int getNumLiterals()
    {
	return literals.size();
    }

    public Vector<Literal> getLiterals()
    {
	return literals;
    }    

    public void addLiteral(Literal lit){
	literals.add(lit);
    }
    
    /** 
     * recover all variables contained in the type
     * literals
     */
    public TreeSet<Variable> getVariables()
    {
	TreeSet<Variable> vars = new TreeSet<Variable>();

	for (int i=0;i<literals.size();i++)
	    vars.addAll(literals.elementAt(i).getVariables());

	return vars;	
    }
    
    /** Substitutes obj for var in all literals in this type */
    public void substitute(Variable var, RelObject obj){
	for (int i=0;i<literals.size();i++){
	    literals.elementAt(i).substitute(var,obj);
	}
    }
 
    /** Returns a vector of all the Variables appearing in all literals */
    public TreeSet<Variable> allVars(){
	TreeSet<Variable> result = new TreeSet<Variable>();
	for (int i=0;i<literals.size(); i++){
	    result.addAll(literals.elementAt(i).allVars());
	}
	return result;
    }

   /** Returns a map having for each variable, a vector of
	occurrences of the variable in the literals of the type, where
	each occurrence is represented as a pair: the name of the
	relation and a pair with the position of the literal in the
	type and the position of the variable as argument of the
	relation. Possibly skip operator literals.
     */
    public LiteralByVariableVecMap literalsByVariable(TreeSet<Variable> vars, boolean skip_operators)
    {
	LiteralByVariableVecMap literalsbyvar = new LiteralByVariableVecMap();

	/* fill map with pairs */
	for(int i = 0; i < literals.size(); i++){
	    Literal literal = literals.elementAt(i);
	    if(skip_operators && literal.rel.isOperator()) // skip operator if needed
		continue;
	    for(int v = 0; v < literal.arguments.length; v++){
		Variable var = (Variable)literal.arguments[v];
		if(vars.contains(var)){
		    /* recover literal vector corresponding to variable */
		    LiteralByVariableVec vec = literalsbyvar.get(var.name());
		    if (vec == null) /* if it's missing initialize map entry */
			literalsbyvar.put(var.name(), vec = new LiteralByVariableVec());
		    /* add current literal by variable to literal vector */
		    vec.add(new LiteralByVariable(literal.rel.name(), 
						  new Pair<Integer, Integer>(new Integer(i),
									     new Integer(v+1))));
		}
	    }
	}
	
	return literalsbyvar;
    }


    /** check whether one of the type literals matches
	exactly the given literal 
	(including variables or relational objects) */
    public boolean containsLiteral(Literal lit)
    {
	for(int i = 0; i < literals.size(); i++)
	    if(literals.elementAt(i).matches(lit))
		return true;
	return false;
    }

    /** checks whether one of the type literals matches
     *	relation rel and has variables vars 
     */
    public boolean matchesRelationWithVariables(Relation rel, TreeSet<Variable> vars)
    {
	for(int i = 0; i < literals.size(); i++)
	    if(literals.elementAt(i).matchesRelationWithVariables(rel, vars))
		return true;
	return false;
    }

    /** check whether one of the type literals contains at least
     *	one of the variables in vars 
     */
    public boolean containsVariable(TreeSet<Variable> vars)
    {
	for(int i = 0; i < literals.size(); i++)
	    if(literals.elementAt(i).containsVariable(vars))
		return true;
	return false;
    }

    /** 
     * check whether the type has any negated literal
     */
    public boolean hasNegatedLiterals()
    {
	for(int i = 0; i < literals.size(); i++)
	    if(literals.elementAt(i).negated)
		return true;
	return false;
    }

    /** check whether all literals match exactly literal lit  
     *	(including variables or relational objects) */
    public boolean allLiteralsMatch(Literal lit)
    {
	for(int i = 0; i < literals.size(); i++)
	    if(!literals.elementAt(i).matches(lit))
		return false;
	return true;
    }
    
    /** check whether the type matches type in a regular
     *  expression fashion
     */
    public boolean matches(Type type)
    {
	/* collect number of type literals */
	int type_litnum = type.literals.size();
	int litnum = this.literals.size();

	/* try to place type within this type, starting from the beginning */
	for(int i = 0; i < litnum; i++){

	    if(type_litnum > (litnum-i)) 
		/* not enouch literals left */
		return false;

	    /* iterate over type literals and try to match each of them */
	    int j = 0;
	    for(; j < type_litnum; j++)
		if(!this.literals.elementAt(i+j).matches(type.literals.elementAt(j))) 
		    /* this literal does not match, try moving along this literals */
		    break; 
	    
	    if(j == type_litnum)
		/* a full match took place */
		return true; 
	}

	/* if we arrived here, no full match took place */
	return false;
    }

    /** 
     * check whether the type is the negation of type
     */
    public boolean isNegationOf(Type type)
    {
	/* if there are zero literals then everything is true */
	/* if there are more than one literal, it cannot be the
	   negation as one of the two should be a disjunction */
	if(this.literals.size() != 1 || type.getNumLiterals() != 1)
	    return false;

	/* literals do not match */
	if(!this.literals.firstElement().matches(type.literals.firstElement()))
	    return false;

	/* literals are both asserted or negated */	
	if(this.literals.firstElement().negated == type.literals.firstElement().negated)
	    return false;

	return true;
    }

    /**
     * check whether the type is the trivial
     * T() type
     */
    public boolean isEmptyType()
    {
	return (literals.size() == 0);
    }

    public Type clone()
    {
	Type clonetype = new Type();

	for(int i = 0; i < literals.size(); i++)
	    clonetype.addLiteral(literals.elementAt(i).clone());
	
	return clonetype;
    }

    public boolean isSymmetricType()
    {
	for(int i = 0; i < literals.size(); i++)
	    if(!literals.elementAt(i).isSymmetric())
		return false;
	return true;
    }
}
