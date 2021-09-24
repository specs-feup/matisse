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

package org.specs.MatlabToC;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.specs.CIR.FunctionInstance.ProviderData;
import org.specs.CIR.Tree.CInstructionList;
import org.specs.JMatIOPlus.MatFile;
import org.specs.Matisse.Matlab.TypesMap;
import org.specs.MatlabToC.MatToBinaryCLib.MatToBinaryCInputs;
import org.specs.MatlabToC.VariableStorage.CPersistenceUtils;

import com.jmatio.types.MLArray;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;

/**
 * @author Joao Bispo
 *
 */
public class MatToBinaryC {

    /**
     * Reads a MATLAB file and generates a binary file that MATISSE main understands. Can pass a folder as second
     * argument.
     * 
     * @param args
     */
    public static void main(String[] args) {
        SpecsSystem.programStandardInit();

        Optional<MatToBinaryCInputs> inputs = parseArgs(args);
        if (!inputs.isPresent()) {
            return;
        }

        File outputFolder = inputs.get().getOutputFolder();
        ProviderData data = ProviderData.newInstance("mat-to-binary-c");

        CPersistenceUtils.extractBigMatrices(inputs.get().getVariables(),
                new CInstructionList(),
                outputFolder,
                ProviderData.newInstance(data.getSettings()),
                new TypesMap(), // FIXME
                true);

    }

    /**
     * @param args
     * @return
     */
    private static Optional<MatToBinaryCInputs> parseArgs(String[] args) {

        if (args.length == 0) {
            SpecsLogs.msgInfo("No arguments given. Please specify the .mat file.");
            SpecsLogs.msgInfo("Usage: mat2binaryc <MAT_FILE> [<OUTPUT_FOLDER>]");
            return Optional.empty();
        }

        if (args.length > 2) {
            SpecsLogs.msgInfo("Found more than two arguments, ignoring them.");
        }

        // Get file
        File matFile = SpecsIo.existingFile(args[0]);
        if (matFile == null) {
            return Optional.empty();
        }

        // Get output folder
        File outputFolder = SpecsIo.getWorkingDir();
        if (args.length > 1) {
            File newOutputFolder = SpecsIo.mkdir(args[1]);
            if (newOutputFolder != null) {
                outputFolder = newOutputFolder;
            }
        }

        // TODO Auto-generated method stub
        // return null;
        // Read the .MAT file
        MatFile file = new MatFile(matFile);
        file.read();

        List<MLArray> variables = file.getVariables();

        return Optional.of(new MatToBinaryCInputs(variables, outputFolder));
    }

}
