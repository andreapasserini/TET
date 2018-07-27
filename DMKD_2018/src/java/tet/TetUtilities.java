package tet;

import java.util.*;
import mymath.*;

public class TetUtilities{

    private static final int default_random_string_length = 3;

    /** Returns the set of variables contained in vars1 and not contained in vars2. Variables
     * in vars1 and vars2 "match" when they have the same name, i.e. they need not be 
     * the same object. The result contains the same objects as contained in vars1.
     * If vars1 contains several duplicates of a variable not in vars2, then the result will
     * also 
     */
    public static Vector<Variable> setDifference(Vector<Variable> vars1, Vector<Variable> vars2){
	/* a naive implementation ... */
	Vector<Variable> result = new Vector<Variable>();
	Variable nextfrom1;
	Boolean found;
	for (int i=0; i < vars1.size();i++){
	    nextfrom1=vars1.elementAt(i);
	    found = false;
	    for (int j=0; j < vars2.size(); j++){
		if (vars2.elementAt(j).equals(nextfrom1))
		    found = true;
	    }
	    if (!found)
		result.add(nextfrom1);
	}
	return result;
    }

    public static Vector<Variable> setIntersection(Vector<Variable> vars1, Vector<Variable> vars2){
	/* a naive implementation ... */
	Vector<Variable> result = new Vector<Variable>();
	Variable nextfrom1;
	for (int i=0; i < vars1.size();i++){
	    nextfrom1=vars1.elementAt(i);
	    for (int j=0; j < vars2.size(); j++)
		if (vars2.elementAt(j).equals(nextfrom1)){
		    result.add(nextfrom1);
		    break;
		}
	}
	return result;
    }
 
    /** Merges the two variable vectors vars1 and vars2 into a single vector containing
     * each variable appearing either in vars1 or vars2. Method intended for vectors
     * not containing duplicates (otherwise behavior a little strange). 
     */
    public static Vector<Variable> mergeVars(Vector<Variable> vars1, Vector<Variable> vars2){
	Vector<Variable> result = new Vector<Variable>();
	for (int i = 0; i< vars1.size(); i++)
	    result.add(vars1.elementAt(i));
	Vector<Variable> newinvars2 = setDifference(vars2,vars1);
	for (int i = 0; i< newinvars2.size(); i++)
	    result.add(newinvars2.elementAt(i));
	return result;
    }

    /** Returns a variable vector that contains the variables in varvec without 
	duplicated copies of the same variable  */
    public static Vector<Variable>  removeDuplicates(Vector<Variable> varvec){
	Vector<Variable> result = new Vector<Variable>();
	for (int i=0; i < varvec.size();i++){
	    Variable nextvar = varvec.elementAt(i);
	    Boolean isold = false;
	    for (int j=0; j < result.size();j++){
		if (nextvar.equals(result.elementAt(j)))
		    isold = true;
	    }
	    if (! isold)
		result.add(nextvar);
	}
	return result;
    }

    /** appends all the elements of vec2 to vec1 */
    public static Vector  append(Vector vec1, Vector vec2){
	for (int i=0; i < vec2.size();i++)
	    vec1.add(vec2.elementAt(i));
	return vec1;
    }

    public static String asString(Vector<Variable> varvec){
	String result = "(";
	for(int i=0; i < varvec.size();i++)
	    result = result + varvec.elementAt(i).name() + ",";
	result = result + ")";
	return result;
    }
    
    public static Vector chooseRelObjectByVar(Vector<Variable> choice_var, 
					      Vector<Variable> all_var, 
					      Vector<RelObject> all_content){
	Vector<RelObject> choice_content = new Vector<RelObject>();
	for(int i = 0; i < choice_var.size(); i++)
	    for(int j = 0; j < all_var.size(); j++){
		if(choice_var.elementAt(i).equals(all_var.elementAt(j))){
		    choice_content.add(all_content.elementAt(j));
		    break;
		}
	    }
	return choice_content;
    }

    public static String randomString()
    {
	return randomString(default_random_string_length);
    }
    
