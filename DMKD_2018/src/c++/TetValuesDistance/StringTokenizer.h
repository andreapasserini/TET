#ifndef STRINGTOKENIZER_H
#define STRINGTOKENIZER_H

#include <iostream>
#include <string>
#include <vector>

class StringTokenizer {

   public:
	std::vector<std::string> tokens;
	int index;

	StringTokenizer();
	StringTokenizer(std::string,std::string,bool);
	std::string nextToken();
};

#endif
