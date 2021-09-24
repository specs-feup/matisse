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

package org.specs.MatlabToC.Functions.Builtin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Types.TypeShape;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.Types.Views.Code.CodeUtils;
import org.specs.MatlabToC.MatlabCFilename;
import org.specs.MatlabToC.Utilities.MatisseChecker;

import pt.up.fe.specs.util.utilities.Replacer;

public class RandSimple extends AInstanceBuilder {

    private static final MatisseChecker CHECKER = new MatisseChecker()
            // Check if has at least 1 input
            .numOfInputsAtLeast(1)
            // Check if all inputs are scalar
            .areScalar();

    public static InstanceProvider newProvider() {
        return new GenericInstanceProvider(RandSimple.CHECKER, data -> new RandSimple(data).create());

    }

    public RandSimple(ProviderData data) {
        super(data);
    }

    /**
     * Creates a new instance of the function 'rand', which returns uniformly distributed pseudorandom numbers.
     * 
     * <p>
     * If as input it is given the dimension of a matrix with a single element (e.g., 1; [1, 1]) the function returns a
     * scalar. Returns a matrix otherwise.
     * 
     * <p>
     * FunctionCall receives:<br>
     * - A variable number of scalars. The scalars represent the shape of the output matrix. If a single scalar is
     * given, it is interpreted as the size of a square matrix.
     */
    @Override
    public FunctionInstance create() {
        // Get ypes
        List<ScalarType> shapeTypes = getData().getInputTypes(ScalarType.class);

        // If only one argument, adjust for square matrix types
        if (shapeTypes.size() == 1) {
            shapeTypes.add(shapeTypes.get(0));
        }

        // Get the shape of the output
        TypeShape outputShape = getShape(shapeTypes);

        if (outputShape.isScalar()) {
            return newScalarInstance();
        }

        return newMatrixInstance(outputShape);
    }

    private FunctionInstance newScalarInstance() {

        // Input types are always ints, input arguments are not used
        List<VariableType> inputTypes = IntStream.range(0, getData().getNumInputs())
                .mapToObj(anInt -> getNumerics().newInt())
                .collect(Collectors.toList());

        VariableType outputType = getScalarOutputType();

        FunctionType functionTypes = FunctionType.newInstance(createNameList("index", getData().getNumInputs()),
                inputTypes, "return_value", outputType);

        List<VariableType> nameTypes = new ArrayList<>(getData().getInputTypes());
        nameTypes.add(outputType);
        String cFunctionName = getFunctionName("rand_scalar", nameTypes);

        String cFilename = MatlabCFilename.MatlabGeneral.getCFilename();

        // CodeReplacer cBody = new CodeReplacer("return (<CAST_RAND>/<CAST_RANDMAX>);");
        Replacer cBody = new Replacer("   return ((<CAST_TYPE>) rand() /(<CAST_TYPE>) RAND_MAX);\n");
        cBody.replace("<CAST_TYPE>", CodeUtils.getType(outputType));
        /*
        	FunctionInstance castInstance = UtilityInstances.newCastToScalar(getNumerics().newInt(), outputType);
        	CNode castRand = CNodeFactory.newFunctionCall(castInstance, CNodeFactory.newLiteral("rand()"));
        	System.out.println("CAST RAND CODE:" + castRand.getCode());
        	cBody.replace("<CAST_RAND>", castRand);
        
        	CNode castRandMax = CNodeFactory.newFunctionCall(castInstance, CNodeFactory.newLiteral("RAND_MAX"));
        	cBody.replace("<CAST_RANDMAX>", castRandMax);
        */
        LiteralInstance randScalar = new LiteralInstance(functionTypes, cFunctionName, cFilename, cBody.toString());

        randScalar.setCustomImplementationIncludes(SystemInclude.Stdlib);

        return randScalar;
    }

    private VariableType getScalarOutputType() {
        VariableType outputType = getData().getOutputType();
        // If not output type defined, return default real
        if (outputType == null) {
            return getSettings().get(CirKeys.DEFAULT_REAL);
        }

        // If defined type is an integer, return default real
        if (ScalarUtils.isInteger(outputType)) {
            return getSettings().get(CirKeys.DEFAULT_REAL);
        }

        return outputType;
    }

    private static FunctionInstance newMatrixInstance(TypeShape outputShape) {
        throw new RuntimeException("Rand not yet implemented when output is a matrix!");
    }
}
