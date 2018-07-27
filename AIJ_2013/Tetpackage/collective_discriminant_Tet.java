package Tetpackage;

import java.util.*;
import mymath.*;
import java.sql.*;
import java.io.*;
import Tetpackage.learner.*;

public class collective_discriminant_Tet{


    public static TreeMap<String,discriminant_Tet> loadTets(String dirname) throws Exception
    {
	if(dirname == null)
	    return null;
	
	File tetdir = new File(dirname);
	
	/* check that file is a directory */
	if(!tetdir.isDirectory())
	    throw new Exception("Directory expected with -t option");
	
	/* recover tet file names */
	String[] tetfiles = tetdir.list();
	
	/* init tet map */
	TreeMap<String,discriminant_Tet> tets = new TreeMap<String,discriminant_Tet>(); 
	
	for(int i = 0; i < tetfiles.length; i++)
	    tets.put(tetfiles[i], new discriminant_Tet(new FileReader(dirname + "/" + tetfiles[i])));
	
	return tets;
    }

    public static TreeMap<String,MySQLDataset> loadDatasets(MySQLRelStructure rstruct,
							    String objdirname, 
							    TreeMap<String,Vector<Variable>> variables,
							    StringCounter table_counter) throws Exception
    {
	File objdir = new File(objdirname);
	
	/* check that file is a directory */
	if(!objdir.isDirectory())
	    throw new Exception("Directory expected for loading datasets");
	
	/* recover tet file names */
	String[] objfiles = objdir.list();

	/* init dataset map */
	TreeMap<String,MySQLDataset> datasets = new TreeMap<String,MySQLDataset>();

	for(int i = 0; i < objfiles.length; i++){

	    /* recover root variables */
	    Vector<Variable> vars = variables.get(objfiles[i]);
	    if(vars == null)
		throw new Exception("Could not find root variables for task " + objfiles[i]);
 
	    /* recover obj dataset */
	    datasets.put(objfiles[i], new MySQLDataset(rstruct, table_counter.next(), vars, objdirname + "/" + objfiles[i]));
	}
    
	return datasets;
    }    

    public static TreeMap<String,Vector<HashMap<String, Object>>> 
	loadVar2ObjMaps(boolean compute_tet_value, 
			String objdirname, 
			TreeMap<String,Vector<Variable>> variables) throws Exception
    {
	if(!compute_tet_value)
	    return null;

	File objdir = new File(objdirname);
	
	/* check that file is a directory */
	if(!objdir.isDirectory())
	    throw new Exception("Directory expected for loading objects");
	
	/* recover tet file names */
	String[] objfiles = objdir.list();

	/* init variable2object map */
	TreeMap<String,Vector<HashMap<String, Object>>> var2objmaps = new TreeMap<String,Vector<HashMap<String, Object>>>();

	for(int i = 0; i < objfiles.length; i++){
	    
	    /* recover root variables */
	    Vector<Variable> vars = variables.get(objfiles[i]);
	    if(vars == null)
		throw new Exception("Could not find root variables for task " + objfiles[i]);
 
	    /* recover variable2object map */
	    var2objmaps.put(objfiles[i], loadVar2ObjMaps(vars, objdirname + "/" + objfiles[i]));
	}
    	
	return var2objmaps;
    }
    
    public static Vector<HashMap<String, Object>> loadVar2ObjMaps(Vector<Variable> vars, String filename) throws Exception
    {
	BufferedReader reader = new BufferedReader(new FileReader(filename));
	String line;
	Vector<HashMap<String, Object>> var2objmaps = new Vector<HashMap<String, Object>>();
	
	while((line = reader.readLine()) != null){

	    StringTokenizer tokenizer = new StringTokenizer(line);

	    HashMap<String, Object> var2objmap  = new HashMap<String, Object>();

	    for(int i = 0; i < vars.size(); i++){
		if(!tokenizer.hasMoreTokens())
		    throw new Exception("Missing objvalue for variable " + vars.elementAt(i));
		var2objmap.put(vars.elementAt(i).name(), new RelObject(tokenizer.nextToken()));
	    }
	    
	    var2objmaps.add(var2objmap);
	}
	
	return var2objmaps;
    }

