package tet.rnn;

import java.util.*;
import java.sql.*;
import java.io.*;
import tet.*;
import com.google.common.primitives.Doubles;

public class Tensor{
    
    public int size;
    public int dimensions;
    public double[] data;

    // WARNING: no checks in these methods (for efficiency)!!       

    public Tensor(int size, int dimensions)
    {
        this.size = size;
        this.dimensions = dimensions;
        data = init_data();
    }

    public Tensor(Tensor src)
    {
        copy(src);
    }

    public void copy(Tensor src)
    {
        this.size = src.size;
        this.dimensions = src.dimensions;
        this.data = Arrays.copyOf(src.data, src.data.length);
    }

    public void increment(int index)
    {
        /* increment bin given by index (assumes 1-dimensional tensor) */
        data[index]++;
    }

    public void add(Tensor tensor)
    {
        if (is_empty())
        {
            copy(tensor);
            return;
        }

        if (tensor.is_empty())
            return;

        for(int i = 0; i < data.length; i++)
            data[i] += tensor.data[i];
    }

    public void add(int index, int value)
    {
        data[index] += value;
    }

    public void init(int index, double value)
    {
        data[index] = value;
    }

    public void normalize()
    {
        double sum = sum();
        if (sum == 0)
                return;
        for(int i = 0; i < data.length; i++)
            data[i] /= sum;
    }

    public double sum()
    {
        double sum = 0.0;
        for(double v : data) 
            sum += v;   
        return sum;
    }

    public boolean is_empty()
    {
        return (sum() == 0);
    }

    public void extend(int index)
    {
        /* add a dimension to the tensor */
        dimensions++;
        double[] newdata = init_data();
        System.arraycopy(data, 0, newdata, index*data.length, data.length);
        data = newdata;
    }

    public String toString()
    {
        String output = String.format("%d\t%d\n", size, dimensions);
        return output;
    }


    public String toFormattedString()
    {   
        String output = String.format("size=%d\tdimensions=%d\n", size, dimensions);
        List list = Doubles.asList(data);
        output += tensor2FormattedString(list, dimensions);
        return output;
    }

    protected String tensor2FormattedString(List list, int dims) 
    {
        if(dims <= 2)
            /* matrix format */
            return matrix2FormattedString(list, size, dims);
            
        String output = "";
        int subdim = dims-1;
        int subdatasize = (int)Math.pow(size, subdim);      
        for(int i = 0; i < size; i++){
            output += Integer.toString(i) + "\n";
            output += tensor2FormattedString(list.subList(i*subdatasize,i*subdatasize+subdatasize), subdim);
        }
        
        return output;
    }

    protected String matrix2FormattedString(List list, int columns, int dims)
    {
	String output = "";

        if(dims == 1)
            output += Arrays.toString(list.subList(0,columns).toArray()) + "\n";
        else
            for(int r=0; r < columns; r++)
                output += Arrays.toString(list.subList(r*columns,r*columns+columns).toArray()) + "\n";
        return output;
    }

    protected double[] init_data()
    {   
        int datasize = (int)Math.pow(size, dimensions);
        double[] data_ = new double[datasize];
        Arrays.fill(data_, 0);
        return data_;
    }

}
