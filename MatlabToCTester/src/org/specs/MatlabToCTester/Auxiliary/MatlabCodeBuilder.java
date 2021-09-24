package org.specs.MatlabToCTester.Auxiliary;

import java.io.File;
import java.util.List;

import org.specs.MatlabIR.MatlabLanguage.LanguageMode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.FunctionDeclarationSt;
import org.specs.MatlabProcessor.MatlabProcessorUtils;

/**
 * 
 * @author Pedro Pinto
 * 
 */
public class MatlabCodeBuilder {

    // The builder used to create the string
    private final StringBuilder builder;

    public MatlabCodeBuilder() {
        builder = new StringBuilder();
    }

    public void newline() {

        builder.append("\n");
    }

    /**
     * Generates several newlines.
     * 
     * @param times
     *            - the number of newlines
     */
    public void newline(int times) {

        while (times-- != 0) {
            newline();
        }
    }

    public void end() {

        builder.append("end");
        newline();
    }

    public void functionHeader(List<String> functionOutputs, String functionName, List<String> functionInputs) {

        int outputsSize = functionOutputs.size();
        int outputsLast = outputsSize - 1;
        int inputsSize = functionInputs.size();
        int inputsLast = inputsSize - 1;

        builder.append("function ");

        // outputs
        if (outputsSize > 0) {

            builder.append("[");
            for (int i = 0; i < outputsLast; i++) {

                builder.append(functionOutputs.get(i));
                builder.append(", ");
            }
            builder.append(functionOutputs.get(outputsLast));
            builder.append("]");
            builder.append(" = ");
        }

        // function name
        builder.append(functionName);

        builder.append("(");

        // inputs
        if (inputsSize > 0) {
            for (int i = 0; i < inputsLast; i++) {

                builder.append(functionInputs.get(i));
                builder.append(", ");
            }
            builder.append(functionInputs.get(inputsLast));
        }
        builder.append(")");
        newline();
    }

    public void clearAll() {

        builder.append("clear all;");
        newline();
    }

    public void clc() {

        builder.append("clc;");
        newline();
    }

    /**
     * Adds an absolute path.
     * 
     * @param path
     */
    public void addPathBegin(File path) {
        addPathBegin(path.getAbsolutePath());
    }

    public void addPathBegin(String path) {

        builder.append("addpath '");
        builder.append(path);
        builder.append("' -BEGIN;");
        newline();
    }

    /**
     * @param absolutePath
     */
    public void addPathEnd(String path) {

        builder.append("addpath '");
        builder.append(path);
        builder.append("' -END;");
        newline();
    }

    /**
     * Builds a Matlab function call.
     * 
     * @param functionOutputs
     *            - the outputs of the function
     * @param functionName
     *            - the name of the function
     * @param functionInputs
     *            - the inputs of the function
     */
    public void functionCall(List<String> functionOutputs, String functionName, List<String> functionInputs) {

        int outputsSize = functionOutputs.size();
        int outputsLast = outputsSize - 1;
        int inputsSize = functionInputs.size();
        int inputsLast = inputsSize - 1;

        // outputs
        if (outputsSize > 0) {

            builder.append("[");
            for (int i = 0; i < outputsLast; i++) {

                builder.append(functionOutputs.get(i));
                builder.append(", ");
            }
            builder.append(functionOutputs.get(outputsLast));
            builder.append("]");
            builder.append(" = ");
        }

        // function name
        builder.append(functionName);

        builder.append("(");

        // inputs
        if (inputsSize > 0) {
            for (int i = 0; i < inputsLast; i++) {

                builder.append(functionInputs.get(i));
                builder.append(", ");
            }
            builder.append(functionInputs.get(inputsLast));
        }
        builder.append(");");
        newline();
    }

    /**
     * Helper function which receives a MATLAB file.
     * 
     * @param functionOutputs
     * @param functionName
     * @param functionInputs
     * @return
     */
    public static String generateFunctionCall(File matlabFile, LanguageMode languageMode) {
        FunctionDeclarationSt functionDec = MatlabProcessorUtils.getFunctionDeclaration(matlabFile, languageMode).get();

        List<String> inputNames = functionDec.getInputNames();
        List<String> outputNames = functionDec.getOutputNames();
        String functionName = functionDec.getFunctionName();

        return generateFunctionCall(outputNames, functionName, inputNames);
    }

    /**
     * Returns a Matlab function call.
     * 
     * @param functionOutputs
     *            - the outputs of the function
     * @param functionName
     *            - the name of the function
     * @param functionInputs
     *            - the inputs of the function
     * @return a string
     */
    public static String generateFunctionCall(List<String> functionOutputs, String functionName,
            List<String> functionInputs) {

        StringBuilder builder = new StringBuilder();

        int outputsSize = functionOutputs.size();
        int outputsLast = outputsSize - 1;
        int inputsSize = functionInputs.size();
        int inputsLast = inputsSize - 1;

        // outputs
        if (outputsSize > 0) {

            builder.append("[");
            for (int i = 0; i < outputsLast; i++) {

                builder.append(functionOutputs.get(i));
                builder.append(", ");
            }
            builder.append(functionOutputs.get(outputsLast));
            builder.append("]");
            builder.append(" = ");
        }

        // function name
        builder.append(functionName);

        builder.append("(");

        // inputs
        if (inputsSize > 0) {
            for (int i = 0; i < inputsLast; i++) {

                builder.append(functionInputs.get(i));
                builder.append(", ");
            }
            builder.append(functionInputs.get(inputsLast));
        }
        builder.append(");");
        builder.append("\n");

        return builder.toString();
    }

