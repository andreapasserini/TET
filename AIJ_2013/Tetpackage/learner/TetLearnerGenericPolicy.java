package Tetpackage.learner;

import Tetpackage.*;

import java.util.*;
import java.io.*;
import mymath.*;

public abstract class TetLearnerGenericPolicy implements TetLearnerPolicy{

    int max_depth = 3; 
    float selection_score_entity_threshold = 0;
    float selection_score_attribute_threshold = 0;
    float evaluation_score_threshold = 0;
    int max_new_vars_num = 2;   
    int min_greedy_search_depth = 3;
    int max_extension_num = 0;
    float max_evaluation_score = 1;
    int max_rejections = Integer.MAX_VALUE;
    boolean skip_negated = false;
    boolean use_set_difference = false;    
    boolean use_gig = false;

    LiteralFactory litfactory;
    
    StringCounter table_counter;

    public TetLearnerGenericPolicy(BufferedReader factoryconfig, 
				   TetLearnerCommandLineOptions options) throws Exception
    {
	litfactory = new LiteralFactory(factoryconfig);

        max_depth = options.max_depth;
	selection_score_entity_threshold = options.selection_score_entity_threshold;
	selection_score_attribute_threshold = options.selection_score_attribute_threshold;
	evaluation_score_threshold = options.evaluation_score_threshold;
	max_new_vars_num = options.max_new_vars_num;
	min_greedy_search_depth = options.min_greedy_search_depth;
	max_extension_num = options.max_extension_num;
	max_evaluation_score = options.max_evaluation_score;
	max_rejections = options.max_rejections;
	skip_negated = options.skip_negated;
	use_set_difference = options.use_set_difference;
	use_gig = options.use_gig;

	table_counter = new StringCounter();
    }

    /* abstract method to be implemented by extended classes */
    public abstract float scoreTET(Tet tet, Dataset dataset) throws Exception;

    public Tet newTet(Type root_type, TreeSet<Variable> freevars, Dataset root_dataset) throws Exception
    {
	return new Tet(root_type, freevars);
    }
        
    public void setNodeParameters(Tet tet, Dataset dataset)
    {
	/* do nothing by default */
    }

    public void freezeExtensionCache(Tet tet) throws Exception
    {
	/* do nothing by default */
    }

    public void cleanupExtensionCache(Tet roottet, Tet extensiontet) throws Exception
    {
	/* do nothing by default */
    }

    public boolean maxDepthReached(int depth)
    {
	return depth >= max_depth;
    }

    public boolean maxScoreReached(float score)
    {
	return score >= max_evaluation_score;
    }

    public boolean maxRejectionsReached(int rejections)
    {
	return rejections >= max_rejections;
    }        

    public boolean greedySearch(int depth)
    {
	return (depth >= min_greedy_search_depth);
    }

    public PriorityQueue<ScoredExtension> selectExtensions(Vector<ScoredExtension> scored_extensions)
    {
	PriorityQueue<ScoredExtension> scored_extensions_queue = new PriorityQueue<ScoredExtension>();
	
	for(int i = 0; i < scored_extensions.size(); i++)
	    if(retainExtension(scored_extensions.elementAt(i)))
		scored_extensions_queue.add(scored_extensions.elementAt(i));
	    else
		scored_extensions.elementAt(i).cleanup();
	
	/* if retained extensions are no more than the maximum number allowed return them */
	if(max_extension_num == 0 || scored_extensions_queue.size() <= max_extension_num)
	    return scored_extensions_queue;

	/* otherwise pop the best ones to the number allowed */
	PriorityQueue<ScoredExtension> selected_scored_extensions_queue = new PriorityQueue<ScoredExtension>();

	for(int i = 0; i < max_extension_num; i++)
	    selected_scored_extensions_queue.add(scored_extensions_queue.poll());
	
	// clean up remaining extensions
	while(scored_extensions_queue.size() > 0)
	    scored_extensions_queue.poll().cleanup();

	return selected_scored_extensions_queue;
    }
    
