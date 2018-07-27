package tet.rnn;

import java.util.*;
import java.sql.*;
import java.io.*;
import tet.*;
import myio.*;
import FastEMD.*;

public class TetHistogramDistance {

    public int depth;
    public int bins;
    public double extra_mass_penalty = -1;
    public double decay = 0.5;
    public double[][] ground_distance_matrix;
    public Vector<TetHistogramDistance> children;
    public boolean marginalized_emd;
    public boolean add_count_distance;
    public EMDNormalization emd_normalization = EMDNormalization.NONE;

    public TetHistogramDistance(Tet tet, int bins, double threshold, double extra_mass_penalty, double decay, 
                                boolean marginalized_emd, boolean add_count_distance, EMDNormalization emd_normalization)
    {
        this(tet, 0, bins, threshold, extra_mass_penalty, decay, marginalized_emd, add_count_distance, emd_normalization);
    }
    
    public TetHistogramDistance(Tet tet, int depth, int bins, double threshold, double extra_mass_penalty, double decay, 
                                boolean marginalized_emd, boolean add_count_distance, EMDNormalization emd_normalization)
    {
        this.depth = depth;
        this.bins = bins;
        this.extra_mass_penalty = extra_mass_penalty;
        this.decay = decay;
        this.marginalized_emd = marginalized_emd;
        this.add_count_distance = add_count_distance;
        this.emd_normalization = emd_normalization;

        ground_distance_matrix = computeGroundDistanceMatrix(threshold);
        children = new Vector<TetHistogramDistance>();
        
        // DEBUG
        // System.out.println(StringOps.matrixToString(ground_distance_matrix));
        
        try{
            for(int i = 0; i < tet.getNumChildren(); i++)
                children.add(new TetHistogramDistance(tet.getChild(i).getSubTree(), depth+1, bins, threshold, 
                                                 extra_mass_penalty, decay, marginalized_emd, add_count_distance, emd_normalization));
        } catch(Exception e){e.printStackTrace();}
        
    }

    public boolean isLeafDistance()
    {
        return children.size() == 0;
    }


    // This is the complete distance between histograms that employs the decay factor (eq. (9) in notes)
    public double distance(TetHistogram x, TetHistogram y) throws Exception
    {
        if(children.size() != x.children.size())
            throw new Exception("TetHistrogram structure does not match expected one in TetHistogramDistance");
        
        double local_distance = local_distance(x, y);
        
        if(decay == 0 || children.size() == 0)
            return local_distance;
        
        double lower_distance = 0;
        for(int i = 0; i < children.size(); i++)
            lower_distance += children.elementAt(i).distance(x.children.elementAt(i),y.children.elementAt(i));
        lower_distance /= children.size();
        
        return local_distance + decay * lower_distance;
    }

    public double distance(MultiHistogram x, MultiHistogram y)
    {
        try 
        {       
            double dist = 0;
            if (x.mass == 0 || y.mass == 0){
                if (x.mass == 0 && y.mass == 0){
                    //DEBUG
                    //System.out.println("Warning: empty histograms, returning distance 0");
                    return 0.0;
                }
                else{   
                    //DEBUG
                    //System.out.println("Warning: empty vs non-empty histogram, returning distance 1");
                    return 1.0;
                }
            }
            if (marginalized_emd)
                dist = marginalizedDistance(x,y);                       
            else
                dist = emd_hat.dist_gd_metric(x.tensor.data, y.tensor.data, ground_distance_matrix, extra_mass_penalty, null);          

            // emd normalization
            if (emd_normalization != EMDNormalization.NONE)
            {
                int effective_dims = x.dimensions;
                if (isLeafDistance() && emd_normalization == EMDNormalization.BOOLEAN)
                    // if BOOLEAN data, leaf histogram has one effective dimension less (only true=1 values can occur)
                    effective_dims -= 1;
                // max hamming distance is obtained moving for x.bins-1 steps of each of the effective_dims-1 dimensions
                dist /= effective_dims*(x.bins-1);      
            }
            if (add_count_distance)
            {
                //DEBUG Manfred ->
                //System.out.println("emd: " + dist + " count: " + count_distance(x, y));
                //<- Manfred
                dist = dist/2 + count_distance(x, y)/2;
            }
            return dist;
        
        } catch (Exception e) 
        {
            e.printStackTrace();
            return 0;
        }
    }

