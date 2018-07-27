package tet.learner;

import tet.*;

import java.util.*;
import java.io.*;
import mymath.*;
import java.sql.*;
import java.lang.Math;

public class MySQLDataset implements Dataset{

    static final String datatype = "INT(11)";
    MySQLRelStructure rstruct;
    String table_name;
    TreeSet<Variable> table_vars;
    boolean has_counts_only = false;

    public MySQLDataset(MySQLRelStructure rstruct, String relation_name, TreeSet<Variable> relation_vars) 
    {
	this.rstruct = rstruct;
	this.table_name = relation_name;
	this.table_vars = relation_vars; // WARNING: need to clone ??
    }

    public MySQLDataset(MySQLRelStructure rstruct, 
			String relation_name, 
			Vector<Variable> relation_vars, 
			String datafile) throws Exception
    {
	this(rstruct, relation_name, new TreeSet(relation_vars));
	
	init(datafile, relation_vars);
    }
        
    public String getRelationName()
    {
	return table_name;
    }

    public TreeSet<Variable> getRelationVars()
    {
	return table_vars;
    }
 
    public Vector<Example> getExamples()
    {
	StringBuffer buf = new StringBuffer();

	buf.append("SELECT * FROM " + table_name + " ORDER BY ");

	/* iterate over table variables to get same order as the one in aggregate value computation */
	for(Iterator<Variable> i = this.table_vars.iterator(); i.hasNext();)
	    buf.append(i.next().name() + ",");      

	/* remove final comma */
	buf.setLength(buf.length()-1);
	
	
	Vector<Example> examples = new Vector<Example>();	

	synchronized(rstruct){ // rstruct must be locked while accessing ResultSet
	
	    ResultSet rs = rstruct.executeQuery(buf.toString());

	    try{
		while(rs.next()){
		    Example ex = new Example();
		    for(Iterator<Variable> i = table_vars.iterator(); i.hasNext();){
			String varname = i.next().name();
			ex.addRelObject(varname, rs.getString(varname));
		    }
		    ex.addTarget(rs.getFloat("target"));		
		    examples.add(ex);
		}
	    } catch (java.sql.SQLException e){
		e.printStackTrace();
		return null;
	    }
	}

	return examples;
    }
    
    public MySQLDataset getSatisfyingDataset(Type type, 
					     String relation_name, 
					     TreeSet<Variable> relation_vars,
					     boolean isLeaf) throws Exception
    {
	MySQLDataset dataset = new MySQLDataset(this.rstruct, relation_name, relation_vars);
	dataset.init(type, this, isLeaf);
	return dataset;
    }

    public MySQLDataset getDatasetDifference(Dataset subset, 
					     String relation_name, 
					     TreeSet<Variable> relation_vars,
					     TreeSet<Variable> difference_vars,
					     boolean isLeaf,
					     boolean skipNegated)
    {
	MySQLDataset dataset = new MySQLDataset(this.rstruct, relation_name, relation_vars);
	
	if(skipNegated)
	    dataset.initToDifferenceCounts(this, (MySQLDataset)subset);
	else
	    dataset.initToDifference(this, (MySQLDataset)subset, difference_vars, isLeaf);
	
	return dataset;	
    }

    public MySQLRelStructure getRelStructure()
    {
	return this.rstruct;
    }

    public float getTargetProportion(float target)
    {
	float targetcount = 0, totcount = 0;

	synchronized(rstruct){ // rstruct must be locked while accessing ResultSet

	    /* excute query to get counts by target */
	    ResultSet res = (this.has_counts_only) ? 
		rstruct.executeQuery("SELECT target,counts from " + table_name) :
		rstruct.executeQuery("SELECT target,COUNT(target) from " + table_name + " GROUP BY target");
	    
	    /* browse query results */
	    try{
		while(res.next()){
		    /* get current count */
		    float currcount = res.getFloat(2);
		    /* update total count */
		    totcount += currcount;
		    /* if currtarget = target update targetcount */
		    if(res.getFloat(1) == target)
			targetcount = currcount;
		}
	    }catch(java.sql.SQLException e){
		e.printStackTrace();
		System.exit(1);
	    }
	}
	
	return (totcount > 0) ? targetcount / totcount : 0;
    }


    /*
     * gig(p(v,w)) = 1/|bindings(w)| * sum_{a in bindings(w)} ig(p(v,a)) =
     * = 1/|bindings(w)| * sum_{a in bindings(w)} H(tt) - |tt_p_a|/|tt| H(tt_p_a) - |!tt_p_a|/|tt| H(!tt_p_a) =
     * = H(tt) - sum_{a in bindings(w)} |tt_p_a|/|bindings(w)||tt| H(tt_p_a) + |!tt_p_a|/|bindings(w)||tt|H(!tt_p_a)
     */
    public Pair<Float,Integer> computeEntropy(TreeSet<Variable> newvars, TreeMap<Float,Float> overallcounts, boolean use_gig) 
	throws java.lang.ArithmeticException
    {
	/* if no entity introduction return standard entropy */
	if(newvars.size() == 0)
	{
	    System.out.println("Basic refinement: compute standard information gain");
	    return computeEntropy();
	}
	
	if(use_gig)
	    return computeGigEntropy(newvars, overallcounts);
	return computeRigEntropy(newvars, overallcounts);
    }

