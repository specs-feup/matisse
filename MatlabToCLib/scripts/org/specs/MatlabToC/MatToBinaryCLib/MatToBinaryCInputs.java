/**
 * Copyright 2014 SPeCS.
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

package org.specs.MatlabToC.MatToBinaryCLib;

import java.io.File;
import java.util.List;

import com.jmatio.types.MLArray;

/**
 * @author Joao Bispo
 *
 */
public class MatToBinaryCInputs {

    private final List<MLArray> variables;
    private final File outputFolder;

    public MatToBinaryCInputs(List<MLArray> variables, File outputFolder) {
	this.variables = variables;
	this.outputFolder = outputFolder;
    }

    /**
     * @return the variables
     */
    public List<MLArray> getVariables() {
	return variables;
    }

    /**
     * @return the outputFolder
     */
    public File getOutputFolder() {
	return outputFolder;
    }

}