    public double count_distance(MultiHistogram x, MultiHistogram y)
    {
        double x_mass = x.mass;
        double y_mass = y.mass;
        //DEBUG
        //System.out.println("x_mass: " + x_mass + " y_mass: " + y_mass);
        return 1. - (Math.min(x_mass,y_mass))/Math.sqrt(x_mass*y_mass);
    }

    public double local_distance(TetHistogram x, TetHistogram y)
    {
        return distance(x.histogram, y.histogram);
    }

    // Marco -- NOTE: this function here computes an average over branches, I do not know where/whether it is actually used...
    public double distance(TetHistogram x, TetHistogram y, int depth) throws Exception
    {
        if(depth == 0)
            return local_distance(x, y);

        if(children.size() != x.children.size())
            throw new Exception("TetHistrogram structure does not match expected one in TetHistogramDistance");

        double lower_distance = 0;
        for(int i = 0; i < children.size(); i++)
            lower_distance += children.elementAt(i).distance(x.children.elementAt(i), y.children.elementAt(i), depth-1);
        return lower_distance /= children.size();
    }

    public double marginalizedDistance(MultiHistogram x, MultiHistogram y)
        throws Exception
    {       
        double sum = 0;

        double[] cum_x = x.getMarginalCumulatives();
        double[] cum_y = y.getMarginalCumulatives();   

        if (cum_x.length != cum_y.length)
            throw new Exception(String.format("ERROR, cum_x and cum_y have different lengths: %d != %d", cum_x.length, cum_y.length));

        for(int i = 0; i < cum_x.length; i++)
            sum += Math.abs(cum_x[i] - cum_y[i]);
  
        return sum;
    }

    protected double[][] computeGroundDistanceMatrix(double threshold)
    {
        /* computes thresholded l1 distance */
        int size = (int)Math.pow(bins,depth+1);
        double[][] m = new double[size][size];

        int dim = depth;
        int slicesize = (int)Math.pow(bins,dim);

        for(int i = 0; i < bins; i++)
            for(int j = 0; j < bins; j++)
                computeGroundDistanceMatrix(m, dim, threshold, i*slicesize, j*slicesize, Math.abs(i-j));

        return m;
    }

    protected void computeGroundDistanceMatrix(double[][] m, int dim, double threshold, int i, int j, double currdist)
    {
        if(dim == 0)
        {
            m[i][j] = Math.min(currdist,threshold);
            return;
        }

        dim = dim -1;
        int slicesize = (int)Math.pow(bins,dim);

        for(int i1 = 0; i1 < bins; i1++)
            for(int j1 = 0; j1 < bins; j1++)
                computeGroundDistanceMatrix(m, dim, threshold, i+i1*slicesize, j+j1*slicesize, currdist+Math.abs(i1-j1));

    }

