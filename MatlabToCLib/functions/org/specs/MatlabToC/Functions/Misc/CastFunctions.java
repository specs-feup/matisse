/**
 * Copyright 2013 SPeCS Research Group.
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

package org.specs.MatlabToC.Functions.Misc;

import java.util.ArrayList;
import java.util.List;

import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.MatlabIR.MatlabLanguage.NumericClassName;
import org.specs.MatlabToC.Functions.MathFunctions.General.GeneralBuilders;
import org.specs.MatlabToC.Functions.MatlabOps.ElementWiseBuilder;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.matisselib.providers.GenericMatlabFunction;
import org.specs.matisselib.providers.MatlabFunction;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * @author Joao Bispo
 * 
 */
public class CastFunctions {

    /**
     * @return
     */
    public static List<MatlabFunction> getCastPrototypes() {

        List<MatlabFunction> castPrototypes = SpecsFactory.newArrayList();

        for (NumericClassName className : NumericClassName.values()) {

            // Get builders
            List<InstanceProvider> builders = newCastBuilder(className);

            // Get name of the function
            String functionName = className.getMatlabString();

            // Build prototype
            MatlabFunction proto = new GenericMatlabFunction(functionName, builders);

            // Add to the list
            castPrototypes.add(proto);
        }

        castPrototypes.add(new GenericMatlabFunction("logical", newLogicalCastBuilder()));

        return castPrototypes;
    }

    private static List<InstanceProvider> newCastBuilder(NumericClassName className) {
        List<InstanceProvider> builders = new ArrayList<>();

        // Builder for scalar inputs
        MatlabInstanceProvider scalarProvider = GeneralBuilders.newCastNumericBuilder(className);
        builders.add(scalarProvider);

        // Builder for matrix inputs (applies element-wise operation)
        builders.add(new ElementWiseBuilder(scalarProvider, 1));

        return builders;
    }

    private static List<InstanceProvider> newLogicalCastBuilder() {
        List<InstanceProvider> builders = new ArrayList<>();

        MatlabInstanceProvider scalarProvider = GeneralBuilders.newCastLogicalBuilder();
        builders.add(scalarProvider);
        builders.add(new ElementWiseBuilder(scalarProvider, 1));

        return builders;
    }
}
