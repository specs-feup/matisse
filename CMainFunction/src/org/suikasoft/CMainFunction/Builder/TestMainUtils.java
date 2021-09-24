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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Language.Operators.COperator;
import org.specs.CIR.Language.Types.CTypeSizes;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodeUtils;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Tree.CNodes.FunctionCallNode;
import org.specs.CIR.Tree.CNodes.VariableNode;
import org.specs.CIR.Tree.Instructions.InstructionType;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.Types.Views.Pointer.ReferenceUtils;
import org.specs.CIR.TypesOld.MatrixUtils.MatrixImplementation;
import org.specs.CIR.Utilities.CirBuilder;
import org.specs.CIRFunctions.MatrixFunction;
import org.specs.CIRFunctions.LibraryFunctions.CStdioFunction;
import org.specs.CIRFunctions.Utilities.UtilityInstances;
import org.specs.CIRFunctions.Utilities.UtilityResource;
import org.specs.CIRFunctions.Utilities.Instances.WriteMatrix2D;
import org.specs.CIRTypes.Language.CLiteral;
import org.specs.CIRTypes.Types.Literal.LiteralType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.CIRTypes.Types.Undefined.UndefinedTypeUtils;
import org.specs.CIRTypes.Types.Void.VoidType;
import org.specs.CIRTypes.Types.Void.VoidTypeUtils;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsStrings;
import pt.up.fe.specs.util.utilities.StringLines;

/**
 * @author Joao Bispo
 * 
 */
public class TestMainUtils extends CirBuilder {

    private static final String REGEX_SUFFIX_KERNEL_CALL = "_[\\w\\d]+(\\(.+\\))";

    private final ProviderData data;

    public TestMainUtils(DataStore setup) {
        super(setup);

        data = ProviderData.newInstance(setup);
    }

    private ProviderData getData() {
        return data;
    }

    /**
     * The arguments of a main function.
     * 
     * @param enableInputArgs
     * 
     * @return
     */
    public static FunctionType newFunctionTypes(boolean enableInputArgs) {

        VariableType intType = new NumericFactory(CTypeSizes.DEFAULT_SIZES).newInt();
        VariableType outputType = intType;

        // If no return arguments

        List<String> inputNames = Arrays.asList("argc", "argv");

        VariableType char2d = LiteralType.newInstance("char**");
        List<VariableType> inputTypes = Arrays.asList(intType, char2d);

        return FunctionType.newInstance(inputNames, inputTypes, null, outputType);
    }

    /**
     * The body of the main function.
     * 
     * <p>
     * Includes a "return 0" statement.
     * 
     * @param functionToTest
     * @param hardcodedInstances
     * @return
     */
    public CInstructionList newMainBodyInstructions(FunctionInstance functionToTest,
            Set<FunctionInstance> hardcodedInstances, TestMainOptions options) {

        return newMainBodyInstructions(functionToTest, hardcodedInstances, options, null, false);
    }

