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

package org.specs.MatlabToC.CCode;

import java.util.Collections;
import java.util.List;

import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.FunctionInstance;
import org.specs.CIR.FunctionInstance.FunctionType;
import org.specs.CIR.FunctionInstance.FunctionTypeBuilder;
import org.specs.CIR.FunctionInstance.InstanceProvider;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.GenericInstanceBuilder;
import org.specs.CIR.FunctionInstance.InstanceBuilder.InstanceBuilder;
import org.specs.CIR.FunctionInstance.Instances.InlineCode;
import org.specs.CIR.FunctionInstance.Instances.InlinedInstance;
import org.specs.CIR.Language.SystemInclude;
import org.specs.CIR.Options.MemoryLayout;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodeUtils;
import org.specs.CIR.Tree.PrecedenceLevel;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.Variable;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIR.Types.Views.Code.CodeUtils;
import org.specs.CIR.TypesOld.CNumber;
import org.specs.CIRTypes.Language.CLiteral;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.DynamicMatrix.Utils.DynamicMatrixStruct;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;
import org.specs.JMatIOPlus.MatUtils;
import org.specs.JMatIOPlus.Coder.CoderResource;
import org.specs.Matisse.Coder.CoderUtils;
import org.specs.Matisse.Matlab.TypesMap;
import org.specs.MatlabIR.MatlabLanguage.NumericClassName;
import org.specs.MatlabToC.MatlabToCTypesUtils;
import org.specs.MatlabToC.Functions.BaseFunctions.Dynamic.ArrayAllocFunctions;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.jmatio.io.MLInt32;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLInt16;
import com.jmatio.types.MLInt64;
import com.jmatio.types.MLInt8;
import com.jmatio.types.MLSingle;
import com.jmatio.types.MLUInt16;
import com.jmatio.types.MLUInt32;
import com.jmatio.types.MLUInt64;
import com.jmatio.types.MLUInt8;

import pt.up.fe.specs.util.SpecsFactory;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.utilities.Replacer;

/**
 * @author Joao Bispo
 * 
 */
public class CWriter {

    // The list of variables
    private final ProviderData pdata;
    private final TypesMap aspectDefinitions;
    private final NumericFactory numerics;

    /**
     * @param variables
     * @param isCoderMain
     * @param memoryLayout
     */
    public CWriter(DataStore setup, TypesMap aspectDefinitions) {
        pdata = ProviderData.newInstance(setup);
        this.aspectDefinitions = aspectDefinitions;
        numerics = pdata.getNumerics();
    }

    public CInstructionList contentsAsCCode(List<MLArray> variables, boolean isCoderMain) {

        CInstructionList instructions = new CInstructionList();

        for (MLArray variable : variables) {

            // See if scalar
            if (MatUtils.isScalar(variable)) {
                processScalar(variable, instructions);
            } else {
                if (isCoderMain) {
                    processMatrixCoder(variable, instructions);
                } else {
                    processMatrix(variable, instructions);
                }

            }

        }

        return instructions;
    }

