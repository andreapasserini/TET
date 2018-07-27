package tet;

import java.util.*;
import mymath.*;
import java.sql.*;
import java.io.*;
import tet.learner.*;

public class discriminant_Tet extends Tet{

    float weight = 0;
    
    float threshold = 1;
    
    Dataset dataset = null;

    DiscriminantValuesCache cache = new DiscriminantValuesCache();

    protected class DiscriminantValuesCache
    {
        Dataset dataset = null;
        boolean freezed = false;
        
        public DiscriminantValuesCache()
        {
            this(null,false);
        }
        
        public DiscriminantValuesCache(Dataset dataset)
        {
            this(dataset, false);
        }
        
        public DiscriminantValuesCache(Dataset dataset, boolean freezed)
        {
            this.dataset = dataset;
            this.freezed = freezed;
        }

        public DiscriminantValuesCache(DiscriminantValuesCache cache)
        {
            this.dataset = cache.dataset;
            this.freezed = cache.freezed;
        }
        
        public void set(Dataset dataset)
        { 
            /* cleanup previous dataset if any */
            clear();

            this.dataset = dataset;
        }

        public Dataset get()
        {
            return this.dataset;
        }

        public boolean isFreezed()
        {
            return freezed;
        }
        
        public void freeze()
        {
            freezed = true;
        }

        public void clear()
        {
            if(dataset != null)
                dataset.cleanup();         
            dataset = null;
            freezed = false;
        }
    }

    public discriminant_Tet()
    {

    }

    public discriminant_Tet(Type t, TreeSet<Variable> freevars)
    {
        super(t, freevars);
    }

    public discriminant_Tet(Type t, TreeSet<Variable> freevars, Dataset dataset)
    {
        this(t, freevars);
        
        this.dataset = dataset;
    }

    public discriminant_Tet(discriminant_Tet dtet)
    {
        super((Tet)dtet);

        this.weight = dtet.weight;
    
        this.threshold = dtet.threshold;

        this.dataset = dtet.dataset;
        
        DiscriminantValuesCache cache = new DiscriminantValuesCache(dtet.cache);
    }

    public discriminant_Tet(FileReader in) throws Exception
    {
        this((new BufferedReader(in)).readLine());
    }

    public discriminant_Tet(String tetstring) throws Exception{

        this(new StringTokenizer(tetstring,"{[(,)]}",true));

    }

