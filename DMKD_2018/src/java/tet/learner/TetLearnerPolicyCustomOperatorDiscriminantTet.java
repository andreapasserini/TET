package tet.learner;

import tet.*;

import java.util.*;
import java.io.*;
import mymath.*;


public class TetLearnerPolicyCustomOperatorDiscriminantTet extends TetLearnerPolicyDiscriminantTet{
    
    public TetLearnerPolicyCustomOperatorDiscriminantTet(BufferedReader factoryconfig, 
							 TetLearnerCommandLineOptions options) throws Exception
    {
	super(factoryconfig, options);
    }
    
    public Tet newTet(Type root_type, TreeSet<Variable> freevars, Dataset dataset) throws Exception
    {
	return new customOperatorDiscriminant_Tet(root_type, freevars, dataset);
    }

    /* learns parameters: for node tet and global ones for root_tet.
       return score */
    public float learnParameters(Tet root_tet, Dataset root_dataset, 
				Tet tet, Dataset dataset) throws Exception
    {
	learnNodeParameters(tet, dataset); 

	/* also learns global parameters (threshold) */
	return learnOperators(root_tet, root_dataset, tet);
    }
    
    public float learnOverallParameters(Tet tet, Dataset dataset) throws Exception
    {
	return scoreTET(tet, dataset);
    }

    public float scoreTET(Tet tet, Dataset dataset) throws Exception
    {
	/* WARNING: scoreTET also sets overall parameters 
	   (overall threshold in discriminant tet or svm model in tet kernel machine). 
	   To be fixed */

	/* cast tet into discriminant tet */
	customOperatorDiscriminant_Tet dtet = (customOperatorDiscriminant_Tet)tet;

	/* ordered structure mapping d+/d- to target */
	TreeMap<Float, List<Float>> d_map = new TreeMap<Float, List<Float>>();

	/* recover dataset as vector of examples */
	Vector<Example> examples = (dataset != null) ? dataset.getExamples() : 
	    dtet.getDataset().getExamples();

	return scoreTET(dtet, examples, d_map, dataset);
    }

    public void learnNodeParameters(Tet tet, Dataset dataset) 
    {
	/* cast tet into discriminant tet */
	customOperatorDiscriminant_Tet dtet = (customOperatorDiscriminant_Tet)tet;
	
	/* set discriminant tet weight to P(+|dataset) */
	dtet.setWeight(dataset.getTargetProportion(1));

	System.out.println("Computed type weight: " + dtet.getWeight() 
			   + " / " + dtet.getRootType());
    }


    public float learnOperators(Tet root_tet, Dataset dataset, 
				Tet current_tet) throws Exception
    {
	/* set custom operators */

	System.out.println("Scoring tet");
	/* get current time for performace evaluation */
	long timebefore = System.currentTimeMillis();

	/* cast tet into discriminant tet */
	customOperatorDiscriminant_Tet dtet = (customOperatorDiscriminant_Tet)root_tet;

	/* ordered structure mapping d+/d- to target */
	TreeMap<Float, List<Float>> d_map = new TreeMap<Float, List<Float>>();

	/* recover dataset as vector of examples */
	Vector<Example> examples = (dataset != null) ? dataset.getExamples() : 
	    dtet.getDataset().getExamples();

	/* iterate over possible custom operator configurations and keep best */
	float bestscore = (float)0.;
	customOperatorDiscriminant_Tet currdtet = (customOperatorDiscriminant_Tet)current_tet;

	/* if leaf node no configuration is needed */
	if (currdtet.isLeaf())
	    bestscore = scoreTET(dtet, examples, d_map, dataset);
	//ALTERNATIVE: always configuring custom operators of parent of 
	//last inserted node
//  	System.out.println("getting node to be configured:");
//  	customOperatorDiscriminant_Tet currdtet = dtet.get_node_to_configure();
//  	/* if single node no configuration is needed */
//  	if (currdtet == null)
// 	    bestscore = scoreTET(dtet, examples, d_map, dataset);
	else{
	    System.out.println(currdtet);

	    /* reset previous configuration */
	    currdtet.clearConfiguration();
	    Pair<Operator,Operator> best_configuration = null;

	    /* iterate over possible configurations */
	    while(currdtet.hasNextConfiguration()){
		
		System.out.println("getting next configuration:");
		/* update currdtet to next possible configuration */
		currdtet.nextConfiguration();
		System.out.println(currdtet.getConfiguration());
		/* score whole dtet with novel configuration for unconfigured node */
		float currscore = scoreTET(dtet, examples, d_map, dataset);
		System.out.println("configuration score: " + currscore);
		if(currscore > bestscore){
		    bestscore = currscore;
		    best_configuration = currdtet.getConfiguration();
		}	    
		/* clear d_map */
		d_map.clear();
	    }	
	    /* update tet with best configuration */
	    currdtet.setConfiguration(best_configuration);
	    /* compute best score. Needed to correctly set cache. */
	    /* WARNING: horrible patch */
	    bestscore = scoreTET(dtet, examples, d_map, dataset);
	}

	/* print time taken in computation */
	System.out.println("Took: " + (System.currentTimeMillis()-timebefore) + " milliseconds");

	/* return score */
	return bestscore;
    }    

