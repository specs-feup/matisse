/**
 * Copyright 2014 SPeCS.
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

package org.specs.MatlabToC.VariableStorage;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.specs.CIR.CirKeys;
import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.FunctionInstance.InstanceBuilder.InstanceBuilder;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.CIR.Tree.CNode;
import org.specs.CIR.Tree.CNodeUtils;
import org.specs.CIR.Tree.CNodes.CNodeFactory;
import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Scalar.ScalarUtils;
import org.specs.CIRTypes.Types.DynamicMatrix.DynamicMatrixType;
import org.specs.CIRTypes.Types.Numeric.NumericFactory;
import org.specs.CIRTypes.Types.StaticMatrix.StaticMatrixType;
import org.specs.Matisse.Matlab.TypesMap;
import org.specs.MatlabIR.MatlabLanguage.NumericClassName;
import org.specs.MatlabToC.MatlabToCTypesUtils;
import org.specs.MatlabToC.CCode.CWriterUtils;

import com.google.common.collect.Lists;
import com.jmatio.io.MLInt32;
import com.jmatio.types.MLArray;
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
import pt.up.fe.specs.util.SpecsLogs;

public class CPersistenceUtils {
    /**
     * Extracts big variables to data files.
     * 
     * @param variables
     *            The list of variables that may be extracted.
     * @param instructions
     *            The instructions that import the extracted files.
     * @param outputFolder
     *            The folder to contain the generated data files.
     * @return The list of variables that were not exported.
     */
    public static List<MLArray> extractBigMatrices(List<MLArray> variables, CInstructionList instructions,
            File outputFolder, ProviderData pdata, TypesMap aspectDefinitions, boolean generateFile) {

        List<MLArray> remainingVariables = Lists.newArrayList();

        for (MLArray variable : variables) {
            if (isBigEnoughToExtract(variable)) {
                buildVariableExtractionCode(pdata, variable, aspectDefinitions, instructions);
                buildVariableFile(variable, outputFolder, generateFile);
            } else {
                remainingVariables.add(variable);
            }
        }

        return remainingVariables;
    }

    public static void buildVariableFile(MLArray variable, File outputFolder, boolean generateFile) {
        File outputFile = new File(SpecsIo.mkdir(outputFolder), variable.getName() + ".dat");
        SpecsLogs.msgLib("Saving " + outputFile);
        if (generateFile) {
            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));) {

                saveMatrix(variable, out);

            } catch (IOException e) {
                SpecsLogs.msgSevere(e.getMessage());
            }
        }
    }

    private static void saveMatrix(MLArray variable, OutputStream out) throws IOException {
        for (int i = 0; i < variable.getSize(); ++i) {
            saveElement(variable, i, out);
        }
    }

    private static void saveElement(MLArray variable, int index, OutputStream out) throws IOException {
        switch (variable.getType()) {
        case MLArray.mxDOUBLE_CLASS:
            double d = ((MLDouble) variable).get(index);
            saveDouble(d, out);
            break;
        case MLArray.mxSINGLE_CLASS:
            float single = ((MLSingle) variable).get(index);
            saveSingle(single, out);
            break;
        case MLArray.mxINT8_CLASS:
            byte b = ((MLInt8) variable).get(index);
            saveInt8(b, out);
            break;
        case MLArray.mxUINT8_CLASS:
            byte u_b = ((MLUInt8) variable).get(index);
            saveInt8(u_b, out);
            break;
        case MLArray.mxINT16_CLASS:
            short s = ((MLInt16) variable).get(index);
            saveInt16(s, out);
            break;
        case MLArray.mxUINT16_CLASS:
            short u_s = ((MLUInt16) variable).get(index);
            saveInt16(u_s, out);
            break;
        case MLArray.mxINT32_CLASS:
            int i = ((MLInt32) variable).get(index);
            saveInt32(i, out);
            break;
        case MLArray.mxUINT32_CLASS:
            int u_i = ((MLUInt32) variable).get(index);
            saveInt32(u_i, out);
            break;
        case MLArray.mxINT64_CLASS:
            long l = ((MLInt64) variable).get(index);
            saveInt64(l, out);
            break;
        case MLArray.mxUINT64_CLASS:
            long u_l = ((MLUInt64) variable).get(index);
            saveInt64(u_l, out);
            break;
        // TODO: Figure out correct CHAR representation
        default:
            throw new RuntimeException("Case not supported on 'saveElement': '"
                    + MLArray.typeToString(variable.getType()) + "', for " + variable.getName());
        }
    }

    private static void saveSingle(float single, OutputStream out) throws IOException {
        saveInt32(Float.floatToRawIntBits(single), out);
    }

    private static void saveDouble(double d, OutputStream out) throws IOException {
        saveInt64(Double.doubleToRawLongBits(d), out);
    }

    private static void saveInt64(long l, OutputStream out) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(l);
        byte[] bytes = byteBuffer.array();
        out.write(bytes);
    }

    private static void saveInt32(int i, OutputStream out) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(i);
        byte[] bytes = byteBuffer.array();
        out.write(bytes);
    }

    private static void saveInt16(short s, OutputStream out) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(s);
        byte[] bytes = byteBuffer.array();
        out.write(bytes);
    }

    private static void saveInt8(byte b, OutputStream out) throws IOException {
        out.write(b);
    }

    private static void buildVariableExtractionCode(ProviderData pdata, MLArray matrix, TypesMap aspectDefinitions,
            CInstructionList instructions) {

        boolean dynamicAllocationAllowed = pdata.getSettings().get(CirKeys.ALLOW_DYNAMIC_ALLOCATION);

        NumericClassName numericClass = CWriterUtils.getEquivalentNumericClass(matrix);

        NumericFactory numerics = pdata.getNumerics();
        VariableType varType = CWriterUtils.getType(matrix.name, numericClass, aspectDefinitions, numerics);

        VariableType originalType = MatlabToCTypesUtils.getVariableType(numericClass, numerics);

        MatrixType matrixType = null;
        List<Integer> shape = SpecsFactory.fromIntArray(matrix.getDimensions());

        if (!dynamicAllocationAllowed) {
            matrixType = StaticMatrixType.newInstance(varType, shape);
        } else {
            matrixType = DynamicMatrixType.newInstance(varType, shape);
        }

        // Create builder
        // InstanceBuilder helper = new GenericInstanceBuilder(pdata.getSetupRaw());

        int charSize = pdata.getNumerics().getSizes().getCharSize();

        // Create input nodes
        List<CNode> inputNodes = new ArrayList<>();
        inputNodes.add(CNodeFactory.newString(matrix.name, charSize));
        inputNodes.addAll(getDimValues(pdata.getNumerics(), shape));

        ProviderData loadData = pdata.create(CNodeUtils.getVariableTypes(inputNodes));
        // Add output as input, otherwise it will create a temporary variable
        inputNodes.add(CNodeFactory.newVariable(matrix.name, matrixType));

        // Set output type for LoadMatrixVariable
        loadData.setOutputType(matrixType);

        InstanceBuilder builder = new LoadMatrixVariable(loadData, ScalarUtils.cast(originalType));
        instructions.addFunctionCall(builder, inputNodes);
        // InstanceProvider provider = LoadMatrixVariable.newInstance(ScalarUtils.cast(originalType));
        // CNode value = helper.getFunctionCall(provider, inputNodes);

        // instructions.addInstruction(value);

    }

    private static List<CNode> getDimValues(NumericFactory numericFactory, List<Integer> shape) {
        List<CNode> dimNodes = new ArrayList<>();

        VariableType intType = numericFactory.newInt();

        for (int i = 0; i < shape.size(); i++) {
            dimNodes.add(CNodeFactory.newCNumber(shape.get(i), intType));
        }

        return dimNodes;

    }

    private static boolean isBigEnoughToExtract(MLArray variable) {
        final long LIMIT_SIZE_IN_BITS = 256 * 8;

        return getSizeOfVariableInBits(variable) > LIMIT_SIZE_IN_BITS;
    }

    private static long getSizeOfVariableInBits(MLArray variable) {
        if (variable.isCell()) {
            throw new UnsupportedOperationException("Cell arrays are not currently supported");
        }

        long sizeInBits = getSizeOfElementInBits(variable) * variable.getSize();

        return sizeInBits;
    }

    private static long getSizeOfElementInBits(MLArray variable) {
        if (variable.isLogical()) {
            return 1;
        }
        if (variable.isInt8() || variable.isUint8()) {
            return 8;
        }
        if (variable.isChar() || variable.isInt16() || variable.isUint16()) {
            return 16;
        }
        if (variable.isInt32() || variable.isUint32() || variable.isSingle()) {
            return 32;
        }
        if (variable.isInt64() || variable.isUint64() || variable.isDouble()) {
            return 64;
        }
        if (variable.isComplex()) {
            return 128;
        }

        throw new UnsupportedOperationException(variable.getName());
    }
}
