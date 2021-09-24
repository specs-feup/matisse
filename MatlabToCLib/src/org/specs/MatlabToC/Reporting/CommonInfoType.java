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

package org.specs.MatlabToC.Reporting;

import pt.up.fe.specs.util.reporting.MessageType;
import pt.up.fe.specs.util.reporting.ReportCategory;
import pt.up.fe.specs.util.reporting.Reporter;

/**
 * Specifies common information types.
 * 
 * @author Luís Reis
 * @see Reporter
 */
public enum CommonInfoType implements MessageType {
    PROBE("MATISSE Probe");

    private final String name;

    private CommonInfoType(String name) {
	this.name = name;
    }

    @Override
    public String toString() {
	return name;
    }

    @Override
    public ReportCategory getMessageCategory() {
	return ReportCategory.INFORMATION;
    }
}
