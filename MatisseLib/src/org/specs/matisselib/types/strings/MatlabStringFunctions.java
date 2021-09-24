package org.specs.matisselib.types.strings;

import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.Types.CommonFunctions;
import org.specs.matisselib.functions.strings.CopyMatlabStringInstance;
import org.specs.matisselib.functions.strings.FreeMatlabStringInstance;

public class MatlabStringFunctions implements CommonFunctions {
    @Override
    public InstanceProvider free() {
        return FreeMatlabStringInstance.getProvider();
    }

    @Override
    public InstanceProvider assign() {
        return CopyMatlabStringInstance.getProvider();
    }
}
