package Tetpackage;

import java.util.*;
import mymath.*;
import java.sql.*;

/** MySQL implementation of RelStructure */

public class MySQLRelStructure implements RelStructure{

    Connection connection;
    Statement statement;
    String database;
    String login; 
    String password;
    Boolean use_TDBU_query = false;

    private static int MAXQUERYSIZE = 2046;
    private static int MAXCOLUMNSIZE = 50000; // WARNING: setting to 100000 does NOT work!!
    private static int TABRANDSIZE = 5; // size of random part of temporary table names
    private static String colprefix = "arg";
    
    // database example is localhost:3306/test
    public MySQLRelStructure(String database_, String login_, String password_) throws 
	java.lang.ClassNotFoundException,java.sql.SQLException
    {
	database = database_;
	login = login_;
	password = password_;

	Connect();
    }

    public MySQLRelStructure(String database_, String login_, String password_, Boolean use_TDBU_query) throws 
	java.lang.ClassNotFoundException,java.sql.SQLException
    {
	this(database_, login_, password_);
	this.use_TDBU_query = use_TDBU_query;
    }

    protected void finalize() throws Throwable
    {
	Close();
    }
    
    public void Connect() throws 
	java.lang.ClassNotFoundException,java.sql.SQLException
    {
	Class.forName("com.mysql.jdbc.Driver");
	connection = DriverManager.getConnection("jdbc:mysql://" + database, login, password);
	statement = connection.createStatement();
	statement.execute("SET SESSION group_concat_max_len = " + MAXCOLUMNSIZE);
	//statement.execute("FLUSH TABLES");
    }

    public void Close() throws java.sql.SQLException
    {
	statement.execute("FLUSH TABLES");
	statement.close();
	connection.close();
    }

    public void setUseTDBUQuery(Boolean use_TDBU_query)
    {
	this.use_TDBU_query = use_TDBU_query;
    }

    public void drop(String tablename) throws java.sql.SQLException
    {
	execute("DROP TABLE " + tablename);
    }
    
    public synchronized void execute(String sql) 
    {
	try{
	    //System.out.println(sql);
	    //long timebefore = System.currentTimeMillis();
	    statement.execute(sql);
	    //System.out.println("Took: " + (System.currentTimeMillis()-timebefore) + " milliseconds");
	}catch(Exception e){
	    e.printStackTrace();
	    System.out.println("Executing sql:");
	    System.out.println(sql);
	    System.out.println("Exiting...");
	    System.exit(1);
	}
    }
    
    public synchronized ResultSet executeQuery(String sql) 
    {
	try{
	    //System.out.println(sql);
	    return statement.executeQuery(sql);
	}catch(Exception e){
	    e.printStackTrace();
	    System.out.println("Executing query:");
	    System.out.println(sql);
	    System.out.println("Exiting...");
	    System.exit(1);
	}

	return null;
    }
    
    public synchronized Vector<HashMap<String, Object>> queryResult(String sql) 
    {
	Vector<HashMap<String, Object>> results = new Vector<HashMap<String, Object>>();

	try{
	    ResultSet rs = executeQuery(sql);
	    ResultSetMetaData rsmd = rs.getMetaData();
	    int numberOfColumns = rsmd.getColumnCount();
	    
	    while(rs.next()){
		HashMap<String, Object> curr_res = new HashMap<String, Object>(numberOfColumns);
		for(int i = 1; i <= numberOfColumns; i++)
		    curr_res.put(rsmd.getColumnLabel(i), new RelObject(rs.getString(i)));		
		results.add(curr_res);
	    }
	}catch(Exception e){
	    e.printStackTrace();
	    System.out.println("Executing query:");
	    System.out.println(sql);
	    System.out.println("Exiting...");
	    System.exit(1);
	}

	return results;	
    }

    public String typeValueQuery(Type type, 
				 String boundtable_name, 
				 TreeSet<Variable> boundtable_vars,
				 Boolean returnvalue) throws Exception
    { 				 
	/* store table in set of boundtables */
	TreeSet<String> boundtables = new TreeSet<String>();
	boundtables.add(boundtable_name);

	/* fill variable values with references to boundtab_name columns */
	HashMap<String, Object> boundvalues = new HashMap<String, Object>();
	for(Iterator<Variable> v = boundtable_vars.iterator(); v.hasNext();){
	    String varname = v.next().name();
	    Variable varvalue = new Variable(boundtable_name + "." + varname);
	    boundvalues.put(varname, varvalue);
	}
	
	return typeValueQuery(type, boundtable_vars, boundvalues, boundtables, returnvalue);
    }
    
