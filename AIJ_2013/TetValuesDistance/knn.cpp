#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>
#include <map>
#include <stdlib.h>
#include "Value.h"

#define MAX_NN_TO_PRINT 500
#define MIN_DISTANCE 0.001

using namespace std;
typedef map<string, int> String2Id;
typedef vector<Value> Values;
typedef vector< int* > Train;
typedef vector< pair <int, int> > Test;
typedef multimap<double, int* > Neighbors;

void string2intvector(const char* k_string, vector<int>& ks)
{
  stringstream ks_stream(k_string);
  int k;
  while(ks_stream.peek(),ks_stream.good()){
    ks_stream >> k >> ws;
    ks.push_back(k);
  }
}

void openfiles(vector<ofstream*>& files, string& prefix, vector<int> suffices)
{
  for(unsigned int i=0; i < suffices.size(); i++){
    stringstream ss;
    ss << prefix << "." << suffices[i];
    files.push_back(new ofstream(ss.str().c_str()));
  }
}

void closefiles(vector<ofstream*>& files)
{
  for(unsigned int i=0; i < files.size(); i++){
    files[i]->close();
    delete files[i];
  }	
}

void classify(Train& train, Test& test, Values& train_values, Values& test_values, String2Id& train2id, String2Id& test2id, 
	      vector<int>& ks, vector<ofstream*> output_files, int consider_duplicates)
{
  int num_ks = ks.size();
  vector<int> tp(num_ks,0),fp(num_ks,0),tn(num_ks,0),fn(num_ks,0);

  // compute distance matrix 
  double* distance_matrix = new double[train_values.size()*test_values.size()];
  for (unsigned int i = 0; i < train_values.size(); i++)
    for (unsigned int j = 0; j < test_values.size(); j++)
      distance_matrix[i*test_values.size()+j]=test_values[j].computeWKdistance(&(train_values[i]));

  for (unsigned int i=0; i<test.size(); i++)
    {
      //cout << "Example " << i << ":\t" << endl;
      
      Neighbors neighbors; // sort labels of neighbors by distance
      
      int testid =test[i].first;
      int test_label = test[i].second;
      
      for (unsigned int i=0; i<train_values.size(); i++)
	{
	  //cout << "Distance " << i+1 << " " << j+1 << " " ;
	  double distance = distance_matrix[i*test_values.size()+testid];
	  //cout << distance << endl;
	  neighbors.insert(make_pair(distance,train[i]));
	}
      
      for(int ki=0; ki < num_ks; ki++)
	{
	  unsigned int k=ks[ki];
	  ofstream& out = *(output_files[ki]);

	  unsigned int pos=0,neg=0,j=0;
	  double distance=0,normdistance=MIN_DISTANCE,pred_weight=0.,tot_weight=0;
	  for(Neighbors::iterator iter = neighbors.begin(); iter != neighbors.end(); iter++){
		      	   
	    if(pos+neg >= k && iter->first != distance)
	      //if(pos+neg >= k)
	      break;

	    out << "Neighbor-" << j+1 << " " << iter->first << endl;
	  
	    distance=iter->first;
	    normdistance = (distance > 0) ? distance : MIN_DISTANCE;

	    if(consider_duplicates){
	      pos+=(iter->second)[1];
	      pred_weight += (iter->second)[1] / normdistance;
	      neg+=(iter->second)[0];	  
	      pred_weight -= (iter->second)[0] / normdistance;
	      tot_weight += (iter->second)[1] / normdistance + (iter->second)[0] / normdistance;
	    }
	    else{
	      if ((iter->second)[1] > 0){
		pos++;
		pred_weight += 1. / normdistance;
		tot_weight += 1. / normdistance;
	      }
	      if ((iter->second)[0] > 0){
		neg++;
		pred_weight -= 1. / normdistance;
		tot_weight += 1. / normdistance;
	      }
	    }	
	    
	    j++;	   
	  }	
		  		  
	  double prob_pos = (double)pos/(pos+neg);
	  pred_weight /= (pos+neg);
	  //pred_weight /= tot_weight;
	  
	  int pred_label;
	  
	  if (prob_pos>0.5) pred_label=1;
	  else pred_label=-1;
	  
	  if (test_label==1)
	    {	
	      if (pred_label==1) tp[ki]++;
	      else fn[ki]++;			
	    }
	  else
	    {
	      if (pred_label==1) fp[ki]++;
	      else tn[ki]++;			
	    }
	  
	  out << "Pred " << pred_label << " " << test_label << " " << pred_weight
	      << " (" << pos << "/" << pos+neg << "=" << prob_pos << ")" << endl;
	}
    }
  
  delete[] distance_matrix;
  
  for(int ki=0; ki < num_ks; ki++)
    {
      ofstream& out = *(output_files[ki]);
      
      double accuracy = (double)(tp[ki]+tn[ki])/(double)(tp[ki]+tn[ki]+fp[ki]+fn[ki]);
      double precision = (double)(tp[ki])/(double)(tp[ki]+fp[ki]);
      double recall = (double)(tp[ki])/(double)(tp[ki]+fn[ki]);
      out << "Accuracy = " << accuracy << endl;
      out << "Precision = " << precision << endl;
      out << "Recall = " << recall << endl;
    }
}

