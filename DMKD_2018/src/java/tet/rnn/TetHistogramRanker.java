package tet.rnn;

import java.util.*;
import java.sql.*;
import java.io.*;
import tet.*;
import myio.*;

public class TetHistogramRanker {

    protected TetHistogramDistance tet_histogram_distance;

    public TetHistogramRanker(Tet tet, int bins, double threshold, double extra_mass_penalty, double decay, 
                              boolean marginalized_emd, boolean add_count_distance, EMDNormalization emd_normalization)
    {
        this(tet, 0, bins, threshold, extra_mass_penalty, decay, marginalized_emd, add_count_distance, emd_normalization);
    }

    public TetHistogramRanker(Tet tet, int depth, int bins, double threshold, double extra_mass_penalty, double decay,
                              boolean marginalized_emd, boolean add_count_distance, EMDNormalization emd_normalization)
    {
        tet_histogram_distance = new TetHistogramDistance(tet, depth, bins, threshold, extra_mass_penalty, decay, 
                                                          marginalized_emd, add_count_distance, emd_normalization);
    }

    public TetHistogramRanking rank(TetHistogram hist, Vector<TetHistogram> histos) throws Exception
    {
        Vector<Pair<Integer, TetHistogram>> histpairs = new Vector<Pair<Integer, TetHistogram>>(); 

        for(int i = 0; i < histos.size(); i++)
            histpairs.add(new Pair(i, histos.elementAt(i)));

        return rank(hist, histpairs, 0);
    }

    public TetHistogramRanking rank(TetHistogram hist, Vector<Pair<Integer, TetHistogram>> histos, int depth) throws Exception
    {
        /* partial ranking for current depth distance */
        TreeMap<Double, Vector<Pair<Integer, TetHistogram>>> partial_ranking = new TreeMap<Double, Vector<Pair<Integer, TetHistogram>>>();
    
        /* compute partial ranking */
        for(int i = 0; i < histos.size(); i++){
            double currdist = tet_histogram_distance.distance(hist, histos.elementAt(i).second(), depth);                   
            if(!partial_ranking.containsKey(currdist))
                partial_ranking.put(currdist, new Vector<Pair<Integer, TetHistogram>>());
            partial_ranking.get(currdist).add(histos.elementAt(i));
        }
        
        /* hierarchical ranking */
        TetHistogramRanking ranking = new TetHistogramRanking();

        /* compute hierarchical ranking by recursion over TetHistogram depth */
        Set<Map.Entry<Double, Vector<Pair<Integer, TetHistogram>>>> entryset = partial_ranking.entrySet();
        for (Iterator<Map.Entry<Double, Vector<Pair<Integer, TetHistogram>>>> i = entryset.iterator(); i.hasNext();){
            Map.Entry<Double, Vector<Pair<Integer, TetHistogram>>> entry = i.next();
            if(entry.getValue().size() > 1)
                ranking.update(entry.getKey(), rank(hist, entry.getValue(), depth+1));
            else
                ranking.update(entry.getKey(), new TetHistogramRanking(entry.getValue().elementAt(0)));
        }

        return ranking;
    }

    public static void main(String[] args)
    {

        TetHistogramRankerCommandLineOptions options = new TetHistogramRankerCommandLineOptions(args);

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

            /* initialize tet histogram ranker */
            TetHistogramRanker ranker = new TetHistogramRanker(tet, options.bins,                                                               
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

            while(st.nextToken() != StreamTokenizer.TT_EOF)
            {
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

                TetHistogram histo = tet.computeTetHistogram(relstruct, objmap, options.bins).second();
                histo.normalize();
                tet_histos.add(histo);                  

                /* remove test view for current example if needed */
                if(testviewer != null)
                    testviewer.removeTestView(objmap);
            }
            
            /* compute ranking */
            TetHistogramRanking ranking = ranker.rank(tet_histos.elementAt(0), tet_histos);
            
            /* print ranking */
            FileWriter out = new FileWriter(options.outfile, false);
            out.write(ranking.toFormattedString());
            out.close();
            relstruct.Close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}

class TetHistogramRankerCommandLineOptions {


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
    public boolean marginalized_emd = false;
    public boolean add_count_distance = false;
    public EMDNormalization emd_normalization = EMDNormalization.NONE;

    public TetHistogramRankerCommandLineOptions(String[] args)
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
        System.out.println("Usage:\n\ttet.rnn.TetHistogramRanker [options] <database> <login> <password> <tetfile> <objfile> <outfile>");
        System.out.println("Options:");
        System.out.println("\t-c <int>   \tconfig file for test view (default=null)");
        System.out.println("\t-d <string>\tsrc db for test view (default=null)");
        System.out.println("\t-b <int>   \tnumber of bins (default=10)");
        System.out.println("\t-t <double>\tdistance threshold (default=0 no threshold)");
        System.out.println("\t-p <double>\textra mass penalty (default=-1)");
        System.out.println("\t-w <double>\tlevelwise weight decay (default=0.5)");
        System.out.println("\t-M         \tuse marginalized EMD (default=false)");
        System.out.println("\t-C         \tadd count distance (default=false)");
        System.out.println("\t-N <int>   \tEMD normalization: NONE=0, BOOLEAN=1, NUMERIC=2 (default=NONE)");
        System.out.println("\t-M         \tuse Marginalized EMD (default=false)");
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
            + "emd_normalization" + emd_normalization + "\n"
            + "marginalized_emd=" + marginalized_emd + "\n"
            + "add_count_distance=" + add_count_distance + "\n";
    }
}
