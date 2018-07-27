#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>
#include <map>
#include "Value.h"

using namespace std;

void classify(vector<Value>* train, vector<Value>* test, vector<double>* train_labels, vector<double>* test_labels, int k, int kmax, int weighted)
{
	double distance=0;
	double mad[kmax+1];
	double perc[kmax+1];
	double rmse[kmax+1];
	for (int q=0; q<=kmax; q++) mad[q]=0;
	for (int q=0; q<=kmax; q++) perc[q]=0;
	for (int q=0; q<=kmax; q++) rmse[q]=0;

	for (unsigned int i=0; i<test->size(); i++)
	{
		//cout << "Example " << i << ":\t" << endl;

		multimap<double,double> neighbors; // sort labels of neighbors by distance

		Value test_value = test->at(i);
		double test_label = test_labels->at(i);

		for (unsigned int j=0; j<train->size(); j++)
		{
			//cout << "Distance " << i+1 << " " << j+1 << " " ;
			distance = test_value.computeWKdistance(&(train->at(j)));
			//cout << distance << endl;
			neighbors.insert(make_pair(distance,train_labels->at(j)));
		}

		for (int nk=k; nk<=kmax; nk++)
		{
			multimap<double,double>::iterator iter = neighbors.begin();
			int j=0; bool tie=false; double old_distance=0;
			double sum=0, denom=0, predicted=0;

			// Count the labels of the nk nearest neighbors, but keep more than nk in case of tie
			while ((j<nk) || tie)
			{
				if (iter->first == old_distance) tie=true;
				else { tie=false; if (j>=nk) break; }

				cout << "Neighbor-" << j+1 << " " << iter->first << endl;

				double dd = iter->first;
				if (dd==0) dd=1e-5;

				double label = iter->second;
				if (!weighted) sum+=label;
				else { sum+=label*(1.0/(dd)); denom+=(1.0/(dd)); }

				old_distance = iter->first;

				iter++;
				j++;
			}

			if (!weighted) predicted = (double)sum/(double)(j);
			else predicted = sum/denom;

			cout << "True " << test_label << " Pred " << predicted << " (" << sum << "/" << j << " = " << predicted << ")" << endl;

			mad[nk] += fabs(test_label-predicted);
			perc[nk] += fabs(test_label-predicted)/(test_label);
			rmse[nk] += (test_label-predicted)*(test_label-predicted);
		}	
	}

	for (int nk=k; nk<=kmax; nk++)
	{
		cout << "MAD[" << nk << "] = " << (double)mad[nk]/(double)(test->size()) << endl;
		cout << "PERC[" << nk << "] = " << (double)perc[nk]/(double)(test->size()) << endl;
		cout << "RMSE[" << nk << "] = " << sqrt((double)rmse[nk]/(double)(test->size())) << endl;
	}
}

int main (int argc, char* argv[])
{
	if (argc<3)
	{
		cout << "Usage:\n\tknn <training_set> <test_set> <k> [kmax] [weighted]\n" << endl;
		exit(1);
	}

	ifstream inf1(argv[1]); // Training set
	ifstream inf2(argv[2]); // Test set
	int k = atoi(argv[3]); // Number of clusters
	int kmax;
	int weighted;
	if (argc>3) kmax = atoi(argv[4]);
	else kmax = k;
	if (argc>4) weighted = atoi(argv[5]);
	else weighted = 0;

	string line;

  	vector<Multiset> multiset_storage;
	vector<Value> value_storage;
	vector<ValueWithCount> value_with_count_storage;

	vector<Value> train;
	vector<Value> test;
	vector<double> train_labels;
	vector<double> test_labels;

	// Load training set
	getline(inf1,line);
	while(!inf1.eof())
	{
		Value v;
		double label;
		string valuestring;

		stringstream ss(line);
		ss>>label; // First column is the class label
		valuestring=line.substr(line.find_first_of("\t")+1); // Then read the value string

  		StringTokenizer* tokenizer = new StringTokenizer(valuestring,"[(,:)]",true);
		v.loadFromString(tokenizer, &value_storage, &multiset_storage, &value_with_count_storage);
		delete tokenizer;

		train.push_back(v);
		train_labels.push_back(label);
		
		getline(inf1,line);
	}
	inf1.close();

	// Load test set
	getline(inf2,line);
	while(!inf2.eof())
	{
		Value v;
		double label;
		string valuestring;

		stringstream ss(line);
		ss>>label; // First column is the class label
		valuestring=line.substr(line.find_first_of("\t")+1); // Then read the value string

  		StringTokenizer* tokenizer = new StringTokenizer(valuestring,"[(,:)]",true);
		v.loadFromString(tokenizer, &value_storage, &multiset_storage, &value_with_count_storage);
		delete tokenizer;

		test.push_back(v);
		test_labels.push_back(label);
		
		getline(inf2,line);
	}
	inf2.close();

	classify(&train,&test,&train_labels,&test_labels,k,kmax,weighted);

	return 0;
}
