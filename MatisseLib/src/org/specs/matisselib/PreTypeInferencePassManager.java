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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.StatementNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.MatlabNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.RootNodeFactory;
import org.specs.MatlabIR.MatlabNode.nodes.root.ScriptFileNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.AssignmentSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.FunctionDeclarationSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.GlobalSt;
import org.specs.MatlabIR.MatlabNode.nodes.statements.StatementFactory;
import org.specs.MatlabIR.MatlabNodePass.CommonPassData;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.MatlabIR.MatlabNodePass.interfaces.MatlabNodePass;
import org.specs.matisselib.passmanager.PassManager;
import org.specs.matisselib.services.DirectiveParser;
import org.specs.matisselib.services.NamingService;
import org.specs.matisselib.services.TokenReportingService;
import org.specs.matisselib.services.UserFileProviderService;
import org.specs.matisselib.services.WideScopeService;
import org.specs.matisselib.services.log.SelectiveLogService;
import org.specs.matisselib.services.naming.CommonNamingService;
import org.specs.matisselib.services.reporting.NodeReportService;
import org.specs.matisselib.services.widescope.ProjectWideScopeService;
import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Interfaces.DataStore;
import org.suikasoft.jOptions.Interfaces.DataView;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.SpecsCollections;
import pt.up.fe.specs.util.providers.ResourceProvider;
import pt.up.fe.specs.util.providers.StringProvider;

public class PreTypeInferencePassManager implements MatlabAstPassManager {

    private final MatlabRecipe preTypeInferenceRecipe;

    private final StringBuilder passLog = new StringBuilder();

    private boolean appliedPreTypeInferencePasses;
    private FunctionIdentification rootFunction;

    private int currentPreTypeRecipePass;

    private final List<FunctionIdentification> parsedIds = new ArrayList<>();
    private final Map<FunctionIdentification, MatlabNode> parsedFunctions = new HashMap<>();
    private final Map<FunctionIdentification, Integer> currentPass = new HashMap<>();
    private final Map<FunctionIdentification, DataStore> uninstancedPassData = new HashMap<>();

    private final DataStore basePassData;

    public PreTypeInferencePassManager(LanguageMode languageMode,
            MatlabRecipe recipe,
            Map<String, StringProvider> availableFiles,
            DataView additionalServices) {

        Preconditions.checkArgument(recipe != null);
        Preconditions.checkArgument(availableFiles != null);

        this.preTypeInferenceRecipe = recipe;

        this.basePassData = new MatisseInit().newPassData(languageMode, new HashMap<>(availableFiles),
                additionalServices);
    }

    /* (non-Javadoc)
     * @see org.specs.matisselib.MatlabAstPassManager#getWriteKeys()
     */
    @Override
    public Collection<DataKey<?>> getWriteKeys() {
        return Arrays.asList(PreTypeInferenceServices.WIDE_SCOPE, PassManager.NODE_REPORTING,
                PreTypeInferenceServices.COMMON_NAMING);
    }

    /* (non-Javadoc)
     * @see org.specs.matisselib.MatlabAstPassManager#getAppliedPreTypeInferencePasses()
     */
    @Override
    public boolean getAppliedPreTypeInferencePasses() {
        return this.appliedPreTypeInferencePasses;
    }

    /*
    public MatlabRecipe getPreTypeInferenceRecipe() {
    return preTypeInferenceRecipe;
    }
     */

    /* (non-Javadoc)
     * @see org.specs.matisselib.MatlabAstPassManager#getRootFunction()
     */
    @Override
    public FunctionIdentification getRootFunction() {
        Preconditions.checkState(this.appliedPreTypeInferencePasses);

        return this.rootFunction;
    }

    private Optional<MatlabNode> getUnderlyingFunctionNode(FunctionIdentification functionId) {

        Optional<FileNode> potentialFileNode = getPassDataForPreTypeInference(functionId).get(
                MatisseInit.MFILES_SERVICE).getFileNode(functionId.getFile());

        return getUnderlyingFunctionNode(functionId, potentialFileNode);
    }

