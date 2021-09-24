/**
 * Copyright 2016 SPeCS.
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

package org.specs.matisselib.tests;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.matisselib.helpers.BlockEditorHelper;
import org.specs.matisselib.services.SystemFunctionProviderService;
import org.specs.matisselib.ssa.FunctionBody;
import org.specs.matisselib.ssa.SsaBlock;
import org.specs.matisselib.typeinference.TypedInstance;

public class FunctionComposer {
    public static TypedInstance create(Map<String, InstanceProvider> functions,
            Consumer<BlockEditorHelper> codeBuilder) {
        FunctionBody body = new FunctionBody();
        body.addBlock(new SsaBlock());

        TypedInstance instance = new TypedInstance(
                new FunctionIdentification("test.m"),
                Collections.emptyList(),
                body,
                () -> "No code available",
                ProviderData.newInstance("test"));

        SystemFunctionProviderService systemFunctions = new TestFunctionProviderService(functions);
        BlockEditorHelper editor = new BlockEditorHelper(instance, systemFunctions, 0);

        codeBuilder.accept(editor);

        return instance;
    }
}
