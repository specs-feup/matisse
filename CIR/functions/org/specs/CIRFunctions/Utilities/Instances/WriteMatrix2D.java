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

package org.specs.CIRFunctions.Utilities.Instances;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIRFunctions.Utilities.UtilityResource;
import org.specs.CIRTypes.Types.String.StringType;
import org.specs.CIRTypes.Types.Void.VoidType;

import pt.up.fe.specs.util.utilities.Replacer;

public class WriteMatrix2D extends AInstanceBuilder {

    static private final String INPUT_MATRIX = "matrix";
    static private final String INPUT_FILENAME = "filename";

    public WriteMatrix2D(ProviderData data) {
        super(data);
    }

    @Override
    public FunctionInstance create() {

        MatrixType inputType = getData().getInputType(MatrixType.class, 0);
        StringType stringType = newStringType(true);

        // Build FunctionType
        List<String> inputNames = Arrays.asList(WriteMatrix2D.INPUT_MATRIX, WriteMatrix2D.INPUT_FILENAME);
        List<VariableType> inputTypes = Arrays.asList(inputType, stringType);
        FunctionType type = FunctionType.newInstance(inputNames, inputTypes, "dummy", VoidType.newInstance());

        String functionName = "write_matrix_2d_" + inputType.getSmallId();
        String filename = UtilityResource.getLibFilename();

        Replacer body = new Replacer(UtilityResource.WRITE_MATRIX_2D);

        // MatrixNodes matrixNodes = new MatrixNodes(getSetup());

        Variable matrixVar = new Variable(WriteMatrix2D.INPUT_MATRIX, inputType);

        CNode numelCall = getNodes().matrix().numel(matrixVar);
        body.replace("<NUMEL>", numelCall.getCode());
        body.replace("<MATRIX>", matrixVar.getName());

        CNode getI1Call;
        CNode getIJCall = null;

        ScalarType doubleType = getData().getNumerics().newDouble();

        if (inputType.getTypeShape().getNumDims() < 2) {
            // FIXME: HACK
            getI1Call = getNodes().matrix().get(matrixVar, "i");
            body.replace("<GET_i_0>", doubleType.conversion().to(getI1Call, doubleType).getCode());

            body.replace("<GET_i_j>", "0.0");
        } else {
            getI1Call = getNodes().matrix().get(matrixVar, "i", "zero");
            body.replace("<GET_i_0>", doubleType.conversion().to(getI1Call, doubleType).getCode());

            getIJCall = getNodes().matrix().get(matrixVar, "i", "j");
            body.replace("<GET_i_j>", doubleType.conversion().to(getIJCall, doubleType).getCode());
        }

        CNode numDimsCall = getNodes().matrix().numDims(matrixVar);
        body.replace("<MATRIX_DIMS>", numDimsCall.getCode());

        CNode getDim0 = getNodes().matrix().getDim(matrixVar, 0);
        body.replace("<SIZE_X_1>", getDim0.getCode());

        CNode getDim1 = getNodes().matrix().getDim(matrixVar, 1);
        body.replace("<SIZE_X_2>", getDim1.getCode());

        LiteralInstance instance = new LiteralInstance(type, functionName, filename, body.toString());

        // Complete instance
        instance.setImplementationIncludesFromName("fopen", "printf", "exit", "fprintf", "fclose");
        if (getIJCall != null) {
            instance.getCustomImplementationInstances().add(getIJCall);
        }
        instance.getCustomImplementationInstances().add(getI1Call, numDimsCall, getDim0, getDim1);

        return instance;
    }

}
