#ifndef VALUEWITHCOUNT_H
#define VALUEWITHCOUNT_H

#include <sstream>
#include <vector>
#include "Value.h"

class Value;

class ValueWithCount
{
   public:
        int val_index;
        long double count;
        
	std::vector<Value>* value_storage;

	ValueWithCount() {}
	ValueWithCount(std::vector<Value>* v_s) { value_storage = v_s;}

        ValueWithCount(int v, long double c)
        {
            val_index = v;
            count = c;
        }

	ValueWithCount(const ValueWithCount& v);
	ValueWithCount& operator=(const ValueWithCount& rhs);
	bool operator==(const ValueWithCount& rhs) const;

	~ValueWithCount();

	bool isValueTrue();
        int getValueIndex();
        long double getCount();
	void setValueIndex(int v) { val_index = v; }
	void setCount(long double c) { count = c; }
	std::string toString();

	void adjustFalseCounts(std::vector<long double> w, std::vector<long double> y, int level);
	void sumTrueCounts(std::vector<long double>* c, std::vector<int>* num, int level);
	void sumFalseCounts(std::vector<long double>* c, std::vector<int>* num, int level);
	void maxTrueCounts(std::vector<long double>* c, int level);
	void maxFalseCounts(std::vector<long double>* c, int level);
};

#endif
