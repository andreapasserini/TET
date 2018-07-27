package tet.learner;

import tet.*;

import java.util.*;
import java.io.*;
import mymath.*;


public class TetLearnerPolicyDiscriminantTet extends TetLearnerGenericPolicy{
    
    public Scorer.ScoreMeasure evaluation_score_measure = Scorer.ScoreMeasure.OPTACC;
    public boolean rebalance_evaluation_score = false;

    public TetLearnerPolicyDiscriminantTet(BufferedReader factoryconfig, 
					   TetLearnerCommandLineOptions options) throws Exception
    {
	super(factoryconfig, options);

	evaluation_score_measure = options.evaluation_score_measure;
	rebalance_evaluation_score = options.rebalance_evaluation_score;
    }
    
    public Tet newTet(Type root_type, TreeSet<Variable> freevars, Dataset dataset) throws Exception
    {
	return new discriminant_Tet(root_type, freevars, dataset);
    }

    /* learns parameters: for node tet and global ones for root_tet.
       return score */
    public float learnParameters(Tet root_tet, Dataset root_dataset, 
				Tet tet, Dataset dataset) throws Exception
    {
	learnNodeParameters(tet, dataset); 

	/* learning global parameters (threshold) */
	return learnOverallParameters(root_tet, root_dataset);
    }

    public void learnNodeParameters(Tet tet, Dataset dataset) 
    {
	discriminant_Tet dtet = (discriminant_Tet)tet;
	
	/* set discriminant tet weight to P(+|dataset) */
	dtet.setWeight(dataset.getTargetProportion(1));

	System.out.println("Computed type weight: " + dtet.getWeight() 
			   + " / " + dtet.getRootType());
    }

    public void freezeExtensionCache(Tet tet) throws Exception
    {
	/* freeze discriminant tet cache */
	((discriminant_Tet)tet).freezeCache();
    }
    
    public void cleanupExtensionCache(Tet roottet, Tet extensiontet) throws Exception
    {
	/* cleanup non freezed cache */
	((discriminant_Tet)roottet).cleanupNonFreezedCache();
	/* finalize rejected extension */
	try{
	    ((discriminant_Tet)extensiontet).finalize();
	}catch(Throwable t){
	    t.printStackTrace();
	    System.exit(0);
	}
    }

    public float learnOverallParameters(Tet tet, Dataset dataset) throws Exception
    {
	return scoreTET(tet, dataset);
    }

    public float scoreTET(Tet tet, Dataset dataset) throws Exception
    {
	System.out.println("Scoring tet");
	/* get current time for performace evaluation */
	long timebefore = System.currentTimeMillis();

	/* cast tet into discriminant tet */
	discriminant_Tet dtet = (discriminant_Tet)tet;

	/* ordered structure mapping d+/d- to target */
	TreeMap<Float, List<Float>> d_map = new TreeMap<Float, List<Float>>();

	/* recover dataset as vector of examples */
	Vector<Example> examples = (dataset != null) ? dataset.getExamples() : dtet.getDataset().getExamples();

	Float[] ds = null;
	
	if(dataset != null){
	    ds = new Float[examples.size()];
	    /* iterate over examples computing d+/d- */
	    for(int i = 0; i < examples.size(); i++)
		ds[i] =	dtet.compute_d(dataset.getRelStructure(), examples.elementAt(i).getRelObjects());	
	}
	else
	    /* recover d+/d- values for the entire dataset */
	    ds = dtet.compute_ds(dtet.getDataset().getRelationVars(), table_counter);
	
	/* iterate over examples inserting d+/d- */
	for(int i = 0; i < examples.size(); i++)
	    TetUtilities.addToMultiMap(d_map, ds[i].floatValue(), examples.elementAt(i).getTarget());

	/* compute score and optimal threshold */
	float[] threshold = new float[1];
	float score = Scorer.score(d_map, evaluation_score_measure, threshold, rebalance_evaluation_score);
	
	/* update tet threshold with current optimal threshold */
	dtet.setThreshold(threshold[0]);

	/* print time taken in computation */
	System.out.println("Took: " + (System.currentTimeMillis()-timebefore) + " milliseconds");

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
	    
	    TetLearnerPolicyDiscriminantTet policy = new TetLearnerPolicyDiscriminantTet(inbuf, options); 

	    inbuf.close();
	    in.close();

	    TetLearner learner = new TetLearner(targetrel, policy);

	    MySQLDataset trainingset = new MySQLDataset(relstruct, policy.table_counter.next(), 
							learner.getRootVariables(),options.trainfile);

	    /* if no validationdb specified assume same database as trainingset */
	    MySQLRelStructure valrelstruct = (options.validationdb != null) ? new MySQLRelStructure(options.validationdb, 
												    options.login, 
												    options.password) : relstruct;

	    MySQLDataset validationset = (options.validationfile != null) ? new MySQLDataset(valrelstruct, policy.table_counter.next(),
											     learner.getRootVariables(),
											     options.validationfile) : null;
	    
	    Tet tet = new Tet();
	    
	    if(options.srctetfile != null){ /* tet structure already available. Learn its parameters */
		
		/* load discriminant tet from file */
		String tetstring = Tet.tetString(options.srctetfile);
		tet = new discriminant_Tet(tetstring);

		/* print it */
		System.out.println("Loaded tet:\n" + tet.toFormattedString());

		/* check consistency */
		if(!learner.freeVarsConsistency(tet))
			throw new Exception("ERROR: learner root vars do not match tet free vars");
		

		/* reset its parameters */
		((discriminant_Tet)tet).resetThresholds();
		
		/* learn its parameters */
		if(options.clamp_weights){
			System.out.println("Weights clamped, learning threshold..."); 
			learner.learnOverallParameters(tet, trainingset);
		}
		else{				
			((discriminant_Tet)tet).resetWeights();
			System.out.println("Learning its parameters..."); 
			learner.learnParameters(tet, trainingset);
		}
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
