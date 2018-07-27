package tet;

import java.util.*;
import mymath.*;
import java.sql.*;
import java.io.*;

public class MySQLTestViewer{

    /* relational structure representing the current database */
    MySQLRelStructure rstruct;
    
    /* src database from which the test view can be collected */
    String srcdb;

    /* relations which should be updated with test view */
    Vector<Relation> relations;

    public MySQLTestViewer(MySQLRelStructure rstruct,
			   String srcdb,
			   String configfile) throws Exception
    {
	this.rstruct = rstruct;
	this.srcdb = srcdb;

	relations = new  Vector<Relation>();

	FileReader in = new FileReader(configfile);
	BufferedReader inbuf = new BufferedReader(in);	
	init(inbuf);
	inbuf.close();
	in.close();
    }
        
    public void addTestView(HashMap<String, Object> boundvalues)
    {
	/* recover bound types */
	TreeSet<VariableType> boundtypes = getBoundtypes(boundvalues.keySet());

	for(int i = 0; i < relations.size(); i++)
	    addTestView(relations.elementAt(i), boundvalues, boundtypes);
    }

    public void removeTestView(HashMap<String, Object> boundvalues)
    {
	for(int i = 0; i < relations.size(); i++)
	    removeTestView(relations.elementAt(i), boundvalues);	
    }


    /*****************************************************************************************************************************/
    /********************************************* PRIVATE METHODS ***************************************************************/

    private void init(BufferedReader inbuf) throws Exception
    {	
	String buf;

	while((buf = inbuf.readLine()) != null){
	    /* skip empty lines */
	    if(buf.length() == 0)
		continue;
	    /* skip comments */
	    if(buf.charAt(0) == '%')
		continue;
	    /* parse current relation and add to vector */	    
	    relations.add(new Relation(buf));
	}       
    }

    private void addTestView(Relation rel, HashMap<String, Object> boundvalues, TreeSet<VariableType> boundtypes)
    {

	/* recover srcrel name in srcdb */
	String srcrel = srcdb + ".`" + rel.name() + "`";
	
	/* build where expression */
	String where_expression = buildWhereExpression(rel, srcrel, boundvalues);
	
	/* if where expression is empty no bound variable matches, skip this relation */
	if(where_expression.length() == 0)	    
	    return;

	/* insert into table rel.name() selecting from the corresponding table in srcdb database based on the where expression */
	String sql = "INSERT INTO `" + rel.name() + "` SELECT " + srcrel + ".* FROM " + srcrel;
	
	/* collect tables and constraints for  variables with same type as boundvariables */
	String typetables = new String();
	String typeconstraints = new String();
	for(int i = 0; i < rel.arity(); i++){

	    /* recover argument type */
	    VariableType type = null;
	    try{
		type = rel.argType(i);
	    } catch(Exception e){ 
		/* exception should never happen, exit */
		e.printStackTrace();
		System.exit(1);
	    }

	    /* recover argument type name */
	    String typename = type.name();

	    /* check whether current argument is bound */
	    if(boundvalues.get(typename) != null)
		continue;

	    /* check whether current argument is of same type as one of the bound variables */
	    if(boundtypes.contains(type)){

		/* add argument type table */
		typetables += ",`" + typename + "` AS `" + typename + i + "`";

		/* add argument type constraint */
		typeconstraints += " AND " + srcrel + ".arg" + (i+1) + "=`" + typename + i + "`.arg1";
	    }
	}
	
	/* merge conditions */
	sql += typetables + " WHERE " + where_expression + typeconstraints;

	/* execute sql */
	rstruct.execute(sql);
    }

    private void removeTestView(Relation rel, HashMap<String, Object> boundvalues)
    {
	String relname = "`" + rel.name() + "`";
	String where_expression = buildWhereExpression(rel, relname, boundvalues);
	
	/* if where expression is empty no bound variable matches, skip this relation */
	if(where_expression.length() == 0)	    
	    return;
	
	/* remove from table rel.name() records matching where expression */
	String sql = "DELETE FROM " + relname + " WHERE " + where_expression;

	/* execute sql */
	rstruct.execute(sql);
    }

    private String buildWhereExpression(Relation rel, String relname, HashMap<String, Object> boundvalues)
    {
	/* build WHERE expression from relation arguments matching boundvalues */

	StringBuffer wherebuffer = new StringBuffer();

	try{
	    /* iterate over relation arguments */
	    for(int i = 0; i < rel.arity(); i++){

		/* check if relation argument is one of the bound variables */
		Object obj = boundvalues.get(rel.argType(i).name());

		/* if not continue to next argument */
		if(obj == null)
		    continue;

		/* add equality condition for current argument */
		wherebuffer.append(relname + ".arg" + (i+1) + "=" + obj + ","); 		
	    }

	    /* remove final comma if needed */
	    if(wherebuffer.length() > 0)	    
		wherebuffer.setLength(wherebuffer.length()-1);	    

	}catch(Exception e){ 
            /* exception should never happen, exit */
            e.printStackTrace();
            System.exit(1);
        }
	
	return wherebuffer.toString();
    }

    /** recover types of bound variables 
     *  assuming the type is the string prefix
     *  before any digit */
    private TreeSet<VariableType> getBoundtypes(Set<String> boundvars)
    {
	TreeSet<VariableType> boundtypes = new TreeSet<VariableType>();

	for(Iterator<String> i = boundvars.iterator(); i.hasNext();)
	    boundtypes.add(getBoundtype(i.next()));
	
	return boundtypes;
    }

    /** recover type of bound variable, 
     *  assuming the type is the string prefix
     *  before any digit */
    private VariableType getBoundtype(String boundvar)
    {
	return new VariableType(TetUtilities.trimUpToDigit(boundvar));
    }

}