    public String TetValueString(Tet tet, 
				 TreeSet<Variable> boundvars, 
				 HashMap<String, Object> boundvalues, 
				 Boolean trueonly) throws Exception
    {	
	if(use_TDBU_query)
	    return TetValueStringTD_BU(tet, boundvars, boundvalues, trueonly);
	
	return TetValueStringSingleQuery(tet, boundvars, boundvalues, trueonly);
    }


    /*******************************************************************************/
    /* SINGLE QUERY VERSION */

    private String TetValueStringSingleQuery(Tet tet, 
					     TreeSet<Variable> boundvars, 
					     HashMap<String, Object> boundvalues, 
					     Boolean trueonly) throws Exception
    {	
	/* initialize a counter to allow unique substructure names in the query */
	Counter counter = new Counter(0);

	/* have the relational structure build the TET value query */
	String query = tetValueQuery(tet, boundvars, boundvalues, trueonly, counter);

	return getValue(query);
    }

    private String tetValueQuery(Tet tet, 
				 TreeSet<Variable> boundvars, 
				 HashMap<String, Object> boundvalues, 
				 Boolean trueonly,
				 Counter counter) throws Exception
    {	
	/* generic value not implemented yet */
	if(!trueonly)
	    throw new Exception("Generic value not implemented yet! (found trueonly=false)"); 	

	/* create query for node type */	
        String type_query = typeValueQuery(tet.root, boundvars, boundvalues, null, true);
	
        /* check if end of recursion */
        if(tet.children.size() == 0)
            return type_query;

        String nodeid = counter.toString(); 
	counter.increment();
	String nodetable = "node_" + nodeid;

	/* build SELECT statement */
	StringBuffer query = new StringBuffer(MAXQUERYSIZE); 
	 

	/* compute free variables for the current node which are not in boundvars */
	TreeSet<Variable> freevars = new TreeSet<Variable>();
	freevars.addAll(tet.freevars());
	freevars.removeAll(boundvars);

	query.append("SELECT ");
	for(Iterator<Variable> v = boundvars.iterator(); v.hasNext();){
	    String varname = v.next().name();
	    query.append(nodetable + "." +  varname + " AS " + varname + ",");
	}
	for(Iterator<Variable> v = freevars.iterator(); v.hasNext();){
	    String varname = v.next().name();
	    query.append(nodetable + "." +  varname + " AS " + varname + ",");
	}
	query.append("CONCAT('('," + nodetable + ".value");
        for(int i = 0; i < tet.children.size(); i++){
	    String counts_table = "counts_child_" + nodeid + "_" + i;
	    query.append(",',',CONCAT('[',COALESCE(GROUP_CONCAT(DISTINCT " + counts_table + ".value" 
		+ " ORDER BY " + counts_table + ".value),' '),']')");	    
	}
	query.append(",')') AS value ");
	    
	/* build FROM statement */ 
	query.append("FROM (" + type_query + ") AS " + nodetable + " ");
        for(int i = 0; i < tet.children.size(); i++){
            String childtable = "child_" + nodeid + "_" + i;
	    String counts_table = "counts_" + childtable;
	    Tet childtet = tet.children.elementAt(i).subtree;
            String childquery = tetValueQuery(childtet, boundvars, boundvalues, trueonly, counter);

	    /* compute free variables for the current child node which are not in boundvars */
	    TreeSet<Variable> childfreevars = new TreeSet<Variable>();
	    childfreevars.addAll(childtet.freevars());
	    childfreevars.removeAll(boundvars);
	    childfreevars.removeAll(tet.children.elementAt(i).edgelabel);

	    query.append(" LEFT JOIN (SELECT ");
	    for(Iterator<Variable> v = boundvars.iterator(); v.hasNext();){
		String varname = v.next().name();
		query.append(childtable + "." + varname + " AS " + varname + ","); 
	    }
	    for(Iterator<Variable> v = childfreevars.iterator(); v.hasNext();){
		String varname = v.next().name();
		query.append(childtable + "." +  varname + " AS " + varname + ",");
	    }
	    query.append("CONCAT_WS(':'," + childtable + ".value,COUNT(" + childtable 
		+ ".value)) AS value FROM (" + childquery + ") AS " + childtable + " GROUP BY ");
	    for(Iterator<Variable> v = boundvars.iterator(); v.hasNext();)
		query.append(childtable + "." + v.next().name() + ",");
	    for(Iterator<Variable> v = childfreevars.iterator(); v.hasNext();)
		query.append(childtable + "." + v.next().name() + ",");
	    String firstvar = boundvars.first().name();
	    query.append(childtable + ".value) AS " + counts_table + " ON ");
	    for(Iterator<Variable> v = boundvars.iterator(); v.hasNext();){
		String varname = v.next().name();
		query.append(counts_table + "." + varname + "="
			     + nodetable + "." + varname + " AND ");
	    }
	    /* compute free variables for the current child which are not bounded
	       but appear also in the parent (the ones parent and child have to JOIN ON) */
	    TreeSet<Variable> childparentfreevars = new TreeSet<Variable>();
	    childparentfreevars.addAll(freevars);
	    childparentfreevars.retainAll(childfreevars);
	    for(Iterator<Variable> v = childparentfreevars.iterator(); v.hasNext();){
		String varname = v.next().name();
		query.append(counts_table + "." + varname + "="
			     + nodetable + "." + varname + " AND ");
	    }
	    query.delete(query.length()-5,query.length()); // remove final AND
	}
	query.append(" GROUP BY "); 
	for(Iterator<Variable> v = boundvars.iterator(); v.hasNext();){
	    String varname = v.next().name();
	    query.append(nodetable + "." + varname + ","); 
	}
	for(Iterator<Variable> v = freevars.iterator(); v.hasNext();){
	    String varname = v.next().name();
	    query.append(nodetable + "." +  varname + ",");
	}
	if(query.charAt(query.length()-1) == ',')
	    query.deleteCharAt(query.length()-1);

	return query.toString();
    }
    