    public static TreeMap<String,Vector<Variable>> loadVariables(String vardirname) throws Exception
    {
	File vardir = new File(vardirname);
	
	/* check that file is a directory */
	if(!vardir.isDirectory())
	    throw new Exception("Directory expected for loading variables");
	
	/* recover tet file names */
	String[] varfiles = vardir.list();

	/* init dataset map */
	TreeMap<String,Vector<Variable>> variables = new TreeMap<String,Vector<Variable>>();

	for(int i = 0; i < varfiles.length; i++)
	    variables.put(varfiles[i], loadVariableVector(vardirname + "/" + varfiles[i]));

	return variables;
    }
    
    public static Vector<Variable> loadVariableVector(String filename) throws Exception
    {       
	Vector<Variable> vars = new Vector<Variable>();
	
	String varline = (new BufferedReader(new FileReader(filename))).readLine();
	
	StringTokenizer tokenizer = new StringTokenizer(varline);
	
	while(tokenizer.hasMoreTokens())
	    vars.add(new Variable(tokenizer.nextToken()));
	
	return vars;
    }

    public static void initPredictions(String task, MySQLDataset dataset, Vector<Variable> variables)
    {
	dataset.createPositiveExampleTable(task, variables);
    }

    public static int updatePredictions(String task, 
					MySQLDataset dataset, 
					Vector<Variable> variables, 				       
					discriminant_Tet tet,
					StringCounter table_counter,
					boolean hasprevious,
					int iteration) throws Exception
    {
	MySQLDataset values_dataset = (MySQLDataset)tet.compute_d_pairs(new TreeSet(variables), table_counter, dataset);

	return values_dataset.createPositiveExampleTable(task, variables, tet.getThreshold(), hasprevious, iteration);
    }

    public static void main(String[] args)
    {
	
	collective_discriminant_TetCommandLineOptions options = new collective_discriminant_TetCommandLineOptions(args);
	
	try{
	    /* create mysql relational structure which uses TD BU procedure */
	    MySQLRelStructure relstruct = new MySQLRelStructure(options.database, options.login, options.password, true);	 
	    
	    /* load bootstrap tets if available */ 
	    TreeMap<String,discriminant_Tet> init_tets = loadTets(options.inittetdir);

	    /* load collective tets */ 
	    TreeMap<String,discriminant_Tet> collective_tets = loadTets(options.tetdir);

	    /* load root variables */
	    TreeMap<String,Vector<Variable>> variables = loadVariables(options.vardir);

	    /* init table counter */
	    StringCounter table_counter = new StringCounter();
	    
	    /* load object datasets */
	    TreeMap<String,MySQLDataset> datasets = loadDatasets(relstruct, options.objdir, variables, table_counter);

	    /* create variable2object map if needed */
	    TreeMap<String,Vector<HashMap<String, Object>>> var2objmaps = loadVar2ObjMaps(options.compute_tet_value, 
											  options.objdir, variables);		
	    /* recover set of tasks */
	    Set<String> tasks = datasets.keySet();
	    
	    /* bootstrap predictions */
	    for(Iterator<String> s = tasks.iterator(); s.hasNext();){
		String task = s.next();		
		discriminant_Tet init_tet = init_tets.get(task);
		if(init_tet != null)
		    /* use predictions provided by init tets */
		    updatePredictions(task, datasets.get(task), variables.get(task), init_tet, table_counter, false, 0);
		else
		    /* use predictions read from obj files */
		    initPredictions(task, datasets.get(task), variables.get(task));
	    }

	    /* iterate collective classification */
	    System.out.println("Iterating collective classification, printing:");
	    System.out.println("<iteration>\t[<taskdiff1>..<taskdiffN]\t<totdiff>");
	    int it = 0;
	    int totdiff = 0;
	    do{
		totdiff = 0;
		System.out.print(it);
		for(Iterator<String> s = tasks.iterator(); s.hasNext();){
		    String task = s.next();
		    discriminant_Tet collective_tet = collective_tets.get(task);
		    /* task collective classification */
		    int taskdiff = updatePredictions(task, datasets.get(task), variables.get(task), 
						     collective_tet, table_counter, true, it);
		    System.out.print("\t" + taskdiff); 
		    totdiff += taskdiff;
		}
		System.out.println("\t" + totdiff); 
		it++;
	    }while(totdiff > 0 && (options.max_iterations == 0 || it < options.max_iterations));

	    /* print out results */
	    for(Iterator<String> s = tasks.iterator(); s.hasNext();){
		
		String task = s.next();
		
		/* open output file */
		FileWriter out = new FileWriter(options.outdir + "/" + task);
		
		discriminant_Tet collective_tet = collective_tets.get(task);

		/* compute ds */
		Float[] ds = collective_tet.compute_ds(new TreeSet<Variable>(variables.get(task)), table_counter, datasets.get(task));
		
		/* compute decisions */
		int[] decisions = collective_tet.compute_decisions(ds);
		
		/* write decisions + ds + (possibly) tet value */
		for(int i = 0; i < ds.length; i++){
		    out.write(decisions[i] + "\t" + ds[i]);
		    if(options.compute_tet_value)
			out.write("\t" + collective_tet.calculateValue(relstruct, var2objmaps.get(task).elementAt(i), true));
		    out.write("\n");
		}
		
		/* close output file */
		out.close();
	    }

	    /* close relational structure */
	    relstruct.Close();
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }

}

class collective_discriminant_TetCommandLineOptions {


