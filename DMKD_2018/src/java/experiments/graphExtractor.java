package experiments;

import java.util.*;
import java.sql.*;
import java.io.*;
import tet.*;
import tet.rnn.*;
import myio.*;

public class graphExtractor
{	
	protected MySQLRelStructure relstruct;

	private int graph_count = 0;
	private int node_count = 0;
	private int edge_count = 0;

	private HashMap<String,Integer> string2int = new HashMap<String,Integer>();

	private boolean verbose_ids = false;

	public graphExtractor(String database, String login, String password, boolean verbose_ids)
             throws Exception
	{		
	 	/* create mysql relational structure which uses TD BU procedure */
      	this.relstruct = new MySQLRelStructure(database, login, password, true);

      	this.verbose_ids = verbose_ids;
	}	

	public void dispose()
	         throws Exception
	{
		relstruct.Close();

	}

	public Vector<HashMap<String, Object>> readData(String datafile)
             throws Exception
	{
		Vector<HashMap<String, Object>> data = new Vector<HashMap<String, Object>>();

	  	FileReader in = new FileReader(datafile);
	    StreamTokenizer st = new StreamTokenizer(in);
	    st.eolIsSignificant(true);
	    st.whitespaceChars(32,47);
	    st.whitespaceChars(58,63);

	    System.out.println("=== Reading data file <" + datafile + "> === ");
	    
	    while(st.nextToken() != StreamTokenizer.TT_EOF)
	    {
	      HashMap<String, Object> objmap = new HashMap<String, Object>();
	      int id = -1;

	      //System.out.println("Processing example: ");
	      String example = "";
	      do
	      {
	        String var = st.sval;
	        st.nextToken();
	        String val = st.sval;
	        if(st.ttype == st.TT_NUMBER)
	        {
	          val = String.valueOf((int)st.nval);
	          id = (int)st.nval;
	        }
	        objmap.put(var,new RelObject(val));
	        example += var + "=" + val + " ";
	      }while(st.nextToken() != StreamTokenizer.TT_EOL);

	      data.add(objmap);

	    }
	
		in.close();

    	return data;
	}

	public void extractTetGraphs(String tetfile, String datafile, String dstdir)
		throws Exception
	{
		/* load discriminant tet from file */
	    String tetstring = Tet.tetString(tetfile);

    	rnn_Tet tet = new rnn_Tet(tetstring);

	    System.out.println("Read discriminant Tet string: " + tet.Serialize());
	    System.out.println("Tet Freevars = " + tet.freevars().toString());

	    reset_graph();

		Vector<HashMap<String, Object>> data = readData(datafile);				

		for(int i = 0; i < data.size(); i++)
		{
			HashMap<String, Object> objmap = data.elementAt(i);					
			
			String dstfile = graphId(objmap) + ".graphml";
			FileWriter out = new FileWriter(dstdir + "/" + dstfile);
			out.write(header());
			extractTetGraph(tet, objmap, out);
			out.write(footer());
			out.close();	
		}		

		System.out.println("Type to id mapping");
		System.out.println(mapToString());

	}

	protected void extractTetGraph(rnn_Tet tet, HashMap<String, Object> boundvalues,  FileWriter out)
		throws Exception
	{
		if (boundvalues.size() > 1)
			throw new Exception("extractTetGraph for multivariate TETs not implemented yet!");		

		out.write("<graph id=\"" + graphId(boundvalues) + "\" edgedefault=\"directed\">\n");
		
		reset_node();
		reset_edge();

		extractTetGraph(tet, boundvalues, out, nodeId(boundvalues));
	
		out.write("</graph>\n");
	}


    public void extractTetGraph(rnn_Tet tet, HashMap<String, Object> boundvalues, FileWriter out, String srcnode) throws Exception
    {   
    	out.write("<node id=\"" + srcnode + "\">\n");        

        if(!tet.getRootType().isEmptyType()){

            /* execute root type query */
            Vector<HashMap<String, Object>> res = 
                relstruct.queryResult(relstruct.typeValueQuery(tet.getRootType(),
                                                         TetUtilities.strings2Variables(boundvalues.keySet()),
                                                         boundvalues, null, false));

            /* check if boundvalues satisfy the root type */
            if(res.size() == 0)
            	throw new Exception("Trying to compute Tet Graph with bound values not satisfying root type");                
            
            /* check that all root type variables are bound */
            if(res.size() > 1)
                throw new Exception("Trying to compute Tet Graph without binding all free variables");
          
            out.write("<data key=\"nt\">" + type2integer(tet.getRootType()) + "</data>\n");

        }
        else
        {
            out.write("<data key=\"nt\">0</data>\n");
        }

        out.write("</node>\n");       
         
        /* base step */
        if (tet.getNumChildren() == 0)
        	return;

        /* collect freevars to be bound */
        TreeSet<Variable> boundvars = TetUtilities.strings2Variables(boundvalues.keySet());
        
        /* iterate over getRootType() children */
        for(int i = 0; i < tet.getNumChildren(); i++)
        {           
            /* get child tet */
            rnn_Tet dtetchild = (rnn_Tet)tet.getChild(i).getSubTree();

            /* execute type query */
            Vector<HashMap<String, Object>> res = relstruct.queryResult(relstruct.typeValueQuery(dtetchild.getRootType(), 
                                                                                           boundvars, boundvalues, null, false));           
            /* iterate over query results */
            for(int j = 0; j < res.size(); j++)
            {
            	String dstnode = nodeId(res.elementAt(j));
            	out.write(String.format("<edge id=\"e%d\" source=\"%s\" target=\"%s\">\n", next_edge(), srcnode, dstnode));
				//out.write("<data key=\"nt\">" + type2id(dtetchild.getRootType()) + "</data>\n");
        		out.write("</edge>\n");    

                /* recursively process tet passing new boundvalues map */ 
                extractTetGraph(dtetchild, res.elementAt(j), out, dstnode);                
            }               
        }
    }

