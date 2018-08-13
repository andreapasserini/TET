package tet.rnn;

import java.util.*;

import tet.*;

public class DatasetElement {

    String id;
    Value value;
    float target;

    public DatasetElement(String id, Value value, float target) {
        this.id = id;
        this.value = value;
        this.target = target;
    }

    public DatasetElement(String id, float target) {
        this.id = id;
        this.target = target;
    }
    
    public DatasetElement(Value v, float target){
        this.value = v;
        this.target = target;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public float getTarget() {
        return target;
    }

    public void setTarget(float target) {
        this.target = target;
    }

    public String toString(){
        return value.toString() + "\t" + String.valueOf(target);
    }
}