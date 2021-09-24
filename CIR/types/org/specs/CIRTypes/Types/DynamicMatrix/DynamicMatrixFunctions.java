/**
 * Copyright 2014 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

package org.specs.CIRTypes.Types.DynamicMatrix;

import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.ATypes.Matrix.MatrixFunctions;
import org.specs.CIRFunctions.Common.CreateAndCopyMatrixInstanceProvider;
import org.specs.CIRTypes.Types.DynamicMatrix.Functions.ChangeShape;
import org.specs.CIRTypes.Types.DynamicMatrix.Functions.Create;
import org.specs.CIRTypes.Types.DynamicMatrix.Functions.CreateFromMatrix;
import org.specs.CIRTypes.Types.DynamicMatrix.Functions.Data;
import org.specs.CIRTypes.Types.DynamicMatrix.Functions.Free;
import org.specs.CIRTypes.Types.DynamicMatrix.Functions.Get;
import org.specs.CIRTypes.Types.DynamicMatrix.Functions.GetDim;
import org.specs.CIRTypes.Types.DynamicMatrix.Functions.NumDims;
import org.specs.CIRTypes.Types.DynamicMatrix.Functions.Numel;
import org.specs.CIRTypes.Types.DynamicMatrix.Functions.SetDynamic;

/**
 * TODO: numel, get and set to not have input checkers.
 * 
 * @author Joao Bispo
 * 
 */
public class DynamicMatrixFunctions implements MatrixFunctions {

    private static final String FILENAME = "lib/dynamic_matrix";

    public static String getFilename() {
        return FILENAME;
    }

    /* (non-Javadoc)
     * @see org.specs.CIRv2.Types.ATypes.Matrix.AMatrixFunctions#numel()
     */
    @Override
    public InstanceProvider numel() {
        return (ProviderData data) -> new Numel(data).create();
    }

    @Override
    public InstanceProvider data() {
        return (ProviderData data) -> new Data(data).create();
    }

    @Override
    public InstanceProvider numDims() {
        return (ProviderData data) -> new NumDims(data).create();
    }

    @Override
    public InstanceProvider get() {
        return (ProviderData data) -> new Get(data).create();
    }

    @Override
    public InstanceProvider set() {
        return (ProviderData data) -> new SetDynamic(data).create();
    }

    @Override
    public InstanceProvider free() {
        return Free.getProvider();
    }

    @Override
    public InstanceProvider createFromMatrix() {
        return CreateFromMatrix.getProvider();
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Types.ATypes.Matrix.MatrixFunctions#getDim()
     */
    @Override
    public InstanceProvider getDim() {
        return GetDim.getProvider();
    }

    @Override
    public InstanceProvider create() {
        return Create.getProvider();
    }

    @Override
    public InstanceProvider changeShape() {
        return ChangeShape.getProvider();
    }

    @Override
    public InstanceProvider assign() {
        return new CreateAndCopyMatrixInstanceProvider();
    }
}
