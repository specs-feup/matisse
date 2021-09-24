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

package org.suikasoft.CMainFunction.Builder;

import java.util.List;
import java.util.Set;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.Tree.CInstructionList;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.utilities.Replacer;

/**
 * @author Joao Bispo
 * 
 */
public class TestMainInstance extends FunctionInstance {

    private final CInstructionList instructions;
    private final String cfilename;

    private final Set<FunctionInstance> customImplementationInstances;
    protected final Set<String> customImplementationIncludes;

    private String mainFinalization;
    protected String beforeMain;

    /**
     * @param enableInputArgs
     * @param parentFunction
     * @param typeData
     */
    protected TestMainInstance(CInstructionList instructions, String cFilename,
            Set<FunctionInstance> customImplementationInstances, boolean enableInputArgs) {

        super(TestMainUtils.newFunctionTypes(enableInputArgs));

        this.instructions = instructions;
        cfilename = cFilename;

        this.customImplementationInstances = customImplementationInstances;
        customImplementationIncludes = SpecsFactory.newHashSet();
        // Always uses stdint
        // customImplementationIncludes.add(SystemInclude.IntTypes.getIncludeName());

        beforeMain = "";
        mainFinalization = "";
    }

    /**
     * Creates a TestMainImplementation for the given test function.
     * 
     * @param testFunction
     * @param cFilename
     *            the name of the file of the main function
     * @param generalSetup
     * @return
     */
    public static FunctionInstance newInstance(FunctionInstance testFunction, String cFilename,
            CInstructionList inputInstructions,
            Set<FunctionInstance> extraDependentInstances, TestMainOptions options, DataStore setup) {

        /*
        if (options.target == KernelTime.MultiTarget) {
            return MultiMainInstance.newInstance(testFunction, cFilename, inputInstructions,
        	    options, setup);
        }
        */

        Set<FunctionInstance> hardcodedInstances = SpecsFactory.newHashSet();
        hardcodedInstances.addAll(extraDependentInstances);
        CInstructionList mainInstructions = new TestMainUtils(setup).newMainBodyInstructions(testFunction,
                hardcodedInstances, options);

        CInstructionList instructions = new CInstructionList(mainInstructions.getFunctionTypes());

        if (!options.extraMainPreparationCode.isEmpty()) {
            instructions.addLiteralInstruction(options.extraMainPreparationCode);
        }

        if (inputInstructions != null) {
            CInstructionList initializationInstructions = addInitializationInstructions(inputInstructions);
            instructions.add(initializationInstructions);
        }

        instructions.add(mainInstructions);

        TestMainInstance mainInstance = new TestMainInstance(instructions, cFilename, hardcodedInstances,
                options.enableSuccessiveExecutions);

        // Add includes for kernel measurement
        if (options.addCodeToMeasureExecution) {
            // List<String> includes = options.timeMeasurer.getImplementationIncludes();
            List<String> includes = options.target.getImplementationIncludes();
            mainInstance.customImplementationIncludes.addAll(includes);
        }

        mainInstance.beforeMain = buildBeforeMain(options.target,
                options.extraMainFilePrefixCode,
                options.addCodeToMeasureExecution,
                options.enableSuccessiveExecutions);

        mainInstance.mainFinalization = options.extraMainFinalizationCode;

        return mainInstance;
    }

    /**
     * @param target
     * @param addCodeToMeasureExecution
     * @param enableSuccessiveExecutions
     * @return
     */
    protected static String buildBeforeMain(MainFunctionTarget target,
            String extraMainFilePrefixCode,
            boolean addCodeToMeasureExecution,
            boolean enableSuccessiveExecutions) {

        // Check if multi_target
        if (target == MainFunctionTarget.MultiTarget) {
            String beforeMain = SpecsIo.getResource(MultiResource.BEFORE_MAIN);

            String multExecs = "0";
            if (enableSuccessiveExecutions) {
                multExecs = "1";
            }

            return extraMainFilePrefixCode + new Replacer(beforeMain).replace("<MULTI_EXEC>", multExecs).toString();
        }

        String prefix = "";
        if (target == MainFunctionTarget.Windows) {
            prefix = SpecsIo.getResource(WindowsResource.BEFORE_MAIN);
        }

        String booleanValue = "0";
        if (addCodeToMeasureExecution) {
            booleanValue = "1";
        }

        String multiExec = "#define MULTI_EXEC " + booleanValue + "\n\n";

        return prefix + extraMainFilePrefixCode + multiExec;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.FunctionInstance#getInstructions()
     */
    @Override
    protected CInstructionList getInstructions() {
        return instructions;
    }

    /**
     * Always generates a main function.
     */
    /* (non-Javadoc)
     * @see org.specs.CIR.Function.SpecializedFunction#getCodeFunctionName()
     */
    @Override
    public String getCName() {
        return "main";
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Function.SpecializedFunction#getHeaderFilename()
     */
    /*
    @Override
    public String getSelfInclude() {
    return null;
    }
    */

    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.FunctionInstance#hasDeclaration()
     */
    @Override
    public boolean hasDeclaration() {
        return false;
    }

    /**
     * Adds the instructions to the beginning of the file.
     * 
     * @param inputInstructions
     * @param functionTypes
     */
    protected static CInstructionList addInitializationInstructions(CInstructionList inputInstructions) {

        CInstructionList newInstructions = new CInstructionList();

        newInstructions.addComment("Initializations");
        newInstructions.add(inputInstructions);

        return newInstructions;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Function.FunctionImplementation#getCFileName()
     */
    @Override
    public String getCFilename() {
        return cfilename;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.FunctionInstance#getImplementationInstances()
     */
    @Override
    public Set<FunctionInstance> getImplementationInstances() {
        Set<FunctionInstance> instances = super.getImplementationInstances();

        SpecsFactory.addAll(instances, customImplementationInstances);

        return instances;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.FunctionInstance#getImplementationIncludes()
     */
    @Override
    public Set<String> getImplementationIncludes() {
        Set<String> implementationIncludes = super.getImplementationIncludes();

        implementationIncludes.addAll(customImplementationIncludes);

        return implementationIncludes;
    }

    /* (non-Javadoc)
     * @see org.specs.CIR.Functions.FunctionInstance#getImplementationCode()
     */
    @Override
    public String getImplementationCode() {
        // String beforeMain = getBeforeMain();
        String mainFunction = super.getImplementationCode();

        return beforeMain + mainFunction;
    }

}