	public String mapToString()
	{
		String buf = "";

		for (Map.Entry<String, Integer> entry : string2int.entrySet()) 
		{
    		buf += entry.getKey() + "\t" + entry.getValue() + "\n";
    	}	

    	return buf;
	}

	protected String header()
	{
		return       "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               		 "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"\n" +  
               		 "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
               		 "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns\n" + 
               		 "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">\n" +
               		 "<key id=\"nt\" for=\"node\" attr.name=\"node_type\" attr.type=\"int\">\n" +               		 
				     //"<default>root</default>\n" +  					
  					 "</key>\n";
	}

	protected String footer()
	{
		return "</graphml>\n";
	}

	protected String graphId(HashMap<String, Object> example)
	{
		if (verbose_ids)
			return "g " + example2Id(example);
		return "g" + next_graph();
	}

	protected String nodeId(HashMap<String, Object> example)
	{
		if (verbose_ids)
			return "n " + example2Id(example);
		return "n" + next_node();
	}

	protected String example2Id(HashMap<String, Object> example)
	{
		String id = "";

		Iterator it = example.entrySet().iterator();
	    while (it.hasNext()) {
	    	if (id != "")
	    		id += " ";
        	Map.Entry pair = (Map.Entry)it.next();
        	id += pair.getKey() + " = " + pair.getValue();        	
        }

       	return id;
	}

	protected String type2id(Type type) throws Exception
	{
		Vector<Literal> lits =	type.getLiterals();

		if (lits.size() == 0)
			return "empty";
		if (lits.size() > 1)
			throw new Exception("extractTetGraph for Types with more than one literal not implemented yet!");

		Literal lit = lits.firstElement();
		if (lit.isNegated())
			return "NOT_" + lit.getRelation().toString();
		else
			return lit.getRelation().toString();		
	}

	protected Integer type2integer(Type type) throws Exception
	{
		String id = type2id(type);

		if (!string2int.containsKey(id))
			string2int.put(id, string2int.size()+1);
		
		return string2int.get(id);
	}

	private int next_graph()
	{	
		return graph_count++;
	}

	private int next_node()
	{	
		return node_count++;
	}

	private int next_edge()
	{	
		return edge_count++;
	}

	private void reset_node()
	{
		node_count = 0;
	}

	private void reset_edge()
	{
		edge_count = 0;
	}

	private void reset_graph()
	{
		graph_count = 0;
	}

	public static void main(String[] args)
  	{
    	graphExtractorCommandLineOptions options = new graphExtractorCommandLineOptions(args);

    	try
    	{
    		graphExtractor extractor = new graphExtractor(options.database, options.login, options.password, options.verbose_ids);
    			
     		extractor.extractTetGraphs(options.tetfile, options.datafile, options.dstdir);

      		extractor.dispose();
    	}
    	catch (Exception e) {
      		e.printStackTrace();
    	}
    	
  	}
}


class	graphExtractorCommandLineOptions {


  public String database;
  public String login;
  public String password;
  public String tetfile;
  public String datafile;
  public String dstdir;  

  public boolean verbose_ids = false; 

  public graphExtractorCommandLineOptions(String[] args)
  {
    int pos = parseOptions(args);

    // chek if compulsory options specified
    if(args.length - pos < 5){
      System.out.println("Missing compulsory option(s):\n");
      printHelp();
      System.exit(1);
    }

    database = args[pos++];
    login = args[pos++];
    password = args[pos++];
    tetfile = args[pos++];
    datafile = args[pos++];
    dstdir = args[pos++];    
  }

  public int parseOptions(String[] options)
  {
    int pos = 0;

    while(pos < options.length && options[pos].charAt(0) == '-'){
      switch(options[pos].charAt(1)){       
      	case 'v':
      		verbose_ids = true;
                break;
        case 'h':
        default:
            printHelp();
            System.exit(1);
      }
      pos++;
    }

    return pos;
  }

  public void printHelp()
  {
    System.out.println("Usage:\n\tgraphExtractor [options] <database> <login> <password> <tetfile> <datafile> <dstdir>");
    System.out.println("Options:");
    System.out.println("\t-v         \tuse vebose ids");
    System.out.println("\t-h         \tprint help");
    System.exit(1);
  }

  public String toString()
  {
    return "database=" + database + "\n"
    + "login=" + login + "\n"
    + "password=" + password + "\n"
    + "tetfile=" + tetfile + "\n"
    + "datafile=" + datafile + "\n"
    + "dstdir=" + dstdir + "\n"
    + "vebose_ids" + "\n";
  }
}
