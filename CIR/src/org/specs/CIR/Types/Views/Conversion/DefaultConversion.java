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

import org.specs.CIR.Types.VariableType;

/**
 * Default implementation of the Conversion class. Does not have any conversion rules.
 * 
 * @author Joao Bispo
 * 
 */
public class DefaultConversion extends AConversion {

    // private static final ConversionRules RULES = new ConversionRules(null, null);

    public DefaultConversion(VariableType selfType) {
	super(selfType);
    }

    /*
        @Override
        public VariableType toScalarType() {
    	return null;
        }
        */
    /*
        @Override
        protected ConversionRules getRules() {
    	return RULES;
        }
        */
}
