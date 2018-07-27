package tet;

import mymath.*;
import java.util.*;

public class Histogram{

    TreeMap<Integer,Float> treemap;
    int size;

    public Histogram(){
	treemap = new TreeMap<Integer,Float>();
	size = 0;
    }

    public void put(int key, float val){
	treemap.put(key, val);
	size+=1;
    }

    public void add_histogram(Histogram histo){

	Set<Map.Entry<Integer,Float>> entryset = histo.treemap.entrySet();
	
	for (Iterator<Map.Entry<Integer,Float>> i = entryset.iterator(); i.hasNext();){
	    Map.Entry<Integer,Float> entry = i.next();
	    Float val = treemap.get(entry.getKey());
	    if(val == null)
		val = new Float(0.);
	    treemap.put(entry.getKey(),entry.getValue()+val);
	}	

	if(size < histo.size){
	    size = histo.size;
	}	    
    }

    public void replicate_histogram(int count, int total){
	
	if(size==1){
	    Float val = treemap.get(1);
	    for(int i = 1; i < count; i++){
		treemap.put(i+1,val);
	    }
	    size = total;
	}
	else{
	    int oldsize=size;

	    Vector<Map.Entry<Integer,Float>> entryset = new Vector<Map.Entry<Integer,Float>>(treemap.entrySet());
	    for(int i = 1; i < count; i++){
		for (Iterator<Map.Entry<Integer,Float>> j = entryset.iterator(); j.hasNext();){
		    Map.Entry<Integer,Float> entry = j.next();
		    treemap.put(size+entry.getKey(),entry.getValue());
		}		
		size+=oldsize;
	    }
	    size+=oldsize*(total-count);
	}
    }

    public String toString(){

	String buf = new String();

	Set<Map.Entry<Integer,Float>> entryset = treemap.entrySet();
	for (Iterator<Map.Entry<Integer,Float>> j = entryset.iterator(); j.hasNext();){
	    Map.Entry<Integer,Float> entry = j.next();
	    buf += entry.getKey() + ":" + entry.getValue() + " ";
	}
	return buf;
    }
}