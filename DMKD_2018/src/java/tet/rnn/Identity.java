package tet.rnn;

import java.util.*;
import tet.Pair;

public class Identity extends ActivationFunction{
    
    // activation type
    String type = "identity";

    public Identity()
    {
    }
    
    public Identity(String params)
    {
    }

    public Identity(String[] params)
    {
    }

    public float forward(float[][] input)
    {
	return input[0][0];
    }

    public float forward(Pair[][] input) {return 1;}

    public float getParameterAt(int i){
        return 0;
    }

    public int getParametersNumber(){ return 0; }

    public float[] backward(FloatValue value, float chain, GradientTree gradient){ return new float[0]; }

    public void optimizeParameters(Optimizer optimizer, GradientTree gradient){
        return;
    }

    public String parametersToString(){ return ""; }

    public void setParameters(StringTokenizer tokenizer){ return; }

    public void setParameters(String parameters) { return; }

    public void setRandomParameters(RandomWeight rw) { return; }

    public String Serialize()
    {
	return type;
    }
}