    private Optional<MatlabNode> getUnderlyingFunctionNode(FunctionIdentification functionId,
            Optional<FileNode> potentialFileNode) {

        if (this.parsedFunctions.containsKey(functionId)) {
            assert this.parsedIds.contains(functionId);

            return Optional.of(this.parsedFunctions.get(functionId));
        }

        // If no node, return empty
        if (!potentialFileNode.isPresent()) {
            return Optional.empty();
        }

        FileNode fileNode = potentialFileNode.get();

        if (fileNode instanceof ScriptFileNode) {
            FunctionNode functionNode = fakeFunctionNode((ScriptFileNode) fileNode);

            this.parsedIds.add(functionId);
            this.parsedFunctions.put(functionId, functionNode);
            this.currentPass.put(functionId, 0);

            return Optional.of(functionNode);
        }

        for (FunctionNode f : fileNode.getFunctions()) {
            if (!f.getFunctionName().equals(functionId.getName())) {
                continue;
            }

            this.parsedIds.add(functionId);
            this.parsedFunctions.put(functionId, f);
            this.currentPass.put(functionId, 0);

            return Optional.of(f);
        }

        return Optional.empty();
    }

    private static FunctionNode fakeFunctionNode(ScriptFileNode fileNode) {

        Set<String> globals = fileNode.getDescendantsStream()
                .filter(GlobalSt.class::isInstance)
                .map(GlobalSt.class::cast)
                .flatMap(global -> global.getIdentifiers().stream())
                .collect(Collectors.toSet());

        List<MatlabNode> outputs = fileNode.getDescendantsStream()
                .filter(AssignmentSt.class::isInstance)
                .map(AssignmentSt.class::cast)
                .map(st -> st.getLeftHand())
                .filter(IdentifierNode.class::isInstance)
                .map(IdentifierNode.class::cast)
                .map(identifier -> identifier.getName())
                .distinct()
                .filter(identifier -> !globals.contains(identifier))
                .map(name -> MatlabNodeFactory.newIdentifier(name))
                .collect(Collectors.toList());

        String functionName = fileNode.getFilename();
        functionName = functionName.substring(0, functionName.length() - ".m".length());

        FunctionDeclarationSt declaration = StatementFactory.newFunctionDeclaration(0,
                functionName,
                Collections.emptyList(),
                outputs);

        List<StatementNode> statements = new ArrayList<>();
        statements.add(declaration);
        statements.addAll(fileNode.getScript()
                .getChildrenStream()
                .map(StatementNode.class::cast)
                .collect(Collectors.toList()));

        int endLine = 0;
        if (!statements.isEmpty()) {
            endLine = SpecsCollections.last(statements).getLine();
        }

        FunctionNode fakeNode = RootNodeFactory.newFunction(endLine, statements);

        // Ensure FunctionNode has a file root.
        RootNodeFactory.newFile(Arrays.asList(fakeNode),
                fileNode.getFilename(),
                fileNode.getOriginalCode());

        return fakeNode;
    }

    /**
     * Helper method which returns the PassData associated with the main function of the given file.
     * 
     * @param filename
     * @return
     */
    private DataStore getPassDataForPreTypeInference(String filename) {
        return getPassDataForPreTypeInference(new FunctionIdentification(filename));
    }

