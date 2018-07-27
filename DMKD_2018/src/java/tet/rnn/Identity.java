package tet.rnn;

import java.util.*;

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

    public String Serialize()
    {
	return type;
    }
}
