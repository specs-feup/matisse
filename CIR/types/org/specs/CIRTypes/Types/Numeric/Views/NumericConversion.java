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

package org.specs.CIRTypes.Types.Numeric.Views;

import org.specs.CIR.Types.ATypes.Scalar.ScalarConversion;
import org.specs.CIR.Types.Views.Conversion.ConversionRules.ConversionMap;
import org.specs.CIRTypes.Types.Logical.LogicalType;
import org.specs.CIRTypes.Types.Numeric.NumericTypeV2;
import org.specs.CIRTypes.Types.Numeric.Conversion.ToLogical;
import org.specs.CIRTypes.Types.Numeric.Conversion.ToNumeric;
import org.specs.CIRTypes.Types.Numeric.Conversion.ToStdInt;
import org.specs.CIRTypes.Types.StdInd.StdIntType;

/**
 * 
 * @author Joao Bispo
 * 
 */
public class NumericConversion extends ScalarConversion {

    // private static final Map<Class<? extends VariableType>, ConversionRule> TO_RULES;
    private static final ConversionMap TO_RULES;
    static {
        // Create 'to' rules
        TO_RULES = new ConversionMap();
        // Populate table
        TO_RULES.put(NumericTypeV2.class, new ToNumeric());
        TO_RULES.put(StdIntType.class, new ToStdInt());
        TO_RULES.put(LogicalType.class, new ToLogical());

        // RULES = new ConversionRules(toRules, null);
    }

    // private final NumericTypeV2 type;

    public NumericConversion(NumericTypeV2 type) {
        super(type, TO_RULES, null);
        // this.type = type;
    }

    /*
    @Override
    protected ConversionRules getRules() {
    return RULES;
    }
    */

}
