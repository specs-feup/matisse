package org.specs.MatlabToC.Functions.MatlabOps;

import java.util.List;
import java.util.Optional;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.matisselib.providers.MatlabFunction;
import org.specs.matisselib.providers.MatlabFunctionProvider;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * Same a {@link ElementWiseBuilder} but allows scalar inputs too.
 * 
 * @author Pedro Pinto
 * 
 */
public class ElementWiseScalarBuilder implements InstanceProvider {

    // The prototype for the function or operator and its arity
    private final MatlabFunctionProvider function;
    private final int arity;
    private final boolean firstIsMatrix;

    // The inputs that are matrices
    // private final List<VariableType> matrices;

    public ElementWiseScalarBuilder(MatlabFunctionProvider function, int arity) {
	this(function, arity, false);
    }

    public ElementWiseScalarBuilder(MatlabFunctionProvider function, int arity, boolean firstIsMatrix) {
	super();

	if (arity < 1) {
	    throw new IllegalArgumentException("Arity should have the value of 1 or more.");
	}

	this.function = function;
	this.arity = arity;
	this.firstIsMatrix = firstIsMatrix;
    }

    private List<VariableType> getNormalizedInputs(ProviderData builderData) {
	// Get the input types
	List<VariableType> inputTypes = builderData.getInputTypes();

	// Check the arity of the operator / function
	if (inputTypes.size() != arity) {
	    return null;
	}

	// At least one of the inputs should be a matrix.
	List<VariableType> matrices = getMatrices(inputTypes);

	// If not matrices are found, return
	if (matrices.size() == 0) {
	    return null;
	}

	// If all inputs are matrices, return (no scalar input was found)
	if (matrices.size() == arity) {
	    return null;
	}

	// Check if the first element is a matrix
	if (firstIsMatrix) {
	    if (!MatrixUtils.isMatrix(inputTypes.get(0))) {
		return null;
	    }
	}

	// All inputs must be of a numeric type
	List<VariableType> normalizedInputs = SpecsFactory.newArrayList();
	for (VariableType inputType : inputTypes) {
	    VariableType normalizedInput = ScalarUtils.toScalar(inputType);
	    if (normalizedInput == null) {
		return null;
	    }

	    normalizedInputs.add(normalizedInput);
	}

	// If declared, all matrices must have the same size
	VariableType firstMatrix = matrices.get(0);
	if (MatrixUtils.isStaticMatrix(firstMatrix)) {
	    int firstLength = MatrixUtils.getShape(firstMatrix).getNumElements();
	    for (int i = 1; i < matrices.size(); i++) {
		VariableType otherMatrix = matrices.get(i);
		int otherLength = MatrixUtils.getShape(otherMatrix).getNumElements();

		if (firstLength != otherLength) {
		    return null;
		}
	    }

	}
	return normalizedInputs;
    }

    private static List<VariableType> getMatrices(List<VariableType> inputTypes) {
	List<VariableType> matrices = SpecsFactory.newArrayList();
	for (VariableType inputType : inputTypes) {
	    if (MatrixUtils.isMatrix(inputType)) {
		/*
		if(!MatrixUtils.isDeclaredMatrix(inputType)) {
		    return null;
		}
		*/
		matrices.add(inputType);
	    }
	}
	return matrices;
    }

    /*
        @Override
        public FunctionInstance create(ProviderData builderData) {

    	FunctionInstance baseFunction = checkRule(builderData);
    	if (baseFunction == null) {
    	    return null;
    	}

    	// The input types of the implementation
    	// List<VariableType> inputTypes = builderData.getInputTypes();

    	FunctionInstance instance = ElementWiseInstance.newProvider(baseFunction).newCInstance(builderData);

    	return instance;
        }
    */
    @Override
    public Optional<InstanceProvider> accepts(ProviderData data) {
	if (getNormalizedInputs(data) == null) {
	    return Optional.empty();
	}

	return Optional.of(this);
    }

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
	MatlabFunction fProto = function.getMatlabFunction();
	FunctionInstance fImpl = fProto.getCheckedInstance(data.create(getNormalizedInputs(data)));

	return ElementWiseInstance.newProvider(fImpl).newCInstance(data);
    }

}
