package tet.learner;

import tet.*;

import java.util.*;
import mymath.*;

public class ScoredExtension implements Comparable{

    float score;
    Extension extension;
    
    public ScoredExtension(float score, Extension extension)
    {
	this.score = score;
	this.extension = extension;
    }
    
    public float getScore()
    {
	return score;
    }
    
    public Extension getExtension()
    {
	return extension;
    }

    public int compareTo(Object o){

        int result = 0;
        try{
	    /* PriorityQueue uses ascending order */
            result = (int)Math.signum(((ScoredExtension)o).getScore() - score);
        }catch (ClassCastException e){
             System.out.println("ClassCastException:" + e + " in function ScoredExtension.compareTo");
             System.exit(1);
        }
        return result;
    }

    public void cleanup()
    {
	extension.cleanup();
    }
}