    public Extensions generateExtensions(VariableByType curr_variables, 
					 String currvarprefix) throws Exception
    {
	// create all literals with current variables and possibly new ones
	litfactory.generateLiterals(curr_variables, currvarprefix);
	
	Extensions extensions = new Extensions();
	// return Extensions from vector of Pair<Literal,VariableByType> 
	Pair<Literal,VariableByType> currpair;      
        while((currpair = litfactory.next()) != null)
	    extensions.addAll(generateExtensions(currpair));
	
	return extensions;
    }

 
    /* remove extensions that shouldn't be evaluated, not even with prior score
     * and correct extensions possibly adding inequality constraints 
     */ 
    public Extensions filterExtensions(Extensions extensions, 
				       Relation targetrel, 
				       Vector<Type> prohibited_types,
				       TreeSet<Variable> parent_variables,
				       TreeSet<Variable> newest_variables,
				       TreeSet<Variable> root_variables,
				       VariableByType root_variables_by_type)
    {
	Extensions allowed_extensions = new Extensions();

	/* recover number on new variables introduced so far in this branch */
	int new_vars = parent_variables.size() - root_variables.size();

	for(int i = 0; i < extensions.size(); i++){
	    /* skip extensions that contain the target relation 
	       instantiated with the root variables */
	    if(extensions.elementAt(i).matchesRelationWithVariables(targetrel, root_variables))
		continue;
	    /* skip extensions that contain more new variables than the maximum allowed */
	    if((extensions.elementAt(i).maxNewVarsNum() + new_vars) > max_new_vars_num)
		continue;
	    /* skip extensions that do not contain any of the newest variables */
	    if(!extensions.elementAt(i).containsVariable(newest_variables))
		continue;
	    /* skip extensions that matches exactly a type in prohibited list */
	    if(matchesTypeList(extensions.elementAt(i), prohibited_types))
		continue;	    
	    // the following are subsets of the previous filters:
// 	    /* skip extensions that contain attributes with no newest variables */
// 	    if(!extensions.elementAt(i).attributesContainVariable(newest_variables))
// 		continue;	    
// 	    /* skip extensions that contain only new variables */
// 	    if(!extensions.elementAt(i).containsVariable(parent_variables))
// 		continue;
	    allowed_extensions.add(correctExtension(extensions.elementAt(i), root_variables_by_type));
	}
	
	return allowed_extensions;
    }
    
    
    public Vector<Type> newProhibitedTypes(Vector<Type> old_prohibited_types, ExtensionType curr_extension_type)
    {
	/* create a new prohibition list */
	Vector<Type> new_prohibited_types = new Vector<Type>();
		
	
	/* if new extension type is attribute, old prohibition list still counts */
	if(curr_extension_type.isAttributeExtensionType())
	    /* add types in prohibition list to the new prohibition list */
	    new_prohibited_types.addAll(old_prohibited_types);
    
	/* create new prohibited type from current extension type, discarding operators */
	/* WARNING: not always operators should be ignored maybe */
	Type new_prohibited_type = new Type();       	
	
	/* recover type literals from current extension */
	Vector<Literal> curr_extension_type_literals = curr_extension_type.getType().getLiterals();

	/* browse literals and add each to new_prohibited_type if it's not an operator */
	for(int i = 0; i < curr_extension_type_literals.size(); i++)
	    if(!curr_extension_type_literals.elementAt(i).isOperator())
		new_prohibited_type.addLiteral(curr_extension_type_literals.elementAt(i));

	/* check if the type contains only symmetric literals, in which case
	   add symmetric versions to prohibition list */
	if(new_prohibited_type.isSymmetricType() && new_prohibited_type.getNumLiterals() > 0){
	    Vector<Type> sym_literal_types = new Vector<Type>();	    
	    /* recover type literals */
	    Vector<Literal> new_prohibited_type_literals = new_prohibited_type.getLiterals();
	    /* insert first literal and its symmetric */
	    sym_literal_types.add(new Type(new_prohibited_type_literals.elementAt(0)));
	    sym_literal_types.add(new Type(new_prohibited_type_literals.elementAt(0).getSymmetricLiteral()));
	    for(int i = 1; i < new_prohibited_type_literals.size(); i++){
		Literal curr_literal = new_prohibited_type_literals.elementAt(i);
		Literal curr_sym_literal = curr_literal.getSymmetricLiteral();
		Vector<Type> new_sym_literal_types = new Vector<Type>();
		/* iterate over current sym literal types */
		for(int j = 0; j < sym_literal_types.size(); j++){
		    /* clone type */
		    new_sym_literal_types.add(sym_literal_types.elementAt(j).clone());
		    /* add current literal */
		    sym_literal_types.elementAt(j).addLiteral(curr_literal);
		    /* add symmetric of current literal */
		    new_sym_literal_types.elementAt(j).addLiteral(curr_sym_literal);
		}
		/* add new sym literal types */
		sym_literal_types.addAll(new_sym_literal_types);
	    }
	    /* add symmetric literal types */
	    new_prohibited_types.addAll(sym_literal_types);	
	}
	else // only add new_prohibited_type
	    new_prohibited_types.add(new_prohibited_type);
	
	return new_prohibited_types;
    }
    
