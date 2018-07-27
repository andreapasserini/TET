package tet.rnn;

import java.util.*;
import java.sql.*;
import java.io.*;
import tet.*;
import myio.*;
import FastEMD.*;

public class TetHistogramKNN {

    public static TetHistogramDistance distance_computer;
    public static Vector<Integer> trainLabels;
    public static Vector<Integer> testLabels;
    public static Vector<Integer> trainIds;
    public static Vector<Integer> testIds;
    public static int num_train_examples;
    public static int num_test_examples;
    public static int K;

    public TetHistogramKNN()
    {
	this.distance_computer = null;
	trainLabels = new Vector<Integer>();
	testLabels = new Vector<Integer>();
	trainIds = new Vector<Integer>();
	testIds = new Vector<Integer>();
    }

    public TetHistogramKNN(Tet tet, int bins, double threshold, double extra_mass_penalty, double decay, boolean marginalized_emd, boolean add_count_distance, EMDNormalization emd_normalization)
    {
	this.distance_computer = new TetHistogramDistance(tet, 0, bins, threshold, extra_mass_penalty, decay, marginalized_emd, add_count_distance, emd_normalization);
	trainLabels = new Vector<Integer>();
	testLabels = new Vector<Integer>();
	trainIds = new Vector<Integer>();
	testIds = new Vector<Integer>();
    }

