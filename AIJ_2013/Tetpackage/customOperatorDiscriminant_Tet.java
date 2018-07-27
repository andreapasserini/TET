package Tetpackage;

import java.util.*;
import mymath.*;
import java.sql.*;
import java.io.*;
import Tetpackage.learner.*;

public class customOperatorDiscriminant_Tet extends discriminant_Tet{
   
    Operator branch_operator = Operator.TIMES;
    Operator value_operator = Operator.TIMES;

    public customOperatorDiscriminant_Tet(Type t, TreeSet<Variable> freevars)
    {
	super(t, freevars);
    }

    public customOperatorDiscriminant_Tet(Type t, TreeSet<Variable> freevars, Dataset dataset)
    {
	super(t, freevars, dataset);
    }

    public customOperatorDiscriminant_Tet(customOperatorDiscriminant_Tet dtet)
    {
	super((discriminant_Tet)dtet);

	this.branch_operator = dtet.branch_operator;
	this.value_operator = dtet.value_operator;
    }

    public customOperatorDiscriminant_Tet(FileReader in) throws Exception
    {
	this((new BufferedReader(in)).readLine());
    }

    public customOperatorDiscriminant_Tet(String tetstring) throws Exception{

	this(new StringTokenizer(tetstring,"[(,)]",true));
    }
    
