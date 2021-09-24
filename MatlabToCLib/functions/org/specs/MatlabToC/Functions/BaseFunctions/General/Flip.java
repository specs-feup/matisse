package org.specs.MatlabToC.Functions.BaseFunctions.General;

import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.MatlabToC.Functions.BaseFunctions.BaseResource;
import org.specs.MatlabToC.MFileInstance.MFileProvider;
import org.specs.MatlabToC.Utilities.MatisseChecker;

public class Flip {
    public static InstanceProvider newFlip1dProvider() {
        MatisseChecker checker = new MatisseChecker()
                .numOfInputs(1)
                .is1DMatrix(0);

        return new GenericInstanceProvider(checker, MFileProvider.getProvider(BaseResource.FLIP1D));
    }
}
