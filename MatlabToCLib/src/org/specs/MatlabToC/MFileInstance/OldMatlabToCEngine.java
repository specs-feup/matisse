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

import java.util.List;
import java.util.Optional;

import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.Matisse.Matlab.TypesMap;
import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabToC.MatlabToCUtils;
import org.specs.MatlabToC.MFunctions.MFunctionProvider;
import org.specs.MatlabToC.SystemInfo.ImplementationData;
import org.specs.matisselib.PassMessage;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.io.SimpleFile;
import pt.up.fe.specs.util.providers.ResourceProvider;

/**
 * @author Joao Bispo
 * 
 */
public class OldMatlabToCEngine implements MatlabToCEngine {

    private final ImplementationData data;

    public OldMatlabToCEngine(ImplementationData data) {
        this.data = data;
    }

    // public static MatlabToCEngine newInstance(List<String> mFileResources, String typesResource,
    /**
     * 
     * @param mFileResources
     * @param types
     *            types definitions (including global variables)
     * @param setup
     * @param fsettings
     * @return
     */
    public static MatlabToCEngine newInstance(List<ResourceProvider> mFileResources, LanguageMode languageMode,
            TypesMap types, DataStore setup) {

        // Create table with the M-files of the project
        ImplementationData implementationData = MatlabToCUtils.newImplementationData(languageMode, types, setup);

        // Add M files
        for (ResourceProvider resource : mFileResources) {
            // String filename = IoUtils.getResourceName(resource);
            // String mfileContents = IoUtils.getResource(resource);

            // implementationData.getProjectMFiles().addUserFile(filename, mfileContents);
            implementationData.getProjectMFiles().addUserFile(resource);
        }

        return new OldMatlabToCEngine(implementationData);
    }

    /**
     * If given function is not in the engine, it is added to it.
     * 
     * @param mainFunctionName
     * @param matlabCode
     * @return
     */
    @Override
    public void addWithCheck(String mainFunctionName, String matlabCode) {
        if (hasMatlabFunction(mainFunctionName)) {
            return;
        }

        SimpleFile matlabFile = SimpleFile.newInstance(mainFunctionName + ".m", matlabCode);

        // data.getProjectMFiles().addUserFile(mainFunctionName, matlabCode);
        data.getProjectMFiles().addUserFile(matlabFile);
    }

    @Override
    public boolean hasMatlabFunction(String mainFunctionName) {
        return data.getProjectMFiles().getUserFunction(mainFunctionName).isPresent();
    }

    @Override
    public InstructionsInstance newFunctionInstance(String matlabFunction, ProviderData inputData) {

        // Get MatlabToken
        Optional<FunctionNode> mtoken = data.getProjectMFiles().getUserFunction(matlabFunction);
        if (!mtoken.isPresent()) {
            inputData.getReportService().emitMessage(PassMessage.FUNCTION_NOT_FOUND,
                    "Could not find MATLAB function '" + matlabFunction + "'.");
            return null;
        }

        // Create builder
        // TODO: STUB filename
        String fileName = matlabFunction + ".m";
        MFunctionProvider builder = MFunctionProvider.newInstance(true, fileName, matlabFunction, mtoken.get(), data);

        // inputData = ProviderData.newUserFunctionInstance(inputData, fileName);
        // Create instance
        return builder.createInstance(inputData);
    }

    @Override
    public InstructionsInstance newFunctionInstance(MatlabTemplate template, ProviderData providerData) {
        String functionName = template.addToEngine(this);
        return newFunctionInstance(functionName, providerData);
    }

    @Override
    public void setCBasefilename(String basefilename) {
        data.setBaseCFilename(basefilename);
    }

    @Override
    public InstructionsInstance newFunctionInstance(ResourceProvider resource, ProviderData data) {
        // Get name of MATLAB file
        String mFilename = SpecsIo.getResourceName(resource.getResource());

        // Get MATLAB function name
        String mfunction = SpecsIo.removeExtension(mFilename);

        return newFunctionInstance(mfunction, data);
    }

    @Override
    public FunctionType getFunctionType(ResourceProvider resource, ProviderData data) {
        return newFunctionInstance(resource, data).getFunctionType();
    }

    @Override
    public void forceLoad(ResourceProvider resource) {
        // No action needed.
        // The old system already greedy-loads everything.
    }

    @Override
    public void forceLoad(MatlabTemplate template) {
        addWithCheck(template.getName(), template.getMCode());
    }

    @Override
    public FunctionType getFunctionType(MatlabTemplate template, ProviderData data) {
        return newFunctionInstance(template, data).getFunctionType();
    }

    /**
     * @param projectMFiles
     * @return
     */
    /*
    private static Map<String, MatlabToken> getTopLevelFunctions(ProjectMFiles projectMFiles) {
    if (data.topLevelMFiles.isEmpty()) {
        return FactoryUtils.newArrayList();
    }
    
    List<MatlabToken> topLevelFunctions = FactoryUtils.newArrayList();
    
    for (String functionName : data.topLevelMFiles) {
    
        // Check if any of the names is <all files>
        if (functionName.equals(ALL_FILES)) {
    	return projectMFiles.getMainUserFunctions();
    	// return matlabToC.getProjectPrototypes().getUserPrototypes();
        }
    
        MatlabToken mainFunction = projectMFiles.getUserFunction(functionName);
    
        if (mainFunction == null) {
    	LoggingUtils.msgInfo("Top-level function: could not find function with name '"
    		+ functionName + "'");
    	continue;
        }
    
        topLevelFunctions.add(mainFunction);
    }
    
    return topLevelFunctions;
    }
    */
}
