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

package org.specs.MatlabProcessor.Reporting;

import pt.up.fe.specs.util.reporting.MessageType;
import pt.up.fe.specs.util.reporting.ReportCategory;

public enum ProcessorErrorType implements MessageType {
    SYNTAX_ERROR("Syntax Error"),
    PARSE_ERROR("Parse Error"),
    OCTAVE_INCOMPATIBILITY("Use of OCTAVE feature");

    private final String messageName;

    private ProcessorErrorType(String messageName) {
	this.messageName = messageName;
    }

    @Override
    public ReportCategory getMessageCategory() {
	return ReportCategory.ERROR;
    }

    @Override
    public String toString() {
	return messageName;
    }

}
