/**
 * Copyright 2013 SPeCS.
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

package org.specs.CIRTypes.Types.Literal;

import org.specs.CIR.Types.AVariableType;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.Views.Code.Code;

/**
 * @author Joao Bispo
 * 
 */
public final class LiteralType extends AVariableType {

    private final String literalType;

    LiteralType(String literalType) {
	this.literalType = literalType;
    }

    public static LiteralType newInstance(String literalType) {
	return new LiteralType(literalType);
    }

    String getLiteralType() {
	return this.literalType;
    }

    @Override
    public Code code() {
	return new LiteralCode(this);
    }

    @Override
    public String getSmallId() {
	return "lit";
    }

    @Override
    public boolean strictEquals(VariableType type) {
	if (type instanceof LiteralType) {
	    return this.literalType.equals(((LiteralType) type).literalType);
	}

	return false;
    }

}
