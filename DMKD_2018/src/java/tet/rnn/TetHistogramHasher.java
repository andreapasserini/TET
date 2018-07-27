package tet.rnn;

import java.util.*;
import java.sql.*;
import java.io.*;
import tet.*;
import myio.*;
import FastEMD.*;

public class TetHistogramHasher {

  public TetHistogramDistance distance_computer;
  public rnn_Tet tet;
  public HierarchicalHashTree hierarchical_hash_tree;
  public double bucket_exploration_rate;
  public int K;
  public Random random;
  public TreeMap<Integer, TetHistogram> data_map;

  public TetHistogramHasher(String tetfile, int bins, double threshold, double extra_mass_penalty, double decay, 
                        boolean marginalized_emd, boolean add_count_distance, EMDNormalization emd_normalization,
                        double bucket_exploration_rate, int K, int random_seed, 
                        int max_hash_tree_depth, int max_bucket_size)
        throws Exception
  {

    /* load discriminant tet from file */
    String tetstring = Tet.tetString(tetfile);

    this.tet = new rnn_Tet(tetstring);

    System.out.println("Read discriminant Tet string: " + tet.Serialize());
    System.out.println("Tet Freevars = " + tet.freevars().toString());


    this.bucket_exploration_rate = bucket_exploration_rate;
    this.K = K;

    this.distance_computer = new TetHistogramDistance(tet, bins, threshold, extra_mass_penalty, decay, marginalized_emd, add_count_distance, emd_normalization);
    
    this.random = new Random();
    random.setSeed(random_seed);    

    this.hierarchical_hash_tree = new HierarchicalHashTree(0, max_hash_tree_depth, max_bucket_size, distance_computer, random);

    this.data_map = null;
  }

  public void buildHierarchicalHashTree(TreeMap<Integer, TetHistogram> data)
        throws Exception
  {
    
    hierarchical_hash_tree.fill(new Vector<Integer>(data.keySet()), "ROOT", "", data);
  }

  public TreeMap<Integer, TetHistogram> readData(String datafile, MySQLRelStructure relstruct, MySQLTestViewer testviewer,int numexamples)
        throws Exception
  { 

    TreeMap<Integer, TetHistogram> data = new TreeMap<Integer, TetHistogram>();

    FileReader in = new FileReader(datafile);
    StreamTokenizer st = new StreamTokenizer(in);
    st.eolIsSignificant(true);
    st.whitespaceChars(32,47);
    st.whitespaceChars(58,63);

    System.out.println("=== Reading data file <" + datafile + "> === ");
    
    int count = 0;
    while(st.nextToken() != StreamTokenizer.TT_EOF && count<numexamples )
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

      //System.out.println(example);

      /* add test view for current example if needed */
      if(testviewer != null)
        testviewer.addTestView(objmap);

      data.put(id,tet.computeTetHistogram(relstruct, objmap, distance_computer.bins).second());
      count++;
      
      /* remove test view for current example if needed */
      if(testviewer != null)
        testviewer.removeTestView(objmap);
    }

    in.close();

