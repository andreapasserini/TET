#include "ValueWithCount.h"


ValueWithCount::~ValueWithCount()
{
}

int ValueWithCount::getValueIndex() { return val_index; }

long double ValueWithCount::getCount() { return count; }

ValueWithCount::ValueWithCount(const ValueWithCount& vwc)
{
	value_storage = vwc.value_storage;
	val_index = vwc.val_index;
	count = vwc.count;
}

ValueWithCount& ValueWithCount::operator=(const ValueWithCount& rhs)
{
	value_storage = rhs.value_storage;
	val_index = rhs.val_index;
	count = rhs.count;
	return *this;
}

bool ValueWithCount::operator==(const ValueWithCount& rhs) const
{
	return (value_storage->at(((ValueWithCount)(*this)).getValueIndex()))==(value_storage->at(((ValueWithCount)rhs).getValueIndex())) && ((ValueWithCount)(*this)).getCount()==((ValueWithCount)rhs).getCount();
}

std::string ValueWithCount::toString()
{
	std::stringstream ss; ss<<count;
        std::string result = (value_storage->at(val_index)).toString();// + ":" + ss.str();
        return result;
}

bool ValueWithCount::isValueTrue()
{
	return value_storage->at(val_index).topbool;
}

void ValueWithCount::sumTrueCounts(std::vector<long double>* c, std::vector<int>* num, int level)
{
	if (value_storage->at(val_index).topbool)
	{
#ifdef DEBUG
		std::cout << "=== DEBUG === Adding True count " << count << " at node " << level << std::endl;
#endif
		c->at(level) += count;
//		num->at(level)++;
	}
	
	value_storage->at(val_index).sumTrueCounts(c,num,level);
}

void ValueWithCount::sumFalseCounts(std::vector<long double>* c, std::vector<int>* num, int level)
{
	if (!(value_storage->at(val_index).topbool))
	{
#ifdef DEBUG
		std::cout << "=== DEBUG === Adding False count " << count << " at node " << level << std::endl;
#endif
		c->at(level) += count;
//		num->at(level)++;
	}
	
	value_storage->at(val_index).sumFalseCounts(c,num,level);
}

void ValueWithCount::maxTrueCounts(std::vector<long double>* c, int level)
{
	if (value_storage->at(val_index).topbool)
	{
		//std::cout << "T-Adding " << count << " at level " << level << std::endl;
		if (count > c->at(level)) c->at(level) = count;
	}
	
	value_storage->at(val_index).maxTrueCounts(c,level);
}

void ValueWithCount::maxFalseCounts(std::vector<long double>* c, int level)
{
	if (!(value_storage->at(val_index).topbool))
	{
		//std::cout << "F-Adding " << count << " at level " << level << std::endl;
		if (count > c->at(level)) c->at(level) = count;
	}
	
	value_storage->at(val_index).maxFalseCounts(c,level);
}

void ValueWithCount::adjustFalseCounts(std::vector<long double> w, std::vector<long double> y, int level)
{
	// Update false counts
	if (!value_storage->at(val_index).topbool) { count *= w.at(level); count*=y.at(level); }

	// Recursive call
	value_storage->at(val_index).adjustFalseCounts(w,y,level);
}
