/*
 *    Ecotype Simulation models the sequence diversity within a bacterial
 *    clade as the evolutionary result of net ecotype formation and periodic
 *    selection, yielding a certain number of ecotypes.
 *
 *    Copyright (C) 2009       Andrew Warner, Wesleyan University
 *    Copyright (C) 2009-2014  Jason M. Wood, Montana State University
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.io.File;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *  The Ecotype %Simulation Graphical User Interface (GUI).
 *
 *  @author Andrew Warner
 *  @author Jason M. Wood
 *  @copyright GNU General Public License
 */
public class SimulationGUI extends Simulation {

    /**
     *  SimulationGUI constructor.  Used for the graphical user
     *  interface of Ecotype Simulation.
     *
     *  @param masterVariables The MasterVariables object.
     *  @param fastaFile The fasta formated sequence file.
     *  @param newickFile The newick formated tree file.
     */
    public SimulationGUI (MasterVariables masterVariables,
        File fastaFile, File newickFile) {
        super (masterVariables, fastaFile, newickFile);
        execs = masterVariables.getExecs ();
        // Display the Ecotype Simulation GUI.
        makeGUI ();
        // None of the programs are currently running.
        running = false;
    }

    private void makeGUI () {
        gui = new JFrame ();
        gui.setTitle ("Ecotype Simulation");
        gui.setJMenuBar (makeMenuBar ());
        gui.setLayout (new BorderLayout (15, 15));
        gui.setMinimumSize (new Dimension (600, 450));
        gui.setPreferredSize (new Dimension (700, 700));
        gui.add (makeButtonPane (), BorderLayout.NORTH);
        gui.add (makeLogPane (), BorderLayout.CENTER);
        gui.pack ();
        //  Watch for window activation and closing events.
        gui.addWindowListener (new WindowAdapter () {
            public void windowClosing (WindowEvent we) {
                exitProgramActionPerformed ();
            }
        });
        // Start the HelpAboutWindow thread.
        helpAbout = new HelpAboutWindow (masterVariables);
        new Thread (helpAbout).start ();
        // Display the GUI window.
        gui.setVisible (true);
    }

