package Tetpackage.learner;

import Tetpackage.*;

import java.util.*;
import java.io.*;
import mymath.*;

public class Example{
    
    HashMap<String, Object> objmap;
    float target;
  
    public Example()
    {
	objmap = new HashMap<String, Object>();
    }
    
    public void addTarget(float target)
    {
	this.target = target;
    }
    
    public void addRelObject(String varname, String relobjname)
    {
	objmap.put(varname, new RelObject(relobjname));
    }

    public float getTarget() 
    {
	return this.target;
    }

    public HashMap<String, Object> getRelObjects() 
    {
	return this.objmap;
    }
    
}
