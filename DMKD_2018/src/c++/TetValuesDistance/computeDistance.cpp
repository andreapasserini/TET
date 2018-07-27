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
  string str_val1, str_val2;
/*
  ifstream v1_inf(argv[1]);
  getline(v1_inf,str_val1);
  v1_inf.close();

  ifstream v2_inf(argv[2]);
  getline(v2_inf,str_val2);
  v2_inf.close();
*/

  ifstream inf(argv[1]);
  getline(inf,str_val1);
  getline(inf,str_val2);
  inf.close();
  
  Value first_value; 
  StringTokenizer* tokenizer = new StringTokenizer(str_val1,"[(,:)]",true);
  first_value.loadFromString(tokenizer, &value_storage, &multiset_storage, &value_with_count_storage);
  delete tokenizer;

  Value second_value;
  StringTokenizer* tokenizer2 = new StringTokenizer(str_val2,"[(,:)]",true);
  second_value.loadFromString(tokenizer2, &value_storage, &multiset_storage, &value_with_count_storage);
  delete tokenizer2;

  cout<<first_value.toString()<<endl;
  cout<<second_value.toString()<<endl;

  // Compute Wasserstein-Kantorovich distance
  long double d_KW = first_value.computeWKdistance(&second_value);

  cout << "Wasserstein-Kantorovich distance = " << d_KW << endl;

  return 0;	
}

