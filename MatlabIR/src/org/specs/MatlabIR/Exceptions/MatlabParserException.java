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

package org.specs.MatlabIR.Exceptions;

import java.util.Optional;

public abstract class MatlabParserException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    protected final Optional<String> line;
    protected final Optional<Integer> lineNumber;

    public MatlabParserException(String message) {
	super(message);

	line = Optional.empty();
	lineNumber = Optional.empty();
    }

    public MatlabParserException(String message, Throwable cause) {
	super(message, cause);

	line = Optional.empty();
	lineNumber = Optional.empty();
    }

    public MatlabParserException(String line, int lineNumber, String message, Throwable cause) {
	super(message, cause);

	this.line = Optional.of(line);
	this.lineNumber = Optional.of(lineNumber);
    }

    public MatlabParserException(String line, int lineNumber, String message) {
	super(message);

	this.line = Optional.of(line);
	this.lineNumber = Optional.of(lineNumber);
    }

    public Optional<Integer> getLineNumber() {
	return lineNumber;
    }

    public Optional<String> getLine() {
	return line;
    }

}