    public Pair<Float,Integer> computeGigEntropy(TreeSet<Variable> newvars, TreeMap<Float,Float> overallcounts) 
    {
	/* create conditioning on new vars */
        String newvarsbuf = new String();       
        for(Iterator<Variable> i = newvars.iterator(); i.hasNext();)
            newvarsbuf += "," + i.next();

        /* recover counts vectors grouped by instantiation of the newly introduced entities */
        HashMap<String, TreeMap<Float,Float>> countsmap = new HashMap<String, TreeMap<Float,Float>>();

        synchronized(rstruct){ // rstruct must be locked while accessing ResultSet

            /* execute assembled query */
            ResultSet res = rstruct.executeQuery("SELECT target,COUNT(target)" + newvarsbuf + 
                                                 " from " + table_name + " GROUP BY target" + newvarsbuf);      
            try{
                while(res.next()){
                    /* recover current target in first column */
                    float currtarget = res.getFloat(1);
                    /* recover current counts in second column */
                    float currcounts = res.getFloat(2);
                    /* build key by concatenating newvar names and values */
                    String key = new String();
                    int j = 3;
                    for(Iterator<Variable> i = newvars.iterator(); i.hasNext(); j++)
                        key += i.next() + "=" + res.getString(j) + "|";
                    /* check if key already present, otherwise add it */
                    TreeMap<Float,Float> m = countsmap.get(key);
                    if (m == null)
                        countsmap.put(key, m=new TreeMap<Float,Float>());
                    /* add current target,counts pair to map associated with the key */
                    m.put(currtarget, currcounts);
                }
            }catch(java.sql.SQLException e){
                e.printStackTrace();
                System.exit(1);
            }
        }
        
        Vector<Pair<Float,Integer>> entropyvec = new Vector<Pair<Float,Integer>>();

        /* compute entropy for each instantiation of the newly introduced entities plus entropy of complement set */
        Set<Map.Entry<String, TreeMap<Float,Float>>> entryset = countsmap.entrySet();
        for(Iterator<Map.Entry<String, TreeMap<Float,Float>>> i = entryset.iterator(); i.hasNext();){
            /* get current entity counts map */
            TreeMap<Float,Float> curr_targetcounts_map = i.next().getValue();
            /* compute entity entropy */
            entropyvec.add(Scorer.computeEntropy(curr_targetcounts_map.values()));
            /* compute entity complement set */
            /* clone overall counts */
            TreeMap<Float,Float> complement_counts = TetUtilities.clone(overallcounts);
            /* remove current entity counts */      
            for(Iterator<Map.Entry<Float,Float>> j = curr_targetcounts_map.entrySet().iterator(); j.hasNext();){
                Map.Entry<Float,Float> curr_target_counts = j.next();
                Float complement_target_counts = complement_counts.get(curr_target_counts.getKey());
                complement_target_counts -= curr_target_counts.getValue();
                complement_counts.put(curr_target_counts.getKey(), complement_target_counts);
            }
            /* compute complement entropy */
            try{
                entropyvec.add(Scorer.computeEntropy(complement_counts.values()));
            }
            catch(java.lang.ArithmeticException e){
                /* division by zero exception implies that complement set is empty,
                   simply ignore it */
            }   
        }
            
        /* sum entropies weighting them by the proportion of their sets sizes */
        return Scorer.weightedSum(entropyvec);
    }

