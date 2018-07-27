package tet;

import java.util.*;
import java.io.*;
import mymath.*;
import myio.*;

public class Value2RDK{

    public static void main(String[] args){
	
	if(args.length > 0 && args[0] == "--help"){
	    System.out.println("Usage: Value2RDK");
	    System.out.println("\t[<affine (0,1), default = 1>]");
	    System.out.println("\t[<normalize_inside (0,1), default = 0>]");
	    System.out.println("\t[<count_operator (2=prod,3=min), default = 2>]");
	    System.out.println("\t< <values> > <rdk>");
	    System.exit(1);
	}

	boolean affine = (args.length > 0 && args[0].equals("0")) ? false : true;  
	boolean normalize_inside = (args.length > 1 && args[1].equals("1")) ? true : false;  
	String count_operator = (args.length > 2) ? args[2] : RDKTypes.ElementProd;

	try{
	    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	    	    
	    while(in.ready()){
		String vstring = in.readLine();
		try{
		    Value value = new Value(vstring);
		    System.out.println(value.writeRDK(affine, normalize_inside, count_operator));
		}catch(Exception e) {
		    e.printStackTrace();
		    System.out.println("Parsing value string " + vstring);
		    System.exit(1);
		}
	    }
	    in.close();
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }
}

