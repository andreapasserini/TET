package tet.rnn;

import java.util.*;
import java.sql.*;
import java.io.*;
import tet.*;

public class rnn_Tet extends Tet{

    ActivationFunction activation;

    public rnn_Tet()
    {

    }

    public rnn_Tet(Type t, TreeSet<Variable> freevars)
    {
        super(t, freevars);
    }

    public rnn_Tet(rnn_Tet dtet)
    {
        super((Tet)dtet);
    }

    public rnn_Tet(FileReader in) throws Exception
    {
        this((new BufferedReader(in)).readLine());
    }

    public rnn_Tet(String tetstring) throws Exception{

        this(new StringTokenizer(tetstring,"{[(,)]}",true));
    }

    public rnn_Tet(StringTokenizer tokenizer) throws Exception{

        String token = tokenizer.nextToken();
        if (token.equals("("))
            parseCompactTet(tokenizer);
        else if (token.equals("{"))
            parseVerboseTet(tokenizer);
        else
            throw new Exception("Malformed TET string, starting with '" + token + "'"); 
    }
    
    public void parseCompactTet(StringTokenizer tokenizer) throws Exception{

        String buf;

        // recover activation function
        activation = readActivationFunction(tokenizer.nextToken(")"));

        if(!(buf = tokenizer.nextToken("[(,)]")).equals(")"))
            throw new Exception("Malformed rnn TET string, expected ')', found '" + buf + "'"); 

        if(!(buf = tokenizer.nextToken()).equals("["))
            throw new Exception("Malformed rnn TET string, expected '[', found '" + buf + "'"); 

        // recover Type
        root = new Type(tokenizer);
        
        // initialize children vector
        children = new Vector<TetChild>();

        while(!(tokenizer.nextToken().equals("]"))){ // end of both recursion and children list
            buf = tokenizer.nextToken() + tokenizer.nextToken();
            if(!buf.equals("[("))
                throw new Exception("Malformed rnn TET string, expected '[(', found '" + buf + "'"); 
            /* recover edge labels */
            TreeSet<Variable> edgevariables = new TreeSet<Variable> ();
            do{
                buf = tokenizer.nextToken();
                if(!buf.equals(")") && !buf.equals(","))  
                    edgevariables.add(new Variable(buf));
            } while(!(buf.equals(")"))); // end of edge labels
            if(!(buf = tokenizer.nextToken()).equals(","))
                throw new Exception("Malformed rnn TET string, expected ',', found '" + buf + "'"); 
            /* recursively run parser on child */
            rnn_Tet childtet = new rnn_Tet(tokenizer);
            /* add child to current Tet's children */
            children.add(new TetChild(childtet,edgevariables));
            if(!(buf = tokenizer.nextToken()).equals("]"))
               throw new Exception("Malformed rnn TET string, expected ']', found '" + buf + "'"); 
        }
        /* set the free variables */
        setFreeVars();
    }

    /* Verbose rnn Tet syntax:
    <actfunc> = (logistic,b0,b1,...,bm) || (identity)
    <type> = (<atom>,...,<atom>)
    <atom> = relation(arg,...,arg) || <>(arg,arg)
    <varlist> = (V1,..Vn)
    
    {NODE {FUNCTION <actfunc>}
      {TYPE <type>}
      {CHILD <varlist> <rnntet>}
      {CHILD <varlist> <rnntet>}
    }
    */
    public void parseVerboseTet(StringTokenizer tokenizer) throws Exception{

        String buf;

        if(!tokenizer.nextToken().equals("NODE") ||
            !tokenizer.nextToken().equals("{") ||
            !tokenizer.nextToken().equals("FUNCTION") ||
            !tokenizer.nextToken().equals("("))   
            throw new Exception("Malformed rnn TET string: expected NODE{FUNCTION(");

        // recover activation function
        activation = readActivationFunction(tokenizer.nextToken(")"));

        if(!tokenizer.nextToken("{[(,)]}").equals(")") ||
            !tokenizer.nextToken().equals("}"))
            throw new Exception("Malformed rnn TET string"); 

        if(!tokenizer.nextToken().equals("{") ||
            !tokenizer.nextToken().equals("TYPE"))
            throw new Exception("Malformed rnn TET string");

        // recover Type
        root = new Type(tokenizer);
        
        if(!tokenizer.nextToken().equals("}"))
            throw new Exception("Malformed rnn TET string");

        // initialize children vector
        children = new Vector<TetChild>();


        while(!(tokenizer.nextToken().equals("}"))){ // end of both recursion and children list
            if(!tokenizer.nextToken().equals("CHILD") ||
                !tokenizer.nextToken().equals("("))
                throw new Exception("Malformed rnn TET string");

            /* recover edge labels */
            TreeSet<Variable> edgevariables = new TreeSet<Variable> ();
            do{
                buf = tokenizer.nextToken();
                if(!buf.equals(")") && !buf.equals(","))  
                    edgevariables.add(new Variable(buf));
            } while(!(buf.equals(")"))); // end of edge labels

            /* recursively run parser on child */
            rnn_Tet childtet = new rnn_Tet(tokenizer);

            /* add child to current Tet's children */
            children.add(new TetChild(childtet,edgevariables));

            if(!tokenizer.nextToken().equals("}"))
               throw new Exception("Malformed rnn TET string");
        }

        /* set the free variables */
        setFreeVars();
    }