    private synchronized String getValue(String query) throws Exception
    {
	String result = new String();
	
	ResultSet rs = executeQuery(query);

	if(!rs.first())
	    result = "F";
	else if(!rs.isLast())
	    throw new Exception("Expected a single row for query:\n" + query);
	else
	    result = rs.getString("value");

	rs.close();
	//statement.execute("FLUSH TABLES");
	
	return result;
    } 


    /*******************************************************************************/
    /* TD BU PROCEDURE VERSION */

    private String TetValueStringTD_BU(Tet tet, 
				       TreeSet<Variable> boundvars, 
				       HashMap<String, Object> boundvalues, 
				       Boolean trueonly) throws Exception
    {	
	/* get random string for temporary table names */
	String tab_rand = TetUtilities.randomString(TABRANDSIZE);
       
	/* initialize a counter to allow unique temporary table names */
	Counter counter = new Counter(0);

	/* create root table for top down procedure */
	computeRootTypeValue(tet.root, boundvars, boundvalues, tab_rand, counter);

	/* run top down procedure */	
	runTetValueComputationTD(tet, boundvars, tab_rand, counter);

	/* reset counter */
	counter.clear();

	/* run bottom up procedure */	
	String result_table = runTetValueComputationBU(tet, boundvars, trueonly, tab_rand, counter);
	
	/* collect Tet Value String */
	String value = collectTetValue(result_table);
	
	/* clean up remained temporary table */
	drop(result_table);

	/* return Tet Value String */
	return value;
    }
    
    private void computeRootTypeValue(Type root, 
				      TreeSet<Variable> boundvars, 
				      HashMap<String, Object> boundvalues, 
				      String tab_rand,
				      Counter counter) throws Exception
    {	   
	/* create query for node type */	
        String type_query = typeValueQuery(root, boundvars, boundvalues, null, true);

	/* create a unique tmp table name */
	String table_name = "tmp_" + tab_rand + "_TD_" + counter.toString(); 

	/* create a temporary table query */
	String buf = "CREATE TEMPORARY TABLE " + table_name + " ENGINE=MEMORY " + type_query;

	/* execute query */
	execute(buf);
    }

    private void computeTypeValue(Type root, 
				  TreeSet<Variable> boundvars, 
				  String boundvalues_table, 
				  String tab_rand,
				  Counter counter) throws Exception
    {	   
	/* create query for node type */ /* WARNING CHECK TYPEVALUEQUERY */
        String type_query = typeValueQuery(root, boundvalues_table, boundvars, true);

	/* create a unique tmp_ table name */
	String table_name = "tmp_" + tab_rand + "_TD_" + counter.toString(); 

	/* create a temporary table query */
	String buf = "CREATE TEMPORARY TABLE " + table_name + " ENGINE=MEMORY " + type_query;

	/* execute query */
	execute(buf);
    }

