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

package org.specs.MatlabIR.MatlabNode.nodes.core;

import java.util.Collection;

import org.specs.MatlabIR.MatlabLanguage.ClassWord;
import org.specs.MatlabIR.MatlabNode.MatlabNode;

/**
 * Identifier in MatLab related to the class syntax.
 * 
 * <p>
 * The content is a String.
 * 
 * @author JoaoBispo
 *
 */
public class ClassWordNode extends MatlabNode {

    private final ClassWord classWord;

    ClassWordNode(ClassWord classWord) {
	// super(classWord.getLiteral(), Collections.emptyList());

	this.classWord = classWord;
    }

    ClassWordNode(Object content, Collection<MatlabNode> children) {
	this(ClassWord.getClassWord((String) content));
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new ClassWordNode(getWord());
    }

    public ClassWord getWord() {
	return classWord;
    }

    @Override
    public String getCode() {
	return classWord.getLiteral();
    }
}
