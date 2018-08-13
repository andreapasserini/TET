package tet.rnn;

import tet.Value;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Vector;
import java.util.Random;

public class Dataset {

    Vector<DatasetElement> elements;
    Vector<DatasetElement> train;
    Vector<DatasetElement> validation;
    Vector<DatasetElement> test;

    public Dataset(){
        /* Elements contains Train + validation set */
        elements = new Vector<DatasetElement>();

        /* Contains the train set */
        train = new Vector<DatasetElement>();

        /* Contains the validation set (after having called splitValidation) */
        validation = new Vector<DatasetElement>();

        /* Contains the test set */
        test = new Vector<DatasetElement>();
    }

    public int size(){ return elements.size(); }

    public int trainSize(){ return train.size(); }

    public int validationSize() { return validation.size(); }

    public int testSize() { return test.size(); }

    public void addElement(Value v, float t){
        elements.add(new DatasetElement(v, t));
    }

    public void addTrainElement(Value v, float t){
        train.add(new DatasetElement(v, t));
    }

    public void addValidationElement(Value v, float t){
        validation.add(new DatasetElement(v, t));
    }

    public void addTestElement(Value v, float t){
        test.add(new DatasetElement(v, t));
    }

    public Value[] getValues(){
        Value[] values = new Value[elements.size()];
        for (int i = 0; i < elements.size(); i++)
            values[i] = elements.get(i).getValue();
        return values;
    }

    public Value[] getTrainValues(){
        Value[] values = new Value[train.size()];
        for (int i = 0; i < train.size(); i++)
            values[i] = train.get(i).getValue();
        return values;
    }

    public Value[] getValidationValues(){
        Value[] values = new Value[validation.size()];
        for (int i = 0; i < validation.size(); i++)
            values[i] = validation.get(i).getValue();
        return values;
    }

    public Value[] getTestValues(){
        Value[] values = new Value[test.size()];
        for (int i = 0; i < test.size(); i++)
            values[i] = test.get(i).getValue();
        return values;
    }

    public float[] getTargets(){
        float[] targets = new float[elements.size()];
        for (int i = 0; i < elements.size(); i++)
            targets[i] = elements.get(i).getTarget();
        return targets;
    }


    public float[] getTrainTargets(){
        float[] targets = new float[train.size()];
        for (int i = 0; i < train.size(); i++)
            targets[i] = train.get(i).getTarget();
        return targets;
    }

    public float[] getValidationTargets(){
        float[] targets = new float[validation.size()];
        for (int i = 0; i < validation.size(); i++)
            targets[i] = validation.get(i).getTarget();
        return targets;
    }

    public float[] getTestTargets(){
        float[] targets = new float[test.size()];
        for (int i = 0; i < test.size(); i++)
            targets[i] = test.get(i).getTarget();
        return targets;
    }

    public Value getValueAt(int i){
        return elements.elementAt(i).getValue();
    }

    public Value getTrainValueAt(int i){
        return train.elementAt(i).getValue();
    }

    public Value getValidationValueAt(int i){
        return validation.elementAt(i).getValue();
    }

    public Value getTestValueAt(int i){
        return test.elementAt(i).getValue();
    }

    public float getTargetAt(int i){
        return elements.elementAt(i).getTarget();
    }

    public float getTrainTargetAt(int i){
        return train.elementAt(i).getTarget();
    }


    public float getValidationTargetAt(int i){
        return validation.elementAt(i).getTarget();
    }

    public float getTestTargetAt(int i){
        return test.elementAt(i).getTarget();
    }


    public void setTargetAt(int i, float target){
        elements.elementAt(i).setTarget(target);
    }

    public void setTrainTargetAt(int i, float target){
        train.elementAt(i).setTarget(target);
    }

    public void setValidationTargetAt(int i, float target){
        validation.elementAt(i).setTarget(target);
    }

    public void setTestTargetAt(int i, float target){
        test.elementAt(i).setTarget(target);
    }

    public void normalize() {
        float[] targets = new float[elements.size()];
        float[] testTargets = new float[test.size()];
        float max = 0;
        for (int i = 0; i < elements.size(); i++){
            targets[i] = elements.get(i).getTarget();
            if (targets[i] > max)
                max = targets[i];
        }
        for (int i = 0; i < test.size(); i++){
            testTargets[i] = test.get(i).getTarget();
            if (testTargets[i] > max)
                max = testTargets[i];
        }
        for (int i = 0; i < elements.size(); i++){
            setTargetAt(i, targets[i]/max);
        }

        for (int i = 0; i < test.size(); i++){
            setTestTargetAt(i, testTargets[i]/max);
        }
    }

    /* Select randomly p percentage of examples as validation set */
    public void splitValidation(int p){

        int valsize = (p * elements.size())/100;
        train = new Vector<DatasetElement>();
        for (int i = 0; i < elements.size(); i++)
            train.add(elements.get(i));

        validation = new Vector<DatasetElement>(valsize);

        Random rnd = new Random();
        rnd.setSeed(1527);
        for (int i = 0; i < valsize; i++){
            int index = rnd.nextInt(train.size());
            DatasetElement el = train.get(index);
            validation.add(el);
            train.remove(index);
        }
    }

    public float getMaxTarget(){
        float max = Float.MIN_VALUE;
        for (int i = 0; i < elements.size(); i++){
            float v = elements.get(i).getTarget();
            if (v > max)
                max = v;
        }
        for (int i = 0; i < test.size(); i++){
            float v = test.get(i).getTarget();
            if (v > max)
                max = v;
        }
        return max;
    }

    /* flip label value from 0 to -1 to deal with the cross entropy */
    public void adjustTargets(){
        for (int i = 0; i < elements.size(); i++){
            if (elements.get(i).getTarget() == 0) {
                elements.get(i).setTarget(-1);
            }
        }
        for (int i = 0; i < test.size(); i++){
            if (test.get(i).getTarget() == 0) {
                test.get(i).setTarget(-1);
            }
        }
    }

    public void printSetSize(){
        System.out.println("Elements size: " + elements.size() + "\tTrain size: " + train.size() + "\tValidation size: " + validation.size() + "\tTest size: " + test.size());
    }

    public String sizeToString(){
        return "Elements size: " + elements.size() + "\tTrain size: " + train.size() + "\tValidation size: " + validation.size() + "\tTest size: " + test.size();
    }

}