void classify(Train& train, Test& test, Values& train_values, Values& test_values, String2Id& train2id, String2Id& test2id, const char * k_string, string output_prefix, int consider_duplicates)
{
  vector<int> ks;
  string2intvector(k_string, ks);

  vector<ofstream*> output_files;  
  openfiles(output_files, output_prefix, ks);

  classify(train,test,train_values,test_values,train2id,test2id,ks,output_files,consider_duplicates);

  closefiles(output_files);
}

int main (int argc, char* argv[])
{
	if (argc<5)
	{
		cout << "Usage:\n\tknn <training_set> <test_set> \"<list of ks>\" <output_prefix> <consider duplicates (0=no,1=yes), default=1\n" << endl;
		exit(1);
	}

	ifstream inf1(argv[1]); // Training set
	ifstream inf2(argv[2]); // Test set
        const char * ks = argv[3]; // Numbers of clusters
	string output_prefix = argv[4];
	int consider_duplicates = (argc>5) ? atoi(argv[5]) : 1;

	string line;

  	vector<Multiset> multiset_storage;
	vector<Value> value_storage;
	vector<ValueWithCount> value_with_count_storage;

	
	String2Id train2id,test2id;
	Values train_values,test_values;
	Train train;
	Test test;

	// Load training set
	getline(inf1,line);
	while(!inf1.eof())
	{
		Value v;
		int label;
		string valuestring;

		stringstream ss(line);
		ss>>label; // First column is the class label

		valuestring=line.substr(line.find_first_of("\t")+1); // Then read the value string
		if (train2id.find(valuestring) == train2id.end()){
		  StringTokenizer* tokenizer = new StringTokenizer(valuestring,"[(,:)]",true);
		  v.loadFromString(tokenizer, &value_storage, &multiset_storage, &value_with_count_storage);
		  delete tokenizer;
		  train2id[valuestring] = train_values.size();
		  train_values.push_back(v);
		  train.push_back((int*)calloc(2,sizeof(int)));
		}
		train[train2id[valuestring]][(label+1)/2]++;
		
		getline(inf1,line);
	}
	inf1.close();

	// Load test set
	getline(inf2,line);
	while(!inf2.eof())
	{
		Value v;
		int label;
		string valuestring;

		stringstream ss(line);
		ss>>label; // First column is the class label
		valuestring=line.substr(line.find_first_of("\t")+1); // Then read the value string

		if (test2id.find(valuestring) == test2id.end()){
		  StringTokenizer* tokenizer = new StringTokenizer(valuestring,"[(,:)]",true);
		  v.loadFromString(tokenizer, &value_storage, &multiset_storage, &value_with_count_storage);
		  delete tokenizer;
		  test2id[valuestring] = test_values.size();
		  test_values.push_back(v);
		}
		test.push_back(make_pair(test2id[valuestring],label));
		
		getline(inf2,line);
	}
	inf2.close();

	classify(train,test,train_values,test_values,train2id,test2id,ks,output_prefix,consider_duplicates);

	return 0;
}