    return data;
  } 

  public Vector<Integer> readLabels(String filename)
        throws Exception
  {
    FileReader in = new FileReader(filename);
    BufferedReader br = new BufferedReader(in);
    String line = null;
    Vector<Integer> labels = new Vector<Integer>();
    while ((line = br.readLine()) != null) 
    {
      labels.add(Integer.parseInt(line));
    }
    br.close();
    return labels;
  }

  public void retrieveNeighbors(TreeMap<Integer, TetHistogram> test_data)
        throws Exception
  {

    for (Iterator<Integer> it = test_data.keySet().iterator(); it.hasNext();) 
    {
      Integer id = it.next();
      TetHistogram histo = test_data.get(id);

      System.out.println("Test example " + id);

      Vector<Integer> closest_neighbors_ids = retrieveNeighbors(histo);

      System.out.print("Neighbors ");
      System.out.println(Arrays.toString(closest_neighbors_ids.toArray()));
    }

  }

  public Vector<Integer> retrieveNeighbors(TetHistogram histo)
  throws Exception
  {

    TreeMap<Double,Vector<Integer>> neighbors_ids = new TreeMap<Double,Vector<Integer>>();

    if (bucket_exploration_rate == 0)
    {
      // If bucket_exploration_rate is = 0, then we will only check the nearest bucket and that's all,
      // thus we can call the getNearestBucket method rather than sorting all other hash bit distances
      Vector<Integer> bucket_ids = hierarchical_hash_tree.getNearestBucket(histo);
      System.out.println("Number of bucket leaves = " +  bucket_ids.size());

      for (int i = 0; i < bucket_ids.size(); i++)
      {   
        Integer id = bucket_ids.elementAt(i);
        Double d = distance_computer.distance(histo, data_map.get(id));
        if (!neighbors_ids.containsKey(d))
          neighbors_ids.put(d, new Vector<Integer>());
        neighbors_ids.get(d).add(id);
      }
    }
    else
      throw new Exception("bucket_exploration_rate > 0 not implemented yet!");
       
    /* retain the K closest neighbors */
    Vector<Integer> closest_neighbors_ids = new Vector<Integer>();
      
    for (Iterator<Double> it = neighbors_ids.keySet().iterator(); it.hasNext() && closest_neighbors_ids.size() < K;)
      closest_neighbors_ids.addAll(neighbors_ids.get(it.next()));

    return closest_neighbors_ids;
  }

  public static void normalize(Collection<TetHistogram> histos)
  {
    for (Iterator<TetHistogram> it = histos.iterator(); it.hasNext();)
      it.next().normalize();
  }

  public static void main(String[] args)
  {
    TetHistogramHasherCommandLineOptions options = new TetHistogramHasherCommandLineOptions(args);

    try
    {
      /* create mysql relational structure which uses TD BU procedure */
      MySQLRelStructure relstruct = new MySQLRelStructure(options.database, options.login, options.password, true);

      /* create MySQLTestViewer if needed */
      MySQLTestViewer testviewer = (options.srcdb != null && options.testviewconfigfile != null) ?
      new MySQLTestViewer(relstruct, options.srcdb, options.testviewconfigfile) : null;

      TetHistogramHasher hasher = new TetHistogramHasher(options.tetfile, options.bins, options.threshold, 
                options.extra_mass_penalty, options.decay, options.marginalized_emd, options.add_count_distance, options.emd_normalization,
                options.bucket_exploration_rate, options.K, options.random_seed, options.max_hash_tree_depth, options.max_bucket_size);

     
      TreeMap<Integer, TetHistogram> train_data = hasher.readData(options.trainfile, relstruct, testviewer,Integer.MAX_VALUE);

      hasher.data_map = train_data;

      TreeMap<Integer, TetHistogram> test_data = hasher.readData(options.testfile, relstruct, testviewer,Integer.MAX_VALUE);
   
      /* normalize training set */
      hasher.normalize(train_data.values());
    
      /* normalize test set */
      hasher.normalize(test_data.values());
      
      /* compute hierarchical hash table */
      long startTime = System.nanoTime();
      hasher.buildHierarchicalHashTree(train_data);
      long estimatedTime = System.nanoTime() - startTime;
      System.out.println("Building hierarchical hash tree took " + estimatedTime + " ns");

      /* Now process test examples and retrieve the nearest bucket(s) */
      startTime = System.nanoTime();
      hasher.retrieveNeighbors(test_data);
      estimatedTime = System.nanoTime() - startTime;
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

class TetHistogramHasherCommandLineOptions {


  public String database;
  public String login;
  public String password;
  public String tetfile;
  public String trainfile;
  public String testfile;
  public String outfile;

  public String srcdb = null;
  public String testviewconfigfile = null;

  public int bins = 10;
  public double threshold = 0;
  public double extra_mass_penalty = -1;
  public double decay = 0.5;

  public int K = 7;
  public boolean marginalized_emd = false;
  public boolean add_count_distance = false;
  public EMDNormalization emd_normalization = EMDNormalization.NONE;
  public int random_seed = 10;
  public int max_hash_tree_depth = 10;
  public int max_bucket_size = 10;
  public double bucket_exploration_rate = 0.0;

  public TetHistogramHasherCommandLineOptions(String[] args)
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
        case 'N':
            emd_normalization = EMDNormalization.values()[Integer.parseInt(options[++pos])];
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
    System.out.println("\t-t <double>\tdistance threshold (default=0 no threshold)");
    System.out.println("\t-p <double>\textra mass penalty (default=-1)");
    System.out.println("\t-w <double>\tlevelwise weight decay (default=0.5)");   
    System.out.println("\t-k <int>   \tneighbors for KNN computation (default=7)");
    System.out.println("\t-L <int>   \tparameter for alternate buckets exploration (default=0)");
    System.out.println("\t-M         \tuse Marginalized EMD (default=false)");
    System.out.println("\t-C         \tadd count distance (default=false)");
    System.out.println("\t-N <int>   \tEMD normalization: NONE=0, BOOLEAN=1, NUMERIC=2 (default=NONE)");
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
    + "threshold=" + threshold + "\n"
    + "extra_mass_penalty=" + extra_mass_penalty + "\n"
    + "decay=" + decay + "\n"
    + "bucket_exploration_rate=" + bucket_exploration_rate + "\n"
    + "random_seed=" + random_seed + "\n"
    + "emd_normalization" + emd_normalization + "\n"
    + "marginalized_emd=" + marginalized_emd + "\n"
    + "add_count_distance=" + add_count_distance + "\n"
    + "max_hash_tree_depth" + max_hash_tree_depth + "\n"
    + "max_bucket_size" + max_bucket_size + "\n";
  }
}