    /**
     * Processes a matrix variable. Initializes it with a call to zeros and then assigns the correct values to each of
     * its positions.
     * 
     * @param matrix
     *            - the variable
     * @param instructions
     *            - the list of instructions
     * @param aspectDefinitions
     * @param mImpl
     */
    private void processMatrixCoder(MLArray matrix, CInstructionList instructions) {

        GenericInstanceBuilder helper = new GenericInstanceBuilder(pdata);

        // A newline before we declare this new variable
        instructions.addLiteralInstruction("");

        boolean allowedDynamicAllocation = pdata.getSettings().get(CirKeys.ALLOW_DYNAMIC_ALLOCATION);
        // The variable
        // MatrixImplementation mImpl = pdata.getSetupTable().getMatrixImplementation();
        // if (mImpl == MatrixImplementation.DECLARED) {
        if (!allowedDynamicAllocation) {
            throw new RuntimeException("Fixed-size arrays not implemented yet");
        }

        // ElementType
        // NumericType numericType = CWriterUtils.getEquivalentCNumericType(matrix);
        NumericClassName matlabClass = CWriterUtils.getEquivalentNumericClass(matrix);

        // Check if there is a numeric type defined in the aspects for this variable
        // numericType = CWriterUtils.updateNumericType(matrix.name, numericType, aspectDefinitions);
        VariableType elementType = CWriterUtils.getType(matrix.name, matlabClass, aspectDefinitions, numerics);

        // VariableType elementType = VariableTypeFactoryOld.newNumeric(numericType);

        List<Integer> shape = SpecsFactory.fromIntArray(matrix.getDimensions());

        VariableType matrixType = null;
        // if (mImpl == MatrixImplementation.DECLARED) {
        if (!allowedDynamicAllocation) {
            matrixType = StaticMatrixType.newInstance(elementType, shape);
        } else {
            matrixType = DynamicMatrixType.newInstance(elementType);
        }

        // String coderType = CoderUtils.getCoderType(numericType);
        String coderType = CoderUtils.getCoderType(elementType);

        // Build temporary variable for static initialization
        String staticName = matrix.name + "_static";
        List<Integer> initShape = SpecsFactory.fromIntArray(matrix.getDimensions());
        VariableType staticType = StaticMatrixType.newInstance(elementType, initShape);
        Variable staticVar = new Variable(staticName, staticType);
        addStaticDeclaration(matrix, staticVar, instructions);

        // Add literal instruction with initialization of values
        List<String> values = instructions.getInitializations().getValues(staticName);
        // instructions.addLiteralInstruction(VariableCode.getVariableDeclaration(staticName, staticType, values) +
        // ";");
        instructions.addLiteralInstruction(CodeUtils.getDeclarationWithInputs(staticType, staticName, values) + ";");

        // Add
        String tempNumel = matrix.name + "_numels";
        instructions.addAssignment(CNodeFactory.newVariable(tempNumel, helper.getNumerics().newInt()),
                CNodeFactory.newCNumber(-1));

        // Add Coder-specific initialization
        int numDims = shape.size();

        // StringBuilder builder = new StringBuilder();

        // Matrix initialization
        String matrixInit = SpecsIo.getResource(CoderResource.MATRIX);
        matrixInit = matrixInit.replace("<CODER_TYPE>", coderType);
        matrixInit = matrixInit.replace("<VARIABLE_NAME>", matrix.name);
        matrixInit = matrixInit.replace("<NUM_DIMS>", Integer.toString(numDims));

        // builder.append(matrixInit);
        instructions.addLiteralInstruction(matrixInit);

        String sizeCalc = getSizeCalc(matrix.name, numDims);

        // Dimension allocation
        for (int i = 0; i < numDims; i++) {
            String dimInit = SpecsIo.getResource(CoderResource.INIT_DIM);
            dimInit = dimInit.replace("<NUMEL_VAR>", tempNumel);
            dimInit = dimInit.replace("<NUMEL_CALC>", sizeCalc);
            dimInit = dimInit.replace("<VAR_NAME>", matrix.name);
            dimInit = dimInit.replace("<DIM>", Integer.toString(i));
            dimInit = dimInit.replace("<DIM_SIZE>", shape.get(i).toString());
            dimInit = dimInit.replace("<CODER_TYPE>", coderType);

            instructions.addLiteralInstruction(dimInit);
        }

        // String dataInit = IoUtils.getResource(CoderResource.INIT_DATA);
        // dataInit = dataInit.replace("<NUMEL_VAR>", tempNumel);
        int totalElements = getTotalElements(shape);

        Replacer dataInit = new Replacer(SpecsIo.getResource(CoderResource.INIT_DATA));
        dataInit.replace("<NUMEL_VAR>", tempNumel);
        dataInit.replace("<TOTAL_ELEMENTS>", totalElements);
        dataInit.replace("<VAR_NAME>", matrix.name);
        dataInit.replace("<STATIC_VAR_NAME>", staticName);

        instructions.addLiteralInstruction(dataInit.toString());

        instructions.addLiteralVariable(new Variable(matrix.name, matrixType));
    }

