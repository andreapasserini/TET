package tet.learner;

import tet.*;

import java.util.*;
import java.io.*;
import mymath.*;


public class TetKernelMachine{

    String[] evaluatecommand;
    String datafile;
    String scorefile;

    public TetKernelMachine(String evaluatecommand, String datafile, String scorefile)
    {
	this.evaluatecommand = tokenizeString(evaluatecommand);
	this.datafile = datafile;
	this.scorefile = scorefile;
    }
    
    public String[] tokenizeString(String cml)
    {	
	StringTokenizer tok = new StringTokenizer(cml," ",false);	
	String[] tokenized = new String[tok.countTokens()];

	int i = 0;
	while(tok.hasMoreTokens())
	    tokenized[i++] = tok.nextToken();
	
	return tokenized;
    }

    public float evaluationScore(Tet tet, Dataset dataset)
    {
	// TODO better implementation !!
	writeDataset(tet, dataset, datafile);
	try{
	    Runtime.getRuntime().exec(evaluatecommand).waitFor();
	    FileReader in = new FileReader(scorefile);
	    return Float.parseFloat(new BufferedReader(in).readLine());  
	}
	catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	return 0;	
    }

    private void writeDataset(Tet tet, Dataset dataset, String outfile)
    {  
	try{
	    FileWriter out = new FileWriter(outfile);
	    
	    Vector<Example> examples = dataset.getExamples();
	    
	    for(Iterator<Example> i = examples.iterator(); i.hasNext();){
		Example e = i.next();
		Value value = tet.calculateValue(dataset.getRelStructure(), e.getRelObjects(), true);
		out.write(e.getTarget() + " " + value.writeRDK() + "\n");
	    }
	    out.close();	
	}
	catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }
}