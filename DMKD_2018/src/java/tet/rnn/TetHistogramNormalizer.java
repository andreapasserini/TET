package tet.rnn;

import java.util.*;
import java.sql.*;
import java.io.*;
import tet.*;
import myio.*;

public class TetHistogramNormalizer{
    
    Normalization normalization = Normalization.NONE; 
    double max_nonzero_count = 0.0;
    Vector<TetHistogramNormalizer> children;

    public TetHistogramNormalizer(Tet tet, Normalization normalization)
    {
	this.normalization = normalization;
	children = new Vector<TetHistogramNormalizer>();
	try{
	    for(int i = 0; i < tet.getNumChildren(); i++)
		children.add(new TetHistogramNormalizer(tet.getChild(i).getSubTree(), normalization));
	} catch(Exception e){e.printStackTrace();}	
    }

    public void updateStatistics(Collection<TetHistogram> histos)
    {
    	for (Iterator<TetHistogram> it = histos.iterator(); it.hasNext();) 
    		updateStatistics(it.next());
    }
    
    public void updateStatistics(TetHistogram histo)
    {
	double nonzero_count = histo.histogram.sum() - histo.histogram.origin();
	
	if(max_nonzero_count <  nonzero_count)
	    max_nonzero_count = nonzero_count;
    
	for(int i = 0; i < children.size(); i++)
	    children.elementAt(i).updateStatistics(histo.children.elementAt(i));
    }

    public void normalize(Collection<TetHistogram> histos) throws Exception
    {
		for (Iterator<TetHistogram> it = histos.iterator(); it.hasNext();) 
    		normalize(it.next());
    }


    public void normalize(TetHistogram histo) throws Exception
    {
	switch(normalization) 
	    {
	    case NONE:
		break;		    
	    case ACTIVE_SIZE:
		double nonzero_count = histo.histogram.sum() - histo.histogram.origin();
		double new_zero_count = Math.max(max_nonzero_count - nonzero_count, 0.0);
		histo.histogram.init(0, new_zero_count);
		histo.histogram.normalize();
		break;
	    case PLAIN:
		histo.histogram.normalize();
		break;		
	    default:
		throw new Exception("Unsupported normalization method: " + normalization);
	    }
    	
	for(int i = 0; i < children.size(); i++)
	    children.elementAt(i).normalize(histo.children.elementAt(i));
    }

}   

