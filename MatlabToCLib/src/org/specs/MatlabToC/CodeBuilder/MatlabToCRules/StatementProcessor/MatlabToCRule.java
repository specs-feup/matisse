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

import java.util.function.BiFunction;

import org.specs.CIR.Tree.CNode;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabToC.CodeBuilder.MatlabToCFunctionData;

/**
 * Rule to be used when converting MatlabToken objects into CToken objects.
 * 
 * @author Joao Bispo
 * 
 */
public interface MatlabToCRule extends BiFunction<MatlabNode, MatlabToCFunctionData, CNode> {

    /**
     * Converts a MatLab Statement into a CToken instruction, and fills the necessary information in MatlabToCData
     * 
     * @param token
     * @param data
     * @return returns the CNode equivalent to the given MatlabToken, or an empty CToken (CTokenFactory.newEmptyToken())
     *         if no equivalent CToken exists (some rules might change only the given data). If returns null, it means
     *         that the rule cannot be applied to the given statement. The method throws an exception if the rule could
     *         be applied, but an error occurred
     * 
     * 
     */
    @Override
    CNode apply(MatlabNode node, MatlabToCFunctionData data);

}
