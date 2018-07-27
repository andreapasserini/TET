package tet;

import java.util.*;

public class OperatorSet
{
    public static String[] operators = {"<", "<=", "=", ">", ">=", "<>"};
    
    public static boolean isOperator(String op)
    {
	for(int i = 0; i < operators.length; i++)
	    if(operators[i].equals(op))
		return true;
	
	return false;
    }
}