package tet.rnn;

import java.util.*;
import java.sql.*;
import java.io.*;
import tet.*;

public class MultiHistogram{

    public int bins;
    public int dimensions;
    public float min_value;
    public float max_value;
    public Tensor tensor;
    public double mass;
    
    protected Marginals marginals;
    protected float bin_size;
    
    public MultiHistogram()
    {

    }

    public MultiHistogram(int bins, float min_value, float max_value)
    {
    	this.bins = bins;
        this.dimensions = 1;
    	this.min_value = min_value;
    	this.max_value = max_value;
    	this.tensor = new Tensor(bins, dimensions);    
        this.marginals = null;    
    	init();
    }

    public MultiHistogram(MultiHistogram histogram)
    {
    	this.bins = histogram.bins;
        this.dimensions = histogram.dimensions;
    	this.min_value = histogram.min_value;
    	this.max_value = histogram.max_value;
    	this.tensor = new Tensor(histogram.tensor);
        this.marginals = null;
    	init();
    }
    
    public void fill(float input)
    {
    	/* fill histogram */
    	tensor.increment(get_bin(input));
    }

    public void fill(int bin, int value)
    {
	   tensor.add(bin, value);
    }

    public void init(int bin, double value)
    {
	   tensor.init(bin, value);
    }

    public void normalize()
    {
        this.mass = tensor.sum();    
    	tensor.normalize();
    }

    public double sum()
    {
	   return tensor.sum();
    }

    public double origin()
    {
	   return tensor.data[0];
    }

    public void add(MultiHistogram histogram)
    {
	   /* add a histogram */
	   tensor.add(histogram.tensor);
    }

    public void extend(float input)
    {
        dimensions++;
	   /* add a dimension */
	   tensor.extend(get_bin(input));
    }
     
    public String toString()
    {
    	String output = String.format("%d\t%d\t%f\t%f\n", bins, dimensions, min_value, max_value);
        if (tensor != null)
        	output += tensor;
        if (marginals != null)
            output += marginals;
    	return output;
    }

    public String toFormattedString()
    {
    	String output = String.format("bins=%d\tdimensions=%d\tmin_value=%f\tmax_value=%f\n", 
    				      bins, dimensions, min_value, max_value);
        if (tensor != null)
        	output += tensor.toFormattedString();
        if (marginals != null)
            output += marginals.toFormattedString();
    	return output;
    }

    protected void init()
    {
	   bin_size = (max_value-min_value)/bins;
    }

    protected int get_bin(float input)
    {
    	if (input == max_value)
    	    return bins-1;
    	else
    	    return (int)((input-min_value) / bin_size);
    }

    public void marginalize()
    {
        marginals = new Marginals(tensor);

        // dereferencing tensor for space efficiency
        //tensor = null; //DEBUG
    }

    
    public Marginals getMarginals()
    {
        if (marginals == null)
            marginalize();

        return marginals;
    }

    public double[] getMarginalCumulatives()
    {
        if (marginals == null)
            marginalize();

        return marginals.cumulative;
    }

    //Manfred:
    public int dimensions(){
	return tensor.dimensions;
    }
}
