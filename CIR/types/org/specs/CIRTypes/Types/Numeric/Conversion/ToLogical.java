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

package org.specs.CIRTypes.Types.Numeric.Conversion;

import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.Views.Conversion.ConversionRule;

/**
 * When target is a stdint, builds a cast to that type.
 * 
 * @author Joao Bispo
 * 
 */
public class ToLogical implements ConversionRule {

    /* (non-Javadoc)
     * @see org.specs.CIRv2.Types.Views.Conversion.ConversionRule#convert(org.specs.CIR.Tree.CToken, org.specs.CIR.Types.VariableType)
     */
    @Override
    public CNode convert(CNode token, VariableType targetType) {
        /*
        // Target type is a LogicalType
        LogicalType logicalType = (LogicalType) targetType;
        
        // Assume there is a conversion from Numeric to the underlying type of LogicalType
        return token.getVariableType().conversion().to(token, logicalType.getImplementation());
        */
        // Just return the node, for now
        return token;

    }

}
