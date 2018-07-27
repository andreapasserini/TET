package tet.rnn;

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

    abstract public String Serialize();
}
