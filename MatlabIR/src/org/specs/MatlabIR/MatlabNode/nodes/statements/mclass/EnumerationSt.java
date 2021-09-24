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

/**
 * The first statement of a properties block in a MATLAB class.
 * <p>
 * The first child is ...
 * 
 * @author JoaoBispo
 *
 */
public class EnumerationSt extends ClassBlockHeaderSt {

    /**
     * TOM Compatibility.
     * 
     * @param data
     * @param children
     */
    public EnumerationSt(StatementData data, Collection<MatlabNode> children) {
	super(data, children);
    }

    public EnumerationSt(int lineNumber, Collection<MatlabNode> children) {
	super(new StatementData(lineNumber, true), children);
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new EnumerationSt(getData(), Collections.emptyList());
    }

    @Override
    public String getStatementCode() {

	if (hasChildren()) {
	    throw new RuntimeException("NOT IMPLEMENTED");
	}

	return "enumeration";
    }
    /*
            @Override
            protected MatlabNode copyPrivate() {
        	return new PropertiesSt(getData(), Collections.emptyList());
            }
    
            public PropertiesSt(int lineNumber, Collection<MatlabNode> children) {
        	super(new StatementData(lineNumber, true, MStatementType.If), children);
            }
        */

}
