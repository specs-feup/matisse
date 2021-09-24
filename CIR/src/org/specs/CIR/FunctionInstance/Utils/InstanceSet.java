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

package org.specs.CIR.FunctionInstance.Utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;

import com.google.common.collect.Sets;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * Holds a set of FunctionInstance objects.
 * 
 * @author Joao Bispo
 *
 */
public class InstanceSet {

    private final Set<FunctionInstance> instances;

    public InstanceSet() {
	instances = Sets.newHashSet();
    }

    /**
     * Helper method accepting CTokens representing functions and variadic inputs.
     * 
     * @param dependentInstances
     */
    public void add(CNode... dependentInstances) {
	List<CNode> ctokens = Arrays.asList(dependentInstances);
	add(ctokens);
    }

    /**
     * Helper method accepting CTokens representing functions and variadic inputs.
     * 
     * @param dependentInstances
     */
    public void add(List<CNode> dependentInstances) {
	Set<FunctionInstance> fInstances = SpecsFactory.newHashSet();

	for (CNode instance : dependentInstances) {
	    // .getType() != CNodeType.FunctionCall
	    if (!(instance instanceof FunctionCallNode)) {
		throw new RuntimeException("Input CTokens must be of type FunctionCall:\n" + dependentInstances);
	    }

	    // FunctionInstance fInst = CTokenContent.getFunctionInstance(instance);
	    // fInstances.add(fInst);
	    fInstances.add(((FunctionCallNode) instance).getFunctionInstance());
	}

	add(fInstances);
    }

    /**
     * Helper method with variadic inputs.
     * 
     * @param dependentInstances
     */
    public void add(FunctionInstance... dependentInstances) {
	Set<FunctionInstance> instances = SpecsFactory.newHashSet();
	for (FunctionInstance instance : dependentInstances) {
	    instances.add(instance);
	}

	add(instances);
    }

    /**
     * @param dependentInstances
     *            the dependentInstances to set
     */
    public void add(Set<FunctionInstance> dependentInstances) {
	// FactoryUtils.addAll(instances, dependentInstances);
	// FactoryUtils.addAll(instances, dependentInstances);
	instances.addAll(dependentInstances);
    }

    /**
     * @return the instances. It is never null.
     */
    public Set<FunctionInstance> getInstances() {
	return Collections.unmodifiableSet(instances);
    }

}
