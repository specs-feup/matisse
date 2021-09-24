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
import java.util.Optional;

import org.specs.MatlabIR.MatlabLanguage.MatlabNumber;
import org.specs.MatlabIR.MatlabNode.MatlabNode;

/**
 * Represents a MatLab number.
 * 
 * <p>
 * The content is a String representing the number.
 * 
 * @author JoaoBispo
 *
 */
public class MatlabNumberNode extends MatlabNode {

    private final String numberString;
    private Optional<MatlabNumber> number;

    MatlabNumberNode(String number) {
	numberString = number;
	this.number = Optional.empty();
    }

    MatlabNumberNode(Object content, Collection<MatlabNode> children) {
	this((String) content);
    }

    @Override
    protected MatlabNode copyPrivate() {
	return new MatlabNumberNode(getNumberString());
    }

    public String getNumberString() {
	return numberString;
    }

    public MatlabNumber getNumber() {
	if (!number.isPresent()) {
	    number = Optional.of(MatlabNumber.getMatlabNumber(numberString));
	}

	return number.get();
    }

    @Override
    public String getCode() {
	return getNumberString();
    }
}
