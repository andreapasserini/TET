package tet;

import java.util.*;
import mymath.*;

public class RelObject{

    private String name;
    
    public RelObject(String n)
    {
	name = n;
    }

    /** Returns the name of this relational object */
    public String name()
    {
	return name;
    }

    public String toString()
    {
	return name;
    }

    /** Tests whether obj is a relational object with the same name as this relational object */
    public boolean equals(Object obj){
	if (obj instanceof RelObject && ((RelObject)obj).name().equals(name))
	    return true;
	else
	    return false;
    }
    
}
