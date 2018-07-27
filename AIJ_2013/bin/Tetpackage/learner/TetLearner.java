package Tetpackage.learner;

import Tetpackage.*;

import java.util.*;
import java.io.*;
import mymath.*;

public class TetLearner{

    TetLearnerPolicy policy;
    
    Counter name_counter;
    Relation targetrel;

    /* contain root variables */
    Vector<Variable> rootvars;

    /* contain root variables grouped by type */
    VariableByType rootvarsbytype;

    /* timestamp to evaluate timing */
    long timestamp;

    public TetLearner(String targetrel, TetLearnerPolicy policy) throws Exception
    {
	this(new Relation(targetrel), policy);
    }
    
    public TetLearner(Relation targetrel, TetLearnerPolicy policy) throws Exception
    {
	this.policy = policy;	
	this.targetrel = targetrel; 

	name_counter = new Counter();
	
	init();
    }
    
    public Tet buildTet(Dataset trainingset) throws Exception
    {
	return buildTet(trainingset, null, new Type());
    }				
    
    public Tet buildTet(Dataset trainingset, Type roottype) throws Exception
    {
	return buildTet(trainingset, null, roottype);
    }				
    
    public Tet buildTet(Dataset trainingset, Dataset validationset) throws Exception
    {       	
	return buildTet(trainingset, validationset, new Type());
    }

    public Tet buildTet(Dataset trainingset, Dataset validationset, Type roottype) throws Exception
    {
	name_counter.setValue(rootvarsbytype.getNumVariables()); 

	timestamp = System.currentTimeMillis();

	return buildTet(trainingset, validationset, roottype, rootvarsbytype, rootvarsbytype.getVariables());	
    }

    public void learnParameters(Tet root_tet, Dataset trainingset) throws Exception
    {
	timestamp = System.currentTimeMillis();

	/* learn node dependent parameters */
	learnNodeParameters(root_tet, trainingset, rootvarsbytype.getVariables(), 0);	

	/* learn overall parameters */
	learnOverallParameters(root_tet, trainingset);	
    }
    
    public Vector<Variable> getRootVariables() 
    {
        return rootvars; 
    }

    public Boolean freeVarsConsistency(Tet root_tet)
    {
	TreeSet<Variable> freevarset=root_tet.freevars();
	TreeSet<Variable> rootvarset=TetUtilities.vector2set(rootvars);

	return TetUtilities.equals(freevarset,rootvarset);
    }

    public void learnOverallParameters(Tet root_tet, Dataset trainingset) throws Exception
    {
	float root_tet_score = policy.learnOverallParameters(root_tet, trainingset);
	System.out.println("Root tet score: " + root_tet_score);
	System.out.println(root_tet.toFormattedString());
    }


    /***************************************************************************************************************/
    /********************************** private methods ***************************************************/


    private void init() throws Exception 
    {
	rootvars = new Vector<Variable>();
	rootvarsbytype = new VariableByType();
	
	// get all target relation variables 
	for(int i = 0; i < targetrel.arity(); i++){
	    Variable var = new Variable(nextName(targetrel.argType(i).name()));
	    rootvars.add(var);
	    rootvarsbytype.addVariable(targetrel.argType(i), var);
	}
    }
    
    private String nextName()
    {
	return nextName("");
    }
    
    private String nextName(String prefix)
    {
	String currname = prefix + name_counter.getValue();
	name_counter.increment(); 
	return currname;
    }
    
    private Tet buildTet(Dataset trainingset, 
			 Dataset validationset,
			 Type root_type, 
			 VariableByType root_variables, 
			 TreeSet<Variable> newest_variables) throws Exception
    {
	/* create initial empty tet */
	Tet root_tet = policy.newTet(root_type, root_variables.getVariables(), trainingset);

	/* recursively extend it */
	buildTet(root_tet, trainingset, validationset, 
		 root_tet, trainingset, new Vector<Type>(), root_variables, newest_variables, 0); 

	/* return extended tet */
	return root_tet;
    }


