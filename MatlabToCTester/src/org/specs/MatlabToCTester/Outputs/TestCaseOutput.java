package org.specs.MatlabToCTester.Outputs;

import java.io.File;
import java.util.List;
import java.util.Scanner;

import org.specs.CIR.CirKeys;
import org.specs.CIR.Options.MemoryLayout;
import org.specs.CIR.Utilities.TypeDecoder;
import org.specs.CIRTypes.Types.BaseTypes;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.MatlabToCTester.Outputs.MatisseOutput.MatisseResult;
import org.specs.MatlabToCTester.Outputs.MatisseOutput.MatisseResultParser;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.system.ProcessOutputAsString;

/**
 * Contains the information about a single test case, the input file name and the outputs resulting from the execution
 * of the binary with said input.
 * 
 * @author Pedro Pinto
 * 
 */
public class TestCaseOutput {

    private final String inputName;
    private final List<OutputVariable> outputVariables;
    private final File inputFile;

    /**
     * 
     * @param inputName
     *            - the name of the input file
     * @param outputVariables
     *            - the output variables
     */
    public TestCaseOutput(String inputName, List<OutputVariable> outputVariables, File inputFile) {
        this.inputName = inputName;
        this.outputVariables = outputVariables;
        this.inputFile = inputFile;
    }

    public String getInputName() {
        return inputName;
    }

    public List<OutputVariable> getOutputVariables() {
        return outputVariables;
    }

    public File getInputVectorFile() {
        return inputFile;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("\n");
        builder.append("\tInput \"" + inputName + "\":\n");

        for (OutputVariable variable : outputVariables) {
            builder.append(variable.toString());
        }

        return builder.toString();
    }

    /**
     * Returns a new instance of TestCaseOutput after parsing the ProcessOutput information.
     * 
     * @param name
     *            - the name of the test case (should be the name of the input)
     * @param processOutput
     *            - the output resulting from the execution
     * @param file
     * @param memLayout
     * @return a new instance of TestCaseOutput
     */
    // public static TestCaseOutput readTestCaseOutput(String name, ProcessOutput processOutput, MemoryLayout memLayout)
    // {
    public static TestCaseOutput readTestCaseOutput(String name, ProcessOutputAsString processOutput, DataStore setup,
            File inputFile) {
        List<OutputVariable> outputVariables = SpecsFactory.newArrayList();

        // If there was an execution error create one single string variable called ERROR_MESSAGE_C that contains the
        // error message
        if (processOutput.isError()) {
            String errorMessage = processOutput.getStdOut();

            OutputVariable variable = new StringOutputVariable("ERROR_MESSAGE_C", errorMessage);
            outputVariables.add(variable);

            return new TestCaseOutput(name, outputVariables, inputFile);
        }

        // Get the stdout output
        String stdout = processOutput.getStdOut();
        // System.out.println("[ readTestCaseOutput ]: output :\n" + stdout);

        try (Scanner lineScanner = new Scanner(stdout)) {
            lineScanner.useDelimiter("\n");

            TypeDecoder decoder = BaseTypes.newTypeDecode(new NumericFactory());

            // For each line
            while (lineScanner.hasNext()) {
                String variableLine = lineScanner.next();

                if (!variableLine.isEmpty() && variableLine.charAt(variableLine.length() - 1) != '\r') {
                    // Ensure Linux systems behave like Windows.
                    variableLine += '\r';
                }

                try {
                    // parseVariableLine(outputVariables, variableLine, memLayout);
                    parseVariableLine(outputVariables, variableLine, setup, decoder);
                } catch (Exception e) {
                    SpecsLogs.msgLib("Ignoring parsing of C output:\n" + variableLine);
                }

            }
        }

        return new TestCaseOutput(name, outputVariables, inputFile);
    }

    private static void parseVariableLine(List<OutputVariable> outputVariables, String variableLine,
            // MemoryLayout memLayout) {
            DataStore setup, TypeDecoder decoder) {

        MatisseResultParser parser = new MatisseResultParser(new NumericFactory(setup));
        MatisseResult result = parser.parse(variableLine);

        MemoryLayout memLayout = CirKeys.getMemoryLayout(setup);

        OutputVariable variable = new NumericOutputVariable(result.getName(), result.getValues(),
                result.getDimensions(), result.getVariableType(), memLayout);
        outputVariables.add(variable);
    }

    /**
     * Takes a string with the values and returns a list of doubles with the same information.
     * 
     * @param variableValues
     *            - the string
     * @param variableType
     * @return the list
     */
    /*
    private static List<Number> processValues(String variableValues, NumericClassName variableType) {
    
    List<Number> values = FactoryUtils.newArrayList();
    
    // Trim string
    variableValues = variableValues.trim();
    
    try (Scanner scanner = new Scanner(variableValues)) {
        scanner.useDelimiter(" ");
    
        while (scanner.hasNext()) {
    	String numberString = scanner.next();
    	Number newNumber = variableType.parseNumber(numberString);
    	values.add(newNumber);
        }
    }
    
    return values;
    }
    */
    /**
     * Takes a string with the dimensions and returns a list of integers with the same information.
     * 
     * @param variableDimensions
     *            - the string
     * @return the list
     */
    /*
    private static List<Integer> processDimensions(String variableDimensions) {
    
    List<Integer> dimensions = FactoryUtils.newArrayList();
    
    variableDimensions = variableDimensions.substring(1, variableDimensions.length() - 1);
    variableDimensions = variableDimensions.replaceAll(" ", "");
    
    Scanner scanner = new Scanner(variableDimensions);
    scanner.useDelimiter(",");
    while (scanner.hasNextInt()) {
    
        dimensions.add(scanner.nextInt());
    }
    scanner.close();
    
    return dimensions;
    }
    */

}
