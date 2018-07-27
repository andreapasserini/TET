#ifndef __SIMPLEMATRIX_H
#define __SIMPLEMATRIX_H
#include <float.h>
#include <stdlib.h>
#include <string.h>
#include <iostream>
#include <fstream>
#include <sstream>
#include <iomanip>
#include <vector>
#include <map>
#include <cmath>
#include "Exception.h"

#define PYTHAG(a,b) ((at=fabs(a)) > (bt=fabs(b)) ? \
(ct=bt/at,at*sqrt(1.0+ct*ct)) : (bt ? (ct=at/bt,bt*sqrt(1.0+ct*ct)): 0.0))

#define SIGNABS(a,b) ((b) >= 0.0 ? fabs(a) : -fabs(a))
#define SIGN(a) ((a) >= 0.0 ? 1 : -1)
#define LOG2(a) (::log(a) * M_LOG2E) 
#define MIDDLE(a,b) a + (double)(b-a)/2.
#define SWAP(a,b) {temp=(a);(a)=(b);(b)=temp;}

using namespace std;

template<class DATATYPE>
class Matrix
{
 public:

  // constructors

  explicit Matrix(int nrows=1, int ncols=1, DATATYPE* mat=NULL);
  Matrix(int nrows, int ncols, const DATATYPE& val);
  Matrix(int nrows, int ncols, DATATYPE** mat);
  Matrix(const Matrix<DATATYPE> &src);
  Matrix(istream& is);
  virtual ~Matrix();

public:

  // initialization
  
  void Init();
  void Fill(const DATATYPE& value);
  void Resize(int nrows, int ncols);
  void Copy(int nrows, int ncols, DATATYPE* mat);
  void Copy(int nrows, int ncols, const vector<DATATYPE>& mat);
  void Copy(int nrows, int ncols, DATATYPE** mat);
  Matrix<DATATYPE>& operator = (const Matrix<DATATYPE> &src);
  bool operator == (const Matrix<DATATYPE> &src) const;
	
  // information

  DATATYPE* &GetBuffer() { return buffer; }
  const DATATYPE* GetConstBuffer() const { return buffer; }
  int GetNoRows() const { return norows; }
  int GetNoCols() const { return nocols; }
  int Size() const { return norows*nocols; }

  // Get/Set
	
  operator DATATYPE() const; // cast to scalar
  DATATYPE& operator () (int r, int c) const;
  DATATYPE& elem(int r, int c) const;
  DATATYPE& elem_nocheck(int r, int c) const { return buffer[r*nocols+c]; }  

  Matrix<DATATYPE> GetSubMatrix(int first_row, int no_rows, int first_col, int no_cols ) const;
  Matrix<DATATYPE> GetRows(int first_row, int no_rows) const { return GetSubMatrix(first_row,no_rows,0,this->nocols); }
  Matrix<DATATYPE> GetRow(int row) const { return GetSubMatrix(row,1,0,this->nocols); }
  Matrix<DATATYPE> GetCols(int first_col, int no_cols) const { return GetSubMatrix(0,this->norows,first_col,no_cols); }
  Matrix<DATATYPE> GetCol(int col) const { return GetSubMatrix(0,this->norows,col,1); }

  void SetSubMatrix(int first_row, int first_col, const Matrix<DATATYPE>& M);
  void SetRows(int first_row, const Matrix<DATATYPE>& M) { SetSubMatrix(first_row,0,M); }
  void SetCols(int first_col, const Matrix<DATATYPE>& M) { return SetSubMatrix(0,first_col,M); }

  Matrix<DATATYPE> GetMinor(int ex_row, int ex_col);
	
  // Arithmetic operation
  
  Matrix<DATATYPE> dotDivide(const Matrix<DATATYPE>& op2) const;
  Matrix<DATATYPE> dotProduct(const Matrix<DATATYPE>& op2) const;
  DATATYPE innerProduct(const Matrix<DATATYPE>& op2) const;
  static Matrix<DATATYPE> log(const Matrix<DATATYPE>& op1);
  static Matrix<DATATYPE> exp(const Matrix<DATATYPE>& op1);
  static Matrix<DATATYPE> pow(const Matrix<DATATYPE>& op1, double op2);

  Matrix<DATATYPE>& operator += (const double& op2);
  Matrix<DATATYPE>& operator -= (const double& op2);
  Matrix<DATATYPE>& operator *= (const double& op2);
  Matrix<DATATYPE>& operator /= (const double& op2);
  Matrix<DATATYPE> operator + (const double& op2) const { Matrix M(*this); return (M+=op2); }
  Matrix<DATATYPE> operator - (const double& op2) const { Matrix M(*this); return (M-=op2); }
  Matrix<DATATYPE> operator * (const double& op2) const { Matrix M(*this); return (M*=op2); }
  Matrix<DATATYPE> operator / (const double& op2) const { Matrix M(*this); return (M/=op2); }

  Matrix<DATATYPE>& operator += (const Matrix<DATATYPE>& op2);
  Matrix<DATATYPE>& operator -= (const Matrix<DATATYPE>& op2);
  Matrix<DATATYPE> operator + (const Matrix<DATATYPE>& op2) const { Matrix M(*this); return (M+=op2); }
  Matrix<DATATYPE> operator - (const Matrix<DATATYPE>& op2) const { Matrix M(*this); return (M-=op2); }
  Matrix<DATATYPE> operator * (const Matrix<DATATYPE>& op2) const;
  Matrix<DATATYPE> operator / (const Matrix<DATATYPE>& op2) const;