    private void runTetValueComputationTD(Tet tet,
					  TreeSet<Variable> boundvars, 
					  String tab_rand,
					  Counter counter) throws Exception					 
    {
	/* get tmp_ table name for root type values */ 
	String table_name = "tmp_" + tab_rand + "_TD_" + counter.toString(); 
	
	/* increment counter to allow unique table names in recursion */
	counter.increment();

	/* check if end of recursion */
        if(tet.children.size() == 0)
            return;

	/* iterate over root children */
	for(int i = 0; i < tet.children.size(); i++){

	    /* collect childtet  */
	    Tet childtet = tet.children.elementAt(i).subtree;

	    /* create temporary table from root + child */
	    computeTypeValue(childtet.root, boundvars, table_name, tab_rand, counter);

	    /* recover new set of bound variables */ 
	    TreeSet<Variable> newboundvars = TetUtilities.merge(boundvars, tet.children.elementAt(i).edgelabel);

	    /* recur into child */
	    runTetValueComputationTD(childtet, newboundvars, tab_rand, counter);
	}

    }

    private String runTetValueComputationBU(Tet tet,
					    TreeSet<Variable> boundvars, 
					    Boolean trueonly,
					    String tab_rand,
					    Counter counter) throws Exception
    {
	/* get tmp_ table id to return */
	String root_id = counter.toString(); 
	
	/* root table name contains values from TD procedure */ 
	String root_table_name = "tmp_" + tab_rand + "_TD_" + root_id; 

	/* increment counter to allow unique table names in recursion */
	counter.increment();

	/* check if end of recursion */
        if(tet.children.size() == 0)
	    /* leaf BU tables coincide with TD ones */
            return root_table_name;

	/* create String Vector to store children tables names  */
	String[] children_table_names = new String[tet.children.size()];

	/* create TreeSet<Variable> Vector to store children boundvars */
	Vector<TreeSet<Variable>> children_boundvars = new Vector<TreeSet<Variable>>();
	/* create TreeSet<Variable> Vector to store children edgevars */
	Vector<TreeSet<Variable>> children_edgevars = new Vector<TreeSet<Variable>>();

	/* recur into root children */
	for(int i = 0; i < tet.children.size(); i++){

	    /* recover new set of bound variables */ 
	    children_boundvars.add(TetUtilities.merge(boundvars, tet.children.elementAt(i).edgelabel));

	    /* recover new set of edge variables */ 
	    children_edgevars.add(tet.children.elementAt(i).edgelabel);

	    /* recur into child */
	    children_table_names[i] = runTetValueComputationBU(tet.children.elementAt(i).subtree, 
							       children_boundvars.elementAt(i), 
							       trueonly, tab_rand, counter);
	}

	/* dst table will store results of value combination */
	String dst_table_name = "tmp_" + tab_rand + "_BU_" + root_id;

	/* create temporary table from root + children aggregating children values */
	combineValues(dst_table_name, root_table_name, children_table_names, boundvars, 
		      children_edgevars, trueonly);

	/* drop already used temporary tables */
	drop(root_table_name);
	for(int i = 0; i < tet.children.size(); i++)
	    drop(children_table_names[i]);

	/* return dst table name  */ 
	return dst_table_name;
    }

    private void combineValues(String dst_table_name,
			       String root_table_name,
			       String[] children_table_names,
			       TreeSet<Variable> boundvars, 
			       Vector<TreeSet<Variable>> children_edgevars,
			       Boolean trueonly) throws Exception
    {
	if(trueonly)
	    combineValuesNoFalse(dst_table_name,
				 root_table_name,
				 children_table_names,
				 boundvars);
	else	    
	    combineValuesWithFalse(dst_table_name,
				   root_table_name,
				   children_table_names,
				   boundvars, 
				   children_edgevars);	
    }

