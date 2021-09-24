package org.specs.matisselib.types.strings;

import org.specs.CIR.FunctionInstance.Instances.StructInstance;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.lazy.ThreadSafeLazy;

public class MatlabStringStructInstance extends StructInstance {

    public static final String STRUCT_NAME = "matlab_string";
    private static final String DECLARATION_RESOURCE = "matisselib/matlab_string_struct.c";
    private static final ThreadSafeLazy<String> DECLARATION_CODE = new ThreadSafeLazy<>(
            () -> SpecsIo.getResource(DECLARATION_RESOURCE)
                    .replace("<STRUCT_NAME>", STRUCT_NAME));

    public MatlabStringStructInstance() {
        super(STRUCT_NAME, MatlabStringType.DECLARATION_FILENAME, getCode());
    }

    private static String getCode() {
        return DECLARATION_CODE.get();
    }

}
