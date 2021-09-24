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

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.specs.CIR.CodeGenerator.CodeGeneratorUtils;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.Instances.StructInstance;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Language.Types.CTypeV2;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIRTypes.Types.Numeric.NumericTypeV2;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsIo;

/**
 * Utility methods related to C intermediate representation.
 * 
 * @author Joao Bispo
 * 
 */
public class CirUtils {

    // TODO: Move back to CirOption after it is converted to a DataStore
    private static final VariableType DEFAULT_REAL_TYPE = NumericTypeV2.newInstance(CTypeV2.DOUBLE, 64);

    private static final boolean USE_WEAK_TYPES = true;

    public static VariableType getDefaultRealType() {
        return CirUtils.DEFAULT_REAL_TYPE;
    }

    public static boolean useWeakTypes() {
        return CirUtils.USE_WEAK_TYPES;
    }

    /**
     * Trims the String, removes '/' from beginning and end, and transforms '\' into '/'.
     * 
     * @param libraryFoldername
     * @return
     */
    public static String formatIncludeFoldername(String libraryFoldername) {
        // Trim
        String formatted = libraryFoldername.trim();

        // Replace '\' for '/'
        formatted = formatted.replaceAll(Pattern.compile("\\/").pattern(), "/");

        // Removed starting '/'
        while (formatted.startsWith("/")) {
            formatted = formatted.substring("/".length());
        }

        // Removed ending '/'
        while (formatted.endsWith("/")) {
            formatted = formatted.substring(0, formatted.length() - "/".length());
        }

        return formatted;
    }

    /**
     * Returns the header includes of the module.
     * <p>
     * They correspond to the includes necessary for the variables in function declarations.
     * 
     * @param cmodule
     * @return
     */
    public static Set<String> getHeaderIncludes(CFile cmodule) {
        Set<String> headerIncludes = new HashSet<>();

        for (FunctionInstance functionData : cmodule.getFunctionList()) {
            headerIncludes.addAll(functionData.getDeclarationIncludes());
        }

        return headerIncludes;
    }

    /**
     * Returns the body includes of the file.
     * <p>
     * They correspond to the includes necessary for executing the function implementations.
     * 
     * @param cmodule
     * @return
     */
    public static Set<String> getBodyIncludes(CFile cmodule) {
        Set<String> totalBodyIncludes = new HashSet<>();

        for (FunctionInstance instance : cmodule.getFunctionList()) {

            totalBodyIncludes.addAll(instance.getImplementationIncludes());
        }

        return totalBodyIncludes;
    }

    /**
     * Collect includes from a CToken, and stores them into the given set.
     * 
     * TODO: Needs to take care of recursion?
     * 
     * @param ctoken
     * @param includes
     */
    public static void collectIncludes(CNode ctoken, Set<String> includes) {

        if (ctoken instanceof VariableNode) {
            Variable var = ((VariableNode) ctoken).getVariable();
            includes.addAll(var.getType().code().getIncludes());
        }

        if (ctoken instanceof FunctionCallNode) {

            FunctionInstance funSig = ((FunctionCallNode) ctoken).getFunctionInstance();

            Set<String> instanceIncludes = funSig.getCallIncludes();
            if (instanceIncludes != null) {
                includes.addAll(instanceIncludes);
            }
        }

        for (CNode child : ctoken.getChildren()) {
            collectIncludes(child, includes);
        }
    }

    /**
     * @param cproject
     * @param outputFolder
     */
    public static void writeProject(CProject cproject, File outputFolder) {

        // Collect implementations
        // Map<String, FunctionInstance> implementations = collectImplementations(cproject);
        Map<String, FunctionInstance> implementations = cproject.getAllInstances();

        List<FunctionInstance> instances = SpecsFactory.newArrayList(implementations.values());

        // Get CFiles
        Collection<CFile> cfiles = buildCFiles(instances);

        for (CFile cmodule : cfiles) {

            // Get .h filename
            if (cmodule.hasHeaderFile()) {
                // File hfile = getHFile(cmodule, cproject, outputFolder);
                File hfile = new File(outputFolder, cmodule.getIncludeSelf());
                SpecsIo.mkdir(hfile.getParentFile().getPath());
                SpecsIo.write(hfile, CodeGeneratorUtils.hFileCode(cmodule));

            }

            // Get .c filename
            if (cmodule.hasImplementationFile()) {
                // File cfile = getCFile(cmodule, cproject, outputFolder);
                File cfile = new File(outputFolder, cmodule.getCFilename());
                SpecsIo.mkdir(cfile.getParentFile().getPath());
                SpecsIo.write(cfile, CodeGeneratorUtils.cFileCode(cmodule));
            }
        }

    }

    public static void writeProjectUniqueFile(CProject cproject, String baseName, File outputFolder) {

        // Collect implementations
        Map<String, FunctionInstance> implementations = cproject.getAllInstances();

        List<FunctionInstance> instances = SpecsFactory.newArrayList(implementations.values());

        // Get CFiles
        Collection<CFile> cfiles = buildCFiles(instances);

        // writeProjectUniqueFileH(cfiles, baseName, outputFolder);
        String hContents = createProjectUniqueFileH(cfiles, baseName);
        writeProjectUniqueFileC(cfiles, baseName, hContents, outputFolder);
    }

