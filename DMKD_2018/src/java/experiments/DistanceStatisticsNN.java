package experiments;

import java.util.*;
import java.sql.*;
import java.io.*;
import tet.*;
import tet.rnn.*;
import myio.*;
import FastEMD.*;
 





public class DistanceStatisticsNN{
  public static void main(String[] args)
  {
    DistanceStatisticsCommandLineOptions options = new DistanceStatisticsCommandLineOptions(args);
    int numtrain = 1000;
    int numtest = 20;
    try{
      /* create mysql relational structure which uses TD BU procedure */
      MySQLRelStructure relstruct = new MySQLRelStructure(options.database, options.login, options.password, true);
 
      /* create MySQLTestViewer if needed */
      MySQLTestViewer testviewer = (options.srcdb != null && options.testviewconfigfile != null) ?
      new MySQLTestViewer(relstruct, options.srcdb, options.testviewconfigfile) : null;
      System.out.println("distbug  decay (DistanceStatistics) : " + options.decay);
      TetHistogramHasher hasher = new TetHistogramHasher(options.tetfile, options.bins, options.threshold, 
                options.extra_mass_penalty, options.decay, options.marginalized_emd, options.add_count_distance,
							 EMDNormalization.BOOLEAN,
                options.bucket_exploration_rate, options.K, options.random_seed, options.max_hash_tree_depth, options.max_bucket_size);

     
      TreeMap<Integer, TetHistogram> train_data = hasher.readData(options.trainfile, relstruct, testviewer,numtrain);
      TreeMap<Integer, TetHistogram> test_data = hasher.readData(options.testfile, relstruct, testviewer,numtest);
      
      hasher.data_map = train_data;
		 
      // hasher.normalizer.updateStatistics(train_data.values());
  
      /* normalize training and test set */
      hasher.normalize(train_data.values());
      hasher.normalize(test_data.values());

      /* build hash table */
      hasher.buildHierarchicalHashTree(train_data);
   	    
      /* initialize tet histogram distance */
   
      TetHistogramDistance distance_computer = hasher.distance_computer;
      
      Set keys = test_data.keySet();
      for (Iterator<Integer> it=test_data.keySet().iterator();it.hasNext();)
      {
	 Integer nexttestid = it.next();
	 TetHistogram nexthisto = test_data.get(nexttestid);
	 System.out.println("test object: " + nexttestid);
	 System.out.println("Histogram:");
	 System.out.println(nexthisto.toFormattedString());
	 Vector<Integer> neighbors = hasher.retrieveNeighbors(nexthisto);
	 System.out.print("test actor " + nexttestid + '\n' + "  NEIGHBORS: ");

	 Double[] neighdists = new Double[neighbors.size()];
	 
	 Integer nextnnid;
	 Double nextdist;
	 for (int i=0;i<neighbors.size();i++)
          {
	      nextnnid = neighbors.elementAt(i);
	      nextdist = distance_computer.distance(train_data.get(nextnnid),test_data.get(nexttestid));
	      neighdists[i]=nextdist;
          }
	 for (int i=0;i<neighdists.length;i++)
	     System.out.print(neighdists[i] + ",");
	 System.out.println();
	 
	 System.out.print("ALLPOINTS:");
	 Double[] alldists = new Double[train_data.keySet().size()];
	 int ii = 0;
	 for (Iterator<Integer> itt=train_data.keySet().iterator();itt.hasNext();ii++)
	     {
		 nextnnid = itt.next();
		 alldists[ii]=distance_computer.distance(train_data.get(nextnnid),test_data.get(nexttestid));
	     }
	 Arrays.sort(alldists);
	 for (int i=0;i<alldists.length;i++)
	     System.out.print(alldists[i] + ",");

	 System.out.println();
	 System.out.print("RANKS:  ");
	 int k=0;
	 for (int i=0;i<neighdists.length;i++){
	     while(alldists[k]<neighdists[i])
		 k++;
	     System.out.print(k+1 + ",");
	 }
	 System.out.println();
      }     
      
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

class DistanceStatisticsNNCommandLineOptions {


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

  public DistanceStatisticsNNCommandLineOptions(String[] args)
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
    System.out.println("Usage:\n\ttet.rnn.DistanceStatistics [options] <database> <login> <password> <tetfile> <trainfile> <testfile> <outfile>");
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
