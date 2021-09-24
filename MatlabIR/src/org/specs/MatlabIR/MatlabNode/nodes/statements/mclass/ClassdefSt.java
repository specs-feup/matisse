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

package org.specs.MatlabIR.MatlabNode.nodes.statements.mclass;

import java.util.Collection;
import java.util.Collections;

import org.specs.MatlabIR.StatementData;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.AttributesNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.BaseClassesNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;

/**
 * General class for block headers in MATLAB syntax for classes.
 * 
 * 
 * @author JoaoBispo
 *
 */
public class ClassdefSt extends StatementNode {

    public ClassdefSt(StatementData data, Collection<MatlabNode> children) {
	super(data, children);
    }

    public ClassdefSt(int lineNumber, Collection<MatlabNode> children) {
	super(new StatementData(lineNumber, true), children);
    }

    /*
        public ClassdefSt(int lineNumber, MatlabNode classname, MatlabNode attributes) {
    	this(lineNumber, Arrays.asList(classname, attributes));
        }
    
        public ClassdefSt(int lineNumber, MatlabNode classname) {
    	this(lineNumber, Arrays.asList(classname));
        }
    */
    @Override
    protected MatlabNode copyPrivate() {
	return new ClassdefSt(getData(), Collections.emptyList());
    }

    public boolean hasAttributes() {
	return getAttributes().getNumChildren() != 0;
    }

    public AttributesNode getAttributes() {
	return (AttributesNode) getChild(0);
    }

    public IdentifierNode getClassNameIdentifier() {
	return (IdentifierNode) getChild(1);
    }

    public String getClassName() {
	return getClassNameIdentifier().getName();
    }

    public BaseClassesNode getBaseClasses() {
	return (BaseClassesNode) getChild(2);
    }

    @Override
    public String getStatementCode() {
	String attributes = "";
	if (hasAttributes()) {
	    attributes = " " + getAttributes().getCode();
	}

	String prefix = "classdef" + attributes + " " + getClassNameIdentifier().getName();

	BaseClassesNode base = getBaseClasses();
	if (base.getNumChildren() != 0) {
	    return prefix + " < " + base.getCode();
	}

	return prefix;
    }

    @Override
    public boolean isBlockIndented() {
	return true;
    }

    @Override
    public boolean isBlockHeader() {
	return true;
    }

}