    public CInstructionList newMainBodyInstructions(FunctionInstance functionToTest,
            Set<FunctionInstance> hardcodedInstances,
            TestMainOptions options, String functionName, boolean isCoder) {

        // Build assignment:
        // Assignment
        // Variable (returnVar)
        // FunctionCall
        // Input1
        // Input2
        // ...

        // Get test variables of function
        FunctionType functionTypes = functionToTest.getFunctionType();
        List<VariableNode> testVars = getTestVariables(functionTypes);

        // Create the variables used as input for the function to test
        List<CNode> inputs = new ArrayList<>();

        // Create a new variable for each input and add it to the inputs
        for (int i = 0; i < functionTypes.getArgumentsNames().size(); i++) {
            String argName = functionTypes.getArgumentsNames().get(i);
            VariableType argType = functionTypes.getArgumentsTypes().get(i);

            CNode var = CNodeFactory.newVariable(argName, argType);
            inputs.add(var);

        }

        // Add out-as-ins to inputs
        if (functionTypes.hasOutputsAsInputs()) {
            inputs.addAll(testVars);
        }

        CInstructionList instList = new CInstructionList(
                TestMainUtils.newFunctionTypes(options.enableSuccessiveExecutions));

        // Add declaration literal code
        if (options.addCodeToMeasureExecution) {
            List<String> declarationCode = options.target.getLiteralDeclaration();
            for (String declaration : declarationCode) {
                instList.addLiteralInstruction(declaration);
            }
        }

        // Add declaration for variables to enable multiple executions
        instList.addLiteralInstructions(StringLines.getLines(SpecsIo.getResource(TimeResource.DECLARATION)));

        // Add initialization of random numbers, in case it is used
        instList.addLiteralInstruction("\n");
        instList.addLiteralInstruction("// Initialize random seed, in case rand is used");
        instList.addLiteralInstruction("srand((unsigned) time(NULL));");
        instList.addLiteralInstruction("\n");

        // Create function call
        CNode fCall = functionToTest.newFunctionCall(inputs);

        // Create kernel call
        CNode kernelCall = getKernelCall(fCall, functionTypes, testVars);
        CNode call;

        if (isCoder) {
            call = CNodeFactory.newLiteral(getCoderCall(functionName, kernelCall, functionTypes));
        } else {
            call = kernelCall;
        }

        if (options.warmup) {
            instList.addComment("Warm-up code");
            instList.addInstruction(call);

            if (!options.extraMainAfterWarmupCode.isEmpty()) {
                instList.addLiteralInstruction(options.extraMainAfterWarmupCode);
            }
        }

        // Code that gets the first command line argument
        instList.addLiteralInstruction(SpecsIo.getResource(TimeResource.READ_ITERATIONS));

        // Create time measurer, which might be optionally used
        TimeMeasurer timeMeasurer = new KernelTimeFactory(getData()).newInstance(options.target);

        // Create printf for cycle count start in simulator
        if (options.addCodeToMeasureExecution) {
            instList.addLiteralInstruction("\n");
            instList.addInstructions(timeMeasurer.getPrologue());
        }

        instList.addLiteralInstruction(SpecsIo.getResource(TimeResource.MULTI_BEFORE));

        instList.addInstruction(call);

        instList.addLiteralInstruction(SpecsIo.getResource(TimeResource.MULTI_AFTER));

        // Create printf for cycle count end in simulator
        if (options.addCodeToMeasureExecution) {
            instList.addInstructions(timeMeasurer.getEpilogue());
        }

        if (options.writeOutputs) {
            // Write files for 2D matrices
            create2DMatrixWriter(testVars, instList);
        }

        // Create printfs for output variables
        if (options.printOutputs) {
            createPrintf(testVars, instList, hardcodedInstances);
        }

        instList.addLiteralInstruction(options.extraMainFinalizationCode);

        CNode returnValue = null;

        // Create return with combination of output variables
        if (options.returnOutputs) {
            returnValue = createCombinedReturnValue(testVars, instList, isCoder);
        } else {
            returnValue = CNodeFactory.newCNumber(CLiteral.newInteger(0));
        }

        for (CNode input : inputs) {
            addFree(instList, input);
        }

        instList.addReturn(returnValue);

        return instList;
    }

    private void addFree(CInstructionList instList, CNode value) {
        VariableType variableType = value.getVariableType();
        if (variableType.usesDynamicAllocation()) {
            InstanceProvider freeProvider = variableType.functions().free();
            FunctionInstance freeInstance = freeProvider.getCheckedInstance(data.createFromNodes(value));

            instList.addFunctionCall(freeInstance, value);
        }
    }

    private void create2DMatrixWriter(List<VariableNode> testVars, CInstructionList instList) {
        for (VariableNode varNode : testVars) {

            if (!MatrixUtils.isMatrix(varNode.getVariableType())) {
                continue;
            }

            MatrixType matrixType = (MatrixType) varNode.getVariableType();

            ProviderData callData = getData().create(matrixType);
            FunctionInstance writeMatrix2D = new WriteMatrix2D(callData).create();

            CNode matrixVar = CNodeFactory.newVariable(varNode.getVariable());
            String filename = varNode.getVariable().getName() + ".txt";
            CNode filenameString = CNodeFactory.newString(filename, getNumerics().getSizes().getCharSize());

            instList.addFunctionCall(writeMatrix2D, matrixVar, filenameString);
        }

    }

    /**
     * @param functionName
     * @param kernelCall
     * @param functionTypes
     * @return
     */
    private static String getCoderCall(String functionName, CNode kernelCall, FunctionType functionTypes) {
        String callCode = kernelCall.getCode();

        int assignIndex = callCode.indexOf('=');

        String prefix = "";
        String suffix = callCode;
        if (assignIndex != -1) {
            // + 2 to account for the space after '='
            prefix = callCode.substring(0, assignIndex + 2);
            suffix = callCode.substring(assignIndex + 2);
        }

        // Starts with name of the function, apply regex
        String callContent = SpecsStrings.getRegexGroup(suffix, functionName + TestMainUtils.REGEX_SUFFIX_KERNEL_CALL,
                1);

        // Remove '&' for matrices
        for (int i = 0; i < functionTypes.getCInputNames().size(); i++) {
            VariableType varType = functionTypes.getCInputTypes().get(i);

            // If not matrix, do nothing
            if (!MatrixUtils.isMatrix(varType)) {
                continue;
            }

            // Remove dereference
            String varName = functionTypes.getCInputNames().get(i);

            callContent = callContent.replaceAll("&\\b" + varName + "\\b", varName);
        }

        // Create call
        String call = prefix + functionName + callContent + ";\n";
        return call;
    }

