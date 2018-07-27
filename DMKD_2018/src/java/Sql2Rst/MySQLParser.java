
import java.util.*;
import java.io.*;
import java.sql.*;

public class MySQLParser{

    Connection connection;
    Statement statement;
    String database;
    String login; 
    String password;    

    // database example is localhost:3306/test
    public MySQLParser(String database_, String login_, String password_) throws 
	java.lang.ClassNotFoundException,java.sql.SQLException
    {
	database = database_;
	login = login_;
	password = password_;

	Connect();
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
    }

    public void Close() throws java.sql.SQLException
    {
	statement.execute("FLUSH TABLES");
	statement.close();
	connection.close();
    }
    
    public synchronized void execute(String sql) 
    {
	try{
	    statement.execute(sql);
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

    public static Vector<Pair<String,Vector<String>>> readConfigfile(String configfile) 
	throws java.io.IOException
    {
	FileReader in = new FileReader(configfile);
	BufferedReader inbuf = new BufferedReader(in);	
	String buf;
	int beg,end;
	Vector<Pair<String,Vector<String>>> tables = new Vector<Pair<String,Vector<String>>>();

	while((buf = inbuf.readLine()) != null){
	     /* skip empty lines */
            if(buf.length() == 0)
                continue;
            /* skip comments */
            if(buf.charAt(0) == '%')
                continue;
	    beg = buf.indexOf('(');
	    if(beg == -1)
		continue;
	    String tablename = buf.substring(0,beg);
	    Vector<String> arguments = new Vector<String>();
	    while ((end = buf.indexOf(',',beg+1)) != -1){
		arguments.add(buf.substring(beg+1,end));
		beg=end;
	    }
	    end = buf.indexOf(')');
	    if(end == -1)
		continue;
	    arguments.add(buf.substring(beg+1,end));
	    tables.add(new Pair(tablename,arguments));
	}

	return tables;
    }
    
    public static Vector<String> getDomainTables(Vector<Pair<String,Vector<String>>> tables)
    {
	Vector<String> domains = new Vector<String>();

	for(int i = 0; i < tables.size(); i++){
	    Pair<String,Vector<String>> table = tables.elementAt(i);
	    if (table.second().size() > 1)
		continue;
	    if (table.first().equals(table.second().firstElement()))
		domains.add(table.first());
	}
	return domains;
    }

    public HashMap<String,Integer> getDomainValues(Vector<String> domain_tables)
    {
	HashMap<String,Integer> domains = new HashMap<String,Integer>();
	int nvals=0;

	for(int i = 0; i < domain_tables.size(); i++){	
	    String table = domain_tables.elementAt(i);
	    try{
	    	ResultSet rs = executeQuery("SELECT * FROM " + table);
	    	while(rs.next()){
		    String key = table + "_" + rs.getString(1);
		    domains.put(key, nvals);
		    nvals+=1;
	    	} 
	    } catch(Exception e){
		e.printStackTrace();
		System.exit(1);
	    }	    
	}
	return domains;
    }

    public String domainString(HashMap<String,Integer> domain_values)
    {
	StringBuffer buf = new StringBuffer("DOMAIN: {");
	Vector<Integer> sorted = new Vector<Integer>(domain_values.values());
	Collections.sort(sorted);
	for (Iterator<Integer> iterator = sorted.iterator(); iterator.hasNext();){ 
	    buf.append(iterator.next() + ","); 
	}
	buf.setCharAt(buf.length()-1, '}');
	buf.append(";");      
	return buf.toString();
    }

    public String relationString(Pair<String,Vector<String>> relation, HashMap<String,Integer> domain_values)
    {
	String relname = relation.first();
	Vector<String> relargs = relation.second();
	StringBuffer buf = new StringBuffer("RELATION: " + relname + "/" + relargs.size() + " {");
	try{
	    ResultSet rs = executeQuery("SELECT * FROM " + relname);
	    while(rs.next()){
		buf.append("(");
		for(int i = 0; i < relargs.size(); i++){
		    String argname=relargs.elementAt(i);
		    buf.append(domain_values.get(argname + "_" + rs.getString(i+1)) + ",");
		} 
		if (relargs.size() > 1)
		    buf.setCharAt(buf.length()-1, ')');
		else
		    buf.setCharAt(buf.length()-1, ')');
	    }
	} catch(Exception e){
	    e.printStackTrace();
	    System.exit(1);
	}	
	buf.append("};");
	return buf.toString();
    } 

    public void parse(String configfile) throws java.io.IOException
    {
	Vector<Pair<String,Vector<String>>> tables = readConfigfile(configfile);
	HashMap<String,Integer> domain_values = getDomainValues(getDomainTables(tables));	
	System.out.println(domainString(domain_values));
	for (int i = 0; i < tables.size(); i++){
	    System.out.println(relationString(tables.elementAt(i), domain_values));
	}
    }

    public static void main(String[] args)
    {
	try{
	    MySQLParser parser = new MySQLParser(args[0],args[1],args[2]);
	    parser.parse(args[3]);
	}
	catch (Exception e){
	    e.printStackTrace();
	    System.exit(1);
	}
    }
}

