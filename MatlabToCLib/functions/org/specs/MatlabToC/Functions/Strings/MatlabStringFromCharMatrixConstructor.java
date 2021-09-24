package org.specs.MatlabToC.Functions.Strings;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.matisselib.helpers.MatisseInputsChecker;
import org.specs.matisselib.types.strings.MatlabStringStructInstance;
import org.specs.matisselib.types.strings.MatlabStringType;

import pt.up.fe.specs.util.SpecsIo;

public class MatlabStringFromCharMatrixConstructor extends AInstanceBuilder {

    private static final String FUNCTION_NAME = "string_from_char_matrix";
    private static final String DECLARATION_FILENAME = "lib/matlabstring";

    public MatlabStringFromCharMatrixConstructor(ProviderData data) {
        super(data);
    }

    @Override
    public FunctionInstance create() {
        MatrixType inputType = getData().getInputType(MatrixType.class, 0);
        MatlabStringType outputType = MatlabStringType.STRING_TYPE;

        String body = SpecsIo.getResource(MatlabStringFunctionResources.STRING_FROM_CHAR_MATRIX);
        body = body.replace("<STRUCT_NAME>", MatlabStringStructInstance.STRUCT_NAME);

        CNode str = CNodeFactory.newVariable("str", inputType);
        CNode i = CNodeFactory.newVariable("i", getNumerics().newInt());

        InstanceProvider numelProvider = inputType.functions().numel();
        FunctionCallNode numelCall = FunctionInstanceUtils.getFunctionCall(numelProvider, getData(), str);
        body = body.replace("<LENGTH>", numelCall.getCode());

        InstanceProvider getProvider = inputType.functions().get();
        FunctionCallNode getCall = FunctionInstanceUtils.getFunctionCall(getProvider, getData(), str, i);
        body = body.replace("<GET_DATA_i>", getCall.getCode());

        FunctionType functionType = FunctionTypeBuilder.newWithSingleOutputAsInput()
                .addInput("str", inputType)
                .addOutputAsInput("outStr", outputType)
                .build();

        LiteralInstance instance = new LiteralInstance(
                functionType,
                FUNCTION_NAME,
                DECLARATION_FILENAME,
                body);
        instance.setCustomImplementationIncludes("stdio.h", "stdlib.h");
        instance.addInstance(numelCall.getFunctionInstance());
        instance.addInstance(getCall.getFunctionInstance());
        return instance;
    }

    public static InstanceProvider getInstanceProvider() {

        MatisseInputsChecker checker = new MatisseInputsChecker()
                .numOfOutputsAtMost(1)
                .numOfInputs(1)
                .isMatrix(0);
        // FIXME: Check that type of input 0 is char array.

        return new GenericInstanceProvider(checker, data -> new MatlabStringFromCharMatrixConstructor(data).create());
    }
}