    public Pair<Float,Integer> computeRigEntropy(TreeSet<Variable> newvars, TreeMap<Float,Float> overallcounts) 
    {
	System.out.println("Variable-Introducing refinement: compute relational information gain");
	Vector<Float> labels = new Vector<Float>();

	/* create conditioning on new vars */
	int newvarsnum = 0;
	String newvarsbuf = new String();	
	TreeSet<String> newvars_str = new TreeSet<String>();
	for(Iterator<Variable> i = newvars.iterator(); i.hasNext();)
	{
	    Variable var = i.next();
	    newvars_str.add(var.name());
	    //System.out.println("Newvar "+var.name());
	    newvarsbuf += "," + var;
	    newvarsnum++;
	}

	/* recover counts vectors grouped by instantiation of the newly introduced entities */
	HashMap<String, TreeMap<Float,Float>> single_countsmap = new HashMap<String, TreeMap<Float,Float>>();

	synchronized(rstruct){ // rstruct must be locked while accessing ResultSet

       		/* execute assembled query */
	        ResultSet res = rstruct.executeQuery("SELECT target,COUNT(target)" + newvarsbuf +
                                             " from " + table_name + " GROUP BY target" + newvarsbuf);
	        try
		{
       			while(res.next())
			{
	              		/* recover current target in first column */
       				float currtarget = res.getFloat(1);
	      	        	/* recover current counts in second column */
        		        float currcounts = res.getFloat(2);
        	       		/* build key by concatenating newvar names and values */
		                String key = new String();
       	 	        	int j = 3;
	                	for(Iterator<Variable> i = newvars.iterator(); i.hasNext(); j++)
                    			key += i.next() + "=" + res.getString(j) + "|";

	               	 	/* check if key already present, otherwise add it */
        	       		TreeMap<Float,Float> sm = single_countsmap.get(key);
                		if (sm == null)
                    			single_countsmap.put(key, sm=new TreeMap<Float,Float>());
                		/* add current target,counts pair to map associated with the key */
                		sm.put(currtarget, currcounts);
        	    	}
	        } catch(java.sql.SQLException e) {
	            e.printStackTrace();
        	    System.exit(1);
        	}
	}

	/* recover counts vectors grouped by instantiation of the newly introduced entities */
	HashMap<String, TreeMap<Integer,Float>> countsmap = new HashMap<String, TreeMap<Integer,Float>>();
	
	/* hashmap containing and index for all the examples (to be used to compute RIG) */
	HashMap<String,Integer> example_index = new HashMap<String,Integer>();

	synchronized(rstruct){ // rstruct must be locked while accessing ResultSet

	    /* execute assembled query */
	    ResultSet res = rstruct.executeQuery("SELECT * from " + table_name);
	    try{
	        int numcolumns = res.getMetaData().getColumnCount();
		//System.out.println(numcolumns+" "+newvarsnum);
		while(res.next()){
		    /* recover current target in first column */
		    int target_index = res.findColumn("target");
		    float currtarget = res.getFloat(target_index);
		    /* build key by concatenating oldvar names and values */
		    String key = new String();
		    String key_example = new String();
		    for(int j=1; j<=numcolumns; j++)
		    {
			String column = res.getMetaData().getColumnName(j);
			if (column=="target") continue;
			//System.out.println("Column "+column);
			if (newvars_str.contains(column))
				key += column + "=" + res.getString(j) + "|";
			else
				key_example += res.getString(j) + "|";
		    }

		    /* check if key_example already present, otherwise add it */
		    Integer index = example_index.get(key_example);
		    if (index == null)
		    {
			index = example_index.size();
			example_index.put(key_example,index);
			for (int k=labels.size(); k<=index; k++) labels.add(new Float(0));
			labels.set(index,currtarget);
		    }

		    //System.out.println("KeyIndex "+key+" "+index+" "+currtarget);

		    /* check if key already present, otherwise add it */
		    // System.out.println("Key "+key);
		    TreeMap<Integer,Float> m = countsmap.get(key);
		    if (m == null)
			countsmap.put(key, m=new TreeMap<Integer,Float>());
		    /* add current target,counts pair to map associated with the key */
		    m.put(index, currtarget);
		}
	    }catch(java.sql.SQLException e){
		e.printStackTrace();
		System.exit(1);
	    }
	}
	
	Vector<Pair<Float,Integer>> entropyvec = new Vector<Pair<Float,Integer>>();

	/* MARCO: compute RIG */
	Set<Map.Entry<String, TreeMap<Integer,Float>>> entryset = countsmap.entrySet();
	//Set<Map.Entry<String, TreeMap<Float,Float>>> entryset = single_countsmap.entrySet();

	Pair<Float,Integer> rig = new Pair<Float,Integer>(new Float(0),new Integer(0));
	Pair<Float,Integer> tmp_rig = new Pair<Float,Integer>(new Float(0),new Integer(0));

	TreeMap<Float,Vector<String>> pos_constants = new TreeMap<Float,Vector<String>>();
	TreeMap<Float,Vector<String>> neg_constants = new TreeMap<Float,Vector<String>>();
	TreeMap<Float,Vector<String>> best_constants = new TreeMap<Float,Vector<String>>();
	Integer idx=0;
	float pos_score=0, neg_score=0, pos_count=0, neg_count=0, pos_prob=0;
	float max=0, tmp_max=0, ig=0;
	boolean first=true;
	int index=0, count_pos=0;

	for (int k=0; k<labels.size(); k++) if (labels.get(k)==1) count_pos++;
	//float probability_pos_class=getTargetProportion(1);
	float probability_pos_class = (float)count_pos/(float)labels.size();

	//System.out.println("P+ = " + probability_pos_class);
	float entropy = computeEntropy().first();

	/* Iterate over constants */
	for(Iterator<Map.Entry<String, TreeMap<Integer,Float>>> i = entryset.iterator(); i.hasNext();)
	{
	    /* get current entity counts map */
	    Map.Entry<String, TreeMap<Integer,Float>> pair = i.next();
	    String key = pair.getKey();
	    TreeMap<Integer,Float> curr_targetcounts_map = pair.getValue();

	    if (curr_targetcounts_map.size()==0) continue;

	    Vector<Float> features = new Vector<Float>();
	    for (int k=0; k<example_index.size(); k++) features.add(new Float(0));
	    pos_count=0; neg_count=0;
	    max=0;

	    for (Map.Entry<Integer,Float> entry : curr_targetcounts_map.entrySet())
	    {
		idx = entry.getKey();
		Float label = entry.getValue();
		if (label==1) pos_count++;
		else neg_count++;
		//Float val = features.get(idx); val++;
		Float val = new Float(1);
		features.set(idx,val);
		if (val>max) max=val;
	    }

	    pos_prob = pos_count/(pos_count+neg_count);
	    ig = entropy-Scorer.informationGain(features,labels,(int)max).first();

	    if (pos_prob > probability_pos_class)
	    {
		Vector<String> v = pos_constants.get(-ig);
		if (v==null) v = new Vector<String>();
		v.add(key);
		pos_constants.put((float)(-ig),v);
		pos_score+=ig;
		//System.out.println("S+ "+ig+" "+pos_score);
	    }
	    else
	    {
		Vector<String> v = neg_constants.get(-ig);
		if (v==null) v = new Vector<String>();
		v.add(key);
		neg_constants.put((float)(-ig),v);
		neg_score+=ig;
		//System.out.println("S- "+ig+" "+neg_score);
	    }
	}

	//System.out.println("Scores: RIG(B+)="+pos_score+" RIG(B-)"+neg_score);

	if (pos_score > neg_score)
		best_constants = pos_constants;
	else
		best_constants = neg_constants;

	Vector<Float> features = new Vector<Float>();
	Vector<Float> tmp_features = new Vector<Float>();

	for (int k=0; k<example_index.size(); k++)
	{
	    features.add(new Float(0));
	    tmp_features.add(new Float(0));
	}

	Vector<String> constants_vector = new Vector<String>();

	for(Iterator<Map.Entry<Float,Vector<String>>> i = best_constants.entrySet().iterator(); i.hasNext();)
	{
	    Map.Entry<Float,Vector<String>> pair = i.next();
	    Vector<String> constants = pair.getValue();
	    for (int p=0; p<constants.size(); p++)
		    constants_vector.add(constants.elementAt(p));
	}

	for(int p=0; p<constants_vector.size(); p++)
	{
	    tmp_features = features;
	    tmp_max = max;

	    Vector<Float> tmp_subs = new Vector<Float>();
	    for (int k=0; k<tmp_features.size(); k++) tmp_subs.add(tmp_features.get(k));

	    //Map.Entry<Float,String> pair = i.next();
	    //Float key = pair.getKey();
	    //String constant = pair.getValue();
	    String constant = constants_vector.get(p);

	    //System.out.println("Constant "+constant);
	    TreeMap<Integer,Float> constant_table = countsmap.get(constant);
	    if (constant_table==null) continue;
	    for(Iterator<Map.Entry<Integer,Float>> it = constant_table.entrySet().iterator(); it.hasNext();)
	    {
	    	idx = it.next().getKey();
	    	//Float val = tmp_features.get(idx); val++;
		Float val = new Float(1);
	    	tmp_features.set(idx,val);
	    	if (val>tmp_max) tmp_max=val;
		//System.out.println(idx+" "+val+" "+tmp_max);
	    }

	    /* compute entity entropy */
	    tmp_rig = Scorer.informationGain(tmp_features,labels,(int)tmp_max);

	    if (first || ((entropy-tmp_rig.first()) >= (entropy-rig.first())))
	    {
		first = false;
		features = tmp_features;
		//System.out.println("Added "+tmp_rig+" "+rig);
		rig = tmp_rig;
		max = tmp_max;
	    }
	    else
	    {
		for (int k=0; k<tmp_subs.size(); k++) tmp_features.set(k,tmp_subs.get(k));
		//System.out.println("Discarded "+tmp_rig+" "+rig);
	    }
	}
   
	/* sum entropies weighting them by the proportion of their sets sizes */
	// return Scorer.weightedSum(entropyvec);
	// return new Pair(computeEntropy().first()-rig.first(), computeEntropy().second()-rig.second());
	return new Pair(rig.first(), rig.second());
    }
    
