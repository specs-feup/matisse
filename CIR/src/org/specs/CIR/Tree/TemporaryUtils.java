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

package org.specs.CIR.Tree;

import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.VariableType;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * Utility methods related to the use of temporary variables in the tree.
 * 
 * @author Joao Bispo
 * 
 */
public class TemporaryUtils {

    private static final String TEMP_VAR_NAME = "!temp!";

    /**
     * Builds a list of CTokens representing temporary variables from the given types.
     * 
     * @param tempTypes
     * @return
     */
    private static List<CNode> buildTemporaryTokens(List<VariableType> tempTypes) {

	List<CNode> newOutsAsIns = SpecsFactory.newArrayList();

	// Add tokens
	for (int i = 0; i < tempTypes.size(); i++) {

	    // Get the type of the out-as-in
	    VariableType outputType = tempTypes.get(i);

	    CNode outVar = CNodeFactory.newVariable(TEMP_VAR_NAME, outputType);
	    newOutsAsIns.add(outVar);
	}

	return newOutsAsIns;
    }

    /**
     * If 'functionTypes' contains outputs-as-inputs, gives them temporary names and returns a new list containing the
     * 'originalArguments' plus the outputs-as-inputs.
     * 
     * TODO: Check if it is possible to deprecate this method and use the one with the same name that receives a
     * FunctioCallToken
     * 
     * @param functionTypes
     * @param originalArguments
     * @return
     */
    public static List<CNode> updateInputsWithOutsAsIns(FunctionType functionTypes, List<CNode> originalArguments) {

	// if (TemporaryUtils.hasTemporaryTokens(originalArguments)) {
	// LoggingUtils.msgWarn("HAS TEMPORARY!");
	// return originalArguments;
	// }

	// Get CTokens for temporary outputs as inputs. Normalize them, since they will be declared in the current
	// function
	List<VariableType> outAsInTypes = functionTypes.getOutputAsInputTypesNormalized();
	if (outAsInTypes.isEmpty()) {
	    return originalArguments;
	}

	List<CNode> tempOutAsIns = TemporaryUtils.buildTemporaryTokens(outAsInTypes);

	// Add them to the new arguments
	originalArguments = SpecsFactory.newArrayList(originalArguments);
	originalArguments.addAll(tempOutAsIns);

	return originalArguments;
    }

    /**
     * @param originalArguments
     * @return
     */
    /*
    private static boolean hasTemporaryTokens(List<CToken> originalArguments) {
    // Check last arg
    CToken token = originalArguments.get(originalArguments.size() - 1);
    if (token.getType() != CTokenType.Variable) {
        return false;
    }

    VariableToken varToken = (VariableToken) token;

    if (varToken.getVariable().getName() != TEMP_VAR_NAME) {
        return false;
    }

    return true;
    }
    */
    public static boolean isTemporaryName(String name) {
	return name.equals(TEMP_VAR_NAME);
    }

    /**
     * @return the tempVarName
     */
    public static String getTempVarName() {
	return TEMP_VAR_NAME;
    }

}
