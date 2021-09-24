/**
 * Copyright 2012 SPeCS Research Group.
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

package org.specs.MatlabToC.MFunctions;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.Instances.InstructionsInstance;
import org.specs.CIR.Types.VariableType;
import org.specs.Matisse.Matlab.TypesMap;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FunctionNode;
import org.specs.MatlabIR.MatlabNodePass.FunctionIdentification;
import org.specs.MatlabToC.MatlabToCFunctionKeys;
import org.specs.MatlabToC.CodeBuilder.MatlabToCFunctionData;
import org.specs.MatlabToC.InstanceProviders.MatlabInstanceProvider;
import org.specs.MatlabToC.SystemInfo.ImplementationData;
import org.specs.matisselib.DefaultReportService;

import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.reporting.Reporter;

/**
 * General InstanceProvider for M-files.
 * 
 * @author Joao Bispo
 * 
 */
public class MFunctionProvider implements MatlabInstanceProvider {

    private final boolean isInternal;
    private final String fileName;
    private final String mFunctionName;
    private final String parentFunctionName;
    // private final List<String> scope;
    private final FunctionNode mFunctionTree;

    // private final TypesMap typesMap;

    // private final ProjectPrototypes functionsTable;
    // private final ProjectMFiles projectMFiles;
    private final ImplementationData implementationData;

    /**
     * @param manager
     */
    public MFunctionProvider(boolean isInternal, String fileName, String mFunctionName, String parentFunctionName,
            List<String> scope, FunctionNode mFunction, ImplementationData implementationData) {

        super();

        this.isInternal = isInternal;

        this.fileName = fileName;
        this.mFunctionName = mFunctionName;
        this.parentFunctionName = parentFunctionName;
        // this.scope = scope;
        // Process token
        implementationData.getMprocessor().process(mFunction);

        mFunctionTree = mFunction;
        this.implementationData = implementationData;

    }

    /**
     * Returns a new instance for main functions.
     * 
     * @param functionName
     * @param function
     * @param iData
     * @return
     */
    public static MFunctionProvider newInstance(boolean isInternal, String fileName, String functionName,
            FunctionNode function, ImplementationData iData) {

        String parentFunctionName = null;
        List<String> scope = Arrays.asList(functionName);

        return new MFunctionProvider(isInternal, fileName, functionName, parentFunctionName, scope, function, iData);
    }

    /**
     * Specialized M-Functions accept any type of data specialization, as long as there are as many types as number of
     * inputs.
     * 
     */
    /* (non-Javadoc)
     * @see org.specs.CIR.Function.SpecializedFunctionBuilder#checkRule(org.specs.CIR.Function.TypeData)
     */
    @Override
    public boolean checkRule(ProviderData fSig) {

        // Get input types
        List<VariableType> typeData = fSig.getInputTypes();

        // Get number of inputs
        int numTypeDataInputs = typeData.size();

        // If number of input arguments is different from the number of inputs
        // of the MATLAB function, return
        // if (numTypeDataInputs != numMatlabInputs) {
        if (numTypeDataInputs > mFunctionTree.getNumInputs()) {

            // Get declaration
            MatlabNode functionDec = mFunctionTree.getDeclarationNode();
            SpecsLogs.warn("Tried to specialize function which has " + mFunctionTree.getNumInputs()
                    + " inputs with a TypeData of " + numTypeDataInputs + " inputs. Function Declaration:\n"
                    + functionDec.getCode());
            return false;
        }

        return true;
    }

    /**
     * Parses the MatLab tree into a C tree.
     * 
     */
    /* (non-Javadoc)
     * @see org.specs.CIR.Function.SpecializedFunctionBuilder#create(org.specs.CIR.Function.GeneralFunction, org.specs.CIR.Function.TypeData)
     */
    @Override
    public FunctionInstance create(ProviderData builderData) {

        return createInstance(builderData);
    }

    public InstructionsInstance createInstance(ProviderData builderData) {
        FileNode fileNode = (FileNode) mFunctionTree.getRoot();

        Reporter reportService = builderData.getReportService();
        PrintStream reportStream = reportService == null ? System.err : reportService.getReportStream();

        // if (!IoUtils.getExtension(fileName).equals("m")) {
        // System.out.println("FILENAME DOES NOT END WITH 'M':" + fileName);
        // }

        Reporter newReportService = new DefaultReportService(reportService,
                reportStream,
                isInternal,
                new FunctionIdentification(fileName, mFunctionName),
                fileNode.getOriginalCode());
        builderData = builderData.withReportService(newReportService);

        if (!checkRule(builderData)) {
            return null;
        }

        // Get MatlabToken
        FunctionNode rootToken = mFunctionTree;

        // TODO: Annotate the MATLAB tree with information about if function is main, subfunction...
        // If subfunction, annotate the parent
        // List<String> scope = getScope();
        // builderData.getSetupRaw().getOptionTable().setOption(getScopeOption(), scope);

        // Apply MATLAB transformations dependent on inputs
        // implementationData.getMprocessorWithInputs().process(rootToken, builderData);

        // Create data
        MatlabToCFunctionData data = MatlabToCFunctionData.newInstance(builderData, implementationData, getScope());

        // Get input types
        List<VariableType> inputTypes = builderData.getInputTypes();

        InstructionsInstance functionInstance = MFunctionInstance.newInstanceV2(mFunctionName, parentFunctionName,
                rootToken, data, inputTypes);

        // Get types for this function
        TypesMap types = data.getTypes();

        // Add to the final types table
        TypesMap totalTypes = builderData.getSettings().get(MatlabToCFunctionKeys.FINAL_TYPE_DEFINITION);
        totalTypes.addSymbols(types);

        return functionInstance;
    }

    /**
     * @return
     */
    private List<String> getScope() {

        if (parentFunctionName == null) {
            return Arrays.asList(mFunctionName);
        }

        if (mFunctionName.equals(parentFunctionName)) {
            SpecsLogs.warn("CHECK: Does this ever happen?");
            return Arrays.asList(mFunctionName);
        }

        return Arrays.asList(parentFunctionName, mFunctionName);
        /*
        if (mFunctionName.equals(parentFunctionName)) {
            return Arrays.asList(parentFunctionName, mFunctionName);
        }
        
        return Arrays.asList(mFunctionName);
        */
    }

    /**
     * @deprecated Implement getScope at the level of the MatlabIR
     * @return
     */
    // public static OptionDefinition getScopeOption() {
    // return new GenericOptionDefinition("MFunctionBuilder_scope", List.class);
    // }
}