    public TreeMap<Float,Float> getTargetDistribution()
    {
	TreeMap<Float,Float> counts = new TreeMap<Float,Float>();
	
	synchronized(rstruct){ // rstruct must be locked while accessing ResultSet

	    /* excute query to get counts by target */
	    ResultSet res = (this.has_counts_only) ? 
		rstruct.executeQuery("SELECT target,counts from " + table_name) :
		rstruct.executeQuery("SELECT target,COUNT(target) from " + table_name + " GROUP BY target");
	    	    
	    /* browse query results */
	    try{
		while(res.next())
		    /* add current target counts pair */
		    counts.put(res.getFloat(1),res.getFloat(2));
	    }catch(java.sql.SQLException e){
		e.printStackTrace();
		System.exit(1);
	    }
	}
	
	return counts;
    }

    public Pair<Float,Integer> computeEntropy() throws java.lang.ArithmeticException
    {	
	return Scorer.computeEntropy(getTargetDistribution().values()); 
    }    

    public void cleanup()
    {
	// destroy table 
	rstruct.execute("DROP TABLE IF EXISTS " + table_name);
    }
    
    protected void finalize() throws Throwable
    {
	cleanup();
    }


    public Integer[] getCountsByVariables(Dataset parent_dataset, Variable[] parent_variables)
    {
	/* create group by condition */
	StringBuffer buf = new StringBuffer();
	for(int i = 0; i < parent_variables.length; i++)
	    buf.append(parent_variables[i].name() + ",");

	/* replace final comma with space */
	buf.setCharAt(buf.length()-1, ' ');
	
	/* recover new variable to remove NULL rows */
	TreeSet<Variable> parent_variables_set = TetUtilities.array2set(parent_variables);
	String newvarname = "*"; // count all rows by default
	for(Iterator<Variable> i = table_vars.iterator(); i.hasNext();){
	    Variable currvar = i.next();
	    if(!parent_variables_set.contains(currvar)){
		newvarname = currvar.name();
		break;
	    }
	}
	    
	Vector<Integer> counts = new Vector<Integer>();

	synchronized(rstruct){ // rstruct must be locked while accessing ResultSet

	    /* excute query to get counts by variables */
	    ResultSet res = rstruct.executeQuery("SELECT COUNT(" + newvarname + ") FROM " + 
						 parent_dataset.getRelationName() +
						 " LEFT JOIN " + table_name + 
						 " USING (" + buf.toString() + ")" +
						 " GROUP BY " + buf.toString());	    
	    /* browse query results */
	    try{
		while(res.next())
		    /* add current count */
		    counts.add(res.getInt(1));
	    }catch(java.sql.SQLException e){
		e.printStackTrace();
		System.exit(1);
	    }
	}
	
	return (Integer[])counts.toArray(new Integer[0]);
    }    

