package Tetpackage;

import java.util.*;
import mymath.*;
import java.io.*;

public class Tet{

    Type root;
    TreeSet<Variable> freevars; /* contains the free variables of this Tet */
    Vector<TetChild> children;

    public Tet(){
	root = new Type(); /* this is now the empty type, evaluating always to true */
	freevars = new TreeSet<Variable>();
	children = new Vector<TetChild>();
    }

    public Tet(TreeSet<Variable> freevars){
	root = new Type(); /* this is now the empty type, evaluating always to true */
	this.freevars = freevars;
	children = new Vector<TetChild>();
    }

    public Tet(Type t){
	root = t; 
	freevars = new TreeSet<Variable>();
	freevars = t.allVars();
	children = new Vector<TetChild>();
    }

    public Tet(Type t, TreeSet<Variable> freevars){
	root = t; 
	this.freevars = freevars;
	children = new Vector<TetChild>();
    }

    public Tet(Tet tet){
	root = new Type(tet.root); 
	freevars = new TreeSet<Variable>(tet.freevars);
	children = new Vector<TetChild>(tet.children.size());
	for(int i = 0; i < tet.children.size(); i++)
	    children.add(i,new TetChild(tet.children.elementAt(i)));
    }

    /* TET strings are as follows
       T = [tau,[w1,T1],..,[wm,Tm]]
       where e.g.
       tau = (s(u,v),t(w)) 
       w1 = (u,v)
    */
    
    public Tet(String tetstring) throws Exception{

	this(new StringTokenizer(tetstring,"[(,)]",true));
    }

    public Tet(StringTokenizer tokenizer) throws Exception{

	String buf;

	if(!(tokenizer.nextToken().equals("[")))
	    throw new Exception("Malformed TET string"); 
	
	// recover Type
	root = new Type(tokenizer);
	
	// initialize children vector
	children = new Vector<TetChild>();

	while(!(tokenizer.nextToken().equals("]"))){ // end of both recursion and children list
	    buf = tokenizer.nextToken() + tokenizer.nextToken();
	    if(!buf.equals("[("))
		throw new Exception("Malformed TET string");
	    /* recover edge labels */
	    TreeSet<Variable> edgevariables = new TreeSet<Variable> ();
	    do{
		buf = tokenizer.nextToken();
		if(!buf.equals(")") && !buf.equals(","))  
		    edgevariables.add(new Variable(buf));
	    } while(!(buf.equals(")"))); // end of edge labels
	    if(!(tokenizer.nextToken().equals(",")))
		throw new Exception("Malformed TET string");
	    /* recursively run parser on child */
	    Tet childtet = new Tet(tokenizer);
	    /* add child to current Tet's children */
	    children.add(new TetChild(childtet,edgevariables));
	    if(!tokenizer.nextToken().equals("]"))
	       throw new Exception("Malformed TET string");
	}
	/* set the free variables */
	setFreeVars();
    }
    
    public void setFreeVars(){

	/* initialize freevars with variables in Type */
	freevars = root.allVars();
	
	/* add freevars from each child, removing those appearing in the edge */
	for(int i = 0; i < children.size(); i++){

	    /* select child */
	    Tet childtet = children.elementAt(i).subtree;

	    /* recursively sets its free vars */
	    childtet.setFreeVars();	    

	    /* init freevars */ 
	    TreeSet<Variable> newfreevars = new TreeSet<Variable>();

	    /* add freevars from child */
	    newfreevars.addAll(childtet.freevars());

	    /* remove edgelabel vars */
	    newfreevars.removeAll(children.elementAt(i).edgelabel);
	    
	    /* add to current tet freevars */
	    freevars.addAll(newfreevars);	    
	}
    }

    public TreeSet<Variable> freevars(){
	return freevars;
    }

    public Type getRootType()
    {
	return root;
    }

    /* TET strings are as follows
       T = [tau,[w1,T1],..,[wm,Tm]]
       where e.g.
       tau = (s(u,v),t(w)) 
       w1 = (u,v)
    */
    public String toString(){
	
	StringBuffer buf = new StringBuffer(1024);

	buf.append("[" + root.toString());

	for(int i = 0; i < children.size(); i++)
	    buf.append("," + children.elementAt(i));
	buf.append("]");

	return buf.toString();	    
    }

    public String toFormattedString()
    {
	return toFormattedString(new String());
    }

