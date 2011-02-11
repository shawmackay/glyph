/*
 Copyright 2006 thor.jini.org Project

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */

/*
 * Created on 24-Jul-2004
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */

package org.jini.glyph.di;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @
 * @author Calum
 * 
 */
public class ConfigurationLoader implements Serializable {
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3258407344076699960L;

    URL source;

    String currentSection;

    int bracketCount = 0;

    private ConfigurationFileScan currentScan;

    public ConfigurationLoader(String urlloc) {

        try {

            this.source = new URL(urlloc);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            File f = new File(urlloc);
            try {
                this.source = f.toURL();
            } catch (MalformedURLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    public ConfigurationLoader(URL source) {
        this.source = source;
    }

    public ConfigurationFileScan getScan() {
        return this.currentScan;
    }

    public void parse() throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(source.openStream()), 2048);
        String line;
        line = reader.readLine();
        boolean inassignment = false;
        ConfigurationComponent currentComponent = null;
        currentScan = new ConfigurationFileScan(source.toExternalForm());
        ConfigurationEntry currentEntry = null;
        String currentvariable;
        String currentExpression;
        StringBuffer currentComment = new StringBuffer();
        while (line != null) {
            line = line.trim();
            if (line.startsWith("import")) {
                String importname = line.substring(7, line.indexOf(";"));
                // System.out.println("Import statement found: [" + importname +
                // "]");
                currentScan.addImport(importname);
            }
            if (line.indexOf("#include") != -1) {
                int quotePos = line.indexOf("'");
                String includename = line.substring(quotePos + 1, line.indexOf("'", quotePos + 1));
                // System.out.println("Include directive found: [" + includename
                // + "]");
                currentScan.addInclude(includename);
            }
            if (line.endsWith("{") && line.indexOf("=") == -1) {

                if (++bracketCount == 1) {
                    currentSection = line.substring(0, line.length() - 1).trim();
                    currentComponent = new ConfigurationComponent(currentSection);
                    // System.out.println("Section found: " + currentSection);
                }
                // System.out.println("BracketCount=" + bracketCount);
            }
            if (line.endsWith("}")) {
                if (--bracketCount == 0) {
                    currentScan.addComponent(currentComponent);
                    // System.out.println("Section ended: " + currentSection);
                }
                // System.out.println("BracketCount=" + bracketCount);
            }
            // Locate comments
            if (line.startsWith("/*")) {
                // System.out.println("Comment Started");
                StringBuffer comment = new StringBuffer();
                boolean end_of_comment = false;
                if (line.startsWith("/*") && line.endsWith("*/")) {
                    comment.append(line.substring(line.indexOf("/*") + 2, line.indexOf("*/")));
                } else {
                    comment.append(line.substring(line.indexOf("/*") + 2));
                    String nextLine = reader.readLine();
                    while (nextLine != null && !end_of_comment) {
                        nextLine = nextLine.trim();
                        if (nextLine.startsWith("* Entry:")) {
                            nextLine = reader.readLine();
                            continue;
                        }
                        if (nextLine.startsWith("*") && !nextLine.startsWith("*/")) {
                            comment.append(nextLine.substring(nextLine.indexOf("*") + 1));
                        } else {
                            if (nextLine.indexOf("*/") != -1)
                                comment.append(nextLine.substring(0, nextLine.indexOf("*/")));
                            else
                                comment.append(nextLine);
                        }
                        if (nextLine.indexOf("*/") != -1) {
                            // System.out.println("Setting end of comment
                            // marker");
                            end_of_comment = true;
                        } else {
                            nextLine = reader.readLine();
                        }
                    }
                }
                currentComment = comment;
                // System.out.println("Comment is: " + comment.toString());
            }
            // Locate assignments
            if (line.indexOf("==") == -1 && line.indexOf("=") != -1 && !(line.startsWith("/*") || line.startsWith("//"))) {
                // System.out.println("Found an assignment in: " + line);
                inassignment = true;
                String variableSide = line.substring(0, line.indexOf("=")).trim();
                String[] variableMods = variableSide.split(" ");
                String variable = variableMods[variableMods.length - 1];
                boolean isStatic = false;
                if (variableMods.length > 1)
                    isStatic = (variableSide.indexOf("static") != -1);
                StringBuffer assignee = new StringBuffer(line.substring(line.indexOf("=") + 1).trim());
                boolean embeddedcomments = false;
                if (assignee.indexOf(";") == -1) {
                    boolean end_of_assignment = false;
                    String nextLine = reader.readLine();
                    while (nextLine != null && !end_of_assignment) {
                        nextLine = nextLine.trim();
                        if (nextLine.startsWith("/*") && nextLine.endsWith("*/")) {
                            if (!embeddedcomments) {
                                embeddedcomments = true;
                                currentComment.append("\n<ul>\n");
                            }
                            currentComment.append("\t<li>" + nextLine.substring(nextLine.indexOf("/*") + 2, nextLine.indexOf("*/")) + "</li>\n");
                        } else
                            assignee.append(nextLine + "\n");
                        if (nextLine.indexOf(";") != -1) {
                            end_of_assignment = true;
                        } else {
                            nextLine = reader.readLine();
                        }
                    }
                }
                currentvariable = variable;
                currentExpression = assignee.toString();
                // System.out.println("\tVariable: " + variable);
                // System.out.println("\tAssignee:" + assignee);
                if (embeddedcomments)
                    currentComment.append("</ul>\n");
                currentEntry = new ConfigurationEntry(isStatic, currentComment.toString(), currentvariable, currentExpression);
                currentComment = new StringBuffer();
                currentComponent.addEntry(currentEntry);
            }
            line = reader.readLine();
        }
    }

    public static void main(String[] args) {
        ConfigurationLoader h = new ConfigurationLoader("d:\\development\\jini2_0_1\\standard\\config\\group\\start-shared-services.config");
        try {
            h.parse();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
