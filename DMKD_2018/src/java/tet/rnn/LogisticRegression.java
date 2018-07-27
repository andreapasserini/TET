package tet.rnn;

import java.util.*;
import java.io.*;

public class LogisticRegression extends ActivationFunction{

    // bias weight
    float beta_0;

    // input weights
    float[] beta;

    // activation type
    String type;

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

    public float minValue()
    {
	return 0;
    }

    public float maxValue()
    {
	return 1;
    }
}
