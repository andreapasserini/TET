package tet.rnn;

public interface Optimizer {
    float[] optimize(float[] parameters, float[] partials);
    void init();

}
