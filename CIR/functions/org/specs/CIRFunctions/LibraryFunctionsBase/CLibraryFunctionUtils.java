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

package org.specs.CIRFunctions.LibraryFunctionsBase;

import java.util.Collections;
import java.util.List;

import org.specs.CIR.CodeGenerator.CodeGeneratorUtils;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.TypesOld.TypeVerification;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * Utility methods for the interface CLibraryFunction.
 * 
 * @author Joao Bispo
 * 
 */
public class CLibraryFunctionUtils {

    /**
     * Creates an instance for a function that is already implemented in a C library.
     * 
     * @param function
     * @param data
     * @return
     */
    public static FunctionInstance newInstance(final CLibraryFunction function, ProviderData data) {
        // final NumericFactoryG numerics = new NumericFactoryG(data.getSetup().getCBitSizes());
        final NumericFactory numerics = data.getNumerics();

        // Get FunctionTypes
        final FunctionType functionTypes = newFunctionTypes(function, numerics);

        // return new CLibraryFunctionInstance(functionTypes, function);

        final String functionName = function.getFunctionName();
        // InlineCode inlineCode = getInlineCode(function, functionTypes, functionName);
        InlineCode inlineCode = new InlineCode() {

            @Override
            public String getInlineCode(List<CNode> arguments) {
                return getCode(function, functionTypes, functionName, arguments, numerics);
            }
        };

        InlinedInstance clibInstance = new InlinedInstance(functionTypes, functionName, inlineCode);
        clibInstance.setCallPrecedenceLevel(PrecedenceLevel.FunctionCall);

        String selfInclude = function.getLibrary().getIncludeName();
        clibInstance.setSelfInclude(selfInclude);

        // Disable checkCallInputs if function as variadic input
        if (function.getInputTypes(numerics) == null) {
            clibInstance.setCheckCallInputs(false);
        }

        return clibInstance;
    }

    /**
     * @param function
     * @param functionTypes
     * @param functionName
     * @param numerics
     * @return
     */
    private static String getCode(CLibraryFunction function, FunctionType functionTypes, String functionName,
            List<CNode> arguments, NumericFactory numerics) {

        List<VariableType> functionInputTypes = functionTypes.getCInputTypes();

        // Input types of CLibrary functions can be null, if the function has
        // variadic inputs (e.g., printf).
        // If so, create a list of input types equal to the arguments
        if (function.getInputTypes(numerics) == null) {
            functionInputTypes = SpecsFactory.newArrayList();
            for (CNode arg : arguments) {
                functionInputTypes.add(arg.getVariableType());
            }
        }

        return CodeGeneratorUtils.functionCallCode(functionName, functionInputTypes,
                arguments);
    }

    private static FunctionType newFunctionTypes(CLibraryFunction function, NumericFactory numerics) {
        List<VariableType> functionInputTypes = function.getInputTypes(numerics);

        if (functionInputTypes == null) {
            functionInputTypes = Collections.emptyList();
        }

        FunctionTypeBuilder builder = FunctionTypeBuilder.newInline()
                .addInputs(functionInputTypes)
                .returning(function.getOutputType(numerics));

        builder.withSideEffectsIf(function.hasSideEffects());

        return builder.build();
    }

    /**
     * @param constants
     * @return
     */
    public static FunctionInstance newInstance(CLibraryConstant constant) {

        List<VariableType> input = SpecsFactory.newArrayList();
        VariableType output = constant.getConstantType();
        FunctionType functionTypes = FunctionType.newInstanceNotImplementable(input, output);

        InlineCode code = FunctionInstanceUtils.newInlineCodeLiteral(constant.getName());

        InlinedInstance instance = new InlinedInstance(functionTypes, constant.getName(), code);

        instance.setCustomCallIncludes(constant.getLibrary().getIncludeName());

        return instance;
    }

    /**
     * Returns true if the types of the arguments are compatible with the types needed to implement this function.
     * 
     * <p>
     * - If the CLibraryFunction does not define inputs (i.e., is null), always returns true; <br>
     * - If the number of function types and argument types do not agree, returns false;
     * 
     * @param argumentTypes
     * @return
     */
    public static boolean checkArgumentTypes(CLibraryFunction function, ProviderData data) {

        // Get input types
        List<VariableType> argumentTypes = data.getInputTypes();
        NumericFactory numerics = data.getNumerics();

        // If no inputs are defined, return true
        if (function.getInputTypes(numerics) == null) {
            return true;
        }

        // Check input types
        if (!TypeVerification.areTypesFunctionAssignable(function.getInputTypes(numerics), argumentTypes)) {
            return false;
        }

        return true;
    }
}
