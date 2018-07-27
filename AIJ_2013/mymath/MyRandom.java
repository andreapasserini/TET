package mymath;

import java.io.*;
import java.util.*;
import myio.*;

public class MyRandom {

    public static boolean toss(double bias){
	double r = Math.random();
	if (r < bias) return true; else return false;
    }

    public static void randSeqToFile(double bias, String filename, int length){
	try{
	    BufferedWriter bw = FileIO.openOutputFile(filename);
	    for (int i=0; i<length; i++)
		if (toss(bias)) bw.write("h"); else bw.write("t");
	    bw.close();
	}
	catch (IOException e){System.out.println(e);};
	
    }

    public  static int randomInteger(int max){
	/* Generates a random integer, uniformly distributed between 0 and max */
	double rand = Math.random();
	double l = (double)1/(max+1);
	return (int)Math.floor(rand/l);
    }

    public static double[] randomCPTRow(int length){
	double[] result = new double[length];
	double sum = 0;
	for (int i=0;i<length;i++){
	    result[i] = Math.random();
	    sum = sum + result[i];
	}
	for (int i=0;i<length;i++)
	    result[i] = result[i]/sum;
	return result;
    }

    public static int[] randomIntArray(int length, int maxindex){
	/* Creates an integer array of length 'length'. Entries are 
	 * randomly selected integers from 0 to maxindex - 1, without repetitions
	 * If length > maxindex, then return only array of length 
	 * maxindex (containing all integers from 0 to maxindex - 1)
	 */
	int resultlength = Math.min(length,maxindex);
	int[] result = new int[resultlength];
	Vector intUrn = new Vector(); /* Vector of Integer */
	int nextdraw;
	Integer drawnint;
	for (int i=0;i<maxindex;i++)
	    intUrn.add(new Integer(i));
	for (int i=0;i<resultlength;i++){
	    nextdraw = randomInteger(intUrn.size()-1);
	    drawnint = (Integer)intUrn.remove(nextdraw);
	    result[i]=drawnint.intValue();
	}
	return result;
    }

}
