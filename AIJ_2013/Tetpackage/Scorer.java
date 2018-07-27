package Tetpackage;

import java.util.*;
import mymath.*;

//DEBUG
import java.io.*;

public class Scorer{

    public enum ScoreMeasure {OPTACC, AUC, OPTF1};
  
    public static float score(TreeMap<Float, List<Float>> pred2target, 
			      ScoreMeasure measure, 
			      float[] optimal_threshold,
			      boolean balanced)
    {
	switch(measure){
	case AUC:
	    optimal_threshold[0] = 1;
	    return scoreAUC(pred2target);
	case OPTF1:
	    return scoreOPTF1(pred2target, optimal_threshold);
	case OPTACC:
	default:
	    return scoreOPTACC(pred2target, optimal_threshold, balanced);
	}
    }

    public static float scoreAUC(TreeMap<Float, List<Float>> pred2target)
    {

	float auc = 0;
	float y = 0;

  	Set<Map.Entry<Float,List<Float>>> entryset = pred2target.entrySet();

	//count #pos/#neg examples
	float numpos = 0, numneg = 0;
	for (Iterator<Map.Entry<Float,List<Float>>> i = entryset.iterator(); i.hasNext();){
	    List<Float> values = i.next().getValue();
	    for(Iterator<Float> j = values.iterator(); j.hasNext();)
		if(j.next() > 0) numpos++; else numneg++;
	}
	
	for (Iterator<Map.Entry<Float,List<Float>>> i = entryset.iterator(); i.hasNext();){
	    List<Float> values = i.next().getValue();
	    float xdiff = 0, ydiff = 0;
	    for(Iterator<Float> j = values.iterator(); j.hasNext();)
		if(j.next() > 0) 
		    ydiff += 1.0/numpos; 
		else 
		    xdiff += 1.0/numneg;	    
	    auc += xdiff*(2*y+ydiff)/2;
	    y += ydiff;
	}

	// entryset is in ascending order, thus the procedure actually computes 1-auc */
	return 1-auc;
    }

    public static float scoreOPTACC(TreeMap<Float, List<Float>> pred2target, 
				    float[] optimal_threshold,
				    boolean balanced)
    {	
	// DEBUG
	//print(pred2target);

  	Set<Map.Entry<Float,List<Float>>> entryset = pred2target.entrySet();

	//count #pos/#neg examples
	float numpos = 0, numneg = 0;
	for (Iterator<Map.Entry<Float,List<Float>>> i = entryset.iterator(); i.hasNext();){
	    List<Float> values = i.next().getValue();
	    for(Iterator<Float> j = values.iterator(); j.hasNext();)
		if(j.next() > 0) numpos++; else numneg++;
	}
	
	/* initialize optacc to predicting everything as positive */
	float posweight = (balanced) ? numneg / numpos : 1;
	float optacc = numpos*posweight;
	float curracc = optacc;
	optimal_threshold[0] = Float.NEGATIVE_INFINITY; 

	/* iterate over possible prediction values setting threshold to each of them */
	for (Iterator<Map.Entry<Float,List<Float>>> i = entryset.iterator(); i.hasNext();){
	    Map.Entry<Float,List<Float>> entry = i.next();
	    Float curr_threshold = entry.getKey();
	    List<Float> values = entry.getValue();
	    for(Iterator<Float> j = values.iterator(); j.hasNext();)
		if(j.next() > 0) 
		    curracc-=posweight; // false negative
		else 
		    curracc++; // true negative
	    if(curracc > optacc){
		optacc = curracc;
		optimal_threshold[0] = curr_threshold;
	    }
	}

	
	if(balanced)
	    return optacc / (2*numneg);
	
	return optacc / (numpos + numneg);
    }

