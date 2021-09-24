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

package org.specs.matisselib.services.typeinformation;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.matisselib.ProjectPassCompilationManager;
import org.specs.matisselib.services.TypedInstanceProviderService;
import org.specs.matisselib.typeinference.TypedInstance;

import com.google.common.base.Preconditions;

public class CommonTypedInstanceProviderService implements TypedInstanceProviderService {

    private final ProjectPassCompilationManager manager;

    public CommonTypedInstanceProviderService(ProjectPassCompilationManager manager) {
	Preconditions.checkArgument(manager != null);

	this.manager = manager;
    }

    @Override
    public TypedInstance getTypedInstance(FunctionIdentification userFunction, ProviderData providerData) {
	return manager.inferFunction(userFunction, providerData);
    }
}
