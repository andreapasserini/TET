package tet.learner;

import tet.*;

import java.util.*;
import java.io.*;
import mymath.*;

public class LiteralFactory{

    Vector<Relation2LiteralGenerator> generators;
    Counter generator_counter;

    public LiteralFactory()
    {
	generators = new Vector<Relation2LiteralGenerator>();
	generator_counter = new Counter();
    }

    public LiteralFactory(String configfile) throws Exception
    {
	this();

	FileReader in = new FileReader(configfile);
	BufferedReader inbuf = new BufferedReader(in);
	init(inbuf);
	inbuf.close();
	in.close();
    }
    
    public LiteralFactory(BufferedReader configbuf) throws Exception
    {
	this();
	init(configbuf);
    }

    private void init(BufferedReader inbuf) throws Exception
    {	
	String buf;

	HashMap<String, Integer> name2generatorpos = new HashMap<String, Integer>();

	while((buf = inbuf.readLine()) != null){
	    /* skip empty lines */
	    if(buf.length() == 0)
		continue;
	    /* skip comments */
	    if(buf.charAt(0) == '%')
		continue;
	    int first = buf.indexOf('(');
	    int last =  buf.lastIndexOf(')');
	    if(first == -1 || last == -1)
		continue;
	    if(buf.substring(0,first).equals("type")){
		Relation rel = new Relation(buf.substring(first+1,last));
		generators.add(new Relation2LiteralGenerator(rel));
		name2generatorpos.put(rel.name() + "/" + rel.arity(), new Integer(generators.size()-1));
	    }
	    else if(buf.substring(0,first).equals("rmode")){
		Relation mod = new Relation(buf.substring(first+1,last));
		Integer pos = name2generatorpos.get(mod.name() + "/" + mod.arity());
		if(pos == null)
		    throw new Exception("No type definition for relation " + mod.name() + "/" + mod.arity());
		generators.elementAt(pos.intValue()).setModes(mod);
	    }		
	}       
    }

    public int generateLiterals(VariableByType obt, String prefix) throws Exception
    {
	int numlits = 0;
	
	generator_counter.clear();

	for(int i = 0; i < generators.size(); i++)
	    numlits += generators.elementAt(i).generateLiterals(obt, prefix);

	return numlits;
    }

    public Pair<Literal,VariableByType> next()
    {
	int val = generator_counter.getValue();

	// check if reached end of generators
	if(val >= generators.size())
	    return null;
	
	// get next literal for current generator
	Pair<Literal,VariableByType> currpair = generators.elementAt(val).next();
	
	// check if reached end of current generator
	if(currpair != null)
	    return currpair;

	// go to next generator
	generator_counter.increment();
	
	// recall next
	return next();
    }
    
    public int numRelations()
    {
	return generators.size();
    }

    public static void main(String[] args)
    {
	if(args.length < 3){
	    System.out.println("Too few arguments, need: <configfile> <var> <nameprefix>");
	    System.exit(1);
	}

	String configfile = args[0];
	String var = args[1];
	String prefix = args[2];
	
	try{
	    LiteralFactory factory = new LiteralFactory(configfile);
	    System.out.println("Loaded  " + factory.numRelations() + " templates");
		    
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
