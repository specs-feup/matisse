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

package org.specs.MatlabToC.VariableStorage;

import java.util.ArrayList;
import java.util.List;

import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Matrix.Functions.MatrixCopy;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRFunctions.MatrixAlloc.TensorProvider;
import org.specs.CIRFunctions.Utilities.UtilityInstances;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.StdInd.StdIntFactory;
import org.specs.MatlabToC.Utilities.MatisseChecker;

import com.google.common.collect.Lists;

/**
 * Loads binary data, of a given type, into a matrix of a possible different type.
 * 
 * <p>
 * The output type is given by ProviderData.getOutputType.
 * 
 * TODO: Should implement InstanceBuilder and not InstanceProvider.
 * 
 * @author JoaoBispo
 *
 */
// public class LoadMatrixVariable implements InstanceProvider {
public class LoadMatrixVariable extends AInstanceBuilder {

    private final static String INPUT_FILENAME = "varname";
    private final static String INPUT_DIM_PREFIX = "dim_";
    private final static String OUTPUT_MATRIX = "out";

    private final static String VARNAME_LOADMATRIX = "load_matrix";
    // The type of the data in the file. It might be different from the input type
    private final ScalarType originalType;

    private final static MatisseChecker CHECKER = new MatisseChecker().
    // String with the name of the file
            isString(0).
            // Remaining are ints for the dimensions
            range(1).areInteger();

    /**
     * Creates a new instance of LoadMatrixVariable.
     * 
     * @param originalType
     *            the type of the data of the binary file being loaded (which can be different from the output type of
     *            the function)
     */
    public LoadMatrixVariable(ProviderData data, ScalarType originalType) {
        super(data);
        this.originalType = originalType;
    }

    /*
    public static InstanceProvider newInstance(ScalarType originalType) {
    return new GenericInstanceProvider(CHECKER, new LoadMatrixVariable(originalType));
    }
    */

    @Override
    protected MatisseChecker getCheckerPrivate() {
        return getChecker();
    }

    public static MatisseChecker getChecker() {
        return LoadMatrixVariable.CHECKER;
    }

