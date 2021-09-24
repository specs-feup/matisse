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

package org.specs.matisselib.services.systemfunctions;

import java.util.Optional;

import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.matisselib.ProjectPassCompilationManager;
import org.specs.matisselib.services.SystemFunctionProviderService;

import com.google.common.base.Preconditions;

public class CommonSystemFunctionProviderService implements SystemFunctionProviderService {

    private ProjectPassCompilationManager manager;

    public CommonSystemFunctionProviderService(ProjectPassCompilationManager manager) {
	Preconditions.checkArgument(manager != null);

	this.manager = manager;
    }

    @Override
    public Optional<InstanceProvider> getSystemFunction(String functionName) {
	Preconditions.checkArgument(functionName != null);

	return manager.getSystemFunction(functionName);
    }

}
