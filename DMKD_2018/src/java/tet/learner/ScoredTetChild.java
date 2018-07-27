package tet.learner;

import tet.*;

import java.util.*;
import mymath.*;

public class ScoredTetChild implements Comparable{

    float score;
    TetChild tetchild;
    
    public ScoredTetChild(float score, TetChild tetchild)
    {
	this.score = score;
	this.tetchild = tetchild;
    }
    
    public float getScore()
    {
	return score;
    }
    
    public TetChild getChild()
    {
	return tetchild;
    }

    public int compareTo(Object o){

        int result = 0;
        try{
	    /* PriorityQueue uses ascending order */
            result = (int)Math.signum(((ScoredTetChild)o).getScore() - score);
        }catch (ClassCastException e){
             System.out.println("ClassCastException:" + e + " in function ScoredTetChild.compareTo");
             System.exit(1);
        }
        return result;
    }
}