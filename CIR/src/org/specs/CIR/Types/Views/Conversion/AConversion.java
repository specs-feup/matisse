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

package org.specs.CIR.Types.Views.Conversion;

import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.Views.Conversion.ConversionRules.ConversionMap;

public abstract class AConversion implements Conversion {

    private final VariableType selfType;
    private final ConversionRules rules;

    // public AConversion(VariableType selfType, Map<Class<? extends VariableType>, ConversionRule> toRules,
    // Map<Class<? extends VariableType>, ConversionRule> toSelfRules) {
    public AConversion(VariableType selfType, ConversionMap toRules, ConversionMap toSelfRules) {

	this.selfType = selfType;
	this.rules = new ConversionRules(toRules, toSelfRules);
    }

    public AConversion(VariableType selfType) {
	this(selfType, null, null);
    }

    @Override
    public CNode to(CNode token, VariableType type) {
	return getRules().convertTo(token, type);
    }

    @Override
    public CNode toSelf(CNode token) {
	return getRules().convertTo(token, selfType);
    }

    @Override
    public boolean isConvertibleTo(VariableType type) {
	return getRules().isConvertibleTo(type);
    }

    @Override
    public boolean isConvertibleToSelf(VariableType type) {
	return getRules().isConvertibleToSelf(type);
    }

    /*
        @Override
        public VariableType toScalarType() {
    	throw new UnsupportedOperationException("No implemented for '" + selfType + "'");
        }
    */
    @Override
    public boolean isAssignable(VariableType targetType) {
	throw new UnsupportedOperationException("At class " + getClass().getName());
    }

    protected ConversionRules getRules() {
	return rules;
    }
}
