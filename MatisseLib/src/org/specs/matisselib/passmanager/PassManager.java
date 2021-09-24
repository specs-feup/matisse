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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.MatlabUnitNode;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.MatlabIR.MatlabNodePass.interfaces.MatlabNodePass;
import org.specs.matisselib.MatisseInit;
import org.specs.matisselib.MatlabRecipe;
import org.specs.matisselib.passmanager.data.DefaultPassManagerData;
import org.specs.matisselib.passmanager.data.PassManagerData;
import org.specs.matisselib.services.DirectiveParser;
import org.specs.matisselib.services.TokenReportingService;
import org.specs.matisselib.servicesv2.ScopeService;
import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Datakey.KeyFactory;
import org.suikasoft.jOptions.Datakey.KeyUser;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.google.common.base.Preconditions;

public abstract class PassManager implements KeyUser {

    /**
     * Reporter which display information using a node. (FunctionIdentification-mapped)
     */
    public static final DataKey<TokenReportingService> NODE_REPORTING = KeyFactory.object("node_report",
            TokenReportingService.class);

    /**
     * Retrieves function ids and pass-updated units (FunctionIdentification-mapped)
     */
    public static final DataKey<ScopeService> SCOPE_SERVICE = KeyFactory.object("scope_service",
            ScopeService.class);

    public static final DataKey<DirectiveParser> DIRECTIVE_PARSER = KeyFactory.object("directive_parser",
            DirectiveParser.class);

    // private final Recipe recipe;
    private final DataKey<PreTypeContext> contextKey;
    private final String managerName;

    // Fro compatibility with old PreTypeInferenceManager
    private FunctionIdentification rootFunction;

    // private final PassManagerData passData;

    public PassManager(DataKey<PreTypeContext> contextKey, String managerName) {
        // this.recipe = recipe;
        this.contextKey = contextKey;
        this.managerName = managerName;

        this.rootFunction = null;
    }

    public FunctionIdentification getRootFunction() {
        return rootFunction;
    }

    protected DataKey<PreTypeContext> getContextKey() {
        return contextKey;
    }

    @Override
    public final Collection<DataKey<?>> getWriteKeys() {
        return Arrays.asList(contextKey, NODE_REPORTING);
    }

    @Override
    public final Collection<DataKey<?>> getReadKeys() {
        return Arrays.asList(MatisseInit.MFILES_SERVICE, MatisseInit.PRINT_STREAM, MatisseInit.PASS_LOG);
    }

    public PassManagerData applyPasses(String rootFile, MatlabRecipe recipe, DataStore data) {

        Preconditions.checkArgument(rootFile != null);

        // Create new PassManagerData

        DefaultPassManagerData passData = new DefaultPassManagerData(managerName, contextKey);

        // Add options from setup
        passData.set(data);

        // TODO: Check if needed
        // Preconditions.checkState(!appliedPreTypeInferencePasses, "Can only call applyPreTypeInferencePasses once");
        // appliedPreTypeInferencePasses = true;

        // Ensure in scope
        FileNode fileNode = passData.get(MatisseInit.MFILES_SERVICE).getFileNodeSafe(rootFile);

        String mainUnitName = fileNode.getMainUnitName();
        FunctionIdentification rootFunction = new FunctionIdentification(rootFile, mainUnitName);

        // For compatibility with old PreTypeInferencePassManager
        this.rootFunction = rootFunction;

        // Create PassContext
        passData.add(contextKey, new PreTypeContext(recipe));

        // Load the root function
        loadUnit(rootFunction, passData);

        // Apply passes
        applyPasses(passData);

        return passData;
    }

    void applyPasses(PassManagerData passData) {
        PreTypeContext context = passData.get(contextKey);

        while (context.getNumAppliedPasses() < context.getRecipe().size()) {
            applyPass(passData);

            context.incrementAppliedPasses();
        }
    }