  // matrix composition 
  Matrix<DATATYPE> joinByCol(const Matrix<DATATYPE>& op2) const;
  Matrix<DATATYPE> joinByRow(const Matrix<DATATYPE>& op2) const;

  Matrix<DATATYPE> AtA() const;
  Matrix<DATATYPE> AAt() const;

  Matrix<DATATYPE> T() const; // transpose
  Matrix<DATATYPE> I() const; // inverse
  double Determinant() const;
  
  Matrix<DATATYPE> MaxR() const; //maximum values in rows (return a row vector); 
  Matrix<DATATYPE> MaxC() const; //maximum values in columns (return a column vector); 
  Matrix<DATATYPE> MinR() const; //minimum values in rows (return a row vector); 
  Matrix<DATATYPE> MinC() const; //minimum values in columns (return a column vector); 

  DATATYPE Sum() const;
  Matrix<DATATYPE> SumR() const; //sum values in rows (return a column vector); 
  Matrix<DATATYPE> SumC() const; //sum values in columns (return a row vector); 

  Matrix<DATATYPE> Norm() const; //cosine normalization (return normed matrix);
  //cosine normalization for nonsquare matrix (return normed matrix);
  Matrix<DATATYPE> Norm(const Matrix<DATATYPE>& rownorm, const Matrix<DATATYPE>& colnorm) const;
  Matrix<DATATYPE> NormR() const; //normalize matrix by row (return normed matrix);
  Matrix<DATATYPE> NormC() const; //normalize matrix by column (return normed matrix);

  Matrix<DATATYPE> RandPerm() const; //randomly premutate matrix (return permutated matrix)

  Matrix<DATATYPE> Reorder(const vector<int>& order) const; //reorder matrix
  Matrix<DATATYPE> ReorderR(const vector<int>& order) const; //reorder rows 
  Matrix<DATATYPE> ReorderC(const vector<int>& order) const; //reorder columns

  vector<DATATYPE> toVector() const; // return a vector containing matrix elements

  Matrix<DATATYPE> Abs() const; //return matrix with abs of each elements 

  Matrix<DATATYPE> SortR(vector<int>& order) const;
  Matrix<DATATYPE> SortC(vector<int>& order) const;
  Matrix<DATATYPE> Sort(vector<int>& order) const; // descending order
  Matrix<DATATYPE> RSort(vector<int>& order) const; // ascending order
  Matrix<DATATYPE> SortR(Matrix<int>& order) const; // sort matrix by rows
  Matrix<DATATYPE> SortC(Matrix<int>& order) const; // sort matrix by cols

  DATATYPE Max(pair<int,int>& bit) const;
  DATATYPE Min(pair<int,int>& bit) const;

  DATATYPE NormFrobenius() const;
  DATATYPE NormFrobenius(const Matrix<DATATYPE>& op2) const;
  DATATYPE AlignmentFrobenius(const Matrix<DATATYPE>& op2) const;
  DATATYPE AlignmentFrobenius(const Matrix<DATATYPE>& op2, DATATYPE op2_norm) const;
  Matrix<DATATYPE> exp(const DATATYPE& beta) const;

  Matrix<DATATYPE> NormalizeMassCenter() const;
  Matrix<DATATYPE> SubtractMeanR() const;
  Matrix<DATATYPE> SubtractMeanC() const;
  void SelfSubtractMeanR();
  void SelfSubtractMeanC();

  // serialization
  void Read(const char *infile);
  void Read(istream& is=cin);
  void ReadFixedSize(istream& is=cin);
  void Write(ostream& os=cout) const;
  void Write(const char *outfile) const;
  void Readln(istream& is=cin);
  void Writeln(ostream& os=cout) const;
  void WriteVector(ostream& os=cout, bool endline = true, long precision=6) const;

 private:

  int norows;
  int nocols;
  DATATYPE *buffer;
};

template<class DATATYPE>
Matrix<DATATYPE>::Matrix(int nrows, int ncols, DATATYPE* mat)
{
  Exception::Assert(nrows>0 && ncols>0,"construction of empty matrix");
  norows = nrows;
  nocols = ncols;
  buffer = new DATATYPE[norows*nocols];
  if( mat!=0 )
    memcpy(buffer,mat,sizeof(double)*norows*nocols);
  else
    memset(buffer,0,sizeof(DATATYPE)*norows*nocols);
}

template<class DATATYPE>
Matrix<DATATYPE>::Matrix(int nrows, int ncols, const DATATYPE& val)
{
  Exception::Assert(nrows>0 && ncols>0,"construction of empty matrix");
  norows = nrows;
  nocols = ncols;
  buffer = new DATATYPE[norows*nocols];
  Fill(val);
}


template<class DATATYPE>
Matrix<DATATYPE>::Matrix(int nrows, int ncols, DATATYPE** mat)
{
  Exception::Assert(nrows>0 && ncols>0, "construction of empty matrix");
  norows = nrows;
  nocols = ncols;
  buffer = new DATATYPE[norows*nocols];
  if( mat!=0 ) {
    for( int r=0; r<norows; r++ )
      for( int c=0; c<nocols; c++ )
	buffer[r*nocols+c] = mat[r][c];
  }
  else
    memset(buffer,0,sizeof(DATATYPE)*norows*nocols);
}