    public static float scoreOPTF1(TreeMap<Float, List<Float>> pred2target, 
				    float[] optimal_threshold)
    {	
	// DEBUG
	//print(pred2target);

  	Set<Map.Entry<Float,List<Float>>> entryset = pred2target.entrySet();

	//count #pos/#neg examples
	float numpos = 0, numneg = 0;
	for (Iterator<Map.Entry<Float,List<Float>>> i = entryset.iterator(); i.hasNext();){
	    List<Float> values = i.next().getValue();
	    for(Iterator<Float> j = values.iterator(); j.hasNext();)
		if(j.next() > 0) numpos++; else numneg++;
	}
	
	/* initialize optacc to predicting everything as positive */
	float tp = numpos, fp = numneg;
	float tn = 0, fn = 0;
	float bestscore = 0;
	optimal_threshold[0] = Float.NEGATIVE_INFINITY; 

	/* iterate over possible prediction values setting threshold to each of them */
	for (Iterator<Map.Entry<Float,List<Float>>> i = entryset.iterator(); i.hasNext();){
	    Map.Entry<Float,List<Float>> entry = i.next();
	    Float curr_threshold = entry.getKey();
	    List<Float> values = entry.getValue();
	    for(Iterator<Float> j = values.iterator(); j.hasNext();){
		if (j.next()> 0){ 
		    tp-=1;
		    fn+=1;
		}
		else{
		    fp-=1;
		    tn+=1;	    
		}
	    }
	    float currscore=computeF1(tp,fp,tn,fn);
            if(currscore>bestscore){
                bestscore=currscore;		
		optimal_threshold[0] = curr_threshold;
	    }
	}

	return bestscore;
    }

    public static float computeF1(float tp, float fp, float tn, float fn)
    {
	float pre=tp/(tp+fp);
	float rec=tp/(tp+fn);
	return (2*pre*rec)/(pre+rec);
    }

    //DEBUG
    public static void print(TreeMap<Float, List<Float>> pred2target)
    {
	String dstfile = "/tmp/tmp_" + TetUtilities.randomString(3);

	System.out.println("dstfile=" + dstfile);


	try{	
	    FileWriter writer = new FileWriter(dstfile);
	
	    Set<Map.Entry<Float,List<Float>>> entryset = pred2target.entrySet();
	    
	    for (Iterator<Map.Entry<Float,List<Float>>> i = entryset.iterator(); i.hasNext();){
		Map.Entry<Float,List<Float>> entry = i.next();
		for(Iterator<Float> j = entry.getValue().iterator(); j.hasNext();)
		    writer.write(j.next() + " " + entry.getKey() + "\n");
	    }	
	    writer.close();
	} catch (Exception e)
	    {
		e.printStackTrace();
	    }

    }

    public static Pair<Float,Integer> weightedSum(Vector<Pair<Float,Integer>> vec) throws java.lang.ArithmeticException
    {
	float wsum = 0;
	int sum = 0;

	/* compute sum */
	for(int i = 0; i < vec.size(); i++)
	    sum += vec.elementAt(i).second();

	/* check if sum != 0 */
	if(sum == 0)
	    throw new java.lang.ArithmeticException("Division by zero attempted");

	/* compute weighted sum */
	for(int i = 0; i < vec.size(); i++)
	    wsum += vec.elementAt(i).first() * vec.elementAt(i).second() / sum;
	
	return new Pair(wsum,sum);
    }

