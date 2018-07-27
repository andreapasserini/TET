#include "Value.h"

Value::Value()
{
	isEmpty = false;
	topbool = true;
}

Value::~Value()
{
}
	
Value::Value(const Value& v)
{
	this->multisets_index.clear();
	for (unsigned int i=0; i<v.multisets_index.size(); i++)
		this->multisets_index.push_back(v.multisets_index.at(i));
	this->isEmpty = v.isEmpty;
	this->topbool = v.topbool;
	this->value_storage = v.value_storage;
	this->multiset_storage = v.multiset_storage;
	this->value_with_count_storage = v.value_with_count_storage;
}

Value& Value::operator=(const Value& rhs)
{
	this->multisets_index.clear();
	for (unsigned int i=0; i<rhs.multisets_index.size(); i++)
		this->multisets_index.push_back(rhs.multisets_index.at(i));
	this->isEmpty = rhs.isEmpty;
	this->topbool = rhs.topbool;
	this->value_storage = rhs.value_storage;
	this->multiset_storage = rhs.multiset_storage;
	this->value_with_count_storage = rhs.value_with_count_storage;

        return *this;
}

bool Value::operator==(const Value& rhs) const
{
	return (((Value)(*this)).toString()==((Value)rhs).toString());
}

/* A Value String is something like:
(T,[(T,[T:4],[T:3]):2,(T,[],[T:3]):3])
*/

bool Value::loadFromString(StringTokenizer* tokenizer, std::vector<Value>* value_storage, std::vector<Multiset>* multiset_storage, std::vector<ValueWithCount>* value_with_count_storage)
{
	this->value_storage = value_storage;
	this->multiset_storage = multiset_storage;
	this->value_with_count_storage = value_with_count_storage;

        std::string buf = tokenizer->nextToken();

        if(buf.compare(" ")==0)
	{
        	//std::cout << "Empty Value !" << std::endl;
		isEmpty = true;
		return true;
	}

        if(buf.compare("(")!=0){ // end of recursion
            setTopValue(buf); // set top value 
            return false;
        }

        buf = tokenizer->nextToken();
        setTopValue(buf); // set top value 

	buf = tokenizer->nextToken();
        if(buf.compare(",")!=0) // check comma
	{
            std::cout << "Malformed Value string" << std::endl;
	    exit(1);
	}

        do{ // multiset recursion
	    Multiset m;
	    m.loadFromString(tokenizer, value_storage, multiset_storage, value_with_count_storage);
	    //std::cout<<"Found multiset "<<m.toString()<<std::endl;
	    multiset_storage->push_back(m);
            multisets_index.push_back(multiset_storage->size()-1);
        }while(tokenizer->nextToken().compare(")")!=0); // end of multiset recursion        

	return false;
}


void Value::setTopValue(std::string valstring)
{
	if(valstring.compare("T")==0)
	{
            topbool = true;
            return;
	}	
        else if(valstring.compare("F")==0)
	{
            topbool = false;
            return;
        }
        
	//std::cout << "Only true/false value implemented for topbool, found: " << valstring << std::endl;
	exit(1);
}

std::string Value::toNumString()
{
	std::string str="";
        for (unsigned int i=0; i<multisets_index.size(); i++)
	{
	    std::stringstream ss_count; ss_count<<multisets_index.at(i)<<" "; str+=ss_count.str();
	}
	return str;
}

std::string Value::toString()
{
	if (multisets_index.size()==0) return topValueString();

        std::string result = "(";
        result = result + topValueString();
        for (unsigned int i=0; i<multisets_index.size(); i++)
            result = result + "," + multiset_storage->at(multisets_index.at(i)).toString();
        result = result + ")";
        return result;
}


void Value::appendValue(Value* v)
{
        if(v->getTopbool() != topbool)
	{
        	std::cout << "Value can be appended only if topbool matches" << std::endl;
		exit(1);
	}

	for (unsigned int k=0; k<v->multisets_index.size(); k++)
	        multisets_index.push_back(v->multisets_index.at(k));
}

