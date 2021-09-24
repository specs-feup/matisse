/**
 * Copyright 2015 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

package org.specs.MatlabIR.Utilities;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.providers.ResourceProvider;
import pt.up.fe.specs.util.utilities.LineStream;

/**
 * Cleans javadoc of MatlabNode classes.
 * 
 * @author JoaoBispo
 *
 */
public class MatlabIRDoc {
    private static String JAVADOC_BEGIN = "JAVADOC_BEGIN:";
    private static String JAVADOC_END = "JAVADOC_END";
    private static String RESOURCE_CORE = "doc/core.txt";
    private static String RESOURCE_ROOT = "doc/root.txt";
    private static String RESOURCE_STATEMENTS = "doc/statements.txt";

    public static void main(String[] args) {

	Map<String, String> nodeDocs = getDocs(() -> MatlabIRDoc.RESOURCE_ROOT);
	nodeDocs.putAll(getDocs(() -> MatlabIRDoc.RESOURCE_CORE));

	Map<String, String> statementDocs = getDocs(() -> MatlabIRDoc.RESOURCE_STATEMENTS);
	String html = generateHtml(nodeDocs, statementDocs);

	SpecsIo.write(new File("MatlabNodes.html"), html);

    }

    private static Map<String, String> getDocs(ResourceProvider resource) {
	Map<String, String> nodeDocs = new LinkedHashMap<>();

	try (LineStream lines = LineStream.newInstance(resource)) {
	    StringBuilder currentBuffer = null;
	    String currentClass = null;

	    for (String line : lines.getIterable()) {
		if (line.startsWith(MatlabIRDoc.JAVADOC_BEGIN)) {
		    // Clear buffer
		    currentBuffer = new StringBuilder();
		    currentClass = line.substring(MatlabIRDoc.JAVADOC_BEGIN.length());

		    // Clean class name
		    if (currentClass.endsWith("Node")) {
			currentClass = currentClass.substring(0, currentClass.length() - "Node".length());
		    }

		    if (currentClass.endsWith("St")) {
			currentClass = currentClass.substring(0, currentClass.length() - "St".length());
		    }

		    continue;
		}

		if (line.startsWith(MatlabIRDoc.JAVADOC_END)) {
		    assert currentBuffer != null;
		    assert currentClass != null;

		    // Ignore MType
		    if (currentClass.equals("MType") || currentClass.equals("MStatementType")) {
			continue;
		    }

		    // Save doc
		    nodeDocs.put(currentClass, currentBuffer.toString());

		    currentBuffer = null;
		    currentClass = null;
		    continue;
		}

		if (currentBuffer != null) {
		    line = line.trim();

		    // Ignore @author lines
		    if (line.startsWith("@author")) {
			continue;
		    }

		    currentBuffer.append(line.trim() + "\n");
		}

	    }
	}
	return nodeDocs;
    }

    private static String generateHtml(Map<String, String> nodeDocs, Map<String, String> statementDocs) {
	StringBuilder builder = new StringBuilder();

	builder.append("\n<h1>MATLAB Nodes</h1>\n");
	for (Entry<String, String> entry : nodeDocs.entrySet()) {
	    builder.append("<h2>" + entry.getKey() + "</h2>\n");
	    builder.append("<p>\n");
	    builder.append(entry.getValue());
	}

	builder.append("\n<h1>Statement Types</h1>\n");
	for (Entry<String, String> entry : statementDocs.entrySet()) {
	    builder.append("<h2>" + entry.getKey() + "</h2>\n");
	    builder.append("<p>\n");
	    builder.append(entry.getValue());
	}

	return builder.toString();
    }

}