    /**
     * Processes a matrix variable. Initializes it with a call to zeros and then assigns the correct values to each of
     * its positions.
     * 
     * @param matrix
     *            - the variable
     * @param instructions
     *            - the list of instructions
     * @param aspectDefinitions
     * @param mImpl
     */
    private void processMatrix(MLArray matrix, CInstructionList instructions) {

        InstanceBuilder helper = new GenericInstanceBuilder(pdata);

        // A newline before we declare this new variable
        instructions.addLiteralInstruction("");

        boolean dynamicAllocationAllowed = pdata.getSettings().get(CirKeys.ALLOW_DYNAMIC_ALLOCATION);
        // The variable
        // MatrixImplementation mImpl = pdata.getSetupTable().getMatrixImplementation();

        // ElementType
        // NumericType numericType = CWriterUtils.getEquivalentCNumericType(matrix);
        // Get the NumericClassName that is equivalent to the element Matlab type of this matrix variable
        NumericClassName numericClass = CWriterUtils.getEquivalentNumericClass(matrix);

        // Check if there is a type defined in the aspects for this variable
        VariableType elementType = CWriterUtils.getType(matrix.name, numericClass, aspectDefinitions, numerics);
        // Check if there is a numeric type defined in the aspects for this variable
        // numericType = CWriterUtils.updateNumericType(matrix.name, numericType, aspectDefinitions);

        // VariableType elementType = VariableTypeFactoryOld.newNumeric(numericType);

        MatrixType matrixType = null;
        List<Integer> shape = SpecsFactory.fromIntArray(matrix.getDimensions());
        if (dynamicAllocationAllowed) {
            matrixType = DynamicMatrixType.newInstance(elementType, shape);
        } else {
            matrixType = StaticMatrixType.newInstance(elementType, shape);
        }

        Variable variable = new Variable(matrix.name, matrixType);

        // If using declared implementation, use static declaration
        if (!dynamicAllocationAllowed) {
            // If number of elements is less than the threshold, implement as new array instruction, to preserve
            // information about constants
            addStaticDeclaration(matrix, variable, instructions);

            return;
        }

        // Build temporary variable for static initialization
        String staticName = variable.getName() + "_static";
        // List<Integer> shape = FactoryUtils.fromIntArray(matrix.getDimensions());
        VariableType staticType = StaticMatrixType.newInstance(elementType, shape);
        Variable staticVar = new Variable(staticName, staticType);
        addStaticDeclaration(matrix, staticVar, instructions);

        // Build zeros inputs
        List<CNode> matlabInputs = SpecsFactory.newArrayList();
        for (int dimension : matrix.getDimensions()) {
            matlabInputs.add(CNodeFactory.newCNumber(dimension));
        }
        // matlabInputs.add(CNodeFactory.newVariable(variable));
        // Get NumericClass
        matlabInputs.add(helper.newString(MatlabToCTypesUtils.getNumericClass(matrixType).getMatlabString()));

        // InstanceProvider zerosProvider = matrixType.matrix().functions().create();
        InstanceProvider zerosProvider = ArrayAllocFunctions.newConstantHelper("zeros",
                CNodeFactory.newCNumber(0));

        FunctionInstance zerosInstance = helper.getInstance(zerosProvider, CNodeUtils.getVariableTypes(matlabInputs));

        List<CNode> cInputs = SpecsFactory.newArrayList();
        cInputs.addAll(matlabInputs.subList(0, matlabInputs.size() - 1));
        // Add temporary variable
        cInputs.add(CNodeFactory.newVariable(variable));

        instructions.addFunctionCall(zerosInstance, cInputs);

        // Add free
        String tensorDataString = variable.getName() + "->" + DynamicMatrixStruct.TENSOR_DATA;
        CNode tensorData = CNodeFactory.newLiteral(tensorDataString);

        CNode sourceData = CNodeFactory.newVariable(staticVar);

        InlineCode code = tokens -> {
            MatrixType variableType = (MatrixType) variable.getType();

            return "memcpy(" + tokens.get(0).getCodeForContent(PrecedenceLevel.Comma) + ", "
                    + tokens.get(1).getCodeForContent(PrecedenceLevel.Comma) + ", sizeof("
                    + variableType.matrix().getElementType().code().getSimpleType() + ") * "
                    + variableType.getTypeShape().getNumElements() + ");";
        };
        FunctionType memcpyType = FunctionTypeBuilder.newInline()
                .addInput(tensorData.getVariableType())
                .addInput(sourceData.getVariableType())
                .returningVoid()
                .build();
        InlinedInstance memcpyInstance = new InlinedInstance(memcpyType, "$memcpy", code);
        memcpyInstance.setCustomCallIncludes(SystemInclude.String);

        instructions.addFunctionCall(memcpyInstance, tensorData, sourceData);

    }

