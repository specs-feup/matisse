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

package org.specs.CIR;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.specs.CIR.FunctionInstance.FunctionInstance;

import pt.up.fe.specs.util.SpecsLogs;

/**
 * Represents a .c and .h pair.
 * 
 * @author Joao Bispo
 *
 */
public class CFile {

    private final static String EXTENSION_IMPLEMENTATION = "c";
    private final static String EXTENSION_HEADER = "h";

    private final String fileName;
    private LinkedHashMap<String, FunctionInstance> functions;
    private String libraryFoldername;

    private boolean hasHeaderFile;
    private boolean hasImplementationFile;

    /**
     * 
     */
    public CFile(String fileName) {
        this.fileName = fileName;
        functions = new LinkedHashMap<>();
        libraryFoldername = null;
        hasHeaderFile = false;
        hasImplementationFile = false;
    }

    /**
     * @param libraryFoldername
     *            the libraryFolder to set
     */
    public void setLibraryFolder(String libraryFoldername) {
        libraryFoldername = CirUtils.formatIncludeFoldername(libraryFoldername);
        this.libraryFoldername = libraryFoldername;
    }

    /**
     * @return the hasHeaderFile
     */
    public boolean hasHeaderFile() {
        return hasHeaderFile;
    }

    public static String getCFileExtension() {
        return EXTENSION_IMPLEMENTATION;
    }

    public static String getCHeaderExtension() {
        return EXTENSION_HEADER;
    }

    /**
     * @return the hasImplementationFile
     */
    public boolean hasImplementationFile() {
        return hasImplementationFile;
    }

    /**
     * @param hasHeaderFile
     *            the hasHeaderFile to set
     */
    /*
    public void setHasHeaderFile(boolean hasHeaderFile) {
    this.hasHeaderFile = hasHeaderFile;
    }
    */

    /**
     * The name of the include needed to use this file in other files.
     * 
     * <p>
     * Corresponds to the header declarations (e.g., "lib.h"), and takes the library path into account (e.g.,
     * "lib/aLib.h").
     * 
     * @return
     */
    public String getIncludeSelf() {
        return getFilename("." + getCHeaderExtension());
    }

    /**
     * The name of the C implementation file (e.g., "lib.c").
     * 
     * @return
     */
    public String getCFilename() {
        return getFilename("." + getCFileExtension());
    }

    private String getFilename(String extension) {
        String headerName = fileName + extension;
        if (libraryFoldername != null) {
            headerName = libraryFoldername + "/" + headerName;
        }

        return headerName;
    }

    /**
     * The name for #ifndef sections. E.g., MODULE_NAME_H
     * 
     * @return
     */
    public String getIfDefName() {
        String defName = fileName.toUpperCase();

        // Sanitize name (e.g., remove illegal characters)
        defName = defName.replaceAll("/", "_");

        return defName + "_H";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("----------------------------\n");
        builder.append("C Module '" + fileName + "':\n");
        // for(String functionName : functions.getKeys()) {
        for (FunctionInstance fdata : getFunctionList()) {
            // SpecializedFunction fdata = functions.get(functionName);
            builder.append("Function '" + fdata.getCName() + "':\n");
            // builder.append("Function '"+fdata.getFunctionCName()+"':\n");
            // builder.append(fdata.toStringTree());
            builder.append(fdata.toString());
            builder.append("\n");
        }
        return builder.toString();
    }

    /**
     * @return the moduleFilename
     */
    public String getModuleName() {
        return fileName;
    }

    /**
     * @return the functions
     */
    /*
    public OrderedMap<String, SpecializedFunction> getFunctions() {
    return functions;
    }
    */

    public List<FunctionInstance> getFunctionList() {
        // return functions.values()getOrderedValues();
        return new ArrayList<>(functions.values());
    }

    /**
     * Adds a function implementation to this file. Automatically sets the CFile of the function.
     * 
     * @param functionData
     */
    public void addFunction(FunctionInstance functionData) {
        // Check if implementation already has a cfile
        // if(functionData.hasCFile()) {
        // return;
        // }

        // Map function name to function data
        // String functionName = functionData.getFunctionSignature().getFunctionName();
        String functionName = functionData.getCName();
        FunctionInstance previousFunction = functions.put(functionName, functionData);
        if (previousFunction != null) {
            SpecsLogs.warn("Replacing function '" + functionName + "' in C file '" + fileName + "'");
        }

        if (functionData.hasDeclaration()) {
            hasHeaderFile = true;
        }

        if (functionData.hasImplementation()) {
            hasImplementationFile = true;
        }
        // Set C module
        // functionData.setCModule(this);
    }
}
