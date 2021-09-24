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

package org.specs.MatlabToC;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Types.VariableType;
import org.specs.Matisse.Matlab.TypesMap;
import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabToC.CodeBuilder.MatlabToCRules.StatementProcessor.MatlabToCException;
import org.specs.MatlabToC.Functions.MathFunction;
import org.specs.MatlabToC.Functions.MatisseHelperFunction;
import org.specs.MatlabToC.Functions.MatisseInternalFunction;
import org.specs.MatlabToC.Functions.MatissePrimitive;
import org.specs.MatlabToC.Functions.MatlabBuiltin;
import org.specs.MatlabToC.Functions.MatlabIO;
import org.specs.MatlabToC.Functions.MatlabOp;
import org.specs.MatlabToC.Functions.Probe;
import org.specs.MatlabToC.Functions.Misc.CastFunctions;
import org.specs.MatlabToC.MFunctions.MFunctionPrototype;
import org.specs.MatlabToC.SystemInfo.ImplementationData;
import org.specs.MatlabToC.SystemInfo.ProjectMFiles;
import org.specs.MatlabToC.jOptions.MatisseOptimization;
import org.specs.MatlabToC.jOptions.MatlabToCKeys;
import org.specs.matisselib.providers.MatlabFunction;
import org.specs.matisselib.providers.MatlabFunctionTable;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.collections.HashSetString;

/**
 * @author Joao Bispo
 * 
 */
public class MatlabToCUtils {

    private static final String ASPECT_EXTENSION = "aspect";
    private static final String MFILE_EXTENSION = "m";
    private static final String MATFILE_EXTENSION = "mat";

    private static final MatlabFunctionTable buildTable() {
        MatlabFunctionTable prototypeTable = new MatlabFunctionTable();

        // Add MATLAB builtins
        // prototypeTable.addPrototypes(BuiltinFunction.class);

        // Add ArrayCreator prototypes
        prototypeTable.addPrototypes(MatlabBuiltin.class);

        // Add MatlabFunction prototypes
        prototypeTable.addPrototypes(MathFunction.class);

        // Add MatlabOperator prototypes
        prototypeTable.addPrototypes(MatlabOp.class);

        // Add I/O prototypes
        prototypeTable.addPrototypes(MatlabIO.class);

        // Add MATISSE extensions.
        prototypeTable.addPrototypes(MatisseInternalFunction.class);

        prototypeTable.addPrototypes(MatissePrimitive.class);

        prototypeTable.addPrototypes(MatisseHelperFunction.class);

        // Add Probes
        prototypeTable.addPrototypes(Probe.class);

        // Add casts
        prototypeTable.addPrototypes(CastFunctions.getCastPrototypes());
        // System.out.println("Custom:" + (prototypeTable.getPrototypes().size() - matlabfunctions));
        return new MatlabFunctionTable(Collections.unmodifiableMap(prototypeTable.getPrototypes()));

    }

    /**
     * @return the aspectExtension
     */
    public static String getAspectExtension() {
        return MatlabToCUtils.ASPECT_EXTENSION;
    }

    /**
     * @return the mfileExtension
     */
    public static String getMfileExtension() {
        return MatlabToCUtils.MFILE_EXTENSION;
    }

    /**
     * @return the matfileExtension
     */
    public static String getMatfileExtension() {
        return MatlabToCUtils.MATFILE_EXTENSION;
    }

    public static List<File> getAspectFiles(File aspectFilesDirectory) {
        return SpecsIo.getFiles(aspectFilesDirectory, MatlabToCUtils.ASPECT_EXTENSION);
    }

    public static MatlabFunctionTable buildMatissePrototypeTable() {
        // return buildTable(new MatisseSetup(setup));
        return buildTable();
    }

