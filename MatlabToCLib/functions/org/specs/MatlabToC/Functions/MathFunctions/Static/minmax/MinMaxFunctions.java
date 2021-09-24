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

package org.specs.MatlabToC.Functions.MathFunctions.Static.minmax;

import static org.specs.MatlabToC.Functions.MathFunctions.Static.minmax.MinMaxDecTemplate.*;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Tree.Utils.ForNodes;
import org.specs.CIR.Tree.Utils.IfNodes;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.Types.Views.Code.CodeUtils;
import org.specs.CIRFunctions.MatrixFunction;
import org.specs.CIRFunctions.MatrixDec.DeclaredProvider;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;
import org.specs.MatlabToC.MatlabCFilename;
import org.specs.MatlabToC.jOptions.AMatlabInstanceBuilder;
import org.specs.MatlabToC.jOptions.MatlabInlinable;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.lazy.ThreadSafeLazy;

/**
 * This class contains methods that create and return new instances for the implementation of the Matlab built-in
 * functions 'max' and 'min'.
 * 
 * 
 * @author Pedro Pinto
 * 
 */
public class MinMaxFunctions extends AMatlabInstanceBuilder {

    private final MinMax minOrMax;

    /**
     * @param data
     */
    public MinMaxFunctions(ProviderData data, MinMax minOrMax) {
        super(data);

        this.minOrMax = minOrMax;
    }

    private final static String C_FILENAME = MatlabCFilename.MatlabGeneral.getCFilename();

    public static InstanceProvider getProviderVectorDec(MinMax minOrMax) {
        return data -> new MinMaxFunctions(data, minOrMax).newMinMaxVectorDecInstance();
    }

    @Override
    public FunctionInstance create() {
        return newMinMaxVectorDecInstance();

    }

    /**
     * Creates a new instance for the cases where the input is a vector. </br>
     * </br>
     * <b>Example call:</b>
     * 
     * <pre>
     * {@code
     * Y = max(X),
     * when X is a vector
     * }
     * </pre>
     * 
     * @param originalTypes
     *            - the original types of the matlab function call
     * @param type
     *            - whether this is a min or a max
     * @return an instance of {@link LiteralInstance}
     */
    public FunctionInstance newMinMaxVectorDecInstance() {
        // Check if inlining is possible
        MatlabInlinable inlinableType = getInlinable(minOrMax);

        boolean inline = isInlinable(inlinableType);

        if (inline) {
            return newMinMaxVectorInstanceInlined();
        }
        return newMinMaxVectorDecInstanceNotInlined();

    }

    private boolean isInlinable(MatlabInlinable inlinableType) {
        // Check if inlining is active for this function
        if (!getSettings().get(CirKeys.INLINE).inline(inlinableType)) {
            return false;
        }

        // Check if function call level is less than 2
        if (getData().getFunctionCallLevel() > 1) {
            return false;
        }

        if (getData().getIsInputAVariable().isEmpty()) {
            // LoggingUtils.msgWarn("Should this be empty?");
            return false;
        }

        // Check if the input is a variable

        if (!getData().getIsInputAVariable().get(0)) {
            return false;
        }

        return true;
    }

    private static MatlabInlinable getInlinable(MinMax minOrMax) {
        if (minOrMax == MinMax.MIN) {
            return MatlabInlinable.MIN;
        }

        if (minOrMax == MinMax.MAX) {
            return MatlabInlinable.MAX;
        }

        throw new RuntimeException("Not implemented '" + minOrMax + "'");
    }

