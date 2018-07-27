package Tetpackage;

public class Counter
{
    int value;

    /**
     * Construct a counter whose value is zero.
     */
    public Counter()
    {
	value = 0;
    }

    /**
     * Construct a counter with given initial value.
     * @param init is the initial value of the counter
     */

    public Counter(int init)
    {
	value = init;
    }

    /**
     * Returns the value of the counter.
     * @return the value of the counter
     */
    public int getValue()
    {
	return value;
    }

    /**
     * Sets the value of the counter.
     * 
     */         
    public void setValue(int value)
    {
	this.value = value;
    }

    /**
     * Zeros the counter 
     */
    public void clear()
    {
	value = 0;
    }

    /**
     * Increase the value of the counter by one.
     */
    public void increment()
    {
	value++;
    }

    /**
     * Return a string representing the value of this counter.
     * @return a string representation of the value
     */
    
    public String toString()
    {
	return ""+value;
    }
}


