/**
 * Copyright 2013 SPeCS Research Group.
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

import java.util.ArrayList;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionInstanceUtils;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodeUtils;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Types.VariableType;
import org.specs.Matisse.Matlab.TypesMap;
import org.specs.MatlabAspects.MatlabAspectsUtils;
import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabToC.Functions.BaseFunctions.BaseResource;
import org.specs.MatlabToC.Functions.MathFunctions.MathResource;
import org.specs.MatlabToC.Functions.MatisseHelperFunctions.HelperFunctionResource;
import org.specs.MatlabToC.Functions.MatlabOpsV2.OpsResource;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.collections.ScopedMap;
import pt.up.fe.specs.util.providers.ResourceProvider;

/**
 * @author Joao Bispo
 * 
 */
public class MFileProvider {

    private static final String RESOURCE_TYPES = "mfiles/types.typedef";
    // private static final MatlabToCEngine ENGINE = buildEngine();
    private static MatlabToCEngine ENGINE = null;

    public static MatlabToCEngine buildEngine(DataStore setup, LanguageMode languageMode) {
        // Get types (including global variables)
        TypesMap types = new TypesMap();

        // Parse file
        MatlabAspectsUtils aspects = new MatlabAspectsUtils(setup);
        ScopedMap<VariableType> libraryTypes = aspects
                .getVariableTypes(SpecsIo.getResource(MFileProvider.RESOURCE_TYPES));
        /*
                // Check for default real
                if (aspects.getDefaultReal().isPresent()) {
                    setup.give().setOption(CirOption.DEFAULT_REAL, aspects.getDefaultReal().get());
                }
        */
        // Add types from file
        if (libraryTypes == null) {
            SpecsLogs.warn(" -> Could not parse types.");

        } else {
            types.addSymbols(libraryTypes);
        }

        // Create engine
        MatlabToCEngine engine = OldMatlabToCEngine.newInstance(getResources(), languageMode, types, setup);

        // Set destination folder for C files created with this engine
        engine.setCBasefilename("libm/");

        return engine;
    }

    /**
     * Resources needed for MFileProvider to work. Used by the old system.
     * 
     * @return
     */
    public static List<ResourceProvider> getResources() {
        List<ResourceProvider> resources = SpecsFactory.newArrayList();

        resources = SpecsFactory.newArrayList();

        resources.addAll(ResourceProvider.getResources(BaseResource.class));
        resources.addAll(ResourceProvider.getResources(MathResource.class));
        resources.addAll(ResourceProvider.getResources(OpsResource.class));
        resources.addAll(ResourceProvider.getResources(HelperFunctionResource.class));

        return resources;
    }

    public static InstanceProvider getProvider(final ResourceProvider resource) {
        return new InstanceProvider() {

            @Override
            public FunctionInstance newCInstance(ProviderData data) {
                return getInstance(resource, data);
            }

            @Override
            public FunctionType getType(ProviderData data) {
                return getFunctionType(resource, data);
            }
        };
    }

    public static InstanceProvider getProvider(final MatlabTemplate template) {
        return new InstanceProvider() {

            @Override
            public FunctionInstance newCInstance(ProviderData data) {
                return getInstance(template, data);
            }

            @Override
            public FunctionType getType(ProviderData data) {
                return getFunctionType(template, data);
            }
        };
    }

    public static InstructionsInstance getInstance(ResourceProvider resource, ProviderData data) {
        MatlabToCEngine engine = getEngine();

        return engine.newFunctionInstance(resource, data);
    }

    public static InstructionsInstance getInstance(String matlabFunction, ProviderData data) {

        MatlabToCEngine engine = getEngine();

        // Set function basename

        InstructionsInstance instance = engine.newFunctionInstance(matlabFunction, data);

        return instance;
        // ENGINE.
        // MFunctionInstance.newInstanceV2(mFunctionName, parentFunctionName, rootToken, data, inputTypes)
    }