template<class DATATYPE>
Matrix<DATATYPE>::Matrix(const Matrix<DATATYPE> &src)
{
  buffer = 0;
  *this = src;
}


template<class DATATYPE>
Matrix<DATATYPE>::Matrix(istream& is)
{
  buffer = 0;
  Read(is);
}

template<class DATATYPE>
Matrix<DATATYPE>::~Matrix()
{
  if( buffer!=0 )
    delete []buffer;
}


template<class DATATYPE>
void Matrix<DATATYPE>::Fill(const DATATYPE& value)
{
  for( int r=0; r<norows; r++ )
    for( int c=0; c<nocols; c++ )
      buffer[r*nocols+c] = value;  
}



template<class DATATYPE>
void Matrix<DATATYPE>::Resize(int nrows, int ncols)
{
  Exception::Assert(nrows>0 && ncols>0, "construction of empty matrix");

  if( buffer!=0 )
    delete []buffer;
  norows = nrows;
  nocols = ncols;
  buffer = new DATATYPE[norows*nocols];
  memset(buffer,0,sizeof(DATATYPE)*norows*nocols);
}


template<class DATATYPE>
void Matrix<DATATYPE>::Copy(int nrows, int ncols, DATATYPE* mat)
{
  Exception::Assert(nrows>0 && ncols>0 && mat!=0, "construction of empty matrix");

  Resize(nrows,ncols);
  memcpy(buffer,mat,sizeof(DATATYPE)*norows*nocols);
}

template<class DATATYPE>
void Matrix<DATATYPE>::Copy(int nrows, int ncols, const vector<DATATYPE>& mat)
{
  Exception::Assert(nrows>0 && ncols>0, "construction of empty matrix");

  Resize(nrows,ncols);
  for( int r=0; r<norows; r++ )
    for( int c=0; c<nocols; c++ )
      buffer[r*nocols+c] = mat[r*nocols+c];
}


template<class DATATYPE>
void Matrix<DATATYPE>::Copy(int nrows, int ncols, DATATYPE** mat)
{
  Exception::Assert(nrows>0 && ncols>0 && mat!=0, "construction of empty matrix");

  Resize(nrows,ncols);
  for( int r=0; r<norows; r++ )
    for( int c=0; c<nocols; c++ )
      buffer[r*nocols+c] = mat[r][c];
}

template<class DATATYPE>
Matrix<DATATYPE>& Matrix<DATATYPE>::operator=(const Matrix<DATATYPE> &src)
{
  Resize(src.norows,src.nocols);
  Copy(src.norows,src.nocols,src.buffer);
  return *this;
}


template<class DATATYPE>
bool Matrix<DATATYPE>::operator == (const Matrix<DATATYPE> &op2) const
{
  if(this->norows!=op2.norows || this->nocols!=op2.nocols)
    return false;
  for( int r=0; r<norows; r++ )
    for( int c=0; c<nocols; c++ )
      if(elem(r,c) != op2(r,c))
	return false;
  return true;
}


template<class DATATYPE>
DATATYPE& Matrix<DATATYPE>::operator()(int r, int c) const
{
  Exception::Assert(r>=0&&r<norows,"index out of bound");
  Exception::Assert(c>=0&&c<nocols,"index out of bound");
  return buffer[r*nocols+c];
}

template<class DATATYPE>
DATATYPE& Matrix<DATATYPE>::elem(int r, int c) const
{
  Exception::Assert(r>=0&&r<norows,"index out of bound");
  Exception::Assert(c>=0&&c<nocols,"index out of bound");
  return buffer[r*nocols+c];
}

template<class DATATYPE>
Matrix<DATATYPE>::operator DATATYPE() const 
{ 
  Exception::Assert(norows==1 && nocols==1,"Matrix is not scalar"); 
  return buffer[0]; 
}


template<class DATATYPE>
void Matrix<DATATYPE>::Read(const char * infile) 
{
  ifstream in(infile);
  Exception::Assert(in.good(),"ERROR in opening file %s",infile);
  Read(in);
  in.close();
}

template<class DATATYPE>
void Matrix<DATATYPE>::Read(istream& is)
{
  DATATYPE dummy;
  int ncols = 0;
  int nrows = 0;

  string line;
  vector<DATATYPE> data;
  while( getline(is,line) ) {
    if( line == "")
      continue;
   
    int cols = 0;
    istringstream iss(line);
    while(iss >> dummy) {
      data.push_back(dummy);
      cols++;
    }
    
    if( ncols==0 )
      ncols = cols;
    Exception::Assert(cols==ncols,"inconsistent number of cols in row %d",nrows);	   

    ++nrows;
  }  

  this->Copy(nrows,ncols,data);
}

template<class DATATYPE>
void Matrix<DATATYPE>::Write(const char * outfile) const 
{
  ofstream out(outfile);
  Exception::Assert(out.good(),"ERROR in opening file %s",outfile);
  Write(out);
  out.close();
}

template<class DATATYPE>
void Matrix<DATATYPE>::Write(ostream &os) const 
{
  for( int r=0; r<norows; r++ ) {
    for( int c=0; c<nocols; c++ )
      os<<buffer[r*nocols+c]<<' ';
    os<<endl;
  }
}