    public ActivationFunction readActivationFunction(String buf) throws Exception
    {
        String[] splitted = buf.split(",");     
        String activationtype = splitted[0];

        if (activationtype.equals("identity"))
            return new Identity(splitted);
        else if (activationtype.equals("logistic"))
            return new LogisticRegression(splitted);
        else
            throw new Exception("Unkown activation function " + activationtype);
    }

    public float prior_d() throws Exception
    {
        throw new Exception("prior_d not implemented in rnn_Tet");
    }


    public float compute_d(RelStructure rstruc, HashMap<String, Object> boundvalues) throws Exception
    {
        if(!this.root.isEmptyType()){

            /* execute root type query */
            Vector<HashMap<String, Object>> res = 
                rstruc.queryResult(rstruc.typeValueQuery(this.root,
                                                         TetUtilities.strings2Variables(boundvalues.keySet()),
                                                         boundvalues, null, false));

            /* check if boundvalues satisfy the root type */
            if(res.size() == 0)
                return prior_d(); /* otherwise return prior proportion */
            
            /* check that all root type variables are bound */
            if(res.size() > 1)
                throw new Exception("Trying to compute rnn_tet function without binding all free variables");
        }
         
        /* base step has input 1 */
        if (children.size() == 0)
            return 1;

        /* collect freevars to be bound */
        TreeSet<Variable> boundvars = TetUtilities.strings2Variables(boundvalues.keySet());

        /* instantiate structure for collecting inputs from lower levels */
        float[][] input = new float[children.size()][];
        
        /* iterate over root children */
        for(int i = 0; i < children.size(); i++){           
            /* get child tet */
            rnn_Tet dtetchild = (rnn_Tet)children.elementAt(i).getSubTree();
            /* execute type query */
            Vector<HashMap<String, Object>> res = rstruc.queryResult(rstruc.typeValueQuery(dtetchild.root, 
                                                                                           boundvars, boundvalues, null, false)); 
            /* instantiate structure for collecting inputs from ith branch */
            input[i] = new float[res.size()];

            /* iterate over query results */
            for(int j = 0; j < res.size(); j++)
                /* recursively update d passing new boundvalues map */ 
                input[i][j] = dtetchild.compute_d(rstruc, res.elementAt(j));        
        }

        return activation.forward(input);
    }

    public String Serialize(){
        
        StringBuffer buf = new StringBuffer(1024);
        
        buf.append("(" + activation.Serialize() + ")");
        buf.append("[" + root.toString());

        for(int i = 0; i < children.size(); i++)
            buf.append(",[" + children.elementAt(i).edgeLabelsToString() + "," 
                       + ((rnn_Tet)children.elementAt(i).getSubTree()).Serialize() + "]");
        buf.append("]");

        return buf.toString();      
    }

    public Pair<Float,TetHistogram> computeTetHistogram(RelStructure rstruc, HashMap<String, Object> boundvalues, int bins) throws Exception
    {   
        TetHistogram hist = new TetHistogram(bins, activation.minValue(), activation.maxValue());

        if(!this.root.isEmptyType()){

            /* execute root type query */
            Vector<HashMap<String, Object>> res = 
                rstruc.queryResult(rstruc.typeValueQuery(this.root,
                                                         TetUtilities.strings2Variables(boundvalues.keySet()),
                                                         boundvalues, null, false));

            /* check if boundvalues satisfy the root type */
            if(res.size() == 0)
                return new Pair(prior_d(), hist); /* otherwise return prior proportion */ 
            
            /* check that all root type variables are bound */
            if(res.size() > 1)
                throw new Exception("Trying to compute rnn_TetHistogram function without binding all free variables");
        }
         
        /* base step has input 1 */
        if (children.size() == 0){
            hist.fill(new Float(1));
            return new Pair(new Float(1), hist);
        }

        /* collect freevars to be bound */
        TreeSet<Variable> boundvars = TetUtilities.strings2Variables(boundvalues.keySet());

        /* instantiate structure for collecting inputs from lower levels */
        float[][] input = new float[children.size()][];
        
        /* iterate over root children */
        for(int i = 0; i < children.size(); i++)
        {           
            /* get child tet */
            rnn_Tet dtetchild = (rnn_Tet)children.elementAt(i).getSubTree();
            /* execute type query */
            Vector<HashMap<String, Object>> res = rstruc.queryResult(rstruc.typeValueQuery(dtetchild.root, 
                                                                                           boundvars, boundvalues, null, false)); 
            /* instantiate structure for collecting inputs from ith branch */
            input[i] = new float[res.size()];

            /* instantiate structure for collecting histograms from ith branch */
            TetHistogram[] histos = new TetHistogram[res.size()];

            /* iterate over query results */
            for(int j = 0; j < res.size(); j++)
            {
                /* recursively update d passing new boundvalues map */ 
                Pair<Float,TetHistogram> data = dtetchild.computeTetHistogram(rstruc, res.elementAt(j), bins);
                input[i][j] = data.first();
                histos[j] = data.second();
            }   

            // DEBUG
            //System.out.println(Arrays.toString(input[i]));

            /* update histogram child */
            if (histos.length > 0)
                hist.addChild(histos);
            else 
                /* add a zero-filled TetHistogram */
                hist.addChild(new TetHistogram(bins, activation.minValue(), activation.maxValue(), dtetchild));
        }

        /* compute activation */
        float forward = activation.forward(input);

        /* update histogram*/
        hist.fill(forward);
        
        // DEBUG
        //System.out.println(Float.toString(forward));

        return new Pair(forward, hist);
    }

