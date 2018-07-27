package Tetpackage.learner;

import Tetpackage.*;

import java.util.*;
import java.io.*;
import mymath.*;

public interface Dataset{
       
    public Dataset getSatisfyingDataset(Type type, 
					String relation_name, 
					TreeSet<Variable> relation_vars, 
					boolean isLeaf) throws Exception;

    public Dataset getDatasetDifference(Dataset subset, 
					String relation_name, 
					TreeSet<Variable> relation_vars,
					TreeSet<Variable> difference_vars,
					boolean isLeaf,
					boolean skipNegated);

    public Vector<Example> getExamples();
    
    public RelStructure getRelStructure();
    
    public String getRelationName();
	
    public TreeSet<Variable> getRelationVars();

    public float getTargetProportion(float target);

    public TreeMap<Float,Float> getTargetDistribution();

    public Pair<Float,Integer> computeEntropy() throws java.lang.ArithmeticException;

    public Pair<Float,Integer> computeEntropy(TreeSet<Variable> newvars, TreeMap<Float,Float> root_distribution, boolean use_gig) 
	throws java.lang.ArithmeticException;

    public void cleanup();

    public Integer[] getCountsByVariables(Dataset parent_dataset, Variable[] parent_variables);

    public int getSize();

    public Dataset getValueDataset(String table_name, TreeSet<Variable> bound_variables, float dpos, float dneg);

    public void aggregateValues(String table_name, Dataset child_dataset, 
				TreeSet<Variable> bound_variables, float dpos, float dneg, 
				Operator branch_operator, Operator value_operator);

    public void reweightValues(float dpos, float dneg);

    public Float[] computeValues();

    public void printTable();
}