    public Vector<ScoredExtension> scoreExtensions(Extensions extensions, 
						   VariableByType root_variables, 
						   Dataset root_dataset,
						   int depth) throws Exception
    {
	Vector<ScoredExtension> scored_extensions = new Vector<ScoredExtension>();

	/* get satisfying datasets */
	getSatisfyingDatasets(extensions, root_variables, root_dataset, maxDepthReached(depth+1));
	
	/* get target distribution in root dataset  */
	TreeMap<Float,Float> root_distribution = root_dataset.getTargetDistribution();

	/* compute root dataset entropy */
	Pair<Float, Integer> root_entropy = Scorer.computeEntropy(root_distribution.values());
	
	System.out.println("Current entropy = " + root_entropy.first() + " (" + root_entropy.second() + ")");

	/* score extensions */
	for(int i = 0; i < extensions.size(); i++)
	    scored_extensions.add(new ScoredExtension(scoreExtension(extensions.elementAt(i), root_distribution, root_entropy), 
						      extensions.elementAt(i)));
	
	return scored_extensions;
    }

    public float scoreExtension(Extension extension, TreeMap<Float,Float> root_distribution, Pair<Float, Integer> root_entropy) 
    {
	/* compute extension generalized entropy */
	Pair<Float, Integer> extension_entropy;

	try{
	    extension_entropy = extension.computeEntropy(root_distribution, use_gig);
	}
	catch(java.lang.ArithmeticException e){
	    /* division by zero exception implies the extension 
	       dataset has zero size, return zero score */ 
	    System.out.println("Candidate extension score = 0.0  /  Nan (0)  /  " + extension); 	    
	    return 0;
	}

	/* the size in extension_entropy compared to that in root_entropy 
	 *  gives an idea of how much the extension induces a partition
	 * in the dataset 
	 */

	System.out.println("Candidate extension score = " + (root_entropy.first()-extension_entropy.first()) + "  /  " 
			   + extension_entropy.first() + " (" + extension_entropy.second() + ")  /  " + extension); 	    

	return root_entropy.first()-extension_entropy.first(); 
    }
    
    public boolean acceptExtension(float prev_score, float ext_score)
    {
	System.out.println(ext_score + " - " + prev_score + " = " + (ext_score - prev_score)); 
	return (ext_score - prev_score) > evaluation_score_threshold;
    }


