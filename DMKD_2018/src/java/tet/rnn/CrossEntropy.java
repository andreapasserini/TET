package tet.rnn;

public class CrossEntropy implements LossFunction {
    @Override
    public float calculateError(float[] evaluations, float[] targets) {
        double epsilon  = 1e-18;
        float entropy = 0;
        for (int i = 0; i < evaluations.length; i ++){
            float y = targets[i];
            float y_pred = evaluations[i];
            float p_e = (float) (y * Math.log(y_pred + epsilon) + (1 - y) * Math.log(1 - y_pred + epsilon));
            //System.out.println("Perror: " + p_e);
            entropy += p_e;
        }
        entropy = - entropy/evaluations.length;
        return entropy;
    }

    @Override
    public float[] getErrorDerivatives(float[] evaluations, float[] targets) {
        float[] chain = new float[evaluations.length];
        for (int i = 0; i < evaluations.length; i++){
            float y = targets[i];
            float y_pred = evaluations[i];
            //chain[i] = - ((y/y_pred) + ((1 - y)/ (1 - y_pred))) /evaluations.length;
            chain[i] = (y_pred - y) / ((y_pred * (1 - y_pred)) + (float)1e-7 ) / evaluations.length;
            //System.out.println("Partial " + chain[i]);
        }

        return chain;
    }
}
