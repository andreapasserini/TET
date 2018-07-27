package tet.rnn;

import java.util.*;
import java.sql.*;
import java.io.*;
import tet.*;

public class TetHistogramWithLabelId{

    public TetHistogram tetHistogram;
    public Integer label;
    public Integer id;    

    public TetHistogramWithLabelId()
    {
    
    }

    public TetHistogramWithLabelId(TetHistogram tetHistogram, Integer label, Integer id)
    {
        this.tetHistogram = tetHistogram;
        this.label = label;
        this.id = id;
    }
}
