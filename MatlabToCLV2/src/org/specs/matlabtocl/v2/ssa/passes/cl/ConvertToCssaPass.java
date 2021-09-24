/**
 * Copyright 2015 SPeCS.
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

package org.specs.matlabtocl.v2.ssa.passes.cl;

import java.util.Optional;

import org.specs.CIR.Types.VariableType;
import org.specs.matisselib.helpers.NameUtils;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matlabtocl.v2.CLServices;
import org.specs.matlabtocl.v2.services.cl.TemporaryAllocatorService;
import org.specs.matlabtocl.v2.services.cl.TypeGetterService;
import org.specs.matlabtocl.v2.ssa.CLPass;
import org.suikasoft.jOptions.Interfaces.DataStore;

public class ConvertToCssaPass implements CLPass {

    @Override
    public void apply(FunctionBody body,
            DataStore passData) {

        TypeGetterService typeGetter = passData.get(CLServices.TYPE_GETTER);
        assert typeGetter != null;
        TemporaryAllocatorService temporaryAllocatorService = passData.get(CLServices.TEMPORARY_ALLOCATOR);
        assert temporaryAllocatorService != null;

        org.specs.matisselib.passes.ssa.ConvertToCssaPass.apply(body, passData, (oldName, targetName) -> {
            assert oldName != null;
            assert targetName != null;

            Optional<VariableType> variableType = typeGetter.getType(targetName);

            String newName = temporaryAllocatorService.makeTemporary(NameUtils.getSuggestedName(oldName),
                    variableType.get());

            return newName;
        });
    }

}
