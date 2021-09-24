/**
 * Copyright 2015 SPeCS.
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

package org.specs.MatlabToC.Functions.StringFunctions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.specs.CIR.CodeGenerator.CodeGenerationException;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Language.Types.CTypeV2;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Tree.Utils.ForNodes;
import org.specs.CIR.Tree.Utils.IfNodes;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixFunctions;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.CIRTypes.Types.Numeric.NumericTypeV2;
import org.specs.CIRTypes.Types.String.StringType;
import org.specs.MatlabToC.Utilities.MatisseChecker;

public class StrcmpBuilder implements InstanceProvider {
    private static final String NAME_TRUE = "strcmp_true";
    private static final String NAME_FALSE = " strcmp_false";
    private static final String NAME_PARTIALLY_CHECKED_PREFIX = "strcmp_partially_checked_";
    private static final String FILE_NAME = "lib/matisse_string";

    private StrcmpBuilder() {
    }

    public static InstanceProvider newStrcmpBuilder() {
        MatisseChecker checker = new MatisseChecker().numOfInputs(2);

        return new GenericInstanceProvider(checker, new StrcmpBuilder());
    }

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
        assert data.getInputTypes().size() == 2;

        boolean onlySimpleStringTypes = true;
        for (VariableType type : data.getInputTypes()) {
            if (type instanceof StringType) {
                continue;
            }
            if (type instanceof DynamicMatrixType) {
                VariableType underlyingType = ((DynamicMatrixType) type).getElementType();
                if (underlyingType instanceof NumericTypeV2) {
                    if (((NumericTypeV2) underlyingType).getCtype() == CTypeV2.CHAR) {
                        onlySimpleStringTypes = false;
                        continue;
                    }
                }
            }
            return newValueInstance(data, false);
        }

        MatrixType type1 = data.getInputType(MatrixType.class, 0);
        MatrixType type2 = data.getInputType(MatrixType.class, 1);
        TypeShape shape1 = type1.matrix().getShape();
        TypeShape shape2 = type2.matrix().getShape();

        if (shape1.isFullyDefined() && shape2.isFullyDefined()) {
            int numel1 = shape1.getNumElements();
            int numel2 = shape2.getNumElements();

            // StringType overreports the actual size of the string by 1 element (due to the final '\0').
            // For that reason, we have to fix it here.
            if (type1 instanceof StringType) {
                numel1--;
            }
            if (type2 instanceof StringType) {
                numel2--;
            }

            if (numel1 != numel2) {
                return newValueInstance(data, false);
            }
        }

        if (onlySimpleStringTypes) {
            return newStaticInstance(data);
        }
        return newCheckedInstance(data);
    }

    private static FunctionInstance newValueInstance(ProviderData data, boolean value) {
        NumericFactory numerics = getNumerics(data);

        FunctionType functionType = FunctionType.newInstance(
                Arrays.asList("a", "b"),
                data.getInputTypes(),
                "result",
                numerics.newInt(value ? 1 : 0));

        String functionName = value ? StrcmpBuilder.NAME_TRUE : StrcmpBuilder.NAME_FALSE;

        return new InlinedInstance(functionType, functionName, value ? arguments -> "1" : arguments -> "0");
    }

    private static FunctionInstance newStaticInstance(ProviderData data) {
        StringType type1 = data.getInputType(StringType.class, 0);
        StringType type2 = data.getInputType(StringType.class, 1);

        if (type1.getString().equals(type2.getString())) {
            return newValueInstance(data, true);
        }

        return newValueInstance(data, false);
    }

    private static FunctionInstance newCheckedInstance(ProviderData data) {
        // NumericFactory numerics = getNumerics(data);

        MatrixType matrix1 = data.getInputType(MatrixType.class, 0);
        MatrixType matrix2 = data.getInputType(MatrixType.class, 1);

        if (matrix1 instanceof StringType) {
            return newPartiallySpecifiedInstance(data, 0, 1);
        }
        if (matrix2 instanceof StringType) {
            return newPartiallySpecifiedInstance(data, 1, 0);
        }

        throw new CodeGenerationException("Not yet implemented.");
    }

    private static FunctionInstance newPartiallySpecifiedInstance(ProviderData data, int stringIndex, int matrixIndex) {

        StringType stringType = data.getInputType(StringType.class, stringIndex);
        int stringSize = stringType.matrix().getShape().getNumElements();

        String functionName = StrcmpBuilder.NAME_PARTIALLY_CHECKED_PREFIX + stringIndex
                + data.getInputTypes().get(matrixIndex).getSmallId() + "_" + stringSize;

        MatrixType matrixType = data.getInputType(MatrixType.class, matrixIndex);

        String stringName = "in_s";
        String matrixName = "in_m";
        String outName = "out";

        List<String> inputs = new ArrayList<>(2);
        inputs.add(null);
        inputs.add(null);
        inputs.set(stringIndex, stringName);
        inputs.set(matrixIndex, matrixName);

        FunctionType functionType = FunctionType.newInstance(
                inputs,
                data.getInputTypes(),
                outName,
                getNumerics(data).newInt());

        CNode matrixNode = CNodeFactory.newVariable(matrixName, matrixType);

        CNode stringSizeNode = CNodeFactory.newCNumber(stringSize);
        CNode matrixSizeNode = CNodeFactory.newVariable("matrix_size", getNumerics(data).newInt());

        CInstructionList body = new CInstructionList(functionType);

        MatrixFunctions matrixFunctions = matrixType.functions();
        body.addAssignment(matrixSizeNode, buildFunction(data, matrixFunctions.numel(), matrixNode));

        CNode returnFalseNode = CNodeFactory.newReturn(CNodeFactory.newCNumber(0));

        CNode sizeCheckCondition = buildFunction(data, COperator.NotEqual, matrixSizeNode, stringSizeNode);
        body.addIf(sizeCheckCondition, returnFalseNode);

        VariableNode inductionVar = CNodeFactory.newVariable("i", getNumerics(data).newInt());
        CNode matrixGetNode = buildFunction(data, matrixType.functions().get(), matrixNode, inductionVar);
        CNode elementCheckCondition = CNodeFactory.newLiteral(matrixGetNode.getCode() + " != in_s[i]");
        CNode forLoopBody = IfNodes.newIfThen(elementCheckCondition, returnFalseNode);
        CNode forLoop = new ForNodes(data).newForLoopBlock(inductionVar, stringSizeNode, forLoopBody);
        body.addInstruction(forLoop);

        body.addReturn(CNodeFactory.newCNumber(1));

        return new InstructionsInstance(functionName, StrcmpBuilder.FILE_NAME, body);
    }

    private static NumericFactory getNumerics(ProviderData data) {
        return data.getNumerics();
    }

    private static CNode buildFunction(ProviderData data, InstanceProvider provider, CNode... nodes) {
        FunctionInstance instance = provider.newCInstance(data.createFromNodes(nodes));

        return CNodeFactory.newFunctionCall(instance, nodes);
    }
}