    private void applyPass(PassManagerData passData) {

        PreTypeContext context = passData.get(contextKey);

        // Iterate while there are passes applied
        while (true) {
            boolean appliedAnyPass = false;

            int ids = context.getNumIds();

            // Create new list, since list of parsedIds can change while applying a pass
            // E.g., When a pass requests a new M-File
            for (FunctionIdentification id : new ArrayList<>(context.getCurrentIds())) {
                int completedPasses = context.getCompletedPasses(id);

                // Apply passes until one more than currently applied passes
                while (completedPasses <= context.getNumAppliedPasses()) {

                    appliedAnyPass = true;

                    // Number of completed passes is the same as the pass index
                    // E.g., 0 completed passes, apply pass 0
                    applyPass(id, completedPasses, passData);
                    completedPasses = context.incrementAppliedPassed(id);
                }
            }

            // Check if there are new ids after applying passes
            // if (ids != parsedIds.size()) {
            if (ids != context.getNumIds()) {
                appliedAnyPass = true;
            }

            // Return if no pass was applied
            if (!appliedAnyPass) {
                return;
            }
        }

    }

    private void applyPass(FunctionIdentification functionId, int recipeIndex, PassManagerData passData) {
        PreTypeContext context = passData.get(contextKey);

        assert functionId != null;
        assert recipeIndex >= 0 && recipeIndex < context.getRecipe().size() : "recipe index out of range: "
                + recipeIndex;

        MatlabNodePass pass = context.getRecipe().get(recipeIndex);

        passData.get(MatisseInit.PASS_LOG)
                .append("Applying pass " + recipeIndex + " " + pass.getName() + " to " + functionId + "\n");

        // Set function id
        Optional<FunctionIdentification> previous = passData.setFunctionId(Optional.of(functionId));

        MatlabUnitNode node = getUnit(functionId, passData);
        pass.apply(node, passData);

        passData.setFunctionId(previous);
    }

    /**
     * Returns a MatlabNode, tracked by the pass manager.
     * 
     * <p>
     * Checks if the node is already loaded. If not, loads a fresh node from the user files and adds it to the pass
     * manager context.
     * 
     * <p>
     * Returns an empty Optional if no node for the given function is found.
     * 
     * @param functionId
     * @return
     */
    public MatlabUnitNode getUnit(FunctionIdentification functionId, DataStore passData) {

        PreTypeContext context = passData.get(contextKey);
        // Check if node was already loaded
        if (context.hasUnit(functionId)) {
            return context.getUnit(functionId);
        }

        return loadUnit(functionId, passData);

    }

    /**
     * Loads a new unit from available user files.
     * 
     * @param functionId
     * @return
     */
    private MatlabUnitNode loadUnit(FunctionIdentification functionId, DataStore passData) {
        // Get a fresh node from the user files

        Optional<FileNode> potentialFileNode = passData.get(MatisseInit.MFILES_SERVICE).getFileNode(
                functionId.getFile());

        // If no node, return empty
        if (!potentialFileNode.isPresent()) {
            throw new RuntimeException("Could not find the file '" + functionId.getFile()
                    + "' in the given user files.");
        }

        // Iterate over the units in the given file node (using units to support functions and scripts)
        Optional<MatlabUnitNode> potentialUnit = potentialFileNode.get().getUnits().stream()
                .filter(f -> f.getUnitName().equals(functionId.getName()))
                .findFirst();

        if (!potentialUnit.isPresent()) {
            throw new RuntimeException("Could not find the MATLAB unit '" + functionId.getName()
                    + "' in the file '" + functionId.getFile() + "'.");
        }

        passData.get(contextKey).addUnit(functionId, potentialUnit.get());

        return potentialUnit.get();
    }

    /**
     * Fetches and applies passes until the unit is up-to-date.
     * 
     * @param functionId
     * @param projectFiles
     * @param context
     * @return
     */
    public MatlabUnitNode getAndUpdateUnit(FunctionIdentification functionId, PassManagerData data) {

        // Make sure the unit exists
        getUnit(functionId, data);

        // Update unit
        PreTypeContext context = data.get(contextKey);
        int nextPassToApply = context.getCompletedPasses(functionId);
        while (nextPassToApply < context.getNumAppliedPasses()) {

            // Apply pass
            applyPass(functionId, nextPassToApply, data);
            nextPassToApply = context.incrementAppliedPassed(functionId);
        }

        // Return a fresh reference
        return context.getUnit(functionId);
    }

}
