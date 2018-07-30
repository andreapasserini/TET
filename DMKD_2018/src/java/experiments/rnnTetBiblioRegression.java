package experiments;

import tet.*;
import tet.rnn.*;

import java.io.*;

public class rnnTetBiblioRegression extends rnnTetLocalStore{

    public static void main(String[] args){
        rnnTetLocalStoreCommandLineOptions options = new rnnTetLocalStoreCommandLineOptions(args);

        System.out.println(options.toString());
        try {
            FileWriter out = new FileWriter(options.outfile, false);
            out.write(options.toString());

            String tetstring = Tet.tetString(options.tetfile);

            System.out.println("tetstring=" + tetstring);
            out.write("tetstring=" + tetstring + "\n");

            rnn_Tet tet = new rnn_Tet(tetstring);

            System.out.println("Read rnn Tet string: " + tet.Serialize());
            System.out.println("Tet Freevars = " + tet.freevars().toString());


            /* Read the tet values and it corresponding target value from the files */
            Dataset dataset = rnnTetLocalStore.getStoredDataset(options);

            /* Set the seed  and generate random parameters */
            if (options.seed != -1)
                tet.setRandomParameters(new RandomWeight(options.seed, 10));

            /* Initialize the optimizer */
            Optimizer adam = new Adam(tet, (float)0.5, (float)0.5, (float)0.5, (float)0.0000001);

            /* Initialize the learner */
            TetParameterLearner learner = new TetParameterLearner(tet, dataset, new MSE(), adam, out);

            /* LEarn the parameters of the regression task */
            learner.learnParameters(options.iterations_limit, 10, "regression");

            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

