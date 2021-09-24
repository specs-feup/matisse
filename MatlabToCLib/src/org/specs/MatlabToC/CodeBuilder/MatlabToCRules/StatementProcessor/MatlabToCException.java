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

package org.specs.MatlabToC.CodeBuilder.MatlabToCRules.StatementProcessor;

import org.specs.MatlabToC.CodeBuilder.MatlabToCFunctionData;

/**
 * Thrown to indicate that an error occurred while transforming MatLab into C.
 * 
 * @author Joao Bispo
 * 
 */
public class MatlabToCException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 30123031201230L;

    transient private MatlabToCFunctionData data;

    /**
     * 
     */
    public MatlabToCException() {
	super();
    }

    /**
     * 
     */
    public MatlabToCException(String message) {
	this(message, null, null);
    }

    public MatlabToCException(String message, MatlabToCFunctionData data) {
	this(message, null, data);
    }

    public MatlabToCException(String message, Throwable cause, MatlabToCFunctionData data) {
	super(message, cause);
	this.data = data;
	// if (cause != null) {
	// this.setStackTrace(cause.getStackTrace());
	// }
	// System.out.println("Message:" + message);
    }

    /* (non-Javadoc)
    * @see java.lang.Throwable#toString()
    */
    /*
    @Override
    public String toString() {
    if (data == null) {
        return super.toString();
    }

    return data.getErrorMessage() + "\n" + getMessage();
    }
    */

    @Override
    public String getMessage() {
	if (data == null) {
	    return super.getMessage();
	}

	return data.getErrorMessage() + "\n" + super.getMessage();
    }
}
