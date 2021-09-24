/**
 * Copyright 2013 SPeCS Research Group.
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

package org.suikasoft.CMainFunction.Tester;

import java.util.List;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.utilities.AverageType;

/**
 * @author Joao Bispo
 * 
 */
public class ExecutionResults {

    private final String programName;
    private final List<Number> runtimes;
    private final AverageType type;

    public ExecutionResults(String programName, List<? extends Number> runtimes, AverageType type) {
	this.programName = programName;
	this.runtimes = SpecsFactory.newArrayList(runtimes);
	this.type = type;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	double averageExecution = type.calcAverage(runtimes);

	StringBuilder builder = new StringBuilder();

	builder.append("Average time: " + KernelUtils.getFormat().format(averageExecution) + "\n");
	// builder.append("Average time: " + averageExecution + "\n");
	builder.append("Average type: " + type.toString() + "\n");

	return builder.toString();
    }

    /**
     * @return the runtimes
     */
    public List<Number> getRuntimes() {
	return runtimes;
    }

    /**
     * @return the programName
     */
    public String getProgramName() {
	return programName;
    }
}