    private static CNode getKernelCall(CNode fCall, FunctionType functionTypes, List<VariableNode> testVars) {

        // If outputs-as-inputs, just call it
        if (functionTypes.hasOutputsAsInputs() || functionTypes.getOutputTypes().size() == 0) {
            return fCall;
        }

        CNode assign = CNodeFactory.newAssignment(testVars.get(0), fCall);
        return assign;

    }

    /**
     * Get test variables from test function.
     * 
     * @param functionTypes
     * @return
     */
    private static List<VariableNode> getTestVariables(FunctionType functionTypes) {
        List<VariableNode> testVars = SpecsFactory.newArrayList();

        if (functionTypes.hasOutputsAsInputs()) {
            for (int i = 0; i < functionTypes.getNumOutsAsIns(); i++) {

                String argName = functionTypes.getOutputAsInputDisplayNames().get(i);
                VariableType argType = functionTypes.getOutputAsInputTypes().get(i);

                // We want the variable type, without pointer, so it can be declared
                argType = ReferenceUtils.getType(argType, false);

                VariableNode var = CNodeFactory.newVariable(argName, argType);

                testVars.add(var);
            }
        } else if (functionTypes.getOutputTypes().size() != 0) {
            // Get name and type of function return variable
            String argName = functionTypes.getCOutputName();
            VariableType argType = functionTypes.getCReturnType();

            // Create variable
            VariableNode var = CNodeFactory.newVariable(argName, argType);
            testVars.add(var);
        }
        return testVars;
    }

    /**
     * @param returnVars
     * @param instList
     * @param isCoder
     * @param hardcodedInstances
     */
    private CNode createCombinedReturnValue(List<VariableNode> returnVars, CInstructionList instList, boolean isCoder) {

        // Create cast functions
        List<CNode> casts = SpecsFactory.newArrayList();
        for (CNode returnVar : returnVars) {
            // Process token
            returnVar = getValue(returnVar);

            // Get type of token
            VariableType returnType = returnVar.getVariableType();

            // Create cast function
            FunctionInstance cast = UtilityInstances.newCastToScalar(returnType, getNumerics().newInt());

            // Add call to cast function
            casts.add(CNodeFactory.newFunctionCall(cast, returnVar));
        }

        // Create sum
        CNode sum = casts.get(0);
        for (int i = 1; i < casts.size(); i++) {
            sum = getFunctionCall(COperator.Addition, sum, casts.get(i));
        }

        if (isCoder) {
            return CNodeFactory.newLiteral(sum.getCode(), NumericFactory.defaultFactory().newInt());
        } else {
            CNode sumVar = CNodeFactory.newVariable("return_value", sum.getVariableType());

            instList.addAssignment(sumVar, sum);

            return sumVar;
        }

    }

    /**
     * @param cToken
     * @return
     */
    private CNode getValue(CNode cToken) {
        if (MatrixUtils.isMatrix(cToken.getVariableType())) {
            cToken = getFunctionCall(MatrixFunction.GET, cToken, CNodeFactory.newCNumber(0));
        }

        return cToken;
    }

    /**
     * Creates printf function calls for all the outputs of the function to be tested.
     * 
     * <p>
     * Currently supports Numeric, Declared Matrix and Allocated Matrix.
     * 
     * @param returnVars
     *            the output variables
     * @param instList
     *            the list of instruction where we add the printf function call instructions
     * @param hardcodedInstances
     */
    private void createPrintf(List<VariableNode> returnVars, CInstructionList instList,
            Set<FunctionInstance> hardcodedInstances) {

        // Iterate over all the return variables
        for (VariableNode varToken : returnVars) {

            Variable variable = varToken.getVariable();

            // Get its type
            VariableType variableType = variable.getType();

            if (variableType instanceof VoidType) {
                continue;
            }

            // Get its name
            String variableName = variable.getName();

            // Call the correct printf for this variable type

            // Scalars
            if (ScalarUtils.isScalar(variableType)) {
                createNumericPrintf(ScalarUtils.cast(variableType), variableName, instList);
                continue;
            }

            // Matrix
            if (MatrixUtils.isMatrix(variableType)) {
                createMatrixPrintf(MatrixUtils.cast(variableType), variableName, instList, hardcodedInstances);
                continue;
            }

            // Undefined
            if (UndefinedTypeUtils.isUndefined(variableType)) {
                createUndefinedPrintf(variableType, variableName, instList);
                continue;
            }

            throw new RuntimeException("Printf for '" + variableType + "' not supported yet.");

        }

    }

    /**
     * @param variableType
     * @param variableName
     * @param instList
     */
    private static void createUndefinedPrintf(VariableType variableType, String variableName,
            CInstructionList instList) {
        instList.addComment("Printf for undefined variable '" + variableName + "'");
    }