    private void combineValuesNoFalse(String dst_table_name,
				      String root_table_name,
				      String[] children_table_names,
				      TreeSet<Variable> boundvars) throws Exception
    {    
	/* check that root table has at least one bound variable */
	if(boundvars.size() == 0)
	    throw new Exception("Expected at least one bound variable in root table, found none.");

	/* build statement */
	StringBuffer sql = new StringBuffer(MAXQUERYSIZE); 
	
	/* initialize statement with create table and select */
	sql.append("CREATE TEMPORARY TABLE " + dst_table_name + " SELECT ");

	/* select all root bound variables */
	for(Iterator<Variable> v = boundvars.iterator(); v.hasNext();){
	    String varname = v.next().name();
	    sql.append(root_table_name + "." +  varname + " AS " + varname + ",");
	}
	
	/* select value */
	sql.append("CONCAT('('," + root_table_name + ".value");
        for(int i = 0; i < children_table_names.length; i++){
	    String counts_table = "counts_child_" + i;
	    sql.append(",',',CONCAT('[',COALESCE(GROUP_CONCAT(DISTINCT " + counts_table + ".value" 
		+ " ORDER BY " + counts_table + ".value),' '),']')");	    	
	}
	sql.append(",')') AS value ");

	/* build FROM statement */ 
	sql.append("FROM  " + root_table_name + " ");

	/* build FROM statement */ 
	for(int i = 0; i < children_table_names.length; i++){
            String childtable = children_table_names[i];
	    String counts_table = "counts_child_" + i;

	    sql.append(" LEFT JOIN (SELECT ");
	    for(Iterator<Variable> v = boundvars.iterator(); v.hasNext();){
		String varname = v.next().name();
		sql.append(childtable + "." + varname + " AS " + varname + ","); 
	    }
	    sql.append("CONCAT_WS(':'," + childtable + ".value,COUNT(" + childtable 
		+ ".value)) AS value FROM " + childtable + " GROUP BY ");
	    for(Iterator<Variable> v = boundvars.iterator(); v.hasNext();)
		sql.append(childtable + "." + v.next().name() + ",");
	    String firstvar = boundvars.first().name();
	    sql.append(childtable + ".value) AS " + counts_table + " ON ");
	    for(Iterator<Variable> v = boundvars.iterator(); v.hasNext();){
		String varname = v.next().name();
		sql.append(counts_table + "." + varname + "="
			     + root_table_name + "." + varname + " AND ");
	    }
	    sql.delete(sql.length()-5,sql.length()); // remove final AND
	}

	/* group by root bound variables */
	sql.append(" GROUP BY "); 
	for(Iterator<Variable> v = boundvars.iterator(); v.hasNext();)
	    sql.append(root_table_name + "." + v.next() + ","); 
	
	/* remove final comma */
	if(sql.charAt(sql.length()-1) == ',')
	    sql.deleteCharAt(sql.length()-1);

	/* execute sql statement */
	execute(sql.toString());
    }        

