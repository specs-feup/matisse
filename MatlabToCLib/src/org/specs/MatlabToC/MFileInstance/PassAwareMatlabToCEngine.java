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

package org.specs.MatlabToC.MFileInstance;

import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.MatlabToC.CodeBuilder.SsaToCBuilder;
import org.specs.MatlabToC.CodeBuilder.SsaToCRules.SsaToCRuleList;
import org.specs.matisselib.ProjectPassCompilationManager;
import org.specs.matisselib.providers.MatlabFunctionTable;
import org.specs.matisselib.typeinference.TypedInstance;
import org.specs.matisselib.unssa.VariableAllocator;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.providers.ResourceProvider;

public class PassAwareMatlabToCEngine implements MatlabToCEngine {

    private final ProjectPassCompilationManager manager;
    private final MatlabFunctionTable functionTable;
    private final VariableAllocator allocator;
    private final SsaToCRuleList ssaToCRules;

    public PassAwareMatlabToCEngine(ProjectPassCompilationManager manager,
	    MatlabFunctionTable functionTable,
	    VariableAllocator allocator,
	    SsaToCRuleList ssaToCRules) {
	Preconditions.checkArgument(manager != null);
	Preconditions.checkArgument(functionTable != null);
	Preconditions.checkArgument(allocator != null);
	Preconditions.checkArgument(ssaToCRules != null);

	this.manager = manager;
	this.functionTable = functionTable;
	this.allocator = allocator;
	this.ssaToCRules = ssaToCRules;
    }

    @Override
    public void addWithCheck(String mainFunctionName, String matlabCode) {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasMatlabFunction(String mainFunctionName) {
	throw new UnsupportedOperationException(mainFunctionName);
    }

    @Override
    public InstructionsInstance newFunctionInstance(String matlabFunction, ProviderData inputData) {
	throw new UnsupportedOperationException(matlabFunction);
    }

    @Override
    public void setCBasefilename(String basefilename) {
	throw new UnsupportedOperationException(basefilename);
    }

    @Override
    public InstructionsInstance newFunctionInstance(ResourceProvider resource, ProviderData data) {
	TypedInstance typedInstance = this.manager.getInstanceFromResource(resource, data);

	return SsaToCBuilder.buildImplementation(this.manager, typedInstance, this.functionTable, this.allocator,
		this.ssaToCRules);
    }

    @Override
    public InstructionsInstance newFunctionInstance(MatlabTemplate template, ProviderData providerData) {
	TypedInstance typedInstance = this.manager.getInstanceFromResource(template.getName(), template.getMCode(),
		providerData);

	return SsaToCBuilder.buildImplementation(this.manager, typedInstance, this.functionTable, this.allocator,
		this.ssaToCRules);
    }

    @Override
    public FunctionType getFunctionType(ResourceProvider resource, ProviderData data) {
	TypedInstance typedInstance = this.manager.getInstanceFromResource(resource, data);

	return typedInstance.getFunctionType();
    }

    @Override
    public FunctionType getFunctionType(MatlabTemplate template, ProviderData data) {
	TypedInstance typedInstance = this.manager.getInstanceFromResource(template.getName(), template.getMCode(),
		data);

	return typedInstance.getFunctionType();
    }

    @Override
    public void forceLoad(ResourceProvider resource) {
	this.manager.forceLoadResource(resource);
    }

    @Override
    public void forceLoad(MatlabTemplate template) {
	this.manager.forceLoadResource(template.getName(), template.getMCode());
    }
}
