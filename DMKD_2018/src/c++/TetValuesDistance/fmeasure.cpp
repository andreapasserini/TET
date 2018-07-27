#include <libgen.h>
#include "Matrix.h"

double Fmeasure(const Matrix<double>& M, double alpha = 1)
{
  int numdata = M.GetNoRows();
  double tp = 0, fp = 0, fn = 0;
  
  for(int i = 0; i < numdata; i++){
    if(M(i,0) == 1){
      if(M(i,1) >= 0)
	tp++;
      else
	fn++;
    }
    else if(M(i,1) >= 0)
      fp++;
  }

  if(tp+fp == 0)
    return 0;

  double precision = tp / (tp + fp);
  double recall = tp / (tp + fn);
  
  return (1+alpha) * precision * recall / (alpha*precision + recall);
} 

int main(int argc, char ** argv){
  
  if(argc>1 && !strcmp(argv[1],"--help")){
    cout << "Usage: " << basename(argv[0]) << " [alpha] << <target prediction>" << endl;
    exit(1);
  }
  double alpha = (argc>1) ? atof(argv[1]) : 1;

  Matrix<double> M(cin);
  
  cout << Fmeasure(M,alpha) << endl; 
}