    public void extendTet(Tet root_tet, Dataset root_dataset, float root_tet_score, 
			  Tet parent_tet, Dataset parent_dataset,
			  ScoredTetChild[] tetchildren) throws Exception
    {
	/* check that tetchildren have at least a candidate */
	if(tetchildren.length == 0)
	    return;

	/* create priority queue of scored children */
	PriorityQueue<ScoredTetChild> scoredtetchildren = new PriorityQueue<ScoredTetChild>(tetchildren.length);
	
	/* iterate over possible tet children and score each of them by attaching it to root_tet */
	System.out.println("-------------------------------------------------------------");
	System.out.println("Scoring possible tet extensions:");
	for(int i = 0; i < tetchildren.length; i++){
	    /* add current child to parent tet */
	    parent_tet.addChild(tetchildren[i].getChild());
	    /* score new tet */ 
	    float new_tet_score = learnParameters(root_tet, root_dataset, 
						  parent_tet, parent_dataset);
	    System.out.println("Candidate tet score: " + new_tet_score);
	    System.out.println(root_tet.toFormattedString());

	    /* insert score and tetchild into priority queue */
	    scoredtetchildren.add(new ScoredTetChild(new_tet_score, tetchildren[i].getChild()));
	    /* remove current child from parent tet */
	    try{
		parent_tet.removeLastChild();
	    } catch(Exception e){
		// should never happen as a child was just added
		e.printStackTrace();
		System.exit(1);
	    }
	}
	
	/* recover best scoring tetchild */
	ScoredTetChild bestscoredtetchild = scoredtetchildren.poll();
	float bestscore = bestscoredtetchild.getScore();
	TetChild besttetchild = bestscoredtetchild.getChild();
	
	System.out.println("-------------------------------------------------------------");
	System.out.println("Verifying if best tet extension satisfies acceptance conditions:");

	/* verify if its score is sufficient for acceptance */
	if(acceptExtension(root_tet_score, bestscore)){

	    /* add best child to parent tet */
	    parent_tet.addChild(besttetchild);	    
	    System.out.println("Accepted tet score: " + bestscore + " > " + root_tet_score);
	    System.out.println(root_tet.toFormattedString());

	    /* freeze recursive tet cache */
	    freezeExtensionCache(besttetchild.getSubTree());

	    /* try to extend new tet with remaining children */
	    extendTet(root_tet, root_dataset, bestscore, parent_tet, parent_dataset, 
		      (ScoredTetChild[])scoredtetchildren.toArray(new ScoredTetChild[0]));
	}
	else
	    System.out.println("Refused tet extension score: " + bestscore + " <= " + root_tet_score + " / " + besttetchild);
	
	System.out.println("Finished scoring possible tet extensions");
	System.out.println("-------------------------------------------------------------");	
    }

    public Dataset getSatisfyingDataset(Type type, 
					TreeSet<Variable> allvars, 
					Dataset parent_dataset,
					Type other_type,
					Dataset other_type_dataset,
					boolean isLeaf) throws Exception
    {	
	if((use_set_difference || skip_negated) && type.isNegationOf(other_type))
	    /* treat special case where current type is negation of previous one */	    
	    return getDatasetDifference(parent_dataset, allvars, type.getVariables(), 
					other_type_dataset, isLeaf);	
	
	return getSatisfyingDataset(type, allvars, parent_dataset, isLeaf);
    }

    public Dataset getSatisfyingDataset(Type type, 
					TreeSet<Variable> allvars, 
					Dataset curr_dataset,
					boolean isLeaf) throws Exception
    {
	String tmptabname = table_counter.next();
	
	return curr_dataset.getSatisfyingDataset(type, tmptabname, allvars, isLeaf);
    }


    /******************************************************************************************************/
    /********************************** private methods ***************************************************/

    /* generate extensions associated with candidate literal */
    private Extensions generateExtensions(Pair<Literal,VariableByType> litvarpair)
    {
	Extensions extensions = new Extensions();
	
	/* if literal introduces new variables (entity introducing) simply add it */
	if(litvarpair.second().size() != 0)
	    extensions.add(new Extension(litvarpair));
	else 
	    extensions.add(new Extension(litvarpair, true));
	
	return extensions;
    }

    private boolean retainExtension(ScoredExtension scored_extension)
    {
	float score = scored_extension.getScore();
	Extension extension = scored_extension.getExtension();	  
	
	/* check correct threshold */
	if(extension.isEntityExtension()){
	    if(score <= selection_score_entity_threshold)
		return false;
	}
	else if(score <= selection_score_attribute_threshold)
	    return false;
	
	if(skip_negated) /* remove types with negated literals from extension */
	    extension.removeNegated();

	System.out.println("Retained extension score = " + score + " / " + extension); 
	
	return true;
    }