    /* (non-Javadoc)
     * @see org.specs.matisselib.MatlabAstPassManager#getPassDataForPreTypeInference(org.specs.MatlabIR.MatlabNodePass.FunctionIdentification)
     */
    @SuppressWarnings("resource")
    @Override
    public DataStore getPassDataForPreTypeInference(FunctionIdentification functionId) {
        Preconditions.checkArgument(functionId != null);

        if (this.uninstancedPassData.containsKey(functionId)) {
            return this.uninstancedPassData.get(functionId);
        }

        ProjectWideScopeService projectWideScopeService = new ProjectWideScopeService(this);

        DataStore data = new CommonPassData(this.basePassData);

        Optional<FileNode> fileNode = data.get(MatisseInit.MFILES_SERVICE).getFileNode(functionId.getFile());

        // If given function does not have a file, just return a simple PassData
        if (!fileNode.isPresent()) {
            return data;
        }

        data.add(PreTypeInferenceServices.LOG,
                new SelectiveLogService(basePassData.get(MatisseLibOption.PASSES_TO_LOG).getStringList()));

        WideScopeService wideScopeService = projectWideScopeService.withFunctionIdentification(functionId);
        data.add(PreTypeInferenceServices.WIDE_SCOPE, wideScopeService);

        PrintStream messageStream = data.get(MatisseInit.PRINT_STREAM);

        TokenReportingService tokenReporting = new NodeReportService(messageStream,
                functionId.getFile(), fileNode.get().getOriginalCode());
        data.add(PassManager.NODE_REPORTING, tokenReporting);
        if (!data.hasValue(PassManager.DIRECTIVE_PARSER)) {
            data.add(PassManager.DIRECTIVE_PARSER, new DirectiveParser());
        }

        // Using this version of getUnderlyingFunctionNode to avoid a cyclic reference (and a stack overflow)
        Optional<MatlabNode> matlabNode = getUnderlyingFunctionNode(functionId, data.get(MatisseInit.MFILES_SERVICE)
                .getFileNode(functionId.getFile()));

        NamingService namingService = new CommonNamingService(wideScopeService, matlabNode.get());
        data.add(PreTypeInferenceServices.COMMON_NAMING, namingService);

        this.uninstancedPassData.put(functionId, data);

        return data;
    }

    /* (non-Javadoc)
     * @see org.specs.matisselib.MatlabAstPassManager#getFunctionNode(org.specs.MatlabIR.MatlabNodePass.FunctionIdentification)
     */
    @Override
    public Optional<MatlabNode> getFunctionNode(FunctionIdentification functionId) {
        Preconditions.checkArgument(functionId != null);

        Optional<MatlabNode> underlyingNode = getUnderlyingFunctionNode(functionId);

        underlyingNode.ifPresent(node -> {
            assert this.currentPass.containsKey(functionId) : "Function ID not found in applied passes: " + functionId
                    + ", full list " + this.currentPass;

            int nextPassToApply = this.currentPass.get(functionId);
            while (nextPassToApply < this.currentPreTypeRecipePass) {
                // Apply pass

                applyPass(functionId, nextPassToApply++);
            }

            this.currentPass.put(functionId, nextPassToApply);
        });

        return underlyingNode;
    }

    /* (non-Javadoc)
     * @see org.specs.matisselib.MatlabAstPassManager#hasFunctionNode(org.specs.MatlabIR.MatlabNodePass.FunctionIdentification)
     */
    @Override
    public boolean hasFunctionNode(FunctionIdentification functionId) {
        Preconditions.checkArgument(functionId != null);

        return getUnderlyingFunctionNode(functionId).isPresent();
    }

