package tet.rnn;

import java.util.Vector;

public class MSE implements LossFunction {
    public float calculateError(float[] evaluations, float[] targets){
        float res = 0;
        if (evaluations.length != targets.length)
            System.out.println("Cannot compute batch error: evaluation and target must have the same size.");
        for (int i = 0; i < evaluations.length; i++){
            res += Math.pow((evaluations[i] - targets[i]), 2);
        }
        res /= 2*evaluations.length;
        //System.out.println("EV: " + evaluations[0] + "\t TG: " + targets[0] + "\tERR: " + res);
        return res;
    }

    public float[] getErrorDerivatives(float[] evaluations, float[] targets){
        float[] der = new float[evaluations.length];
        for (int i = 0; i < evaluations.length; i++)
            der[i] = (evaluations[i] - targets[i]) / evaluations.length;
        return der;
    }
}