    public String toFormattedString(String prefix)
    {	
	/* attach type */
	String buf = root.toString() + "\n";
	
	/* attach children */
	if(children.size() > 0){	    
	    for(int i = 0; i < children.size()-1; i++)
		buf += prefix + "|\n" + prefix + "--" + children.elementAt(i).toFormattedString(prefix + "| ");
	    buf += prefix + "|\n" + prefix + "--" + children.lastElement().toFormattedString(prefix + "  ");
	}

	return buf;
    }


    public void addSubTree(Tet subtet, TreeSet<Variable> el)
    {
	addChild(new TetChild(subtet,el));
    }

    public void addChild(TetChild newchild)
    {
	// check if trying to add null subtree
        if(newchild.getSubTree() == null)
            return;
        
        children.add(newchild);

        /* update the free variables:*/
        TreeSet<Variable> newfreevars = new TreeSet<Variable>();
        newfreevars.addAll(newchild.getSubTree().freevars());
        newfreevars.removeAll(newchild.getEdgeLabels());
        freevars.addAll(newfreevars);  
    }

    public void removeLastChild() throws Exception
    {
	if(children.size() == 0)
	    throw new Exception("Trying to remove last child but tet has no children");

	children.remove(children.size()-1);

	/* update the free variables:*/
	setFreeVars();
    }
    
    public void removeChildren(int firstpos, int afterlastpos) throws Exception
    {
	if(firstpos < 0 || afterlastpos > children.size())
	    throw new Exception("Range of subtrees to remove extends beyond available ones");
	
	for(int i = firstpos; i < afterlastpos; i++)
	    children.remove(firstpos);
	
	/* update the free variables:*/
	setFreeVars();
    }

    /** Calculates the value of this Tet for objects objargs. The size of 
     * objargs must be the same as the size of this.freevars. Objargs may contain
     * several occurrences of one object. 
     * When trueonly is set to true, then subvalues starting with a boolean false 
     * are not computed.
     * Example:<p>
     * Value with trueonly = false:<p>
     * (true,[(false,[(false) -> 7]) -> 3(true,[(true) -> 2(false) -> 5]) -> 4])<p>
     * Same value computed with trueonly = true:<p>
     * (true,[(true,[(true) -> 2]) -> 4])<p>
     * Values obtained with trueonly = true are ordinary values, except that the
     * sizes of the different multisets are not any more determined as some power
     * of the domainsize. 
     */
   public Value calculateValue(RelStructure rstruc, 
				HashMap<String, Object> boundvalues, 
				Boolean trueonly) throws Exception 
    {
// 	if(trueonly == false)
// 	    throw new Exception("Only true values implemented so far");
	
	/* call relational structure to compute Tet value */ 
	String queryresult = rstruc.TetValueString(this, freevars(), boundvalues, trueonly);

	/* parse value string into Value */
	try{
	    return new Value(queryresult);
	}catch(Exception e){
	    e.printStackTrace();
            System.out.println("Parsing query result:");
            System.out.println(queryresult);
            System.out.println("Exiting...");
            System.exit(1);
	}
	return new Value();
    }

    public void addChildren(Vector<TetChild> newchildren){

	for(int i = 0; i < newchildren.size(); i++)
	    addSubTree(newchildren.elementAt(i).getSubTree(),newchildren.elementAt(i).getEdgeLabels());
    }
    public Vector<TetChild> getChildren()
    {
	return children;
    }

    public int getNumChildren()
    {
	return children.size();
    }
    
    public TetChild getChild(int i) throws Exception
    {
	if(i >= children.size() || i < 0)
	    throw new Exception("Asked for child " + i + " when tet has " + children.size() + " children");
	
	return children.elementAt(i);
    }

    public void printFreeVarsRecursive()
    {
	System.out.println(root);
	for(Iterator<Variable> i = freevars.iterator(); i.hasNext(); )
	    System.out.println(i.next());
	for(int i = 0; i < children.size(); i++)
	    children.elementAt(i).getSubTree().printFreeVarsRecursive();
    }

    public String Serialize()
    {
	return toString();
    }

    public boolean isLeaf()
    {
	return children.size() == 0;
    }

    public static String tetString(String tetfile) throws Exception
    {
	File f = new File(tetfile);
	byte[] b = new byte[(int) f.length()]; 
	FileInputStream in = new FileInputStream(f);
	in.read(b);
	in.close();
	String tetstring = new String(b);
	
	return tetstring.replaceAll("\\s","");
    }

    public static void main(String[] args){
	
	if(args.length < 1){
	    System.out.println("Too few arguments, need: <tetfile>");
	    System.exit(1);
	}

	String tetfile = args[0];
	
	try{

	    String tetstring = Tet.tetString(tetfile);
	    
	    Tet tet = new Tet(tetstring);

	    System.out.println(tet.toFormattedString());

	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
