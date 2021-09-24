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

import java.util.HashMap;
import java.util.Map;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.MatlabNodeIterator;
import org.specs.MatlabIR.matlabrules.MatlabNodeRule;

/**
 * Maps node classes to transformations.
 * 
 * @author JoaoBispo
 *
 */
public class NodeMapRule implements MatlabNodeRule {

    static class Builder {
	Map<Class<?>, IteratorTransform<?>> transformations;

	public Builder() {
	    transformations = new HashMap<>();
	}

	public <T extends MatlabNode> Builder add(Class<T> aClass, IteratorTransform<T> transform) {
	    transformations.put(aClass, transform);
	    return this;
	}

	public NodeMapRule build() {
	    return new NodeMapRule(transformations);
	}
    }

    private final Map<Class<?>, IteratorTransform<?>> transformations;

    private NodeMapRule(Map<Class<?>, IteratorTransform<?>> transformations) {
	this.transformations = new HashMap<>(transformations);
    }

    @Override
    public MatlabNode apply(MatlabNode token) {
	// Do nothing if there are not children
	if (!token.hasChildren()) {
	    return token;
	}

	// Iterate over children
	MatlabNodeIterator iterator = token.getChildrenIterator();
	while (iterator.hasNext()) {
	    MatlabNode node = iterator.next();

	    // Check if there is a transformation for this node
	    IteratorTransform<?> transform = transformations.get(node.getClass());

	    // If no transform, continue
	    if (transform == null) {
		continue;
	    }

	    // Apply transformation
	    transform.applyUnsafe(node, iterator);
	}

	return token;
    }

}
