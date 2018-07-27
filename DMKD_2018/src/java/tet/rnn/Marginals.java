package tet.rnn;

import java.util.*;
import java.sql.*;
import java.io.*;
import tet.*;
import com.google.common.primitives.Doubles;

public class Marginals{
    
    public int size;
    public int dimensions;
    public double[] data;
    public double[] cumulative;

    // WARNING: no checks in these methods (for efficiency)!!       

    public Marginals(int size, int dimensions)
    {
        this.size = size;
        this.dimensions = dimensions;
        data = init_data();
        cumulative = init_data();
    }

    public Marginals(Marginals src)
    {
        this.size = src.size;
        this.dimensions = src.dimensions;
        this.data = Arrays.copyOf(src.data, src.data.length);
        this.cumulative = Arrays.copyOf(src.cumulative, src.cumulative.length);
    }


    public Marginals(Tensor tensor)
    {
        this.size = tensor.size;
        this.dimensions = tensor.dimensions;
        data = init_data();
        cumulative = init_data();

        compute_marginals(tensor);

        compute_cumulatives();
    }

    public void compute_marginals(Tensor tensor)
    {
        int dataSize = (int)(Math.pow(size,dimensions));

        for (int i = 0; i < dataSize; i++)
        {
            double value = tensor.data[i];

            for (int j = 0; j < dimensions; j++)
            {
                int index = (i / (int)(Math.pow(size,j))) % size;
                data[j*size+index] += value;
            }
        }
    }


    public void normalize()
    {
        for (int i = 0; i < dimensions; i++)
            normalize(i);
    }

    public void normalize(int dim)
    {
        double sum = sum(dim);
        for(int i = dim*size; i < dim*size + size; i++)
            data[i] /= sum;
    }
            

    public double sum(int dim)
    {
        double sum = 0.0;
        for(int i = dim*size; i < dim*size + size; i++)
            sum += data[i]; 
        return sum;
    }

    public void compute_cumulatives()
    {
        System.arraycopy(data, 0, cumulative, 0, data.length);
        
        for (int i = 0; i < dimensions; i++)
            compute_cumulatives(i);
    }

    public void compute_cumulatives(int dim)
    {   
        for(int i = dim*size+1; i < dim*size + size; i++)
            cumulative[i] += cumulative[i-1];
    }

    public String toString()
    {
        String output = String.format("%d\t%d\n", size, dimensions);
        output += "MARGINALS\n";
        output += Arrays.toString(data) + "\n";
        output += "CUMULATIVE MARGINALS\n";
        output += Arrays.toString(cumulative) + "\n";
        return output;
    }


    public String toFormattedString()
    {   
        String output = String.format("size=%d\tdimensions=%d\n", size, dimensions);
        output += "MARGINALS\n";
        output += toFormattedString(data); 
        output += "CUMULATIVE MARGINALS\n";
        output += toFormattedString(cumulative);
        return output;
    }

    public String toFormattedString(double[] data)
    {
        String output = "";
        List list = Doubles.asList(data);
        for (int i = 0; i < dimensions; i++)
            output += Arrays.toString(list.subList(i*size,i*size+size).toArray()) + "\n";
        return output;
    }

    protected double[] init_data()
    {   
        int datasize = size * dimensions;
        double[] data_ = new double[datasize];
        Arrays.fill(data_, 0);
        return data_;
    }

}
