/**
 * Copyright 2012 SPeCS Research Group.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License. under the License.
 */

package org.specs.MatlabToC.Program;

import org.specs.CIR.Tree.CInstructionList;
import org.specs.Matisse.Matlab.TypesMap;

/**
 * @author Joao Bispo
 * 
 */
public class MatlabToCVectorsOutput {

    public final CInstructionList instructions;
    public final TypesMap localVariables;

    /**
     * @param instructions
     * @param localVariables
     */
    public MatlabToCVectorsOutput(CInstructionList instructions, TypesMap localVariables) {
	this.instructions = instructions;
	this.localVariables = localVariables;
    }

}
