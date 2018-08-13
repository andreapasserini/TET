package tet.rnn;

import tet.Pair;

import java.util.*;

abstract public class ActivationFunction{
    
    public ActivationFunction()
    {
    }
    
    public ActivationFunction(String params)
    {
    }

    public ActivationFunction(String[] params)
    {
    }

    public float minValue()
    {
	return 0;
    }

    public float maxValue()
    {
	return 1;
    }

    abstract public float forward(float[][] input);

    abstract public float forward(Pair<Float, Integer>[][] input);

    abstract public float getParameterAt(int i);

    abstract public int getParametersNumber();

    abstract public float[] backward(FloatValue value, float chain, GradientTree gradient);

    abstract public void optimizeParameters(Optimizer optimizer, GradientTree gradient);

    abstract public String parametersToString();

    abstract public void setParameters(StringTokenizer tokenizer);

    abstract public void setParameters(String parameters);

    abstract public void setRandomParameters(RandomWeight rw);

    abstract public String Serialize();
}
