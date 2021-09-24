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

package org.specs.matisselib;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.OutputData;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.Matisse.Matlab.TypesMap;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabIR.MatlabNodePass.CommonPassData;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.matisselib.passes.ssa.SsaValidatorPass;
import org.specs.matisselib.providers.MatlabFunctionTable;
import org.specs.matisselib.services.DefaultDataProviderService;
import org.specs.matisselib.services.scalarbuilderinfo.SimpleScalarValueInformationBuilderService;
import org.specs.matisselib.services.scalarbuilderinfo.Z3ScalarValueInformationBuilderService;
import org.specs.matisselib.services.systemfunctions.CommonSystemFunctionProviderService;
import org.specs.matisselib.services.typeinformation.CommonTypedInstanceProviderService;
import org.specs.matisselib.ssa.SsaRecipe;
import org.specs.matisselib.typeinference.InferenceRuleList;
import org.specs.matisselib.typeinference.PostTypeInferencePass;
import org.specs.matisselib.typeinference.PostTypeInferenceRecipe;
import org.specs.matisselib.typeinference.ScriptInferenceResult;
import org.specs.matisselib.typeinference.TypeInferencePass;
import org.specs.matisselib.typeinference.TypedInstance;
import org.specs.matisselib.typeinference.TypedInstanceStateList;
import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Datakey.KeyUser;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.collections.MultiMap;
import pt.up.fe.specs.util.providers.ResourceProvider;

public class ProjectPassCompilationManager implements KeyUser, AutoCloseable {
    private static final boolean ENABLE_AGGRESSIVE_VALIDATION = false;

    private final MatlabAstPassManager preTypeInferenceManager;

    private final SsaRecipe ssaRecipe;
    private final PostTypeInferenceRecipe postTypeInferenceRecipe;
    private final InferenceRuleList inferenceRuleList;
    private TypesMap defaultTypes;

    private final MatlabFunctionTable functionTable;
    private final boolean enableZ3;

    private TypeInferencePass typeInference;
    private boolean appliedPostTypeInferencePasses = false;

    private int currentPostTypeInferencePass;

    private final MultiMap<FunctionIdentification, InferenceResult> inferenceResults = new MultiMap<>();

    public ProjectPassCompilationManager(ProjectPassCompilationOptions options) {

        Preconditions.checkArgument(options != null);
        options.validate();

        preTypeInferenceManager = new PreTypeInferencePassManager(options.getLanguageMode(),
                options.getPreTypeInferenceRecipe(),
                options.getAvailableFiles(),
                options.getAdditionalServices());
        ssaRecipe = options.getSsaRecipe();
        postTypeInferenceRecipe = options.getPostTypeInferenceRecipe();
        functionTable = options.getSystemFunctions();
        inferenceRuleList = options.getInferenceRuleList();
        enableZ3 = options.isz3Enabled();
        defaultTypes = options.getDefaultTypes();
    }

    @Override
    public Collection<DataKey<?>> getWriteKeys() {
        return Arrays.asList(ProjectPassServices.TYPED_INSTANCE_PROVIDER, ProjectPassServices.SYSTEM_FUNCTION_PROVIDER);
    }

    public void setDefaultTypes(TypesMap types) {
        defaultTypes = types;
    }

    public PostTypeInferenceRecipe getPostTypeInferenceRecipe() {
        return postTypeInferenceRecipe;
    }

    public void applyPreTypeInferencePasses(String rootFile) {
        Preconditions.checkArgument(rootFile != null);

        preTypeInferenceManager.applyPreTypeInferencePasses(rootFile);
    }

    public TypedInstance applyTypeInference(DataStore setupTable) {
        Preconditions.checkArgument(setupTable != null);
        Preconditions.checkState(preTypeInferenceManager.getAppliedPreTypeInferencePasses(),
                "Can only call applyTypeInference after applyPreTypeInferencePasses");
        Preconditions.checkState(typeInference == null,
                "Can only call applyTypeInference once");

        typeInference = new TypeInferencePass(defaultTypes, setupTable, this);

        return typeInference.inferMainFunctionTypes(preTypeInferenceManager.getRootFunction());
    }

    public ScriptInferenceResult applyScriptTypeInference(DataStore setupTable) {

        TypedInstance instance = applyTypeInference(setupTable);

        TypesMap returnTypes = instance.getReturnVariableTypes();
        TypesMap globalTypes = getGlobalTypes();

        return new ScriptInferenceResult(returnTypes, globalTypes, instance);

    }