template<class DATATYPE>
void Matrix<DATATYPE>::Readln(istream& is)
{
  double dummy;
  int ncols = 0;
  int nrows = 0;

  Exception::Assert(is.good(),"missing rows number");	   
  is >> nrows;
  Exception::Assert(is.good(),"missing cols number");
  is >> ncols;
  this->Resize(nrows,ncols);
  for(int r = 0; r < nrows; r++)
    for(int c = 0; c < ncols; c++){
      Exception::Assert(is.good(),"inconsistent number of cols in row %d",r);	   
      is >> dummy;
      elem(r,c) = dummy;
    }
  is >> ws;
}

template<class DATATYPE>
void Matrix<DATATYPE>::Writeln(ostream &os) const {

  os << norows << " " << nocols << " ";
  for( int r=0; r<norows; r++ )
    for( int c=0; c<nocols; c++ )
      os<<buffer[r*nocols+c]<<' ';
  os<<endl;
}

template<class DATATYPE>
void Matrix<DATATYPE>::WriteVector(ostream &os, bool endline, long precision) const {
  os.precision(precision);
  for( int r=0; r<norows; r++ )
    for( int c=0; c<nocols; c++ )
      os << buffer[r*nocols+c]<<' ';
  if (endline)
    os<<endl;	
}

template<class DATATYPE>
void Matrix<DATATYPE>::ReadFixedSize(istream &is) {
  for( int r=0; r<norows; r++ ) {
    for( int c=0; c<nocols; c++ ) {
      is >> buffer[r*nocols+c]; 
      Exception::Assert(is.good(), "Unexpected EOF while reading matrix");
    }
  }
}

template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::GetSubMatrix(int first_row, int no_rows, int first_col, int no_cols ) const {
  Exception::Assert(no_rows>0 && no_cols>0,"empty matrix");
  Exception::Assert(first_row>=0 && first_row<norows,"empty matrix");  
  Exception::Assert(first_col>=0 && first_col<nocols,"empty matrix");
  Exception::Assert(first_row+no_rows<=norows && first_col+no_cols<=nocols,"index out of bound");
  
  Matrix M(no_rows,no_cols);
  for( int r=0; r<no_rows; r++ )
    for( int c=0; c<no_cols; c++ )
      M(r,c) = (*this)(r+first_row,c+first_col);

  return M;
}

template<class DATATYPE>
void Matrix<DATATYPE>::SetSubMatrix(int first_row, int first_col, const Matrix<DATATYPE>& M) {
  Exception::Assert(M.norows>0 && M.nocols>0,"empty matrix");
  Exception::Assert(first_row>=0 && first_row<norows,"empty matrix");  
  Exception::Assert(first_col>=0 && first_col<nocols,"empty matrix");
  Exception::Assert(first_row+M.norows<=norows && first_col+M.nocols<=nocols,"index out of bound");
  
  for( int r=0; r<M.norows; r++ )
    for( int c=0; c<M.nocols; c++ )
      (*this)(r+first_row,c+first_col) = M(r,c);
}

template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::GetMinor(int ex_row, int ex_col) {
  Exception::Assert(norows>1 && nocols>1,"empty matrix");
  Exception::Assert(ex_row>=0 && ex_row<=norows,"empty matrix");  
  Exception::Assert(ex_col>=0 && ex_col<=nocols,"empty matrix");

  Matrix M(norows-1,nocols-1);
  for( int r=0,r1=0; r<norows; r++ ) {
    if( r==ex_row )
      continue;

    for( int c=0,c1=0; c<nocols; c++ ) {
      if( c==ex_col )
	continue;

      M(r1,c1) = (*this)(r,c);
      c1++;
    }
    r1++;
  }

  return M;
}

// Arithmetic operation

template<class DATATYPE>
Matrix<DATATYPE>& Matrix<DATATYPE>::operator += (const double& op2) {
  for( int r=0; r<norows; r++ )
    for( int c=0; c<nocols; c++ )
      (*this)(r,c) += op2;
  return (*this);
}

template<class DATATYPE>
Matrix<DATATYPE>& Matrix<DATATYPE>::operator -= (const double& op2) {
  for( int r=0; r<norows; r++ )
    for( int c=0; c<nocols; c++ )
      (*this)(r,c) -= op2;
  return (*this);
}

template<class DATATYPE>
Matrix<DATATYPE>& Matrix<DATATYPE>::operator *= (const double& op2) {
  for( int r=0; r<norows; r++ )
    for( int c=0; c<nocols; c++ )
      (*this)(r,c) *= op2;
  return (*this);
}

template<class DATATYPE>
Matrix<DATATYPE>& Matrix<DATATYPE>::operator /= (const double& op2) {
  for( int r=0; r<norows; r++ )
    for( int c=0; c<nocols; c++ )
      (*this)(r,c) /= op2;
  return (*this);
}

template<class DATATYPE>
Matrix<DATATYPE>& Matrix<DATATYPE>::operator += (const Matrix<DATATYPE>& op2) {
  Exception::Assert(this->norows==op2.norows && this->nocols==op2.nocols,"Matrices have different sizes");

  for( int r=0; r<norows; r++ )
    for( int c=0; c<nocols; c++ )
      (*this)(r,c) += op2(r,c);
  return (*this);  
}

