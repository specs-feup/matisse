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

package org.specs.MatlabIR.Processor;

import org.specs.MatlabIR.StatementData;

/**
 * Thrown to indicate that an error occurred while manipulating the MatLab tree representation.
 * 
 * @author Joao Bispo
 * 
 */
public class TreeTransformException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final StatementData data;

    public TreeTransformException(StatementData data) {
	super();
	this.data = data;
    }

    /**
     * 
     */
    public TreeTransformException() {
	this((StatementData) null);
    }

    /**
     * 
     */
    public TreeTransformException(String message) {
	this(message, null);
    }

    public TreeTransformException(String message, StatementData data) {
	super(message);
	this.data = data;
    }

    /**
     * @return the data
     */
    public StatementData getStatementData() {
	return data;
    }

}
