package org.specs.MatlabToC.Functions.BaseFunctions.General;

import java.util.Arrays;
import java.util.Optional;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.CNumberNode;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;

public class EmptyAllocProvider implements InstanceProvider {

    private static final String FUNCTION_NAME = "make_empty";
    private static final String FILENAME = "lib/matrix";

    @Override
    public Optional<InstanceProvider> accepts(ProviderData data) {
        if (!data.getInputTypes().isEmpty()) {
            return Optional.empty();
        }

        if (data.getOutputType() == null) {
            return Optional.empty();
        }

        if (!(data.getOutputType() instanceof MatrixType)) {
            // Don't know how to handle this case.
            return Optional.empty();
        }

        return InstanceProvider.super.accepts(data);
    }

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
        MatrixType returnType = (MatrixType) data.getOutputType();

        FunctionType functionType = FunctionTypeBuilder
                .newWithSingleOutputAsInput()
                .addOutputAsInput("out", returnType)
                .build();

        InstanceProvider createProvider = returnType
                .functions()
                .create();

        VariableNode outVar = CNodeFactory.newVariable("out", returnType);

        CInstructionList body = new CInstructionList(functionType);
        CNumberNode zeroNode = CNodeFactory.newCNumber(0);
        if (returnType.matrix().usesDynamicAllocation()) {
            body.addInstruction(FunctionInstanceUtils.getFunctionCall(createProvider,
                    data,
                    Arrays.asList(zeroNode, zeroNode),
                    Arrays.asList(outVar)));
        }
        body.addReturn(outVar);

        InstructionsInstance instance = new InstructionsInstance(FUNCTION_NAME + "_" + returnType.getSmallId(),
                FILENAME, body);
        return instance;
    }
}
