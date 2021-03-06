/*
 *    Ecotype Simulation models the sequence diversity within a bacterial
 *    clade as the evolutionary result of net ecotype formation and periodic
 *    selection, yielding a certain number of ecotypes.
 *
 *    Copyright (C) 2009-2013  Jason M. Wood, Montana State University
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
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import javax.swing.text.html.HTMLDocument;

/**
 *  Displays a Help/About GUI window.
 *
 *  @author Jason M. Wood
 *  @copyright GNU General Public License
 */
public class HelpAboutWindow extends JFrame implements Runnable {

    public HelpAboutWindow (MasterVariables masterVariables) {
        this.masterVariables = masterVariables;
    }

    public void run () {
        // Preprocess the help files.
        preprocess ("stylesheet.css");
        preprocess ("menu.html");
        preprocess ("about.html");
        preprocess ("userguide.html");
        preprocess ("license.html");
        // Make an index pane for each possible entry point.
        aboutPane = makeIndexPane (about);
        userGuidePane = makeIndexPane (userGuide);
        licensePane = makeIndexPane (license);
        // Make the Help / About window.
        scrollPane = new JScrollPane ();
        setTitle ("Help / About");
        setMinimumSize (new Dimension (640, 480));
        setPreferredSize (new Dimension (1420, 800));
        setLayout (new BorderLayout ());
        add (scrollPane, BorderLayout.CENTER);
        // Listen for the window to close.
        addWindowListener (new WindowAdapter () {
            public void windowClosing (WindowEvent evt) {
                exitActionPerformed ();
            }
        });
        pack ();
    }

    /**
     *  Set the HelpAboutWindow visible, with the given pane selected.
     *
     *  @param type The type of pane to display.
     */
    public void setVisible (String type) {
        switch (type) {
            case about:
                scrollPane.setViewportView (aboutPane);
                break;
            case userGuide:
                scrollPane.setViewportView (userGuidePane);
                break;
            case license:
                scrollPane.setViewportView (licensePane);
                break;
            default:
                break;
        }
        setVisible (true);
    }

    /**
     *  Preproccess the help files to place them in the appropriate
     *  directory, and replace variables with their proper values.
     *
     *  @param page The page to preprocess.
     */
    private void preprocess (String page) {
        File inputFile = new File (
            masterVariables.getHelpDirectory () + page
        );
        File outputFile = new File (
            masterVariables.getWorkingDirectory () + page
        );
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            reader = new BufferedReader (new FileReader (inputFile));
            writer = new BufferedWriter (new FileWriter (outputFile));
            String nextLine = reader.readLine ();
            while (nextLine != null) {
                // Point the images to the right directory.
                nextLine = nextLine.replaceAll ("<img src=\"images",
                    String.format (
                        "<img src=\"%simages",
                        masterVariables.getHelpDirectory ()
                    )
                );
                // Replace variables with their values.
                nextLine = nextLine.replaceAll (
                    "%ECOSIM_VERSION%", masterVariables.getVersion ()
                );
                // Write the contents to the new file.
                writer.write (nextLine + "\n");
                nextLine = reader.readLine ();
            }
        }
        catch (IOException e) {
            System.out.println ("Error reading the html file.");
        }
        finally {
            if (reader != null) {
                try {
                    reader.close ();
                }
                catch (IOException e) {
                    System.out.println ("Error closing the input file.");
                }
            }
            if (writer != null) {
                try {
                    writer.close ();
                }
                catch (IOException e) {
                    System.out.println ("Error closing the output file.");
                }
            }
        }
    }

    /**
     *  Make the Index page.
     *
     *  @param type The type of index to display.
     */
    private JEditorPane makeIndexPane (String type) {
        JEditorPane editorPane = new JEditorPane ();
        File indexFile = null;
        String index =
            "<!DOCTYPE html>\n" +
            "<html lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
            "  <head>\n" +
            "    <title>\n" +
            "      Help / About\n" +
            "    </title>\n" +
            "  </head>\n" +
            "  <frameset cols=\"20%,80%\">\n" +
            "    <frame src=\"file:///" +
            masterVariables.getWorkingDirectory () + "menu.html" +
            "\" name=\"menu\" />\n";
        switch (type) {
            case about:
                indexFile = new File (
                    masterVariables.getWorkingDirectory () +
                    "aboutIndex.html"
                );
                index += "    <frame src=\"file:///" +
                    masterVariables.getWorkingDirectory () +
                    "about.html\" name=\"body\" />\n";
                break;
            case userGuide:
                indexFile = new File (
                    masterVariables.getWorkingDirectory () +
                    "userGuideIndex.html"
                );
                index += "    <frame src=\"file:///" +
                    masterVariables.getWorkingDirectory () +
                    "userguide.html" + "\" name=\"body\" />\n";
                break;
            case license:
                indexFile = new File (
                    masterVariables.getWorkingDirectory () +
                    "licenseIndex.html"
                );
                index += "    <frame src=\"file:///" +
                    masterVariables.getWorkingDirectory () +
                    "license.html" + "\" name=\"body\" />\n";
                break;
            default:
                break;
        }
        index += "  </frameset>\n";
        index += "</html>\n";
        try {
            BufferedWriter out = new BufferedWriter (
                new FileWriter (indexFile)
            );
            out.write (index);
            out.close ();
            editorPane.setPage (indexFile.toURI ().toURL ());
        }
        catch (Exception e) {
            e.printStackTrace ();
        }
        editorPane.setContentType ("text/html;charset=UTF-8");
        editorPane.setEditable (false);
        editorPane.addHyperlinkListener (new Hyperactive ());
        return editorPane;
    }

    /**
     *  Perform the exit action.
     */
    private void exitActionPerformed () {
        dispose ();
    }

    /**
     *  Availiable pages.
     */
    public static final String about = "About";
    public static final String userGuide = "User Guide";
    public static final String license = "License";

    /**
     *  Private variables.
     */
    private MasterVariables masterVariables;
    private JEditorPane aboutPane;
    private JEditorPane userGuidePane;
    private JEditorPane licensePane;

    private JScrollPane scrollPane;

    /**
     *  Defines a custom HyperlinkLister to listen for clicked hyperlinks.
     */
    private class Hyperactive implements HyperlinkListener {
        public void hyperlinkUpdate (HyperlinkEvent evt) {
            if (evt.getEventType () == HyperlinkEvent.EventType.ACTIVATED) {
                JEditorPane pane = (JEditorPane) evt.getSource ();
                try {
                    String url = evt.getURL ().toString ();
                    // Open external links in the default browser.
                    if (url.startsWith ("http")) {
                        Desktop d = Desktop.getDesktop ();
                        d.browse (evt.getURL ().toURI ());
                    }
                    // Open mailto links in the default mailer.
                    else if (url.startsWith ("mailto")) {
                        Desktop d = Desktop.getDesktop ();
                        d.mail (evt.getURL ().toURI ());
                    }
                    // Open internal links within the proper frame.
                    else if (evt instanceof HTMLFrameHyperlinkEvent) {
                        HTMLFrameHyperlinkEvent frameEvt;
                        HTMLDocument doc;
                        frameEvt = (HTMLFrameHyperlinkEvent) evt;
                        doc = (HTMLDocument) pane.getDocument ();
                        doc.processHTMLFrameHyperlinkEvent (frameEvt);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace ();
                }
            }
        }
    }
}
