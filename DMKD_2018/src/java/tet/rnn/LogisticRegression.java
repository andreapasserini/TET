package tet.rnn;

import java.util.*;
import java.io.*;
import tet.Pair;

public class LogisticRegression extends ActivationFunction{

    // bias weight
    float beta_0;

    // input weights
    float[] beta;

    // activation type
    String type;

    int iteration = 0;

    public LogisticRegression(int num_inputs)
    {
	init(num_inputs);
    }

    public LogisticRegression(String params)
    {
	this(params.split(","));
    }

    public LogisticRegression(String[] params) 
    {
	// recover type
	type = params[0];

	// recover bias
        beta_0 = Float.parseFloat(params[1]);

	// recover input weights
	beta = new float[params.length-2];
	for(int i = 0; i < beta.length; i++)
	    beta[i] = Float.parseFloat(params[2+i]);
    }
    

    public void init(int num_inputs)
    {
	RandomWeight rnd = new RandomWeight();
	beta_0 = rnd.nextWeight();
	beta = new float[num_inputs];
	for(int i = 0; i < num_inputs; i++)
	    beta[i] = rnd.nextWeight();	
    }

    public float forward(float[][] input)
    {
	float output = beta_0;

	for(int i = 0; i < input.length; i++){
	    float curr_output = 0;
	    for(int j = 0; j < input[i].length; j++)
		curr_output += input[i][j];
	    output += beta[i]*curr_output;
	}

	return sigmoid(output);
    }

    // Compute the logistic evaluation from a TET value
    public float forward (Pair<Float, Integer>[][] input)
    {
        float output = beta_0;

        for (int i = 0; i < input.length; i++) {
            float curr_output = 0;
            for (int j = 0; j < input[i].length; j++)
                curr_output += input[i][j].first() * input[i][j].second();
            output += beta[i] * curr_output;
        }
        return sigmoid(output);
    }
    
    public static float sigmoid(float x){
	
	return (float) 1 /  (float) (1 + Math.exp(-x));
    }

    public String Serialize()
    {
	StringBuffer buf = new StringBuffer(1024);
	
	buf.append(type + "," + beta_0);
	for(int i = 0; i < beta.length; i++)
	    buf.append("," + beta[i]);

	return buf.toString();	 
    }

    public float getParameterAt(int i){ return beta[i]; }

    public int getParametersNumber(){ return (1 + beta.length); }

    public float[] backward(FloatValue tetvalue, float chain, GradientTree gradient){

        float[] chains = new float[tetvalue.getNumberOfMultisets()];
        float activationder = tetvalue.getTopValue() * (1 - tetvalue.getTopValue());
        // Add to the gradient node the partial derivative of this value

        // Add the bias partial der at pos 0 in gradient
        gradient.sumAt(0, chain * activationder);
        for (int i = 0; i < tetvalue.getNumberOfMultisets(); i++){
            // Compute the derivatives of the child weights
            FloatMultiset multiset = tetvalue.getMultisetAt(i);

            float evaluationsum = 0;
            for (int j = 0; j < multiset.getNumberOfElements(); j++){
                evaluationsum += multiset.getValueAt(j).getTopValue() * multiset.getCountAt(j);
            }

            // Add ith weight partial der to gradient
            gradient.sumAt(i + 1, chain * activationder * evaluationsum);

            // Compute the chain values for the children nodes
            chains[i] = chain * activationder * beta[i];
        }
        return chains;
    }

    // Update the parameters of the rnn_Tet
    public void optimizeParameters(Optimizer optimizer, GradientTree gradient){
        float[] parameters = new float[1+beta.length];
        /* Put the values of the parameters as array and pass it to the optimizer */
        parameters[0] = beta_0;
        for (int i = 0; i < beta.length; i++)
            parameters[i+1] = beta[i];

        // Pass to the optimizer the current values of the parameters and return
        //the updated values
        float [] new_parameters = optimizer.optimize(parameters, gradient.getPartials());

        /* Update parameters */
        beta_0 = new_parameters[0];
        for (int i = 0; i < beta.length; i++) {
            beta[i] = new_parameters[i+1];
        }
    }

    public String parametersToString(){
        String str = String.valueOf(beta_0);
        for (int i = 0; i < beta.length; i++)
            str += "," + beta[i];
        return str + ",";
    }

    public void setParameters(StringTokenizer tokenizer){
        beta_0 = Integer.valueOf(tokenizer.nextToken(","));
        beta[0] = Integer.valueOf(tokenizer.nextToken(","));
    }

    public void setParameters(String parameters){
        String[] splitted = parameters.split(",");

        beta_0 = Float.parseFloat(splitted[0]);
        beta = new float[beta.length];
        for (int i = 0; i < beta.length; i++)
            beta[i] = Float.parseFloat(splitted[i+1]);
    }

    public void setRandomParameters(RandomWeight rw){
        beta_0 = rw.nextWeight();
        for (int i = 0; i < beta.length; i++){
            beta[i] = rw.nextWeight();
        }
    }

    public float minValue()
    {
	return 0;
    }

    public float maxValue()
    {
	return 1;
    }

}
