package tet.rnn;

import java.util.Vector;

public class GradientTree {
    /* Gradient tree contains the partial derivatives of the parameters.
       h-index gradient tree is printed like (0.0,0.0,(0.0,0.0,( )))
       Has methods to update the parameters
     */
    float[] partials;
    Vector<GradientTree> children;
    boolean isLeaf;

    public GradientTree(boolean isLeaf) {
        this.isLeaf = isLeaf;
    }

    public GradientTree(int n_parameters, boolean isLeaf) {
        partials = new float[n_parameters];
        this.isLeaf = isLeaf;
        children = new Vector<GradientTree>();
    }

    public void addChild(GradientTree child){
        children.add(child);
    }

    public GradientTree getSubtreeAt(int i){
        return children.elementAt(i);
    }

    public void sumAt(int i, float d){
        partials[i] += d;
    }

    public float getPartialAt(int i){
        return partials[i];
    }

    public float[] getPartials(){
        return partials;
    }

    public String parametersToString(){
        if (isLeaf){
            return "( )";
        }

        String str = "(";
        for (int i = 0; i < partials.length; i++)
            str += partials[i] + ",";

        for (int i = 0; i < children.size(); i++){
            str += children.elementAt(i).parametersToString();
        }
        return str + ")";
    }
}
