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

package org.specs.matisselib.passmanager;

import java.io.PrintStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.matisselib.MatisseInit;
import org.specs.matisselib.MatlabAstPassManager;
import org.specs.matisselib.MatlabRecipe;
import org.specs.matisselib.passmanager.data.PassManagerData;
import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Datakey.KeyFactory;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.exceptions.NotImplementedException;
import pt.up.fe.specs.util.providers.ResourceProvider;

public class PreTypeInferenceManagerV2 implements MatlabAstPassManager {

    private static final String SETUP_NAME = "Pretype-inference";

    /**
     * Context for pre-type passes.
     */
    public static final DataKey<PreTypeContext> PRETYPE_PASS_CONTEXT = KeyFactory.object("pretype_pass_context",
            PreTypeContext.class);

    // public PreTypeInferencePassManager(Recipe recipe, PassData passData) {
    /*
    public PreTypeInferenceManager(Recipe recipe, Map<String, StringProvider> availableFiles) {
    this(recipe, new MatisseInit().newPassData(availableFiles));
    }
    
    
    public PreTypeInferenceManager(Recipe recipe, CleanSetup setup) {
    super(recipe, PRETYPE_PASS_CONTEXT, new DefaultPassManagerData("Pre type-inference", setup));
    }
    */

    private final PassManager passManager;
    private final MatlabRecipe recipe;
    private final DataStore data;

    private boolean appliedPreTypeInferencePasses;
    private Optional<PassManagerData> passData;
    private StringBuilder passLog = new StringBuilder();

    public PreTypeInferenceManagerV2(MatlabRecipe recipe, DataStore data) {
        passManager = new GenericPassManager(PRETYPE_PASS_CONTEXT, SETUP_NAME);

        this.recipe = recipe;
        this.data = data;

        appliedPreTypeInferencePasses = false;
        passData = Optional.empty();
    }

    /*
    private PreTypeInferenceManagerV2(MatlabRecipe recipe, DataStore data, Optional<PassManagerData> passData) {
    
    }
    */

    /*
    public PreTypeInferenceManager() {
    super(PRETYPE_PASS_CONTEXT, SETUP_NAME);
    }
    */

    @Override
    public PassManagerData getPassData() {
        if (!passData.isPresent()) {
            throw new RuntimeException("No result is present. Have the passes run?");
        }

        return passData.get();
    }

    @Override
    public PassManager getPassManager() {
        return passManager;
    }

    @Override
    public boolean getAppliedPreTypeInferencePasses() {
        return appliedPreTypeInferencePasses;
    }

    @Override
    public FunctionIdentification getRootFunction() {
        Preconditions.checkState(appliedPreTypeInferencePasses);

        return passManager.getRootFunction();
    }

    @Override
    public DataStore getPassDataForPreTypeInference(FunctionIdentification functionId) {
        return getPassData().newData(Optional.of(functionId)).get();
    }

    @Override
    public Optional<MatlabNode> getFunctionNode(FunctionIdentification functionId) {
        Preconditions.checkArgument(functionId != null);

        return Optional.of(passManager.getUnit(functionId, getPassData()));
    }

    @Override
    public boolean hasFunctionNode(FunctionIdentification functionId) {
        Preconditions.checkArgument(functionId != null);

        return getFunctionNode(functionId).isPresent();
    }

    @Override
    public Optional<List<FunctionIdentification>> getFunctionsIn(String file) {
        Preconditions.checkArgument(file != null, "file must not be null.");

        // Get potential file node
        Optional<FileNode> fileNode = getPassDataForPreTypeInference(new FunctionIdentification(file))
                .get(MatisseInit.MFILES_SERVICE).getFileNode(file);

        // No filenode found, return empty
        if (!fileNode.isPresent()) {
            return Optional.empty();
        }

        // Create a FunctionIdentification for each function inside file
        List<FunctionIdentification> functionsList = fileNode.get().getFunctions().stream()
                .map(f -> new FunctionIdentification(file, f.getFunctionName()))
                .collect(Collectors.toList());

        return Optional.of(functionsList);
    }

    @Override
    public void applyPreTypeInferencePasses(String rootFile) {
        Preconditions.checkArgument(rootFile != null);
        Preconditions.checkState(!appliedPreTypeInferencePasses, "Can only call applyPreTypeInferencePasses once");

        appliedPreTypeInferencePasses = true;

        passData = Optional.of(passManager.applyPasses(rootFile, recipe, data));
    }

    @Override
    public FunctionIdentification processResource(ResourceProvider resource) {
        throw new NotImplementedException("Not implemented for class '" + getClass() + "'");
        /*
        String resourceName = resource.getResourceName();
        FunctionIdentification functionId = new FunctionIdentification(resourceName);
        
        if (hasFunctionNode(functionId)) {
        return functionId;
        }
        
        if (parsedFunctions.containsKey(functionId)) {
        return functionId;
        }
        
        DataStore passData = getPassDataForPreTypeInference(functionId);
        UserFileProviderService files = passData.get(MatisseInit.MFILES_SERVICE);
        files.addResourceFile(resourceName, resource);
        
        FileNode fileNode = files.getFileNodeSafe(resourceName);
        for (FunctionNode function : fileNode.getFunctions()) {
        FunctionIdentification key = new FunctionIdentification(resourceName, function.getFunctionName());
        
        parsedFunctions.put(key, function);
        parsedIds.add(key);
        currentPass.put(key, 0);
        }
        
        applyPreTypeInferencePass();
        
        return functionId;
        */
    }

    @Override
    public FunctionIdentification processResource(String name, String code) {
        return null;
    }

    /**
     * Convenience method which creates a new instance from a recipe and a setup.
     * 
     * @param recipe
     * @param setup
     * @return
     */
    /*
    public static PreTypeInferenceManager newInstance(Recipe recipe, CleanSetup setup) {
    
    DefaultPassManagerData managerData = new DefaultPassManagerData(SETUP_NAME,
    	PassManagerUtils.newProvider(SETUP_NAME));
    
    // Add setup
    managerData.setValues(setup);
    // Create general options
    // PassData generalData = new MatisseInit().newPassData(availableFiles);
    
    return new PreTypeInferenceManager(recipe, managerData);
    // managerData.
    }
    */

    @Override
    public void log(String message) {
        passLog.append(message + "\n");
    }

    @Override
    public String getPassLog() {
        return passLog.toString();
    }

    @Override
    public void setReportStream(PrintStream reportStream) {
        data.set(MatisseInit.PRINT_STREAM, reportStream);
    }

}
