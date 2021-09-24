package org.specs.MatlabProcessor;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.FileNode;
import org.specs.MatlabIR.MatlabNode.nodes.root.MatlabUnitNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.FunctionDeclarationSt;
import org.specs.MatlabProcessor.Exceptions.CodeGenerationException;
import org.specs.MatlabProcessor.MatlabParser.MatlabLineParser;
import org.specs.MatlabProcessor.MatlabParser.MatlabParser;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.utilities.LineStream;

/**
 * This library provides methods for creating MatlabTokens from MatLab code and vice-versa.
 * 
 * <p>
 * A MatlabToken object is an internal representation while compiling .m files.
 * 
 * @author Remi Fradet / SPeCS: http://paginas.fe.up.pt/~specs/page6/page6.html
 * 
 */
public class MatlabProcessorUtils {

    private final static String SUBFUNCTION_SEPARATOR = "-";

    /**
     * Builds a default name from the M file, to be used to identify Matlab scripts.
     * 
     * <p>
     * Current implementation returns the name of file without the extension.
     * 
     * @param mFile
     * @return
     */
    public static String getDefaultName(File mFile) {
        return getDefaultName(mFile.getName());
    }

    public static String getDefaultName(String filename) {
        return SpecsIo.removeExtension(filename);
    }

    /**
     * Writes the MatlabToken representation into the file given in parameter.
     * 
     * @param matlabTokenRepresentation
     *            The MatlabToken representation the user wants to write into a file
     * @param mFile
     *            The MatLab file the user wants to write the MatLab Token representation in.
     * 
     * @throws CodeGenerationException
     *             If an error occurs while the code generation.
     */
    public static void toMFile(MatlabNode matlabTokenRepresentation, File mFile) {

        String mFileContent = matlabTokenRepresentation.getCode();

        SpecsIo.write(mFile, mFileContent);

    }

    /**
     * Returns the FunctionDeclaration token from the specified mFile, or null if none is found.
     * 
     * <p>
     * TODO: Check uses, use FunctionDeclarationSt instead of MatlabNode
     * 
     * @param mFile
     * @return
     */
    public static Optional<FunctionDeclarationSt> getFunctionDeclaration(File mFile, LanguageMode languageMode) {
        try (LineStream reader = LineStream.newInstance(mFile)) {
            return getFunctionDeclaration(reader, languageMode);
        }
    }

    public static Optional<FunctionDeclarationSt> getFunctionDeclaration(LineStream reader, LanguageMode languageMode) {
        // MatlabLineParser lineParser = new MatlabLineParser(mFile.getName(), Optional.empty());
        MatlabLineParser lineParser = new MatlabLineParser(reader.getFilename().orElse("dummy_name"), languageMode,
                Optional.empty());

        // Parse MATLAB file until a FunctionDeclaration is found
        // int counter = 1;
        // try (LineStream reader = LineStream.newInstance(mFile)) {
        for (String line : reader.getIterable()) {
            // lineParser.addLine(line, counter);
            lineParser.addLine(line);
            // counter++;

            if (lineParser.hasCreated(FunctionDeclarationSt.class)) {
                break;
            }
        }
        // }
        // Return the Function Declaration
        return lineParser.getFileNode().getFirstChildRecursiveOptional(FunctionDeclarationSt.class);

    }

    /**
     * 
     * @param matlabCode
     * @return the name of the function of the given MATLAB code
     */
    public static Optional<String> getFunctionName(String matlabCode, LanguageMode languageMode) {
        try (StringReader reader = new StringReader(matlabCode);
                LineStream lineStream = LineStream.newInstance(reader, Optional.of(FileNode.getNoFilename()))) {

            Optional<FunctionDeclarationSt> functionDeclaration = MatlabProcessorUtils
                    .getFunctionDeclaration(lineStream, languageMode);

            if (!functionDeclaration.isPresent()) {
                return Optional.empty();
            }

            return Optional.of(functionDeclaration.get().getFunctionName());
        }
    }

    public static Optional<String> getFunctionName(String matlabCode) {
        return getFunctionName(matlabCode, LanguageMode.MATLAB);
    }

    /**
     * Creates an object with information about the function declaration.
     * 
     * @param mFile
     * @return
     */
    /*
    public static FunctionDeclarationInfo parseMFunctionDeclaration(File mFile) {
    Optional<FunctionDeclarationSt> functionDeclarationOpt = getFunctionDeclaration(mFile);
    
    if (!functionDeclarationOpt.isPresent()) {
        LoggingUtils.msgInfo("Could not find function declaration for file '" + mFile + "'");
        return null;
    }
    
    FunctionDeclarationSt functionDeclaration = functionDeclarationOpt.get();
    String functionName = functionDeclaration.getFunctionName();
    // MatlabNode id = StatementAccess.getFunctionDeclarationIdentifier(functionDeclaration);
    // String functionName = MatlabTokenContent.getIdentifierName(id);
    
    List<String> inputNames = functionDeclaration.getInputs().getNames();
    // List<String> inputNames = MatlabTokenContent
    // .getFunctionDeclarationInputNames(functionDeclaration);
    List<String> outputNames = functionDeclaration.getOutputs().getNames();
    // MatlabNode outputs = StatementAccess.getFunctionDeclarationOutputs(functionDeclaration);
    // List<String> outputNames = MatlabTokenContent.getOutputsNames(outputs);
    
    return new FunctionDeclarationInfo(functionName, inputNames, outputNames);
    }
    */
    /**
     * Given the name of an M-file and of the function, returns an ID for the name of the function.
     * 
     * @param mfileName
     * @param functionName
     * @return
     */
    public static String getFunctionId(String mfileName, String functionName) {
        if (isMainFunction(mfileName, functionName)) {
            return functionName;
        }

        return mfileName + MatlabProcessorUtils.SUBFUNCTION_SEPARATOR + functionName;
    }

    /**
     * Given the name of an M-file and of the function, returns true if the function name corresponds to the main
     * function of the file.
     * 
     * @param mfileName
     * @param functionName
     * @return
     */
    public static boolean isMainFunction(String mfileName, String functionName) {
        if (mfileName == null) {
            return true;
        }

        return mfileName.equals(functionName);
    }

    /**
     * Parses the given MATLAB code and returns the first node corresponding to a Function or Script.
     * 
     * @param code
     * @return
     */
    public static MatlabUnitNode newToken(String code) {
        FileNode token = new MatlabParser().parse(code);

        if (token.getNumChildren() < 1) {
            throw new RuntimeException("Could not find a script/function in the given code:\n\"" + code + "\"");
        }

        // Return the first unit
        return token.getUnits().get(0);
    }

    /**
     * 
     * @param folder
     * @return all the .m files in the given folder
     */
    public static List<File> crawlFolder(File folder) {
        List<File> files = new ArrayList<>();

        crawlFolder(folder, files);

        return files;
    }

    private static void crawlFolder(File folder, List<File> files) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                crawlFolder(file, files);
                continue;
            }

            if (!file.getName().endsWith(".m")) {
                continue;
            }

            files.add(file);
        }
    }

    /**
     * Removes files that are compiled M files.
     * 
     * @param files
     * @return
     */
    public static List<File> filterCompiled(List<File> files) {
        List<File> newFiles = new ArrayList<>();
        for (File file : files) {
            try (LineStream reader = LineStream.newInstance(file)) {

                // Check first line
                String firstLine = reader.nextLine();
                if (firstLine != null && firstLine.startsWith("V1MCC")) {
                    continue;
                }

                newFiles.add(file);

            }
        }

        return newFiles;
    }
}
