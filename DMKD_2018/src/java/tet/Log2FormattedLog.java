package tet;

import java.util.*;
import mymath.*;
import java.io.*;

public class Log2FormattedLog{

    public static void main(String[] args){
        
        if(args.length < 1){
            System.out.println("Too few arguments, need: <logfile>");
            System.exit(1);
        }

        String logfile = args[0];
        
        try{

            FileReader in = new FileReader(logfile);
            BufferedReader rin = new BufferedReader(in);
            
	    while(rin.ready()){

		/* recover next line */
		String line = rin.readLine();

		/* if it does not contain a tet string 
		   (or is the Final tet score which does not print the tet string) 
		   prints it */
		if(!line.contains("tet score:") || line.contains("Final tet score:")){
		    System.out.println(line);
		    continue;
		}

		/* recover beginning of tet string */
		int start = line.indexOf('[');

		/* print score substring */
		System.out.println(line.substring(0,start));

		/* print formatted tet string */
		System.out.println((new Tet(line.substring(start))).toFormattedString());
	    }
	    
            rin.close();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
