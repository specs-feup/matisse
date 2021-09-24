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

package org.specs.matisselib;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.specs.Matisse.Matlab.TypesMap;
import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.matisselib.providers.MatlabFunctionTable;
import org.specs.matisselib.ssa.SsaRecipe;
import org.specs.matisselib.typeinference.InferenceRuleList;
import org.specs.matisselib.typeinference.PostTypeInferenceRecipe;
import org.specs.matisselib.typeinference.TypeInferencePass;
import org.suikasoft.jOptions.Interfaces.DataView;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.util.providers.StringProvider;

public class ProjectPassCompilationOptions {
    private MatlabRecipe preTypeInferenceRecipe;
    private SsaRecipe ssaRecipe;
    private PostTypeInferenceRecipe postTypeInferenceRecipe;
    private InferenceRuleList inferenceRuleList = new InferenceRuleList(TypeInferencePass.BASE_TYPE_INFERENCE_RULES);
    private Map<String, StringProvider> availableFiles;
    private MatlabFunctionTable systemFunctions;
    private DataView additionalServices = DataView.empty();
    private LanguageMode languageMode;
    private boolean enableZ3 = false;
    private TypesMap defaultTypes = new TypesMap();

    public ProjectPassCompilationOptions() {
    }

    public ProjectPassCompilationOptions(ProjectPassCompilationOptions options) {
        this.preTypeInferenceRecipe = options.preTypeInferenceRecipe;
        this.ssaRecipe = options.ssaRecipe;
        this.postTypeInferenceRecipe = options.postTypeInferenceRecipe;
        this.inferenceRuleList = options.inferenceRuleList;
        this.availableFiles = options.availableFiles;
        this.systemFunctions = options.systemFunctions;
        this.additionalServices = options.additionalServices;
        this.languageMode = options.languageMode;
        this.enableZ3 = options.enableZ3;
        this.defaultTypes = options.defaultTypes;
    }

    public ProjectPassCompilationOptions withPreTypeInferenceRecipe(MatlabRecipe preTypeInferenceRecipe) {
        this.preTypeInferenceRecipe = preTypeInferenceRecipe;

        return this;
    }

    public ProjectPassCompilationOptions withSsaRecipe(SsaRecipe ssaRecipe) {
        this.ssaRecipe = ssaRecipe;

        return this;
    }

    public ProjectPassCompilationOptions withPostTypeInferenceRecipe(PostTypeInferenceRecipe postTypeInferenceRecipe) {
        this.postTypeInferenceRecipe = postTypeInferenceRecipe;

        return this;
    }

    public ProjectPassCompilationOptions withInferenceRuleList(InferenceRuleList inferenceRuleList) {
        this.inferenceRuleList = inferenceRuleList;

        return this;
    }

    public ProjectPassCompilationOptions withAvailableFiles(Map<String, StringProvider> availableFiles) {
        this.availableFiles = availableFiles;

        return this;
    }

    public ProjectPassCompilationOptions withAvailableFiles(List<File> availableFiles) {
        Preconditions.checkArgument(availableFiles != null);

        Map<String, StringProvider> fileMap = availableFiles.stream()
                .collect(Collectors.toMap(file -> file.getName(), StringProvider::newInstance));
        return withAvailableFiles(fileMap);
    }

    public ProjectPassCompilationOptions withSystemFunctions(MatlabFunctionTable systemFunctions) {
        this.systemFunctions = systemFunctions;

        return this;
    }

    public ProjectPassCompilationOptions withAdditionalServices(DataView additionalServices) {
        this.additionalServices = additionalServices;

        return this;
    }

    public ProjectPassCompilationOptions withLanguageMode(LanguageMode languageMode) {
        this.languageMode = languageMode;

        return this;
    }

    public ProjectPassCompilationOptions withZ3Enabled(boolean enableZ3) {
        this.enableZ3 = enableZ3;

        return this;
    }

    public ProjectPassCompilationOptions withDefaultTypes(TypesMap typesMap) {
        this.defaultTypes = typesMap;

        return this;
    }

    public void validate() {
        Preconditions.checkState(this.preTypeInferenceRecipe != null);
        Preconditions.checkState(this.ssaRecipe != null);
        Preconditions.checkState(this.postTypeInferenceRecipe != null);
        Preconditions.checkState(this.inferenceRuleList != null);
        Preconditions.checkState(this.availableFiles != null);
        Preconditions.checkState(this.systemFunctions != null);
        Preconditions.checkState(this.additionalServices != null);
    }

    public MatlabRecipe getPreTypeInferenceRecipe() {
        return this.preTypeInferenceRecipe;
    }

    public SsaRecipe getSsaRecipe() {
        return this.ssaRecipe;
    }

    public PostTypeInferenceRecipe getPostTypeInferenceRecipe() {
        return this.postTypeInferenceRecipe;
    }

    public InferenceRuleList getInferenceRuleList() {
        return this.inferenceRuleList;
    }

    public Map<String, StringProvider> getAvailableFiles() {
        return Collections.unmodifiableMap(this.availableFiles);
    }

    public MatlabFunctionTable getSystemFunctions() {
        return this.systemFunctions;
    }

    public DataView getAdditionalServices() {
        return this.additionalServices;
    }

    public boolean isz3Enabled() {
        return this.enableZ3;
    }

    public LanguageMode getLanguageMode() {
        return this.languageMode;
    }

    public TypesMap getDefaultTypes() {
        return this.defaultTypes;
    }
}
