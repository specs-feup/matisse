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

package org.specs.CIRTypes.Types.DynamicMatrix.Functions;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.Utilities.InputChecker.CirInputsChecker;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixUtils;
import org.specs.CIRTypes.Types.DynamicMatrix.Utils.DynamicMatrixResource;

import pt.up.fe.specs.util.utilities.Replacer;

/**
 * Instance returns a new matrix, with the shape indicated by the inputs. No guarantees are made about the contents of
 * the matrix (it might not be initialized). The type of the matrix is determined by the value in
 * ProviderData.getOutputType().
 * 
 * Inputs:<br>
 * - As many integers as the number of dimensions, specifying the size of each dimension. If only one integer is passed,
 * the function creates a row-vector (shape 1xN);
 * 
 * 
 * @author JoaoBispo
 *
 */
public class Create extends AInstanceBuilder {

    private static final String OUTPUT_NAME = "t";

    public Create(ProviderData data) {
        super(data);
    }

    public static InstanceProvider getProvider() {

        CirInputsChecker checker = new CirInputsChecker()
                // All inputs should be scalar
                .ofType(ScalarType.class);

        return new GenericInstanceProvider(checker, data -> new Create(data).create());
    }

    @Override
    public FunctionInstance create() {

        // Get information
        int numIndexes = getData().getNumInputs();
        ScalarType elementType = getElementType();

        // Build FunctionType
        List<String> inputNames = FunctionInstanceUtils.createNameList("index_", numIndexes);
        List<VariableType> inputTypes = FunctionInstanceUtils.createTypeList(getNumerics().newInt(), numIndexes);
        String outputName = Create.OUTPUT_NAME;

        List<Integer> dims = new ArrayList<>(numIndexes);
        for (int i = 0; i < numIndexes; ++i) {
            ScalarType inputType = getData().getInputType(ScalarType.class, i);
            Number number = inputType.scalar().getConstant();
            int dim = -1;
            if (number != null) {
                if (number.doubleValue() == number.intValue()) {
                    dim = number.intValue();
                }
            }
            dims.add(dim);
        }
        if (dims.size() == 0) {
            dims.add(1);
            dims.add(1);
        } else if (dims.size() == 1) {
            dims.add(dims.get(0));
        }
        MatrixType outputType = DynamicMatrixType.newInstance(elementType, TypeShape.newInstance(dims));

        FunctionType type = FunctionType.newInstanceWithOutputsAsInputs(inputNames, inputTypes, outputName, outputType);
        // CInstructionList insts = getInstructions(type);

        String functionName = "create_" + outputType.getSmallId() + "_" + numIndexes;
        String filename = DynamicMatrixUtils.getFilename();

        List<CNode> callInstances = new ArrayList<>();
        String body = getBody(inputNames, outputType, callInstances);

        // InstructionsInstance fInst = new InstructionsInstance(functionName, filename, insts);
        LiteralInstance instance = new LiteralInstance(type, functionName, filename, body);

        // Complete instance
        instance.setCustomImplementationIncludes(SystemInclude.Stdlib, SystemInclude.Stdio);
        instance.getCustomImplementationInstances().add(callInstances);

        return instance;
    }

    private String getBody(List<String> inputNames, MatrixType outputType, List<CNode> callInstances) {
        Replacer body = new Replacer(DynamicMatrixResource.CREATE_BODY);

        String structureName = DynamicMatrixUtils.getStructInstance(outputType).getCName();
        body.replace("<TENSOR_STRUCT>", structureName);

        ScalarType elementType = outputType.matrix().getElementType();
        body.replace("<DATA_TYPE>", elementType.code().getType());

        body.replace("<DIMS>", inputNames.size());

        body.replaceRegex("<CUSTOM_DATA_ALLOCATOR>\\[\\[(.*)\\]\\]",
                getData().getSettings().get(CirKeys.CUSTOM_DATA_ALLOCATOR));

        // MatrixNodes matrixNodes = new MatrixNodes(getSetup());

        Variable matrixVar = new Variable(Create.OUTPUT_NAME, outputType.pointer().getType(true));
        CNode freeCall = getNodes().matrix().free(matrixVar);
        callInstances.add(freeCall);
        body.replace("<CALL_FREE>", freeCall.getCode());

        StringJoiner lengthCalc = new StringJoiner(" * ");
        inputNames.forEach((input) -> lengthCalc.add(input));
        body.replace("<LENGTH_CALC>", lengthCalc.toString());

        StringBuilder shapeAssign = new StringBuilder();
        String shapeTemplate = "\t(*t)->shape[<INDEX>] = <INPUT>;\n";
        for (int i = 0; i < inputNames.size(); i++) {
            String shapeCurrent = shapeTemplate.replace("<INDEX>", String.valueOf(i));
            shapeCurrent = shapeCurrent.replace("<INPUT>", inputNames.get(i));
            shapeAssign.append(shapeCurrent);
        }

        body.replace("<SHAPE_ASSIGN>", shapeAssign.toString());

        if (elementType.code().requiresExplicitInitialization()) {
            CInstructionList instructions = elementType.code().getSafeDefaultDeclaration(
                    CNodeFactory.newLiteral("((*t)->data[i])", elementType.pointer().getType(true),
                            PrecedenceLevel.Atom),
                    getData());

            StringBuilder initialization = new StringBuilder();
            initialization.append("for (i = 0; i < length; i++) {\n");
            for (CNode node : instructions.get()) {
                if (node instanceof FunctionCallNode) {
                    callInstances.add(node);
                }

                initialization.append(node.getCode());
                initialization.append("\n");
            }
            initialization.append("}\n");

            body.replace("<INITIALIZE_DATA>", initialization);
        } else {
            body.replace("<INITIALIZE_DATA>", "");
        }

        return body.toString();
    }

    private ScalarType getElementType() {
        // Get output type from 'data'

        VariableType outputType = getData().getOutputType();

        // Use default real if null
        if (outputType == null) {
            outputType = getSettings().get(CirKeys.DEFAULT_REAL);
        }

        // Just return, if cannot be cast to Scalar, throws exception
        return ScalarUtils.toScalar(outputType);
    }
}
