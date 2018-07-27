package tet;

import java.util.*;
import java.io.*;
import mymath.*;
import myio.*;

public class MysqlComputeValues{

    public static void main(String[] args){
	
	if(args.length < 7){
	    System.out.println("Too few arguments, need: <database> <login> <password> <tetstring> <objfile> <valuefile> <rdkfile> [label]");
	    System.exit(1);
	}

	String database = args[0];
	String login = args[1];
	String password = args[2];
	String tetstring = args[3];
	String objfile = args[4];
	String valuefile = args[5];
	String rdkfile = args[6];
	String label = (args.length > 7) ? args[7] : "0"; 
	
	try{
	    MySQLRelStructure relstruct = new MySQLRelStructure(database, login, password);
	 
	    Tet tet = new Tet(tetstring);

	    System.out.println("Read Tet string: " + tet.toString());
	    System.out.println("Tet Freevars = " + tet.freevars().toString()); 	 



	    FileReader in = new FileReader(objfile);
	    StreamTokenizer st = new StreamTokenizer(in);
	    st.eolIsSignificant(true);
	    st.whitespaceChars(32,47);
	    st.whitespaceChars(58,63);

	    FileWriter out_value = new FileWriter(valuefile, true);
	    FileWriter out_rdk = new FileWriter(rdkfile, true);

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
		
		System.out.println("Writing tet value:");
		System.out.println(value.toString());
		out_value.write(value.toString() + "\n");
		System.out.println("Done");
		
		System.out.println("Writing RDK formatted tet value");
		out_rdk.write(label + " " + value.writeRDK() + "\n");
		System.out.println("Done");
	    }
	    out_value.close();
	    out_rdk.close();
	    relstruct.Close();
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }
}