    public discriminant_Tet(StringTokenizer tokenizer) throws Exception{

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

        // recover weight
        weight = Float.parseFloat(tokenizer.nextToken());

        if(!(tokenizer.nextToken().equals(",")))
            throw new Exception("Malformed discriminant TET string"); 

        // recover threshold
        threshold = Float.parseFloat(tokenizer.nextToken());

        if(!(tokenizer.nextToken().equals(")")))
            throw new Exception("Malformed discriminant TET string"); 

        if(!(tokenizer.nextToken().equals("[")))
            throw new Exception("Malformed discriminant TET string"); 

        // recover Type
        root = new Type(tokenizer);
        
        // initialize children vector
        children = new Vector<TetChild>();

        while(!(tokenizer.nextToken().equals("]"))){ // end of both recursion and children list
            buf = tokenizer.nextToken() + tokenizer.nextToken();
            if(!buf.equals("[("))
                throw new Exception("Malformed TET string");
            /* recover edge labels */
            TreeSet<Variable> edgevariables = new TreeSet<Variable> ();
            do{
                buf = tokenizer.nextToken();
                if(!buf.equals(")") && !buf.equals(","))  
                    edgevariables.add(new Variable(buf));
            } while(!(buf.equals(")"))); // end of edge labels
            if(!(tokenizer.nextToken().equals(",")))
                throw new Exception("Malformed discriminant TET string");
            /* recursively run parser on child */
            discriminant_Tet childtet = new discriminant_Tet(tokenizer);
            /* add child to current Tet's children */
            children.add(new TetChild(childtet,edgevariables));
            if(!tokenizer.nextToken().equals("]"))
               throw new Exception("Malformed discriminant TET string");
        }
        /* set the free variables */
        setFreeVars();
    }

    
    /* Verbose discriminant Tet syntax:
    <actfunc> = (logistic,b0,b1,...,bm) || (identity)
    <type> = (<atom>,...,<atom>)
    <atom> = relation(arg,...,arg) || <>(arg,arg)
    <varlist> = (V1,..Vn)
    
    {NODE {WEIGHT (float,float)}
      {TYPE <type>}
      {CHILD <varlist> <rnntet>}
      {CHILD <varlist> <rnntet>}
    }
    */
    public void parseVerboseTet(StringTokenizer tokenizer) throws Exception{

        String buf;

        if(!tokenizer.nextToken().equals("NODE") ||
            !tokenizer.nextToken().equals("{") ||
            !tokenizer.nextToken().equals("WEIGHT") ||
            !tokenizer.nextToken().equals("("))   
            throw new Exception("Malformed TET string");

        // recover weight
        weight = Float.parseFloat(tokenizer.nextToken());

        if(!(tokenizer.nextToken().equals(",")))
            throw new Exception("Malformed discriminant TET string"); 

        // recover threshold
        threshold = Float.parseFloat(tokenizer.nextToken());

        if(!tokenizer.nextToken().equals(")") ||
            !tokenizer.nextToken().equals("}"))
            throw new Exception("Malformed discriminant TET string"); 

        if(!tokenizer.nextToken().equals("{") ||
            !tokenizer.nextToken().equals("TYPE"))
            throw new Exception("Malformed TET string");

        // recover Type
        root = new Type(tokenizer);
        
        if(!tokenizer.nextToken().equals("}"))
            throw new Exception("Malformed TET string");

        // initialize children vector
        children = new Vector<TetChild>();


        while(!(tokenizer.nextToken().equals("}"))){ // end of both recursion and children list
            if(!tokenizer.nextToken().equals("CHILD") ||
                !tokenizer.nextToken().equals("("))
                throw new Exception("Malformed TET string");

            /* recover edge labels */
            TreeSet<Variable> edgevariables = new TreeSet<Variable> ();
            do{
                buf = tokenizer.nextToken();
                if(!buf.equals(")") && !buf.equals(","))  
                    edgevariables.add(new Variable(buf));
            } while(!(buf.equals(")"))); // end of edge labels

            /* recursively run parser on child */
            discriminant_Tet childtet = new discriminant_Tet(tokenizer);

            /* add child to current Tet's children */
            children.add(new TetChild(childtet,edgevariables));

            if(!tokenizer.nextToken().equals("}"))
               throw new Exception("Malformed TET string");
        }

        /* set the free variables */
        setFreeVars();
    }
    
    


    public void setWeight(float weight)
    {
        this.weight = weight;
    }

    public float getWeight()
    {
        return this.weight;
    }

    public void setThreshold(float threshold)
    {
        this.threshold = threshold;
    }

    public float getThreshold()
    {
        return this.threshold;
    }

    public void resetThresholds()
    {
        /* reset root threshold */
        this.threshold = 1;

        /* iterate over root children */
        for(int i = 0; i < children.size(); i++)    
            ((discriminant_Tet)children.elementAt(i).subtree).resetThresholds();
    }

    public void resetWeights()
    {
        /* reset root threshold */
        this.weight = 1;

        /* iterate over root children */
        for(int i = 0; i < children.size(); i++)    
            ((discriminant_Tet)children.elementAt(i).subtree).resetWeights();   
    }

    public Dataset getDataset()
    {
        return dataset;
    }

    public Pair<Double,Double> compute_d_pair(RelStructure rstruc, HashMap<String, Object> boundvalues) throws Exception
    {
        /* initialize d */
        float dpos = weight;
        float dneg = 1-weight;

        /* collect freevars to be bound */
        TreeSet<Variable> boundvars = TetUtilities.strings2Variables(boundvalues.keySet());

        /* iterate over root children */
        for(int i = 0; i < children.size(); i++){           
            /* get child tet */
            discriminant_Tet dtetchild = (discriminant_Tet)children.elementAt(i).subtree;
            /* execute type query */
            Vector<HashMap<String, Object>> res = rstruc.queryResult(rstruc.typeValueQuery(dtetchild.root, 
                                                                                           boundvars, boundvalues, null, false)); 
            /* iterate over query results */
            for(int j = 0; j < res.size(); j++){
                /* recursively update d passing new boundvalues map */ 
                Pair<Double,Double> newd = (dtetchild.compute_d_pair(rstruc, res.elementAt(j)));
                dpos *= newd.first().floatValue() / weight;
                dneg *= newd.second().floatValue() / (1-weight);
            }
        }

        /* create new pair */
        Pair<Double,Double> newpair = new Pair(new Double(dpos), new Double(dneg));

        /* DEBUG */
        //System.out.println("dpos=" + dpos + "\tdneg=" + dneg);
        
        return newpair; 
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
                return weight / (1-weight); /* otherwise return prior proportion */
            
            /* check that all root type variables are bound */
            if(res.size() > 1)
                throw new Exception("Trying to compute discriminant_tet function without binding all free variables");
        }
         