    /* (non-Javadoc)
     * @see org.specs.matisselib.MatlabAstPassManager#getFunctionsIn(java.lang.String)
     */
    @Override
    public Optional<List<FunctionIdentification>> getFunctionsIn(String file) {
        Preconditions.checkArgument(file != null, "file must not be null.");

        // Get potential file node
        Optional<FileNode> fileNode = getPassDataForPreTypeInference(file)
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

    /**
     * Applies passes in lock-step with all loaded M-files
     */
    private void applyPreTypeInferencePass() {
        boolean appliedAnyPass;
        do {
            appliedAnyPass = false;
            int ids = this.parsedIds.size();
            assert ids == this.parsedFunctions.size() && ids == this.currentPass.size();

            // Create new list, since list of parsedIds can change while applying a pass
            // E.g., When a pass requests a new M-File
            for (FunctionIdentification id : new ArrayList<>(this.parsedIds)) {
                int lastPass = this.currentPass.get(id);
                while (lastPass <= this.currentPreTypeRecipePass && lastPass < this.preTypeInferenceRecipe.size()) {
                    appliedAnyPass = true;

                    applyPass(id, lastPass++);
                }

                this.currentPass.put(id, lastPass);
            }

            if (ids != this.parsedIds.size()) {
                appliedAnyPass = true;
            }
        } while (appliedAnyPass);
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
    private void applyPass(FunctionIdentification functionId, int passId) {
        assert functionId != null;
        assert passId >= 0 && passId < this.preTypeInferenceRecipe.size() : "passId out of range: " + passId;

        MatlabNodePass pass = this.preTypeInferenceRecipe.get(passId);
        log("Applying AST pass " + passId + " " + pass.getName() + " to " + functionId);

        DataStore data = getPassDataForPreTypeInference(functionId);

        MatlabNode node = getUnderlyingFunctionNode(functionId).get();
        pass.apply(node, data);
    }

    /* (non-Javadoc)
     * @see org.specs.matisselib.MatlabAstPassManager#applyPreTypeInferencePasses(java.lang.String)
     */
    @Override
    public void applyPreTypeInferencePasses(String rootFile) {
        Preconditions.checkArgument(rootFile != null);
        Preconditions.checkState(!this.appliedPreTypeInferencePasses, "Can only call applyPreTypeInferencePasses once");

        this.appliedPreTypeInferencePasses = true;

        DataStore passData = getPassDataForPreTypeInference(rootFile);

        // Ensure in scope
        FileNode fileNode = passData.get(MatisseInit.MFILES_SERVICE).getFileNode(rootFile)
                .orElseThrow(() -> new IllegalArgumentException("No such file: " + rootFile));

        String mainUnitName = fileNode.getMainUnitName();
        this.rootFunction = new FunctionIdentification(rootFile, mainUnitName);

        // Ensure function is loaded
        if (!getUnderlyingFunctionNode(this.rootFunction).isPresent()) {
            throw new RuntimeException("Could not find specified function.");
        }

        // Now apply the passes themselves
        while (this.currentPreTypeRecipePass < this.preTypeInferenceRecipe.size()) {
            applyPreTypeInferencePass();

            ++this.currentPreTypeRecipePass;
        }
    }

    @Override
    public void log(String message) {
        this.passLog.append(message + "\n");
    }

    @Override
    public String getPassLog() {
        return this.passLog.toString();
    }

    /* (non-Javadoc)
     * @see org.specs.matisselib.MatlabAstPassManager#processResource(pt.up.fe.specs.util.Interfaces.ResourceProvider)
     */
    @Override
    public FunctionIdentification processResource(ResourceProvider resource) {
        return processResource(resource.getResourceName(), StringProvider.newInstance(resource));
    }

    @Override
    public FunctionIdentification processResource(String resourceName, String code) {
        return processResource(resourceName + ".m", StringProvider.newInstance(code));
    }

    private FunctionIdentification processResource(String resourceName, StringProvider fileProvider) {
        FunctionIdentification functionId = new FunctionIdentification(resourceName);
        if (this.parsedFunctions.containsKey(functionId)) {
            return functionId;
        }

        DataStore passData = getPassDataForPreTypeInference(functionId);
        UserFileProviderService files = passData.get(MatisseInit.MFILES_SERVICE);
        files.addResourceFile(resourceName, fileProvider);

        FileNode fileNode = files.getFileNodeSafe(resourceName);
        for (FunctionNode function : fileNode.getFunctions()) {
            FunctionIdentification key = new FunctionIdentification(resourceName, function.getFunctionName());

            this.parsedFunctions.put(key, function);
            this.parsedIds.add(key);
            this.currentPass.put(key, 0);
        }

        applyPreTypeInferencePass();

        return functionId;
    }

    @Override
    public void setReportStream(PrintStream reportStream) {
        this.basePassData.set(MatisseInit.PRINT_STREAM, reportStream);
    }
}
