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

package org.specs.MatlabIR.MatlabNode.nodes.statements;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.specs.MatlabIR.StatementData;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.AccessCallNode;

/**
 * A simple function call, or array access. The first child is an AccessCall.
 * 
 * @author JoaoBispo
 *
 */
public class AccessCallSt extends StatementNode {

    AccessCallSt(StatementData data, Collection<MatlabNode> children) {
	super(data, children);
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new AccessCallSt(getData(), Collections.emptyList());
    }

    /*
    public AccessCallSt(int line, boolean display, String name, List<MatlabNode> nameAndParameters) {
    this(new StatementData(line, display, StatementType.AccessCall), nameAndParameters);
    }
    */
    public AccessCallSt(int line, boolean display, List<MatlabNode> children) {
	this(new StatementData(line, display), children);
    }

    public AccessCallSt(int line, boolean display, AccessCallNode accessCall) {
	this(line, display, Arrays.asList(accessCall));
    }

    public AccessCallNode getAccessCall() {
	return getChild(AccessCallNode.class, 0);
    }

    @Override
    public String getStatementCode() {
	// TODO: Example breaks rule:
	// file:/C:/Users/JoaoBispo/Dropbox/Code-Repositories/AMADEUS-Benchs/dft-fft-cos-sin/manual_matlab_range_find/range_find.m

	// return getNode().getCode();
	return getAccessCall().getCode();
    }

}
