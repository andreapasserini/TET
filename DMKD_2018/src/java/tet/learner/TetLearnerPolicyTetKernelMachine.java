package tet.learner;

import tet.*;

import java.util.*;
import java.io.*;
import mymath.*;


public class TetLearnerPolicyTetKernelMachine extends TetLearnerGenericPolicy{

    TetKernelMachine tkm;

    public TetLearnerPolicyTetKernelMachine(BufferedReader factoryconfig, 
					    TetLearnerCommandLineOptions options,
					    TetKernelMachine tkm) throws Exception
    {
	super(factoryconfig, options);

	this.tkm = tkm;
    }

    /* learns parameters: for node tet and global ones for root_tet.
       return score */
    public float learnParameters(Tet root_tet, Dataset root_dataset, 
				Tet tet, Dataset dataset) throws Exception
    {	
	learnNodeParameters(tet, dataset); 

	return learnOverallParameters(root_tet, root_dataset);
    }

    public void learnNodeParameters(Tet tet, Dataset dataset) 
    {

    }
    
    public float learnOverallParameters(Tet tet, Dataset dataset) throws Exception
    {
	return scoreTET(tet, dataset);
    }
	
    public float scoreTET(Tet tet, Dataset dataset) throws Exception
    {
	return tkm.evaluationScore(tet, dataset);
    }   

    public static void main(String[] args)
    {
	TetLearnerCommandLineOptions options = new TetLearnerCommandLineOptions(args);
		
	//System.out.println("CommandLineOptions:");
	//System.out.println(options.toString());
	
	try{
	    MySQLRelStructure relstruct = new MySQLRelStructure(options.database, options.login, 
								options.password);

	    TetKernelMachine tkm = new TetKernelMachine(options.evaluatecommand, options.rdkdatafile, options.scorefile);

	    FileReader in = new FileReader(options.configfile);
	    BufferedReader inbuf = new BufferedReader(in);

	    // get target relation
	    String targetrel = inbuf.readLine();
	    
	    TetLearnerPolicyTetKernelMachine policy = new TetLearnerPolicyTetKernelMachine(inbuf, options, tkm); 

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

	    Tet tet = learner.buildTet(trainingset, validationset);
	    
	    System.out.println("Final tet:\n" + tet.toFormattedString());
	    
	    /* score final tet */
	    /* WARNING: should set final tet models ???  */
	    System.out.println("Final tet score: " + policy.scoreTET(tet, validationset));
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
