/*
 *    Ecotype Simulation models the sequence diversity within a bacterial
 *    clade as the evolutionary result of net ecotype formation and periodic
 *    selection, yielding a certain number of ecotypes.
 *
 *    Copyright (C) 2013-2014  Jason M. Wood, Montana State University
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package ecosim;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 *  Object to interact with the bruteforce search program.
 *
 *  @author Jason M. Wood
 *  @copyright GNU General Public License
 */
public class Bruteforce implements Runnable {

    /**
     *  Run the bruteforce program.
     *
     *  @param masterVariables The MasterVariables object.
     *  @param nu The number of environmental sequences.
     *  @param length The length of the sequences being analyzed.
     *  @param binning The Binning object.
     */
    public Bruteforce (MasterVariables masterVariables,
        int nu, int length, Binning binning) {
        this.masterVariables = masterVariables;
        this.nu = nu;
        this.length = length;
        this.binning = binning;
        results = new ArrayList<ParameterSet<Likelihood>> ();
        String workingDirectory = masterVariables.getWorkingDirectory ();
        inputFileName = workingDirectory + "bruteforceIn.dat";
        outputFileName = workingDirectory + "bruteforceOut.dat";
        hasRun = false;
    }

    /**
     *  Run the bruteforce program.
     */
    public void run () {
        Execs execs = masterVariables.getExecs ();
        File inputFile = new File (inputFileName);
        File outputFile = new File (outputFileName);
        // Write the input values for the program to the input file.
        writeInputFile (inputFile);
        // Run the bruteforce program.
        execs.runBruteforce (inputFile, outputFile);
        // Get the results from the output of the bruteforce program.
        readOutputFile (outputFile);
        // Set the flag stating the the bruteforce program has been run.
        if (results.size () > 0) {
            hasRun = true;
        }
    }

    /**
     *  Returns true if bruteforce has been run, false otherwise.
     *
     *  @return True if bruteforce has been run, false otherwise.
     */
    public boolean hasRun () {
        return hasRun;
    }

    /**
     *  Returns all of the results.
     *
     *  @return The results.
     */
    public ArrayList<ParameterSet<Likelihood>> getResults () {
        return results;
    }

    /**
     *  Sort the results and return the best one.
     *
     *  @return The best result.
     */
    public ParameterSet<Likelihood> getBestResult () {
        ParameterSet<Likelihood> result;
        if (results.size () > 1) {
            // Sort the results.
            Heapsorter<ParameterSet<Likelihood>> h =
                new Heapsorter<ParameterSet<Likelihood>> ();
            h.heapSort (results);
        }
        if (results.size () > 0) {
            result = results.get (0);
        }
        else {
            Double likelihood[] = { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
            result = new ParameterSet<Likelihood> (
                0.0, 0.0, 0, new Likelihood (masterVariables, likelihood)
            );
        }
        return result;
    }

    /**
     *  Return the number of results.
     *
     *  @return The number of results.
     */
    public int getNumResults () {
        return results.size ();
    }

   /**
     *  Add a result to the list of results.
     *
     *  @param result The result to add to the list.
     */
    public void addResult (ParameterSet<Likelihood> result) {
      results.add (result);
    }

    /**
     *  Changes the value of hasRun.
     *
     *  @param hasRun The new value of hasRun.
     */
    public void setHasRun (boolean hasRun) {
        this.hasRun = hasRun;
    }

    /**
     *  Returns the best bruteforce search result as a String.
     *
     *  @return the best bruteforce search result.
     */
    public String toString () {
        return getBestResult ().toString ();
    }

    /**
     *  Private method to read in all of the results from the bruteforce
     *  program.
     *
     *  @param outputFile The file to read from.
     */
    private void readOutputFile (File outputFile) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader (new FileReader (outputFile));
            // Read the first line of the file.
            String nextLine = reader.readLine ();
            while (nextLine != null) {
                // Remove the commas from the line.
                nextLine = nextLine.replace (',', ' ');
                // Break each value in the string into individual tokens.
                StringTokenizer st = new StringTokenizer (nextLine);
                Double omega = new Double (st.nextToken ());
                Double sigma = new Double (st.nextToken ());
                Integer npop = new Integer (st.nextToken ());
                Double [] p = new Double[6];
                for (int i = 0; i < 6; i ++) {
                    p[i] = new Double (st.nextToken ());
                }
                // Add the result to the list of results.
                ParameterSet<Likelihood> result =
                    new ParameterSet<Likelihood> (
                        omega, sigma, npop,
                        new Likelihood (masterVariables, p)
                    );
                results.add (result);
                // Read the next line of the file.
                nextLine = reader.readLine ();
            }
        }
        catch (IOException e) {
            System.out.println ("Error reading the output file.");
        }
        finally {
            if (reader != null) {
                try {
                    reader.close ();
                }
                catch (IOException e) {
                    System.out.println ("Error closing the output file.");
                }
            }
        }
    }

    /**
     *  Private method to write the input file for the bruteforce program.
     *
     *  @param inputFile The file to write to.
     */
    private void writeInputFile (File inputFile) {
        ArrayList<BinLevel> bins = binning.getBins ();
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter (new FileWriter (inputFile));
            writer.write (String.format ("%-20d numcrit\n", bins.size ()));
            // Output the crit levels and the number of clusters.
            for (int j = 0; j < bins.size (); j ++) {
                writer.write (String.format (
                    "%-20.6f %-20d\n",
                    bins.get (j).getCrit (),
                    bins.get (j).getLevel ()
                ));
            }
            // Write the range of omega.
            writer.write (String.format ("%-20s omega range\n",
                String.format ("%.4f,%.4f", omegaRange[0], omegaRange[1])
            ));
            // Write the range of sigma.
            writer.write (String.format ("%-20s sigma range\n",
                String.format ("%.4f,%.4f", sigmaRange[0], sigmaRange[1])
            ));
            // Write the range of npop.
            writer.write (String.format ("%-20s npop range\n",
                String.format ("%d,%d", 1, nu)
            ));
            // Write the numics values.
            writer.write (String.format (
                "%-20s numincs (omega, sigma, npop)\n",
                String.format (
                    "%d,%d,%d", numincs[0], numincs[1], numincs[2]
                )
            ));
            // Write the nu value.
            writer.write (String.format ("%-20d nu\n", nu));
            // Write the nrep value.
            writer.write (String.format ("%-20d nrep\n", nrep));
            // Create the random number seed; an odd integer less than nine
            // digits long.
            long iii = (long)(100000000 * Math.random ());
            if (iii % 2 == 0) {
                iii ++;
            }
            // Write the random number seed.
            writer.write (
                String.format ("%-20d iii (random number seed)\n", iii)
            );
            // Write the length of the sequences.
            writer.write (
                String.format (
                    "%-20d lengthseq (after deleting gaps, etc.)\n",
                    length
                )
            );

        }
        catch (IOException e) {
            System.out.println (
                "Error writing the input file."
            );
        }
        finally {
            if (writer != null) {
                try {
                    writer.close ();
                }
                catch (IOException e) {
                    System.out.println (
                        "Error closing the input file."
                    );
                }
            }
        }
    }

    private String inputFileName;
    private String outputFileName;

    private MasterVariables masterVariables;
    private int nu;
    private int length;
    private Binning binning;

    private int nrep = 100;
    private double[] omegaRange = { 0.001, 100.0 };
    private double[] sigmaRange = { 0.001, 100.0 };
    private int[] numincs = { 20, 20, 20 };

    private ArrayList<ParameterSet<Likelihood>> results;

    private boolean hasRun;

}
