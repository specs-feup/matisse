/**
 * Copyright 2012 SPeCS Research Group.
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

package org.specs.MatlabIR.Exceptions;

/**
 * Exception to be used when an error was detected in the code during the parsing.
 * 
 * @author Joao Bispo
 *
 */
public class CodeParsingException extends MatlabParserException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public CodeParsingException(String message) {
	super(message);
    }

    public CodeParsingException(String line, int lineNumber, String message) {
	super(line, lineNumber, "Parsing error on line " + lineNumber + ": " + line + "\n" + message);

    }

    public CodeParsingException(String msg, Throwable cause) {
	super(msg, cause);

    }

    public CodeParsingException(String line, int lineNumber, Throwable cause) {
	super(line, lineNumber, "Parsing error on line " + lineNumber + ": " + line + "\nProblem: "
		+ cause.getMessage(), cause);

    }
}