        Pair<Double,Double> dpair = compute_d_pair(rstruc, boundvalues);
        
        return (float)(dpair.first()/dpair.second());
    }

    public int compute_decision(RelStructure rstruc, HashMap<String, Object> boundvalues) throws Exception
    {
        return compute_decision(compute_d(rstruc, boundvalues));
    }

    public int compute_decision(float d)
    {
        if(d > threshold)
            return 1;
        return -1;
    }

    public String Serialize(){
        
        StringBuffer buf = new StringBuffer(1024);
        
        buf.append("(" + weight + "," + threshold + ")");
        buf.append("[" + root.toString());

        for(int i = 0; i < children.size(); i++)
            buf.append(",[" + children.elementAt(i).edgeLabelsToString() + "," 
                       + ((discriminant_Tet)children.elementAt(i).subtree).Serialize() + "]");
        buf.append("]");

        return buf.toString();      
    }
    
    public Dataset compute_d_pairs(TreeSet<Variable> bound_variables,
                                   StringCounter table_counter,
                                   Dataset parent_examples_dataset) throws Exception
    {
        /* check if cached dataset is valid */
        if(cache.isFreezed())
            return cache.get();

        /* init allvars with type variables */
        TreeSet<Variable> allvars = this.root.getVariables();

        /* merge with bound variables set */
        allvars.addAll(bound_variables);

        /* recover current examples dataset, either from stored one or computing it from scratch */
        Dataset curr_examples_dataset = getExamplesDataset(allvars, table_counter, parent_examples_dataset);

        /* initialize curr_value_dataset with weights */
        Dataset curr_value_dataset = curr_examples_dataset.getValueDataset(table_counter.next(), bound_variables, weight, 1-weight);
        
        /* iterate over root children */
        for(int i = 0; i < children.size(); i++){                   

            /* get child tet */
            discriminant_Tet dtetchild = (discriminant_Tet)children.elementAt(i).subtree;
                    
            /* recursively compute value dataset */
            Dataset child_value_dataset = dtetchild.compute_d_pairs(allvars, 
                                                                    table_counter,
                                                                    (parent_examples_dataset != null) ? curr_examples_dataset : null);
            /* update values dataset with recursive values */
            curr_value_dataset.aggregateValues(table_counter.next(), child_value_dataset, 
                                               bound_variables, weight, 1-weight, 
                                               Operator.TIMES, Operator.TIMES);
        }
                
        /* store values in cache */
        cache.set(curr_value_dataset);

        return curr_value_dataset;
    }

    public Float[] compute_ds(TreeSet<Variable> bound_variables,
                              StringCounter table_counter,
                              Dataset bound_dataset) throws Exception
    {   
        /* recover values dataset */
        Dataset values_dataset = compute_d_pairs(bound_variables, table_counter, bound_dataset);

        /* compute divisions */
        return values_dataset.computeValues();
    }

    public Float[] compute_ds(TreeSet<Variable> bound_variables, StringCounter table_counter) throws Exception
    {
        return compute_ds(bound_variables, table_counter, null);
    }
    
    public int[] compute_decisions(TreeSet<Variable> bound_variables,
                                   StringCounter table_counter,
                                   Dataset bound_dataset) throws Exception
    {   
        Float[] ds = compute_ds(bound_variables, table_counter, bound_dataset);
        
        return compute_decisions(ds);
    }

    public int[] compute_decisions(TreeSet<Variable> bound_variables,
                                   StringCounter table_counter) throws Exception
    {   
        return compute_decisions(bound_variables, table_counter, null);
    }

    public int[] compute_decisions(Float[] ds)
    {
        int[] decisions = new int[ds.length];
        
        for(int i = 0; i < ds.length; i++)
            decisions[i] = compute_decision(ds[i].floatValue());
        
        return decisions;
    }
    
    public void freezeCache() throws Exception
    {
        cache.freeze();

        // cleanup datasets recursively as they won't be used
        if(dataset != null)
            dataset.cleanup();

        try{
            for( int i = 0; i < children.size(); i++)
                ((discriminant_Tet)children.elementAt(i).getSubTree()).finalize();
        }catch(Throwable t){
            t.printStackTrace();
            System.exit(1);
        }
    }   

    public void cleanupNonFreezedCache() throws Exception
    {
        if(cache.isFreezed())
            return;
        
        cache.clear();

        for(int i = 0; i < children.size(); i++)
            ((discriminant_Tet)children.elementAt(i).getSubTree()).cleanupNonFreezedCache();
    }

    public void finalize() throws Throwable
    {
        if(dataset != null)
            dataset.cleanup();
        if(cache != null)
            cache.clear();

        for( int i = 0; i < children.size(); i++)
            ((discriminant_Tet)children.elementAt(i).getSubTree()).finalize();
    }

    protected Dataset getExamplesDataset(TreeSet<Variable> allvars,
                                       StringCounter table_counter,
                                       Dataset parent_examples_dataset) throws Exception
    {
        Dataset examples_dataset = null;
        
        /* current dataset has to be computed from scratch */
        if(parent_examples_dataset != null){
                        
            if(this.root.isEmptyType()) // the entire dataset will satisfy the empty type
                examples_dataset = parent_examples_dataset; 
            else
                /* collect dataset satisfying type and bound table variables */ 
                examples_dataset = parent_examples_dataset.getSatisfyingDataset(this.root, table_counter.next(), allvars, this.isLeaf());
        }
        else{ /* check that dataset is not null */
            if(dataset == null)
                throw new Exception("null dataset found in computing d_pairs");
            examples_dataset = dataset;
        }
        
        return examples_dataset;
    }

    public static void main(String[] args)
    {
        
        discriminant_TetCommandLineOptions options = new discriminant_TetCommandLineOptions(args);
        
        try{
            /* load discriminant tet from file */
            String tetstring = Tet.tetString(options.tetfile);
            
            discriminant_Tet tet = new discriminant_Tet(tetstring);
            
            System.out.println("Read discriminant Tet string: " + tet.Serialize());
            System.out.println("Tet Freevars = " + tet.freevars().toString());   
            
            /* create mysql relational structure which uses TD BU procedure */
            MySQLRelStructure relstruct = new MySQLRelStructure(options.database, options.login, options.password, true);
         
            /* create MySQLTestViewer if needed */
            MySQLTestViewer testviewer = (options.srcdb != null && options.testviewconfigfile != null) ? 
                new MySQLTestViewer(relstruct, options.srcdb, options.testviewconfigfile) : null;
            
            /* parse object file and compute discriminant value for each object  */
            FileReader in = new FileReader(options.objfile);
            StreamTokenizer st = new StreamTokenizer(in);
            st.eolIsSignificant(true);
            st.whitespaceChars(32,47);
            st.whitespaceChars(58,63);

            FileWriter out = new FileWriter(options.outfile, false);

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
                                
                float d = tet.compute_d(relstruct, objmap);                             

                /* print decision and d value */
                out.write(tet.compute_decision(d) + "\t" + d); 
                
                /*  compute tet value and print it if needed */
                if(options.compute_tet_value){
                    Value value = tet.calculateValue(relstruct, objmap, !options.compute_tet_false_value);
                    out.write("\t" + value);
                    /*  compute rdk value and print it if needed */
                    if(options.compute_rdk_value)
                        out.write("\t" + value.writeRDK());
                }

                /* print newline */
                out.write("\n");
                out.flush();

                /* remove test view for current example if needed */
                if(testviewer != null)
                    testviewer.removeTestView(objmap);
            }
            out.close();
            relstruct.Close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}

class discriminant_TetCommandLineOptions {


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
    public boolean compute_rdk_value = false;


    public discriminant_TetCommandLineOptions(String[] args)
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
            case 'k':
                compute_rdk_value = true;
                compute_tet_value = true;
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
        System.out.println("Usage:\n\ttet.discriminant_Tet [options] <database> <login> <password> <tetfile> <objfile> <outfile>");
        System.out.println("Options:");
        System.out.println("\t-c <int>   \tconfig file for test view (default=null)");
        System.out.println("\t-d <string>\tsrc db for test view (default=null)");
        System.out.println("\t-v         \tcompute tet value (default=false)");
        System.out.println("\t-f         \tcompute tet false value (default=false)");
        System.out.println("\t-k         \tcompute rdk value (default=false)");
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
            + "compute_rdk_value=" + compute_rdk_value + "\n"; 
    }
}
