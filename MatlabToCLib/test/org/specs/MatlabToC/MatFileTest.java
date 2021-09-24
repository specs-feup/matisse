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

import static org.junit.Assert.*;

import java.io.File;
import java.math.BigInteger;

import org.junit.Test;
import org.specs.CIR.CodeGenerator.CodeGeneratorUtils;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.JMatIOPlus.MatFile;
import org.specs.JMatIOPlus.MatUtils;
import org.specs.Matisse.Matlab.TypesMap;
import org.specs.MatlabToC.CCode.CWriter;
import org.suikasoft.jOptions.Interfaces.DataStore;

import com.jmatio.types.MLChar;
import com.jmatio.types.MLUInt64;

import pt.up.fe.specs.util.SpecsIo;

/**
 * @author Joao Bispo
 * 
 */
public class MatFileTest {

    public void testToMatlabCode() {
        String matDir = "C:\\temp_dir";
        String matFileName = "inputv.mat";
        MatFile matFile = new MatFile(new File(matDir, matFileName));

        matFile.read();
        // System.out.println(matFile.contentsAsMatlabCode());

        File matlabFile = new File(matDir, "inputv.m");
        matFile.writeToMatlabFileAsFunction(matlabFile);
    }

    @Test
    public void testByteParsingBigInt() {
        String binaryString = MatUtils.getUnsignedBinaryV2("255", 8);
        byte aByte = Byte.parseByte(binaryString, 2);

        assertEquals(-1, aByte);

        assertEquals(-21, Byte.parseByte(MatUtils.getUnsignedBinaryV2("235", 8), 2));

    }

    @Test
    public void testByteParsingShort() {
        String binaryString = MatUtils.getUnsignedBinaryV2("65535", 16);
        short aShort = Byte.parseByte(binaryString, 2);

        assertEquals(-1, aShort);
    }

    @Test
    public void testByteBI() {
        // BigInteger bI = new BigInteger("255");

        // System.out.println(bI.byteValue());

        // BigInteger bI2 = new BigInteger("65535");

        // System.out.println(bI2.shortValue());

        assertEquals(-1, -1);
    }

    @Test
    public void testUnsignedByte() {
        // String aString = "255";
        String aString = "9223372036854775808";
        BigInteger a = new BigInteger(aString);
        // System.out.println("255 string:"+Long.toBinaryString(Long.decode("255")));
        // System.out.println("-1 string:"+Long.toBinaryString(Byte.decode("-1")));

        int[] dims = new int[2];
        dims[0] = 1;
        dims[1] = 1;

        MLUInt64 variable = new MLUInt64("UNSIGNED_TEST_C", dims);
        variable.set(a.longValue(), 0);

        // Create the file
        MatFile matFile = new MatFile(new File(SpecsIo.getWorkingDir().getAbsolutePath(), "cOutput.mat"));

        // Create a new matlab variable and add it to the list
        matFile.addVariable(variable);

        // Write the matlab data file
        matFile.write();

        // System.out.println("OUTPUT DIR:\n" + IoUtils.getWorkingDir().getAbsolutePath());
    }

    public void testUnsignedByteStress() {
        for (long i = 0; i < 64000; i++) {
            String a = Long.toBinaryString(i);
            BigInteger b = new BigInteger(a);
            long c = b.longValue();
            System.out.println(c);
        }
    }

    public void testContentsAsCCode() {

        // Read the contents of the mat file
        MatFile file = new MatFile(new File("C:\\temp_dir", "test.mat"));
        file.read();

        // Convert the contents to C code and print them
        CWriter cwriter = new CWriter(DataStore.newInstance("Default MatFileTest"), new TypesMap());
        CInstructionList cCode = cwriter.contentsAsCCode(file.getVariables(), false);
        // CInstructionList cCode = file.contentsAsCCode(new TypesMap(),
        // SimpleSetup.newInstance("Default MatFileTest"));
        String codeString = CodeGeneratorUtils.functionImplementation(cCode);

        System.out.println(codeString);
    }

    public void testWriteString() {

        // Create the string
        String testString = "Hello World!";
        MLChar charMatrix = new MLChar("HWS", testString);

        // Create the .MAT file and add the string variable to it
        MatFile file = new MatFile(new File("C:\\temp_dir", "string_test.mat"));
        file.addVariable(charMatrix);

        // Write the file to the disk
        assertEquals(0, file.write());
    }

    // @Test
    public void testMatToM() {
        MatFile file = new MatFile(new File(
                "C:\\Users\\user\\Dropbox\\MatlabCompiler\\MatlabToCTests\\MatrixPower\\input_vectors\\matrix_power",
                "matrix_25_x_25_10.mat"));

        file.read();

        System.out.println(file.contentsAsMatlabCode());
    }
}
