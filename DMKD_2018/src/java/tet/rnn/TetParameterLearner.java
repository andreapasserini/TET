package tet.rnn;

import tet.Value;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

public class TetParameterLearner {

    rnn_Tet tet;
    Dataset dataset;
    LossFunction loss;
    Optimizer optimizer;
    FileWriter output;

    public TetParameterLearner(rnn_Tet tet, Dataset dataset, LossFunction loss, Optimizer optimizer) throws IOException {
        this.tet = tet;
        this.dataset = dataset;
        this.loss = loss;
        this.optimizer = optimizer;
        this.output = new FileWriter("parameter_learning.out");
    }

    public TetParameterLearner(rnn_Tet tet, Dataset dataset, LossFunction loss, Optimizer optimizer, FileWriter out) throws IOException{
        this.tet = tet;
        this.dataset = dataset;
        this.loss = loss;
        this.optimizer = optimizer;
        this.output = out;
    }

    public void learnParameters(int iterations, int validationSize, String type){
        boolean normalized = false;
        if ("regression".equals(type))
            normalized = true;
        else
            normalized = false;
        try {

            float maxTarget = 0;
            if (normalized) {
                maxTarget = dataset.getMaxTarget();
            }

            dataset.splitValidation(validationSize);

            printInitialInfo();

            /* Initialize variables to keep the best model so far */
            float bestErrorSoFar = Float.MAX_VALUE;
            String bestParametersSoFar = tet.parametersToString();
            int bestIterationSoFar = 0;

            /* Train and validation targets values */
            float[] trainTargets = dataset.getTrainTargets();
            float[] valTargets = dataset.getValidationTargets();

            int i = 0;
            boolean stop = false;
            int stopCount = 0;
            String lastParameters = tet.parametersToString();
            float lastError = Float.MAX_VALUE;

            while (i < iterations && !stop){
                i++;

                /* Train and validation evaluations of the TET-value */
                FloatValue[] trainValues = new FloatValue[dataset.trainSize()];
                FloatValue[] valValues = new FloatValue[dataset.validationSize()];

                /* In here will be stored the evaluation of the root *times*
                   the max value of the dataset (in case of regression).*/
                float[] trainEvaluations = new float[dataset.trainSize()];
                float[] valEvaluations = new float[dataset.validationSize()];
                
                /*#################*/
                /* Calculate the evaluations of the train set */
                for (int j = 0; j < dataset.trainSize(); j++) {
                    Value value = dataset.getTrainValueAt(j);
                    trainValues[j] = tet.compute_d_value(value);
                    if (normalized)
                        trainEvaluations[j] = trainValues[j].getTopValue() * maxTarget;
                    else
                        trainEvaluations[j] = trainValues[j].getTopValue();
                }

                /* Calculate the error of the train set */
                float trainError = loss.calculateError(trainEvaluations, trainTargets);
                

                /* Calculate the evaluations of the validation set */
                for (int j = 0; j < dataset.validationSize(); j++){
                    Value value = dataset.getValidationValueAt(j);
                    valValues[j] = tet.compute_d_value(value);
                    if (normalized)
                        valEvaluations[j] = valValues[j].getTopValue() * maxTarget;
                    else
                        valEvaluations[j] = valValues[j].getTopValue();
                }

                /* Calculate the error of the validation set */
                float valError = loss.calculateError(valEvaluations, valTargets);

                if (normalized){
                    String trainStats = i + "\t" + Math.sqrt(trainError) + "\t" + Math.sqrt(valError) + "\t" + tet.parametersToString() + "\n";
                    System.out.print(trainStats);
                    output.write(trainStats);
                }else{
                    String trainStats = i + "\t" + trainError + "\t" + valError + "\t" + tet.parametersToString() + "\n";
                    System.out.print(trainStats);
                    output.write(trainStats);
                }
                /* Check if it is the best validation error so far, if so keep
                   the parameters of the TET */
                if(bestErrorSoFar > valError){
                    bestErrorSoFar = valError;
                    bestParametersSoFar = tet.parametersToString();
                    bestIterationSoFar = i;
                }

                float[] errorChain = loss.getErrorDerivatives(trainEvaluations, trainTargets);

                /* Calculate the gradient */
                tet.compute_gradient(trainValues, errorChain);

                /* Optimize the parameters */
                tet.optimize(optimizer);

                /* If there is no update of the parameters for n steps, stop */
                if (lastParameters.equals(tet.parametersToString()) || lastError == valError) {
                    stopCount += 1;
                }
                else {
                    stopCount = 0;
                    lastError = valError;
                    lastParameters = tet.parametersToString();
                }

                if (stopCount == 10)
                    stop = true;

            }

            String bestStats;

            if (normalized){
                bestStats = "\nIteration: " + bestIterationSoFar + "\tBest error: " + Math.sqrt(bestErrorSoFar) + "\tBest parameters: " + bestParametersSoFar + "\n";
            }else{
                bestStats = "\nIteration: " + bestIterationSoFar + "\tBest error: " + bestErrorSoFar + "\tBest parameters: " + bestParametersSoFar + "\n";
            }
            output.write(bestStats);
            System.out.println(bestStats);

            /* Set best parameters before testing */
            tet.setParameters(bestParametersSoFar);

            /* Measure test error */
            float testerror = test(normalized);

            /*String testStats = "Test error MSE:\t" + testerror + "\n";
            System.out.println(testStats);
            output.write(testStats);*/

            if (normalized){
                String testStats = "\nTest error RMSE:\t" + Math.sqrt(testerror) + "\n";
                System.out.println(testStats);
                output.write(testStats);
            }else{
                String testStats = "\nTest error Cross Entropy:\t" + testerror + "\n";
                System.out.println(testStats);
                output.write(testStats);
            }

        }catch (Exception e) { e.printStackTrace(); }
    }