    /**
     * Processes a scalar variable. Initializes the variable and assigns the correct value.
     * 
     * @param scalar
     *            - the variable
     * @param instructions
     *            - the list of instructions
     * @param aspectDefinitions
     */
    private void processScalar(MLArray scalar, CInstructionList instructions) {

        // Get the C type that is equivalent to the Matlab type of this scalar variable
        // NumericType type = CWriterUtils.getEquivalentCNumericType(scalar);
        // Get the NumericClassName that is equivalent to the Matlab type of this scalar variable
        NumericClassName numericClass = CWriterUtils.getEquivalentNumericClass(scalar);

        // Check if there is a type defined in the aspects for this variable
        // type = CWriterUtils.updateNumericType(scalar.name, type, aspectDefinitions);
        VariableType type = CWriterUtils.getType(scalar.name, numericClass, aspectDefinitions, numerics);

        // Get the number
        CNumber cNumber = getMLArrayCNumber(scalar, 0);
        // NumericData numericData = NumericDataFactory.newInstance(type, cNumber.getNumber());
        type = ScalarUtils.setConstant(type, cNumber.getNumber());

        // CToken variableT = CTokenFactory.newVariable(scalar.name, VariableTypeFactoryOld.newNumeric(numericData));
        CNode variableT = CNodeFactory.newVariable(scalar.name, type);
        CNode assignmentT = CNodeFactory.newAssignment(variableT, CNodeFactory.newCNumber(cNumber));

        instructions.addAssignment(assignmentT);
    }

    /**
     * @param matrix
     * @param matrixVariable
     * @param instructions
     * @return
     */
    private void addStaticDeclaration(MLArray matrix, Variable matrixVariable, CInstructionList instructions) {

        // Add initialization values
        // List<String> values = FactoryUtils.newArrayList();

        // Calculate cumulative product for subscript calculation
        List<Integer> cumprod = Collections.emptyList();

        MemoryLayout memoryLayout = pdata.getMemoryLayout();
        if (memoryLayout == MemoryLayout.ROW_MAJOR) {
            cumprod = newCumulativeProduct(matrix);
        }

        // System.out.println("SIZE:"+Arrays.toString(matrix.getDimensions()));
        // System.out.println("CUMPROD:"+cumprod);

        for (int i = 0; i < matrix.getSize(); i++) {
            // Parse index
            // List<Integer> subscripts = getSubscriptsRowMajor(i, cumprod);
            // Get equivalent MATLAB index
            // int matlabIndex = sub2indColumnMajor(subscripts, matrix.getDimensions());

            int matlabIndex = indRow2IndCol(i, cumprod, matrix.getDimensions(), memoryLayout);
            /*
            int matlabIndex = i;
            
            // int matlabIndex = indRow2IndCol(i, cumprod, matrix.getDimensions());
            */
            // System.out.println("INDEX: "+i+"; Subs: "+subscripts+"; MAT INDEX: " + matlabIndex);

            int index = parseIndex(matrix, matlabIndex);

            CNumber number = getMLArrayCNumber(matrix, index);
            String valueString = number.toCString();
            // values.add(valueString);
            instructions.getInitializations().append(matrixVariable.getName(), valueString);
        }

        // instructions.getInitializations().add(matrixVariable.getName(), values);

        // Add variable
        instructions.addLiteralVariable(matrixVariable);

    }

    /**
     * @param i
     * @param cumprod
     * @param dimensions
     * @param memoryLayout2
     * @return
     */
    public static int indRow2IndCol(int index, List<Integer> cumprod, int[] dimensions, MemoryLayout memoryLayout) {

        if (memoryLayout == MemoryLayout.COLUMN_MAJOR) {
            return index;
        }

        if (memoryLayout != MemoryLayout.ROW_MAJOR) {
            throw new RuntimeException("Case not defined:" + memoryLayout);
        }

        // Parse index
        List<Integer> subscripts = getSubscriptsRowMajor(index, cumprod);

        // Get equivalent MATLAB index
        int colIndex = sub2indColumnMajor(subscripts, dimensions);

        return colIndex;
    }

    /**
     * @param matrix
     * @return
     */
    public static List<Integer> newCumulativeProduct(MLArray matrix) {
        List<Integer> cumprod = SpecsFactory.newArrayList();
        int currentProd = 1;
        cumprod.add(currentProd);
        for (int i = matrix.getNDimensions() - 1; i >= 1; i--) {
            currentProd *= matrix.getDimensions()[i];
            cumprod.add(currentProd);
        }

        Collections.reverse(cumprod);
        return cumprod;
    }

    /**
     * @param subscripts
     * @param dimensions
     * @return
     */
    private static int sub2indColumnMajor(List<Integer> subscripts, int[] dimensions) {
        int currentIndex = subscripts.get(0);
        int accProd = 1;
        for (int i = 1; i < subscripts.size(); i++) {
            accProd *= dimensions[i - 1];
            currentIndex += subscripts.get(i) * accProd;
        }

        return currentIndex;
    }

