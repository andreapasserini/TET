#include "Multiset.h"
#include "transportSimplex.h"

Multiset::Multiset()
{
}

/* A Multiset String is something like:
   [(T,[T:4],[T:3]):2,(T,[],[T:3]):3]
*/

Multiset::~Multiset()
{
}

Multiset::Multiset(const Multiset& m)
{
	this->value_storage = m.value_storage;
	this->multiset_storage = m.multiset_storage;
	this->value_with_count_storage = m.value_with_count_storage;
	this->elements.clear();
	for (unsigned int k=0; k<m.elements.size(); k++)
		this->elements.push_back(m.elements.at(k));
}

Multiset& Multiset::operator=(const Multiset& rhs)
{
	this->value_storage = rhs.value_storage;
	this->multiset_storage = rhs.multiset_storage;
	this->value_with_count_storage = rhs.value_with_count_storage;
	this->elements.clear();
	for (unsigned int k=0; k<rhs.elements.size(); k++)
		this->elements.push_back(rhs.elements.at(k));

	return *this;
}

bool Multiset::operator==(const Multiset& rhs) const
{
	return (((Multiset)(*this)).toString()==((Multiset)rhs).toString());
}


void Multiset::loadFromString(StringTokenizer* tokenizer, std::vector<Value>* value_storage, std::vector<Multiset>* multiset_storage, std::vector<ValueWithCount>* value_with_count_storage)
{
	this->value_storage = value_storage;
	this->multiset_storage = multiset_storage;
	this->value_with_count_storage = value_with_count_storage;

	std::string str = tokenizer->nextToken();

        if(str.compare("[")!=0) // check parenthesis
	{
            std::cout<<"Malformed Multiset string"<<std::endl;
	    exit(1);
	}
        do{ // value:count recursion
                Value val;
		bool isEmpty = val.loadFromString(tokenizer, value_storage, multiset_storage, value_with_count_storage); // recover value
		long double count = 0;

		//if (value is empty) continue; ???
		if (isEmpty)
		{
			count = 1;
		}
		else
		{	
	                if(tokenizer->nextToken().compare(":")!=0)  // check ':'
			{
                	    std::cout<<"Malformed Multiset string"<<std::endl;
			    exit(1);
			}
                	count = atof(tokenizer->nextToken().c_str()); // recover count
                	this->add(val,count); // add value:count pair
			//std::cout<<"Found value "<<val.toString()<<std::endl;
		}

        }while(tokenizer->nextToken().compare("]")!=0); // end of multiset recursion
}

int Multiset::getValueIndexAt(int i)
{
	return value_with_count_storage->at(elements.at(i)).getValueIndex();
}

long double Multiset::getCountAt(int i)
{
	return value_with_count_storage->at(elements.at(i)).getCount();
}

void Multiset::setCountAt(int i, long double c)
{
	value_with_count_storage->at(elements.at(i)).setCount(c);
}

void Multiset::add(Value v, long double c)
{
        /* Note: it is not checked whether 'v' already exists
         * in multiset, and only count needs to be incremented!
         */

	value_storage->push_back(v);
	ValueWithCount vwc(value_storage);
	vwc.setValueIndex(value_storage->size()-1);
	vwc.setCount(c);
	
	value_with_count_storage->push_back(vwc);
        elements.push_back(value_with_count_storage->size()-1);
}

std::string Multiset::toNumString()
{
	std::string str="";
        for (unsigned int i=0; i<elements.size(); i++)
	{
	    std::stringstream ss_count; ss_count<<elements.at(i)<<" "; str+=ss_count.str();
	}
	return str;
}

std::string Multiset::toString()
{
        if(elements.size() == 0)
            return "[ ]";

        std::string result = "[";
        for (unsigned int i=0; i<elements.size(); i++)
	{
            ValueWithCount wc = value_with_count_storage->at(elements.at(i));
	    //if (wc.getValue()->isEmpty) continue;
	    std::stringstream ss_count; ss_count<<wc.getCount();		
            result = result + value_storage->at(wc.getValueIndex()).toString() + ":" + ss_count.str() + ",";
        }
        return result.substr(0,result.length()-1) + "]";
    }


long double Dist(Value* v1, Value* v2)
{
	long double d = v1->computeWKdistance(v2);
	return d;
};