    public void load(String file) {

        builder.append("load '");
        builder.append(file);
        builder.append("';");
        newline();
    }

    /**
     * Removes an absolute path.
     * 
     * @param path
     */
    public void removePath(File path) {
        removePath(path.getAbsolutePath());
    }

    public void removePath(String path) {

        builder.append("rmpath '");
        builder.append(path);
        builder.append("';");
        newline();
    }

    public void ifThen(String condition, List<String> thenBlock) {

        builder.append("if( ");
        builder.append(condition);
        builder.append(" )");

        newline();

        for (String thenInstruction : thenBlock) {

            builder.append("\t");
            builder.append(thenInstruction);
            newline();
        }

        end();
    }

    public void ifThenElse(String condition, List<String> thenBlock, List<String> elseBlock) {

        builder.append("if( ");
        builder.append(condition);
        builder.append(" )");

        newline();

        for (String thenInstruction : thenBlock) {

            builder.append("\t");
            builder.append(thenInstruction);
            newline();
        }

        builder.append("else");
        newline();

        for (String elseInstruction : elseBlock) {

            builder.append("\t");
            builder.append(elseInstruction);
            newline();
        }

        end();
    }

    /**
     * Builds a TRY CATCH Matlab block.
     * 
     * @param tryInstructions
     *            - the try instructions
     * @param catchInstructions
     *            - the catch instructions
     * @param catchVariable
     *            - the variable that 'catches' the exception
     */
    public void tryCatch(List<String> tryInstructions, List<String> catchInstructions, String catchVariable) {

        builder.append("try");
        newline();

        for (String tryString : tryInstructions) {
            builder.append("\t");
            builder.append(tryString);
            newline();
        }

        builder.append("catch ");
        builder.append(catchVariable);
        newline();

        for (String catchInstruction : catchInstructions) {

            builder.append("\t");
            builder.append(catchInstruction);
            newline();
        }

        end();
    }

    /**
     * Returns a TRY CATCH Matlab block.
     * 
     * @param tryInstructions
     *            - the try instructions
     * @param catchInstructions
     *            - the catch instructions
     * @param catchVariable
     *            - the variable that 'catches' the exception
     * @return a string
     */
    public static String generateTryCatch(List<String> tryInstructions, List<String> catchInstructions,
            String catchVariable) {

        MatlabCodeBuilder builder = new MatlabCodeBuilder();

        builder.append("try");
        builder.newline();

        for (String tryString : tryInstructions) {
            builder.append("\t");
            builder.append(tryString);
            builder.newline();
        }

        builder.append("catch ");
        builder.append(catchVariable);
        builder.newline();

        for (String catchInstruction : catchInstructions) {

            builder.append("\t");
            builder.append(catchInstruction);
            builder.newline();
        }

        builder.end();

        return builder.toString();
    }

    public static String generateIfThenElse(String condition, List<String> thenBlock, List<String> elseBlock) {

        StringBuilder staticBuilder = new StringBuilder();

        staticBuilder.append("if( ");
        staticBuilder.append(condition);
        staticBuilder.append(" )");

        staticBuilder.append("\n");

        for (String thenInstruction : thenBlock) {

            staticBuilder.append("\t");
            staticBuilder.append(thenInstruction);
            staticBuilder.append("\n");
        }

        staticBuilder.append("else");
        staticBuilder.append("\n");

        for (String elseInstruction : elseBlock) {

            staticBuilder.append("\t");
            staticBuilder.append(elseInstruction);
            staticBuilder.append("\n");
        }

        staticBuilder.append("end");
        staticBuilder.append("\n");
        staticBuilder.append("\n");

        return staticBuilder.toString();
    }

    public static String generateIfThen(String condition, List<String> thenBlock) {

        StringBuilder staticBuilder = new StringBuilder();

        staticBuilder.append("if( ");
        staticBuilder.append(condition);
        staticBuilder.append(" )");

        staticBuilder.append("\n");

        for (String thenInstruction : thenBlock) {

            staticBuilder.append("\t");
            staticBuilder.append(thenInstruction);
            staticBuilder.append("\n");
        }

        staticBuilder.append("end");
        staticBuilder.append("\n");
        staticBuilder.append("\n");

        return staticBuilder.toString();
    }

    public void comment(String comment) {

        builder.append("% ");
        builder.append(comment);
        newline();
    }

    public void disp(String message) {

        builder.append("disp('");
        builder.append(message);
        builder.append("');");
        newline();
    }

    public void append(String code) {

        builder.append(code);
        newline();
    }

    @Override
    public String toString() {
        return builder.toString();
    }

    /**
     * Returns a Matlab return instruction.
     * 
     * @return a string
     */
    public static String generateReturn() {

        return "return;\n";
    }

}
