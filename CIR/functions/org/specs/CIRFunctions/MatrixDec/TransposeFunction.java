/**
 * h * Copyright 2012 SPeCS Research Group.
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

package org.specs.CIRFunctions.MatrixDec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.specs.CIR.CodeGenerator.CodeGenerationException;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.Instructions.InstructionType;
import org.specs.CIR.Tree.Utils.ForNodes;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIRFunctions.CirFilename;

import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class TransposeFunction extends AInstanceBuilder {

    /**
     * @param data
     */
    public TransposeFunction(ProviderData data) {
        super(data);
    }

    // The names
    private final static String NT_OUTPUT_NAME = "transposed_matrix";
    private final static String NT_INPUT_NAME = "input_matrix";
    private final static String NT_BASE_FUNCTION_NAME = "numeric_transpose_dec";

    /**
     * Builds and returns a new {@link TransposeFunction}.
     * 
     * @param originalTypes
     *            - a list of {@link VariableType} that <b>MUST</b> contain the following:
     *            <ul>
     *            <li>m: a numeric matrix ( allocated or declared )</li>
     *            </ul>
     * @param useLinearArrays
     *            - whether this instance will use linearized versions of the matrices
     * @return a new {@link TransposeFunction}
     */
    private static FunctionInstance newInstancePrivate(ProviderData data) {
        return new TransposeFunction(data).create();
    }

    @Override
    public FunctionInstance create() {

        // List<VariableType> originalTypes = getData().getInputTypes();

        FunctionTypesOutput output = buildFunctionTypes();

        // return new NumericTransposeInstance(output.getFunctionTypes(),
        // output.getMatrixImplementation());
        String typesSuffix = FunctionInstanceUtils.getTypesSuffix(output.getFunctionTypes().getCInputTypes());
        String cFunctionName = TransposeFunction.NT_BASE_FUNCTION_NAME + typesSuffix;

        String cFilename = CirFilename.DECLARED.getFilename();
        CInstructionList cBody;
        switch (getTypeAtIndex(MatrixType.class, 0).getTypeShape().getNumDims()) {
        case 0:
            throw new CodeGenerationException("Not yet implemented: transpose for empty matrices");
        case 1:
            cBody = buildInstructions1D(output);
            break;
        case 2:
            cBody = buildInstructions2D(output);
            break;
        default:
            throw new CodeGenerationException();
        }

        return new InstructionsInstance(output.getFunctionTypes(), cFunctionName, cFilename, cBody);
    }

    /**
     * Builds the function types used by this instance.
     * 
     * @param originalTypes
     * @param useLinearArrays
     * @return
     */
    private FunctionTypesOutput buildFunctionTypes() {

        // Data about the input matrix
        MatrixType matrix = getTypeAtIndex(MatrixType.class, 0);
        // ScalarType matrixInnerType = MatrixUtilsV2.getElementType(matrix);
        // MatrixImplementation matrixImplementation = MatrixUtilsV2.getImplementation(matrix);

        // The input
        List<String> inputNames = Arrays.asList(TransposeFunction.NT_INPUT_NAME);
        List<VariableType> inputTypes = Arrays.asList(matrix);

        VariableType outputType = getOutputType(matrix);

        // The output name
        String outputName = TransposeFunction.NT_OUTPUT_NAME;

        FunctionType functionTypes = FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, outputName,
                outputType);

        return new FunctionTypesOutput(functionTypes, matrix);
    }

    private static VariableType getOutputType(MatrixType matrix) {
        TypeShape shape = matrix.getTypeShape();

        // If it has two dimensions, invert them. Otherwise, use a MatrixShape with 2 dimensions
        switch (shape.getRawNumDims()) {
        case 0:
        case 1:
            throw new NotImplementedException(TransposeFunction.class);
        case 2:
            List<Integer> newShape = new ArrayList<>(shape.getDims());
            Collections.reverse(newShape);
            shape = TypeShape.newInstance(newShape);
            break;
        default:
            throw new CodeGenerationException(
                    "Using transpose on matrix with more than 2 dimensions. Use transmute instead.");
        }

        // Return a copy of the matrix, with the new shape
        return matrix.copy().matrix().setShape(shape);

        /*
        
        	VariableType outputType = null;
        
        	// The output type
        	if (matrixImplementation == MatrixImplementation.DECLARED) {
        
        	    // If the input is a declared matrix, the output is a declared matrix too
        	    Integer M = MatrixUtils.get2DM(matrix);
        	    Integer N = MatrixUtils.get2DN(matrix);
        
        	    outputType = StaticMatrixType.newInstance(matrixInnerType, Arrays.asList(N, M));
        
        	} else {
        
        	    // If the input is an allocated matrix, the output is an allocated matrix too
        	    outputType = DynamicMatrixType.newInstance(matrixInnerType);
        	}
        	return outputType;
        	*/
    }

    /**
     * Builds the instructions for this instance, assuming a 2D matrix.
     * 
     * @param functionSettings
     * @param matrixImplementation
     * @return a new instance of {@link CInstructionList}
     */
    private CInstructionList buildInstructions2D(FunctionTypesOutput data) {
        FunctionType functionTypes = data.getFunctionTypes();

        CInstructionList instructions = new CInstructionList(functionTypes);

        // The input and output variables and their tokens
        Variable inputVariable = functionTypes.getInputVar(TransposeFunction.NT_INPUT_NAME);
        Variable outputVariable = functionTypes.getInputVar(TransposeFunction.NT_OUTPUT_NAME);

        CNode inputVarT = CNodeFactory.newVariable(inputVariable);
        CNode outputVarT = CNodeFactory.newVariable(outputVariable);

        // The values for M and N
        CNode mValue = getEndValue(inputVarT, 0, data.getInputType());
        CNode nValue = getEndValue(inputVarT, 1, data.getInputType());

        // Create a new variable, of the same type and shape of the output
        Variable tempVariable = new Variable("temp",
                functionTypes.getInputVar(TransposeFunction.NT_OUTPUT_NAME).getType());
        CNode tempVarT = CNodeFactory.newVariable(tempVariable);

        // Build the FOR loop nest
        CNode loopNestT = buildLoopNest(tempVarT, tempVariable, inputVarT, inputVariable,
                data.getInputType(), mValue, nValue);
        instructions.addInstruction(loopNestT, InstructionType.Block);

        // Call copy from temp to the output
        CNode copyCallT = buildCopyCall(tempVarT, outputVarT, data.getInputType());
        instructions.addInstruction(copyCallT, InstructionType.FunctionCall);

        // Add the return instruction
        CNode returnT = CNodeFactory.newReturn(outputVarT);
        instructions.addInstruction(returnT, InstructionType.Return);

        return instructions;
    }

    /**
     * Builds the instructions for this instance, assuming a 1D matrix.
     * 
     * @param functionSettings
     * @param matrixImplementation
     * @return a new instance of {@link CInstructionList}
     */
    private CInstructionList buildInstructions1D(FunctionTypesOutput data) {
        FunctionType functionTypes = data.getFunctionTypes();
        MatrixType matrixType = data.getInputType();

        CInstructionList instructions = new CInstructionList(functionTypes);

        // The input and output variables and their tokens
        Variable inputVariable = functionTypes.getInputVar(TransposeFunction.NT_INPUT_NAME);
        Variable outputVariable = functionTypes.getInputVar(TransposeFunction.NT_OUTPUT_NAME);
        Variable numelVariable = new Variable("numel", getNumerics().newInt());

        CNode numelToken = CNodeFactory.newVariable(numelVariable);
        CNode inputVarT = CNodeFactory.newVariable(inputVariable);
        CNode outputVarT = CNodeFactory.newVariable(outputVariable);

        CNode setNumel = CNodeFactory.newAssignment(numelToken,
                getFunctionCall(matrixType.matrix().functions().numel(),
                        Arrays.asList(inputVarT)));

        instructions.addInstruction(setNumel);

        // Create a new variable, of the same type and shape of the output
        Variable tempVariable = new Variable("temp",
                functionTypes.getInputVar(TransposeFunction.NT_OUTPUT_NAME).getType());
        CNode tempVarT = CNodeFactory.newVariable(tempVariable);

        Variable iVar = new Variable("i", getNumerics().newInt());
        CNode iToken = CNodeFactory.newVariable(iVar);
        CNode startValue = CNodeFactory.newCNumber(0);
        COperator stopOp = COperator.LessThan;

        CNode endValueI = numelToken;

        COperator incrementOp = COperator.Addition;

        List<CNode> getArguments = Arrays.asList(inputVarT, iToken);
        CNode getCallT = getFunctionCall(matrixType.matrix().functions().get(), getArguments);
        List<CNode> setArguments = Arrays.asList(tempVarT, iToken, getCallT);
        CNode loopContent = getFunctionCall(matrixType.matrix().functions().set(), setArguments);

        CNode forNode = new ForNodes(getData()).newForLoopBlock(iVar, startValue, stopOp, endValueI, incrementOp,
                CNodeFactory.newCNumber(1),
                loopContent);
        instructions.addInstruction(forNode);

        // Call copy from temp to the output
        CNode copyCallT = buildCopyCall(tempVarT, outputVarT, data.getInputType());
        instructions.addInstruction(copyCallT, InstructionType.FunctionCall);

        // Add the return instruction
        CNode returnT = CNodeFactory.newReturn(outputVarT);
        instructions.addInstruction(returnT, InstructionType.Return);

        return instructions;
    }

    /**
     * Builds the loop nest used in this instance.
     * 
     * @param inputVarT
     * @param tempVarT
     * 
     * @return
     */
    private CNode buildLoopNest(CNode tempVarT, Variable tempVar, CNode inputVarT, Variable inputVar,
            MatrixType matrixType, CNode mValue, CNode nValue) {

        // The induction variables and their tokens
        Variable iVar = new Variable("i", getNumerics().newInt());
        Variable jVar = new Variable("j", getNumerics().newInt());

        CNode iToken = CNodeFactory.newVariable(iVar);
        CNode jToken = CNodeFactory.newVariable(jVar);

        // The data for the loops
        CNode startValue = CNodeFactory.newCNumber(0);
        COperator stopOp = COperator.LessThan;

        CNode endValueI = mValue;
        CNode endValueJ = nValue;

        COperator incrementOp = COperator.Addition;

        // Build the get instruction of the inner loop body
        List<CNode> getArguments = Arrays.asList(inputVarT, iToken, jToken);

        CNode getCallT = getFunctionCall(matrixType.matrix().functions().get(), getArguments);

        // Build the set instruction of the inner loop body, the inner loop instruction
        List<CNode> setArguments = Arrays.asList(tempVarT, jToken, iToken, getCallT);

        CNode setCallT = getFunctionCall(matrixType.matrix().functions().set(), setArguments);

        // Build the inner loop
        CNode innerLoop = new ForNodes(getData()).newForLoopBlock(jVar, startValue, stopOp, endValueJ, incrementOp,
                CNodeFactory.newCNumber(1), setCallT);

        // Build the outer loop
        return new ForNodes(getData()).newForLoopBlock(iVar, startValue, stopOp, endValueI, incrementOp,
                CNodeFactory.newCNumber(1),
                innerLoop);
    }

    /**
     * Creates the function call to the function 'dim_size' that returns the size of a given dimension. This is used as
     * the end value for the loop in this instance.
     * 
     * @param inputVarT
     *            - the input variable token
     * @param i
     *            - the induction variable token
     * @param matrixImplementation
     *            - the matrix implementation
     * @return a {@link CNode} with the function call to 'dim_size'
     */
    private CNode getEndValue(CNode inputVarT, int i, MatrixType matrixType) {

        List<CNode> dimSizeArguments = Arrays.asList(inputVarT, CNodeFactory.newCNumber(i));

        // InstanceProvider dimSizeProvider = MatrixFunction.DIM_SIZE.getProvider(matrixImplementation);
        CNode dimSizeCallT = getFunctionCall(matrixType.matrix().functions().getDim(), dimSizeArguments);

        return dimSizeCallT;
    }

    /**
     * Builds and returns the copy function call.
     * 
     * @param tempVarT
     *            - the source
     * @param outputVarT
     *            - the destination
     * @param matrixImplementation
     *            - the implementation of the matrices
     * 
     * @return a {@link CNode} with the function call
     */
    private CNode buildCopyCall(CNode tempVarT, CNode outputVarT, MatrixType matrixType) {

        // InstanceProvider copyProvider = MatrixFunction.COPY.getProvider(matrixImplementation);
        return getFunctionCall(matrixType.matrix().functions().copy(), tempVarT, outputVarT);
    }

    private static class FunctionTypesOutput {

        private final FunctionType functionTypes;
        // private final MatrixImplementation matrixImplementation;
        private final MatrixType inputType;

        // public FunctionTypesOutput(FunctionType functionTypes, MatrixImplementation matrixImplementation) {
        public FunctionTypesOutput(FunctionType functionTypes, MatrixType inputType) {
            this.functionTypes = functionTypes;
            // this.matrixImplementation = matrixImplementation;
            this.inputType = inputType;
        }

        /**
         * @return the functionTypes
         */
        public FunctionType getFunctionTypes() {
            return this.functionTypes;
        }

        /**
         * @return the matrixImplementation
         */
        /*
        public MatrixImplementation getMatrixImplementation() {
        return matrixImplementation;
        }
        */

        public MatrixType getInputType() {
            return this.inputType;
        }

    }

    /**
     * @return
     */
    public static InstanceProvider getProvider() {
        return new InstanceProvider() {

            @Override
            public FunctionInstance newCInstance(ProviderData data) {
                return newInstancePrivate(data);
            }
        };
    }
}
