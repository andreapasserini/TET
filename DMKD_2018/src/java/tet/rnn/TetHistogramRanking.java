package tet.rnn;

import java.util.*;
import java.sql.*;
import java.io.*;
import tet.*;
import myio.*;

public class TetHistogramRanking {

    protected TreeMap<Double, TetHistogramRanking> ranking_set = null;
    protected Pair<Integer, TetHistogram> ranking_element = null;

    public TetHistogramRanking()
    {
	ranking_set = new  TreeMap<Double, TetHistogramRanking>();
	ranking_element = null;
    }	
	
    public TetHistogramRanking(Pair<Integer, TetHistogram> element)
    {
	ranking_element = element;
	ranking_set = null;
    }

    public void update(double score, TetHistogramRanking ranking)
    {
	ranking_set.put(score, ranking);
    }

    public String toFormattedString()
    {
	return toFormattedString(0);
    }

    public String toFormattedString(int offset)
    {
	if(ranking_element != null)
	    return StringOps.repeat("\t\t", offset) + ranking_element.first().toString() + "\n";

	String output = "";

	Set<Map.Entry<Double, TetHistogramRanking>> entryset = ranking_set.entrySet();
	for (Iterator<Map.Entry<Double, TetHistogramRanking>> i = entryset.iterator(); i.hasNext();){
	    Map.Entry<Double, TetHistogramRanking> entry = i.next();
	    output += StringOps.repeat("\t\t", offset) + entry.getKey() + "\n" + entry.getValue().toFormattedString(offset+1);
	}

	return output;
    }  
}
