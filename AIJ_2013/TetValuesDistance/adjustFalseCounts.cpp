/*
adjusteFalseCounts.cpp

Adjust false counts for a TET value according to some policy

*/

#include <cmath>
#include <cstdlib>
#include <cstdio>
#include <iostream>
#include <fstream>
#include <string>
#include <cstring>
#include <vector>
#include "Multiset.h"
#include "Value.h"
#include "ValueWithCount.h"

using namespace std;

vector<long double> compute_avg_w(vector<Value> values)
{
	vector<long double> wt, wf, w;
	vector<int> numt, numf;

	for (unsigned int k=0; k<values.size(); k++)
	{
		int level=-1;
		Value v = values.at(k);
		v.sumTrueCounts(&wt,&numt,level);
		v.sumFalseCounts(&wf,&numf,level);
	}

#ifdef DEBUG
	for (unsigned int k=0; k<wt.size()-1; k++)
	{
		cout<<"=== DEBUG === True Counts at node "<<k<<" : "<<wt.at(k)<<endl;//" ("<<numt.at(k)<<")"<<endl;
//		if (numt.at(k)!=0) wt.at(k) /= numt.at(k);
	}

	for (unsigned int k=0; k<wf.size()-1; k++)
	{
		cout<<"=== DEBUG === False Counts at node "<<k<<" : "<<wf.at(k)<<endl;//" ("<<numf.at(k)<<")"<<endl;
//		if (numf.at(k)!=0) wf.at(k) /= numf.at(k);
	}
#endif

	for (unsigned int k=0; k<wt.size(); k++)
	{
		if (wf.at(k)!=0)
		{
			w.push_back(wt.at(k)/wf.at(k));
#ifdef DEBUG
			cout<<"=== DEBUG === Ratio at node "<<k<<" : "<<wt.at(k)<<"/"<<wf.at(k)<<" = "<<w.at(k)<<endl;
#endif
		}
	}

	return w;
}

vector<long double> compute_max_w(vector<Value> values)
{
	vector<long double> wt, wf, w;
	vector<int> numt, numf;

	for (unsigned int k=0; k<values.size(); k++)
	{
		int level=-1;
		Value v = values.at(k);
		v.maxTrueCounts(&wt,level);
		v.maxFalseCounts(&wf,level);
	}

	for (unsigned int k=0; k<wt.size(); k++)
	{
		if (wf.at(k)!=0) w.push_back(wt.at(k)/wf.at(k));
	}

	return w;
}

int main (int argc, char* argv[])
{
  std::vector<Multiset> multiset_storage;
  std::vector<Value> value_storage;
  std::vector<ValueWithCount> value_with_count_storage;
  std::vector<Value> values;
  std::vector<int> labels;
  string str_val, str_label, str;

  if (argc<3)
  {
	cout << "Usage:\n\tadjustFalseCounts <values> <reweighting_option>\n" << endl;
	cout << "\t\t<reweighting_option> might be:" << endl;
	cout << "\t\t-w [long double] -- false_counts_i = w * actual_false_counts_i" << endl;
	cout << "\t\t-avg <y_file> -- false_counts_i = y * (true_avg / actual_false_avg) * true_false_i\n\t\t\t\t\t(values for y are contained in y_file, one per line)" << endl;
	//cout << "\t\t-max <y_file> -- false_counts_i = y * (true_max / actual_false_max) * true_false_i\n\t\t\t\t\t(values for y are contained in y_file, one per line)" << endl << endl;
	exit(1);
  }

  long double weight=0;
  vector<long double> y;
  string y_file_name;

  if (strcmp(argv[2],"-w")==0) weight = atof(argv[3]);
  else if (strcmp(argv[2],"-avg")==0) y_file_name = argv[3];
  //else if (strcmp(argv[2],"-max")==0) y_file_name = argv[3];
  else { cout << "Unrecognized option " << argv[2] << endl; exit(0); }

  if (strcmp(argv[2],"-w")!=0)
  {
	ifstream yinf(y_file_name.c_str());
	getline(yinf,str);

	while (!yinf.eof())
  	{
		y.push_back(atof(str.c_str()));
		getline(yinf,str);
	}

	yinf.close();
  }

  ifstream inf(argv[1]);

  getline(inf,str);

  while (!inf.eof())
  {
  	Value value;
	stringstream ss(str); ss>>str_label; str_val = str.substr(str.find_first_of("\t")+1); // Then read the value string
	//str_val = str;
	StringTokenizer* tokenizer = new StringTokenizer(str_val,"[(,:)]",true);
	value.loadFromString(tokenizer, &value_storage, &multiset_storage, &value_with_count_storage);
	labels.push_back(atoi(str_label.c_str()));
	values.push_back(value);
	delete tokenizer;

	getline(inf,str);
  }
  
  inf.close();

  vector<long double> weights_vector;

  if (strcmp(argv[2],"-avg")==0)
	weights_vector = compute_avg_w(values);
  else if (strcmp(argv[2],"-max")==0)
	weights_vector = compute_max_w(values);

  for (unsigned int k=0; k<values.size(); k++)
  {
	Value value = values.at(k);

	int level=-1;
	value.adjustFalseCounts(weights_vector,y,level);

	cout << labels.at(k) << "\t" << value.toString() << endl;
	//cout << value.toString() << endl;
  }

  return 0;	
}

