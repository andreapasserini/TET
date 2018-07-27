package tet;

public class StringCounter
{
    String prefix = "tmp";
    Counter counter = new Counter();

    public StringCounter()
    {
	prefix = "tmp";
	counter = new Counter();
    }
    public StringCounter(String prefix)
    {
	this.prefix = prefix;
    }

    public String next()
    {
	String currname = prefix + counter.getValue();
	counter.increment(); 
	return currname;
    }
}

