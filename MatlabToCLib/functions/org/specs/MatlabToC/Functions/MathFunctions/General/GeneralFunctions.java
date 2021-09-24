/**
 * Copyright 2012 SPeCS Research Group.
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

package org.specs.MatlabToC.Functions.MathFunctions.General;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.Utilities.CirBuilder;
import org.specs.CIRFunctions.LibraryFunctions.CMathFunction;
import org.specs.CIRFunctions.Utilities.UtilityInstances;
import org.specs.MatlabIR.MatlabLanguage.NumericClassName;
import org.specs.MatlabToC.MatlabToCTypesUtils;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * @author Joao Bispo
 * 
 */
public class GeneralFunctions extends CirBuilder {

    public GeneralFunctions(DataStore setup) {
        super(setup);
    }

    public FunctionInstance newCast(VariableType inputType, NumericClassName className) {

        // Build VariableType of output from className
        VariableType outputType = MatlabToCTypesUtils.getVariableType(className, getNumerics());

        // If not casting from a float to an int, just return normal cast
        boolean castingFloatToInt = !ScalarUtils.isInteger(inputType) && ScalarUtils.isInteger(outputType);
        if (!castingFloatToInt) {
            return UtilityInstances.newCastToScalar(inputType, outputType);
        }

        // Otherwise, add call to round

        FunctionType fType = FunctionType.newInstanceNotImplementable(Arrays.asList(inputType), outputType);
        String functionName = "cast_with_rounding_" + inputType.getSmallId() + "_to_" + outputType.getSmallId();

        InlineCode code = args -> {

            CNode input = args.get(0);

            // Add call to round
            CNode roundCall = getFunctionCall(CMathFunction.ROUND, input);

            // Add call to cast
            CNode castCall = getFunctionCall(UtilityInstances.getCastToScalarProvider(outputType), roundCall);

            return castCall.getCode();
        };

        InlinedInstance instance = new InlinedInstance(fType, functionName, code);

        Set<String> includes = new HashSet<>();
        includes.add(SystemInclude.Math.getIncludeName());
        includes.addAll(outputType.code().getIncludes());

        instance.setCustomCallIncludes(includes);

        return instance;
        // Use a round on the input number

        // NumericType outputTypeNumeric = MatlabToCTypes.getNumericType(className);

        // return UtilityInstances.newCast(inputType, outputTypeNumeric);
    }
}