    private void combineValuesWithFalse(String dst_table_name,
					String root_table_name,
					String[] children_table_names,
					TreeSet<Variable> boundvars, 
					Vector<TreeSet<Variable>> children_edgevars) throws Exception
    {    
	/* check that root table has at least one bound variable */
	if(boundvars.size() == 0)
	    throw new Exception("Expected at least one bound variable in root table, found none.");

	/* build statement */
	StringBuffer sql = new StringBuffer(MAXQUERYSIZE); 
	
	/* initialize statement with create table and select */
	sql.append("CREATE TEMPORARY TABLE " + dst_table_name + " SELECT ");

	/* select all root bound variables */
	for(Iterator<Variable> v = boundvars.iterator(); v.hasNext();){
	    String varname = v.next().name();
	    sql.append(root_table_name + "." +  varname + " AS " + varname + ",");
	}
	
	/* select value */
	sql.append("CONCAT('('," + root_table_name + ".value");
        for(int i = 0; i < children_table_names.length; i++){
            String childtable = children_table_names[i];
	    String counts_table = "counts_child_" + i;


	    /* recover tot counts */
	    String tot_count = "";
	    if(children_edgevars.elementAt(i).size() > 0)
		tot_count = "tot_" + i + ".tot_count";
	    else /* recover tot counts from parent table */
		tot_count = "count(*)";

	    if(isLeafTable(childtable))
		sql.append(",',',CONCAT('[',COALESCE(GROUP_CONCAT(DISTINCT " + counts_table + ".value" 
			   + " ORDER BY " + counts_table + ".value)," 
			   + "CONCAT('F:'," + tot_count + ")),']')");	  
	    else
		sql.append(",',',CONCAT('[',COALESCE(TRIM(LEADING ',' FROM CONCAT("
			   + "GROUP_CONCAT(DISTINCT CONCAT("
			   + counts_table + ".value,':'," + counts_table + ".pos_count)"
			   + " ORDER BY " + counts_table + ".value),',')),''),CONCAT_WS(':','(F,[ ])'," 
			   + tot_count + "-COALESCE(MAX(" 
			   + counts_table + ".pos_count),0)),']')");
	}
	sql.append(",')') AS value ");

	/* build FROM statement */ 
	sql.append("FROM  (" + root_table_name);

	/* recover tot counts */ 
	for(int i = 0; i < children_table_names.length; i++){
	    String tot_table = "tot_" + i;

	    if(children_edgevars.elementAt(i).size() > 0){
		/* cartesian product of edgevartypes */
		    sql.append(",(SELECT count(*) as tot_count FROM ");
		    int j = 0;
		    for(Iterator<Variable> ev = children_edgevars.elementAt(i).iterator(); ev.hasNext();j++){
			String tname = ev.next().type(); 
			sql.append(tname + " as " + tname + j + ",");
		    }
		    /* remove final comma */
		    if(sql.charAt(sql.length()-1) == ',')
			sql.deleteCharAt(sql.length()-1);
		    sql.append(") AS " + tot_table);
	    }	      
	}
	
	sql.append(")");
	
	/* recover pos and tot counts */
	for(int i = 0; i < children_table_names.length; i++){
            String childtable = children_table_names[i];
	    String counts_table = "counts_child_" + i;

	    /* recover childvars */
	    String childvars = "";
	    
	    sql.append(" LEFT JOIN (SELECT ");
	    /* recover childvars */
	    for(Iterator<Variable> v = boundvars.iterator(); v.hasNext();)
		sql.append(v.next().name() + ",");

	    /* combine pos and neg counts */	    
	    if(isLeafTable(childtable)){
		sql.append("CONCAT_WS(',',CONCAT_WS(':',pos.value,pos.pos_count),CONCAT_WS(':','F',tot.tot_count-pos.pos_count)) AS value FROM ");
		/* compute pos counts for value */	    
		sql.append("(SELECT ");
		/* recover childvars */
		for(Iterator<Variable> v = boundvars.iterator(); v.hasNext();){
		    String varname = v.next().name();
		    sql.append(childtable + "." + varname + " AS " + varname + ","); 
		}
	    }
	    sql.append(childtable + ".value,COUNT(" + childtable + ".value) AS pos_count FROM "
		       + childtable + " GROUP BY ");
	    for(Iterator<Variable> v = boundvars.iterator(); v.hasNext();)
		sql.append(childtable + "." + v.next().name() + ",");
	    String firstvar = boundvars.first().name();
	    sql.append(childtable + ".value");
	    
	    if(isLeafTable(childtable)){
		 sql.append(") AS pos JOIN ");
	    
		/* compute tot counts for value */	    
		
		if(children_edgevars.elementAt(i).size() > 0){
		    /* cartesian product of edgevartypes */
		    sql.append("(SELECT count(*) as tot_count FROM ");
		    int j = 0;
		    for(Iterator<Variable> ev = children_edgevars.elementAt(i).iterator(); ev.hasNext();j++){
			String tname = ev.next().type(); 
			sql.append(tname + " as " + tname + j + ",");
		    }
		    /* remove final comma */
		    if(sql.charAt(sql.length()-1) == ',')
			sql.deleteCharAt(sql.length()-1);
		    sql.append(") AS tot)");
		}
		//		else /* recover tot counts from parent table */
		//   sql.append("(SELECT count(*) as tot_count FROM " + root_table_name + ") AS tot)");
		else /* if attribute node there are no false counts */
		    sql.append("(SELECT 1 as tot_count) AS tot)"); //DEBUG
	    }
	    else{
		sql.append(" WITH ROLLUP)");
	    }

	    sql.append(" AS " + counts_table + " ON ");
	    for(Iterator<Variable> v = boundvars.iterator(); v.hasNext();){
		String varname = v.next().name();
		sql.append(counts_table + "." + varname + "="
			     + root_table_name + "." + varname + " AND ");
	    }
	    sql.delete(sql.length()-5,sql.length()); // remove final AND
	}

	/* group by root bound variables */
	sql.append(" GROUP BY "); 
	for(Iterator<Variable> v = boundvars.iterator(); v.hasNext();)
	    sql.append(root_table_name + "." + v.next() + ","); 
	
	/* remove final comma */
	if(sql.charAt(sql.length()-1) == ',')
	    sql.deleteCharAt(sql.length()-1);

	/* execute sql statement */
	execute(sql.toString());
    }        

    private String collectTetValue(String table_name) throws Exception
    {
	/* call queryResult to turn ResultSet in String and check correctness */
	return getValue("SELECT value FROM " + table_name);
    } 

