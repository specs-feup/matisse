/**
 * Copyright 2012 SPeCS Research Group.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * Add a comment to this line http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

package org.specs.MatlabIR;

import java.io.File;

/**
 * @author Joao Bispo
 * 
 */
public class MatlabCodeUtils {

    /**
     * <p>
     * E.g.: addpath 'C:\Users\Joao Bispo\Work\Code\Outputs\MatlabToCTester\.\builtins' -END;
     * 
     * @param absolutePath
     */
    public static String addPath(File path, String... options) {
	return addPath(path.getAbsolutePath(), options);
    }

    public static String addPath(String path, String... options) {

	StringBuilder builder = new StringBuilder();

	builder.append("addpath(");

	builder.append(makeString(path));

	for (String option : options) {
	    builder.append(" ,");
	    builder.append(makeString(option));
	}

	builder.append(");\n");

	return builder.toString();
    }

    public static String rmPath(File path, String... options) {
	return rmPath(path.getAbsolutePath(), options);
    }

    public static String rmPath(String path, String... options) {

	StringBuilder builder = new StringBuilder();

	builder.append("rmpath(");
	builder.append(makeString(path));

	for (String option : options) {
	    builder.append(" ,");
	    builder.append(makeString(option));
	}

	builder.append(");\n");

	return builder.toString();
    }

    public static String makeString(String baseString) {
	StringBuilder builder = new StringBuilder("'");

	boolean foundSpecialCharacters = false;
	for (int i = 0; i < baseString.length(); ++i) {
	    char ch = baseString.charAt(i);

	    if (ch == '\'') {
		builder.append("''");
		continue;
	    }
	    if (ch >= 32 && ch <= 126) {
		builder.append(ch);
		continue;
	    }

	    foundSpecialCharacters = true;
	    builder.append("' char(");
	    builder.append((int) ch);
	    builder.append(") '");
	}

	builder.append("'");
	if (foundSpecialCharacters) {
	    builder.insert(0, "[");
	    builder.append("]");
	}
	return builder.toString();
    }
}