    public TypesMap getGlobalTypes() {
        return defaultTypes.getTypesMap(Arrays.asList("global"));
    }

    public String getPassLog() {
        return preTypeInferenceManager.getPassLog();
    }

    public void setErrorReportStream(PrintStream reportStream) {
        preTypeInferenceManager.setReportStream(reportStream);
    }

    public void log(String message) {
        preTypeInferenceManager.log(message);
    }

    public TypedInstance inferFunction(FunctionIdentification functionId, ProviderData data) {
        Preconditions.checkArgument(functionId != null);
        Preconditions.checkArgument(data != null);
        Preconditions.checkArgument(typeInference != null);

        List<InferenceResult> candidates = inferenceResults.get(functionId);
        for (InferenceResult candidate : candidates) {
            if (candidate.accepts(data)) {
                return candidate.instance;
            }
        }

        String arguments = data.getInputTypes()
                .stream()
                .map(v -> v.code().getSimpleType())
                .collect(Collectors.joining(",", "(", ")"));
        log("Applying type inference to " + functionId + arguments);

        FunctionNode function = (FunctionNode) preTypeInferenceManager.getFunctionNode(functionId).get();
        DataStore passData = new CommonPassData(
                preTypeInferenceManager.getPassDataForPreTypeInference(functionId));

        passData.add(ProjectPassServices.TYPED_INSTANCE_PROVIDER, new CommonTypedInstanceProviderService(this));
        passData.add(ProjectPassServices.SYSTEM_FUNCTION_PROVIDER, new CommonSystemFunctionProviderService(this));
        passData.add(ProjectPassServices.SCALAR_VALUE_INFO_BUILDER_PROVIDER,
                enableZ3 ? new Z3ScalarValueInformationBuilderService()
                        : new SimpleScalarValueInformationBuilderService());
        passData.set(ProjectPassServices.GLOBAL_TYPE_PROVIDER,
                name -> Optional.ofNullable(defaultTypes.getSymbol("global", name)));

        TypedInstance instance = typeInference.inferTypes(functionId,
                function,
                passData,
                data,
                ssaRecipe,
                inferenceRuleList);

        passData.add(ProjectPassServices.DATA_PROVIDER, new DefaultDataProviderService(instance, passData));

        List<OutputData> outputData = data.getOutputData();
        outputData = outputData == null ? null : new ArrayList<>(outputData);

        InferenceResult result = new InferenceResult(instance, passData, outputData);
        inferenceResults.put(functionId, result);

        return instance;
    }

