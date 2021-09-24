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

package org.specs.matisselib.services;

import java.util.Optional;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.matisselib.PreTypeInferenceServices;

import com.google.common.base.Preconditions;

/**
 * 
 * @see PreTypeInferenceServices#WIDE_SCOPE
 */
public interface WideScopeService {
    /**
     * Gets the identification of a function
     * 
     * @param context
     *            The context that the function is being retrieved from.
     * @param name
     *            The name of the function that is being called.
     * @return The FunctionIdentification, if any was found, or empty if there is no such function.
     */
    Optional<FunctionIdentification> getUserFunction(FunctionIdentification context, String name);

    default Optional<FunctionIdentification> getUserFunction(String name) {
	Preconditions.checkArgument(name != null);

	return getUserFunction(getCurrentContext(), name);
    }

    /**
     * Gets the current function.
     * 
     * @return The function identification of the current function
     */
    FunctionIdentification getCurrentContext();

    /**
     * Gets the MatlabNode that contains the function definition
     * 
     * @param functionIdentification
     *            The identification of the function
     * @return The function node, or empty if none was found
     */
    Optional<MatlabNode> getFunctionNode(FunctionIdentification functionIdentification);

    /**
     * Gets a new WideScopeService with a different context.
     * 
     * @param functionIdentification
     *            The context of the new scope service.
     * @return The new scope service
     */
    WideScopeService withFunctionIdentification(FunctionIdentification functionIdentification);

}