    /**
     * @param cfiles
     * @param baseName
     * @param outputFolder
     */
    /*
    private static void writeProjectUniqueFileH(Collection<CFile> cfiles, String baseName, File outputFolder) {
    
    File cfile = new File(outputFolder, baseName + "." + CFile.getCHeaderExtension());
    IoUtils.safeFolder(cfile.getParentFile().getPath());
    
    String hContents = createProjectUniqueFileH(cfiles, baseName);
    IoUtils.write(cfile, hContents);
    }
    */
    private static String createProjectUniqueFileH(Collection<CFile> cfiles, String baseName) {

        // Collect includes
        Set<String> headerIncludes = SpecsFactory.newHashSet();

        // Collect code for H
        StringBuilder buffer = new StringBuilder();

        // Get includes
        for (CFile cmodule : cfiles) {

            // Get .h filename
            if (!cmodule.hasHeaderFile()) {
                continue;
            }

            headerIncludes.addAll(CirUtils.getHeaderIncludes(cmodule));
        }

        // Only write system includes
        Set<String> usedIncludes = SpecsFactory.newHashSet();
        for (String include : headerIncludes) {
            if (isModuleInclude(cfiles, include)) {
                continue;
            }

            usedIncludes.add(include);
        }

        // Write include code
        buffer.append(CodeGeneratorUtils.getIncludeCode(usedIncludes, baseName));

        // Structures and Function declarations
        StringBuilder structBuffer = new StringBuilder();
        StringBuilder functionBuffer = new StringBuilder();
        for (CFile cfile : cfiles) {
            for (FunctionInstance functionData : cfile.getFunctionList()) {

                // Check if main function
                boolean isMainFunction = functionData.getCName().equals("main");

                if (!functionData.hasDeclaration() && !isMainFunction) {
                    continue;
                }

                StringBuilder pickedBuffer = pickBuffer(functionData, structBuffer, functionBuffer);

                // String declaration = functionDeclarationHeader(functionData);
                String declaration = functionData.getDeclarationCode();

                pickedBuffer.append(declaration).append("\n\n");
            }
        }

        buffer.append(structBuffer.toString());
        buffer.append(functionBuffer.toString());

        return buffer.toString();

    }

    private static boolean isModuleInclude(Collection<CFile> cfiles, String include) {
        if (SystemInclude.isSystemInclude(include)) {
            return false;
        }

        return cfiles.stream()
                .anyMatch(cFile -> cFile.hasHeaderFile() && cFile.getIncludeSelf().equals(include));
    }

    /**
     * @param functionData
     * @param structBuffer
     * @param functionBuffer
     * @return
     */
    private static StringBuilder pickBuffer(FunctionInstance functionData, StringBuilder structBuffer,
            StringBuilder functionBuffer) {

        if (functionData instanceof StructInstance) {
            return structBuffer;
        }

        return functionBuffer;
    }

    /**
     * 
     * @param cfiles
     * @param baseName
     * @param hContents
     * @param outputFolder
     */
    // private static void writeProjectUniqueFileC(Collection<CFile> cfiles, String baseName, File outputFolder) {
    private static void writeProjectUniqueFileC(Collection<CFile> cfiles, String baseName, String hContents,
            File outputFolder) {
        // Collect includes
        Set<String> includes = SpecsFactory.newHashSet();

        // Collect code
        StringBuilder buffer = new StringBuilder();

        // Get includes
        for (CFile cmodule : cfiles) {

            // Get .h filename
            if (!cmodule.hasImplementationFile()) {
                continue;
            }

            includes.addAll(CirUtils.getBodyIncludes(cmodule));
        }

        // Only write system includes
        Set<String> systemIncludes = SpecsFactory.newHashSet();
        for (String include : includes) {
            if (!SystemInclude.isSystemInclude(include)) {
                continue;
            }

            systemIncludes.add(include);
        }

        // Do not add self
        // systemIncludes.add(baseName + "." + CFile.getCHeaderExtension());

        // Write include code
        buffer.append(CodeGeneratorUtils.getIncludeCode(systemIncludes, baseName));

        // Include self H-file contents
        buffer.append("\n/****** " + baseName + ".h BEGIN ******/\n\n")
                .append(hContents)
                .append("/****** " + baseName + ".h END ******/\n");

        // Function implementations
        for (CFile cfile : cfiles) {
            for (FunctionInstance functionData : cfile.getFunctionList()) {

                if (!functionData.hasImplementation()) {
                    continue;
                }

                // String declaration = functionDeclarationHeader(functionData);
                String functionImplementation = functionData.getImplementationCode();
                buffer.append("\n").append(functionImplementation);
            }
        }

        // File cfile = new File(outputFolder, baseName + ".c");
        File cfile = new File(outputFolder, baseName + "." + CFile.getCFileExtension());
        SpecsIo.mkdir(cfile.getParentFile().getPath());
        SpecsIo.write(cfile, buffer.toString());
    }

    /**
     * @param values
     * @return
     */
    private static Collection<CFile> buildCFiles(Collection<FunctionInstance> values) {
        Map<String, CFile> cfiles = SpecsFactory.newHashMap();

        for (FunctionInstance instance : values) {
            // If instance neither as declaration nor implementation code, do
            // not add to a CFile.
            if (!instance.hasDeclaration() && !instance.hasImplementation()) {
                continue;
            }

            String cfilename = instance.getCFilename();
            CFile cfile = cfiles.get(cfilename);
            if (cfile == null) {
                // Create CFile
                cfile = new CFile(cfilename);
                cfiles.put(cfilename, cfile);
            }

            // Add implementation to the CFile
            cfile.addFunction(instance);
        }

        return cfiles.values();
    }

    public static String getFilename(String filepath, String extension) {
        filepath = CirUtils.formatIncludeFoldername(filepath);
        String headerName = filepath + extension;

        return headerName;
    }

    /*
    public static void throwUnsupportedException(Class<?> typeClass) {
    // StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
    
    throw new UnsupportedOperationException("Not yet implemented for type '" + typeClass.getSimpleName() + "'");
    }
    */
}
