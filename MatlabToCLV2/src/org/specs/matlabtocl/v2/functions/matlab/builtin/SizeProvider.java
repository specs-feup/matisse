package org.specs.matlabtocl.v2.functions.matlab.builtin;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.matlabtocl.v2.functions.builtins.CLBinaryOperator;
import org.specs.matlabtocl.v2.types.kernel.CLNativeType;

public class SizeProvider implements MatlabInstanceProvider {
    @Override
    public boolean checkRule(ProviderData data) {
	if (data.getInputTypes().size() != 2) {
	    return false;
	}

	if (!(data.getInputTypes().get(0) instanceof MatrixType)) {
	    return false;
	}

	if (!(data.getInputTypes().get(1) instanceof ScalarType)) {
	    return false;
	}

	return true;
    }

    @Override
    public FunctionInstance create(ProviderData providerData) {
	MatrixType matrixType = providerData.getInputType(MatrixType.class, 0);
	CLNativeType dimType = providerData.getInputType(CLNativeType.class, 1);

	Number constant = dimType.getConstant();
	Number zeroBasedConstant;
	if (constant == null) {
	    zeroBasedConstant = null;
	} else {
	    zeroBasedConstant = constant.intValue() - 1;
	}
	CLNativeType zeroBasedDimType = dimType.setConstant(zeroBasedConstant);

	ProviderData dimData = providerData.create(matrixType, zeroBasedDimType);

	InstanceProvider dimProvider = matrixType.matrix().functions().getDim();
	VariableType returnType = dimProvider.getType(dimData).getOutputTypes().get(0);

	FunctionType functionType = FunctionTypeBuilder.newInline()
		.addInput(matrixType)
		.addInput(dimType)
		.returning(returnType)
		.build();

	InlineCode code = tokens -> {

	    CNode oneBasedDimNode = tokens.get(1);
	    CNode oneNode = CNodeFactory.newCNumber(1, CLNativeType.INT);

	    ProviderData subtractionData = providerData.createFromNodes(oneBasedDimNode, oneNode);

	    CNode zeroBasedDimNode = CLBinaryOperator.SUBTRACTION
		    .getCheckedInstance(subtractionData)
		    .newFunctionCall(oneBasedDimNode, oneNode);
	    return dimProvider.newCInstance(dimData).newFunctionCall(tokens.get(0), zeroBasedDimNode).getCode();
	};

	InlinedInstance instance = new InlinedInstance(functionType,
		"size$" + matrixType.getSmallId() + "_" + dimType.getSmallId(),
		code);
	instance.setCallPrecedenceLevel(PrecedenceLevel.MemberAccessThroughPointer); // FIXME?
	return instance;
    }
}
