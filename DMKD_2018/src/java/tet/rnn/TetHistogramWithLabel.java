package tet.rnn;

import java.util.*;
import java.sql.*;
import java.io.*;
import tet.*;

public class TetHistogramWithLabel{

    public TetHistogram tetHistogram;
    public Integer label;
    
    public TetHistogramWithLabel()
    {
    
    }

    public TetHistogramWithLabel(TetHistogram tetHistogram, Integer label)
    {
        this.tetHistogram = tetHistogram;
        this.label = label;
    }
}