    public static void main(String[] args)
    {

        TetHistogramDistanceCommandLineOptions options = new TetHistogramDistanceCommandLineOptions(args);

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
            TetHistogramDistance distance_computer = new TetHistogramDistance(tet, options.bins,
                                                                              options.threshold,
                                                                              options.extra_mass_penalty,
                                                                              options.decay,
                                                                              options.marginalized_emd,
                                                                              options.add_count_distance,
                                                                              options.emd_normalization);
            
            Vector<TetHistogram> tet_histos = new Vector<TetHistogram>();

            /* parse object file and compute discriminant value for each object  */
            FileReader in = new FileReader(options.objfile);
            StreamTokenizer st = new StreamTokenizer(in);
            st.eolIsSignificant(true);
            st.whitespaceChars(32,47);
            st.whitespaceChars(58,63);

            while(st.nextToken() != StreamTokenizer.TT_EOF){

                HashMap<String, Object> objmap = new HashMap<String, Object>();

                System.out.println("Processing example: ");
                String example = "";
                do{
                    String var = st.sval;
                    st.nextToken();
                    String val = st.sval;
                    if(st.ttype == st.TT_NUMBER)
                        val = String.valueOf((int)st.nval);
                    objmap.put(var,new RelObject(val));
                    example += var + "=" + val + " ";
                }while(st.nextToken() != StreamTokenizer.TT_EOL);

                System.out.println(example);

                /* add test view for current example if needed */
                if(testviewer != null)
                    testviewer.addTestView(objmap);

                tet_histos.add(tet.computeTetHistogram(relstruct, objmap, options.bins).second());

                /* remove test view for current example if needed */
                if(testviewer != null)
                    testviewer.removeTestView(objmap);
            }

            /* compute distance matrix */
            int N = tet_histos.size();
            /* MARCO */
            for(int i = 0; i < N; i++)
                        tet_histos.elementAt(i).normalize();
            
            double[][] distance_matrix = new double[N][N];
            for(int i = 0; i < N; i++){
                distance_matrix[i][i] = 0.;
                for(int j = i+1; j < N; j++)
                    distance_matrix[i][j] = distance_matrix[j][i] = distance_computer.distance(tet_histos.elementAt(i), tet_histos.elementAt(j));
            }

            /* print distance matrix */
            FileWriter out = new FileWriter(options.outfile, false);
            out.write(StringOps.matrixToString(distance_matrix));
            out.close();
            relstruct.Close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}

class TetHistogramDistanceCommandLineOptions {


    public String database;
    public String login;
    public String password;
    public String tetfile;
    public String objfile;
    public String outfile;

    public String srcdb = null;
    public String testviewconfigfile = null;

    public int bins = 10;
    public double threshold = 0;
    public double extra_mass_penalty = -1;
    public double decay = 0.5;
    public EMDNormalization emd_normalization = EMDNormalization.NONE;
    public boolean marginalized_emd = false;
    public boolean add_count_distance = false;

    public TetHistogramDistanceCommandLineOptions(String[] args)
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
        objfile = args[pos++];
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
        System.out.println("Usage:\n\ttet.rnn.TetHistogramDistance [options] <database> <login> <password> <tetfile> <objfile> <outfile>");
        System.out.println("Options:");
        System.out.println("\t-c <int>   \tconfig file for test view (default=null)");
        System.out.println("\t-d <string>\tsrc db for test view (default=null)");
        System.out.println("\t-b <int>   \tnumber of bins (default=10)");
        System.out.println("\t-t <double>\tdistance threshold (default=0 no threshold)");
        System.out.println("\t-p <double>\textra mass penalty (default=-1)");
        System.out.println("\t-w <double>\tlevelwise weight decay (default=0.5)");
        System.out.println("\t-N <int>   \tEMD normalization: NONE=0, BOOLEAN=1, NUMERIC=2 (default=NONE)");
        System.out.println("\t-M         \tuse marginalized EMD (default=false)");
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
            + "objfile=" + objfile + "\n"
            + "outfile=" + outfile + "\n"
            + "srcdb=" + srcdb + "\n"
            + "testviewconfigfile=" + testviewconfigfile + "\n"
            + "bins=" + bins + "\n"
            + "threshold=" + threshold + "\n"
            + "extra_mass_penalty=" + extra_mass_penalty + "\n"
            + "decay=" + decay + "\n"
            + "marginalized_emd=" + marginalized_emd + "\n"
            + "emd_normalization" + emd_normalization + "\n"
            + "add_count_distance=" + add_count_distance + "\n";          
    }
}