    private String literalFromQuery(Literal literal, int position) throws Exception
    {	
	/* if literal is not negated simply return relation name with relation+position as alias */
	if(!literal.negated)
	    return "`" + literal.rel.name() + "` AS `" + literal.rel.name() + position + "`";
	
	/* if relation has no argument return empty string */
	if(literal.rel.arity() == 0) 
	    return "";

	/* if literal is negated create query to select literal relation complement */
	StringBuffer query = new StringBuffer(MAXQUERYSIZE); 
	    
	query.append("(SELECT ");

	/* select literal relation arguments from tables corresponding to arguments types */
	for(int i = 0; i < literal.rel.arity(); i++)
	    /* collect argument from argtype table and rename it to match current position */
	    query.append("`" + literal.rel.argType(i).name() + i + "`." + colprefix + "1 AS " + colprefix + (i+1) + ",");
	
	// remove final comma
	query.deleteCharAt(query.length()-1);

	query.append(" FROM ");

	/* from statement including all argument type tables */ 
	for(int i = 0; i < literal.rel.arity(); i++)
	    query.append("`" + literal.rel.argType(i).name() + "` AS `" + literal.rel.argType(i).name() + i + "`,");

	// remove final comma
	query.deleteCharAt(query.length()-1);

	query.append(" WHERE (");
	
	/* group type arguments */
	for(int i = 0; i < literal.rel.arity(); i++)
	    query.append("`" + literal.rel.argType(i).name() + i + "`." + colprefix + "1,");
	
	// replace final comma with parenthesis
	query.setCharAt(query.length()-1,')');

	/* select whatever is not in literal relation table */
	query.append(" NOT IN (SELECT * FROM `" + literal.rel.name() + "`)");

	/* add alias to derived table */
	query.append(") AS `" + literal.rel.name() + position + "`");

	return query.toString();
    }

    public String typeValueQuery(Type type, 
				 TreeSet<Variable> boundvars, 
				 HashMap<String, Object> boundvalues, 
				 TreeSet<String> boundtables,
				 Boolean returnvalue) throws Exception
    {
	/* check that all free variables are instantiated */
	if(boundvars.size() != boundvalues.size())
	   throw new Exception("Size of boundvars differ from that of boundvalues: " 
			       + boundvars.size() + " != " + boundvalues.size());

	StringBuffer query = new StringBuffer(MAXQUERYSIZE); 
	    
	//DEBUG	query.append("SELECT ");
	query.append("SELECT DISTINCT ");

	/* recover type variables which are not bound */
	TreeSet<Variable> freetypevars = type.allVars();
	freetypevars.removeAll(boundvars);

	/* select free variables instantiated to boundvalues */  
	for(Iterator<Variable> v = boundvars.iterator(); v.hasNext();){
	    String varname = v.next().name();
	    Object val = boundvalues.get(varname);
	    if(val == null)
		throw new Exception("Free variable " + varname + " not instantiated");
	    query.append(makeName(val) + " AS " + varname + ","); 
	}

	/* recover literals divided by free type variable */
	LiteralByVariableVecMap freetypevarliterats = type.literalsByVariable(freetypevars, true);

	/* select freetype variables from Type */
	for(Iterator<Variable> v = freetypevars.iterator(); v.hasNext();){
	    String varname = v.next().name();
	    LiteralByVariableVec literalvec = freetypevarliterats.get(varname);
	    if(literalvec == null)
		throw new Exception("New type variable " + varname + " not found in LiteralByVariableVec");
	    LiteralByVariable literal = literalvec.firstElement();
	    String relname = literal.first();
	    String relpos = literal.second().first().toString();
	    String colpos = literal.second().second().toString();
	    query.append("`" + relname + relpos + "`." + colprefix + colpos + " AS " + varname + ",");
	} 

	if(returnvalue)
	    /* append value field (always TRUE for now) */
	    query.append("'T' as value");
	else
	    /* remove final comma */
	    query.deleteCharAt(query.length()-1);
	
	/* if Type has no literals simply return instantiation for whole TET free variables */
	if(type.literals.size() == 0)
	    return query.toString();
	
	/* from expression */
	query.append(" FROM ");

	/* recover tables from type literals */
	for(int i = 0; i < type.literals.size(); i++)
	    if(!type.literals.elementAt(i).rel.isOperator()) // skip operators like = <= etc
		query.append(literalFromQuery(type.literals.elementAt(i), i) + ",");

	/* recover boundtables */
	if(boundtables != null)
	    for(Iterator<String> t = boundtables.iterator(); t.hasNext();)
		query.append(t.next() + ",");

	if(query.charAt(query.length()-1) == ',') // remove final comma
	    query.deleteCharAt(query.length()-1);
	
	/* where expression */
	StringBuffer where = new StringBuffer(MAXQUERYSIZE); 
	where.append(" WHERE ");
	/* binding free variables */
	/* recover literals divided by free variable */
	LiteralByVariableVecMap freevarliterats = type.literalsByVariable(boundvars, true);
	for(Iterator<Variable> v = boundvars.iterator(); v.hasNext();){
	    String varname = v.next().name();
	    Object val = boundvalues.get(varname);
	    if(val == null)
		throw new Exception("Free variable " + varname + " not instantiated");
	    String valname = makeName(val);
	    LiteralByVariableVec freevarliteralsvec = freevarliterats.get(varname);
	    if(freevarliteralsvec != null)
		for(int j = 0; j < freevarliteralsvec.size(); j++){
		    LiteralByVariable literal = freevarliteralsvec.elementAt(j);
		    String relname = literal.first();
		    String relpos = literal.second().first().toString();
		    String colpos = literal.second().second().toString();		    
		    where.append("`" + relname + relpos + "`." + colprefix 
				 + colpos + "=" + valname + " AND ");
		}
	}

	/* assuring that freetypevars satisfy the Type */
	for(Iterator<Variable> v = freetypevars.iterator(); v.hasNext();){
	    String varname = v.next().name();
	    LiteralByVariableVec varliterals = freetypevarliterats.get(varname);
	    if(varliterals == null)
		throw new Exception("New type variable " + varname + " not found in LiteralByVariableVec");
	    String firstrelname = "`" + varliterals.firstElement().first() + varliterals.firstElement().second().first() + "`";
	    String firstcolname = colprefix + varliterals.firstElement().second().second();
	    for(int j = 1; j < varliterals.size(); j++){
		String secondrelname = "`" + varliterals.elementAt(j).first() + varliterals.elementAt(j).second().first() + "`";
		String secondcolname = colprefix + varliterals.elementAt(j).second().second();
		where.append(firstrelname + "." + firstcolname + "=" + secondrelname + "." + secondcolname + " AND ");
	    }
	}

	/* assure that type satisfies operators conditions */
	for(int i = 0; i < type.literals.size(); i++){
	    Literal literal = type.literals.elementAt(i);
	    if(!literal.rel.isOperator()) // skip non-operators
		continue;	    
	    if(literal.rel.arity()!= 2) // only binary operators implemented 
		throw new Exception("Only binary operators implemented, found " + literal.rel + "/" + literal.rel.arity());
	    try{
		where.append(bindArgument(literal.arguments[0], boundvalues, freetypevarliterats) +
			     Operator(literal.rel.name()) +
			     bindArgument(literal.arguments[1], boundvalues, freetypevarliterats) + " AND "); 
	    }catch(Exception e){
		// should never happen
		e.printStackTrace();
		System.exit(1);
	    }
	}

	if(where.substring(where.length()-5).equals(" AND ")) // remove final AND
	    where.delete(where.length()-5,where.length());	

	/* add where expression if it contains anything */
	if(where.length() > 7)
	    query.append(where);

	return query.toString();	
    }