    public customOperatorDiscriminant_Tet(StringTokenizer tokenizer) throws Exception{

	String buf;

	if(!(tokenizer.nextToken().equals("(")))
	    throw new Exception("Malformed discriminant TET string"); 

	// recover weight
	weight = Float.parseFloat(tokenizer.nextToken());

	if(!(tokenizer.nextToken().equals(",")))
	    throw new Exception("Malformed discriminant TET string"); 

	// recover threshold
	threshold = Float.parseFloat(tokenizer.nextToken());

	// check if operators available
	buf = tokenizer.nextToken();
	
	
	if(buf.equals(",")){ 

	    // recover branch operator 
	    branch_operator = Operator.valueOf(tokenizer.nextToken());

	    if(!(tokenizer.nextToken().equals(",")))
		throw new Exception("Malformed discriminant TET string"); 

	    // recover value operator 
	    value_operator = Operator.valueOf(tokenizer.nextToken());

	    if(!(tokenizer.nextToken().equals(")")))
		throw new Exception("Malformed discriminant TET string"); 
	}
	else if(!buf.equals(")"))
	    throw new Exception("Malformed discriminant TET string"); 

	if(!(tokenizer.nextToken().equals("[")))
	    throw new Exception("Malformed discriminant TET string"); 

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
		throw new Exception("Malformed discriminant TET string");
	    /* recursively run parser on child */
	    customOperatorDiscriminant_Tet childtet = new customOperatorDiscriminant_Tet(tokenizer);
	    /* add child to current Tet's children */
	    children.add(new TetChild(childtet,edgevariables));
	    if(!tokenizer.nextToken().equals("]"))
	       throw new Exception("Malformed discriminant TET string");
	}
	/* set the free variables */
	setFreeVars();
    }

    public Pair<Double,Double> compute_d_pair(RelStructure rstruc, HashMap<String, Object> boundvalues) throws Exception
    {
	/* if leaf, return weight */
	if(children.size()==0)
	    return new Pair(new Double(weight), new Double(1-weight));

	/* initialize d */
	float dpos = branch_operator.neutral();
	float dneg = branch_operator.neutral();

	/* collect freevars to be bound */
	TreeSet<Variable> boundvars = TetUtilities.strings2Variables(boundvalues.keySet());

	/* iterate over root children */
	for(int i = 0; i < children.size(); i++){	    
	    /* get child tet */
	    discriminant_Tet dtetchild = (discriminant_Tet)children.elementAt(i).subtree;
	    /* execute type query */
	    Vector<HashMap<String, Object>> res = 
		rstruc.queryResult(rstruc.typeValueQuery(dtetchild.root,
							 boundvars, boundvalues, 
							 null, false)); 

	    //ALTERNATIVE: 
	    //FOR FALSE VALUES DEFAULTING TO BRANCH_OPERATOR NEUTRAL
// 	    if(res.size() == 0){  
// 	       continue;      
	       
// 	    }
  
	    float branch_pos = value_operator.neutral();
	    float branch_neg = value_operator.neutral();

	    /* iterate over query results */
	    for(int j = 0; j < res.size(); j++){
		/* recursively update d passing new boundvalues map */ 
		Pair<Double,Double> newd = (dtetchild.compute_d_pair(rstruc, res.elementAt(j)));
		branch_pos = value_operator.eval(branch_pos, newd.first().floatValue() / weight);
		branch_neg = value_operator.eval(branch_neg, newd.second().floatValue() / (1-weight));
	    }
	    
	    dpos = branch_operator.eval(dpos, branch_pos);
	    dneg = branch_operator.eval(dneg, branch_neg);
	}

	/* multiply result by weight */
	dpos *= weight;
	dneg *= (1-weight);
	
	/* create new pair */
	Pair<Double,Double> newpair = new Pair(new Double(dpos), new Double(dneg));
	
	return newpair; 
    }

    public customOperatorDiscriminant_Tet get_node_to_configure()
    {
	/* if no children, its a single node. No configuration yet */
	if(children.size() == 0)
	    return null;
	
	/* recover last branch */
	customOperatorDiscriminant_Tet dtetchild = (customOperatorDiscriminant_Tet)children.lastElement().subtree;

	/* if it's a leaf, current node is to be configured */
	if(dtetchild.isLeaf())
	    return this;
	
	/* otherwise it's inside the last branch */
	return dtetchild.get_node_to_configure();
    }

    public boolean is_unconfigured()
    {
	return (branch_operator==null || value_operator==null);
    }


    public boolean hasNextConfiguration()
    {	
	if(is_unconfigured())
	    return true;
	if(branch_operator.ordinal() < (Operator.values().length-1))
	    return true;
	if(value_operator.ordinal() < (Operator.values().length-1))
	    return true;
	return false;
    }

    public boolean nextConfiguration()
    {	
	if (is_unconfigured()){	    
	    branch_operator = Operator.values()[0];
	    value_operator = Operator.values()[0];
	    return true;
	}
	if(value_operator.ordinal() < (Operator.values().length-1)){
	    value_operator = Operator.values()[value_operator.ordinal()+1];
	    return true;
	}
	if(branch_operator.ordinal() < (Operator.values().length-1)){
	    branch_operator = Operator.values()[branch_operator.ordinal()+1];
	    value_operator = Operator.values()[0];	    
	    return true;
	}
	return false;
    }
    
    public void setConfiguration(Pair<Operator,Operator> operators)
    {
	this.branch_operator=operators.first();
	this.value_operator=operators.second();
    }

    public Pair<Operator,Operator> getConfiguration()
    {
	return new Pair<Operator,Operator>(branch_operator, value_operator);
    }

    public void clearConfiguration()
    {
	branch_operator = null;
	value_operator = null;
    }

    public String Serialize(){
	
	StringBuffer buf = new StringBuffer(1024);
	
	buf.append("(" + weight + "," + threshold + "," + branch_operator + "," + value_operator + ")");
	buf.append("[" + root.toString());

	for(int i = 0; i < children.size(); i++)
	    buf.append(",[" + children.elementAt(i).edgeLabelsToString() + "," 
		       + ((discriminant_Tet)children.elementAt(i).subtree).Serialize() + "]");
	buf.append("]");

	return buf.toString();	    
    }
        
    public Dataset compute_d_pairs(TreeSet<Variable> bound_variables,
				   StringCounter table_counter,
				   Dataset parent_examples_dataset) throws Exception
    {

	/* check if cached dataset is valid */
	if(cache.isFreezed())
	    return cache.get();

	/* init allvars with type variables */
	TreeSet<Variable> allvars = this.root.getVariables();

	/* merge with bound variables set */
	allvars.addAll(bound_variables);

	/* recover current examples dataset, either from stored one or computing it from scratch */
	Dataset curr_examples_dataset = getExamplesDataset(allvars, table_counter, parent_examples_dataset);

	/* initialize curr_value_dataset with branch_operator neutral element,
	   or 1 if no children (neutral element as 1 x weight) */	
	float initvalue = (float)1.0;
	if(children.size() > 0)
	    initvalue = branch_operator.neutral();
	Dataset curr_value_dataset=curr_examples_dataset.getValueDataset(table_counter.next(), bound_variables, 
									 initvalue, initvalue);
	
	/* iterate over root children */
	for(int i = 0; i < children.size(); i++){	    	    

	    /* get child tet */
	    customOperatorDiscriminant_Tet dtetchild = (customOperatorDiscriminant_Tet)children.elementAt(i).subtree;
	    	    
	    /* recursively compute value dataset */
	    Dataset child_value_dataset = dtetchild.compute_d_pairs(allvars, 
								    table_counter,
								    (parent_examples_dataset != null) ? curr_examples_dataset : null);	
	    /* update values dataset with recursive values */
	    curr_value_dataset.aggregateValues(table_counter.next(), child_value_dataset, 
					       bound_variables, weight, 1-weight, branch_operator, value_operator);

	    //DEBUG
	    //System.out.println("Scoring child " + i + "\n" + dtetchild.toFormattedString());
	    //child_value_dataset.printTable();	    
	    //curr_value_dataset.printTable();
	}

	/* reweight values by node weight */
	curr_value_dataset.reweightValues(weight, 1-weight);

	//DEBUG
	//System.out.println("Scoring tet\n" + this.toFormattedString());
	//curr_value_dataset.printTable();

	/* store values in cache */
	cache.set(curr_value_dataset);

	return curr_value_dataset;
    }

    public Float[] compute_ds(TreeSet<Variable> bound_variables,
			      StringCounter table_counter,
			      Dataset bound_dataset) throws Exception
    {	
	/* recover values dataset */
	Dataset values_dataset = compute_d_pairs(bound_variables, table_counter, bound_dataset);

	/* compute divisions */
	return values_dataset.computeValues();
    }
    
    public String toFormattedString(String prefix)
    {	
	/* attach type */
	String buf = root.toString();
	
	/* attach children */
	if(children.size() > 0){	    
	    /* attach operators */
	    buf +=  " (" + branch_operator + "," + value_operator + ")\n";
	    for(int i = 0; i < children.size()-1; i++)
		buf += prefix + "|\n" + prefix + "--" + children.elementAt(i).toFormattedString(prefix + "| ");
	    buf += prefix + "|\n" + prefix + "--" + children.lastElement().toFormattedString(prefix + "  ");
	}
	else
	    buf += "\n";

	return buf;
    }

    

    public static void main(String[] args)
    {
	
	discriminant_TetCommandLineOptions options = new discriminant_TetCommandLineOptions(args);
	
	try{
	    /* create mysql relational structure which uses TD BU procedure */
	    MySQLRelStructure relstruct = new MySQLRelStructure(options.database, options.login, options.password, true);
	 
	    /* create MySQLTestViewer if needed */
	    MySQLTestViewer testviewer = (options.srcdb != null && options.testviewconfigfile != null) ? 
		new MySQLTestViewer(relstruct, options.srcdb, options.testviewconfigfile) : null;

	    /* load discriminant tet from file */
	    String tetstring = Tet.tetString(options.tetfile);	    
	    discriminant_Tet tet = new customOperatorDiscriminant_Tet(tetstring);
	    
	    System.out.println("Read discriminant Tet string: " + tet.Serialize());
	    System.out.println("Tet Freevars = " + tet.freevars().toString()); 	 
	    
	    /* parse object file and compute discriminant value for each object  */
	    FileReader in = new FileReader(options.objfile);
	    StreamTokenizer st = new StreamTokenizer(in);
	    st.eolIsSignificant(true);
	    st.whitespaceChars(32,47);
	    st.whitespaceChars(58,63);

	    FileWriter out = new FileWriter(options.outfile, false);

	    while(st.nextToken() != StreamTokenizer.TT_EOF){

		HashMap<String, Object> objmap = new HashMap<String, Object>();

		do{
		    String var = st.sval;
		    st.nextToken();
		    String val = st.sval;
		    if(st.ttype == st.TT_NUMBER)
			val = String.valueOf((int)st.nval);
		    objmap.put(var,new RelObject(val));
		}while(st.nextToken() != StreamTokenizer.TT_EOL);
		
		/* add test view for current example if needed */
		if(testviewer != null)
		    testviewer.addTestView(objmap);
				
		float d = tet.compute_d(relstruct, objmap);				

		/* print decision and d value */
		out.write(tet.compute_decision(d) + "\t" + d); 
		
		/*  compute tet value and print it if needed */
		if(options.compute_tet_value){		    
		    Value value = tet.calculateValue(relstruct, objmap, !options.compute_tet_false_value);
		    out.write("\t" + value);
		    /*  compute rdk value and print it if needed */
		    if(options.compute_rdk_value)
			out.write("\t" + value.writeRDK());
		}

		/* print newline */
		out.write("\n");

		/* remove test view for current example if needed */
		if(testviewer != null)
		    testviewer.removeTestView(objmap);
	    }
	    out.close();
	    relstruct.Close();
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }

}
