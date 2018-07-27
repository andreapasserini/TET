#ifndef VALUE_H
#define VALUE_H

#include <cstdlib>
#include <cmath>
#include <iostream>
#include <string>
#include <vector>
#include "Multiset.h"
#include "StringTokenizer.h"

class Multiset;
class ValueWithCount;

class Value {

   public:
	bool topbool;
	bool isEmpty;
	std::vector<int> multisets_index;
	std::vector<Value>* value_storage;
	std::vector<Multiset>* multiset_storage;
	std::vector<ValueWithCount>* value_with_count_storage;

	Value();
	Value(const Value& v);
	~Value();
	Value& operator=(const Value& rhs);
	bool operator==(const Value& rhs) const;

	bool loadFromString(StringTokenizer* tokenizer, std::vector<Value>* value_storage, std::vector<Multiset>* multiset_storage, std::vector<ValueWithCount>* value_with_count_storage);
	void setTopValue(std::string valuestring);

	bool getTopbool(){ return topbool; }
	int getMultisetIndexAt(int i) { return multisets_index.at(i); }
	std::string topValueString() { if(topbool) return "T"; return "F"; }
	std::string topValueID() { if(topbool) return "1"; return "0"; }
	long double topValue() { if(topbool) return (long double)1.; return (long double)0.; }
 	bool isLeaf(){ return (multisets_index.size() == 0); }
	void setEmpty() { isEmpty = true; }

	std::string toString();
	std::string toNumString();

	void appendValue(Value* value);
	void adjustFalseCounts(std::vector<long double> w, std::vector<long double> y, int level);
	void sumTrueCounts(std::vector<long double>* c, std::vector<int>* num, int level);
	void sumFalseCounts(std::vector<long double>* c, std::vector<int>* num, int level);
	void maxTrueCounts(std::vector<long double>* c, int level);
	void maxFalseCounts(std::vector<long double>* c, int level);

	long double computeWKdistance(Value* other);

};

std::ostream& operator<<(std::ostream& os, Value& v);

#endif
