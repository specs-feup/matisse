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

package org.specs.MatlabIR;

/**
 * @author Joao Bispo
 * 
 */
public class StatementData {

    // private final int lineNumber;
    private final int lineNumber;
    private final boolean displayResults;

    /**
     * @param lineNumber
     * @param displayResults
     */
    public StatementData(int lineNumber, boolean displayResults) {
	this.lineNumber = lineNumber;
	this.displayResults = displayResults;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	String display = "";
	if (!displayResults) {
	    display = ";";
	}

	// return statementType + " (line " + getLine() + display + ")";
	return "line " + getLine() + display;
    }

    /**
     * If false, it means that the statement should have a ';' as suffix.
     * 
     * @return the displayResults
     */
    public boolean isDisplay() {
	return displayResults;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + (displayResults ? 1231 : 1237);
	result = prime * result + getLine();
	return result;
    }

    @Override
    public boolean equals(Object obj) {

	if (this == obj) {
	    return true;
	}

	if (obj == null) {
	    return false;
	}

	if (getClass() != obj.getClass()) {
	    return false;
	}

	StatementData other = (StatementData) obj;
	if (displayResults != other.displayResults) {
	    return false;
	}

	if (getLine() != other.getLine()) {
	    return false;
	}

	return true;

    }

    /**
     * @return the lineNumber
     */
    public int getLine() {
	return lineNumber;
    }

    /**
     * @param lineNumber
     *            the lineNumber to set
     */
    public StatementData setLineNumber(int lineNumber) {
	return new StatementData(lineNumber, displayResults);
    }

}