    private void getSatisfyingDatasets(Extensions extensions, 
				       VariableByType root_variables, 
				       Dataset root_dataset,
				       boolean isLeaf) throws Exception
    {
	for(int i = 0; i < extensions.size(); i++){
	    Extension curr_extension = extensions.elementAt(i);
	    // iterate over types within extension	    
	    for(int j = 0; j < curr_extension.size(); j++){
		// get curr extensiontype
		ExtensionType curr_extension_type = curr_extension.elementAt(j);
		// compute all variables
		curr_extension_type.computeAllVars(root_variables);
		
		// get satisfying dataset
		System.out.println("Recovering dataset for extension type: " + curr_extension_type); 
		long timebefore = System.currentTimeMillis();
		Type curr_type = curr_extension_type.getType();
		TreeSet<Variable> allvars = curr_extension_type.getAllVariables().getVariables();

		if(j>0) /* call general method which checks whether to use information from previous type computation */
		    curr_extension_type.setDataset(getSatisfyingDataset(curr_type, allvars, root_dataset, 
									curr_extension.elementAt(j-1).getType(),
									curr_extension.elementAt(j-1).getDataset(),
									isLeaf));
		else
		    curr_extension_type.setDataset(getSatisfyingDataset(curr_type, allvars, root_dataset, isLeaf));

		System.out.println("Took: " + (System.currentTimeMillis()-timebefore) + " milliseconds");
	    }
	}
    }
    
    private Dataset getDatasetDifference(Dataset fullset,
					 TreeSet<Variable> allvars, 
					 TreeSet<Variable> diffvars, 
					 Dataset subset,
					 boolean isLeaf) throws Exception
    {
	String tmptabname = table_counter.next();
	
	return fullset.getDatasetDifference(subset, tmptabname, allvars, diffvars, isLeaf, skip_negated);
    }


    private Extension correctExtension(Extension extension, VariableByType root_variables)
    {
	/* iterate over extension types */
	for(int ex = 0; ex < extension.size(); ex++){	 
	    
	    /* recover type of current extension type */
	    Type type = extension.elementAt(ex).getType(); 
	
	    /* recover newly introduced variables by type as an entry set */ 
	    Set<Map.Entry<String,TreeSet<Variable>>> entryset = extension.elementAt(ex).getNewVariablesByType().entrySet();
        
	    /* iterate over it */
	    for(Iterator<Map.Entry<String,TreeSet<Variable>>> i = entryset.iterator(); i.hasNext() ;){

		/* recover set entry */
		Map.Entry<String,TreeSet<Variable>> entry = i.next();
		
		/* check if current entry type is within those of root variables */
		TreeSet<Variable> root_entry_vars = root_variables.get(entry.getKey());
		
		/* skip entry if not */
		if(root_entry_vars == null)
		    continue;
		
		/* for each new variable v and root variable r pair with matching type, add a <>(v,r) literal */
		for(Iterator<Variable> e = entry.getValue().iterator(); e.hasNext();){
		    
		    /* create vector of relation arguments types */
		    VariableType[] atype = new VariableType[2];
		    
		    /* recover entry variable */
		    Variable entryvar = e.next();
		    
		    /* insert entry and root variable types */
		    atype[0] = atype[1] = new VariableType(entry.getKey());
		    
		    for(Iterator<Variable> r = root_entry_vars.iterator(); r.hasNext();){
			
			/* create vector of literal variables */
			Variable[] ne_variables = new Variable[2]; 
			
			/* insert entry variable */
			ne_variables[0] = entryvar;
			
			/* insert root variable */
			ne_variables[1] = r.next();
			
			/* create literal and append to type */
			type.addLiteral(new Literal(new Relation(new String("<>"), 2, atype), false, ne_variables));
		    }
		}	    
	    }
	}

	/* return corrected extension */
	return extension;   
    }

    /**
     * extension matches at least one of the types in  prohibited_types list
     */
    private boolean matchesTypeList(Extension extension, Vector<Type> prohibited_types)
    {
	for(int t = 0; t < prohibited_types.size(); t++)
	    if(extension.allTypesMatch(prohibited_types.elementAt(t)))
		return true;
	return false;
    }

}  
