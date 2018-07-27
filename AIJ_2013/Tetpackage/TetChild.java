package Tetpackage;

import java.util.*;
import mymath.*;

public class TetChild{

    Tet subtree;
    TreeSet<Variable> edgelabel;
    
    public TetChild(Tet st, TreeSet<Variable> el)
    {	    
	subtree = st;
	edgelabel = el;
    }		

    public TetChild(TetChild tet)
    {	    
	subtree = new Tet(tet.subtree);
	edgelabel = new TreeSet<Variable>(tet.edgelabel);
    }		

    public Tet getSubTree()
    {
	return subtree;
    }
    public TreeSet<Variable> getEdgeLabels()
    {
	return edgelabel;
    }    

    public String toString()
    {
	return "[" + edgeLabelsToString() + "," + subtree.toString() + "]";
    }
    
    public String toFormattedString(String prefix)
    {
	/* attach edge labels */
	String buf = edgeLabelsToString() + "--";

	return buf + subtree.toFormattedString(prefix + new String(TetUtilities.spaceString(buf.length())));
    }

    public String edgeLabelsToString()
    {
	StringBuffer buf = new StringBuffer(1024);
	
	buf.append("(");

	/* append edge labels */
	for(Iterator<Variable> v = edgelabel.iterator(); v.hasNext();)
	    buf.append(v.next().toString() + ",");

	/* replace final comma with parenthesis or append it if no edgelabel */
	if(edgelabel.size() > 0)
	    buf.setCharAt(buf.length()-1,')');
	else
	    buf.append(")");

	/* return string */
	return buf.toString();
    }

}