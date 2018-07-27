package experiments;

import java.util.*;
import java.sql.*;
import java.io.*;
import tet.*;
import tet.rnn.*;
import myio.*;
import FastEMD.*;
 




public class BiblioRegression{

    public static TreeMap<Integer,Double> buildIndxValueMap(String idfile, String valfile, int numlines){
	/* 
	 * Lines in idfile are of the form <string><integer>.
	 * Lines in valfile are of the form <double>
	 * creates a mapping from the integer ids in idfile to double values from corresponding
	 * lines in valfile for the first numlines lines of the two files (both must have at least
	 * numlines lines)
	 */

	TreeMap<Integer,Double> result = new TreeMap<Integer,Double>();
	try{
	    FileReader inid = new FileReader(idfile);
	    StreamTokenizer stid = new StreamTokenizer(inid);
	    stid.eolIsSignificant(true);
	    stid.whitespaceChars(32,47);
	    stid.whitespaceChars(58,63);

	    FileReader inval = new FileReader(valfile);
	    StreamTokenizer stval = new StreamTokenizer(inval);
	    stval.eolIsSignificant(true);
	    stval.whitespaceChars(32,47);
	    stval.whitespaceChars(58,63);

	    Integer nextid;
	    Double nextval;
	    int count =0;
	    while(stid.nextToken() != StreamTokenizer.TT_EOF && count<numlines){
		do{
		    stid.nextToken();
		}while(stid.nextToken() != StreamTokenizer.TT_EOL);
		nextid = new Integer((int)stid.nval);
		do{
		    stval.nextToken();
		}while(stval.nextToken() != StreamTokenizer.TT_EOL);
		nextval = new Double(stval.nval);
		result.put(nextid,nextval);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	
	return result;
    }

    public static TreeMap<Double,Vector<Integer>> buildValueIndexMap(String idfile, String valfile, int numlines){
	/* 
	 * Same types of inputs as for buildIndxValueMap. Numbers in valfile now
	 * become the keys in the map. All values in idfile with corresponding equal entries in valfile
	 * are collected in a vector.
	 */

	TreeMap<Double,Vector<Integer>> result = new TreeMap<Double,Vector<Integer>>();
	try{
	    FileReader inid = new FileReader(idfile);
	    StreamTokenizer stid = new StreamTokenizer(inid);
	    stid.eolIsSignificant(true);
	    stid.whitespaceChars(32,47);
	    stid.whitespaceChars(58,63);

	    FileReader inval = new FileReader(valfile);
	    StreamTokenizer stval = new StreamTokenizer(inval);
	    stval.eolIsSignificant(true);
	    stval.whitespaceChars(32,47);
	    stval.whitespaceChars(58,63);

	    Integer nextid;
	    Double nextval;
	    int count =0;
	    while(stid.nextToken() != StreamTokenizer.TT_EOF && count<numlines){
		do{
		    stid.nextToken();
		}while(stid.nextToken() != StreamTokenizer.TT_EOL);
		nextid = new Integer((int)stid.nval);
		do{
		    stval.nextToken();
		}while(stval.nextToken() != StreamTokenizer.TT_EOL);
		nextval = new Double(stval.nval);

		Vector<Integer> valentry = result.get(nextval);
		if (valentry == null)
		    {
			Vector<Integer> newvec = new Vector<Integer>();
			newvec.add(nextid);
			result.put(nextval,newvec);
		    }
		else
		    valentry.add(nextid);
		
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	
	return result;
    }

    public  static int randomInteger(int max){
    	/* Generates a random integer, uniformly distributed between 0 and max */
    	double rand = Math.random();
    	double l = (double)1/(max+1);
    	return (int)Math.floor(rand/l);
    }

    public static int[] randomIntArray(int length, int maxindex){
	/* Creates an integer array of length 'length'. Entries are 
	 * randomly selected integers from 0 to maxindex - 1, without repetitions
	 * If length > maxindex, then return only array of length 
	 * maxindex (containing all integers from 0 to maxindex - 1)
	 */
	int resultlength = Math.min(length,maxindex);
	int[] result = new int[resultlength];
	Vector<Integer> intUrn = new Vector<Integer>(); 
	int nextdraw;
	Integer drawnint;
	for (int i=0;i<maxindex;i++)
	    intUrn.add(new Integer(i));
	for (int i=0;i<resultlength;i++){
	    nextdraw = randomInteger(intUrn.size()-1);
	    drawnint = (Integer)intUrn.remove(nextdraw);
	    result[i]=drawnint.intValue();
	}
	return result;
    }

		
    public static Vector<Integer> getKNN(TreeMap<Double,Vector<Integer>> traindata, double key, int k){
	/* 
	 * Returns k nearest neighbors of 'key' from 'traindata'
	 * 
	 *
	 */

	int[] selind;
	Vector<Integer> allneighbors = new Vector<Integer>();
	Vector<Integer>  farneighbors = traindata.get(key);
	if (farneighbors != null)
	    allneighbors.addAll(farneighbors);
	System.out.print("Allneighbors.size:" + allneighbors.size());
	
	if (allneighbors.size()>k){
	    selind = randomIntArray(k,allneighbors.size());
	    Vector<Integer> subneighbors = new Vector<Integer>();
	    for (int i=0;i<selind.length;i++){
		subneighbors.add(allneighbors.elementAt(selind[i]));
	    }
	    allneighbors=subneighbors;
	}
		
		
	int dist = 0;
	while (allneighbors.size() < k){
	    dist++;
	    farneighbors = new Vector<Integer>();
	    Vector<Integer> rightneighbors = traindata.get(key+dist);
	    Vector<Integer> leftneighbors = traindata.get(key-dist);
	    if (rightneighbors != null)
		farneighbors.addAll(rightneighbors);
	    if (leftneighbors != null)
		farneighbors.addAll(leftneighbors);
	    if (allneighbors.size()+farneighbors.size() > k){
		selind = randomIntArray( k-allneighbors.size(),farneighbors.size());
		Vector<Integer> subneighbors = new Vector<Integer>();
		for (int i=0;i<selind.length;i++){
		    subneighbors.add(farneighbors.elementAt(selind[i]));
		}
		farneighbors=subneighbors;
	    }
	    allneighbors.addAll(farneighbors);
	    System.out.print("   " + allneighbors.size());
	}
	System.out.println();
	return allneighbors;
    }
		
    public static void main(String[] args)
    {
	BiblioRegressionCommandLineOptions options = new BiblioRegressionCommandLineOptions(args);
	int trainsize = 100000;
	int testsize = 100000;
	System.out.println("#Train: " + trainsize + " #Test: " + testsize);
	try{
	    /* create mysql relational structure which uses TD BU procedure */
	    MySQLRelStructure relstruct = new MySQLRelStructure(options.database, options.login, options.password, true);

	    /* create MySQLTestViewer if needed */
	    MySQLTestViewer testviewer = (options.srcdb != null && options.testviewconfigfile != null) ?
		new MySQLTestViewer(relstruct, options.srcdb, options.testviewconfigfile) : null;

	    TetHistogramHasher hasher = new TetHistogramHasher(options.tetfile, options.bins, options.threshold, 
							       options.extra_mass_penalty, options.decay,
							       options.marginalized_emd, options.add_count_distance,
							       EMDNormalization.BOOLEAN,
							       options.bucket_exploration_rate, options.K, options.random_seed,
							       options.max_hash_tree_depth, options.max_bucket_size);

	    long startTime = System.currentTimeMillis();
	    TreeMap<Integer, TetHistogram> train_data = hasher.readData(options.trainfile,  relstruct, testviewer, trainsize);
	    TreeMap<Integer, TetHistogram> test_data = hasher.readData(options.testfile,  relstruct, testviewer, testsize);
	    hasher.data_map = train_data;
	    relstruct.Close();
	    long estimatedTime = (System.currentTimeMillis() - startTime)/1000;
	    System.out.println("Processing train and test examples took " + estimatedTime + " s");
	    //hasher.normalizer.updateStatistics(train_data.values());


      
	    /* normalize */
	    hasher.normalize(train_data.values());
	    hasher.normalize(test_data.values());

 
	    // /* Compute hierarchical hash table */
	    startTime = System.currentTimeMillis();
	    hasher.buildHierarchicalHashTree(train_data);
	    estimatedTime = (System.currentTimeMillis() - startTime)/1000;
	    System.out.println("Building hierarchical hash tree took " + estimatedTime + " s");

	    Vector<String> alllabels = new Vector<String>();
	    alllabels.add("hindex");
	    alllabels.add("gindex");
	    alllabels.add("eindex");
	    alllabels.add("i10index");

	    /* Cut-off values for hindex, gindex, eindex, i10index (in this order)
	     * when for regression label is effectively replaced by min(label,cutoffval)
	     */
	    int cutoffvals[] = {20,7,30,30};
	    boolean doalsocitesknn = false; // whether we also want to predict based on neighbors defined by same number
	                                    // of total cites
	    
	    startTime = System.currentTimeMillis();

	    String traincitefile="totcit.train";
	    String testcitefile="totcit.test";
	    TreeMap<Integer,Double> testcits = null;
	    TreeMap<Double,Vector<Integer>> cite2indx = null;
	    if (doalsocitesknn){
		testcits =  buildIndxValueMap(options.testfile,testcitefile,testsize);
		cite2indx =  buildValueIndexMap(options.trainfile,traincitefile,trainsize);
	    }

	    
	    int indx =0;
	    for (Enumeration<String> e=alllabels.elements();e.hasMoreElements();indx++){
		String labeltype=e.nextElement();
		String trainvalfile=labeltype+".train";
		String testvalfile=labeltype+".test";
		
		TreeMap<Integer,Double> trainvals =  buildIndxValueMap(options.trainfile,trainvalfile,trainsize);
		TreeMap<Integer,Double> testvals =  buildIndxValueMap(options.testfile,testvalfile,testsize);
		//TreeMap<Integer,Double> traincits =  buildIndxValueMap(options.trainfile,traincitefile,trainsize);
		
		double rmse = 0;
		double rmse_cutoff = 0;
	    	double rmse_cites = 0;
		for (Iterator<Integer> it=test_data.keySet().iterator();it.hasNext();){
		    Integer nexttestid = it.next();
		    TetHistogram nexthisto = test_data.get(nexttestid);
		    Vector<Integer> neighbors = hasher.retrieveNeighbors(nexthisto);
		    // System.out.print("test id " + nexttestid + "  neighbors: ");
		    // for (int i=0;i<neighbors.size();i++)
		    // 	System.out.println(" " + neighbors.elementAt(i));
		    double testtarget = testvals.get(nexttestid);
		    double testtarget_cutoff = Math.min(testtarget,cutoffvals[indx]);
		    double predtarget =0;
		    double predtarget_cutoff =0;
		    for (int i=0;i<neighbors.size();i++){
			    predtarget_cutoff=predtarget_cutoff+Math.min(trainvals.get(neighbors.elementAt(i)),cutoffvals[indx]);
			    predtarget=predtarget+trainvals.get(neighbors.elementAt(i));
		    }
		    predtarget = predtarget/neighbors.size();
		    predtarget_cutoff = predtarget_cutoff/neighbors.size();
		    rmse = rmse + Math.pow(testtarget - predtarget,2);
		    rmse_cutoff = rmse_cutoff + Math.pow(testtarget_cutoff - predtarget_cutoff,2);
		    /* Now compute prediction based on total cite counts alone: */
		    double  predtarget_cites=0;
		    if (doalsocitesknn){
			Vector<Integer> nearestCiteNeighbors = getKNN(cite2indx,testcits.get(nexttestid),options.K);
			for (int i=0;i<nearestCiteNeighbors.size();i++)
			    predtarget_cites=predtarget_cites+trainvals.get(nearestCiteNeighbors.elementAt(i));
			predtarget_cites=predtarget_cites/nearestCiteNeighbors.size();
			rmse_cites = rmse_cites + Math.pow(testtarget - predtarget_cites,2);
		    }
		    
		    System.out.print(labeltype + " Test example " + nexttestid + " true: " + testtarget
				       + " predicted: " + predtarget + " predicted_cutoff: " + predtarget_cutoff );
		    if (doalsocitesknn)
			System.out.print("   predicted_cites: " + predtarget_cites);
		    System.out.println();
		    
		}
		rmse = Math.sqrt(rmse/test_data.size());
		rmse_cutoff  = Math.sqrt(rmse_cutoff/test_data.size());
		rmse_cites = Math.sqrt(rmse_cites/test_data.size());
		
		System.out.println("RMSE for " +  labeltype + ": " +  rmse);
		System.out.println("RMSE for " +  labeltype + "with cutoff at " + cutoffvals[indx]+ ": " +  rmse_cutoff);
		 if (doalsocitesknn)
		     System.out.println("RMSE with cites for " +  labeltype + ": " +  rmse_cites);
		
	    }
	    estimatedTime = (System.currentTimeMillis() - startTime)/1000;
	    System.out.println("Evaluating test cases took " + estimatedTime + " s");
	    
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }

}

class BiblioRegressionCommandLineOptions {


    public String database;
    public String login;
    public String password;
    public String tetfile;
    public String trainfile;
    public String testfile;
    public String outfile;
    public String trainvalfile;  
    public String testvalfile;  

    public String srcdb = null;
    public String testviewconfigfile = null;

    public int bins = 10;
    public int false_path_counts = 0;
    public double threshold = 0;
    public double extra_mass_penalty = -1;
    public double decay = 0.5;
    public Normalization histogram_normalization = Normalization.NONE;

    public int K = 7;
    public boolean marginalized_emd = false;
    public boolean add_count_distance = false;
    public int random_seed = 10;
    public int max_hash_tree_depth = 15;
    public int max_bucket_size = 30;
    public double bucket_exploration_rate = 0.0;

    public BiblioRegressionCommandLineOptions(String[] args)
    {
	int pos = parseOptions(args);

	// chek if compulsory options specified
	if(args.length - pos < 6){
	    System.out.println("Missing compulsory option(s):\n");
	    printHelp();
	    System.exit(1);
	}

	database = args[pos++];
	login = args[pos++];
	password = args[pos++];
	tetfile = args[pos++];
	trainfile = args[pos++];
	testfile = args[pos++];
	// trainvalfile = args[pos++];
	// testvalfile = args[pos++];
	outfile = args[pos++];
    }

    public int parseOptions(String[] options)
    {
	int pos = 0;

	while(pos < options.length && options[pos].charAt(0) == '-'){
	    switch(options[pos].charAt(1)){
	    case 'c':
		testviewconfigfile = options[++pos];
		break;
	    case 'd':
		srcdb = options[++pos];
		break;
	    case 'b':
		bins = Integer.parseInt(options[++pos]);
		break;
	    case 't':
		threshold = Double.parseDouble(options[++pos]);
		break;
	    case 'p':
		extra_mass_penalty = Double.parseDouble(options[++pos]);
		break;
	    case 'F':
		false_path_counts = Integer.parseInt(options[++pos]);
		break;
	    case 'n':
		histogram_normalization = Normalization.values()[Integer.parseInt(options[++pos])];
		break;
	    case 'w':
		decay = Double.parseDouble(options[++pos]);
		break;
	    case 'k':
		K = Integer.parseInt(options[++pos]);
		break;
	    case 'M':
		marginalized_emd = true;
		break;
	    case 'C':
		add_count_distance = true;
		break;
	    case 'L':
		bucket_exploration_rate = Double.parseDouble(options[++pos]);
		break;
	    case 'R':
		random_seed = Integer.parseInt(options[++pos]);
		break;    
	    case 'D':
		max_hash_tree_depth = Integer.parseInt(options[++pos]);
		break;
	    case 'S':
		max_bucket_size = Integer.parseInt(options[++pos]);
		break;
	    case 'h':
	    default:
		printHelp();
		System.exit(1);
	    }
	    pos++;
	}

	if(threshold == 0)
	    threshold = Double.MAX_VALUE;

	return pos;
    }

    public void printHelp()
    {
	System.out.println("Usage:\n\ttet.rnn.TetHistogramHasher [options] <database> <login> <password> <tetfile> <trainfile> <testfile> <outfile>");
	System.out.println("Options:");
	System.out.println("\t-c <int>   \tconfig file for test view (default=null)");
	System.out.println("\t-d <string>\tsrc db for test view (default=null)");
	System.out.println("\t-b <int>   \tnumber of bins (default=10)");
	System.out.println("\t-F <int>   \tfalse path counts (default=0)");
	System.out.println("\t-t <double>\tdistance threshold (default=0 no threshold)");
	System.out.println("\t-p <double>\textra mass penalty (default=-1)");
	System.out.println("\t-w <double>\tlevelwise weight decay (default=0.5)");
	System.out.println("\t-n <int>   \thistogram normalization: NONE=0, ACTIVE_SIZE=1, REBALANCE=2, PLAIN=3 (default=NONE)");
	System.out.println("\t-k <int>   \tneighbors for KNN computation (default=7)");
	System.out.println("\t-L <int>   \tparameter for alternate buckets exploration (default=0)");
	System.out.println("\t-M         \tuse Marginalized EMD (default=false)");
	System.out.println("\t-C         \tadd count distance (default=false)");
	System.out.println("\t-R <int>   \trandom seed (default=0)");
	System.out.println("\t-D <int>   \tmax hash tree depth (default=10)");
	System.out.println("\t-S <int>   \tmax bucket size (default=10)");
	System.out.println("\t-h         \tprint help");
	System.exit(1);
    }

    public String toString()
    {
	return "database=" + database + "\n"
	    + "login=" + login + "\n"
	    + "password=" + password + "\n"
	    + "tetfile=" + tetfile + "\n"
	    + "trainfile=" + trainfile + "\n"
	    + "testfile=" + testfile + "\n"
	    + "outfile=" + outfile + "\n"
	    + "srcdb=" + srcdb + "\n"
	    + "testviewconfigfile=" + testviewconfigfile + "\n"
	    + "bins=" + bins + "\n"
	    + "false_path_counts=" + false_path_counts + "\n"
	    + "threshold=" + threshold + "\n"
	    + "extra_mass_penalty=" + extra_mass_penalty + "\n"
	    + "decay=" + decay + "\n"
	    + "histogram_normalization=" + histogram_normalization + "\n"
	    + "bucket_exploration_rate=" + bucket_exploration_rate + "\n"
	    + "random_seed=" + random_seed + "\n"
	    + "marginalized_emd=" + marginalized_emd + "\n"
	    + "add_count_distance=" + add_count_distance + "\n"
	    + "max_hash_tree_depth" + max_hash_tree_depth + "\n"
	    + "max_bucket_size" + max_bucket_size + "\n";
    }
}
