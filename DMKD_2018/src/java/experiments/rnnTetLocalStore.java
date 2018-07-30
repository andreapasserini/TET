package experiments;

import tet.Tet;
import tet.Value;
import tet.rnn.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StreamTokenizer;

public class rnnTetLocalStore {
    public static void main(String[] args){

        Dataset dataset = new Dataset();
        rnnTetLocalStoreCommandLineOptions options = new rnnTetLocalStoreCommandLineOptions(args);
        System.out.println(options.toString());
        try {


            String tetstring = Tet.tetString(options.tetfile);

            System.out.println("tetstring=" + tetstring);

            rnn_Tet tet = new rnn_Tet(tetstring);

            System.out.println("Read rnn Tet string: " + tet.Serialize());
            System.out.println("Tet Freevars = " + tet.freevars().toString());


            /* Read the tet values and it corresponding target value from the files */
            FileReader train_data = new FileReader(options.trainfile);
            BufferedReader data_reader = new BufferedReader(train_data);

            FileReader train_label = new FileReader(options.trainlabel);
            BufferedReader label_reader = new BufferedReader(train_label);

            String datum = null;
            String target = null;

            while ((datum=data_reader.readLine())!=null){
                Value tetvalue = new Value(datum);
                target = label_reader.readLine();
                dataset.addElement(tetvalue, Float.valueOf(target));
            }

            /* Read the tet values and it corresponding target value from the files */
            FileReader test_data = new FileReader(options.testfile);
            data_reader = new BufferedReader(test_data);

            FileReader test_label = new FileReader(options.testlabel);
            label_reader = new BufferedReader(test_label);

            while ((datum=data_reader.readLine())!=null){
                Value tetvalue = new Value(datum);
                target = label_reader.readLine();
                dataset.addTestElement(tetvalue, Float.valueOf(target));
            }


            if (options.seed != -1)
                tet.setRandomParameters(new RandomWeight(options.seed, 50));
            Optimizer adam = new Adam(tet, (float)0.5, (float)0.5, (float)0.5, (float)0.0000001);
            //System.out.println(adam);
            /* TET parameter learning procedure */
            TetParameterLearner learner = new TetParameterLearner(tet, dataset, new MSE(), adam);
            //learner.learnParameters(options.iterations_limit);

            FileWriter out = new FileWriter(options.outfile, false);

            float error = learner.test(false);

            out.write(tet.parametersToString() + "\n");
            out.write("Test error=" + error);
            out.close();

        }catch(Exception e){ e.printStackTrace();}
    }

    public static Dataset getStoredDataset(rnnTetLocalStoreCommandLineOptions options){
        Dataset dataset = new Dataset();
        try{

            /* Read the tet values and it corresponding target value from the files */
            FileReader train_data = new FileReader(options.trainfile);
            BufferedReader data_reader = new BufferedReader(train_data);

            FileReader train_label = new FileReader(options.trainlabel);
            BufferedReader label_reader = new BufferedReader(train_label);

            String datum = null;
            String target = null;

            while ((datum=data_reader.readLine())!=null){
                Value tetvalue = new Value(datum);
                target = label_reader.readLine();
                dataset.addElement(tetvalue, Float.valueOf(target));
            }

            /* Read the tet values and it corresponding target value from the files */
            FileReader test_data = new FileReader(options.testfile);
            data_reader = new BufferedReader(test_data);

            FileReader test_label = new FileReader(options.testlabel);
            label_reader = new BufferedReader(test_label);

            while ((datum=data_reader.readLine())!=null){
                Value tetvalue = new Value(datum);
                target = label_reader.readLine();
                dataset.addTestElement(tetvalue, Float.valueOf(target));
            }

            return dataset;
        }catch(Exception e){
            e.printStackTrace();
        }
        return dataset;
    }
}


class rnnTetLocalStoreCommandLineOptions {

    public String tetfile;
    public String trainfile;
    public String testfile;
    public String trainlabel;
    public String testlabel;
    public String outfile;

    public String srcdb = null;
    public String testviewconfigfile = null;

    public boolean compute_tet_value = true;
    public boolean compute_tet_false_value = false;

    public int iterations_limit = 100;
    public int n_examples = Integer.MAX_VALUE;
    public int seed = -1;

    public rnnTetLocalStoreCommandLineOptions(String[] args)
    {
        int pos = parseOptions(args);

        // chek if compulsory options specified
        if(args.length - pos < 6){
            System.out.println("Missing compulsory option(s):\n");
            printHelp();
            System.exit(1);
        }

        tetfile = args[pos++];
        trainfile = args[pos++];
        testfile = args[pos++];
        trainlabel = args[pos++];
        testlabel = args[pos++];
        outfile = args[pos++];
    }

    public int parseOptions(String[] options)
    {
        int pos = 0;
        while(pos < options.length && options[pos].charAt(0) == '-'){

            switch(options[pos].charAt(1)){
                case 'c':
                    testviewconfigfile = options[++pos];
                    break;
                case 'd':
                    srcdb = options[++pos];
                    break;
                case 'v':
                    compute_tet_value = false;
                    break;
                case 'f':
                    compute_tet_false_value = true;
                    break;
                case 'i':
                    iterations_limit = Integer.valueOf(options[++pos]);
                    break;
                case 'n':
                    n_examples = Integer.valueOf(options[++pos]);
                    break;
                case 's':
                    seed = Integer.valueOf(options[++pos]);
                    break;
                case 'h':
                default:
                    printHelp();
                    System.exit(1);
            }
            pos++;
        }
        return pos;
    }

    public void printHelp()
    {
        System.out.println("Usage:\n\ttet.rnn.rnn_Tet [options] <tetfile> <trainfile> <testfile> <trainlabel> <testlabel> <objfile> <outfile>");
        System.out.println("Options:");
        System.out.println("\t-c <int>   \tconfig file for test view (default=null)");
        System.out.println("\t-d <string>\tsrc db for test view (default=null)");
        System.out.println("\t-v         \tcompute tet value (default=true)");
        System.out.println("\t-f         \tcompute tet false value (default=false)");
        System.out.println("\t-i         \tmaximum number of iterations (default=100)");
        System.out.println("\t-n         \textract only the first n elements from the db (default=MAX_VALUE)");
        System.out.println("\t-h         \tprint help");
        System.exit(1);
    }

    public String toString()
    {
        return    "tetfile=" + tetfile + "\n"
                + "trainfile=" + trainfile + "\n"
                + "testfile=" + testfile + "\n"
                + "trainlabel=" + trainlabel + "\n"
                + "testlabel=" + testlabel + "\n"
                + "outfile=" + outfile + "\n"
                + "srcdb=" + srcdb + "\n"
                + "testviewconfigfile=" + testviewconfigfile + "\n"
                + "compute_tet_value=" + compute_tet_value + "\n"
                + "compute_tet_false_value=" + compute_tet_false_value + "\n"
                + "iterations_limit=" + iterations_limit + "\n"
                + "n_examples=" + n_examples + "\n"
                + "seed=" + seed + "\n";
    }
}

