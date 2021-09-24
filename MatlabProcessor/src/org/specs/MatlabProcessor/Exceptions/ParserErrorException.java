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

package org.specs.MatlabProcessor.Exceptions;

import org.specs.MatlabIR.Exceptions.MatlabParserException;

/**
 * Exception to be used when there is an internal error in the parser.
 * 
 * @author Joao Bispo
 *
 */
public class ParserErrorException extends MatlabParserException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ParserErrorException(String msg) {
	super(msg);
    }

    public ParserErrorException(String msg, Throwable e) {
	super(msg, e);
    }

    public ParserErrorException(String line, int lineNumber, Throwable cause) {
	super("Error while parsing line " + lineNumber + ": " + line, cause);
    }
}
