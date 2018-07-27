package tet.learner;

import tet.*;

import java.util.*;
import java.io.*;
import mymath.*;

class Extensions extends Vector<Extension>{}

class Extension extends Vector<ExtensionType>{

    public Extension(Pair<Literal,VariableByType> litvarpair)
    {    
	add(new ExtensionType(litvarpair));
    }

    public Extension(Type type, VariableByType varbytype)
    {    
	add(new ExtensionType(type, varbytype));
    }

    public Extension(Pair<Literal,VariableByType> litvarpair, boolean addnegated)
    {   
	this(litvarpair);
	
	if(addnegated){
	    Literal negatedlit = litvarpair.first().clone();
	    negatedlit.setNegated(true);
	    add(new ExtensionType(new Pair(negatedlit,litvarpair.second())));
	}
    }
   
    public Pair<Float,Integer> computeEntropy(TreeMap<Float,Float> root_distribution, boolean use_gig) throws java.lang.ArithmeticException
    {
	Vector<Pair<Float,Integer>> entropyvec = new Vector<Pair<Float,Integer>>();
	
	for(int i = 0; i < this.size(); i++)	 
	    entropyvec.add(this.elementAt(i).computeEntropy(root_distribution, use_gig));
	
	/* sum entropies weighting them by the proportion of their sets sizes */
	return Scorer.weightedSum(entropyvec);
    }

    public String toString()
    {
	StringBuffer buf = new StringBuffer("[");
	
	for(int i = 0; i < this.size(); i++)
	    buf.append(this.elementAt(i) + ",");
	
	if(buf.charAt(buf.length()-1) == ',')
	    buf.setCharAt(buf.length()-1,']');
	else
	    buf.append("]");

	return buf.toString();
    }

    /** checks whether the extension contains a given
     *	literal in any of its extension types 
     */
    public boolean containsLiteral(Literal lit)
    {
	for(int i = 0; i < this.size(); i++)
	    if(this.elementAt(i).containsLiteral(lit))
		return true;
	return false;
    }

    /** checks whether the extension contains a 
     *	literal with relation rel and having variables vars 
     */
    public boolean matchesRelationWithVariables(Relation rel, TreeSet<Variable> vars)
    {
	for(int i = 0; i < this.size(); i++)
	    if(this.elementAt(i).matchesRelationWithVariables(rel, vars))
		return true;
	return false;
    }

    /** check whether the extension contains at least
     *	one of the variables in vars 
     */
    public boolean containsVariable(TreeSet<Variable> vars)
    {
	for(int i = 0; i < this.size(); i++)
	    if(this.elementAt(i).containsVariable(vars))
		return true;
	return false;
    }
    
    /** return the maximum number of new variables introduced
     *	by any of its extension types 
     */
    public int maxNewVarsNum()
    {
	int new_vars_num = 0;

	for(int i = 0; i < this.size(); i++){
	    int curr_new_vars_num = this.elementAt(i).getNewVariables().size();
	    if(curr_new_vars_num > new_vars_num)
		new_vars_num = curr_new_vars_num;
	}
	return new_vars_num;
    }

    /** checks whether the types in extension that do not introduce
     *  new variables have at least one variable from vars
     */
    public boolean attributesContainVariable(TreeSet<Variable> vars)
    {
	for(int i = 0; i < this.size(); i++)
	    if(this.elementAt(i).isAttributeExtensionType() &&
	       !this.elementAt(i).containsVariable(vars))
		return false;
	return true;
    }

    /**
     * removes types containing negated literals 
     */
    public boolean removeNegated()
    {
	boolean removed_anything = false;
	
	for(int i = 0; i < this.size(); i++)
	    if(this.elementAt(i).hasNegatedLiterals()){
		this.remove(i);
		removed_anything = true;
	    }
	
	return removed_anything;
    }

    /**
     * check whether the extension introduces new variables 
     */
    public boolean isEntityExtension()
    {
	for(int i = 0; i < this.size(); i++)
	    if(this.elementAt(i).isEntityExtensionType())
		return true;
	return false;
    }

    /**
     * check whether the extension contains only types matching 
     * a given type 
     */
    public boolean allTypesMatch(Type type)
    {
	for(int i = 0; i < this.size(); i++)
	    if(!this.elementAt(i).typeMatches(type))
		return false;
	return true;
    }

    public void cleanup()
    {
	for(int i = 0; i < this.size(); i++)
	    this.elementAt(i).cleanup();
    }
}

class ExtensionType{

    Pair<Type,VariableByType> typevarpair;
    Dataset dataset;
    VariableByType allvars;

    public ExtensionType(Pair<Literal,VariableByType> litvarpair)
    {
	typevarpair = new Pair(new Type(litvarpair.first()), litvarpair.second());
    }

    public ExtensionType(Type type, VariableByType varbytype)
    {
	this.typevarpair = new Pair(type, varbytype);
    }

    public void computeAllVars(VariableByType root_vars)
    {
	allvars = root_vars.merge(typevarpair.second());
    }
    
    public void setDataset(Dataset dataset)
    {
	this.dataset = dataset;
    }

    public Dataset getDataset()
    {
	return this.dataset;
    }

    public Type getType()
    {
	return typevarpair.first();
    }

    public Pair<Type,VariableByType> getTypeVarPair() 
    {
	return typevarpair;
    }       

    public TreeSet<Variable> getNewVariables()
    {
	return typevarpair.second().getVariables();
    }

    public VariableByType getNewVariablesByType()
    {
	return typevarpair.second();
    }

    public VariableByType getAllVariables()
    {
	return allvars;
    }

    public Pair<Float,Integer> computeEntropy(TreeMap<Float,Float> root_distribution, boolean use_gig) throws java.lang.ArithmeticException
    {
	return dataset.computeEntropy(getNewVariables(), root_distribution, use_gig);
    }

    public String toString()
    {
	StringBuffer buf = new StringBuffer("[(");
	
	TreeSet<Variable> newvars = getNewVariables();

	for(Iterator<Variable> i = newvars.iterator(); i.hasNext(); )
	    buf.append(i.next() + ",");
	
	if(buf.charAt(buf.length()-1) == ',')
	    buf.setCharAt(buf.length()-1,')');
	else
	    buf.append(")");

	buf.append(",[" + typevarpair.first() + "]");
	
	return buf.toString();
    }

    /** checks whether the extension type contains 
	a given literal  */
    public boolean containsLiteral(Literal lit)
    {
	return typevarpair.first().containsLiteral(lit);
    }

    
    /** checks whether the extension type contains a 
     *	literal with relation rel and having variables vars 
     */
    public boolean matchesRelationWithVariables(Relation rel, TreeSet<Variable> vars)
    {
	return typevarpair.first().matchesRelationWithVariables(rel, vars);
    }

    /** check whether the extension type contains at least
     *	one of the variables in vars 
     */
    public boolean containsVariable(TreeSet<Variable> vars)
    {
	return typevarpair.first().containsVariable(vars);
    }

    public boolean isAttributeExtensionType()
    {
	return (typevarpair.second().size() == 0);
    }

    /**
     * check whether the extension type introduces new variables 
     */
    public boolean isEntityExtensionType()
    {
	return (typevarpair.second().size() > 0);
    }
    
    /** 
     * check whether the type has any negated literal
     */
    public boolean hasNegatedLiterals()
    {
	return  typevarpair.first().hasNegatedLiterals();
    }

    /**
     * check whether the extension type matches the 
     * given type 
     */
    public boolean typeMatches(Type type)
    {
	return typevarpair.first().matches(type);
    }

    public void cleanup()
    {
	dataset.cleanup();
    }
}
