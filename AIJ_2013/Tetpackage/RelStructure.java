package Tetpackage;

import java.util.*;
import mymath.*;

/** Interface between the Tet implementation and a relational data structure, 
    e.g. a SQL database or a prolog knowledge base */

public interface RelStructure{

    public void execute(String command);

    public Vector<HashMap<String, Object>> queryResult(String query);
 
    public String TetValueString(Tet tet, 
				 TreeSet<Variable> boundvars, 
				 HashMap<String, Object> boundvalues, 
				 Boolean trueonly) throws Exception;
    
    public String typeValueQuery(Type type, 
				 TreeSet<Variable> boundvars, 
				 HashMap<String, Object> boundvalues, 
				 TreeSet<String> boundtables, 
				 Boolean returnvalue) throws Exception;

    public String typeValueQuery(Type type, 
				 String boundtable_name, 
				 TreeSet<Variable> boundtable_vars,
				 Boolean returnvalue) throws Exception;
}