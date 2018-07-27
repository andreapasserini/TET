package tet;

import java.util.*;
import java.io.*;
import mymath.*;
import myio.*;

public class TesterMysql{

    public static void main(String[] args){
	
	if(args.length < 6){
	    System.out.println("Too few arguments, need: <database> <login> <password> <tetstring> <objfile> <outfile>");
	    System.out.println("                         [label] [value computation (0=singlequery 1=td_bu) default=0]");
	    System.exit(1);
	}

	String database = args[0];
	String login = args[1];
	String password = args[2];
	String tetstring = args[3];
	String objfile = args[4];
	String outfile = args[5];
	String label = (args.length > 6) ? args[6] : "0"; 
	Boolean use_TDBU_query = (args.length > 7) ? Boolean.parseBoolean(args[7]) : false;
	
	try{
	    MySQLRelStructure relstruct = new MySQLRelStructure(database, login, password, use_TDBU_query);
	 
	    Tet tet = new Tet(tetstring);

	    System.out.println("Read Tet string: " + tet.toString());
	    System.out.println("Tet Freevars = " + tet.freevars().toString()); 	 



	    FileReader in = new FileReader(objfile);
	    StreamTokenizer st = new StreamTokenizer(in);
	    st.eolIsSignificant(true);
	    st.whitespaceChars(32,47);
	    st.whitespaceChars(58,63);

	    FileWriter out = new FileWriter(outfile, true);

	    while(st.nextToken() != StreamTokenizer.TT_EOF){

		HashMap<String, Object> objmap = new HashMap<String, Object>();

		do{
		    String var = st.sval;
		    st.nextToken();
		    String val = st.sval;
		    if(st.ttype == st.TT_NUMBER)
			val = String.valueOf((int)st.nval);
		    objmap.put(var,new RelObject(val));
		    System.out.println("Read pair: " + var + "=" + val);
		}while(st.nextToken() != StreamTokenizer.TT_EOL);
		
		Value value = tet.calculateValue(relstruct, objmap, true);
		
		System.out.println("Computed tet value:");
		System.out.println(value.toString());
		
		System.out.println("Writing RDK formatted tet value");
		out.write(label + " " + value.writeRDK() + "\n");
		System.out.println("Done");
	    }
	    out.close();
	    relstruct.Close();
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }
}