    /**
     *  Make the menu bar.
     */
    private JMenuBar makeMenuBar () {
        JMenuBar menuBar = new JMenuBar ();
        JMenuItem openSequenceFile = new JMenuItem ();
        openSequenceFile.setText ("Load Sequence File");
        openSequenceFile.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent evt) {
                openSequenceFileActionPerformed ();
            }
        });
        JMenuItem loadProjectFile = new JMenuItem ();
        loadProjectFile.setText ("Load Project File");
        loadProjectFile.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent evt) {
                loadProjectFileActionPerformed ();
            }
        });
        JMenuItem saveProjectFile = new JMenuItem ();
        saveProjectFile.setText ("Save Project File");
        saveProjectFile.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent evt) {
                saveProjectFileActionPerformed ();
            }
        });
        JMenuItem exitProgram = new JMenuItem ();
        exitProgram.setText ("Exit");
        exitProgram.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent evt) {
                exitProgramActionPerformed ();
            }
        });
        // Add Menu Items to the File Menu.
        JMenu fileMenu = new JMenu ();
        fileMenu.setText ("File");
        fileMenu.add (openSequenceFile);
        fileMenu.addSeparator ();
        fileMenu.add (loadProjectFile);
        fileMenu.add (saveProjectFile);
        fileMenu.addSeparator ();
        fileMenu.addSeparator ();
        fileMenu.add (exitProgram);
        // Create Help About Menu.
        JMenu helpAboutMenu = new JMenu ();
        helpAboutMenu.setText ("Help");
        JMenuItem aboutWindow = new JMenuItem ();
        aboutWindow.setText (HelpAboutWindow.about);
        aboutWindow.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent evt) {
                helpAbout.setVisible (HelpAboutWindow.about);
            }
        });
        JMenuItem userGuideWindow = new JMenuItem ();
        userGuideWindow.setText (HelpAboutWindow.userGuide);
        userGuideWindow.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent evt) {
                helpAbout.setVisible (HelpAboutWindow.userGuide);
            }
        });
        JMenuItem licenseWindow = new JMenuItem ();
        licenseWindow.setText (HelpAboutWindow.license);
        licenseWindow.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent evt) {
                helpAbout.setVisible (HelpAboutWindow.license);
            }
        });
        helpAboutMenu.add (aboutWindow);
        helpAboutMenu.add (userGuideWindow);
        helpAboutMenu.add (licenseWindow);
        // Add Menus to the MenuBar.
        menuBar.add (fileMenu);
        menuBar.add (helpAboutMenu);
        return menuBar;
    }

    /**
     *  Make the button pane.
     */
    private JComponent makeButtonPane () {
        JPanel pane = new JPanel ();
        JPanel buttons = new JPanel ();
        // Create a border around the button area.
        pane.setLayout (new BorderLayout ());
        pane.add (Box.createRigidArea (
            new Dimension (0, 15)), BorderLayout.NORTH
        );
        pane.add (Box.createRigidArea (
            new Dimension (0, 15)), BorderLayout.SOUTH)
        ;
        pane.add (Box.createRigidArea (
            new Dimension (15, 0)), BorderLayout.EAST
        );
        pane.add (Box.createRigidArea (
            new Dimension (15, 0)), BorderLayout.WEST
        );
        // Add the buttons.
        buttons.setLayout (new GridLayout (1, 2, 15, 15));
        buttons.add (makeSimulationButtonPane ());
        buttons.add (makeConfidenceIntervalButtonPane ());
        pane.add (buttons, BorderLayout.CENTER);
        return pane;
    }

    /**
     *  Make the button pane for the simulation.
     */
    private JComponent makeSimulationButtonPane () {
        JPanel pane = new JPanel ();
        pane.setLayout (new GridLayout (5,1,15,15));
        // Create Buttons.
        JButton runBruteforce = new JButton ();
        runBruteforce.setText ("Run Bruteforce Search");
        runBruteforce.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent evt) {
                runBruteforceActionPerformed ();
            }
        });
        JButton runHillclimbing = new JButton ();
        runHillclimbing.setText ("Run Hillclimbing");
        runHillclimbing.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent evt) {
                runHillclimbingActionPerformed ();
            }
        });
        JButton runAll = new JButton ();
        runAll.setText ("Run Everything (no demarcation)");
        runAll.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent evt) {
                runAllActionPerformed ();
            }
        });
        JButton runAllDemarcation = new JButton ();
        runAllDemarcation.setText ("Run Everything (with demarcation)");
        runAllDemarcation.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent evt) {
                runAllDemarcationActionPerformed ();
            }
        });
        // Add everything to the pane.
        pane.add (runBruteforce);
        pane.add (runHillclimbing);
        pane.add (runAll);
        pane.add (runAllDemarcation);
        return pane;
    }

    /**
     *  Make the button pane for the confidence interval programs.
     */
    private JComponent makeConfidenceIntervalButtonPane () {
        JPanel pane = new JPanel ();
        pane.setLayout (new GridLayout (5,1,15,15));
        // Create Buttons.
        JButton omegaConfButton = new JButton ();
        omegaConfButton.setText ("Run Omega Confidence Interval");
        omegaConfButton.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent evt) {
                omegaConfActionPerformed ();
            }
        });
        JButton sigmaConfButton = new JButton ();
        sigmaConfButton.setText ("Run Sigma Confidence Interval");
        sigmaConfButton.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent evt) {
                sigmaConfActionPerformed ();
            }
        });
        JButton npopConfButton = new JButton ();
        npopConfButton.setText ("Run Npop Confidence Interval");
        npopConfButton.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent evt) {
                npopConfActionPerformed ();
            }
        });
        JButton demarcationButton = new JButton ();
        demarcationButton.setText ("Run Demarcation");
        demarcationButton.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent evt) {
                runDemarcationActionPerformed ();
            }
        });
        // Create the criterion selector.
        critSelector = new JComboBox<String> ();
        critSelector.setModel (new DefaultComboBoxModel<String> (
            masterVariables.getCriterionLabels ()
        ));
        critSelector.setSelectedIndex (masterVariables.getCriterion () - 1);
        critSelector.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent evt) {
                critSelectorActionPerformed (
                    critSelector.getSelectedIndex () + 1
                );
            }
        });
        JLabel critJLabel = new JLabel (
            "Select Criterion: ", JLabel.TRAILING
        );
        JPanel crit = new JPanel ();
        crit.add (critJLabel);
        crit.add (critSelector);
        // Add the Buttons to the Pane.
        pane.add (omegaConfButton);
        pane.add (sigmaConfButton);
        pane.add (npopConfButton);
        pane.add (demarcationButton);
        pane.add (crit);
        return pane;
    }

    /**
     *  Make the log pane.
     */
    private JComponent makeLogPane () {
        JScrollPane pane = new JScrollPane ();
        // Setup the log text area.
        final JTextArea logTextArea = new JTextArea ();
        logTextArea.setColumns (20);
        logTextArea.setRows (5);
        logTextArea.setFont(new Font("monospaced", Font.PLAIN, 12));
        logTextArea.setEditable (false);
        logTextArea.setDoubleBuffered (true);
        logTextArea.append (log.toString ());
        // Update the log text area when the log changes.
        log.addObserver (new Observer () {
            public void update (Observable o, Object str) {
                logTextArea.append ((String) str);
                // Auto update the caret position.
                logTextArea.setCaretPosition (log.length ());
                // Repaint the log area.
                logTextArea.repaint ();
            }
        });
        // Add the log text area to the pane.
        pane.setViewportView (logTextArea);
        return pane;
    }

    /**
     *  The user has asked to exit the program.
     */
    private void exitProgramActionPerformed () {
        System.exit (0);
    }

    /**
     *  The user has asked to change the criterion value.
     *
     *  @param criterion The new criterion value.
     */
    private void critSelectorActionPerformed (int criterion) {
        if (criterion != masterVariables.getCriterion ()) {
            // Update the criterion value.
            masterVariables.setCriterion (criterion);
            log.append (
                "Criterion set to: " +
                masterVariables.getCriterionLabel (criterion) + "\n"
            );
            if (bruteforce != null && bruteforce.hasRun ()) {
                log.append (
                    "The best bruteforce result using the new criterion " +
                    "value:\n"
                );
                log.append (bruteforce.getBestResult () + "\n");
            }
            // Update the hasRun variables if needed.
            if (hillclimb != null && hillclimb.hasRun ()) {
                log.append (
                    "You will need to rerun hillclimbing for the new " +
                    "criterion value.\n"
                );
                hillclimb.setHasRun (false);
                if (omegaCI != null) {
                    omegaCI.setHasRun (false);
                }
                if (sigmaCI != null) {
                    sigmaCI.setHasRun (false);
                }
                if (npopCI != null) {
                    npopCI.setHasRun (false);
                }
                if (demarcation != null) {
                    demarcation.setHasRun (false);
                }
            }
        }
    }

    /**
     *  The user has asked to save a project file.
     */
    private void saveProjectFileActionPerformed () {
        if (binning == null || ! binning.hasRun ()) {
            log.append (
                "Please run at least binning before saving to a file.\n"
            );
            return;
        }
        FileChooser fc = new FileChooser ("xml");
        int returnVal = fc.showSaveDialog (gui);
        if (returnVal == FileChooser.APPROVE_OPTION) {
            File userFile = fc.getSelectedFile ();
            String path = userFile.getPath ();
            // if it doesn't already have the xml extension, add it.
            String ext = path.substring (path.length () - 4, path.length ());
            if (! ext.equals (".xml")) {
                userFile = new File (userFile.getPath () + ".xml");
            }
            log.append ("Saving to: " + userFile.getName () + "\n");
            saveProjectFile (userFile);
        }
    }

    /**
     *  The user has asked to load a previously saved project file.
     */
    private void loadProjectFileActionPerformed () {
        FileChooser fc = new FileChooser ("xml");
        int returnVal = fc.showOpenDialog (gui);
        if (returnVal == FileChooser.APPROVE_OPTION) {
            File userFile = fc.getSelectedFile ();
            log.append ("Opening: " + userFile.getPath () + "\n\n");
            // Test that the file can be read.
            if (! userFile.canRead ()) {
                log.append ("Unable to read from file!\n");
                return;
            }
            // Load the project file.
            loadProjectFile (userFile);
            // Update the crit selector.
            critSelector.setSelectedIndex (
                masterVariables.getCriterion () - 1
            );
            // Output the values stored in the project file.
            if (nu > 0) {
                log.append ("Phylogeny results:\n");
                log.append (String.format (
                    "  %s environmental sequences.\n" +
                    "  %s is the outgroup.\n\n",
                    nu, outgroup
                ));
            }
            if (tree != null && tree.isValid ()) {
                // Launch NJPlot to view the tree.
                log.append ("Displaying the tree with NJplot.\n\n");
                File treeFile = new File (
                    masterVariables.getWorkingDirectory () + "outtree"
                );
                tree.save (treeFile);
                execs.openTree (treeFile);
            }
            if (binning != null && binning.hasRun ()) {
                log.append ("Binning result:\n");
                ArrayList<BinLevel> bins = binning.getBins ();
                for (int i = 0; i < bins.size (); i ++) {
                    log.append ("  " + bins.get (i).toString () + "\n");
                }
                log.append ("\n");
            }
            if (bruteforce != null && bruteforce.hasRun ()) {
                log.append ("Bruteforce result:\n");
                log.append (bruteforce.toString () + "\n\n");
            }
            if (hillclimb != null && hillclimb.hasRun ()) {
                log.append ("Hillclimbing result:\n");
                log.append (hillclimb.toString () + "\n\n");
            }
            if (omegaCI != null && omegaCI.hasRun ()) {
                log.append ("Omega confidence interval result:\n");
                log.append ("  " + omegaCI.toString () + "\n\n");
            }
            if (sigmaCI != null && sigmaCI.hasRun ()) {
                log.append ("Sigma confidence interval result:\n");
                log.append ("  " + sigmaCI.toString () + "\n\n");
            }
            if (npopCI != null && npopCI.hasRun ()) {
                log.append ("Npop confidence interval result:\n");
                log.append ("  " + npopCI.toString () + "\n\n");
            }
            if (demarcation != null && demarcation.hasRun ()) {
                log.append ("Demarcation result:\n");
                log.append (demarcation.toString () + "\n\n");
            }
        }
    }

   /**
    *  The user has asked to load a sequence file.
    */
    private void openSequenceFileActionPerformed () {
        FileChooser fc;
        int returnVal;
        // Open the fasta file chooser dialog.
        fc = new FileChooser ("fasta");
        returnVal = fc.showOpenDialog (gui);
        File sequenceFile = fc.getSelectedFile ();
        if (returnVal == FileChooser.APPROVE_OPTION) {
            fastaFile = fc.getSelectedFile ();
            loadSequenceFile ();
        }
        else {
            return;
        }
        // Ask the user if they want to provide or generate a tree.
        String[] options = { "Generate", "Newick" };
        int type = 0; // Default to using the Parsimony method.
        type = JOptionPane.showOptionDialog (
            gui,
            "Generate a tree with FastTree or use a Newick formatted file?",
            "Tree Type",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[type]
        );
        switch (options[type]) {
            case "Generate":
                // Generate a tree with FastTree.
                generateTree ();
                break;
            case "Newick":
                // Open the newick file chooser dialog.
                fc = new FileChooser ("newick");
                returnVal = fc.showOpenDialog (gui);
                if (returnVal == FileChooser.APPROVE_OPTION) {
                    newickFile = fc.getSelectedFile ();
                    loadTreeFile ();
                }
                break;
        }
        // Launch NJPlot to view the tree.
        log.append ("Displaying the tree with NJplot.\n\n");
        execs.openTree (newickFile);
        // Run binning on the tree.
        runBinning ();
    }

    /**
     *  The user has asked to run the bruteforce program,
     *  but not the hillclimb, omega, sigma, or npop confidence interval
     *  programs, or the demarcation program.
     */
    private void runBruteforceActionPerformed () {
        if (binning == null || ! binning.hasRun ()) {
            log.append ("Please open a valid fasta file first.\n");
            return;
        }
        Thread thread = new Thread () {
            public void run () {
                if (running) {
                    log.append ("Already running...\n");
                    return;
                }
                running = true;
                runBruteforce ();
                running = false;
            }
        };
        thread.start ();
    }

    /**
     *  The user has asked to run the hillclimbing program, but not the omega,
     *  sigma, or npop confidence interval programs, or the demarcation
     *  program.
     */
    private void runHillclimbingActionPerformed () {
        if (bruteforce == null || ! bruteforce.hasRun ()) {
            log.append ("Please run through bruteforce first.\n");
            return;
        }
        Thread thread = new Thread () {
            public void run () {
                if (running) {
                    log.append ("Already running...\n");
                    return;
                }
                running = true;
                runHillclimbing ();
                running = false;
            }
        };
        thread.start ();
    }

    /**
     *  The user has asked to run the binning, bruteforce, hillclimb, and the
     *  omega, sigma, and npop confidence interval programs, but not the
     *  demarcation program.
     */
    private void runAllActionPerformed () {
        if (nu == 0) {
            log.append ("Please open a valid fasta file first.\n");
            return;
        }
        Thread thread = new Thread () {
            public void run () {
                if (running) {
                    log.append ("Already running...\n");
                    return;
                }
                running = true;
                runBruteforce ();
                runHillclimbing ();
                runOmegaConfidenceInterval ();
                runSigmaConfidenceInterval ();
                runNpopConfidenceInterval ();
                running = false;
            }
        };
        thread.start ();
    }

    /**
     *  The user has asked to run the binning, bruteforce, hillclimb, the
     *  omega, sigma, and npop confidence interval programs, and the
     *  demarcation program.
     */
    private void runAllDemarcationActionPerformed () {
        if (nu == 0) {
            log.append ("Please open a valid fasta file first.\n");
            return;
        }
        Thread thread = new Thread () {
            public void run () {
                if (running) {
                    log.append ("Already running...\n");
                    return;
                }
                running = true;
                runBruteforce ();
                runHillclimbing ();
                runOmegaConfidenceInterval ();
                runSigmaConfidenceInterval ();
                runNpopConfidenceInterval ();
                runDemarcation ();
                running = false;
            }
        };
        thread.start ();
    }

    /**
     *  The user has asked to run just the omega confidence interval program.
     */
    private void omegaConfActionPerformed () {
        if (binning == null || ! binning.hasRun () ||
            hillclimb == null || ! hillclimb.hasRun ()) {
            log.append ("Please run through hillclimbing first.\n");
            return;
        }
        Thread thread = new Thread () {
            public void run () {
                if (running) {
                    log.append ("Already running...\n");
                    return;
                }
                running = true;
                runOmegaConfidenceInterval ();
                running = false;
            }
        };
        thread.start ();
    }

    /**
     *  The user has asked to run just the sigma confidence interval program.
     */
    private void sigmaConfActionPerformed () {
        if (binning == null || ! binning.hasRun () ||
            hillclimb == null || ! hillclimb.hasRun ()) {
            log.append ("Please run through hillclimbing first.\n");
            return;
        }
        Thread thread = new Thread () {
            public void run () {
                if (running) {
                    log.append ("Already running...\n");
                    return;
                }
                running = true;
                runSigmaConfidenceInterval ();
                running = false;
            }
        };
        thread.start ();
    }

    /**
     *  The user has asked to run just the npop confidence interval program.
     */
    private void npopConfActionPerformed () {
        if (binning == null || ! binning.hasRun () ||
            hillclimb == null || ! hillclimb.hasRun ()) {
            log.append ("Please run through hillclimbing first.\n");
            return;
        }
        Thread thread = new Thread () {
            public void run () {
                if (running) {
                    log.append ("Already running...\n");
                    return;
                }
                running = true;
                runNpopConfidenceInterval ();
                running = false;
            }
        };
        thread.start ();
    }

    /**
     *  The user has asked to run just the demarcation program.
     */
    private void runDemarcationActionPerformed () {
        if (binning == null || ! binning.hasRun () ||
            hillclimb == null || ! hillclimb.hasRun ()) {
            log.append ("Please run through hillclimbing first.\n");
            return;
        }
        Thread thread = new Thread () {
            public void run () {
                if (running) {
                    log.append ("Already running...\n");
                    return;
                }
                running = true;
                runDemarcation ();
                running = false;
            }
        };
        thread.start ();
    }

    private JFrame gui;
    private JComboBox<String> critSelector;

    private Execs execs;
    private HelpAboutWindow helpAbout;

    private boolean running;

}
