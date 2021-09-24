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

package org.specs.CIR.Types.ATypes.Matrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsStrings;

/**
 * CNode factory for Matrix functions.
 * 
 * @author Joao Bispo
 *
 */
public class MatrixNodes {

    private final ProviderData data;

    /**
     * Builds a new matrix nodes.
     * 
     * @param setup
     */
    public MatrixNodes(DataStore setup) {
        data = ProviderData.newInstance(setup);
    }

    /**
     * Helper method, only uses information from Setup.
     * 
     * @param data
     */
    /*
    public MatrixNodes(ProviderData data) {
    this(data.getSetupRaw());
    }
    */

    /**
     * Creates a CNode representing a function call to getDim. Uses zero-based indexing.
     * 
     * @param matrix
     * @param index
     * @return
     */
    public CNode getDim(Variable matrix, int index) {

        MatrixType matrixType = (MatrixType) matrix.getType();

        // Get provider
        InstanceProvider getDim = matrixType.matrix().functions().getDim();

        Variable matrixVar = new Variable(matrix.getName(), matrixType);

        CNode matrixNode = CNodeFactory.newVariable(matrixVar);
        CNode indexNode = CNodeFactory.newCNumber(index);

        // Create ProviderData for FunctionInstance
        ProviderData callData = data.createFromNodes(matrixNode, indexNode);

        return getDim.newCInstance(callData).newFunctionCall(matrixNode, indexNode);
        // return CNodeFactory.newFunctionCall(getDim.newCInstance(callData), matrixNode, indexNode);
    }

    /*
    public CNode create(String name, MatrixType matrixType, int... indexes) {
    List<CNode> indexNodes = new ArrayList<>();
    for (int index : indexes) {
        indexNodes.add(CNodeFactory.newCNumber(index));
    }
    
    return create(name, matrixType, indexNodes);
    }
    */

    // public CNode create(String name, MatrixType matrixType, Variable... variables) {
    public CNode create(Variable matrix, Variable... variables) {
        List<CNode> indexNodes = new ArrayList<>();
        for (Variable var : variables) {
            indexNodes.add(CNodeFactory.newVariable(var));
        }

        return create(matrix, indexNodes);
    }

    private CNode create(Variable matrix, List<CNode> indexNodes) {

        MatrixType matrixType = (MatrixType) matrix.getType();

        // Get provider
        InstanceProvider create = matrixType.matrix().functions().create();

        // Create ProviderData for FunctionInstance, using only the indexNodes
        ProviderData callData = data.createFromNodes(indexNodes);

        // Inputs
        List<CNode> inputs = new ArrayList<>(indexNodes);
        inputs.add(CNodeFactory.newVariable(matrix.getName(), matrixType));

        // Set output type
        callData.setOutputType(Arrays.asList(matrixType.matrix().getElementType()));

        // Create new instance, passing the output as argument
        return create.newCInstance(callData).newFunctionCall(inputs);
    }

    public CNode free(Variable matrix) {
        MatrixType type = (MatrixType) matrix.getType();
        InstanceProvider free = type.matrix().functions().free();
        CNode matrixVar = CNodeFactory.newVariable(matrix);

        // Create ProviderData for FunctionInstance, using only the indexNodes
        ProviderData callData = data.create(type);

        return free.newCInstance(callData).newFunctionCall(matrixVar);

    }

    public CNode numel(Variable matrix) {
        MatrixType type = (MatrixType) matrix.getType();

        InstanceProvider numel = type.matrix().functions().numel();
        CNode matrixVar = CNodeFactory.newVariable(matrix.getName(), type);

        // Create ProviderData for FunctionInstance, using only the indexNodes
        ProviderData callData = data.create(type);

        return numel.newCInstance(callData).newFunctionCall(matrixVar);

    }

    // public CNode get(String matrixName, MatrixType type, String... indexNames) {
    // return get(matrixName, type, Arrays.asList(indexNames));
    // }

    /**
     * Helper method which accepts a matrix Variable and a CNode.
     * 
     * @param matrixVar
     * @param index
     * @return
     */
    public CNode get(Variable matrixVar, CNode index) {
        MatrixType type = (MatrixType) matrixVar.getType();
        InstanceProvider get = type.matrix().functions().get();

        List<CNode> inputs = new ArrayList<>();
        inputs.add(CNodeFactory.newVariable(matrixVar));
        inputs.add(index);

        ProviderData callData = data.createFromNodes(inputs);

        return get.newCInstance(callData).newFunctionCall(inputs);

    }