    /**
     * Applies the pass of index passId to the function indicated by functionId.
     * 
     * <p>
     * Before applying the pass, retrieves the corresponding PassData for the given function.
     * 
     * @param functionId
     * @param passId
     */
    private void applyCatchUpPass(InferenceResult inferenceResult, int passId) {
        assert inferenceResult != null;
        assert passId >= 0 && passId < postTypeInferenceRecipe.size() : "passId out of range: " + passId;

        PostTypeInferencePass pass = postTypeInferenceRecipe.get(passId);
        log("Applying post-type-inference pass " + passId + " " + pass.getName() + " to "
                + inferenceResult.instance.getFunctionIdentification());

        DataStore data = inferenceResult.instancedPassData;

        try {
            pass.apply(inferenceResult.instance, data);

            runAfterPassCode(inferenceResult.instance, pass, data);
        } catch (RuntimeException e) {
            System.err.println("At:");
            System.err.println(inferenceResult.instance.getFunctionBody());

            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void runAfterPassCode(TypedInstance instance, PostTypeInferencePass pass, DataStore data) {
        if (ProjectPassCompilationManager.ENABLE_AGGRESSIVE_VALIDATION) {
            new SsaValidatorPass("auto-after-" + pass.getName()).apply(instance, data);
        }

        data.get(ProjectPassServices.DATA_PROVIDER).update(pass);
    }

    private void applyPostTypeInferencePass() {
        boolean appliedAnyPass;
        do {
            appliedAnyPass = false;

            // Create new list, since list of inferenceResults can change while applying a pass
            for (FunctionIdentification id : new ArrayList<>(inferenceResults.keySet())) {
                for (InferenceResult result : inferenceResults.get(id)) {
                    while (result.getCurrentPass() < currentPostTypeInferencePass
                            && result.getCurrentPass() < postTypeInferenceRecipe.size()
                            && !result.isInPass()) {
                        appliedAnyPass = true;

                        result.setInPass(true);
                        applyCatchUpPass(result, result.getCurrentPass());
                        result.nextPass();
                        result.setInPass(false);
                    }
                }
            }
        } while (appliedAnyPass);
    }

    public void applyPostTypeInferencePasses() {
        Preconditions.checkState(!appliedPostTypeInferencePasses,
                "Can only call applyPostTypeInferencePasses once");

        appliedPostTypeInferencePasses = true;

        // Now apply the passes themselves
        while (currentPostTypeInferencePass < postTypeInferenceRecipe.size()) {

            PostTypeInferencePass pass = postTypeInferenceRecipe.get(currentPostTypeInferencePass);

            List<InferenceResult> instances = inferenceResults.flatValues();

            for (InferenceResult result : instances) {
                log("Applying post-type-inference pass " + currentPostTypeInferencePass + " " + pass.getName() + " to "
                        + result.instance.getFunctionIdentification());

                assert !result.isInPass();
                result.setInPass(true);
            }

            pass.apply(new TypedInstanceStateList(instances));

            for (InferenceResult result : instances) {
                result.nextPass();

                runAfterPassCode(result.instance, pass, result.instancedPassData);

                assert result.isInPass();
                result.setInPass(false);
            }

            ++currentPostTypeInferencePass;
        }
    }

    public void forceLoadResource(ResourceProvider resource) {
        preTypeInferenceManager.processResource(resource);
    }

    public void forceLoadResource(String name, String code) {
        preTypeInferenceManager.processResource(name, code);
    }

    public TypedInstance getInstanceFromResource(ResourceProvider resource, ProviderData providerData) {
        FunctionIdentification identification = preTypeInferenceManager.processResource(resource);
        TypedInstance typedInstance = inferFunction(identification, providerData);

        applyPostTypeInferencePass();

        return typedInstance;
    }

    public TypedInstance getInstanceFromResource(String name, String code, ProviderData providerData) {
        FunctionIdentification identification = preTypeInferenceManager.processResource(name, code);
        TypedInstance typedInstance = inferFunction(identification, providerData);

        applyPostTypeInferencePass();

        return typedInstance;
    }

    public Optional<InstanceProvider> getSystemFunction(String functionName) {
        return Optional.ofNullable(functionTable.getPrototypes().get(functionName));
    }

    public Optional<MatlabNode> getFunctionNode(FunctionIdentification functionId) {
        return preTypeInferenceManager.getFunctionNode(functionId);
    }

    public TypedInstance getSpecializedUserFunction(FunctionIdentification functionIdentification,
            String functionName,
            ProviderData providerData) {

        FunctionIdentification calledFunction = preTypeInferenceManager
                .getPassDataForPreTypeInference(functionIdentification)
                .get(PreTypeInferenceServices.WIDE_SCOPE)
                .getUserFunction(functionName).get();

        return getSpecializedUserFunction(calledFunction, providerData);
    }

    private TypedInstance getSpecializedUserFunction(FunctionIdentification calledFunction, ProviderData providerData) {
        // FIXME Is this correct?
        TypedInstance instance = inferFunction(calledFunction, providerData);
        return instance;
    }

    public List<TypedInstance> getInferredInstances() {
        List<TypedInstance> instances = new ArrayList<>();

        for (FunctionIdentification key : inferenceResults.keySet()) {
            for (InferenceResult inferenceResult : inferenceResults.get(key)) {
                instances.add(inferenceResult.instance);
            }
        }

        return instances;
    }

    public DataStore getPassData(TypedInstance instance) {
        List<InferenceResult> candidates = inferenceResults.get(instance.getFunctionIdentification());
        for (InferenceResult candidate : candidates) {
            if (candidate.accepts(instance.getProviderData())) {
                return candidate.instancedPassData;
            }
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        for (InferenceResult result : inferenceResults.flatValues()) {

            // for (Object obj : result.instancedPassData.getValuesMap().values()) {
            for (Object obj : result.instancedPassData.getValues()) {
                if (obj instanceof DataStoreOwned) {
                    try {
                        ((AutoCloseable) obj).close();
                    } catch (Exception e) {
                        SpecsLogs.warn("Error message:\n", e);
                    }
                }
            }
        }
    }
}
