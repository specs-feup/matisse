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

package org.specs.MatlabIR.Exceptions;

import java.util.Arrays;
import java.util.Collection;
import java.util.StringJoiner;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNodePass.interfaces.MatlabNodePass;

public class UnsupportedPassTypeException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /*
    public UnsupportedPassTypeException(MatlabNodePass pass, MatlabNode problemNode,
        MatlabNodeType... supportedTypes) {
    
    this(pass, problemNode, Arrays.asList(supportedTypes));
    }
    
    public UnsupportedPassTypeException(MatlabNodePass pass, MatlabNode problemNode,
        Collection<MatlabNodeType> supportedTypes) {
    
    super(buildMessage(pass, problemNode, supportedTypes));
    }
    */
    /*
        private static String buildMessage(MatlabNodePass pass, MatlabNode problemNode,
    	    Collection<MatlabNodeType> supportedTypes) {
    
    	StringBuilder builder = new StringBuilder();
    
    	builder.append("Pass '" + pass.getName() + "' cannot be applied over nodes of type '" + problemNode.getType()
    		+ "'");
    
    	if (!supportedTypes.isEmpty()) {
    	    builder.append(", only ");
    
    	    StringJoiner joiner = new StringJoiner(", ");
    	    supportedTypes.forEach(type -> joiner.add(type.name()));
    	    builder.append(joiner);
    	}
    
    	return builder.toString();
    
        }
    */
    // "Pass '"+getName()+"' cannot be applied over nodes of type '"+rootNode.getType()+"', only Function ");

    public UnsupportedPassTypeException(MatlabNodePass pass, MatlabNode problemNode,
	    Class<?>... supportedTypes) {

	this(pass, problemNode, Arrays.asList(supportedTypes));
    }

    public UnsupportedPassTypeException(MatlabNodePass pass, MatlabNode problemNode,
	    Collection<Class<?>> supportedTypes) {

	super(buildMessage(pass, problemNode, supportedTypes));
    }

    private static String buildMessage(MatlabNodePass pass, MatlabNode problemNode,
	    Collection<Class<?>> supportedTypes) {

	StringBuilder builder = new StringBuilder();

	builder.append("Pass '" + pass.getName() + "' cannot be applied over nodes of type '"
		+ problemNode.getNodeName() + "'");

	if (!supportedTypes.isEmpty()) {
	    builder.append(", only ");

	    StringJoiner joiner = new StringJoiner(", ");
	    supportedTypes.forEach(type -> joiner.add(type.getSimpleName()));
	    builder.append(joiner);
	}

	return builder.toString();

    }
}