template<class DATATYPE>
Matrix<DATATYPE>& Matrix<DATATYPE>::operator -= (const Matrix<DATATYPE>& op2) {
  Exception::Assert(this->norows==op2.norows && this->nocols==op2.nocols,"Matrices have different sizes");

  for( int r=0; r<norows; r++ )
    for( int c=0; c<nocols; c++ )
      (*this)(r,c) -= op2(r,c);
  return (*this);  
}

template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::operator * (const Matrix<DATATYPE>& op2) const {
  Exception::Assert(this->nocols==op2.norows,"Matrices have incompatible sizes");

  Matrix M(this->norows,op2.nocols);
  for( int r=0; r<norows; r++ )
    for( int c=0; c<op2.nocols; c++ )
      for( int k=0; k<nocols; k++)
	M(r,c) += (*this)(r,k) * op2(k,c);
  return M;  
}

template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::operator / (const Matrix<DATATYPE>& op2) const {
  return ((*this)*op2.I());
}

template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::dotDivide(const Matrix<DATATYPE>& op2) const
{
  Exception::Assert(this->norows==op2.norows && this->nocols==op2.nocols,"Matrices have different sizes");

  Matrix M(*this);
  for( int r=0; r<norows; r++ )
    for( int c=0; c<nocols; c++ )
      M(r,c) /= op2(r,c);

  return M;
}


template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::dotProduct(const Matrix<DATATYPE>& op2) const
{
  Exception::Assert(this->norows==op2.norows && this->nocols==op2.nocols,"Matrices have different sizes");

  Matrix M(*this);
  for( int r=0; r<norows; r++ )
    for( int c=0; c<nocols; c++ )
      M(r,c) *= op2(r,c);

  return M;
}


template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::log(const Matrix<DATATYPE>& op2)
{
  Matrix M(op2.norows,op2.nocols);
  for( int r=0; r<op2.norows; r++ )
    for( int c=0; c<op2.nocols; c++ ) {
      if( op2(r,c)!=0. )
	M(r,c) = (DATATYPE)::log(op2(r,c));
      else
	M(r,c) = -DBL_MAX;
    }

  return M;
}

template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::exp(const Matrix<DATATYPE>& op2)
{
  Matrix M(op2.norows,op2.nocols);
  for( int r=0; r<op2.norows; r++ )
    for( int c=0; c<op2.nocols; c++ )
      M(r,c) = ::exp(op2(r,c));

  return M;
}
template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::pow(const Matrix<DATATYPE>& op1, double op2)
{
  Matrix M(op1.norows,op1.nocols);
  for( int r=0; r<op1.norows; r++ )
    for( int c=0; c<op1.nocols; c++ )
      M(r,c) = ::pow(op1(r,c),op2);

  return M;
}

template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::AAt() const {
  Matrix<DATATYPE> M(norows,norows);
  for( int r=0; r<norows; r++ )
    for( int c=0; c<norows; c++ ) 
      for( int k=0; k<nocols; k ++ )
       M(r,c) += (*this)(r,k)*(*this)(c,k);
  return M;
}

template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::AtA() const {
  Matrix<DATATYPE> M(nocols,nocols);
  for( int r=0; r<nocols; r++ )
    for( int c=0; c<nocols; c++ ) 
      for( int k=0; k<norows; k ++ )
	M(r,c) += (*this)(k,r)*(*this)(k,c);
  return M;
}

template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::T() const {
  Matrix<DATATYPE> M(nocols,norows);
  for( int r=0; r<norows; r++ )
    for( int c=0; c<nocols; c++ )
      M(c,r) = (*this)(r,c);
  return M;
}

template<class DATATYPE>
double Matrix<DATATYPE>::Determinant() const {
  Exception::Assert(norows==nocols,"Not a square matrix");
  Exception::Assert(norows<=2,"Determinant operation supported only for N=2");

  if( norows==1 )
    return (*this)(0,0);
  return ((*this)(0,0)*(*this)(1,1)-(*this)(0,1)*(*this)(1,0));
}
	
template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::MaxR() const
{
  Matrix<DATATYPE> maxr(1,nocols);
  for( int r=0; r<norows; r++ ) {
    for( int c=0; c<nocols; c++ ) {
      if( r==0 )
	maxr(0,c) = elem(r,c);
      else 
	maxr(0,c) = (maxr(0,c)>elem(r,c)) ?maxr(0,c) :elem(r,c);
    }
  }
  return maxr;
}

template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::MaxC() const
{
  Matrix<DATATYPE> maxc(norows,1);

  for( int c=0; c<nocols; c++ ) {
    for( int r=0; r<norows; r++ ) {      
      if( c==0 )
	maxc(r,0) = elem(r,c);
      else 
	maxc(r,0) = (maxc(r,0)>elem(r,c)) ?maxc(r,0) :elem(r,c);
    }
  }
  return maxc;
}

template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::MinR() const 
{
  Matrix<DATATYPE> minr(1,nocols);

  for( int r=0; r<norows; r++ ) {
    for( int c=0; c<nocols; c++ ) {
      if( r==0 )
	minr(0,c) = elem(r,c);
      else 
	minr(0,c) = (minr(0,c)<elem(r,c)) ?minr(0,c) :elem(r,c);
    }
  }
  return minr;
} 

template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::MinC() const
{  
  Matrix<DATATYPE> minc(norows,1);

  for( int c=0; c<nocols; c++ ) {
    for( int r=0; r<norows; r++ ) {      
      if( c==0 )
	minc(r,0) = elem(r,c);
      else 
	minc(r,0) = (minc(r,0)<elem(r,c)) ?minc(r,0) :elem(r,c);
    }
  }
  return minc;
} 