    public float test(boolean normalized){
        try {
            float maxTarget = dataset.getMaxTarget();

            float[] testTargets = dataset.getTestTargets();
            FloatValue[] testValues = new FloatValue[dataset.testSize()];
            float[] testEvaluations = new float[dataset.testSize()];

            for (int j = 0; j < dataset.testSize(); j++) {
                Value value = dataset.getTestValueAt(j);
                testValues[j] = tet.compute_d_value(value);
                if (normalized)
                    testEvaluations[j] = testValues[j].getTopValue() * maxTarget;
                else
                    testEvaluations[j] = testValues[j].getTopValue();
            }

            if (!normalized)
                try {
                    printConfusionMatrix(testEvaluations, testTargets);
                }catch(Exception e){ e.printStackTrace();}

            /*float pearson = calculatePearson(testEvaluations, testTargets);
            System.out.println("Test error Pearson: " + pearson);
            output.write("Test error Pearson: " + pearson + "\n");*/

            return loss.calculateError(testEvaluations, testTargets);

        }catch(Exception e){ e.printStackTrace(); }

        return -1;
    }

    private void printInitialInfo(){
        try {
            /* Print dataset size informations */
            System.out.println("Dataset Size");
            dataset.printSetSize();
            output.write("Dataset size\t" + dataset.sizeToString() + "\n");

            /* Print the initial parameters of the TET */
            System.out.println("Initial Parameters: " + tet.parametersToString());
            output.write("Initial Parameters: " + tet.parametersToString() + "\n");

            String header = "Itr\tTError\t\tVError\t\tParameters\n";
            System.out.println(header);
            output.write(header);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void printConfusionMatrix(float[] evaluations, float[] targets) throws IOException {
        int TP = 0;
        int FN = 0;
        int FP = 0;
        int TN = 0;
        for (int i = 0; i < evaluations.length; i++){
            String t = String.valueOf(targets[i]);
            float ev = evaluations[i];
            if (ev >= 0.5)
                ev = 1;
            else if(ev < 0.5)
                ev = 0;
            String e = String.valueOf(ev);
            if ("1.0".equals(t) && "1.0".equals(e))
                TP += 1;
            else if ("1.0".equals(t) && "0.0".equals(e))
                FN += 1;
            else if ("0.0".equals(t) && "1.0".equals(e))
                FP += 1;
            else if ("0.0".equals(t) && "0.0".equals(e))
                TN += 1;
        }

        System.out.println("TP: " + TP + "\tFP: " + FP);
        output.write("TP: " + TP + "\tFP: " + FP + "\n");
        System.out.println("FN: " + FN + "\tTN: " + TN);
        output.write("FN: " + FN + "\tTN: " + TN + "\n");
        System.out.println("\nAccuracy: " + (float)(TP+TN)/(TP+FN+FP+TN));
        output.write("\nAccuracy: " +(float)(TP+TN)/(TP+FN+FP+TN));
    }

    /*private float calculatePearson(float[] evaluations, float[] targets){
        float ev_mean = mean(evaluations);
        float ta_mean = mean(targets);

        float cov = 0;
        float var_ev = 0;
        float var_ta = 0;

        for (int i = 0; i < evaluations.length; i++) {
            cov += (evaluations[i] - ev_mean) * (targets[i] - ta_mean);
            var_ev += Math.pow(evaluations[i] - ev_mean, 2);
            var_ta += Math.pow(targets[i] - ta_mean, 2);
        }

        float sd_ev = (float)Math.sqrt(var_ev);
        float sd_ta = (float)Math.sqrt(var_ta);

        float r = cov / (sd_ev * sd_ta);
        return  r;
    }*/

    private float mean(float[] values){
        float sum = 0;
        for (int i = 0; i < values.length; i++){
            sum += values[i];
        }
        return sum/values.length;
    }

}