    /**
     * @param index
     * @param cumprod
     *            a list of cumulative products of the size, higher to lower
     * @return
     */
    private static List<Integer> getSubscriptsRowMajor(int index, List<Integer> cumprod) {
        List<Integer> subscripts = SpecsFactory.newArrayList();

        int currentIndex = index;
        for (int i = 0; i < cumprod.size(); i++) {
            // Get coordinate
            int subscript = currentIndex / cumprod.get(i);
            subscripts.add(subscript);

            // Prepare index
            currentIndex = currentIndex % cumprod.get(i);
            /*
            // Get remainder
            int rem = currentIndex % cumprod.get(i);
            System.out.println("CURRENTINDX:"+currentIndex);
            System.out.println("CUMPROD:"+cumprod.get(i));
            System.out.println("REM:"+rem);
            // Subtract remainder from tota
            int subtracted = currentIndex - rem;
            // Divide by product
            int subscript = subtracted / cumprod.get(i);
            
            subscripts.add(subscript);
            currentIndex = subtracted;
            System.out.println("Subtracted:"+subtracted);
            */
        }

        return subscripts;
    }

    /**
     * @param shape
     * @return
     */
    private static int getTotalElements(List<Integer> shape) {
        int acc = shape.get(0);

        for (int i = 1; i < shape.size(); i++) {
            acc *= shape.get(i);
        }

        return acc;
    }

    /**
     * @param name
     * @param numDims
     * @return
     */
    private static String getSizeCalc(String name, int numDims) {
        StringBuilder builder = new StringBuilder();

        if (numDims > 0) {
            builder.append(name).append("->size[0]");
        }

        for (int i = 1; i < numDims; i++) {
            builder.append(" * ").append(name).append("->size[").append(i).append("]");
        }

        return builder.toString();
    }

    /**
     * Builds a CNumber from a MLArray variable and the index ( for matrix accesses ).
     * 
     * @param variable
     * @param index
     * @return
     */
    public static CNumber getMLArrayCNumber(MLArray variable, int index) {

        switch (variable.getType()) {
        case MLArray.mxDOUBLE_CLASS:
            double d = ((MLDouble) variable).get(index);
            return CLiteral.newReal(d);
        case MLArray.mxSINGLE_CLASS:
            float single = ((MLSingle) variable).get(index);
            return CLiteral.newReal(new Float(single));
        case MLArray.mxINT8_CLASS:
            byte b = ((MLInt8) variable).get(index);
            return CLiteral.newInteger(b);
        case MLArray.mxUINT8_CLASS:
            byte u_b = ((MLUInt8) variable).get(index);
            return CLiteral.newInteger(u_b);
        case MLArray.mxINT16_CLASS:
            short s = ((MLInt16) variable).get(index);
            return CLiteral.newInteger(s);
        case MLArray.mxUINT16_CLASS:
            short u_s = ((MLUInt16) variable).get(index);
            return CLiteral.newInteger(u_s);
        case MLArray.mxINT32_CLASS:
            int i = ((MLInt32) variable).get(index);
            return CLiteral.newInteger(i);
        case MLArray.mxUINT32_CLASS:
            int u_i = ((MLUInt32) variable).get(index);
            return CLiteral.newInteger(u_i);
        case MLArray.mxINT64_CLASS:
            long l = ((MLInt64) variable).get(index);
            return CLiteral.newInteger(l);
        case MLArray.mxUINT64_CLASS:
            long u_l = ((MLUInt64) variable).get(index);
            return CLiteral.newInteger(u_l);
        case MLArray.mxCHAR_CLASS:
            // System.out.println("CHAR:" + Arrays.asList(((MLChar) variable).exportChar()));
            char c = ((MLChar) variable).getChar(index, 0);
            return CLiteral.newInteger(c);
        default:
            throw new RuntimeException("Case not supported on 'getMLArrayCNumber': '" + variable.getType() + "'.");
        }
    }

    /**
     * @param variable
     * @param index
     * @return
     */
    private int parseIndex(MLArray variable, int index) {
        MemoryLayout memoryLayout = pdata.getMemoryLayout();

        // If column-major, do nothing
        if (memoryLayout == MemoryLayout.COLUMN_MAJOR) {
            return index;
        }

        // If row-major, translate
        if (memoryLayout == MemoryLayout.ROW_MAJOR) {
            // Transform index
            return index;
        }

        throw new RuntimeException("Case not defined:" + memoryLayout);
    }

}
