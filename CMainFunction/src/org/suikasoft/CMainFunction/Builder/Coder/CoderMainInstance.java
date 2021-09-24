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

package org.suikasoft.CMainFunction.Builder.Coder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.Views.Code.CodeUtils;
import org.specs.CIR.Types.Views.Pointer.ReferenceUtils;
import org.specs.Matisse.Coder.CoderUtils;
import org.suikasoft.CMainFunction.Builder.TestMainInstance;
import org.suikasoft.CMainFunction.Builder.TestMainOptions;
import org.suikasoft.CMainFunction.Builder.TestMainUtils;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsFactory;

/**
 * @author Joao Bispo
 * 
 */
public class CoderMainInstance extends TestMainInstance {

    private final String functionToPutMain;
    private final Set<String> additionalIncludes;

    /**
     * @param instructions
     * @param cFilename
     * @param customImplementationInstances
     * @param enableInputArgs
     */
    protected CoderMainInstance(CInstructionList instructions, String cFilename,
            Set<FunctionInstance> customImplementationInstances, boolean enableInputArgs, String functionToPutMain) {

        super(instructions, cFilename, customImplementationInstances, enableInputArgs);

        // this.hasMatrices = hasMatrices;
        this.functionToPutMain = functionToPutMain;
        additionalIncludes = SpecsFactory.newHashSet();
    }

    /**
     * Creates a CoderTestMainImplementation for the given test function.
     * 
     * @param testFunction
     * @param cFilename
     *            the name of the file of the main function
     * @param functionToPutMain
     * @param generalSetup
     * @return
     */
    public static FunctionInstance newInstance(FunctionInstance testFunction, String cFilename,
            CInstructionList inputInstructions, TestMainOptions options, DataStore setup, String functionToPutMain) {

        // Check if test function uses matrices as input/output
        /*
        boolean hasMatrices = false;
        if (MatrixUtils.isMatrix(testFunction.getFunctionTypes().getCInputTypes())) {
            hasMatrices = true;
        }
        if (MatrixUtils.isMatrix(testFunction.getFunctionTypes().getCReturnType())) {
            hasMatrices = true;
        }
        */

        /*
        if (options.target == KernelTime.MultiTarget) {
            return MultiMainInstance.newInstance(testFunction, cFilename, inputInstructions,
        	    options, setup);
        }
        */

        Set<FunctionInstance> hardcodedInstances = SpecsFactory.newHashSet();
        CInstructionList instructions = new TestMainUtils(setup).newMainBodyInstructions(testFunction,
                hardcodedInstances, options, functionToPutMain, true);

        if (inputInstructions == null) {
            inputInstructions = new CInstructionList();
        }

        // Add initializations for output matrices
        Set<String> additionalIncludes = addOutputInitializations(inputInstructions, testFunction.getFunctionType());

        // if (inputInstructions != null) {
        instructions.add(addInitializationInstructions(inputInstructions));
        // }

        CoderMainInstance mainInstance = new CoderMainInstance(instructions, cFilename, hardcodedInstances,
                options.enableSuccessiveExecutions, functionToPutMain);

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

        CoderMainInstance.addImplementationIncludes(additionalIncludes);

        return mainInstance;
    }

    /**
     * @param additionalIncludes
     */
    private static void addImplementationIncludes(Set<String> additionalIncludes) {
        additionalIncludes.addAll(additionalIncludes);
    }

    /**
     * @param inputInstructions
     * @param functionTypes
     */
    private static Set<String> addOutputInitializations(CInstructionList inputInstructions,
            FunctionType functionTypes) {

        Set<String> additionalIncludes = SpecsFactory.newHashSet();
        Set<String> declaredOutputNames = SpecsFactory.newHashSet();

        // Matrices are always passed as outputs-as-inputs, so check there
        for (int i = 0; i < functionTypes.getNumOutsAsIns(); i++) {
            String varName = functionTypes.getOutputAsInputNames().get(i);
            declaredOutputNames.add(varName);

            VariableType varType = functionTypes.getOutputAsInputTypes().get(i);
            // Clean pointer status
            varType = ReferenceUtils.getType(varType, false);

            // Only need to initialize matrix types
            if (MatrixUtils.isMatrix(varType)) {
                List<Integer> shape = MatrixUtils.getShapeDims(varType);

                // As default, number of dimensions is 2
                int numDims = 2;
                // Check if shape is defined
                if (!shape.isEmpty()) {
                    numDims = shape.size();
                }

                // String coderType = CoderUtils.getCoderType(VariableTypeUtilsOld.getNumericType(varType));
                // String coderType = CoderUtils.getCoderType(varType);
                String coderType = CoderUtils.getCoderType(MatrixUtils.getElementType(varType));

                String declaration = "emxArray_" + coderType + "* " + varName + " = NULL;";
                // String init = "emxInit_" + coderType + "(&" + varName + ", 2);";
                String init = "emxInit_" + coderType + "(&" + varName + ", " + numDims + ");";

                inputInstructions.addLiteralInstruction(declaration);
                inputInstructions.addLiteralInstruction(init);
            }
            // Otherwise, just declare it with CIR type
            else {
                // String dec = VariableCode.getVariableDeclaration(varName, varType, new ArrayList<String>());
                String dec = CodeUtils.getDeclarationWithInputs(varType, varName, new ArrayList<String>());

                inputInstructions.addLiteralInstruction(dec + ";");

                additionalIncludes.addAll(CodeUtils.getIncludes(varType));
            }
        }

        // Check if output already added

        if (functionTypes.hasReturnVar()) {
            Variable returnVar = functionTypes.getReturnVar();
            if (!declaredOutputNames.contains(returnVar.getName())) {
                // String dec = VariableCode.getVariableDeclaration(returnVar.getName(), returnVar.getType(),
                // new ArrayList<String>());
                String dec = CodeUtils.getDeclarationWithInputs(returnVar.getType(), returnVar.getName(),
                        new ArrayList<String>());

                inputInstructions.addLiteralInstruction(dec + ";");

                additionalIncludes.addAll(CodeUtils.getIncludes(returnVar.getType()));
            }
        }

        return additionalIncludes;
    }

    /* (non-Javadoc)
     * @see org.suikasoft.CMainFunction.Builder.TestMainInstance#getImplementationIncludes()
     */
    @Override
    public Set<String> getImplementationIncludes() {
        Set<String> implementationIncludes = super.getImplementationIncludes();

        // Make a copy so that we can remove elements from set
        Collection<String> includes = SpecsFactory.newArrayList(implementationIncludes);
        // Remove includes starting with "lib/"
        for (String include : includes) {
            if (include.startsWith("lib/")) {
                implementationIncludes.remove(include);
            }
        }

        // Add include for main function
        implementationIncludes.add(functionToPutMain + ".h");
        implementationIncludes.add(functionToPutMain + "_emxutil.h");

        // Add additional includes
        implementationIncludes.addAll(additionalIncludes);
        // Get include which is not a system include

        // If has matrices, add include
        /*
        if (hasMatrices) {
            implementationIncludes.add(functionToPutMain + "_emxutil.h");
        }
        */

        return implementationIncludes;
    }

}
