package org.specs.matlabtocl.v2.functions.builtins;

import java.util.List;

import org.specs.CIR.CodeGenerator.CodeGeneratorUtils;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Types.VariableType;
import org.specs.matlabtocl.v2.codegen.RequiredCLExtensionAnnotation;

public enum CLGroupReduceFunction implements InstanceProvider {
    WORK_GROUP_REDUCE_ADD("work_group_reduce_add", null),
    SUB_GROUP_REDUCE_ADD("sub_group_reduce_add", "cl_khr_subgroups");

    private final String functionName;
    private final String extension;

    private CLGroupReduceFunction(String functionName, String extension) {
        this.functionName = functionName;
        this.extension = extension;
    }

    @Override
    public FunctionInstance newCInstance(ProviderData data) {
        List<VariableType> inputTypes = data.getInputTypes();
        VariableType outputType = inputTypes.get(0);

        InlineCode code = arguments -> {
            return CodeGeneratorUtils.functionCallCode(functionName, inputTypes, arguments);
        };

        FunctionTypeBuilder functionTypeBuilder = FunctionTypeBuilder
                .newInline()
                .addInputs(inputTypes)
                .returning(outputType);
        if (extension != null) {
            functionTypeBuilder.addAnnotation(new RequiredCLExtensionAnnotation(extension));
        }
        FunctionType functionType = functionTypeBuilder
                .build();

        InlinedInstance instance = new InlinedInstance(functionType, functionName, code);
        instance.setCallPrecedenceLevel(PrecedenceLevel.FunctionCall);
        return instance;
    }
}
