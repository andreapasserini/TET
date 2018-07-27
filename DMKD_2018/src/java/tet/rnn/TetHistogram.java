package tet.rnn;

import java.util.*;
import java.sql.*;
import java.io.*;
import tet.*;

public class TetHistogram{

    public MultiHistogram histogram;
    public Vector<TetHistogram> children;

    public TetHistogram()
    {

    }

    public TetHistogram(int bins, float min_value, float max_value)
    {
        histogram = new MultiHistogram(bins, min_value, max_value);
        children = new Vector<TetHistogram>();
    }

    public TetHistogram(int bins, float min_value, float max_value, Tet tet)
    {
        this(bins, min_value, max_value);

        try{
            for(int i = 0; i < tet.getNumChildren(); i++)
                children.add(new TetHistogram(bins, min_value, max_value,
                                              tet.getChild(i).getSubTree()));
        } catch(Exception e){e.printStackTrace();}
    }

    public TetHistogram(MultiHistogram histogram, Vector<TetHistogram> children)
    {
        this.histogram = new MultiHistogram(histogram);
        this.children = new Vector<TetHistogram>();

        for(int i = 0; i < children.size(); i++)
            this.children.add(new TetHistogram(children.elementAt(i)));
    }

    public TetHistogram(TetHistogram hist)
    {
        this(hist.histogram,hist.children);
    }

    public TetHistogram(TetHistogram[] histos)
    {
        this(merge(histos));
    }


    public static TetHistogram merge(TetHistogram[] histos)
    {
        System.out.println("Merging histograms:");
        for(int i = 0; i < histos.length; i++)
        {
	    System.out.println("Histo " + i);
            System.out.println(histos[i].toFormattedString());
        }

        TetHistogram hist = new TetHistogram(histos[0]);

        for(int i = 1; i < histos.length; i++)
            hist.add(histos[i]);

        System.out.println("Merged histogram:");
        System.out.println(hist.toFormattedString());

        return hist;
    }

    public void add(TetHistogram hist)
    {
        /* local addition */
        this.histogram.add(hist.histogram);

        /* recursive addition */
        for(int i = 0; i < children.size(); i++)
            children.elementAt(i).add(hist.children.elementAt(i));
    }

    public void fill(float input)
    {
        /* fill local histogram */
        histogram.fill(input);

        /* update lower level histograms */
        for(int i = 0; i < children.size(); i++)
            children.elementAt(i).extend(input);
    }

    public void extend(float input)
    {
        /* update local histogram */
        histogram.extend(input);

        /* update lower level histograms */
        for(int i = 0; i < children.size(); i++)
            children.elementAt(i).extend(input);
    }

    public void addChild(TetHistogram hist)
    {
        children.add(hist);
    }

    public void addChild(TetHistogram[] histos)
    {
        if (histos.length == 0)
            return;

        /* initialize child with first histogram */
        children.add(new TetHistogram(histos));
    }

    public String toString()
    {
        String output = histogram.toString();

        for(int i = 0; i < children.size(); i++)
            output += children.elementAt(i).toString();

        return output;
    }

    public String toFormattedString()
    {
        String output = "NODE HISTOGRAM\n";
        output += histogram.toFormattedString();

        for(int i = 0; i < children.size(); i++){
            output += "CHILD " + i + "\n";
            output += children.elementAt(i).toFormattedString();
        }

        return output;
    }

    public String Serialize()
    {
        return toString();
    }

    public boolean isLeaf()
    {
        return children.size() == 0;
    }

    public int getDepth()
    {
        if (children.size() == 0) return 0;
        else return 1 + children.elementAt(0).getDepth();
    }

    public TetHistogram getChild(int i) throws Exception
    {
        if(i >= children.size() || i < 0)
            throw new Exception("Asked for child " + i + " when TetHistogram has " + children.size() + " children");

        return children.elementAt(i);
    }

    public void normalize()
    {
        histogram.normalize();

        for(int i = 0; i < children.size(); i++)
            children.elementAt(i).normalize();
    }
}
