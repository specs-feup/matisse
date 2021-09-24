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

package org.specs.MatlabIR.MatlabNodePass.interfaces;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * Represents a transformation that is to be applied only on the given MatlabNode.
 * 
 * @author JoaoBispo
 *
 */
@FunctionalInterface
public interface MatlabNodePass {

    /**
     * Applies the transformation over the rootNode and its children, storing and using information in the given data
     * instance.
     * 
     * @param node
     * @param data
     * @return the rootNode, or a new rootNode if the original was modified
     */
    MatlabNode apply(MatlabNode node, DataStore data);

    /**
     * The name of the pass. As default, returns the full name of the class.
     * 
     * @return
     */
    default String getName() {
	return this.getClass().getName();
    }

}