    private LiteralInstance newMinMaxVectorDecInstanceNotInlined() {

        // Calculate the length of the linearized version of the array
        MatrixType vector = getTypeAtIndex(MatrixType.class, 0);

        // Get the correct operator
        String operatorString = minOrMax.getOperatorString();

        // Build the function types
        List<VariableType> inputTypes = Arrays.asList(vector);
        List<String> inputNames = Arrays.asList(MinMaxDecTemplate.MM_VECTOR_INPUT_NAME);

        // NumericType vectorNumericType = VariableTypeUtilsOld.getNumericType(vector);
        VariableType outputType = MatrixUtils.getElementType(vector);
        // VariableType outputType = VariableTypeFactoryOld.newNumeric(vectorNumericType);
        String outputName = MinMaxDecTemplate.MM_OUTPUT_MAX_NAME;

        FunctionType functionTypes = FunctionType.newInstance(inputNames, inputTypes, outputName, outputType);

        // vector.matrix().functions().numel()
        // Integer length = vector.getTypeShape().getNumElements();

        VariableType intType = getNumerics().newInt();
        FunctionInstance numelInstance = getInstance(vector.matrix().functions().numel(), vector);
        FunctionInstance getInstance = getInstance(vector.matrix().functions().get(), vector, intType);

        CNode vectorVariable = CNodeFactory.newVariable(inputNames.get(0), inputTypes.get(0));
        CNode iVariable = CNodeFactory.newVariable("i", intType);

        // Get the output numeric type string
        // String maxTypeString = vectorNumericType.getDeclarationCode();
        String maxTypeString = CodeUtils.getSimpleType(outputType);
        String numelCall = numelInstance.getCallCode(vectorVariable);
        String get0Call = getInstance.getCallCode(vectorVariable, CNodeFactory.newCNumber(0));
        String getiCall = getInstance.getCallCode(vectorVariable, iVariable);

        // Get the template and replace the tags
        String cBody = SpecsIo.getResource(MinMaxDecResources.MIN_MAX_VECTOR_DEC.getResource());
        cBody = MinMaxDecTemplate.parseTemplate(cBody, MM_LENGTH.getTag(), numelCall, MM_OPERATOR.getTag(),
                operatorString, MM_MAX_TYPE.getTag(), maxTypeString, MM_VECTOR_GET_0.getTag(), get0Call,
                MM_VECTOR_GET_I.getTag(), getiCall);

        // Create the function name
        String baseName = "vector_dec";
        String typesSuffix = FunctionInstanceUtils.getTypesSuffix(functionTypes.getCInputTypes());
        String cFunctionName = minOrMax.getName() + "_" + baseName + typesSuffix;

        // Create the new instance ( there are no dependencies nor includes ) and return
        LiteralInstance instance = new LiteralInstance(functionTypes, cFunctionName, MinMaxFunctions.C_FILENAME,
                cBody);
        instance.setComments(" " + minOrMax.getName() + " implementation for Y = " + minOrMax.getName()
                + "(X), when X is a vector ");

        instance.addInstance(numelInstance, getInstance);

        return instance;
    }

    /**
     * Creates a new instance for the cases where the input is a vector and the output also contains the indexes. </br>
     * </br>
     * <b>Example call:</b>
     * 
     * <pre>
     * {@code
     * [Y,I] = max(X),
     * when X is a vector
     * }
     * </pre>
     * 
     * @param originalTypes
     *            - the original types of the matlab function call
     * @param minOrMax
     *            - whether this is a min or a max
     * @return an instance of {@link LiteralInstance}
     */
    public LiteralInstance newMinMaxVectorIndexDecInstance() {

        // Calculate the length of the linearized version of the array
        MatrixType vector = getTypeAtIndex(MatrixType.class, 0);

        // Integer length = MatrixUtils.getVectorLength(vector);
        Integer length = vector.getTypeShape().getNumElements();

        // Get the correct operator
        String operatorString = minOrMax.getOperatorString();

        // Get the template and replace the tags
        String cBody = SpecsIo.getResource(MinMaxDecResources.MIN_MAX_VECTOR_INDEX_DEC.getResource());
        cBody = MinMaxDecTemplate.parseTemplate(cBody, MM_LENGTH.getTag(), length.toString(), MM_OPERATOR.getTag(),
                operatorString);

        // Build the function types
        List<VariableType> inputTypes = Arrays.asList(vector);
        List<String> inputNames = Arrays.asList(MinMaxDecTemplate.MM_VECTOR_INPUT_NAME);

        // NumericType vectorNumericType = VariableTypeUtilsOld.getNumericType(vector);
        VariableType firstOutput = MatrixUtils.getElementType(vector);
        // VariableType firstOutput = VariableTypeFactoryOld.newNumeric(vectorNumericType);
        VariableType secondOutput = getNumerics().newInt();
        List<VariableType> outputTypes = Arrays.asList(firstOutput, secondOutput);

        List<String> outputNames = Arrays.asList(MinMaxDecTemplate.MM_OUTPUT_MAX_NAME,
                MinMaxDecTemplate.MM_VECTOR_OUTPUT_INDEX_NAME);

        FunctionType functionTypes = FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, outputNames,
                outputTypes);

