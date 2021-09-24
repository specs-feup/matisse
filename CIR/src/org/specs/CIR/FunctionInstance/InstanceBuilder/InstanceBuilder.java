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

package org.specs.CIR.FunctionInstance.InstanceBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Tree.CNodes.StringNode;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIRFunctions.CLibrary.CFunctions;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.CIRTypes.Types.String.StringType;
import org.suikasoft.jOptions.Interfaces.DataStore;

/**
 * Interface for classes that builds instances, contains several helper methods useful for Instance building. One
 * InstanceBuilder class should correspond to a single function.
 * 
 * <p>
 * An instance of an InstanceBuilder receives a ProviderData object with input type information, and is already
 * specialized for a specific FunctionInstance. Implementations of this class should stay mostly behind-the-scenes,
 * their purpose is to do the heavy-work needed when creating a FunctionInstance.
 * 
 * @author Joao Bispo
 * @see AInstanceBuilder
 * 
 */
public interface InstanceBuilder {

    DataStore getSettings();

    NumericFactory getNumerics();

    ProviderData getData();

    CFunctions getFunctions();

    /**
     * Creates the FunctionInstance of this Builder.
     * 
     * @return
     */
    public FunctionInstance create();

    /**
     * Creates a FunctionCall token which this operator for the given arguments.
     * 
     * <p>
     * Creates an implementation with 'invertArgs' and 'propagateConstants' set to false.
     * 
     * <p>
     * Creates a provider with a function call level incremented by one
     * 
     * @param arguments
     * @return
     */
    default FunctionCallNode getFunctionCall(InstanceProvider provider, List<CNode> arguments) {
        return FunctionInstanceUtils.getFunctionCall(provider, getData(), arguments);
    }

    /**
     * Helper method with variadic inputs and constant propagation turned off.
     * 
     * @param arguments
     * @return
     */
    default FunctionCallNode getFunctionCall(InstanceProvider provider, CNode... arguments) {
        return getFunctionCall(provider, Arrays.asList(arguments));
    }

    /**
     * Helper method with variadic inputs.
     * 
     * @param provider
     * @param inputTypes
     * @return
     */
    default FunctionInstance getInstance(InstanceProvider provider, VariableType... inputTypes) {
        return getInstance(provider, Arrays.asList(inputTypes));
    }

    default FunctionInstance getInstance(InstanceProvider provider, List<VariableType> inputTypes) {
        // Create new ProviderData
        ProviderData newData = ProviderData.newInstance(inputTypes, getSettings());

        // Get implementation
        FunctionInstance instance = provider.newCInstance(newData);

        if (instance == null) {
            throw new RuntimeException("Input types not supported for provider '" + provider + "':"
                    + newData.getInputTypes());
        }

        return instance;
    }

    /**
     * Creates a constant string node with the given value.
     * 
     * @param string
     * @return
     */
    default StringNode newString(String string) {
        return CNodeFactory.newString(string, getNumerics().newChar().getBits());
    }

    /**
     * Creates a string type without value.
     * 
     * @param string
     * @return
     */
    default StringType newStringType(boolean isConstant) {
        return StringType.create(null, getNumerics().newChar().getBits(), true);
    }

    /**
     * Infers the type of an expression with scalars, from the types of the inputs, and the type we might want as
     * output.
     * 
     * <p>
     * TODO: These rules should be studied with more care
     * 
     * @param inputTypes
     * @param outputType
     * @return
     */
    default <T extends ScalarType, U extends ScalarType> ScalarType getInferredType(List<T> inputTypes,
            Optional<U> outputType) {

        List<ScalarType> possibleTypes = new ArrayList<>(inputTypes);

        // If final output type is defined, add it to types to test
        if (outputType.isPresent()) {
            possibleTypes.add(outputType.get());
        }

        List<ScalarType> validTypes = new ArrayList<>(InstanceBuilderUtils.getValidTypes(possibleTypes));

        // Choose as type of the expression the type which better fits the valid types
        ScalarType maxType = ScalarUtils.getMaxRank(validTypes);

        return maxType;
    }

    /**
     * A type is weak for inference if it is a weak type and an integer.
     * 
     * @param types
     * @return true if all types are considered weak for inference
     */
    default <T extends ScalarType> boolean isInferenceWeak(List<T> types) {

        for (ScalarType type : types) {
            if (!type.isWeakType()) {
                return false;
            }

            // Ignore if weak type is integer
            if (!type.scalar().isInteger()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Creates a shape from a list of scalar types.
     * 
     * @param scalars
     * @return
     */
    default TypeShape getShape(List<ScalarType> scalars) {

        List<Integer> indexes = scalars.stream()
                // For each scalar, if it has a constant value, get the string value. Otherwise, use -1 (undefined
                // dimension)
                // .map(scalar -> scalar.scalar().hasConstant() ? scalar.scalar().getConstantString() : "-1")
                .map(scalar -> scalar.scalar().hasConstant() ? scalar.scalar().getConstant().intValue() : -1)
                .collect(Collectors.toList());

        return TypeShape.newInstance(indexes);
    }

    default List<String> createNameList(String prefix, int numArgs) {
        return FunctionInstanceUtils.createNameList(prefix, numArgs);
    }

}