    private void buildTet(Tet root_tet, Dataset trainingset, Dataset validationset, 
			  Tet parent_tet, Dataset parent_dataset, 
			  Vector<Type> prohibited_types,
			  VariableByType parent_variables, 
			  TreeSet<Variable> newest_variables, int depth) throws Exception
    {
	System.out.println("-------------------------------------------------------------");
	System.out.println("Current depth: " + depth );

	/* score root TET */
	float root_tet_score = policy.learnParameters(root_tet, validationset, 
						      parent_tet, parent_dataset);
	System.out.println("Root tet score: " + root_tet_score);
	System.out.println(root_tet.toFormattedString());

	if(policy.maxDepthReached(depth)){ // stop recursion
	    System.out.println("Max depth reached.");
	    System.out.println("-------------------------------------------------------------");
	    return;
	}

	if(policy.maxScoreReached(root_tet_score)){ // stop recursion
	    System.out.println("Max score reached.");
	    System.out.println("-------------------------------------------------------------");
	    return;
	}
	System.out.println("-------------------------------------------------------------");

	// increment counter to assure distinct variable names
	String currvarprefix = nextName() + "_";

	// create candidate extensions
	Extensions extensions = policy.generateExtensions(parent_variables, currvarprefix);	
 
	// remove extensions that shouldn't even be evaluated with prior score 
	/* create target literal with parent variables */
	Extensions allowed_extensions = policy.filterExtensions(extensions, 
								targetrel,
								prohibited_types,
								parent_variables.getVariables(),
								newest_variables,
								rootvarsbytype.getVariables(), 
								rootvarsbytype);

	// assign prior score to candidate extensions
	System.out.println("-------------------------------------------------------------");
	Vector<ScoredExtension> extensions_vec = policy.scoreExtensions(allowed_extensions, parent_variables, parent_dataset, depth);
	System.out.println("-------------------------------------------------------------");

	// retain extensions satisfying selection heuristic 
	System.out.println("-------------------------------------------------------------");
	PriorityQueue<ScoredExtension> retained_extensions_queue = policy.selectExtensions(extensions_vec);
	System.out.println("-------------------------------------------------------------");

	/* initialize vector of candidate tet children */
	Vector<ScoredTetChild> tetchildren = new Vector<ScoredTetChild>();

	/* keep the number of rejected extensions in order to prematurely leave node refinement */
	int rejected_extensions = 0;

	// iterate over all retained extensions 
	while(!retained_extensions_queue.isEmpty()){
	    Extension curr_extension = retained_extensions_queue.poll().getExtension();
	    // iterate over types within extension	    
	    for(int j = 0; j < curr_extension.size(); j++){		

		/* select current extension type */
		ExtensionType curr_extension_type = curr_extension.elementAt(j);

		/* create candidate extension tet with current extension type */
		Tet recursivetet =  policy.newTet(curr_extension_type.getType(), 
						  curr_extension_type.getAllVariables().getVariables(),
						  curr_extension_type.getDataset());
		
		/* create candidate extension tet child */
		TetChild recursivetetchild = new TetChild(recursivetet, curr_extension_type.getNewVariables());
		
		/* attach candidate extension tet child to parent */
		parent_tet.addChild(recursivetetchild);
		
		/* recover current newest variables */
		TreeSet<Variable> curr_newest_variables = curr_extension_type.getNewVariables();

		/* create a new prohibition list */
		Vector<Type> curr_prohibited_types = policy.newProhibitedTypes(prohibited_types, curr_extension_type);
		
		if(curr_newest_variables.size() == 0) /* attribute extension */
		    /* retain previous newest variables */
		    curr_newest_variables = newest_variables;		    

		/* recursively refine tet */
		buildTet(root_tet, trainingset, validationset, recursivetet, curr_extension_type.getDataset(), 
			 curr_prohibited_types, curr_extension_type.getAllVariables(), curr_newest_variables, depth+1);

		if(policy.greedySearch(depth)){

		    /* score candidate recursivetet and retain it if above threshold */	
		    System.out.println("-------------------------------------------------------------");
		    System.out.println("Current depth: " + depth );

		    float new_tetscore = policy.learnParameters(root_tet, validationset, 
								parent_tet, parent_dataset);
		    
		    System.out.println("Candidate tet score: " + new_tetscore);
		    System.out.println(root_tet.toFormattedString());
	
		    // evaluate extended tet
		    if(policy.acceptExtension(root_tet_score, new_tetscore)){
			System.out.println("Extension accepted");
			System.out.println("Current computing time: " + ((float)(System.currentTimeMillis()-timestamp)/60000) + " min");
			System.out.println("Current best tet:\n" + root_tet.Serialize());			

			/* update evaluation score*/
			root_tet_score = new_tetscore;

			/* freeze recursive tet cache */
			policy.freezeExtensionCache(recursivetet);
		    
			/* reset number of rejections */
			rejected_extensions = 0;
		    }
		    else{ 
			System.out.println("Extension rejected");
			System.out.println("Current computing time: " + ((float)(System.currentTimeMillis()-timestamp)/60000) + " min");
			/* cleanup non-freezed caches */
			policy.cleanupExtensionCache(root_tet, recursivetet);
			/* remove candidate extension tet from parent */
			parent_tet.removeLastChild();
			/* increase number of rejections */
			rejected_extensions++;
		    }	

		    boolean stop_recursion = false;

		    /* if maximum score reached stop recursion */
		    if(policy.maxScoreReached(new_tetscore)){
			System.out.println("Max score reached.");
			stop_recursion = true;
		    }
		    /* if maximum number of rejections reached stop recursion */
		    if(policy.maxRejectionsReached(rejected_extensions)){
			System.out.println("Max rejections reached.");
			stop_recursion = true;
		    }

		    System.out.println("-------------------------------------------------------------");		    

		    if(stop_recursion)
			return;		    
		}
		else{
		    /* remove candidate extension tet from parent */
		    parent_tet.removeLastChild();

		    /* add candidate extension tet to tet children */
		    tetchildren.add(new ScoredTetChild(0, recursivetetchild));
		}
	    }
	}

	if(!policy.greedySearch(depth)){
	    /* score candidate tet children and retain optimal scoring ones */	
	    System.out.println("-------------------------------------------------------------");
	    System.out.println("Current depth: " + depth );
	    policy.extendTet(root_tet, validationset, root_tet_score, parent_tet, parent_dataset,
			     (ScoredTetChild[])tetchildren.toArray(new ScoredTetChild[0]));
	    System.out.println("Current computing time: " + ((float)(System.currentTimeMillis()-timestamp)/60000) + " min");
	    System.out.println("Current best tet extension:\n" + root_tet.Serialize());			
	    System.out.println("-------------------------------------------------------------");
	}
    }

