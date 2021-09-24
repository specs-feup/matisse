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

package org.specs.MatlabIR.Processor;

import org.specs.MatlabIR.MatlabNode.MatlabNode;

/**
 * Applies a transformation rule over a MatlabToken instance.
 * 
 * <p>
 * Transformations that need to create new nodes, must create them on the children of the root node when using this
 * interface, since it does not allow to return the root node itself.
 * 
 * @author Joao Bispo
 * 
 */
@FunctionalInterface
public interface TreeTransformRule {

    /**
     * 
     * Applies a transformation over a MatlabToken instance.
     * 
     * @param token
     * @return true if the rule changed the tree
     * @throws TreeTransformException
     */
    boolean apply(MatlabNode token) throws TreeTransformException;

}