    private void createMatrixPrintf(MatrixType matrixType, String matrixName, CInstructionList instList,
            Set<FunctionInstance> hardcodedInstances) {

        // Add printf instance
        FunctionInstance printfFunction = CStdioFunction.PRINTF.newCInstance(getData());
        hardcodedInstances.add(printfFunction);

        // The type of each element inside the matrix (used in the printf
        // arguments)
        ScalarType elementType = matrixType.matrix().getElementType();
        String printfSymbol = elementType.scalar().getPrintSymbol();

        // Get the numeric type string
        String numericTypeString = elementType.code().getSimpleType();

        // Reset the iteration variable -> index = 0;
        // Add it has an instruction so that variable 'index' gets declared
        VariableType intType = getNumerics().newInt();
        Variable indexVar = new Variable("index", intType);

        // Using C indexes, starting at 0
        instList.addAssignment(indexVar, CNodeFactory.newCNumber(0, intType));

        // Assign the number of elements -> numels = <NUMEL_CALL>;

        // Get numels function
        CNode matrixVariable = CNodeFactory.newVariable(matrixName, matrixType);
        CNode numelsCall = getFunctionCall(matrixType.matrix().functions().numel(), matrixVariable);

        // CToken numelsCall = getHelper().getFunctionCall(matrixType.matrix().functions().numel(), matrixVariable);

        // Build assignment
        String numelsVarName = "numels_v";
        Variable numelsVar = new Variable(numelsVarName, intType);

        instList.addAssignment(numelsVar, numelsCall);

        String body = SpecsIo.getResource(UtilityResource.PRINTF_MATRIX);

        String varNameTag = "<VARIABLE_NAME>";
        body = body.replace(varNameTag, matrixName);

        String dimTag = "<PRINT_DIMENSIONS>";

        // Build print_dim
        MatrixImplementation matrixImpl = MatrixUtils.getImplementation(matrixType);
        FunctionCallNode printDimCall = getFunctionCall(MatrixFunction.PRINT_DIM.getProvider(matrixImpl),
                matrixVariable);

        // Add function to set
        hardcodedInstances.add(printDimCall.getFunctionInstance());

        // Replace with code for function call
        body = body.replace(dimTag, printDimCall.getCode());

        String numericTypeTag = "<NUMERIC_TYPE>";
        body = body.replace(numericTypeTag, numericTypeString);

        String numelsVarTag = "<NUMELS_VAR>";
        body = body.replace(numelsVarTag, numelsVarName);

        String printfSymbTag = "<PRINTF_SYMBOL>";
        body = body.replace(printfSymbTag, printfSymbol);

        String getCallTag = "<GET_CALL>";

        // Build print_dim
        List<CNode> inputs = Arrays.asList(matrixVariable, CNodeFactory.newVariable(indexVar));

        InstanceProvider getProvider = MatrixFunction.GET.getProvider(matrixImpl);
        FunctionCallNode getLinearCall = getFunctionCall(getProvider, inputs);

        // Add function to set
        hardcodedInstances.add(getLinearCall.getFunctionInstance());

        // Replace with code for function call
        body = body.replace(getCallTag, getLinearCall.getCode());

        // Insert as literal instruction
        instList.addLiteralInstruction(body);
    }

    private void createNumericPrintf(ScalarType returnType, String variableName, CInstructionList instList) {

        String varPrintfSymbol = returnType.scalar().getPrintSymbol();

        // Get the numeric type string
        String numericTypeString = returnType.code().getSimpleType();

        // Create the CToken and add it to the instruction list
        FunctionInstance printfPrototype = CStdioFunction.PRINTF.newCInstance(getData());

        String printfString = "\"" + variableName + ":[1]:" + numericTypeString + "=" + varPrintfSymbol + "\\n\"";

        String varArg = returnType.scalar().getPrintArgument(variableName);
        List<CNode> printfArgs = CNodeUtils.buildLiteralTokens(printfString, varArg);
        CNode printfCall = printfPrototype.newFunctionCall(printfArgs);

        instList.addInstruction(printfCall, InstructionType.FunctionCall);
    }

    /**
     * Returns true if the given type needs a C assignment.
     * 
     * @param returnType
     * @return
     */
    public static boolean needsAssignment(VariableType returnType) {
        // Void type is not assignable
        if (VoidTypeUtils.isVoid(returnType)) {
            return false;
        }

        // If type is pointer, it will be passed as input, does not need assignment
        if (ReferenceUtils.isPointer(returnType)) {
            return false;
        }

        // Declared matrixes does not need an assignment
        if (MatrixUtils.isStaticMatrix(returnType)) {
            return false;
        }

        return true;
    }
}