    public static Pair<Float,Integer> informationGain(Vector<Float> f, Vector<Float> labels, int max) throws java.lang.ArithmeticException
    {
	if (f.size()==0) return new Pair(0,0);

	float prob_class_pos=0, entropy=0, ig=0;

	int[] histP = new int[max+1];
	int[] histN = new int[max+1];
	for(int k=0; k<=max; k++) { histP[k]=0; histN[k]=0; }

        for(int k=0; k<f.size(); k++)
        {
		float label = labels.elementAt(k);
		int idx = f.elementAt(k).intValue();
                if (label==1) histP[idx]++;
                else histN[idx]++;
		//System.out.println("F="+idx+" "+label);
        }

        for(int k=0; k<=max; k++)
        {
		float histTot = histP[k] + histN[k];
		float prob = histTot/f.size();
                if (histTot>0) prob_class_pos = (float)histP[k]/(float)histTot;
		else prob_class_pos = 0;

		if (prob_class_pos == 0 || prob_class_pos == 1) entropy=0;
		else entropy = -prob_class_pos * (float)(Math.log(prob_class_pos)) / (float)(Math.log((float)2)) - (1-prob_class_pos) * (float)(Math.log(1-prob_class_pos)) / (float)(Math.log((float)2));
		//else entropy = -prob_class_pos * (float)(Math.log(prob_class_pos)) / (float)(Math.log((float)2));

		//System.out.println("F="+k+" "+prob_class_pos+" "+entropy+" "+prob+" "+entropy*prob);

                ig+= prob*entropy;
        }

	return new Pair(ig,f.size());
    }

    public static Pair<Float,Integer> computeEntropy(Collection<Float> counts) throws java.lang.ArithmeticException
    {
	return computeEntropy((Float[])counts.toArray(new Float[0]));
    }
    
    public static Pair<Float,Integer> computeEntropy(Float[] counts) throws java.lang.ArithmeticException
    {	
	/* compute overall sum */
	int sum = 0;
	for(int i = 0; i < counts.length; i++)
	    sum += counts[i];

	/* check if sum != 0 */
	if(sum == 0)
	    throw new java.lang.ArithmeticException("Division by zero attempted");
	
	/* compute entropy */
	float H = 0;
	for(int i = 0; i < counts.length; i++){
	    float currprob = counts[i] / sum;
	    if (currprob!=0)
 	       H -= currprob * Math.log(currprob) / Math.log(2);
	}
	
	return new Pair(H,sum);
    }

    public static TreeMap<Float, List<Float>> read(String filename) throws Exception
    {
	TreeMap<Float, List<Float>> pred2target = new TreeMap<Float, List<Float>>();
	
	FileReader freader = new FileReader(filename);
	
	StreamTokenizer st = new StreamTokenizer(freader);
	st.eolIsSignificant(true);
	st.whitespaceChars(32,32); // space
	st.whitespaceChars(9,9); // tab
	st.parseNumbers();

	float target, pred;

	while(st.nextToken() != StreamTokenizer.TT_EOF){
	    /* recover target */
	    if(st.ttype != st.TT_NUMBER)
		throw new Exception("Malformed pred2target file");
	    target  = (float)st.nval;

	    /* recover pred */
	    st.nextToken();
	    if(st.ttype != st.TT_NUMBER)
		throw new Exception("Malformed pred2target file");	   
	    pred = (float)st.nval;

	    /* add current pred-target pair */
	    List<Float> l = pred2target.get(pred);
	    if (l == null)
		pred2target.put(pred, l=new ArrayList<Float>());
	    l.add(target);
	    /* check that EOL reached */
	    if(st.nextToken() != StreamTokenizer.TT_EOL)
		throw new Exception("Malformed pred2target file");
	}

	return pred2target;
    }
    
    public static void main(String[] args)
    {
	if(args.length < 3){
	    System.out.println("Too few arguments, need: <datafile> <measure (0=OPTACC,1=AUC)> <balanced>");
	    System.exit(1);
	}
	
	String datafile = args[0];
	int measure = Integer.parseInt(args[1]);
	boolean balanced = Boolean.parseBoolean(args[2]);

	try{
	    TreeMap<Float, List<Float>> predictions = read(datafile);
	    
	    if(measure == 1)
		System.out.println("AUC=" + scoreAUC(predictions));
	    else{
		float[] optthreshold = new float[1];
		System.out.println("OPTACC=" + scoreOPTACC(predictions, optthreshold, balanced));
		System.out.println("OPTTHRESHOLD=" + optthreshold[0]);
	    }
	}catch (Exception e) {
	    e.printStackTrace();
	}
    }
}