    public static void main(String[] args)
    {
        
        rnn_TetCommandLineOptions options = new rnn_TetCommandLineOptions(args);
        
        try
        {
            /* load discriminant tet from file */
            String tetstring = Tet.tetString(options.tetfile);

            System.out.println("tetstring=" + tetstring);

            rnn_Tet tet = new rnn_Tet(tetstring);
            
            System.out.println("Read rnn Tet string: " + tet.Serialize());
            System.out.println("Tet Freevars = " + tet.freevars().toString());   

            /* create mysql relational structure which uses TD BU procedure */
            MySQLRelStructure relstruct = new MySQLRelStructure(options.database, options.login, options.password, true);
         
            /* create MySQLTestViewer if needed */
            MySQLTestViewer testviewer = (options.srcdb != null && options.testviewconfigfile != null) ? 
                new MySQLTestViewer(relstruct, options.srcdb, options.testviewconfigfile) : null;

            Vector<TetHistogram> histos = new Vector<TetHistogram>();

            /* parse object file and compute discriminant value for each object  */
                FileReader in = new FileReader(options.objfile);
            StreamTokenizer st = new StreamTokenizer(in);
            st.eolIsSignificant(true);
            st.whitespaceChars(32,47);
            st.whitespaceChars(58,63);

            FileWriter out = new FileWriter(options.outfile, false);

            while(st.nextToken() != StreamTokenizer.TT_EOF)
            {
                HashMap<String, Object> objmap = new HashMap<String, Object>();

                System.out.println("Processing example: ");
                String example = "";
                do
                {
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
                                
                if(options.compute_tet_histogram)
                {
                    histos.add(tet.computeTetHistogram(relstruct, objmap, 
                                               options.histogram_bin_number).second());              
                    out.write(histos.lastElement().toFormattedString()); 
                }
                else
                    /* print d value */
                    out.write(Float.toString(tet.compute_d(relstruct, objmap)));

                /*  compute tet value and print it if needed */
                if(options.compute_tet_value)
                {
                    Value value = tet.calculateValue(relstruct, objmap, !options.compute_tet_false_value);
                    out.write("\t" + value);
                }

                /* print newline */
                out.write("\n");
                out.flush();

                /* remove test view for current example if needed */
                if(testviewer != null)
                    testviewer.removeTestView(objmap);
            }

            /* compute and print normalized histograms */
            System.out.println("Normalizing histograms");
            out.write("\n\nNormalized histograms\n\n");
            for(int i = 0; i < histos.size(); i++)
            {
                histos.elementAt(i).normalize();
                out.write(histos.elementAt(i).toFormattedString()); 
            }
                
            out.close();
            relstruct.Close();
        }
        catch (Exception e)     
        {
            e.printStackTrace();        
        }    
    }

}

class rnn_TetCommandLineOptions {

    public String database;
    public String login; 
    public String password; 
    public String tetfile; 
    public String objfile; 
    public String outfile; 
        
    public String srcdb = null;
    public String testviewconfigfile = null;

    public boolean compute_tet_value = false;
    public boolean compute_tet_false_value = false;
    public boolean compute_tet_histogram = false;
    public int histogram_bin_number = 10;   

    public rnn_TetCommandLineOptions(String[] args)
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
            case 'v':
                compute_tet_value = true;
                break;          
            case 'f':
                compute_tet_false_value = true;
                break;          
            case 'H':
                compute_tet_histogram = true;
                break;
            case 'b':
                histogram_bin_number = Integer.parseInt(options[++pos]);
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
        System.out.println("Usage:\n\ttet.rnn.rnn_Tet [options] <database> <login> <password> <tetfile> <objfile> <outfile>");
        System.out.println("Options:");
        System.out.println("\t-c <int>   \tconfig file for test view (default=null)");
        System.out.println("\t-d <string>\tsrc db for test view (default=null)");
        System.out.println("\t-v         \tcompute tet value (default=false)");
        System.out.println("\t-f         \tcompute tet false value (default=false)");
        System.out.println("\t-H         \tcompute tet histogram (default=false)");
        System.out.println("\t-b <int>   \thistogram bin number (default=10)"); 
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
            + "compute_tet_value=" + compute_tet_value + "\n"
            + "compute_tet_false_value=" + compute_tet_false_value + "\n"
            + "compute_tet_histogram=" + compute_tet_histogram + "\n"
            + "histogram_bin_number=" + histogram_bin_number + "\n";        
    }
}