    private String bindArgument(Object argument, 
				HashMap<String, Object> boundvalues, 
				LiteralByVariableVecMap freetypevarliterats) throws Exception
    {
	/* if argument bound to RelObject simply return its value */
	if(argument instanceof RelObject)
	    return ((RelObject)argument).name();

	/* otherwise cast argument to Variable and recover name */
	String varname = ((Variable)argument).name();

	/* if argument is in boundvalues return its value from there */
	Object obj = boundvalues.get(varname);
	if(obj != null)
	    if(obj instanceof Variable)
		return ((Variable)obj).name();
	    else
		return ((RelObject)obj).name();
	
	/* otherwise get first literal containing the argument and return its binding */
	LiteralByVariableVec varliterals = freetypevarliterats.get(varname);
	if(varliterals == null)
	    throw new Exception("Argument is variable and is nor bound neither appears in any literal");	
	String firstrelname = "`" + varliterals.firstElement().first() + varliterals.firstElement().second().first() + "`";
	String firstcolname = colprefix + varliterals.firstElement().second().second();
	
	return firstrelname + "." + firstcolname;
    }

    private String Operator(String operatorname)
    {
	return operatorname;
    }

    private String makeName(Object obj) throws Exception
    {
	if(obj instanceof Variable)
	    return ((Variable)obj).name();
	else if (obj instanceof RelObject) /* protect value with ' ' */
	    return "'" + ((RelObject)obj).name() + "'";
	
	throw new Exception("Object obj should be either Variable or RelObject");
    }

    private Boolean isLeafTable(String table)
    {
	return table.contains("_TD_");
    }
}