    public int getSize()
    {
	synchronized(rstruct){ // rstruct must be locked while accessing ResultSet

	    /* excute query to get table counts */
	    ResultSet res = rstruct.executeQuery("SELECT COUNT(*) FROM " + table_name);

	    /* browse query results */
	    try{
		res.first();
		return res.getInt(1);
	    }catch(java.sql.SQLException e){
		e.printStackTrace();
		System.exit(1);
	    }	    
	}

	return 0;
    }

    public Dataset getValueDataset(String table_name, TreeSet<Variable> bound_variables, float dpos, float dneg)
    {
	/* create list of variables to select */
	String selectvars = new String();

	/* iterate over table variables */
	for(Iterator<Variable> i = this.table_vars.iterator(); i.hasNext();)
	    selectvars += i.next().name() + ",";      

	/* add dpos and dneg columns */
	selectvars += dpos + " AS dpos," + dneg + " AS dneg";

	/* create temporary table sql */
	StringBuffer buf = new StringBuffer();
	buf.append("CREATE TEMPORARY TABLE " + table_name + "(");

	/* define type for dpos,dneg,otherwise precision depends on passed value!!*/
	buf.append("dpos FLOAT, dneg FLOAT,");

	/* add indices for table variables */
	buf.append("INDEX (");
	for(Iterator<Variable> i = this.table_vars.iterator(); i.hasNext();)
	    buf.append(i.next().name() + ",");

	/* replace final comma with parenthesis */
	buf.setCharAt(buf.length()-1, ')');

	/* add indices for bound variables */
	buf.append(", INDEX (");
	for(Iterator<Variable> i = bound_variables.iterator(); i.hasNext();)
	    buf.append(i.next().name() + ",");

	/* replace final comma with parenthesis */
	buf.setCharAt(buf.length()-1, ')');

	/* add select */
	buf.append(") SELECT " + selectvars + " FROM " + this.table_name);   

	/* execute sql */
	rstruct.execute(buf.toString());
	
	/* return newly created dataset */
	return new MySQLDataset(this.rstruct, table_name, this.table_vars);
    }