long double Value::computeWKdistance(Value* other)
{
	// This is for managing "[ ]" 
	// We decided with Manfred that a distance between x="[ ]" and y!="[ ]" should be 1
	if (this->isEmpty && !(other->isEmpty)) return 1;
	if (other->isEmpty && !(this->isEmpty)) return 1;

	if (!this->getTopbool() && other->getTopbool()) return 1;
	if (this->getTopbool() && !other->getTopbool()) return 1;
	if (!this->getTopbool() && !other->getTopbool()) return 0;

	if (this->isLeaf() && other->isLeaf())
	{
		// Leaf values
		//std::cout << "Both leaves: computing 0/1 distance" << std::endl;
		return fabs(this->topValue()-(other->topValue()));
	}
	else
	{
		// If we are here it means that at least one of the two values is not a leaf
		// so if one of the two is a leaf, return 1
		if (this->isLeaf() || other->isLeaf())
		{
			//std::cout << "Leaf vs. not leaf: distance set to 1" << std::endl;
			return 1;
		}

		//std::cout << "Computing WK-distance between values" 
		//	<< this->toString() << " and " << other->toString() << std::endl;

		long double distance=0;
		std::vector<long double> alphas; alphas.resize(multisets_index.size());
		for (unsigned int k=0; k<multisets_index.size(); k++)
		{
			// Set the alphas to a uniform distribution ---> compute plain avg
			alphas.at(k)=1.0/((long double)(multisets_index.size()));
			distance+=alphas.at(k)*multiset_storage->at(multisets_index.at(k)).computeWKdistance(&(multiset_storage->at(other->multisets_index.at(k))));
		}
		//std::cout << "Distance between values = " << distance << std::endl;
		return distance;
	}
}

void Value::adjustFalseCounts(std::vector<long double> w, std::vector<long double> y, int level)
{
	level++;

	for (unsigned int i=0; i<multisets_index.size(); i++)
	{
		multiset_storage->at(multisets_index.at(i)).adjustFalseCounts(w,y,level);
	}
}

void Value::sumTrueCounts(std::vector<long double>* c, std::vector<int>* num, int level)
{
	level++;
	if (level >= (int)(num->size())) num->push_back(multisets_index.size());
	if (level >= (int)(c->size())) c->push_back(0);

	for (unsigned int i=0; i<multisets_index.size(); i++)
	{
		multiset_storage->at(multisets_index.at(i)).sumTrueCounts(c,num,level);
	}
}

void Value::sumFalseCounts(std::vector<long double>* c, std::vector<int>* num, int level)
{
	level++;
	if (level >= (int)(num->size())) num->push_back(multisets_index.size());
	if (level >= (int)(c->size())) c->push_back(0);

	for (unsigned int i=0; i<multisets_index.size(); i++)
	{
		multiset_storage->at(multisets_index.at(i)).sumFalseCounts(c,num,level);
	}
}

void Value::maxTrueCounts(std::vector<long double>* c, int level)
{
/*
	level++;
	if (level >= (int)(c->size()))
	{
		c->push_back(0);
	}
*/
	for (unsigned int i=0; i<multisets_index.size(); i++)
	{
		multiset_storage->at(multisets_index.at(i)).maxTrueCounts(c,level);
	}
}

void Value::maxFalseCounts(std::vector<long double>* c, int level)
{
/*
	level++;
	if (level >= (int)(c->size()))
	{
		c->push_back(0);
	}
*/
	for (unsigned int i=0; i<multisets_index.size(); i++)
	{
		multiset_storage->at(multisets_index.at(i)).maxFalseCounts(c,level);
	}
}

std::ostream& operator<<(std::ostream& os, Value& v)
{
	os<<v.toString();
	return os;
}