    private void learnNodeParameters(Tet parent_tet, Dataset parent_dataset, 
				    TreeSet<Variable> parent_variables, int depth) throws Exception
    {

	System.out.println("-------------------------------------------------------------");
	System.out.println("Current depth: " + depth );

	/* set tet parameters */
	System.out.println("Setting parameters for tet type: " + parent_tet.getRootType());
	policy.learnNodeParameters(parent_tet, parent_dataset);
	System.out.println("-------------------------------------------------------------");

	/* initialize vector of datasets  */
	Vector<Dataset> children_dataset = new Vector<Dataset>();

	/* iterate over tet node children */
	for(int i = 0; i < parent_tet.getNumChildren(); i++){

	    /* get current child tet */
	    Tet curr_tet = parent_tet.getChild(i).getSubTree();

	    /* get its type */
	    Type curr_type = curr_tet.getRootType();

	    /* get type variables */
	    TreeSet<Variable> allvars = curr_type.getVariables();

	    /* merge with parent vars */
	    allvars.addAll(parent_variables);
	    
	    /* recover dataset satisfying tetchild root type */
	    System.out.println("Recovering dataset for type: " + curr_type); 
	    long timebefore = System.currentTimeMillis();

	    Dataset curr_dataset = (i > 0) ? 
		/* if previous dataset available pass it as it could be used for efficiency */
		policy.getSatisfyingDataset(curr_type,
					    allvars, 
					    parent_dataset,
					    parent_tet.getChild(i-1).getSubTree().getRootType(),
					    children_dataset.lastElement(),
					    curr_tet.isLeaf()) :
		/* otherwise call standard method */
		policy.getSatisfyingDataset(curr_type,
					    allvars, 
					    parent_dataset,
					    curr_tet.isLeaf());								

	    /* print timing information */
	    System.out.println("Took: " + (System.currentTimeMillis()-timebefore) + " milliseconds");	    

	    /* recursively learn parameters of tetchild */
	    learnNodeParameters(curr_tet, curr_dataset, allvars, depth+1);

	    /* add current dataset to children_dataset vector */
	    children_dataset.add(curr_dataset);
	}
    }	


    /***************************************************************************************************************/

}
