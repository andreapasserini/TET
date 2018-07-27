package Tetpackage;

import java.util.*;
import java.io.*;
import mymath.*;
import myio.*;

public class MergeValues{

    public static void main(String[] args){
	
	if(args.length < 3){
	    System.out.println("Too few arguments, need: <dstvalues> <srcvalue1> <srcvalue2> [<srcvalue3>..]");
	    System.exit(1);
	}

	String dstvalues = args[0];
	String srcvalues = args[1];

	int values2add = args.length-2;

	try{
	    FileWriter out = new FileWriter(dstvalues, false);

	    BufferedReader in = new BufferedReader(new FileReader(srcvalues));
	    
	    BufferedReader in2add[] = new BufferedReader[values2add];

	    for(int i = 0; i < values2add; i++)
		in2add[i] = new BufferedReader(new FileReader(args[i+2]));
	    
	    while(in.ready()){

		String vstring = in.readLine();
		Value value = new Value(vstring);
		
		for(int i = 0; i < values2add; i++){
		    String v2addstring = in2add[i].readLine();
		    try{
			value.appendValue(new Value(v2addstring));
		    }catch(Exception e) {
			e.printStackTrace();
			System.out.println("Adding value string\n" + v2addstring);
			System.out.println("From srcvalue\n" + args[i+2]);
			System.exit(1);
		    }
		}
		out.write(value.toString() + "\n");
	    }
	    
	    in.close();
	    for(int i = 0; i < values2add; i++)
		in2add[i].close();

	    out.close();
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }
}

