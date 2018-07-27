package Tetpackage;

import java.util.*;
import mymath.*;

public class Literal implements Cloneable{

    Relation rel;
    Boolean negated;
    Object[] arguments; /* an array of RelObject or Variable */

    public Literal(Relation r, Boolean n, Object[] args)
    {
	rel = r;
	negated = n;
	arguments = args;
    }

    public Literal(Relation r, Boolean n)
    {
	rel = r;
	negated = n;
	
	arguments = new Variable[rel.arity()];
	for(int i = 0; i < rel.arity(); i++)
	    arguments[i] = new Variable();
	
    }

    public Literal(Literal lit)
    {
	rel = lit.rel;
	negated = lit.negated;
	arguments = lit.arguments;
    }

    /** literal string is something like !s(u,v) */
    public Literal(String head, StringTokenizer tokenizer) throws Exception
    {
	negated = false;
	if(head.charAt(0) == '!'){
	    negated = true;
	    head = head.substring(1);
	}

	String buf;
	
	if(!tokenizer.nextToken().equals("("))
	    throw new Exception("Malformed Type string"); 

	Vector<String> args = new Vector<String>();
	do{
	    buf = tokenizer.nextToken();
	    if(buf.equals(")"))
		break;
	    args.add(new String(buf));
	}while(!tokenizer.nextToken().equals(")")); // end of literals arguments

	/* assume variable names are made of variable type + variable identifier starting with a digit */
	VariableType[] argstype = new VariableType[args.size()];
	for(int i = 0; i < args.size(); i++)
	    argstype[i] = new VariableType(TetUtilities.trimUpToDigit(args.elementAt(i)));

	rel = new Relation(head,args.size(), argstype);
	arguments = new Variable[args.size()];
	for(int i = 0; i < args.size(); i++)
	    arguments[i] = new Variable(args.elementAt(i));	
    }

    /** literal string is something like !s(u,v) */
    public String toString(){
	
	StringBuffer buf = new StringBuffer(1024);
	
	if(negated)
	    buf.append("!");
	buf.append(rel.toString() + "(");
	
	for (int i=0;i<arguments.length; i++)
	    buf.append((arguments[i]).toString()  + ",");
	if(arguments.length > 0)
	    buf.setCharAt(buf.length()-1,')');
	else
	    buf.append(")");
	
	return buf.toString();	
    }

    /**
       Substitutes all occurrences of variable var in this literal's 
       arguments by obj
    */
    public void substitute(Variable var, RelObject obj){
	for (int i=0; i<arguments.length; i++){
	    if (var.equals(arguments[i]) )
		arguments[i]=obj;
	}

    }

    /** 
	binds argument at position pos in Literal
	with relational object obj
    */
    public void setVariable(int pos, Variable var) throws Exception
    {
	if(pos >= rel.arity())
	    throw new Exception("Trying to set variable at position " + pos + " in a relation of arity " + rel.arity());	

	arguments[pos]=var;	
    }

    public void setNegated(Boolean negated)
    {
	this.negated = negated;
    }

    /** Returns a vector of all the Variables in the arguments (without repetitions) */
    public TreeSet<Variable> allVars(){
	TreeSet<Variable> result = new TreeSet<Variable>();
	for (int i=0;i<arguments.length; i++){
	    if (arguments[i] instanceof Variable)
		result.add((Variable)arguments[i]);
	}
	return result;
    }
 
    public Literal clone()
    {
	Relation clone_rel = rel.clone();
	
	Object[] clone_arguments = new Object[arguments.length];
	for (int i=0;i<arguments.length; i++)
	    clone_arguments[i] = ((Variable)(arguments[i])).clone();

	return new Literal(clone_rel, negated, clone_arguments);
    }

    /** check whether the literal is exactly the same as
     *	lit (including negated flag) 
     */
    public boolean equals(Literal lit)
    {
	/* negated flag differs */
	if(this.negated != lit.negated)
	    return false;

	/* check if literals match */
	return matches(lit);
    }

    /** check whether the literal matches exactly literal lit  
     * (including variables or relational objects).
     * It's like a regexp match, thus if lit is not negated and
     * this is negated it still matches. 
     */
    public boolean matches(Literal lit)
    {
	/* negation harms match */
	if(lit.negated && !this.negated) 
	    return false;

	/* relations differ */
	if(!this.rel.equals(lit.rel))
	    return false;

	/* arguments differ */
	for (int i=0;i<arguments.length; i++)
	    if (arguments[i] instanceof Variable){
		if (!((Variable)arguments[i]).equals(lit.arguments[i]))
		    return false;
	    }
	    else{
		if (!((RelObject)arguments[i]).equals(lit.arguments[i]))
		    return false;
	    }
	
	return true;
    }

    /** check whether the literal matches relation rel 
     *  and uses variables vars 
     */
    public boolean matchesRelationWithVariables(Relation rel, TreeSet<Variable> vars)
    {
	/* relations differ */
	if(!this.rel.equals(rel))
	    return false;

	/* recover variables in literal */
	TreeSet<Variable> litvars = allVars();

	/* check that all variables in vars appear in the literal */
	for (Iterator<Variable> v = vars.iterator(); v.hasNext();)
	    if(!litvars.contains(v.next()))
	       return false;
	       
	return true;
    }

    /** check whether the literal contains at least
     *	one of the variables in vars 
     */
    public boolean containsVariable(TreeSet<Variable> vars)
    {
	for (int i=0;i<arguments.length; i++)
	    if (arguments[i] instanceof Variable && vars.contains(arguments[i]))
		return true;
	return false;
    }

    public boolean isOperator()
    {
	return rel.isOperator();
    }

    public boolean isSymmetric()
    {
	return rel.isSymmetric();
    }
    
    /** 
     * return symmetric version of current literal
     *
     */   
    public Literal getSymmetricLiteral()
    {
	if(!isSymmetric())
	    return null;

	Object[] sym_arguments = new Object[2];
	
	sym_arguments[0] = arguments[1];	
	sym_arguments[1] = arguments[0];

	return new Literal(rel, negated, sym_arguments);
    }

    /** 
     * recover all variables contained in the literal 
     */
    public TreeSet<Variable> getVariables()
    {
	TreeSet<Variable> vars = new TreeSet<Variable>();

	for (int i=0;i<arguments.length; i++)
	    if (arguments[i] instanceof Variable)
		vars.add((Variable)arguments[i]);

	return vars;	
    }

}




