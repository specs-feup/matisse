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

package org.specs.MatlabProcessor.MatlabParser.Rules;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.MatlabNodeIterator;

public interface IteratorTransform<T extends MatlabNode> {

    /**
     * Applies a transformation over the iterator.
     * <p>
     * 
     * All modifications in the current tree level have to be done using the given iterator.
     * 
     * @param previousNode
     *            the last node returned by the iterator
     * @param iterator
     *            an iterator with the cursor just after the given previous node
     */
    void apply(T previousNode, MatlabNodeIterator iterator);

    @SuppressWarnings("unchecked")
    default void applyUnsafe(MatlabNode previousNode, MatlabNodeIterator iterator) {
	apply((T) previousNode, iterator);
    }
}
