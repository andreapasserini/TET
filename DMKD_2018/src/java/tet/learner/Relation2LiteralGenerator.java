package tet.learner;

import tet.*;

import java.util.*;
import java.io.*;
import mymath.*;

public class Relation2LiteralGenerator{

    Relation template;
    Vector<int[]> modes;
    int arity;

    static final int I = 0, O = 1, IO = 2; // rmodes

    Vector<Literal> literals;
    Vector<VariableByType> newvariables;
    VariableByType variables;

    Counter name_counter;
    Counter literal_counter;
    String nameprefix;

    public Relation2LiteralGenerator(Relation rel) throws Exception
    {
	template = rel;
	arity = rel.arity();	
	modes = new Vector<int[]>();

	name_counter = new Counter();
	literal_counter = new Counter();
    }    

    public Relation2LiteralGenerator(Relation rel, Relation mod) throws Exception
    {
	this(rel);

	setModes(mod);
    }
    
    public void setModes(Relation mod) throws Exception
    {
	int[] currmodes = new int[arity];
	for(int i = 0; i < arity; i++)
	    currmodes[i] = toRMode(mod.argType(i).name());
	
	modes.add(currmodes);
    }

    public int generateLiterals(VariableByType obt, String prefix) throws Exception
    {
	literals = new Vector<Literal>();
	newvariables = new Vector<VariableByType>();
	variables = obt;
	name_counter.clear();
	literal_counter.clear();
	nameprefix = prefix;

	Literal startlit = new Literal(template, false);
	VariableByType startnewobt = new VariableByType();
	
	for(int m = 0; m < modes.size(); m++)
	    generateLiterals(startlit, startnewobt, modes.elementAt(m), 0); 	

	return literals.size();
    }

    public void generateLiterals(Literal srclit, VariableByType srcnewvar, int[] mode, int pos) throws Exception
    {
	Literal lit = srclit.clone();
	VariableByType newvar = srcnewvar.clone();

	if(pos >= arity)
	    throw new Exception("Position " + pos + " exceeds template arity " + arity);

	VariableType vtype = template.argType(pos);

	switch(mode[pos]){
	case I:
	    generateLiterals(lit, variables.get(vtype), newvar, mode, pos); 
	    break;
	case O:
	    generateLiterals(lit, newvar, vtype, mode, pos); 
	    break;
	case IO:
	    generateLiterals(lit, variables.get(vtype), newvar, mode, pos); 
	    generateLiterals(lit, newvar, vtype, mode, pos); 
	    break;
	}
    }
    
    public void generateLiterals(Literal srclit, 
				 TreeSet<Variable> varpos, 
				 VariableByType srcnewvar, 
				 int[] mode, 
				 int pos) throws Exception
    {
	Literal lit = srclit.clone();
	VariableByType newvar = srcnewvar.clone();

	if(varpos == null) // no  variable satisfies type
	    return;

	for(Iterator<Variable> i = varpos.iterator(); i.hasNext();)
	    generateLiterals(lit, i.next(), newvar, mode, pos);
    }

    public void generateLiterals(Literal srclit, 
				 Variable var, 
				 VariableByType srcnewvar, 
				 int[] mode, 
				 int pos) throws Exception
    {
	Literal lit = srclit.clone();
	VariableByType newvar = srcnewvar.clone();

	lit.setVariable(pos, var); // place current variable at position pos in current literal
	
	if(pos == arity-1){ // already processed all arguments, candidate literal is ready
	    literals.add(lit);
	    newvariables.add(newvar);
	}
	else
	    generateLiterals(lit, newvar, mode, ++pos);
    }

    public void generateLiterals(Literal srclit, 
				 VariableByType srcnewvar, 
				 VariableType postype,
				 int[] mode, 
				 int pos) throws Exception
    {
	Literal lit = srclit.clone();
	VariableByType newvar = srcnewvar.clone();

	TreeSet<Variable> newvarpos = newvar.get(postype);
	
	if(newvarpos != null) // assign position to one of the newly introduced variables
	    for(Iterator<Variable> i = newvarpos.iterator(); i.hasNext();)
		generateLiterals(lit, i.next(), newvar, mode, pos);

	// introduce new variable to be  at current position
	String relvarname = nextName(postype.name());
	Variable relvar = new Variable(relvarname, postype);
	// add variable to current map of new variables
	newvar.addVariable(postype, relvar);
	// assign new variable to current position
	generateLiterals(lit, relvar, newvar, mode, pos);	    
    }

    public int toRMode(String string) throws Exception
    {
	if(string.equals("+"))
	    return I;
	if(string.equals("-"))
	    return O;
	if(string.equals("+-"))
	    return IO;
	
	throw new Exception("Undefined rmode " + string);
    }

    protected String nextName(String prefix)
    {
	String name = prefix + nameprefix + name_counter.toString();
	name_counter.increment();
	
	return name;
    }

    public Pair<Literal,VariableByType> next()
    {
	int val = literal_counter.getValue();

	if(val >= literals.size())
	    return null;
	
	Pair<Literal,VariableByType> currpair = new 
	    Pair<Literal,VariableByType>(literals.elementAt(val), newvariables.elementAt(val));

	literal_counter.increment();

	return currpair;
    }

    public static void main(String[] args)
    {
	if(args.length < 4){
	    System.out.println("Too few arguments, need: <relation> <rmodefile> <var> <nameprefix>");
	    System.exit(1);
	}

	String relstring = args[0];
	String modfile = args[1];
	String var = args[2];
	String prefix = args[3];
	
	try{
	    Relation rel = new Relation(relstring);
	    System.out.println("Loaded relation: " + rel.toTypeString());
	    Relation2LiteralGenerator factory = new Relation2LiteralGenerator(rel);

	    BufferedReader bin = new BufferedReader(new FileReader(modfile));
	    String buf;

	    while((buf = bin.readLine()) != null){
		Relation mod = new Relation(buf); 
		System.out.println("Loaded modes: " + mod.toTypeString());
		factory.setModes(mod);
	    }
	    
	    VariableByType obt = new VariableByType(var);
	    System.out.println("Loaded  variables: " + obt.toString());

	    int litnum = factory.generateLiterals(obt, prefix);
	    System.out.println("Generated " + litnum + " literals");	    

	    Pair<Literal,VariableByType> currpair;
	    
	    while((currpair = factory.next()) != null)
		System.out.println(currpair.first().toString());
	}
	catch (Exception e) {
	    System.out.println("Exception:" + e);
	}
    }
}
