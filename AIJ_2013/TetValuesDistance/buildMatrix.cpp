/*
computeDistance.cpp

Compute Wasserstein-Kantorovich distance between two TET values

*/

#include <cmath>
#include <cstdlib>
#include <cstdio>
#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include "Multiset.h"
#include "Value.h"
#include "ValueWithCount.h"

using namespace std;

int main (int argc, char* argv[])
{
  std::vector<Multiset> multiset_storage;
  std::vector<Value> value_storage;
  std::vector<ValueWithCount> value_with_count_storage;
  string str_val;

  std::vector<Value> values;

/*
  ifstream v1_inf(argv[1]);
  getline(v1_inf,str_val1);
  v1_inf.close();

  ifstream v2_inf(argv[2]);
  getline(v2_inf,str_val2);
  v2_inf.close();
*/

  ifstream inf(argv[1]);

  getline(inf,str_val);
  while (!inf.eof())
  {
	Value val;
	StringTokenizer* tokenizer = new StringTokenizer(str_val,"[(,:)]",true);
	val.loadFromString(tokenizer, &value_storage, &multiset_storage, &value_with_count_storage);
	delete tokenizer;

	values.push_back(val);

	getline(inf,str_val);
  }
  inf.close();
  
  long double matrix[values.size()*values.size()];

  for (unsigned int i=0; i<values.size(); i++)
  {
	for (unsigned int j=i; j<values.size(); j++)
	{
		if (i==j) matrix[i*values.size()+j]=0;
		Value first = values.at(i);
		Value second = values.at(j);
		long double d_KW = first.computeWKdistance(&second);
		matrix[i*values.size()+j] = d_KW;
		matrix[j*values.size()+i] = d_KW;
	}
  }

  for (unsigned int i=0; i<values.size(); i++)
  {
	for (unsigned int j=0; j<values.size(); j++)
	{
		cout << matrix[i*values.size()+j] << " ";
	}

	cout << endl;
  }

  return 0;	
}