    public void aggregateValues(String table_name, Dataset child_dataset, 
				TreeSet<Variable> bound_variables, float dpos, float dneg, 
				Operator branch_operator, Operator value_operator)
    {	
	/* create select and group by condition */
	StringBuffer varbuf = new StringBuffer();

	/* iterate over table variables */
	for(Iterator<Variable> i = this.table_vars.iterator(); i.hasNext();)
	    varbuf.append(i.next().name() + ",");      

	/* remove final comma */
	varbuf.setLength(varbuf.length()-1);

	String dposvar,dnegvar;
        String branch_operation;

        switch(branch_operator){
        case PLUS:  
            branch_operation = "+";
            break;
        case TIMES:
        default:
            branch_operation = "*";
            break;
        }


	//ALTERNATIVE: 
	//FOR FALSE VALUES DEFAULTING TO BRANCH_OPERATOR NEUTRAL
// 	switch(value_operator){
// 	case PLUS:  
// 	    String sumquery = "sum(coalesce(" + child_dataset.getRelationName() + ".dpos/" + dpos + ",0))";	    
// 	    dposvar = this.table_name + ".dpos" + branch_operation  
// 		+ "IF(COUNT(" + child_dataset.getRelationName() + ".dpos)," + sumquery + "," 		
// 		+ branch_operator.neutral() + ")" + " AS dpos";
// 	    sumquery = "sum(coalesce(" + child_dataset.getRelationName() + ".dneg/" + dneg + ",0))";
// 	    dnegvar = this.table_name + ".dneg" + branch_operation
// 		+ "IF(COUNT(" + child_dataset.getRelationName() + ".dneg)," + sumquery + "," 
// 		+ branch_operator.neutral() + ")" + " AS dneg";
// 	    break;
// 	case TIMES:
// 	default:
// 	    /* create dpos and dneg column conditions, setting them to zero if 
// 	       there is at least a zero entry (log(0) != -Infinity in MySQL) */

// 	    dposvar = this.table_name + ".dpos" + branch_operation 
// 		+ "IF(COUNT(" + child_dataset.getRelationName() + ".dpos)," 
// 		+ "IF(MIN(coalesce(" + child_dataset.getRelationName() + ".dpos,1))=0,0,exp(sum(log(coalesce(" 
// 		+ child_dataset.getRelationName() + ".dpos/" + dpos + ",1))))), "
// 		+ branch_operator.neutral() + ")" + " AS dpos";

// 	    dnegvar = this.table_name + ".dneg" + branch_operation 
// 		+ "IF(COUNT(" + child_dataset.getRelationName() + ".dneg)," 
// 		+ "IF(MIN(coalesce(" + child_dataset.getRelationName() + ".dneg,1))=0,0,exp(sum(log(coalesce(" 
// 		+ child_dataset.getRelationName() + ".dneg/" + dneg + ",1))))), "
// 		+ branch_operator.neutral() + ")" + " AS dneg";

// 	    break;
// 	}	

	//REMOVE FOR FALSE VALUES DEFAULTING TO BRANCH_OPERATOR NEUTRAL
	switch(value_operator){
	case PLUS:  
	    dposvar = this.table_name + ".dpos" + branch_operation + "sum(coalesce(" 
		+ child_dataset.getRelationName() + ".dpos/" + dpos + ",0)) AS dpos";
	    dnegvar = this.table_name + ".dneg" + branch_operation + "sum(coalesce(" 
		+ child_dataset.getRelationName() + ".dneg/" + dneg + ",0)) AS dneg";
	    break;
	case TIMES:
	default:
	    /* create dpos and dneg column conditions, setting them to zero if 
	       there is at least a zero entry (log(0) != -Infinity in MySQL) */
	    dposvar = "IF(MIN(coalesce(" + child_dataset.getRelationName() + ".dpos,1))=0," +
		this.table_name + ".dpos" + branch_operation + "0," +
		this.table_name + ".dpos" + branch_operation + "exp(sum(log(coalesce(" 
		+ child_dataset.getRelationName() + ".dpos/" + dpos + ",1))))) AS dpos";
	    dnegvar = "IF(MIN(coalesce(" + child_dataset.getRelationName() + ".dneg,1))=0," +
		this.table_name + ".dneg" + branch_operation + "0," +
		this.table_name + ".dneg" + branch_operation + "exp(sum(log(coalesce(" 
		+ child_dataset.getRelationName() + ".dneg/" + dneg + ",1))))) AS dneg";
	    break;
	}	

	/* create temporary table sql */
	StringBuffer buf = new StringBuffer();
	buf.append("CREATE TEMPORARY TABLE " + table_name + " (");

	/* add indices for bound variables */
	buf.append("INDEX (");
	for(Iterator<Variable> i = bound_variables.iterator(); i.hasNext();)
	    buf.append(i.next().name() + ",");

	/* replace final comma with parenthesis */
	buf.setCharAt(buf.length()-1, ')');

	/* add select */
	buf.append(") SELECT " + varbuf + "," + dposvar + "," + dnegvar + " FROM " + this.table_name);

	/* add join condition */
	buf.append(" LEFT JOIN " + child_dataset.getRelationName() + " USING (" + varbuf + ") GROUP BY " + varbuf);

	/* execute create table statement */       
	rstruct.execute(buf.toString());

	/* drop current table */
	cleanup();
	
	/* replace with newly created one */
	this.table_name = table_name;
    }

    public void reweightValues(float dpos, float dneg)
    {
	/* create select sql */
	StringBuffer buf = new StringBuffer();
	buf.append("UPDATE " + table_name + " SET dpos=dpos*" + dpos + ",dneg=dneg*" + dneg);

	/* execute create table statement */       
	rstruct.execute(buf.toString());
    }
	    