    /**
     * Builds a default ImplementationData.
     * 
     * <p>
     * - ProjectMFiles instance already has with the library M-files;<br>
     * - PrototypesTable with library prototypes;<br>
     * 
     * TODO: Remove TypesMap, use only setup.<br>
     * TODO: Instead of always building ImplementationData, build at beginning and pass it instead o setup
     * 
     * @param aspectDefinitions
     * @return
     */
    public static ImplementationData newImplementationData(LanguageMode languageMode, TypesMap aspectDefinitions,
            DataStore setup) {
        ProjectMFiles projectMFiles = new ProjectMFiles();
        MatlabFunctionTable prototypesTable = new MatlabFunctionTable(buildMatissePrototypeTable());

        ImplementationData implementationData = new ImplementationData(aspectDefinitions, projectMFiles, languageMode,
                prototypesTable, setup);

        return implementationData;
    }

    /**
     * Implements a function from a prototype, according to the given Workspace Variables.
     * 
     * @param function
     * @param varTypeDefinition
     * @param setup
     *            TODO
     * @return
     */
    public static FunctionInstance buildImplementation(MatlabFunction function, TypesMap varTypeDefinition,
            DataStore setup) {

        // Check if given FunctionPrototype is of the type MFunctionPrototype
        if (!MFunctionPrototype.class.isInstance(function)) {
            SpecsLogs.warn("Class '" + function.getClass()
                    + "' not supported, must be assignment-compatible with '" + MFunctionPrototype.class + "'");
            return null;
        }

        MFunctionPrototype mFunction = (MFunctionPrototype) function;

        List<String> inputNames = mFunction.getInputNames();

        return buildImplementation(mFunction, inputNames, varTypeDefinition, setup);
    }

    public static FunctionInstance buildImplementation(MFunctionPrototype function, List<String> inputNames,
            TypesMap varTypeDefinition, DataStore setup) {

        // Get types of input names
        ProviderData inputTypes = getTypes(function, inputNames, varTypeDefinition, setup);

        if (inputTypes == null) {
            return null;
        }

        FunctionInstance impl = function.getCheckedInstance(inputTypes);
        return impl;
    }

    /**
     * Collects the input types for the given M-function, from the Workspace Variables.
     * 
     * @param mfunction
     * @param varTypeDefinition
     * @return
     */
    public static ProviderData getTypes(MFunctionPrototype mfunction, List<String> inputNames,
            TypesMap varTypeDefinition, DataStore setup) {

        // Check if given FunctionPrototype is of the type MFunctionPrototype
        if (!MFunctionPrototype.class.isInstance(mfunction)) {
            SpecsLogs.warn("Class '" + mfunction.getClass()
                    + "' not supported, must be assignment-compatible with '" + MFunctionPrototype.class + "'");
            return null;
        }

        MFunctionPrototype mFunction = mfunction;

        List<String> scope = mFunction.getScope();

        List<VariableType> inputTypes = getTypes(inputNames, scope, varTypeDefinition);

        ProviderData fSig = ProviderData.newInstance(inputTypes, setup);

        return fSig;
    }

    /**
     * @param varTypeDefinition
     * @param mFunction
     * @param scope
     * @return
     */
    private static List<VariableType> getTypes(List<String> varNames, List<String> scope, TypesMap varTypeDefinition) {

        // For each name, determine the type
        List<VariableType> varTypes = new ArrayList<>();
        for (String varName : varNames) {
            if (!varTypeDefinition.containsSymbol(scope, varName)) {
                throw new MatlabToCException("Variable '" + varName + "' not found in the aspects table.");
            }

            VariableType type;
            try {
                type = varTypeDefinition.getSymbol(scope, varName);
            } catch (MatlabToCException e) {
                SpecsLogs.msgInfo(e.getMessage());
                return null;
            }

            varTypes.add(type);
        }

        return varTypes;
    }

    /**
     * Returns true if the given optimization is active.
     * 
     * @param setup
     * @param opt
     * @return
     */
    public static boolean isActive(DataStore setup, MatisseOptimization opt) {
        HashSetString opts = setup.get(MatlabToCKeys.MATISSE_OPTIMIZATIONS);

        return opts.contains(opt.getName());
    }

}