template<class DATATYPE>
DATATYPE Matrix<DATATYPE>::innerProduct(const Matrix<DATATYPE>& op2) const
{
  DATATYPE prod = 0;
  for( int r=0; r<norows; r++ )
    for( int c=0; c<nocols; c++ )
      prod += elem(r,c)*op2(r,c);
  return prod;
}

template<class DATATYPE>
DATATYPE Matrix<DATATYPE>::Sum() const
{
  DATATYPE sum = 0;
  for( int r=0; r<norows; r++ )
    for( int c=0; c<nocols; c++ )
      sum += elem(r,c);
  return sum;
}

template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::SumR() const
{
  Matrix<DATATYPE> sumr(1,nocols);
  for( int r=0; r<norows; r++ ) {
    for( int c=0; c<nocols; c++ ) {
      if( r==0 )
	sumr(0,c) = elem(r,c);
      else 
	sumr(0,c) += elem(r,c);
    }
  }
  return sumr;
}


template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::SumC() const
{  
  Matrix<DATATYPE> sumc(norows,1);
  for( int c=0; c<nocols; c++ ) {
    for( int r=0; r<norows; r++ ) {      
      if( c==0 )
	sumc(r,0) = elem(r,c);
      else 
	sumc(r,0) += elem(r,c);
    }
  }
  return sumc;
} 

template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::Norm() const
{
  Matrix<DATATYPE> normed(*this);
  
  for( int c=0; c<nocols; c++ )
    for( int r=0; r<norows; r++ )
      if(normed(r,c) != 0)  /* 0/0 = 0 by convention */
	normed(r,c) /= sqrt(elem(r,r)*elem(c,c));

  return normed;
}

template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::Norm(const Matrix<DATATYPE>& rownorm,
					const Matrix<DATATYPE>& colnorm) const
{
  Matrix<DATATYPE> normed(*this);
  
  for( int c=0; c<nocols; c++ )
    for( int r=0; r<norows; r++ )
      normed(r,c) /= sqrt(rownorm(r,0)*colnorm(c,0));

  return normed;
}



template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::NormR() const
{
  Matrix<DATATYPE> normed(*this);
  
  Matrix<DATATYPE> norm = this->SumR();
  for( int c=0; c<nocols; c++ )
    for( int r=0; r<norows; r++ )
      normed(r,c) /= norm(0,c);
     
  return normed;
}


template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::NormC() const
{
  Matrix<DATATYPE> normed(*this);
  
  Matrix<DATATYPE> norm = this->SumC();
  for( int c=0; c<nocols; c++ )
    for( int r=0; r<norows; r++ )
      normed(r,c) /= norm(r,0);
     
  return normed;
}

template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::Abs() const
{
  Matrix<DATATYPE> M(norows,nocols);
  
  for( int c=0; c<nocols; c++ )
    for( int r=0; r<norows; r++ )
      M(r,c) = fabs((double)(*this)(r,c));
     
  return M;
}


template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::SubtractMeanR() const
{
  Matrix<DATATYPE> normed(*this);
  
  Matrix<DATATYPE> norm = this->SumR();
  for( int c=0; c<nocols; c++ )
    for( int r=0; r<norows; r++ )
      normed(r,c) -= norm(0,c)/norows;
     
  return normed;
}


template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::SubtractMeanC() const
{
  Matrix<DATATYPE> normed(*this);
  
  Matrix<DATATYPE> norm = this->SumC();
  for( int c=0; c<nocols; c++ )
    for( int r=0; r<norows; r++ )
      normed(r,c) -= norm(r,0)/nocols;
     
  return normed;
}

template<class DATATYPE>
void Matrix<DATATYPE>::SelfSubtractMeanR()
{
  Matrix<DATATYPE> norm = this->SumR();
  for( int c=0; c<nocols; c++ )
    for( int r=0; r<norows; r++ )
      elem(r,c) -= norm(0,c)/norows;    
}


template<class DATATYPE>
void Matrix<DATATYPE>::SelfSubtractMeanC() 
{
  Matrix<DATATYPE> norm = this->SumC();
  for( int c=0; c<nocols; c++ )
    for( int r=0; r<norows; r++ )
      elem(r,c) -= norm(r,0)/nocols;     
}

template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::NormalizeMassCenter() const
{
  Exception::Assert(norows == nocols, "Matrix must be square for NormalizeMassCenter function");

  Matrix<DATATYPE> N(norows,nocols);

  Matrix<DATATYPE> sr = SumR();
  Matrix<DATATYPE> sc = SumC();
  DATATYPE s = sr.SumC();

  for(int r = 0; r < norows; r++)
    for(int c = 0; c < nocols; c++)
      N(r,c) = elem(r,c) - sr(0,c)/norows - sc(r,0)/norows + s/(norows*norows);
  
  return N;
}

template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::Reorder(const vector<int>& order) const
{
  return ReorderR(order).ReorderC(order);
}

template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::ReorderR(const vector<int>& order) const
{
  Matrix<DATATYPE> M(norows,nocols);
  
  for(int i = 0; i < norows; i++)
    for(int c = 0; c < nocols; c++)
      M(i,c) = (*this)(order[i],c);
  
  return M;
}