    /**
     * 
     * @param template
     * @param builderData
     * @return
     */
    public static InstructionsInstance getInstance(MatlabTemplate template, ProviderData builderData) {
        return getEngine().newFunctionInstance(template, builderData);
    }

    public static FunctionType getFunctionType(ResourceProvider resource, ProviderData data) {
        MatlabToCEngine engine = getEngine();

        return engine.getFunctionType(resource, data);
    }

    public static FunctionType getFunctionType(MatlabTemplate template, ProviderData data) {
        MatlabToCEngine engine = getEngine();

        return engine.getFunctionType(template, data);
    }

    public static void setEngine(MatlabToCEngine engine) {
        MFileProvider.ENGINE = engine;
    }

    /**
     * @return
     */
    public static MatlabToCEngine getEngine() {
        if (MFileProvider.ENGINE == null) {
            throw new IllegalStateException("Engine has not been initialized");
        }

        return MFileProvider.ENGINE;
    }

    /**
     * @param functionName
     * @param inputs
     * @param settings
     * @return
     */
    public static CNode getFunctionCall(String functionName, List<CNode> inputs, ProviderData data) {

        // ProviderData data = ProviderData.newInstance(CTokenUtils.getVariableTypes(inputs), settings, setup);
        ProviderData newData = ProviderData.newInstance(data, CNodeUtils.getVariableTypes(inputs));

        // Get instance
        FunctionInstance instance = getInstance(functionName, newData);

        return FunctionInstanceUtils.getFunctionCall(instance, inputs);
        // return CTokenFactory.newFunctionCall(instance, inputs);
    }

    /**
     * @param template
     * @param setup
     * @param args
     * @param iSettings
     * @return
     */
    public static FunctionCallNode getFunctionCall(MatlabTemplate template, List<CNode> inputs, ProviderData data) {

        List<VariableType> inputTypes = CNodeUtils.getVariableTypes(inputs);

        FunctionInstance instance = getInstance(template, data.create(inputTypes));

        return instance.newFunctionCall(inputs);
    }

    public static CNode getFunctionCallWithOutputsAsInputs(MatlabTemplate template, List<CNode> inputs,
            List<CNode> outputs, ProviderData baseProviderData) {

        List<VariableType> inputTypes = CNodeUtils.getVariableTypes(inputs);
        ProviderData providerData = baseProviderData.create(inputTypes);
        providerData.setNargouts(outputs.size());
        FunctionInstance instance = getInstance(template, providerData);

        List<CNode> arguments = new ArrayList<>();
        arguments.addAll(inputs);
        arguments.addAll(outputs);

        return instance.newFunctionCall(arguments);
    }

    public static void forceLoad(ResourceProvider resource) {
        getEngine().forceLoad(resource);
    }

    // public static void forceLoad(MatlabTemplate template, ProviderData builderData) {
    // getEngine().forceLoad(template);
    // }

    public static InstanceProvider getProviderWithDependencies(ResourceProvider mainResource,
            ResourceProvider... dependencies) {
        return new InstanceProvider() {

            @Override
            public FunctionInstance newCInstance(ProviderData data) {
                loadDependencies(dependencies, data);

                return getInstance(mainResource, data);
            }

            private void loadDependencies(ResourceProvider[] dependencies, ProviderData data) {
                for (ResourceProvider dependency : dependencies) {
                    forceLoad(dependency);
                }
            }

            @Override
            public FunctionType getType(ProviderData data) {
                loadDependencies(dependencies, data);

                return getFunctionType(mainResource, data);
            }
        };
    }

    public static InstanceProvider getProviderWithLibraryDependencies(ResourceProvider mainResource,
            String... includeNames) {
        return new InstanceProvider() {

            @Override
            public FunctionInstance newCInstance(ProviderData data) {
                InstructionsInstance instance = getInstance(mainResource, data);

                instance.setCustomIncludes(includeNames);

                return instance;
            }

            @Override
            public FunctionType getType(ProviderData data) {
                FunctionType functionType = getFunctionType(mainResource, data);

                return functionType;
            }
        };
    }
}