    public Float[] computeValues()
    {
	Vector<Float> values = new Vector<Float>();

	synchronized(rstruct){ // rstruct must be locked while accessing ResultSet

	    StringBuffer buf = new StringBuffer();
	    
	    buf.append("SELECT dpos,dneg FROM " + this.table_name + " ORDER BY ");

	    /* iterate over table variables to get same order as the one in aggregate value computation */
	    for(Iterator<Variable> i = this.table_vars.iterator(); i.hasNext();)
		buf.append(i.next().name() + ",");      
	    
	    /* remove final comma */
	    buf.setLength(buf.length()-1);

	    /* excute query to get dpos dneg values */
	    ResultSet res = rstruct.executeQuery(buf.toString());
	    	    
	    /* browse query results */
	    try{
		while(res.next())
		    /* add current value */
		    values.add((float)(res.getDouble(1)/res.getDouble(2)));
	    }catch(java.sql.SQLException e){
		e.printStackTrace();
		System.exit(1);
	    }
	}
	
	return (Float[])values.toArray(new Float[0]);
    }

    /** 
     * create a table with all positive examples 
     */
    public void createPositiveExampleTable(String positive_table_name, Vector<Variable> vars)
    {
	/* drop positive examples table if it exists */
	rstruct.execute("DROP TABLE IF EXISTS " + positive_table_name);

	StringBuffer buf = new StringBuffer(1024);
	buf.append("CREATE TABLE " + positive_table_name + " (");

	/* add indices */
	for(int i = 0; i < vars.size(); i++)
	    buf.append("INDEX ("+ vars.elementAt(i).name() + "),");

	/* replace final comma with parenthesis */
	buf.setCharAt(buf.length()-1, ')');

	/* select variables in correct order */
	buf.append("SELECT ");
	for(int i = 0; i < vars.size(); i++)
	    buf.append(vars.elementAt(i).name() + "AS arg" + i+1 + ",");

	/* replace final comma with space */
	buf.setCharAt(buf.length()-1, ' ');
	
	/* select only positive examples */
	buf.append(" FROM " + this.table_name + " WHERE target=1");

	rstruct.execute(buf.toString());
    }

    /** 
     * create a table with all positive examples given specified threshold
     */
    public int createPositiveExampleTable(String positive_table_name, Vector<Variable> vars, 
					  double threshold, boolean hasprevious, int iteration)
    {
	String tmp_positive_table_name = positive_table_name + "_tmp";

	/* drop previous tmp tables if any */
	rstruct.execute("DROP TABLE IF EXISTS " + tmp_positive_table_name);

	if(hasprevious)
	    /* move previous positive examples table to temporary one */
	    rstruct.execute("RENAME TABLE " + positive_table_name + " TO " + tmp_positive_table_name);
	else
	    /* drop previous positive tables if any */
	    rstruct.execute("DROP TABLE IF EXISTS " + positive_table_name);

	StringBuffer buf = new StringBuffer(1024);
	buf.append("CREATE TABLE " + positive_table_name + " (");

	/* add indices */
	for(int i = 0; i < vars.size(); i++)
	    buf.append("INDEX (arg" + (i+1) + "),");

	/* add overall index */
	buf.append("INDEX (");
	for(int i = 0; i < vars.size(); i++)
	    buf.append("arg" + (i+1) + ","); 
	/* replace final comma with parenthesis */
	buf.setCharAt(buf.length()-1, ')');
	

	/* select variables in correct order */
	buf.append(") SELECT ");
	for(int i = 0; i < vars.size(); i++)
	    buf.append(vars.elementAt(i).name() + " AS arg" + (i+1) + ",");

	/* replace final comma with space */
	buf.setCharAt(buf.length()-1, ' ');
	
	/* select only positive examples */
	buf.append(" FROM " + this.table_name + " WHERE dneg=0 AND dpos>0 OR dpos/dneg > " + threshold);

	rstruct.execute(buf.toString());

	/* collect number of disagreements with previous table */
	if(hasprevious){
	    try{
		synchronized(rstruct){ // rstruct must be locked while accessing ResultSet

		    ResultSet rs;
		    
		    /* collect number of previously classified positives */
		    rs = rstruct.executeQuery("SELECT COUNT(*) FROM " + tmp_positive_table_name);
		    rs.first();
		    int oldpos = rs.getInt(1);

		    // DEBUG
		    //rstruct.execute("SELECT * FROM " + tmp_positive_table_name + 
		    //" INTO OUTFILE '/tmp/" + tmp_positive_table_name + iteration + "'");

		    /* collect number of currently classified positives */
		    rs = rstruct.executeQuery("SELECT COUNT(*) FROM " + positive_table_name);
		    rs.first();
		    int newpos = rs.getInt(1);
		    
		    // DEBUG
		    //rstruct.execute("SELECT * FROM " + positive_table_name + 
		    //" INTO OUTFILE '/tmp/" + positive_table_name + iteration + "'");

		    /* collect classification intersection */
		    rs = rstruct.executeQuery("SELECT COUNT(*) FROM " + 
					      tmp_positive_table_name + 
					      " NATURAL JOIN " + positive_table_name);
		    rs.first();
		    int intersection = rs.getInt(1);

		    return oldpos+newpos-2*intersection;
		}

	    }catch(java.sql.SQLException e){
		e.printStackTrace();
		System.exit(1);
	    }
	}

	return Integer.MAX_VALUE;	
    }	

    
    public void printTable()
    {

        try{
	    synchronized(rstruct){ // rstruct must be locked while accessing ResultSet

		ResultSet rs = rstruct.executeQuery("SELECT * FROM " + table_name);
		ResultSetMetaData rsmd = rs.getMetaData();  
		int numberOfColumns = rsmd.getColumnCount();
		
		System.out.println("Begin printing table: " + table_name);
		
		while(rs.next()){   
		    String s="";
		    for(int i = 1; i <= numberOfColumns; i++){
			s += rs.getObject(i) + "\t";
		    }       
		    System.out.println(s);
		}
		System.out.println("Done printing table: " + table_name);
	    }
        }catch(java.sql.SQLException e){
            e.printStackTrace();
            System.exit(1);
        }

    }