template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::ReorderC(const vector<int>& order) const
{
  Matrix<DATATYPE> M(norows,nocols);
  
  for(int i = 0; i < nocols; i++)
    for(int r = 0; r < norows; r++)
      M(r,i) = (*this)(r,order[i]);
  
  return M;
}

template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::SortR(vector<int>& order) const
{
  return SumR().T().Sort(order);
}

template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::SortC(vector<int>& order) const
{
  return SumC().Sort(order);
}

template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::Sort(vector<int>& order) const
{
  Exception::Assert(nocols == 1, "Matrix must be a column vector for Sort function");
  Matrix<DATATYPE> M(*this);

  for(int r = 0; r < norows; r++)
    order[r] = r;
  
  for(int pass = 0; pass < norows-1; pass++)
    for(int r = 0; r < norows-1; r++)
      if(M(r,0) < M(r+1,0)){
	DATATYPE hold = M(r,0);
	M(r,0) = M(r+1,0);
	M(r+1,0) = hold;
	int pos = order[r];
	order[r] = order[r+1];
	order[r+1] = pos;
      }

  return M;
}

template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::RSort(vector<int>& order) const
{
  Exception::Assert(nocols == 1, "Matrix must be a column vector for Sort function");
  Matrix<DATATYPE> M(*this);

  for(int r = 0; r < norows; r++)
    order[r] = r;
  
  for(int pass = 0; pass < norows-1; pass++)
    for(int r = 0; r < norows-1; r++)
      if(M(r,0) > M(r+1,0)){
	DATATYPE hold = M(r,0);
	M(r,0) = M(r+1,0);
	M(r+1,0) = hold;
	int pos = order[r];
	order[r] = order[r+1];
	order[r+1] = pos;
      }

  return M;
}

template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::SortR(Matrix<int>& order) const
{
  Matrix<DATATYPE> M(*this);
  order.Resize(norows,nocols);
  
  for(int r = 0; r < norows; r++)
    for(int c = 0; c < nocols; c++)
      order(r,c) = c;
  
  for(int r = 0; r < norows; r++)
    for(int pass = 0; pass < nocols-1; pass++)
      for(int c = 0; c < nocols-1; c++)
	if(M(r,c) < M(r,c+1)){
	  DATATYPE hold = M(r,c);
	  M(r,c) = M(r,c+1);
	  M(r,c+1) = hold;
	  int pos = order(r,c);
	  order(r,c) = order(r,c+1);
	  order(r,c+1) = pos;
	}  
  return M;
}

template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::SortC(Matrix<int>& order) const
{
  Matrix<DATATYPE> M(*this);
  order.Resize(norows,nocols);  
  
  for(int c = 0; c < nocols; c++)
    for(int r = 0; r < norows; r++)
      order(r,c) = r;

  for(int c = 0; c < nocols; c++)  
    for(int pass = 0; pass < norows-1; pass++)
      for(int r = 0; r < norows-1; r++)
	if(M(r,c) < M(r+1,c)){
	  DATATYPE hold = M(r,c);
	  M(r,c) = M(r+1,c);
	  M(r+1,c) = hold;
	  int pos = order(r,c);
	  order(r,c) = order(r+1,c);
	  order(r+1,c) = pos;
	}  
  return M;
}

template<class DATATYPE>
DATATYPE Matrix<DATATYPE>::Max(pair<int,int>& bit) const
{
  DATATYPE max = -FLT_MAX;
  bit = make_pair(-1,-1);
  for( int r=0; r<norows; r++ )
    for( int c=0; c<nocols; c++ )
      if((*this)(r,c) > max){
	max = (*this)(r,c);
	bit = make_pair(r,c);
      }
  return max;
}

template<class DATATYPE>
DATATYPE Matrix<DATATYPE>::Min(pair<int,int>& bit) const
{
  DATATYPE min = FLT_MAX;
  bit = make_pair(-1,-1);
  for( int r=0; r<norows; r++ )
    for( int c=0; c<nocols; c++ )
      if((*this)(r,c) < min){
	min = (*this)(r,c);
	bit = make_pair(r,c);
      }
  return min;
}

template<class DATATYPE>
DATATYPE Matrix<DATATYPE>::NormFrobenius() const
{
  
  DATATYPE norm = 0;

  for( int r=0; r<norows; r++ )
    for( int c=0; c<nocols; c++ )
      norm += (*this)(r,c) * (*this)(r,c);

  return norm;
}

template<class DATATYPE>
DATATYPE Matrix<DATATYPE>::NormFrobenius(const Matrix<DATATYPE>& op2) const
{
  Exception::Assert(this->norows==op2.norows && this->nocols==op2.nocols,"Matrices have different sizes");

  DATATYPE norm = 0;

  for( int r=0; r<norows; r++ )
    for( int c=0; c<nocols; c++ )
      norm += (*this)(r,c) * op2(r,c);

  return norm;
}

template<class DATATYPE>
DATATYPE Matrix<DATATYPE>::AlignmentFrobenius(const Matrix<DATATYPE>& op2) const
{
  return NormFrobenius(op2) / sqrt(NormFrobenius()*op2.NormFrobenius());
}

template<class DATATYPE>
DATATYPE Matrix<DATATYPE>::AlignmentFrobenius(const Matrix<DATATYPE>& op2, DATATYPE op2_norm) const
{
  return NormFrobenius(op2) / sqrt(NormFrobenius()*op2_norm);
}

