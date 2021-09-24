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

package org.specs.MatlabIR.MatlabNodePass.APasses;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNodePass.MatlabPassUtils;
import org.specs.MatlabIR.MatlabNodePass.interfaces.MatlabNodePass;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * A MatlabNodePass which is applied on the given node and its children recursively, bottom-up.
 * 
 * @author JoaoBispo
 *
 */
public interface BottomUpPass extends MatlabNodePass {

    @Override
    default MatlabNode apply(MatlabNode node, DataStore data) {
	return MatlabPassUtils.bottomUpTraversal(node, (n, d) -> applySingle(n, d), data);
    }

    /**
     * Applies the rule to a single node.
     * 
     * @param node
     * @param data
     * @return
     */
    MatlabNode applySingle(MatlabNode node, DataStore data);
}