    public float scoreTET(customOperatorDiscriminant_Tet dtet, 
			  Vector<Example> examples, 
			  TreeMap<Float, List<Float>> d_map, 
			  Dataset dataset) throws Exception
    {
	Float[] ds = null;
	
	if(dataset != null){
	    ds = new Float[examples.size()];
	    /* iterate over examples computing d+/d- */
	    for(int i = 0; i < examples.size(); i++)
		ds[i] =	dtet.compute_d(dataset.getRelStructure(), 
				       examples.elementAt(i).getRelObjects());	
	}
	else
	    /* recover d+/d- values for the entire dataset */
	    ds = dtet.compute_ds(dtet.getDataset().getRelationVars(), table_counter);
	
	/* iterate over examples inserting d+/d- */
	for(int i = 0; i < examples.size(); i++)
	    TetUtilities.addToMultiMap(d_map, ds[i].floatValue(), 
				       examples.elementAt(i).getTarget());

	/* compute score and optimal threshold */
	float[] threshold = new float[1];
	float score = Scorer.score(d_map, evaluation_score_measure, threshold, 
				   rebalance_evaluation_score);
	
	/* update tet threshold with current optimal threshold */
	dtet.setThreshold(threshold[0]);

	/* return score */
	return score;
    }

    public static void main(String[] args)
    {
	TetLearnerCommandLineOptions options = new TetLearnerCommandLineOptions(args);
		
	//System.out.println("CommandLineOptions:");
	//System.out.println(options.toString());

	try{
	    MySQLRelStructure relstruct = new MySQLRelStructure(options.database, options.login, 
								options.password);

	    FileReader in = new FileReader(options.configfile);
	    BufferedReader inbuf = new BufferedReader(in);

	    // get target relation
	    String targetrel = inbuf.readLine();
	    
	    TetLearnerPolicyCustomOperatorDiscriminantTet policy = new TetLearnerPolicyCustomOperatorDiscriminantTet(inbuf, options); 

	    inbuf.close();
	    in.close();

	    TetLearner learner = new TetLearner(targetrel, policy);

	    MySQLDataset trainingset = new MySQLDataset(relstruct, policy.table_counter.next(), 
							learner.getRootVariables(),options.trainfile);

	    /* if no validationdb specified assume same database as trainingset */
	    MySQLRelStructure valrelstruct = (options.validationdb != null) ? 
		new MySQLRelStructure(options.validationdb, 
				      options.login, 
				      options.password) : relstruct;

	    MySQLDataset validationset = (options.validationfile != null) ? 
		new MySQLDataset(valrelstruct, policy.table_counter.next(),
				 learner.getRootVariables(),
				 options.validationfile) : null;
	    
	    Tet tet = new Tet();
	    
	    if(options.srctetfile != null){ 
		/* tet structure already available. Learn its parameters */
		
		/* load discriminant tet from file */
                FileReader tetin = new FileReader(options.srctetfile);
                BufferedReader buftetin = new BufferedReader(tetin);
                String tetstring = buftetin.readLine();
                tet = new customOperatorDiscriminant_Tet(tetstring);
                buftetin.close();
                tetin.close();

                /* print it */
                System.out.println("Loaded tet:\n" + tet.toFormattedString());

                /* check consistency */
                if(!learner.freeVarsConsistency(tet))
                        throw new Exception("ERROR: learner root vars do not match tet free vars");

                /* reset its parameters */
                ((customOperatorDiscriminant_Tet)tet).resetWeights();
                ((customOperatorDiscriminant_Tet)tet).resetThresholds();
                
                /* learn its parameters */
                System.out.println("Learning its parameters..."); 
                learner.learnParameters(tet, trainingset);
                System.out.println("Parameter learning finished."); 
	    }
	    else{ /* learn structure and parameters */

		tet = learner.buildTet(trainingset, validationset);
	    
		System.out.println("Final tet:\n" + tet.toFormattedString());

		/* score final tet */
		System.out.println("Final tet score: " + policy.scoreTET(tet, validationset));
	    }
	    
	    /* write final tet to tetfile */
	    FileWriter out = new FileWriter(options.tetfile);
	    out.write(tet.Serialize() + "\n");
	    out.close();
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }
}  
