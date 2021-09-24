package org.specs.MatlabToC.Functions;

import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.MatlabToC.Functions.FileIO.FcloseProvider;
import org.specs.MatlabToC.Functions.FileIO.FopenProvider;
import org.specs.MatlabToC.Functions.FileIO.MatisseFgetl;
import org.specs.MatlabToC.MatlabFunction.MatlabFunctionProviderEnum;

public enum MatlabIO implements MatlabFunctionProviderEnum {
    FOPEN("fopen") {
        @Override
        public List<InstanceProvider> getProviders() {
            return Arrays.asList(FopenProvider.getProvider());
        }
    },

    FCLOSE("fclose") {
        @Override
        public List<InstanceProvider> getProviders() {
            return Arrays.asList(FcloseProvider.getProvider());
        }
    },

    MATISSE_FGETL("matisse_fgetl") {
        @Override
        public List<InstanceProvider> getProviders() {
            return Arrays.asList(MatisseFgetl.getProvider());
        }
    };

    private final String matlabFunctionName;

    /**
     * Declare 'getBuilders' abstract, so that it can be implemented by each enumeration field.
     * 
     * @return
     */
    @Override
    public abstract List<InstanceProvider> getProviders();

    /**
     * Constructor.
     * 
     * @param matlabFunctionName
     */
    private MatlabIO(String matlabFunctionName) {
        this.matlabFunctionName = matlabFunctionName;
    }

    @Override
    public String getName() {
        return this.matlabFunctionName;
    }
}
