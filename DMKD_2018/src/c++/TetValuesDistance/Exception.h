#ifndef __EXCEPTION_H
#define __EXCEPTION_H

#include <string>
#include <iostream>
using namespace std;

//------------------------------------------------------------------------------------------------
// Exception container
//------------------------------------------------------------------------------------------------

class Exception
{
 private:
	
  string message;

 public:

  Exception() { message="No message"; }
  Exception(const string &mex) { message=mex; }
  Exception(const char *format, ... );
	
  const char *GetMessage() const { return message.c_str(); }
  void PrintMessage() const { cerr<<message<<'\n'; }

  static void Throw() { throw new Exception(); }
  static void Throw(const string &mex) { throw new Exception(mex); }
  static void Throw(const char *const format,...);
  static void Assert(bool condition) { if(!condition) throw new Exception(); }
  static void Assert(bool condition, const char *const format,...);
  static void Assert(bool condition, const string &mex) { if(!condition) throw new Exception(mex); }
};

#endif
