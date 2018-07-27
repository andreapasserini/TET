#ifndef MULTISET_H
#define MULTISET_H

#include <string>
#include <sstream>
#include <ostream>
#include <vector>
#include "StringTokenizer.h"
#include "Value.h"
#include "ValueWithCount.h"

class Value;
class ValueWithCount;

class Multiset
{
   public:
	std::vector<int> elements; // Vector of ValueWithCount
	std::vector<Value>* value_storage;
	std::vector<Multiset>* multiset_storage;
	std::vector<ValueWithCount>* value_with_count_storage;

	Multiset();
	~Multiset();
	void loadFromString(StringTokenizer* tokenizer, std::vector<Value>* value_storage, std::vector<Multiset>* multiset_storage, std::vector<ValueWithCount>* value_with_count_storage);

	Multiset(const Multiset& v);
	Multiset& operator=(const Multiset& rhs);
	bool operator==(const Multiset& rhs) const;

	int getNumberOfElements() { return elements.size(); }
	int getValueIndexAt(int i);
	std::string toString();
	std::string toNumString();
	void add(Value v, long double c);
	long double getCountAt(int i);
	void setCountAt(int i, long double c);

	void adjustFalseCounts(std::vector<long double> w, std::vector<long double> y, int level);
	void sumTrueCounts(std::vector<long double>* c, std::vector<int>* num, int level);
	void sumFalseCounts(std::vector<long double>* c, std::vector<int>* num, int level);
	void maxTrueCounts(std::vector<long double>* c, int level);
	void maxFalseCounts(std::vector<long double>* c, int level);

	long double computeWKdistance(Multiset* other);
};

long double Dist(Value* v1, Value* v2);
std::ostream& operator<<(std::ostream& os, Multiset& m);

#endif
