package tet.learner;

import tet.*;

import java.util.*;
import java.io.*;
import mymath.*;

public interface TetLearnerPolicy{
    
    public Tet newTet(Type root_type, TreeSet<Variable> freevars, Dataset root_dataset) throws Exception;

    public boolean maxDepthReached(int depth);

    public boolean maxScoreReached(float score);

    public boolean maxRejectionsReached(int rejections);

    public float scoreTET(Tet tet, Dataset curr_dataset) throws Exception; 

    public Extensions generateExtensions(VariableByType curr_variables, 
					 String currvarprefix) throws Exception;
    public Extensions filterExtensions(Extensions extensions, 
				       Relation targetrel, 
				       Vector<Type> prohibited_types,
				       TreeSet<Variable> parent_variables,
				       TreeSet<Variable> newest_variables,
				       TreeSet<Variable> root_variables,
				       VariableByType root_variables_by_type);
    
    public Vector<ScoredExtension> scoreExtensions(Extensions extensions, 
						   VariableByType root_variables, 
						   Dataset root_dataset,
						   int depth) throws Exception;

    public PriorityQueue<ScoredExtension> selectExtensions(Vector<ScoredExtension> extensions);

    public void extendTet(Tet root_tet, Dataset root_dataset, float root_tet_score, 
			  Tet parent_tet, Dataset parent_dataset,
			  ScoredTetChild[] tetchildren) throws Exception;

    public boolean acceptExtension(float prev_score, float ext_score);

    public boolean greedySearch(int depth);

    public Vector<Type> newProhibitedTypes(Vector<Type> old_prohibited_types, 
					   ExtensionType curr_extension_type);


    public float learnParameters(Tet root_tet, Dataset root_dataset, 
				 Tet tet, Dataset dataset) throws Exception;

    public void learnNodeParameters(Tet tet, Dataset dataset);

    public float learnOverallParameters(Tet tet, Dataset dataset) throws Exception;

    public Dataset getSatisfyingDataset(Type type, 
					TreeSet<Variable> allvars, 
					Dataset parent_dataset,
					Type other_type,
					Dataset other_type_dataset,
					boolean isLeaf) throws Exception;

    public Dataset getSatisfyingDataset(Type type, 
					TreeSet<Variable> allvars, 
					Dataset curr_dataset,
					boolean isLeaf) throws Exception;

    public void freezeExtensionCache(Tet tet) throws Exception;

    public void cleanupExtensionCache(Tet roottet, Tet extensiontet) throws Exception;
}
