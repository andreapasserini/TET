package tet.rnn;

import java.util.*;

public class RandomWeight{

    Random rnd;
    float range;

    public RandomWeight()
    {
	rnd = new Random();
	rnd.setSeed(1);
	range = (float)0.01;
    }

    public float nextWeight()
    {
	return ((rnd.nextFloat()*2)-1)*range;
    }
}
