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

package org.specs.MatlabToC.Utilities;

import java.util.Collections;
import java.util.List;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CNode;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;

/**
 * Filters the input arguments of a , which can be set in LiteralFunctionInstance.
 * 
 * @author Joao Bispo
 * @see MatlabInstanceProvider#getInputsFilter()
 * 
 */
public interface InputsFilter {

    /**
     * This method returns a corrected list of arguments when then input types needed to call the function
     * implementation is different from the input types needed to call the function prototype.
     * 
     * <p>
     * This method is used by FunctionPrototype. In most cases you should not need to explicitly call this method in
     * regular code. If you feel that you need to, check if FunctionPrototype.newFunctionCall can solve the problem.
     * 
     * <p>
     * If the implementation uses outs-as-ins, use the method 'TemporaryUtils.buildTemporaryTokens' to add temporary
     * tokens to the modified argument list.
     * 
     * <p>
     * E.g., the zeros function when using declared matrixes specializes for the inputs. The inputs are not needed, and
     * a temporary matrix is added to the inputs (if the temporary matrix is the result of an assignment, the name is
     * automatically resolved later, during assignment transformation).
     * 
     * <p>
     * TODO Remove ProviderData from input?
     * 
     * @param data
     * 
     * @param originalArguments
     * 
     * 
     * @return
     */
    public List<CNode> filterInputArguments(ProviderData data, List<CNode> originalArguments);

    /**
     * Default parser, which returns the input arguments unchanged.
     */
    public static final InputsFilter DEFAULT_FILTER = (data, args) -> args;

    /**
     * Discards all original arguments and returns an empty list.
     */
    public static final InputsFilter EMPTY_FILTER = (data, args) -> Collections.emptyList();

}
