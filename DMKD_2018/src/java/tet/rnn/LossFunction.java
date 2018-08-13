package tet.rnn;

import java.util.Vector;

public interface LossFunction {
    /*
    Interface to implement whether a new loss function is created
     */
    float calculateError(float[] evaluations, float[] targets);
    float[] getErrorDerivatives(float[] evaluations, float[] targets);
}