    public String database;
    public String login; 
    public String password; 
    public String tetdir; 
    public String objdir; 
    public String vardir; 
    public String outdir; 
	
    public String inittetdir = null; 
    public boolean compute_tet_value = false;
    public int max_iterations = 0;

    public collective_discriminant_TetCommandLineOptions(String[] args)
    {
	int pos = parseOptions(args);

	// chek if compulsory options specified
	if(args.length - pos < 7){ 
	    System.out.println("Missing compulsory option(s):\n");
	    printHelp();
	    System.exit(1);
	}

	database = args[pos++];
	login = args[pos++];
	password = args[pos++];
	tetdir = args[pos++];
	objdir = System.getenv("PWD") + "/" + args[pos++]; 
	vardir = args[pos++];
	outdir = args[pos++];
    }
    
    public int parseOptions(String[] options)
    {       	
	int pos = 0;

	while(pos < options.length && options[pos].charAt(0) == '-'){
	    switch(options[pos].charAt(1)){
	    case 't':
		inittetdir = options[++pos];
		break;
	    case 'v':
		compute_tet_value = true;
		break;		
	    case 'm':
		max_iterations = Integer.parseInt(options[++pos]);
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
	System.out.println("Usage:\n\tTetpackage.collective_discriminant_Tet [options] <database> <login> <password> ");
	System.out.println("\t                                                 <tetdir> <objdir> <vardir> <outdir>");
	System.out.println("Options:");
	System.out.println("\t-t <string>\tdir with tets for initializing collective classification (default=null)");
	System.out.println("\t-m <int>\tmaximum number of iterations (default=0 -> no limit)");
	System.out.println("\t-v         \tcompute tet value (default=false)");
	System.out.println("\t-h         \tprint help");
	System.exit(1);
    }

    public String toString()
    {
	return "database=" + database + "\n" 	    	    
	    + "login=" + login + "\n" 
	    + "password=" + password + "\n"	    
	    + "tetdir=" + tetdir + "\n" 
	    + "objdir=" + objdir + "\n" 
	    + "vardir=" + vardir + "\n" 
	    + "outdir=" + outdir + "\n" 
	    + "inittetdir=" + inittetdir + "\n" 
	    + "max_iterations=" + max_iterations + "\n" 
	    + "compute_tet_value=" + compute_tet_value + "\n"; 
    }
}