    @Override
    public FunctionInstance create() {

        // Number of dimensions is number of inputs, minus 1 (the file name)
        int numDims = getData().getInputTypes().size() - 1;

        // List<String> inputNames = Lists.newArrayList("varname", "out");

        List<String> dimNames = getDimNames(numDims);

        List<String> inputNames = Lists.newArrayList(LoadMatrixVariable.INPUT_FILENAME);
        inputNames.addAll(dimNames);

        // List<VariableType> inputTypes = data.getInputTypes();

        /*
        if (inputTypes.size() != 2) {
            throw new UnsupportedOperationException("All LoadMatrix calls must have two argument, got "
        	    + inputTypes.size());
        }
        */

        // MatrixType matrixType = (MatrixType) inputTypes.get(1);
        // Get output type
        MatrixType matrixType = MatrixUtils.cast(getData().getOutputType());

        ScalarType outputScalar = matrixType.matrix().getElementType();

        boolean isOriginalTypeDiff = false;
        if (!outputScalar.equals(originalType)) {
            isOriginalTypeDiff = true;

        }

        // numDims is necessary, for instance for dynamic matrices
        // Because the type itself doesn't care about the number of dimensions,
        // but the function does and will cause a compile error in some cases without it.
        String functionName = "load_matrix_variable_" + numDims + "_" + matrixType.getSmallId();
        if (isOriginalTypeDiff) {
            functionName = functionName + "_from_" + originalType.getSmallId();
        }

        // VariableType matrixPointer = PointerUtils.getType(matrixType, true);

        FunctionType functionType = FunctionType.newInstanceWithOutputsAsInputs(inputNames,
                getData().getInputTypes(), LoadMatrixVariable.OUTPUT_MATRIX, matrixType);
        // Arrays.asList(inputTypes.get(0), matrixPointer), null, VoidType.newInstance());

        // InstanceBuilder helper = new GenericInstanceBuilder(data);

        CInstructionList instructions = new CInstructionList();
        Variable varName = new Variable(inputNames.get(0), getData().getInputTypes().get(0));
        // Variable outVar = new Variable(inputNames.get(1), matrixPointer);
        Variable outVar = new Variable(LoadMatrixVariable.OUTPUT_MATRIX, functionType.getOutputAsInputTypes().get(0));

        CNode outvarToken = CNodeFactory.newVariable(outVar);

        // Create variable for loading
        MatrixType loadType = null;
        if (isOriginalTypeDiff) {
            loadType = matrixType.matrix().setElementType(originalType);
        }

        // int numElements = 1;
        if (matrixType instanceof DynamicMatrixType) {
            List<CNode> arguments = Lists.newArrayList();
            /*
            List<Integer> dimensions = matrixType.matrix().getShape().getDims();
            if (dimensions.size() == 0) {
            throw new UnsupportedOperationException("No shape defined.");
            }
            if (dimensions.size() == 1) {
            arguments.add(CNodeFactory.newCNumber(1));
            }
            for (Integer dim : dimensions) {
            if (dim == null) {
                throw new UnsupportedOperationException("Exact shape of matrix must be known to use LoadMatrix.");
            }
            functionName += "_" + dim;
            numElements *= dim;
            arguments.add(CNodeFactory.newCNumber(dim));
            }
            */
            VariableType intType = getNumerics().newInt();
            dimNames.forEach(name -> arguments.add(CNodeFactory.newVariable(name, intType)));
            arguments.add(outvarToken);

            CNode token = getFunctionCall(TensorProvider.NEW_ARRAY, arguments);
            instructions.addInstruction(token);

            // If types are different, add new for load type
            if (isOriginalTypeDiff) {
                List<CNode> loadArgs = arguments.subList(0, arguments.size() - 1);
                loadArgs.add(CNodeFactory.newVariable(LoadMatrixVariable.VARNAME_LOADMATRIX, loadType));
                CNode loadToken = getFunctionCall(TensorProvider.NEW_ARRAY, loadArgs);
                instructions.addInstruction(loadToken);
            }
        }

        CNode varNameToken = CNodeFactory.newVariable(varName);
        // CNode numElementsToken = CNodeFactory.newCNumber(numElements, StdIntFactory.newUInt32());
        CNode numElementsToken = getNumElementsNode(dimNames);

        CNode loadToken = outvarToken;
        if (isOriginalTypeDiff) {
            loadToken = CNodeFactory.newVariable(LoadMatrixVariable.VARNAME_LOADMATRIX, loadType);
        }

        CNode load = getFunctionCall(new LoadRawDataFromFile(), varNameToken, numElementsToken, loadToken);
        instructions.addInstruction(load);

        String extraInclude = "";
        // If type is different, add copy and free
        if (isOriginalTypeDiff) {
            instructions.addInstruction(getFunctionCall(MatrixCopy.getProvider(), loadToken, outvarToken));
            String freeCode = getData().getSettings().get(CirKeys.CUSTOM_FREE_DATA_CODE);
            instructions.addLiteralInstruction(freeCode.replace("$1", loadToken.getCode()) + ";");

            extraInclude = getData().getSettings().get(CirKeys.CUSTOM_ALLOCATION_HEADER);
        }

        instructions.addReturn(outvarToken);

        InstructionsInstance instance = new InstructionsInstance(functionType, functionName, "lib/load", instructions);
        if (!extraInclude.isEmpty()) {
            instance.setCustomIncludes(extraInclude);
        }
        return instance;
    }

    private CNode getNumElementsNode(List<String> dimNames) {
        InstanceProvider uint32 = UtilityInstances.getCastToScalarProvider(StdIntFactory.newUInt32());

        CNodeFactory cnodes = new CNodeFactory(getSettings());
        VariableType intType = getNumerics().newInt();

        // Get first dim
        CNode numElements = cnodes.newFunctionCall(uint32, CNodeFactory.newVariable(dimNames.get(0), intType));

        for (int i = 1; i < dimNames.size(); i++) {
            CNode dim = CNodeFactory.newVariable(dimNames.get(i), intType);
            CNode cast = cnodes.newFunctionCall(uint32, dim);
            numElements = cnodes.newFunctionCall(COperator.Multiplication, numElements, cast);
        }

        return numElements;
    }

    // private static List<String> getDimNames(List<VariableType> inputTypes) {
    private static List<String> getDimNames(int numDims) {
        List<String> dimNames = new ArrayList<>();

        // Start a 1, first type is the string with the filename
        for (int i = 0; i < numDims; i++) {
            dimNames.add(LoadMatrixVariable.INPUT_DIM_PREFIX + i);
        }

        return dimNames;
    }
}
