#include <stdarg.h>
#include <stdio.h>
#include "Exception.h"

//------------------------------------------------------------------------------------------------
// Exception container
//------------------------------------------------------------------------------------------------

Exception::Exception(const char *format, ... )
{
  va_list arglist;
  
  char mex[1000];
  va_start(arglist,format);
  vsprintf(mex,format,arglist);
  message = mex;
}

void Exception::Throw(const char *format,...)
{
  va_list arglist;
  char mex[1000];
  va_start(arglist,format);
  vsprintf(mex,format,arglist);
  throw new Exception(mex);
}


void Exception::Assert(bool condition, const char *format,...)
{
  va_list arglist;
  if( !condition ) {
    char mex[1000];
    va_start(arglist,format);
    vsprintf(mex,format,arglist);
    throw new Exception(mex);
  }
}
