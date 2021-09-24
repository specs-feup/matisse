package org.specs.MatlabToC.Functions.CustomFunctions;

import org.specs.CIR.FunctionInstance.GenericInstanceProvider;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.MatlabToC.MFileInstance.MFileProvider;
import org.specs.MatlabToC.Utilities.MatisseChecker;

public class DeleteSingleIndex {
    public static InstanceProvider newDeleteSingleIndexMatrix() {
        MatisseChecker checker = new MatisseChecker()
                .isMatrix(0)
                .isMatrix(1)
                .hasConstantValue(2, 0)
                .hasConstantValue(3, 1);

        return new GenericInstanceProvider(checker,
                MFileProvider.getProvider(BuiltinTemplate.DELETE_SINGLE_INDEX_MATRIX));
    }

    public static InstanceProvider newDeleteSingleIndexScalar() {
        MatisseChecker checker = new MatisseChecker()
                .isMatrix(0)
                .isScalar(1)
                .hasConstantValue(2, 0)
                .hasConstantValue(3, 1);

        return new GenericInstanceProvider(checker,
                MFileProvider.getProvider(BuiltinTemplate.DELETE_SINGLE_INDEX_SCALAR));
    }
}
