#include "StringTokenizer.h"

StringTokenizer::StringTokenizer()
{
	index=0;
}

StringTokenizer::StringTokenizer(std::string str, std::string delim, bool returnDelims)
{
	index=0;
	std::string tok="";

	for (unsigned int k=0; k<str.length(); k++)
	{
		if (delim.find(str.at(k))==std::string::npos)
		{
			tok+=str.at(k);
		}
		else
		{
			if (tok!="") { tokens.push_back(tok); } // std::cout<<tok<<std::endl; }
			tok=str.at(k);
//			std::cout<<tok<<std::endl;
			tokens.push_back(tok);
			tok="";
		}
	}
}

std::string StringTokenizer::nextToken()
{
	return tokens.at(index++);
}
