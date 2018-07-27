package Tetpackage.learner;

import Tetpackage.*;

import java.util.*;
import mymath.*;

public class TetLearnerCommandLineOptions {

    public int max_depth = 3; 
    public float selection_score_entity_threshold = 0;
    public float selection_score_attribute_threshold = 0;
    public float evaluation_score_threshold = 0;
    public int max_new_vars_num = 2;
    public int min_greedy_search_depth = 0;
    public int max_extension_num = 0;
    public float max_evaluation_score = 1;
    public int max_rejections = Integer.MAX_VALUE;
    public boolean skip_negated = false;
    public boolean use_set_difference = false;
    public boolean rebalance_evaluation_score = false;
    public boolean use_gig = false;
    public boolean clamp_weights = false;

    public String configfile;
    public String trainfile;
    public String tetfile;
    public String validationfile = null;
    public String validationdb = null;
    
    /* tetfile with learned structure. If present implies learning its parameters */
    public String srctetfile = null;

    public String database;
    public String login;
    public String password;

    public Scorer.ScoreMeasure evaluation_score_measure = Scorer.ScoreMeasure.OPTACC;

    // KernelMachine arguments 
    public String evaluatecommand ="";
    public String rdkdatafile ="";
    public String scorefile ="";
    
    public TetLearnerCommandLineOptions(String[] args)
    {
	int pos = parseOptions(args);

	// chek if compulsory options specified
	if(args.length - pos < 6){ 
	    System.out.println("Missing compulsory option(s):\n");
	    printHelp();
	    System.exit(1);
	}

	configfile = args[pos++];
	trainfile = System.getenv("PWD") + "/" + args[pos++];
	tetfile = args[pos++];
	database = args[pos++];
	login = args[pos++];
	password = args[pos++];	

	/* if validation is same as train skip it */
	if(trainfile.equals(validationfile))
	    validationfile = null;
	if(database.equals(validationdb))
	    validationdb = null;
    }
    
    public int parseOptions(String[] options)
    {       	
	int pos = 0;

	while(pos < options.length && options[pos].charAt(0) == '-'){
	    switch(options[pos].charAt(1)){
	    case 'a':
		selection_score_attribute_threshold = Float.parseFloat(options[++pos]);
		break;
	    case 'd':
		max_depth = Integer.parseInt(options[++pos]);
		break;
	    case 'e':
		selection_score_entity_threshold = Float.parseFloat(options[++pos]);
		break;
	    case 'g':
		min_greedy_search_depth = Integer.parseInt(options[++pos]);
		break;
	    case 'm':
		max_evaluation_score = Float.parseFloat(options[++pos]);
		break;		
	    case 'n':
		max_new_vars_num = Integer.parseInt(options[++pos]);
		break;
	    case 'r':
		max_rejections = Integer.parseInt(options[++pos]);
		break;
	    case 's':
		evaluation_score_measure = Scorer.ScoreMeasure.values()[Integer.parseInt(options[++pos])];
		break;
	    case 't':
		skip_negated = true;
		break;		
	    case 'u':
		use_set_difference = true;
		break;		
	    case 'v':
		validationfile = System.getenv("PWD") + "/" + options[++pos];
		break;		
	    case 'C':
		evaluatecommand = options[++pos];
		break;
	    case 'D':
		rdkdatafile = options[++pos];
		break;
	    case 'E':
		evaluation_score_threshold = Float.parseFloat(options[++pos]);
		break;
	    case 'G':
		use_gig = true;
		break;
	    case 'M':
		max_extension_num = Integer.parseInt(options[++pos]);
		break;
	    case 'R':
		rebalance_evaluation_score = true;
		break;
	    case 'S':
		scorefile = options[++pos];
		break;
	    case 'T':
		srctetfile = options[++pos];
		break;
	    case 'V':
		validationdb = options[++pos];
		break;
	    case 'W':
		clamp_weights = true;
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
	System.out.println("Usage:\n\tTetpackage.TetLearner [options] <configfile> <trainfile> <tetfile> <database> <login> <password>\n");
	System.out.println("Generic options:");
	System.out.println("\t-d <int>\tmaximum Tet depth (default=3)");
	System.out.println("\t-n <int>\tmaximum number of new variables (default=2)");
	System.out.println("\t-M <int>\tmaximum number of candidate extensions (default=0 -> no limit)");
	System.out.println("\t-e <float>\tselection score entity threshold (default=0)");
	System.out.println("\t-a <float>\tselection score attribute threshold (default=0)");
	System.out.println("\t-E <float>\tevaluation score threshold (default=0)");
	System.out.println("\t-s <int>\tevaluation score measure (0=OPTACC,1=AUC,2=OPTF1) (default=0)");
	System.out.println("\t-m <float>\tmax evaluation score (default=1)");
	System.out.println("\t-R      \trebalance evaluation score (default=false)");
	System.out.println("\t-r <int>\tmaximum number of consecutive per node rejections (default=inf)");
	System.out.println("\t-g <int>\tminimum depth for starting greedy search (default=0)");
	System.out.println("\t-G      \tuse GIG instead of RIG as literal evaluation function (default=false)");
	System.out.println("\t-t      \tskip negated branches (default=false)");
	System.out.println("\t-u      \tuse set difference (default=false)");
	System.out.println("\t-v <string>\tvalidation file (default=none)");
	System.out.println("\t-V <string>\tvalidation database (default=none)");
	System.out.println("\t-T <string>\tsource tetfile. Implies learning its parameters (default=null)");
	System.out.println("\t-W      \tclamp tet weights (default=false)");
	System.out.println("TKM options:");
	System.out.println("\t-C <string>\tevaluation command");
	System.out.println("\t-D <string>\tRDK data file");
	System.out.println("\t-S <string>\tscore file");
	System.out.println("\t-h      \tprint help");
    }

    public String toString()
    {
	return "max_depth=" + max_depth + "\n" 
	    + "max_new_vars_num=" + max_new_vars_num + "\n" 
	    + "min_greedy_search_depth=" + min_greedy_search_depth + "\n" 
	    + "max_extension_num=" + max_extension_num + "\n" 
	    + "selection_score_entity_threshold=" + selection_score_entity_threshold + "\n" 
	    + "selection_score_attribute_threshold=" + selection_score_attribute_threshold + "\n" 
	    + "evaluation_score_threshold=" + evaluation_score_threshold + "\n" 
	    + "max_evaluation_score=" + max_evaluation_score + "\n" 
	    + "max_rejections=" + max_rejections + "\n" 
	    + "configfile=" + configfile + "\n" 
	    + "trainfile=" + trainfile + "\n" 
	    + "tetfile=" + tetfile + "\n" 
	    + "srctetfile=" + srctetfile + "\n" 
	    + "validationfile=" + validationfile + "\n" 
	    + "validationdb=" + validationdb + "\n" 
	    + "database=" + database + "\n" 
	    + "login=" + login + "\n" 
	    + "password=" + password + "\n"
	    + "evaluation_score_measure=" + evaluation_score_measure + "\n"
	    + "skip_negated=" + skip_negated + "\n"
	    + "use_set_difference=" + use_set_difference + "\n"
	    + "rebalance_evaluation_score=" + rebalance_evaluation_score + "\n"
	    + "clamp_weights=" + clamp_weights + "\n"
	    // KernelMachine arguments 
	    + "evaluatecommand=\"" + evaluatecommand + "\"\n"
	    + "rdkdatafile=\"" + rdkdatafile + "\"\n"
	    + "scorefile=\"" + scorefile + "\"\n";
    }
}