    public static void main(String[] args)
    {

        TetHistogramKNNCommandLineOptions options = new TetHistogramKNNCommandLineOptions(args);

        try{
            /* create mysql relational structure which uses TD BU procedure */
            MySQLRelStructure relstruct = new MySQLRelStructure(options.database, options.login, options.password, true);

            /* create MySQLTestViewer if needed */
            MySQLTestViewer testviewer = (options.srcdb != null && options.testviewconfigfile != null) ?
                new MySQLTestViewer(relstruct, options.srcdb, options.testviewconfigfile) : null;

            /* load discriminant tet from file */
            String tetstring = Tet.tetString(options.tetfile);

            rnn_Tet tet = new rnn_Tet(tetstring);

            System.out.println("Read discriminant Tet string: " + tet.Serialize());
            System.out.println("Tet Freevars = " + tet.freevars().toString());

	    /* initialize tet histogram distance */
	    distance_computer = new TetHistogramDistance(tet, options.bins,								
							 options.threshold,
							 options.extra_mass_penalty,
							 options.decay,		
							 options.marginalized_emd,
							 options.add_count_distance,
							 options.emd_normalization);

	    TetHistogramNormalizer normalizer = new TetHistogramNormalizer(tet, options.histogram_normalization);
	    Vector<TetHistogram> tet_train_histos = new Vector<TetHistogram>();
	    Vector<TetHistogram> tet_test_histos = new Vector<TetHistogram>();

	    K = options.K;

            /* parse object file and compute discriminant value for each object  */
            FileReader in = new FileReader(options.trainfile);
            StreamTokenizer st = new StreamTokenizer(in);
            st.eolIsSignificant(true);
            st.whitespaceChars(32,47);
            st.whitespaceChars(58,63);

	    System.out.println("=== Training set === ");
	    trainIds = new Vector<Integer>();

	    num_train_examples = 0;

            while(st.nextToken() != StreamTokenizer.TT_EOF){

                HashMap<String, Object> objmap = new HashMap<String, Object>();

                System.out.println("Processing example: ");
                String example = "";
                do{
                    String var = st.sval;
                    st.nextToken();
                    String val = st.sval;
                    if(st.ttype == st.TT_NUMBER)
		    {
                        val = String.valueOf((int)st.nval);
			trainIds.add((int)st.nval);
		    }
                    objmap.put(var,new RelObject(val));
                    example += var + "=" + val + " ";
                }while(st.nextToken() != StreamTokenizer.TT_EOL);

                System.out.println(example);

                /* add test view for current example if needed */
                if(testviewer != null)
                    testviewer.addTestView(objmap);

		tet_train_histos.add(tet.computeTetHistogram(relstruct, objmap, options.bins).second()); 

		normalizer.updateStatistics(tet_train_histos.lastElement());

                /* remove test view for current example if needed */
                if(testviewer != null)
                    testviewer.removeTestView(objmap);

		num_train_examples++;
            }

	    in.close();

            /* parse object file and compute discriminant value for each object  */
            in = new FileReader(options.testfile);
            st = new StreamTokenizer(in);
            st.eolIsSignificant(true);
            st.whitespaceChars(32,47);
            st.whitespaceChars(58,63);

	    System.out.println("=== Test set === ");
	    testIds = new Vector<Integer>();

	    num_test_examples = 0;

            while(st.nextToken() != StreamTokenizer.TT_EOF){

                HashMap<String, Object> objmap = new HashMap<String, Object>();

                System.out.println("Processing example: ");
                String example = "";
                do{
                    String var = st.sval;
                    st.nextToken();
                    String val = st.sval;
                    if(st.ttype == st.TT_NUMBER)
		    {
                        val = String.valueOf((int)st.nval);
			testIds.add((int)st.nval);
		    }
                    objmap.put(var,new RelObject(val));
                    example += var + "=" + val + " ";
                }while(st.nextToken() != StreamTokenizer.TT_EOL);

                System.out.println(example);

                /* add test view for current example if needed */
                if(testviewer != null)
                    testviewer.addTestView(objmap);

		tet_test_histos.add(tet.computeTetHistogram(relstruct, objmap, options.bins).second()); 

		//normalizer.updateStatistics(tet_test_histos.lastElement());

                /* remove test view for current example if needed */
                if(testviewer != null)
                    testviewer.removeTestView(objmap);

		num_test_examples++;
            }

            /* parse train labels file */
            FileReader inTrainLabels = new FileReader(options.trainlabelsfile);
	    BufferedReader br = new BufferedReader(inTrainLabels);
	    String line = null;
	    trainLabels = new Vector<Integer>();
            while ((line = br.readLine()) != null) {
		trainLabels.add(Integer.parseInt(line));
	    }
	    br.close();

            /* parse test labels file */
            FileReader inTestLabels = new FileReader(options.testlabelsfile);
	    br = new BufferedReader(inTestLabels);
	    line = null;
	    testLabels = new Vector<Integer>();
            while ((line = br.readLine()) != null) {
		testLabels.add(Integer.parseInt(line));
	    }
	    br.close();
            
	    /* normalize training set */
	    for(int i = 0; i < num_train_examples; i++){
		normalizer.normalize(tet_train_histos.elementAt(i));
	    }

	    /* normalize test set */
	    for(int i = 0; i < num_test_examples; i++){
		normalizer.normalize(tet_test_histos.elementAt(i));
	    }

	    long startTime = System.nanoTime();

	    for(int i = 0; i < num_test_examples; i++){
	        System.out.println("Test example " + i);
	        System.out.print("Neighbors ");
	        TreeMap<Double,Vector<Integer>> neighborsLabels = new TreeMap<Double,Vector<Integer>>();
	        TreeMap<Double,Vector<Integer>> neighborsIds = new TreeMap<Double,Vector<Integer>>();
	        for(int j = 0; j < num_train_examples; j++)
		{
		    double d = distance_computer.distance(tet_test_histos.elementAt(i),tet_train_histos.elementAt(j));
		    Vector<Integer> vecLabels = neighborsLabels.get(d);
		    Vector<Integer> vecIds = neighborsIds.get(d);

		    if (vecLabels == null)
		    {
	                //System.out.println("Compare with training example " + j + " at distance " + d + " (new distance)");
			vecLabels = new Vector<Integer>();
			vecLabels.add(trainLabels.elementAt(j));
			neighborsLabels.put(d,vecLabels);

			vecIds = new Vector<Integer>();
			vecIds.add(trainIds.elementAt(j));
			neighborsIds.put(d,vecIds);
		    }
		    else
		    {
	                //System.out.println("Compare with training example " + j + " at distance " + d + " (distance already present with " + v.size() + " elements)");
			neighborsLabels.remove(d);
			vecLabels.add(trainLabels.elementAt(j));
			neighborsLabels.put(d,vecLabels);

			neighborsIds.remove(d);
			vecIds.add(trainIds.elementAt(j));
			neighborsIds.put(d,vecIds);
		    }
		}

		int jj = 0;
		int count = 0;
		int pos = 0;

		do
		{
		    String dist = neighborsLabels.keySet().toArray()[jj].toString();
		    Vector<Integer> vecLabels = neighborsLabels.get(Double.parseDouble(dist));
		    Vector<Integer> vecIds = neighborsIds.get(Double.parseDouble(dist));
		    jj++;
		    count += vecLabels.size();
	            //System.out.println(v.size() + " neighbors for distance " + dist);
		    for (int ii = 0; ii < vecLabels.size(); ii++)
		    {
			System.out.print(vecIds.elementAt(ii) + " ");

			if (vecLabels.elementAt(ii) == 1)
			{
			    pos++;
			}
		    }
		}
		while(count < K);
		System.out.print("\n");

		double prediction = -1;
		if ((double)pos/(double)count >= 0.5) prediction = 1;

		System.out.println(pos + " positives out of " + count + " ---> " + prediction + " with label " + testLabels.elementAt(i));
	    }
      	    long estimatedTime = System.nanoTime() - startTime;
            System.out.println("Retrieval of test examples took " + estimatedTime + " ns");
		    

	    /* print distance matrix */
	    //FileWriter out = new FileWriter(options.outfile, false);
	    //out.write(StringOps.matrixToString(distance_matrix));
	    //out.close();
            relstruct.Close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}

class TetHistogramKNNCommandLineOptions {


    public String database;
    public String login; 
    public String password; 
    public String tetfile; 
    public String trainfile; 
    public String trainlabelsfile; 
    public String testfile; 
    public String testlabelsfile; 
    public String outfile; 
	
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
	public EMDNormalization emd_normalization = EMDNormalization.NONE;

    public TetHistogramKNNCommandLineOptions(String[] args)
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
	trainlabelsfile = args[pos++];
	testfile = args[pos++];
	testlabelsfile = args[pos++];
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
		case 'N':
        emd_normalization = EMDNormalization.values()[Integer.parseInt(options[++pos])];
      	break;
	    case 'M':
		marginalized_emd = true; 
		break;
	    case 'C':
		add_count_distance = true;
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
	System.out.println("Usage:\n\ttet.rnn.TetHistogramKNN [options] <database> <login> <password> <tetfile> <trainfile> <trainlabelsfile> <testfile> <testlabelsfile> <outfile>");
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
	System.out.println("\t-N <int>   \tEMD normalization: NONE=0, BOOLEAN=1, NUMERIC=2 (default=NONE)");
	System.out.println("\t-M         \tuse Marginalized EMD (default=false)");
    System.out.println("\t-C         \tadd count distance (default=false)");
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
	    + "trainlabelsfile=" + trainlabelsfile + "\n" 
	    + "testfile=" + testfile + "\n" 
	    + "testlabelsfile=" + testlabelsfile + "\n" 
	    + "outfile=" + outfile + "\n" 
	    + "srcdb=" + srcdb + "\n" 
	    + "testviewconfigfile=" + testviewconfigfile + "\n" 
	    + "bins=" + bins + "\n"
	    + "false_path_counts=" + false_path_counts + "\n"
	    + "threshold=" + threshold + "\n"
	    + "extra_mass_penalty=" + extra_mass_penalty + "\n"
	    + "decay=" + decay + "\n"
	    + "emd_normalization" + emd_normalization + "\n"
	    + "marginalized_emd=" + marginalized_emd + "\n"
        + "add_count_distance=" + add_count_distance + "\n"
	    + "histogram_normalization=" + histogram_normalization + "\n";
    }
}

