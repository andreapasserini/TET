package Tetpackage;

public enum Operator {
	TIMES{
	    float eval(float x, float y) { return x * y; } 
	    public float neutral() { return (float)1.;} 
	},
	PLUS{
 	    float eval(float x, float y) { return x + y; } 	    
 	    public float neutral() { return (float)0.;} 
// 	},
// 	MAX{
// 	    int p=10;
// 	    float one=(float)1.;
// 	    float eval(float x, float y) { 
// 		return one/(float)Math.pow(Math.pow(one/x,p) 
// 					   + Math.pow(one/y,p),(one/p)); 
// 	    } 
// 	    public float neutral() { return (float)0.;} 

	};
	abstract float eval(float x, float y);
	public abstract float neutral();
}
