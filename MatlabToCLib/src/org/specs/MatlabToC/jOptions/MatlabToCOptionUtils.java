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

package org.specs.MatlabToC.jOptions;

import static org.specs.MatlabToC.Functions.MathFunction.*;
import static org.specs.MatlabToC.Functions.MatlabOp.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.specs.CIR.CirKeys;
import org.specs.CIR.Language.Types.CTypeV2;
import org.specs.CIR.Types.ATypes.Matrix.MatrixFunctionName;
import org.specs.CIR.Utilities.AvoidableFunctionsData;
import org.specs.CIR.Utilities.Inlining.InliningData;
import org.specs.CIRFunctions.LibraryFunctions.CLibraryAvoidable;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.CIRTypes.Types.Numeric.NumericTypeV2;
import org.specs.MatlabToC.MatlabFunction.MatlabFunctionProviderEnum;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.collections.HashSetString;

/**
 * Utility methods related to MatlabToCOptions.
 * 
 * @author Joao Bispo
 * 
 */
public class MatlabToCOptionUtils {

    private static final String SETUP_NAME = "MATISSE Setup";

    private static final List<Class<? extends Enum<?>>> SUPPORTED_INLINES;

    static {
        SUPPORTED_INLINES = SpecsFactory.newArrayList();

        MatlabToCOptionUtils.SUPPORTED_INLINES.add(MatrixFunctionName.class);
        MatlabToCOptionUtils.SUPPORTED_INLINES.add(MatlabInlinable.class);
    }

    private static final List<Class<? extends Enum<?>>> SUPPORTED_AVOIDABLES;

    static {
        SUPPORTED_AVOIDABLES = SpecsFactory.newArrayList();

        MatlabToCOptionUtils.SUPPORTED_AVOIDABLES.add(CLibraryAvoidable.class);
    }

    private static final Set<String> ELEMENT_WISE_FUNCTIONS;

    static {
        ELEMENT_WISE_FUNCTIONS = new HashSet<>();

        MatlabToCOptionUtils.ELEMENT_WISE_FUNCTIONS.addAll(getFunctionNames(ABS, COS, MOD, ROUND, SIN, SQRT));
        MatlabToCOptionUtils.ELEMENT_WISE_FUNCTIONS
                .addAll(getFunctionNames(Addition, ElementWiseAnd, ElementWiseOr, Equal, GreaterThan,
                        GreaterThanOrEqual, LeftDivision, LessThan, LessThanOrEqual, LogicalNegation, Multiplication,
                        NotEqual,
                        Power, RightDivision, Subtraction, UnaryMinus, UnaryPlus));
    }

    private static final Set<String> MATISSE_OPTIMIZATIONS;

    static {
        MATISSE_OPTIMIZATIONS = new HashSet<>();

        for (MatisseOptimization opt : MatisseOptimization.values()) {
            MatlabToCOptionUtils.MATISSE_OPTIMIZATIONS.add(opt.getName());
        }
    }

    private static Set<String> getFunctionNames(MatlabFunctionProviderEnum... functions) {
        Set<String> names = new HashSet<>();

        for (MatlabFunctionProviderEnum function : functions) {
            names.add(function.getName());
        }

        return names;
    }

    /**
     * @return the supportedInlines
     */
    /*
    public static <T extends Enum<T> & FunctionName> List<Class<T>> getSupportedInlines() {
    List<Class<T>> supportedInlines = FactoryUtils.newArrayList();
    
    supportedInlines.add(CirInlinable.class);
    
    return supportedInlines;
    }
    */

    /**
     * Helper method with all inlines active by default.
     * 
     * @return
     */
    public static InliningData newInliningDataDefault() {
        return newInliningDataDefault(true);
    }

    public static InliningData newInliningDataDefault(boolean allActive) {

        Class<?>[] classes = MatlabToCOptionUtils.SUPPORTED_INLINES
                .toArray(new Class<?>[MatlabToCOptionUtils.SUPPORTED_INLINES.size()]);
        return InliningData.newInstanceWithChecks(allActive, classes);
    }

    /**
     * Helper method with all avoids disabled by default.
     * 
     * @return
     */
    public static AvoidableFunctionsData newAvoidStoreDefault() {
        return newAvoidStoreDefault(false);
    }

    public static AvoidableFunctionsData newAvoidStoreDefault(boolean allActive) {

        Class<?>[] classes = MatlabToCOptionUtils.SUPPORTED_AVOIDABLES
                .toArray(new Class<?>[MatlabToCOptionUtils.SUPPORTED_AVOIDABLES.size()]);

        return AvoidableFunctionsData.newInstanceWithChecks(allActive, classes);
    }

    public static DataStore newDefaultSettings() {
        DataStore defaultSetup = DataStore.newInstance(MatlabToCOptionUtils.SETUP_NAME);

        // Default inlines
        defaultSetup.set(CirKeys.INLINE, MatlabToCOptionUtils.newInliningDataDefault());
        // Default real, 64-bit double
        defaultSetup.set(CirKeys.DEFAULT_REAL, NumericTypeV2.newInstance(CTypeV2.DOUBLE, 64));

        // Default element-wise functions
        HashSetString elementWiseFunctions = new HashSetString(MatlabToCOptionUtils.ELEMENT_WISE_FUNCTIONS);
        defaultSetup.set(MatlabToCKeys.ELEMENT_WISE_FUNCTIONS, elementWiseFunctions);

        // Default optimizations
        HashSetString optimizations = new HashSetString(MatlabToCOptionUtils.MATISSE_OPTIMIZATIONS);
        defaultSetup.set(MatlabToCKeys.MATISSE_OPTIMIZATIONS, optimizations);

        // Enable Z3 by default
        defaultSetup.set(MatlabToCKeys.ENABLE_Z3, true);

        return defaultSetup;
    }

    public static NumericFactory newDefaultNumerics() {
        return new NumericFactory(CirKeys.C_BIT_SIZES.getDefault().get());
    }

}
