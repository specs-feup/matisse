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

package org.specs.MatlabAspects;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Utilities.DecoderDefault;
import org.specs.CIR.Utilities.TypeDecoder;
import org.specs.CIR.Utilities.TypeDecoderUtils;
import org.specs.CIRTypes.Types.BaseTypes;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.Matisse.MatisseKeys;
import org.specs.Matisse.Matlab.TypesMap;
import org.specs.matisselib.types.strings.MatlabStringTypeDecoder;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.collections.ScopedMap;

/**
 * Utility methods related to the parsing of type-related MATLAB aspects.
 * 
 * @author Joao Bispo
 * 
 */
public class MatlabAspectsUtils {

    private final DataStore setup;
    private final TypeDecoder decoder;
    private Optional<VariableType> defaultReal;

    public MatlabAspectsUtils(DataStore setup) {
        this.setup = setup;

        DecoderDefault decoderDefault = BaseTypes.newTypeDecode(new NumericFactory(setup));
        decoderDefault.addDecoder(new MatlabStringTypeDecoder());
        decoder = decoderDefault;

        defaultReal = Optional.empty();
    }

    public Optional<VariableType> getDefaultReal() {
        return defaultReal;
    }

    /**
     * Add aspects.
     * 
     * @param aspectsFileContents
     * @return true if parsing was successful, false otherwise
     */
    public ScopedMap<VariableType> getVariableTypes(String aspectsFileContents) {
        // Check if contents are empty
        if (aspectsFileContents.trim().isEmpty()) {
            SpecsLogs.msgLib("Ignoring empty aspect file");
            return new ScopedMap<>();
        }

        MatlabAspects.setSettings(setup);
        ScopedMap<Symbol> table = MatlabAspects.getSymbolMap(aspectsFileContents);
        Optional<Symbol> defaultReal = MatlabAspects.getDefaultReal();

        // Add default real
        if (defaultReal.isPresent()) {
            this.defaultReal = defaultReal.map(symbol -> symbol.convert(getDecoder()));
            // this.defaultReal = Optional.of(convertSymbol(defaultReal.get()));
        }

        if (table == null) {
            return null;
        }

        ScopedMap<VariableType> types = new ScopedMap<>();

        // Convert elements in SymbolMap from Symbol to VariableType
        boolean success = true;
        for (List<String> key : table.getKeys()) {

            // Get Symbol
            Symbol symbol = table.getSymbol(key);

            // VariableType variableType = convertSymbol(symbol);
            VariableType variableType = symbol.convert(getDecoder());
            if (variableType == null) {
                SpecsLogs.msgInfo(" -> Ignoring definition for symbol '" + symbol.name + "', scope '"
                        + key.subList(0, key.size() - 1) + "'");
                success = false;
                continue;
            }

            types.addSymbol(key, variableType);
        }

        if (!success) {
            SpecsLogs.msgInfo(TypeDecoderUtils.getSupportTypesMsg(decoder));
        }

        return types;
    }

    /**
     * Converts a MatLab Aspect symbol into a VariableType.
     * 
     * @param symbol
     * @param impl
     *            the type of matrix implementation. Can be null
     * @return
     */

    /*
    VariableType convertSymbol(Symbol symbol) {
    
    // MatrixImplementation impl = setup.getMatrixImplementation();
    
    // Get base type
    String symbolType = symbol.matlabType;
    
    // For compatibility with previous version
    if (symbolType.equals("single")) {
        symbolType = "float";
    }
    
    // Check if can be decoded in a VariableType
    VariableType varType = decoder.decode(symbolType);
    
    // VariableType varType = decoder.decode(symbolType);
    // Check if corresponds to a simply NumericType
    // NumericType baseType = ASPECT_TYPES.get(symbolType);
    // if (baseType == null) {
    if (varType == null) {
    
        LoggingUtils.msgWarn("Aspect symbol not supported:" + symbolType);
        LoggingUtils.msgWarn(symbol.toStringInfo());
        return null;
    }
    
    // If shape is defined, return a static matrix type
    if (symbol.dimType == Symbol.ARRAY_SHAPE) {
        return StaticMatrixType.newInstance(varType, symbol.getMatrixShape().getDims());
    }
    
    // If shape undefined, return a dynamic matrix type
    if (symbol.dimType == Symbol.UNDEFINED_ARRAY) {
        return DynamicMatrixType.newInstance(varType, symbol.getMatrixShape());
    }
    
    // Simple type
    return varType;
    
    }
    */
    /**
     * Builds a WorkspaceVariables instance and fills it with the information in the given aspect files.
     * 
     * <p>
     * Logs the loaded files to .info.
     * 
     * @param aspectFiles
     * @param implementationSettings
     */
    // public static TypesMap newInstance(List<File> aspectFiles, FunctionSettings settings) {
    public TypesMap newTypesMap(List<File> aspectFiles) {

        // Read TypesMap from setup
        TypesMap workspaceVars = new TypesMap();
        workspaceVars.addSymbols(setup.get(MatisseKeys.TYPE_DEFINITION));

        for (File aspectFile : aspectFiles) {
            SpecsLogs.msgLib("Adding aspect file '" + aspectFile + "'");

            // Parse file
            String aspectsFileContents = SpecsIo.read(aspectFile);

            ScopedMap<VariableType> types = getVariableTypes(aspectsFileContents);

            if (types == null) {
                SpecsLogs.msgInfo(" -> Could not parse file '" + aspectFile + "' as an aspect.");
                continue;
            }

            // Add types
            workspaceVars.addSymbols(types);
        }

        return workspaceVars;
    }

    public TypeDecoder getDecoder() {
        return decoder;
    }

    public static String getTypesContent(List<String> scope, String varName, String type) {
        StringBuilder builder = new StringBuilder();

        // Open brackets
        for (String function : scope) {
            builder.append("scope ").append(function).append("{\n");
        }

        // Add type
        builder.append(varName).append(":").append(type);

        // Close brackets
        IntStream.range(0, scope.size()).forEach(i -> builder.append("}\n"));

        return builder.toString();
    }

}
