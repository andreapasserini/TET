package tet.rnn;

import java.util.Vector;

public class Adam implements Optimizer {

    static int iteration;
    float learningRate;
    float[] m;
    float[] m_e;
    float[] v;
    float[] v_e;
    float b_1;
    float b_2;
    float epsilon;
    boolean hasUpdated;
    Vector<Adam> children;

    public Adam(rnn_Tet tet, float learningRate, float b_1, float b_2, float epsilon){
        this.learningRate = learningRate;
        this.b_1 = b_1;
        this.b_2 = b_2;
        this.epsilon = epsilon;
        int n_parameters = tet.activation.getParametersNumber();
        m = new float[n_parameters];
        v = new float[n_parameters];
        m_e = new float[n_parameters];
        v_e = new float[n_parameters];
        iteration = 0;
        hasUpdated = false;
        children = new Vector<Adam>();
        initializeAdam(tet);
    }

    public void initializeAdam(rnn_Tet tet){
        for (int i = 0; i < tet.getNumChildren(); i++){
            try {
                rnn_Tet child = (rnn_Tet) tet.getChild(i).getSubTree();
                if (child.isLeaf()){
                    return;
                }
                Adam sub = new Adam(child, learningRate, b_1, b_2, epsilon);
                children.add(sub);
            }catch(Exception e){e.printStackTrace();}
        }
    }

    private void updateM(float[] partials){
        //System.out.print("M: [");
        for (int i = 0; i < partials.length; i++){
            m[i] = (b_1 * m[i]) + (1 - b_1) * partials[i];
            //System.out.print(m[i] + ", ");
        }
        //System.out.println("]");
    }

    private void updateV(float[] partials){
        //System.out.print("V: [");
        for (int i = 0; i < partials.length; i++){
            v[i] = (float) ((b_2 * v[i]) + (1 - b_2) * Math.pow(partials[i], 2));
            //System.out.print(v[i] + ", ");
        }
        //System.out.println("]");
    }

    private void estimateM(){
        //System.out.print("Me: [");
        for (int i = 0; i < m.length; i++){
            m_e[i] = (float) (m[i] / (1 - Math.pow(b_1,iteration)));
            //System.out.print(m_e[i] + ", ");
        }
        //System.out.println("]");
    }

    private void estimateV(){
        //System.out.print("Ve: [");
        for (int i = 0; i < v.length; i++){
            v_e[i] = (float) (v[i] / (1 - Math.pow(b_2,iteration)));
            //System.out.print(v_e[i] + ", ");
        }
        //System.out.println("]");
    }

    private void updateIteration(){
        iteration++;
    }

    public float[] optimize(float[] parameters, float[] partials){
        //this.iteration = iteration;
        float[] new_parameters = new float[parameters.length];
        updateM(partials);
        estimateM();
        updateV(partials);
        estimateV();
        for (int i = 0; i < parameters.length; i++){
            new_parameters[i] =(float) (parameters[i] - ((learningRate * m_e[i]) / (Math.sqrt(v_e[i]) + epsilon)));
        }

        return new_parameters;
    }

    public void init(){ iteration += 1; }

    public String toString(){
        String str = "Node ";
        //String str = "\n\tm: " + m.toString() + ", v: " + v.toString() + ", m_e: " + m_e.toString() + ", v_e: " + ", iteration: " + iteration;
        for (int i = 0; i < children.size(); i++){
            str += children.get(i).toString();
        }
        return str;
    }
}
