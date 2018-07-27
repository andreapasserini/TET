package tet.rnn;

import java.util.*;
import java.sql.*;
import java.io.*;
import tet.*;
import myio.*;
import FastEMD.*;

public class HierarchicalHashTree {

  public int depth;
  public int max_depth;
  public int max_bucket_size;
  protected TetHistogramDistance distance_computer;
  protected Random random;
  protected Pair<Integer,Integer> z;
  public HierarchicalHashTree left_branch;
  public HierarchicalHashTree right_branch;
  protected Vector<Integer> items;
  protected int max_tries;
  protected TreeMap<Integer, TetHistogram> data_map;

  public HierarchicalHashTree()
  {
    this(0, 0, 0, null, null);
  }

  public HierarchicalHashTree(int depth, int max_depth, int max_bucket_size, TetHistogramDistance distance_computer, Random random)
  {
    this.depth = depth;
    this.max_depth = max_depth;  
    this.max_bucket_size = max_bucket_size;  
    this.distance_computer = distance_computer;
    this.random = random;
    this.z = null;
    this.left_branch = null;
    this.right_branch = null;
    this.items = null;
    this.max_tries = 10;
    this.data_map = null;
  }

  public boolean isLeaf()
  {
    return left_branch == null && right_branch == null;
  }

  public void extractZeta(Vector<Integer> ids)
    throws Exception
  {
    int tries = 0;
    double dist = 0;
    Integer z1, z2;

    do{
        z1 = ids.elementAt(random.nextInt(ids.size()));
        z2 = ids.elementAt(random.nextInt(ids.size()));
    //DEBUG            
    //System.out.println("z1: " + data_map.get(z1).toString());
    //System.out.println("z2: " + data_map.get(z2).toString());
        dist = distance_computer.distance(data_map.get(z1),data_map.get(z2));
        tries++; 
    } while (dist == 0 && tries < max_tries);
    
    System.out.println("(z1,z2) distance = " + dist);
    z = new Pair(z1,z2);
  }

  public Pair<Vector<Integer>, Vector<Integer>> splitData(Vector<Integer> ids)
    throws Exception
  {
    Vector<Integer> left_ids = new Vector<Integer>();
    Vector<Integer> right_ids = new Vector<Integer>();

    for (int i = 0; i < ids.size(); i++)    
    {   
      Integer id = ids.elementAt(i);
      TetHistogram histo = data_map.get(id);

      //DEBUG
      // System.out.println("histo id = " + id);
      // System.out.println("z1 id = " + z.first());
      // System.out.println("z2 id = " + z.second());

      if (distance_computer.distance(histo, data_map.get(z.first())) <= 
          distance_computer.distance(histo, data_map.get(z.second())))
        left_ids.add(id);
       else  
        right_ids.add(id);
    }

    return new Pair(left_ids, right_ids);
  }

  public void fill(Vector<Integer> ids, String bucket_idx_str, String parent_bucket_idx_str, TreeMap<Integer, TetHistogram> data)
    throws Exception
  
  {
    this.data_map = data;

    int size = ids.size();

    if (size == 0)
    {
      System.out.println("Bucket " + bucket_idx_str + " at level " + depth + " (parent " + parent_bucket_idx_str + ") --> " + size + " items");    
      return;
    }

    // At leaf, all histograms become items (no further partitioning)
    if (depth == max_depth || size <= max_bucket_size)      
    {
      System.out.println("Bucket " + bucket_idx_str + " at level " + depth + " (parent " + parent_bucket_idx_str + ") --> " + size + " items");    
      items = ids;
      return;
    }
   
    // extract zeta pair from set of data
    extractZeta(ids);

    // split data according to zeta pair
    Pair<Vector<Integer>, Vector<Integer>> splitted_data = splitData(ids);

    // recur in left branch    
    left_branch = new HierarchicalHashTree(depth+1, max_depth, max_bucket_size, distance_computer, random);
    String left_branch_idx_str = (bucket_idx_str == "ROOT") ? "0" : bucket_idx_str + "0";
    left_branch.fill(splitted_data.first(), left_branch_idx_str, bucket_idx_str, data);
  
    // recur in right branch
    right_branch = new HierarchicalHashTree(depth+1, max_depth, max_bucket_size, distance_computer, random);
    String right_branch_idx_str = (bucket_idx_str == "ROOT") ? "1" : bucket_idx_str + "1";
    right_branch.fill(splitted_data.second(), right_branch_idx_str, bucket_idx_str, data);
  }


  public Vector<Integer> getNearestBucket(TetHistogram x)
    throws Exception
  {
    if (isLeaf())
    {
      return items;
    }

    if (distance_computer.distance(x, data_map.get(z.first())) <= 
        distance_computer.distance(x, data_map.get(z.second())))
        return left_branch.getNearestBucket(x);
    else
        return right_branch.getNearestBucket(x);
  }
}
