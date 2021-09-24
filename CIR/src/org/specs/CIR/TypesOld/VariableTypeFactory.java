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

package org.specs.CIR.TypesOld;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;
import org.specs.CIRTypes.Types.String.StringType;

/**
 * @author Joao Bispo
 * 
 *         TODO: deprecate this class
 */
public class VariableTypeFactory {

    /**
     * Helper method.
     * 
     * @param varType
     * @param shape
     * @return
     */
    public static VariableType newDeclaredMatrix(VariableType varType, Integer... shape) {
	List<Integer> shapeList = Arrays.asList(shape);
	return StaticMatrixType.newInstance(varType, shapeList);
    }

    /**
     * Creates a new string which uses 8 bits per character.
     * 
     * @param string
     * @param charBits
     * @return
     */
    public static VariableType newString(String string) {
	return StringType.create(string, 8);
    }

}