        // Create the function name
        String baseName = "vector_index_dec";
        String typesSuffix = FunctionInstanceUtils.getTypesSuffix(functionTypes.getCInputTypes());
        String cFunctionName = minOrMax.getName() + "_" + baseName + typesSuffix;

        // Create the new instance ( there are no dependencies nor includes ) and return
        LiteralInstance instance = new LiteralInstance(functionTypes, cFunctionName, MinMaxFunctions.C_FILENAME,
                cBody);
        instance.setComments(" " + minOrMax.getName() + " implementation for [Y,I] = " + minOrMax.getName()
                + "(X), when X is a vector and the indexes are also returned ");

        return instance;
    }

    private FunctionInstance newMinMaxVectorInstanceInlined() {

        // Calculate the length of the linearized version of the array
        MatrixType vector = getTypeAtIndex(MatrixType.class, 0);

        // Build the function types
        List<VariableType> inputTypes = Arrays.asList(vector);
        // List<VariableType> inputTypes = CollectionUtils.asListSame(vector);

        VariableType outputType = MatrixUtils.getElementType(vector);

        // FunctionTypes functionTypes = FunctionTypes.newInstanceNotImplementable(inputTypes, outputType);
        FunctionType functionTypes = FunctionType.newInstanceWithOutputsAsInputs(null, inputTypes, null, outputType);

        // Create the function name
        String baseName = "vector_dec";
        String typesSuffix = FunctionInstanceUtils.getTypesSuffix(functionTypes.getCInputTypes());
        String cFunctionName = minOrMax.getName() + "_" + baseName + typesSuffix;

        // Create the new instance ( there are no dependencies nor includes ) and return
        // LiteralInstance instance = new LiteralInstance(functionTypes, cFunctionName, C_FILENAME, cBody);
        // instance.setComments(" " + type.getName() + " implementation for Y = " + type.getName()
        // + "(X), when X is a vector ");

        // String cFilename = MatlabCFilename.ArrayCreatorsDec.getCFilename();
        // CInstructionList cBody = buildBodyInstructions(functionTypes);

        // Create get for input array and one index
        FunctionInstance get = getInstance(MatrixFunction.GET, vector, getNumerics().newInt());
        FunctionInstance numel = getInstance(vector.matrix().functions().numel(), vector);

        // Iterator variable
        Variable iteratorVar = new Variable("matisse_min_i", getNumerics().newInt());

        InlineCode code = buildInstanceInlinedBody(functionTypes, minOrMax, get, numel, iteratorVar);

        InlinedInstance instance = new InlinedInstance(functionTypes, cFunctionName, code);

        instance.setCallInstances(get, numel);
        instance.setCallVars(iteratorVar);

        return instance;

    }

    private InlineCode buildInstanceInlinedBody(final FunctionType functionTypes, final MinMax type,
            final FunctionInstance get, final FunctionInstance numel, final Variable iteratorVar) {

        return new InlineCode() {

            /**
             * First argument is the 1-dimension matrix with the values where to calculate the min/max; <br>
             * Second argument is the scalar output variable;
             */
            @Override
            public String getInlineCode(List<CNode> arguments) {

                // MatrixUtils matrixUtils = new MatrixUtils(getProviderSetup());

                CInstructionList insts = new CInstructionList(functionTypes);

                // Arguments
                CNode inputArray = arguments.get(0);
                CNode outputVar = arguments.get(1);

                // Array access index
                CNode indexZero = CNodeFactory.newCNumber(0);

                // If input array is not a variable, put it in a temporary variable
                // This declared a temporary variable in-place, is compatible only with C99
                // TODO: Fazer esta trasformação (inlining do min) a nível do MATLAB? Ou será melhor ter mais informação
                // disponível e.g., se input type veio de uma variável/id
                /*
                if (inputArray.getType() != CNodeType.Variable) {
                    Variable tempVariable = new Variable("min_input_temp", inputArray.getVariableType());
                
                    CNode tempVar = CNodeFactory.newLiteral(inputArray.getVariableType().code()
                	    .getType() + " min_input_temp");
                    // CNode tempVar = CNodeFactory.newVariable(tempVariable);
                
                    insts.addAssignment(tempVar, inputArray);
                
                    // Temporary replaces input array
                    inputArray = tempVar;
                }
                */

                // Set output var to the first element of input vector
                // outputVar = inputArray[0];
                insts.addAssignment(outputVar, CNodeFactory.newFunctionCall(get, inputArray, indexZero));

                VariableNode iVar = CNodeFactory.newVariable(iteratorVar);

                // Build assigment
                // out = in[i];
                CNode getCall = CNodeFactory.newFunctionCall(get, inputArray, iVar);
                CNode assign = CNodeFactory.newAssignment(outputVar, getCall);

                // Build if for min/max
                String getCode = getCall.getCode();
                String varCode = outputVar.getCode();
                CNode condition = CNodeFactory.newLiteral(getCode + " " + type.getOperatorString() + " " + varCode);
                CNode ifToken = IfNodes.newIfThen(condition, assign);

                // Build for from 1 to number of elements of input array
                CNode numberOne = CNodeFactory.newCNumber(1);
                CNode numelCall = CNodeFactory.newFunctionCall(numel, inputArray);
                COperator stopOp = COperator.LessThan;
                CNode forToken = new ForNodes(getData()).newForLoopBlock(iVar, numberOne, numelCall, stopOp,
                        Arrays.asList(ifToken));

                // CTokenUtils.collectInstances(insts.getRoot(), callInstances);

                insts.addInstruction(forToken);

                return insts.toString();
            }
        };
    }

    /**
     * Creates a new instance for the cases where there are two inputs, both matrices. </br>
     * </br>
     * <b>Example call:</b>
     * 
     * <pre>
     * {@code
     * Y = max(X,Z),
     * when both X and Z are matrices
     * }
     * </pre>
     * 
     * @param originalTypes
     *            - the original types of the matlab function call
     * @param functionSettings
     *            - the function settings
     * @param minOrMax
     *            - whether this is a min or max
     * @return an instance of {@link LiteralInstance}
     */
    public LiteralInstance newMinMaxMatricesDecInstance() {

        // Get the shape and calculate the length of the linearized version of the array
        MatrixType matrix1 = getTypeAtIndex(MatrixType.class, 0);
        MatrixType matrix2 = getTypeAtIndex(MatrixType.class, 1);
        // Integer length = MatrixUtils.getMatrixLength(matrix1);
        Integer length = matrix1.getTypeShape().getNumElements();
        List<Integer> shape = MatrixUtils.getShapeDims(matrix1);

        // Get the correct operator
        String operatorString = minOrMax.getOperatorString();

        // Get the copy instance
        FunctionInstance copyInstance = newCopyInstance(matrix1, length);

        // Build the function types
        List<VariableType> inputTypes = Arrays.asList(matrix1, matrix2);
        List<String> inputNames = Arrays.asList(MinMaxDecTemplate.MM_MATRICES_INPUT_1_NAME,
                MinMaxDecTemplate.MM_MATRICES_INPUT_2_NAME);

        // VariableType outputElementType = VariableTypeUtilsG.getMaximalFit(Arrays.asList(
        List<VariableType> types = Arrays.asList(matrix1.matrix().getElementType(), matrix2.matrix().getElementType());
        VariableType outputElementType = ScalarUtils.getMaxRank(ScalarUtils.cast(types));

        VariableType outputType = StaticMatrixType.newInstance(outputElementType, shape);
        // VariableTypeFactoryOld.newNumeric(outputNumericType), shape);
        String outputName = MinMaxDecTemplate.MM_OUTPUT_MAX_NAME;

        FunctionType functionTypes = FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, outputName,
                outputType);

        // Get the temp type string
        // String tempTypeString = outputNumericType.getDeclarationCode();
        String tempTypeString = CodeUtils.getSimpleType(outputElementType);

        // Get the template and replace the tags
        String cBody = SpecsIo.getResource(MinMaxDecResources.MIN_MAX_MATRICES_DEC.getResource());
        cBody = MinMaxDecTemplate.parseTemplate(cBody, MM_LENGTH.getTag(), length.toString(), MM_OPERATOR.getTag(),
                operatorString, MM_COPY_CALL.getTag(), copyInstance.getCName(), MM_TEMP_TYPE.getTag(), tempTypeString);

        // Create the function name
        String baseName = "matrices_dec";
        String typesSuffix = FunctionInstanceUtils.getTypesSuffix(functionTypes.getCInputTypes());
        String cFunctionName = minOrMax.getName() + "_" + baseName + typesSuffix;

        // Create the new instance ( there are no includes )
        LiteralInstance instance = new LiteralInstance(functionTypes, cFunctionName, MinMaxFunctions.C_FILENAME,
                cBody);
        instance.setComments(" " + minOrMax.getName() + " implementation for Y = " + minOrMax.getName()
                + "(X,Z), when X and Z are both matrices ");

        // Set the copy function dependency and return
        instance.getCustomImplementationInstances().add(copyInstance);

        return instance;
    }

    private static final ThreadSafeLazy<String> minMaxScalarsDecResource = new ThreadSafeLazy<>(
            () -> SpecsIo.getResource(MinMaxDecResources.MIN_MAX_SCALARS_DEC.getResource()));

    /**
     * Creates a new instance for the cases where there are two inputs, both scalars. </br>
     * </br>
     * <b>Example call:</b>
     * 
     * <pre>
     * {@code
     * Y = max(X,Z),
     * when both X and Z are scalars
     * }
     * </pre>
     * 
     * @param originalTypes
     *            - the original types of the matlab function call
     * @param type
     *            - whether this is a min or max
     * @return an instance of {@link LiteralInstance}
     */
    public static LiteralInstance newMinMaxScalarsInstance(List<VariableType> originalTypes, MinMax type) {

        // VariableType scalar1 = originalTypes.get(0);
        // VariableType scalar2 = originalTypes.get(1);

        // Get the correct operator
        String operatorString = type.getOperatorString();

        // Get the template and replace the tags
        String cBody = MinMaxFunctions.minMaxScalarsDecResource.getValue();
        cBody = MinMaxDecTemplate.parseTemplate(cBody, MM_OPERATOR.getTag(), operatorString);

        // Build the function types
        // List<VariableType> inputTypes = Arrays.asList(scalar1, scalar2);
        List<String> inputNames = Arrays.asList(MinMaxDecTemplate.MM_SCALARS_INPUT_1_NAME,
                MinMaxDecTemplate.MM_SCALARS_INPUT_2_NAME);

        // VariableType outputType = VariableTypeUtilsG.getMaximalFit(originalTypes);
        VariableType outputType = ScalarUtils.getMaxRank(ScalarUtils.cast(originalTypes));
        String outputName = MinMaxDecTemplate.MM_OUTPUT_MAX_NAME;

        FunctionType functionTypes = FunctionType.newInstance(inputNames, originalTypes, outputName, outputType);

        // Create the function name
        String baseName = "scalars_dec";
        String typesSuffix = FunctionInstanceUtils.getTypesSuffix(functionTypes.getCInputTypes());
        String cFunctionName = type.getName() + "_" + baseName + typesSuffix;

        // Create the new instance ( there are no dependencies nor includes ) and return
        LiteralInstance instance = new LiteralInstance(functionTypes, cFunctionName, MinMaxFunctions.C_FILENAME,
                cBody);
        instance.setComments(" " + type.getName() + " implementation for Y = " + type.getName()
                + "(X,Z), when X and Z are both scalars ");

        return instance;
    }

    /**
     * Creates a new instance for the cases where there are two inputs, a matrix and a scalar. </br>
     * </br>
     * <b>Example call:</b>
     * 
     * <pre>
     * {@code
     * Y = max(X,Z),
     * when both X is a matrix and Z is a scalar or vice-versa
     * }
     * </pre>
     * 
     * @param originalTypes
     *            - the original types of the matlab function call
     * @param functionSettings
     *            - the function settings
     * @param minOrMax
     *            - whether this is a min or max
     * @return an instance of {@link LiteralInstance}
     */
    public LiteralInstance newMinMaxMatrixScalarDecInstance() {

        boolean matrixFirst = true;

        int matrixIndex = 0;
        int scalarIndex = 1;

        // If first input is not a matrix, invert indexes
        if (!MatrixUtils.isMatrix(getData().getInputTypes().get(0))) {
            matrixIndex = 1;
            scalarIndex = 0;
            matrixFirst = false;
        }

        // Get the two inputs, the shape and calculate the length of the linearized version of the matrix
        MatrixType matrix = getTypeAtIndex(MatrixType.class, matrixIndex);
        ScalarType scalar = getTypeAtIndex(ScalarType.class, scalarIndex);
        /*
        	// Get the two inputs, the shape and calculate the length of the linearized version of the matrix
        	MatrixType matrix = null;
        	ScalarType scalar = null;
        
        	if (MatrixUtilsV2.isMatrix(getData().getInputTypes().get(0))) {
        	    // if (MatrixUtilsV2.isStaticMatrix(originalTypes.get(0))) {
        	    matrix = getTypeAtIndex(MatrixType.class, 0);
        	    scalar = getTypeAtIndex(ScalarType.class, 1);
        	    // matrix = originalTypes.get(0);
        	    // scalar = originalTypes.get(1);
        	} else {
        	    matrix = getTypeAtIndex(MatrixType.class, 1);
        	    scalar = getTypeAtIndex(ScalarType.class, 0);
        	    // matrix = originalTypes.get(1);
        	    // scalar = originalTypes.get(0);
        
        	    matrixFirst = false;
        	}
        	*/

        Integer length = matrix.getTypeShape().getNumElements();
        List<Integer> shape = matrix.getTypeShape().getDims();

        // Get the correct operator
        String operatorString = minOrMax.getOperatorString();

        // Get the copy instance
        FunctionInstance copyInstance = newCopyInstance(matrix, length);

        // Build the function types
        List<VariableType> inputTypes = null;
        List<String> inputNames = null;
        if (matrixFirst) {

            inputTypes = Arrays.asList(matrix, scalar);
            inputNames = Arrays.asList(MinMaxDecTemplate.MM_MATRIX_INPUT_NAME, MinMaxDecTemplate.MM_SCALAR_INPUT_NAME);
        } else {

            inputTypes = Arrays.asList(scalar, matrix);
            inputNames = Arrays.asList(MinMaxDecTemplate.MM_SCALAR_INPUT_NAME, MinMaxDecTemplate.MM_MATRIX_INPUT_NAME);
        }

        // NumericType outputNumericType = VariableTypeUtilsOld.getNumericType(matrix);
        VariableType elementType = MatrixUtils.getElementType(matrix);
        VariableType outputType = StaticMatrixType.newInstance(elementType, shape);
        // VariableTypeFactoryOld.newNumeric(outputNumericType), shape);
        String outputName = MinMaxDecTemplate.MM_OUTPUT_MAX_NAME;

        FunctionType functionTypes = FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, outputName,
                outputType);

        // Get the temp type string
        // String tempTypeString = outputNumericType.getDeclarationCode();
        String tempTypeString = CodeUtils.getSimpleType(elementType);

        // Get the template and replace the tags
        String cBody = SpecsIo.getResource(MinMaxDecResources.MIN_MAX_MATRIX_SCALAR_DEC.getResource());
        cBody = MinMaxDecTemplate.parseTemplate(cBody, MM_LENGTH.getTag(), length.toString(), MM_OPERATOR.getTag(),
                operatorString, MM_COPY_CALL.getTag(), copyInstance.getCName(), MM_TEMP_TYPE.getTag(), tempTypeString);

        // Create the function name
        String baseName = "matrix_scalar_dec";
        String typesSuffix = FunctionInstanceUtils.getTypesSuffix(functionTypes.getCInputTypes());
        String cFunctionName = minOrMax.getName() + "_" + baseName + typesSuffix;

        // Create the new instance ( there are no includes )
        LiteralInstance instance = new LiteralInstance(functionTypes, cFunctionName, MinMaxFunctions.C_FILENAME,
                cBody);
        instance.setComments(" " + minOrMax.getName() + " implementation for Y = " + minOrMax.getName()
                + "(X,Z), when one input is a matrix and the other is a scalar ");

        // Set the copy function dependency and return
        instance.getCustomImplementationInstances().add(copyInstance);

        return instance;
    }

    /**
     * Private method to create an instance of the copy function for the Min or Max function instances.
     * 
     * @param functionSettings
     * @param matrix
     * @param length
     * @return
     */
    private FunctionInstance newCopyInstance(VariableType matrix, Integer length) {

        List<VariableType> copyInputs = Arrays.asList(matrix, matrix, getNumerics().newInt(length));
        // VariableTypeFactoryOld.newNumeric(NumericDataFactory.newInstance(length)));

        FunctionInstance copy = getInstance(DeclaredProvider.COPY, copyInputs);

        return copy;
    }

}