    public static String randomString(int length)
    {	
	if(length <= 0)
	    return new String();
	
	char[] chars  = new char[length];	

	for(int i = 0; i < length; i++)
	    chars[i] = (char)(Math.random()* 26 + 'a');

	return new String(chars);
    }
    
    
    public static TreeSet<Variable> merge(TreeSet<Variable> a, TreeSet<Variable> b)
    {
	TreeSet<Variable> c = new TreeSet<Variable>(); 
	c.addAll(a); 
	c.addAll(b);      
	return c;
    }

    public static TreeSet<Variable> intersect(TreeSet<Variable> a, TreeSet<Variable> b)
    {
	TreeSet<Variable> c = new TreeSet<Variable>(); 
	c.addAll(a); 
	c.retainAll(b);      
	return c;
    }

    public static TreeSet<Variable> strings2Variables(Set<String> stringset)
    {
	TreeSet<Variable> vars = new TreeSet<Variable>();

	for(Iterator<String> i = stringset.iterator(); i.hasNext();)
	    vars.add(new Variable(i.next()));

	return vars;
    }

    public static String trimUpToDigit(String buf)
    {
	StringTokenizer tokenizer = new StringTokenizer(buf,"0123456789",false);
	String trimmed;
	
	try{
	    trimmed = tokenizer.nextToken();
	}catch (NoSuchElementException e){ // no next token -> buf is empty
	    return "";
	}
	catch(NullPointerException e){ // empty delimiter -> return full buf
	    return buf;
	}
	return trimmed;
    }


    public static TreeMap<Float,Float> clone(TreeMap<Float,Float> o)
    {
	TreeMap<Float,Float> clone = new TreeMap<Float,Float>();

	for(Iterator<Map.Entry<Float,Float>> i = o.entrySet().iterator(); i.hasNext();){
	    Map.Entry<Float,Float> entry = i.next();
	    clone.put(new Float(entry.getKey().floatValue()), new Float(entry.getValue().floatValue()));
	}

	return clone;
    }

    public static char[] spaceString(int length)
    {
	char[] space = new char[length];
	
	for(int i = 0; i < length; i++)
	    space[i] = ' ';
	
	return space;
    }


    public static void addToMultiMap(TreeMap<Float, List<Float>> map, Float key, Float value)
    {
	List<Float> l = map.get(key);
	if (l == null)
	    map.put(key, l=new ArrayList<Float>());
	l.add(value);
    }

    public static TreeSet<Variable> array2set(Variable[] array)
    {
	TreeSet<Variable>  set = new TreeSet<Variable>();
	
	for(int i = 0; i < array.length; i++)
	    set.add(array[i]);

	return set;	
    }

    
    public static TreeSet<Variable> vector2set(Vector<Variable> vector)
    {
	TreeSet<Variable>  set = new TreeSet<Variable>();
	
	for(int i = 0; i < vector.size(); i++)
	    set.add(vector.elementAt(i));

	return set;	
    }

    public static Boolean equals(TreeSet<Variable> a, TreeSet<Variable> b)
    {
	if (a.size() != b.size())
	    return false;


	for(Iterator<Variable> i = a.iterator(); i.hasNext();)
	    if(!b.contains(i.next()))
		return false;

	return true;
    }


    public static Variable[] append(Variable[] a, Variable[] b)
    {
	Variable[] c = new Variable[a.length+b.length];
	
	int ci = 0;

	for(int ai = 0; ai < a.length; ai++, ci++)
	    c[ci] = a[ai];

    	for(int bi = 0; bi < b.length; bi++, bi++)
	    c[ci] = b[bi];
	
	return c;
    }

    public static void main(String[] args){
	
        if(args.length < 2){
            System.out.println("Too few arguments, need: <length> <num>");
            System.exit(1);
        }

        int length = Integer.parseInt(args[0]);
        int num = Integer.parseInt(args[1]);
	
	for(int i = 0; i < num; i++)
	    System.out.println(randomString(length));
    }    
}