long double Multiset::computeWKdistance(Multiset* other)
{
	//std::cout << "Computing WK-distance between multisets" << this->toString() << " and " << other->toString() << std::endl;
	// Normalized count vectors for the two multisets
	long double p1[elements.size()];
	long double p2[other->elements.size()];
	long double sum1=0;
	for (unsigned int k=0; k<elements.size(); k++) sum1+=(long double)(value_with_count_storage->at(elements.at(k)).getCount());
	for (unsigned int k=0; k<elements.size(); k++) p1[k]=(long double)(value_with_count_storage->at(elements.at(k)).getCount())/sum1;
	long double sum2=0;
	for (unsigned int k=0; k<other->elements.size(); k++) sum2+=(long double)(value_with_count_storage->at(other->elements.at(k)).getCount());
	for (unsigned int k=0; k<other->elements.size(); k++) p2[k]=(long double)(value_with_count_storage->at(other->elements.at(k)).getCount())/sum2;

	Value values1[elements.size()];
	Value values2[other->elements.size()];
	//std::cout << "Values for Multiset 1" << std::endl;
	for (unsigned int k=0; k<elements.size(); k++)
	{
		values1[k] = value_storage->at(this->getValueIndexAt(k));
		//std::cout << values1[k].toString() << " " << p1[k] << std::endl;
	}
	//std::cout << "Values for Multiset 2" << std::endl;
	for (unsigned int k=0; k<other->elements.size(); k++)
	{
		values2[k] = value_storage->at(other->getValueIndexAt(k));
		//std::cout << values2[k].toString() << " " << p2[k] << std::endl;
	}

	t_simplex::TsSignature<Value> sig1(elements.size(), values1, p1);
	t_simplex::TsSignature<Value> sig2(other->elements.size(), values2, p2);

	//std::cout<<"Size sig1 "<<sig1.n<<std::endl;
	//std::cout<<"Size sig2 "<<sig2.n<<std::endl;

	t_simplex::TsFlow flow[(sig1.n)*(sig2.n)];
	int flowVars = 0;
	long double result = 0;

	if (sig1.n*sig2.n==0 && sig1.n+sig2.n>0)
		result = 1;
	else
		result = transportSimplex(&sig1, &sig2, Dist, flow, &flowVars);

	//std::cout << "Simplex returned cost = " << result << std::endl;

	long double new_result = 0;
	for (int i=0; i<flowVars; i++)
	{
		//std::cout << flow[i].from << " " <<flow[i].to << " " << flow[i].amount << std::endl;
		new_result += flow[i].amount * (values1[flow[i].from].computeWKdistance(&(values2[flow[i].to])));
	}

	return new_result;
}

void Multiset::adjustFalseCounts(std::vector<long double> w, std::vector<long double> y, int level)
{
//	level++;
        for (unsigned int i=0; i<elements.size(); i++)
	{
		value_with_count_storage->at(elements.at(i)).adjustFalseCounts(w,y,level);
	}
}

void Multiset::sumTrueCounts(std::vector<long double>* c, std::vector<int>* num, int level)
{
/*	level++;
	if (level >= (int)(c->size()))
	{
		c->push_back(0);
		num->push_back(0);
	}*/
	for (unsigned int i=0; i<elements.size(); i++)
	{
		value_with_count_storage->at(elements.at(i)).sumTrueCounts(c,num,level);
	}
}

void Multiset::sumFalseCounts(std::vector<long double>* c, std::vector<int>* num, int level)
{
/*	level++;
	if (level >= (int)(c->size()))
	{
		c->push_back(0);
		num->push_back(0);
	}*/
	for (unsigned int i=0; i<elements.size(); i++)
	{
		value_with_count_storage->at(elements.at(i)).sumFalseCounts(c,num,level);
	}
}

void Multiset::maxTrueCounts(std::vector<long double>* c, int level)
{
	level++;
	if (level >= (int)(c->size()))
	{
		c->push_back(0);
	}
	for (unsigned int i=0; i<elements.size(); i++)
	{
		value_with_count_storage->at(elements.at(i)).maxTrueCounts(c,level);
	}
}

void Multiset::maxFalseCounts(std::vector<long double>* c, int level)
{
	level++;
	if (level >= (int)(c->size()))
	{
		c->push_back(0);
	}
	for (unsigned int i=0; i<elements.size(); i++)
	{
		value_with_count_storage->at(elements.at(i)).maxFalseCounts(c,level);
	}
}

std::ostream& operator<<(std::ostream& os, Multiset& m)
{
	os<<m.toString();
	return os;
}