    /**
     * Helper method which accepts a matrix Variable and variable arguments.
     * 
     * @param matrixVar
     * @param indexNames
     * @return
     */
    public CNode get(Variable matrixVar, String... indexNames) {
        return get(matrixVar, Arrays.asList(indexNames));
    }

    /**
     * Helper method which accepts a matrix Variable.
     * 
     * @param matrixVar
     * @param indexName
     * @return
     */
    /*
    public CNode get(Variable matrixVar, List<String> indexNames) {
    // String matrixName = matrixVar.getName();
    // MatrixType matrixType = MatrixUtilsV2.cast(matrixVar.getType());
    
    return get(matrixName, matrixType, indexNames);
    }
    */

    /**
     * Builds a 'get' call for the given index variables.
     * 
     * <p>
     * If an index name is a number, the function transforms it to a CNumber of type 'int'.
     * 
     * @param matrixName
     * @param type
     * @param indexName
     * @return
     */
    public CNode get(Variable matrixVar, List<String> indexName) {
        // String matrixName = matrixVar2.getName();
        MatrixType type = MatrixUtils.cast(matrixVar.getType());

        InstanceProvider get = type.matrix().functions().get();

        List<CNode> indexVars = getIndexNodes(indexName);

        // indexName.forEach(name -> indexVars.add(CNodeFactory.newVariable(name, intType)));

        List<CNode> inputs = new ArrayList<>();
        inputs.add(CNodeFactory.newVariable(matrixVar));
        inputs.addAll(indexVars);

        ProviderData callData = data.createFromNodes(inputs);

        return get.newCInstance(callData).newFunctionCall(inputs);

    }

    /**
     * Helper method with variable number of arguments.
     * 
     * @param matrixVar
     * @param value
     * @param indexName
     * @return
     */
    public CNode set(Variable matrixVar, CNode value, String... indexNames) {
        return set(matrixVar, value, Arrays.asList(indexNames));
    }

    /**
     * Builds a 'set' call for the given index variables.
     * 
     * <p>
     * If an index name is a number, the function transforms it to a CNumber of type 'int'.
     * 
     * @param matrixName
     * @param type
     * @param indexNames
     * @return
     */
    public CNode set(Variable matrixVar, CNode value, List<String> indexNames) {
        MatrixType type = MatrixUtils.cast(matrixVar.getType());

        InstanceProvider set = type.matrix().functions().set();

        List<CNode> indexVars = getIndexNodes(indexNames);

        List<CNode> inputs = new ArrayList<>();
        inputs.add(CNodeFactory.newVariable(matrixVar));
        inputs.addAll(indexVars);
        inputs.add(value);

        ProviderData callData = data.createFromNodes(inputs);

        return set.newCInstance(callData).newFunctionCall(inputs);
    }

    /**
     * Builds index nodes from the given index names.
     * 
     * <p>
     * If the name starts with a digit, assumes it is an integer. Otherwise, creates a variable of int type.
     * 
     * @param indexName
     * @return
     */
    private List<CNode> getIndexNodes(List<String> indexName) {
        List<CNode> indexVars = new ArrayList<>();
        VariableType intType = data.getNumerics().newInt();

        for (String name : indexName) {
            // Check if name is a number
            if (Character.isDigit(name.charAt(0))) {
                Integer number = SpecsStrings.parseInteger(name);

                // Throw exception if could not parse to an integer
                if (number == null) {
                    throw new RuntimeException("Only supports integer indexes, gave index with value '" + name
                            + "'");
                }

                indexVars.add(CNodeFactory.newCNumber(number));
                continue;
            }

            // If not a number, add as a variable
            indexVars.add(CNodeFactory.newVariable(name, intType));
        }

        return indexVars;
    }

    public CNode numDims(Variable matrix) {
        MatrixType matrixType = (MatrixType) matrix.getType();

        InstanceProvider numDims = matrixType.matrix().functions().numDims();
        CNode matrixVar = CNodeFactory.newVariable(matrix);

        ProviderData callData = data.createFromNodes(matrixVar);

        return numDims.newCInstance(callData).newFunctionCall(matrixVar);
    }

    // public CNode data(String matrixName, MatrixType type) {
    public CNode data(Variable matrix) {
        MatrixType matrixType = (MatrixType) matrix.getType();

        InstanceProvider dataProvider = matrixType.matrix().functions().data();
        CNode matrixVar = CNodeFactory.newVariable(matrix);

        ProviderData callData = data.createFromNodes(matrixVar);

        return dataProvider.newCInstance(callData).newFunctionCall(matrixVar);
    }
}
