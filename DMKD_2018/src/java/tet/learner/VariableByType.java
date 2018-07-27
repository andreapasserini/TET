package tet.learner;

import tet.*;

import java.util.*;
import mymath.*;

public class VariableByType extends HashMap<String, TreeSet<Variable>>
    implements Cloneable
{
    public VariableByType(){
    }    

    public VariableByType(String obtstring) throws Exception{
	this(new StringTokenizer(obtstring," =",true));
    }
    
    public VariableByType(StringTokenizer tokenizer) throws Exception{
	
	while(tokenizer.hasMoreTokens()){
	    String objname = tokenizer.nextToken();
	    if(! tokenizer.nextToken().equals("="))
		throw new Exception("Malformed VariableByType string, missing '='");
	    String objtype = tokenizer.nextToken();
	    addVariable(objtype, objname);
	    if(tokenizer.hasMoreTokens() && !tokenizer.nextToken().equals(" "))
		throw new Exception("Malformed VariableByType string, missing ' '");
	} 
    }
    
    public void addVariable(String typename, String varname)
    {	
	addVariable(typename, new Variable(varname));
    }

    public void addVariable(VariableType type, Variable var)
    {
	addVariable(type.toString(), var);
    }

    public void addVariable(VariableType type, String varname)
    {
	addVariable(type.toString(), new Variable(varname));
    }

    public void addVariable(String typename, Variable var)
    {
	TreeSet<Variable> varset = this.get(typename);
	
	if(varset == null)
	    varset = new TreeSet<Variable>();
	varset.add(var);
	this.put(typename,varset);	
    }

    public TreeSet<Variable> get(VariableType type)
    {
	return this.get(type.toString());
    }

    public String toString()
    {
	if(this.size() == 0)
	    return "";

	StringBuffer buf = new StringBuffer(1024);
	Set<Map.Entry<String,TreeSet<Variable>>> entryset = this.entrySet();
	
	for(Iterator<Map.Entry<String,TreeSet<Variable>>> i = entryset.iterator(); i.hasNext() ;){
	    Map.Entry<String,TreeSet<Variable>> entry = i.next();
	    for(Iterator<Variable> v = entry.getValue().iterator(); v.hasNext();)
		buf.append(v.next() + "=" + entry.getKey() + " ");	    	    
	}
	buf.deleteCharAt(buf.length()-1);

	return buf.toString();
    }
    
    public VariableByType clone()
    {
	VariableByType clone = new VariableByType();

	Set<Map.Entry<String,TreeSet<Variable>>> entryset = this.entrySet();
	
	for(Iterator<Map.Entry<String,TreeSet<Variable>>> i = entryset.iterator(); i.hasNext() ;){
	    Map.Entry<String,TreeSet<Variable>> entry = i.next();
	    TreeSet<Variable> clone_entry_set = new TreeSet<Variable>();
	    for(Iterator<Variable> v = entry.getValue().iterator(); v.hasNext();)
		clone_entry_set.add(v.next().clone());
	    clone.put(entry.getKey(),clone_entry_set);
	}
	return clone;
    }

    public TreeSet<Variable> getVariables()
    {
	TreeSet<Variable> vars = new TreeSet<Variable>();

	Set<Map.Entry<String,TreeSet<Variable>>> entryset = this.entrySet();
	
	for(Iterator<Map.Entry<String,TreeSet<Variable>>> i = entryset.iterator(); i.hasNext() ;)
	    vars.addAll(i.next().getValue());       
	
	return vars;
    }

    public int getNumVariables()
    {
	int num = 0;

	Set<Map.Entry<String,TreeSet<Variable>>> entryset = this.entrySet();
	
	for(Iterator<Map.Entry<String,TreeSet<Variable>>> i = entryset.iterator(); i.hasNext() ;)
	    num += i.next().getValue().size();

	return num;
    }

    public VariableByType merge(VariableByType vars2)
    {
	VariableByType vars = this.clone();
	vars.addAll(vars2);
	
	return vars;
    }

    public void addAll(VariableByType vars)
    {
	Set<Map.Entry<String,TreeSet<Variable>>> entryset = vars.entrySet();
	
	for(Iterator<Map.Entry<String,TreeSet<Variable>>> i = entryset.iterator(); i.hasNext() ;)
	    add(i.next());
    }

    public void add(Map.Entry<String,TreeSet<Variable>> entry)
    {
	TreeSet<Variable> varset = this.get(entry.getKey());

	if(varset == null)
	    this.put(entry.getKey(), entry.getValue());
	else
	    varset.addAll(entry.getValue());
    }
}