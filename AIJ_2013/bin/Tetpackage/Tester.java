package Tetpackage;

import mymath.*;
import myio.*;

public class Tester{

    public static void main(String[] args){

	Value gamma_1_1 = new Value(true,0,2,0);
	Value gamma_1_2 = new Value(false,0,2,1);
    
	Multiset ms_1_1 = new Multiset();
	ms_1_1.add(gamma_1_2,7);
        
	Multiset ms_1_2 = new Multiset();
	ms_1_2.add(gamma_1_1,1);
	ms_1_2.add(gamma_1_2,6);

	Multiset ms_1_3 = new Multiset();
	ms_1_3.add(gamma_1_1,2);
	ms_1_3.add(gamma_1_2,5);

	Multiset ms_1_4 = new Multiset();
	ms_1_4.add(gamma_1_1,3);
	ms_1_4.add(gamma_1_2,4);

	Value gamma_2_1 = new Value(false,1,5,0);
	gamma_2_1.setMultiset(ms_1_1,0);
	System.out.println("gamma_2_1: " + gamma_2_1.toString());

	Value gamma_2_2 = new Value(true,1,5,1);
	gamma_2_2.setMultiset(ms_1_2,0);
	System.out.println("gamma_2_2: " + gamma_2_2.toString());

	Value gamma_2_3 = new Value(true,1,5,2);
	gamma_2_3.setMultiset(ms_1_4,0);
	System.out.println("gamma_2_3: " + gamma_2_3.toString());

	Value gamma_2_4 = new Value(true,1,5,3);
	gamma_2_4.setMultiset(ms_1_3,0);
	System.out.println("gamma_2_4: " + gamma_2_4.toString());

	Multiset ms_2_1 = new Multiset();
	ms_2_1.add(gamma_2_1,4);
	ms_2_1.add(gamma_2_2,3);

	Multiset ms_2_2 = new Multiset();
	ms_2_2.add(gamma_2_1,6);
	ms_2_2.add(gamma_2_3,1);

	Multiset ms_2_3 = new Multiset();
	ms_2_3.add(gamma_2_1,5);
	ms_2_3.add(gamma_2_4,2);

	Value gamma_3_1 = new Value(true,1,5,0);
	gamma_3_1.setMultiset(ms_2_1,0);
	System.out.println("gamma_3_1: " + gamma_3_1.toString());

	Value gamma_3_2 = new Value(true,1,5,1);
	gamma_3_2.setMultiset(ms_2_2,0);
	System.out.println("gamma_3_2: " + gamma_3_2.toString());

	Value gamma_3_3 = new Value(true,1,5,2);
	gamma_3_3.setMultiset(ms_2_3,0);
	System.out.println("gamma_3_3: " + gamma_3_3.toString());
	

	double[] phi1_2_1 = gamma_2_1.phi1();
	double[] phi1_2_2 = gamma_2_2.phi1();
	double[] phi1_2_3 = gamma_2_3.phi1();
	double[] phi1_2_4 = gamma_2_4.phi1();

	double[] phi1_3_1 = gamma_3_1.phi1();
	double[] phi1_3_2 = gamma_3_2.phi1();
	double[] phi1_3_3 = gamma_3_3.phi1();

	
	double[] phi_2_1 = gamma_2_1.phi();
	double[] phi_2_2 = gamma_2_2.phi();
	double[] phi_2_3 = gamma_2_3.phi();
	double[] phi_2_4 = gamma_2_4.phi();

	double[] phi_3_1 = gamma_3_1.phi();
	double[] phi_3_2 = gamma_3_2.phi();
	double[] phi_3_3 = gamma_3_3.phi();

	System.out.println("phi1(gamma_2_1):" + StringOps.arrayToString(phi1_2_1,5));
	System.out.println("phi1(gamma_2_2):" + StringOps.arrayToString(phi1_2_2,5));
	System.out.println("phi1(gamma_2_3):" + StringOps.arrayToString(phi1_2_3,5));
	System.out.println("phi1(gamma_2_4):" + StringOps.arrayToString(phi1_2_4,5));

	System.out.println("phi1(gamma_3_1):" + StringOps.arrayToString(phi1_3_1,5));
	System.out.println("phi1(gamma_3_2):" + StringOps.arrayToString(phi1_3_2,5));
	System.out.println("phi1(gamma_3_3):" + StringOps.arrayToString(phi1_3_3,5));

	System.out.println("<phi1(gamma_2_1),phi1(gamma_2_2)>:" + MyMathOps.innerProduct(phi1_2_1,phi1_2_2));
	System.out.println("<phi1(gamma_2_1),phi1(gamma_2_3)>:" + MyMathOps.innerProduct(phi1_2_1,phi1_2_3));
	System.out.println("<phi1(gamma_2_1),phi1(gamma_2_4)>:" + MyMathOps.innerProduct(phi1_2_1,phi1_2_4));
	System.out.println("<phi1(gamma_2_2),phi1(gamma_2_4)>:" + MyMathOps.innerProduct(phi1_2_2,phi1_2_4));
	

	System.out.println("<phi1(gamma_3_1),phi1(gamma_3_1)>:" + MyMathOps.innerProduct(phi1_3_1,phi1_3_1));
	System.out.println("<phi1(gamma_3_1),phi1(gamma_3_2)>:" + MyMathOps.innerProduct(phi1_3_1,phi1_3_2));
	System.out.println("<phi1(gamma_3_1),phi1(gamma_3_3)>:" + MyMathOps.innerProduct(phi1_3_1,phi1_3_3));
	System.out.println("<phi1(gamma_3_2),phi1(gamma_3_3)>:" + MyMathOps.innerProduct(phi1_3_2,phi1_3_3));
	System.out.println();

	System.out.println("phi(gamma_2_1):" + StringOps.arrayToString(phi_2_1,5));
	System.out.println("phi(gamma_2_2):" + StringOps.arrayToString(phi_2_2,5));
	System.out.println("phi(gamma_2_3):" + StringOps.arrayToString(phi_2_3,5));
	System.out.println("phi(gamma_2_4):" + StringOps.arrayToString(phi_2_4,5));
	

	System.out.println("phi(gamma_3_1):" + StringOps.arrayToString(phi_3_1,5));
	System.out.println("phi(gamma_3_2):" + StringOps.arrayToString(phi_3_2,5));
	System.out.println("phi(gamma_3_3):" + StringOps.arrayToString(phi_3_3,5));


	System.out.println("<phi(gamma_2_1),phi(gamma_2_2)>:" + MyMathOps.innerProduct(phi_2_1,phi_2_2));
	System.out.println("<phi(gamma_2_1),phi(gamma_2_3)>:" + MyMathOps.innerProduct(phi_2_1,phi_2_3));
	System.out.println("<phi(gamma_2_1),phi(gamma_2_4)>:" + MyMathOps.innerProduct(phi_2_1,phi_2_4));
	System.out.println("<phi(gamma_2_2),phi(gamma_2_4)>:" + MyMathOps.innerProduct(phi_2_2,phi_2_4));
	

	System.out.println("<phi(gamma_3_1),phi(gamma_3_1)>:" + MyMathOps.innerProduct(phi_3_1,phi_3_1));
	System.out.println("<phi(gamma_3_1),phi(gamma_3_2)>:" + MyMathOps.innerProduct(phi_3_1,phi_3_2));
	System.out.println("<phi(gamma_3_1),phi(gamma_3_3)>:" + MyMathOps.innerProduct(phi_3_1,phi_3_3));
	System.out.println("<phi(gamma_3_2),phi(gamma_3_3)>:" + MyMathOps.innerProduct(phi_3_2,phi_3_3));
	System.out.println();

	System.out.println("kappa(gamma_2_1,gamma_2_1):" + gamma_2_1.normalkappa(gamma_2_1));
	System.out.println("kappa(gamma_2_1,gamma_2_2):" + gamma_2_1.normalkappa(gamma_2_2));
	System.out.println("kappa(gamma_2_1,gamma_2_3):" + gamma_2_1.normalkappa(gamma_2_3));
	System.out.println("kappa(gamma_2_1,gamma_2_4):" + gamma_2_1.normalkappa(gamma_2_4));
	System.out.println("kappa(gamma_2_2,gamma_2_3):" + gamma_2_2.normalkappa(gamma_2_3));
	System.out.println("kappa(gamma_2_2,gamma_2_4):" + gamma_2_2.normalkappa(gamma_2_4));
	System.out.println("kappa(gamma_2_3,gamma_2_4):" + gamma_2_3.normalkappa(gamma_2_4));
	



        System.out.println("kappa(gamma_3_1,gamma_3_1):" + gamma_3_1.normalkappa(gamma_3_1));
	System.out.println("kappa(gamma_3_1,gamma_3_2):" + gamma_3_1.normalkappa(gamma_3_2));
	System.out.println("kappa(gamma_3_1,gamma_3_3):" + gamma_3_1.normalkappa(gamma_3_3));
	System.out.println("kappa(gamma_3_2,gamma_3_3):" + gamma_3_2.normalkappa(gamma_3_3));
			   

    }
}