    /**********************************************************************************************************/
    /*************************************** private methods **************************************************/

    private void init(String datafile, Vector<Variable> relation_vars) throws Exception
    {

	if(table_vars.size() == 0)
	    throw new Exception("Trying to create table with no column in MySQLDataset");       

	// create empty table
	StringBuffer buf = new StringBuffer(1024);
	buf.append("CREATE TEMPORARY TABLE " + table_name + " (");
	/* add indices */
	for(int i = 0; i < relation_vars.size(); i++)
	    buf.append("INDEX ("+ relation_vars.elementAt(i).name() + "),");
	/* add column definitions */
	for(int i = 0; i < relation_vars.size(); i++)
	    buf.append(relation_vars.elementAt(i).name() + " " + datatype + ",");
	buf.append("target FLOAT)");

	rstruct.execute(buf.toString());

	// fill table from file datafile
	rstruct.execute("LOAD DATA LOCAL infile '" +  datafile + "' into table `" + table_name + "`");
    }

    private void init(Type type, MySQLDataset parent, boolean isLeaf) throws Exception
    {	
	/* recover parent table variables */
	TreeSet<Variable> parent_table_vars = parent.getRelationVars();

	/* add target variable */
	Variable targetvar = new Variable("target");
	parent_table_vars.add(targetvar);

	/* create type query */
	String typequery = rstruct.typeValueQuery(type, parent.getRelationName(), parent_table_vars, false);

	/* remove target variable */
	parent_table_vars.remove(targetvar);

	/* create temporary table sql */
	StringBuffer buf = new StringBuffer();
	buf.append("CREATE TEMPORARY TABLE " + table_name);
	
	/* add indices if not in a leaf */
	if(!isLeaf){
	    buf.append("(");
	    for(Iterator<Variable> i = table_vars.iterator(); i.hasNext();)
		buf.append("INDEX ("+ i.next().name() + "),"); 
	    /* replace final comma with parenthesis */
	    buf.setCharAt(buf.length()-1, ')');
	}

	/* add type query */
	buf.append(" " + typequery);

	rstruct.execute(buf.toString());
    }

    private void initToDifference(MySQLDataset fullset, MySQLDataset subset, 
				  TreeSet<Variable> difference_vars, boolean isLeaf)
    {
	/* create list of variables to bound with */
	StringBuffer bindbuf = new StringBuffer();	

	/* group arguments */
	for(Iterator<Variable> i = difference_vars.iterator(); i.hasNext();)
	    bindbuf.append(i.next().name() + ",");      

	/* replace final comma with parenthesis */
	if(bindbuf.length() > 0)
	    bindbuf.deleteCharAt(bindbuf.length()-1);

	/* create temporary table sql */
	StringBuffer buf = new StringBuffer();
	buf.append("CREATE TEMPORARY TABLE " + table_name);
	
	/* add indices if not in a leaf */
	if(!isLeaf){
	    buf.append("(");
	    for(Iterator<Variable> i = table_vars.iterator(); i.hasNext();)
		buf.append("INDEX ("+ i.next().name() + "),"); 
	    /* replace final comma with parenthesis */
	    buf.setCharAt(buf.length()-1, ')');
	}

	/* add select */
	buf.append(" SELECT * FROM " + fullset.getRelationName());
    
	/* add complement condition if present */
	if(bindbuf.length() > 0)
	    buf.append(" WHERE (" + bindbuf.toString() + ") NOT IN (SELECT " + 
		       bindbuf.toString() + " FROM " + subset.getRelationName() + ")");

	/* execute sql */
	rstruct.execute(buf.toString());
    }    
    
    /** 
     * init dataset to target counts of difference between fullset and subset
     */
    private void initToDifferenceCounts(MySQLDataset fullset, MySQLDataset subset)
    {
	/* set has_counts_only attribute */
	has_counts_only = true;

	String fullcounts = "(SELECT target,count(target) AS fullcounts FROM " 
	    + fullset.getRelationName() + " GROUP BY target) as fulltable";

	String aftercounts = "(SELECT target,count(target) AS subcounts FROM " 
	    + subset.getRelationName() + " GROUP BY target) as subtable";

	rstruct.execute("CREATE TEMPORARY TABLE " + table_name + 
			" SELECT target,fullcounts-COALESCE(subcounts,0) as counts FROM " +
			fullcounts + " NATURAL LEFT JOIN " + aftercounts);
    }
}
