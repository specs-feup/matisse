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

import pt.up.fe.specs.util.providers.ResourceProvider;

/**
 * @see MFileProvider
 */
public interface MatlabToCEngine {
    public void addWithCheck(String mainFunctionName, String matlabCode);

    public boolean hasMatlabFunction(String mainFunctionName);

    public InstructionsInstance newFunctionInstance(String matlabFunction, ProviderData inputData);

    public InstructionsInstance newFunctionInstance(ResourceProvider resource, ProviderData data);

    public void setCBasefilename(String basefilename);

    public FunctionType getFunctionType(ResourceProvider resource, ProviderData data);

    public FunctionType getFunctionType(MatlabTemplate template, ProviderData data);

    public void forceLoad(ResourceProvider resource);

    public void forceLoad(MatlabTemplate template);

    public InstructionsInstance newFunctionInstance(MatlabTemplate template, ProviderData providerData);
}