template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::exp(const DATATYPE& beta) const
{
  Matrix<DATATYPE> E,V,expE(norows,nocols);
  this->Eig(E, V);
  for(int r = 0; r < norows; r++)
    expE(r,r) = E(r,0);
  E = Matrix<DATATYPE>::exp(E*beta);
  for(int r = 0; r < norows; r++)
    expE(r,r) = E(r,0);
  return V.I() * expE * V;
}


template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::RandPerm() const
{
  Matrix<DATATYPE> M(*this);
  for(int s = 0; s < norows; s++){
    int r2,r1 = rand() % norows;
    while((r2 = (rand() % norows)) == r1)
      ;
    for(int c = 0; c < nocols; c++){
      DATATYPE tmp = M(r1,c); 
      M(r1,c) = M(r2,c);
      M(r2,c) = tmp;
    }
  }
  return M;
}


template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::I() const {
  
  Exception::Assert(norows==nocols,"Not a square matrix");
    
  Matrix<DATATYPE> a(*this);
  int n = norows; 
  // The integer arrays ipiv, indxr, and indxc 
  // are used for bookkeeping on the pivoting.  
  int indxc[n], indxr[n], ipiv[n];
  int i,icol,irow,j,k,l,ll; 
  float big,dum,pivinv,temp;
  
  for (j=0;j<n;j++) 
    ipiv[j]=0; 
  for (i=0;i<n;i++){  //    This is the main loop over the columns to be reduced.
    big=0.0; 
    for (j=0;j<n;j++) // This is the outer loop of the search for a pivot element.
      if (ipiv[j] != 1) 
	for (k=0;k<n;k++){
	  if (ipiv[k] == 0){
	    if (fabs(a(j,k)) >= big){
	      big=fabs(a(j,k));
	      irow=j; 
	      icol=k;
	    }
	  }
	  else 
	    Exception::Assert(ipiv[k] <= 1,"gaussj: Singular Matrix-1");
	} 
    ++(ipiv[icol]); 
    /*    
	  We now have the pivot element, so we interchange rows, if needed, to put the pivot element on the diagonal. The columns are not physically interchanged, only relabeled: indxc[i], the column of the ith pivot element, is the ith column that is reduced, while indxr[i] is the row in which that pivot element was originally located. If indxr[i] 6= indxc[i] there is an implied column interchange. With this form of bookkeeping, the solution b's will end up in the correct order, and the inverse matrix will be scrambled by columns.
    */
    if (irow != icol){
      for (l=0;l<n;l++) 
	SWAP(a(irow,l),a(icol,l));
    }
    // We are now ready to divide the pivot row by the
    // pivot element, located at irow and icol.
    indxr[i]=irow; 
    indxc[i]=icol; 
    Exception::Assert(a(icol,icol) != 0.0,"gaussj: Singular Matrix-2");
    pivinv=1.0/a(icol,icol); 
    a(icol,icol)=1.0; 
    for (l=0;l<n;l++) 
      a(icol,l) *= pivinv; 
    for (ll=0;ll<n;ll++) // Next, we reduce the rows...
      if (ll != icol){ // ...except for the pivot one, of course.
	dum=a(ll,icol); 
	a(ll,icol)=0.0; 
	for (l=0;l<n;l++) 
	  a(ll,l) -= a(icol,l)*dum; 
      }
  }
  /* 
     This is the end of the main loop over columns of the reduction. It only remains to unscramble the solution in view of the column interchanges. We do this by interchanging pairs of columns in the reverse order that the permutation was built up. 
  */
  for (l=n-1;l>=0;l--){
    if (indxr[l] != indxc[l])
      for (k=0;k<n;k++)
	SWAP(a(k,indxr[l]),a(k,indxc[l])); // And we are done. 
  }
  return a;
}

template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::joinByCol(const Matrix<DATATYPE>& op2) const
{
  Exception::Assert(this->norows==op2.norows,"Matrices have different number of rows");
  
  int norows = this->norows;
  int nocols = this->nocols + op2.nocols;
  Matrix<DATATYPE> M(norows,nocols);
  
  for(int r = 0; r < norows; r++){
    int c = 0;
    for(; c < this->nocols; c++)
      M(r,c) = (*this)(r,c);
    for(int c1 = 0; c1 < op2.nocols; c1++,c++)
      M(r,c) = op2(r,c1);
  }
  return M;
}

template<class DATATYPE>
Matrix<DATATYPE> Matrix<DATATYPE>::joinByRow(const Matrix<DATATYPE>& op2) const
{
  Exception::Assert(this->nocols==op2.nocols,"Matrices have different number of columns");

  int norows = this->norows + op2.norows;
  int nocols = this->nocols;
  
  Matrix<DATATYPE> M(norows,nocols);

  for(int c = 0; c < nocols; c++){
    int r = 0;
    for(; r < this->norows; r++)
      M(r,c) = (*this)(r,c);
    for(int r1 = 0; r1 < op2.norows; r1++,r++)
      M(r,c) = op2(r1,c);
  }
  return M;
}

template<class DATATYPE>
vector<DATATYPE> Matrix<DATATYPE>::toVector() const
{
  vector<DATATYPE> v(norows*nocols);

  for(int r = 0; r < norows; r++)
    for(int c = 0; c < nocols; c++)
      v[r*nocols+c] = (*this)(r,c);

  return v;
}

#endif

