package org.specs.matisselib.functions.strings;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.AInstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.LiteralInstance;
import org.specs.matisselib.types.strings.MatlabStringStructInstance;
import org.specs.matisselib.types.strings.MatlabStringType;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.lazy.ThreadSafeLazy;

public class CopyMatlabStringInstance extends AInstanceBuilder {

    private static final String FUNCTION_NAME = "matlab_string_copy";
    private static final ThreadSafeLazy<String> CODE = new ThreadSafeLazy<String>(
            () -> SpecsIo.getResource("matisselib/matlab_string_copy.c")
                    .replace("<STRUCT_NAME>", MatlabStringStructInstance.STRUCT_NAME));
    private static final String DECLARATION_FILENAME = "lib/matlabstring";

    public CopyMatlabStringInstance(ProviderData data) {
        super(data);
    }

    @Override
    public FunctionInstance create() {
        FunctionType functionType = FunctionTypeBuilder
                .newWithSingleOutputAsInput()
                .addInput("in", MatlabStringType.STRING_TYPE)
                .addOutputAsInput("out", MatlabStringType.STRING_TYPE)
                .build();

        LiteralInstance instance = new LiteralInstance(functionType,
                FUNCTION_NAME,
                DECLARATION_FILENAME,
                CODE.get());
        instance.setCustomImplementationIncludes("assert.h", "stdio.h", "string.h", "stdlib.h");
        return instance;
    }

    public static InstanceProvider getProvider() {
        return data -> new CopyMatlabStringInstance(data).create();
    }

}
