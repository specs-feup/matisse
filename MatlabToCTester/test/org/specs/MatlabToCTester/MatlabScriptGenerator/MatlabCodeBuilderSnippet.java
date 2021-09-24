package org.specs.MatlabToCTester.MatlabScriptGenerator;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.specs.JMatIOPlus.MatFile;
import org.specs.MatlabToCTester.Outputs.OutputVariable;
import org.specs.MatlabToCTester.Outputs.TestCaseOutput;
import org.suikasoft.jOptions.Interfaces.DataStore;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.system.ProcessOutputAsString;

public class MatlabCodeBuilderSnippet {

    public void test() {
        assertSame("Message", "a", "a");
    }

    public void stringToMat() {

        String output = SpecsIo.read(new File("C:\\temp_dir", "c_out.txt"));

        ProcessOutputAsString processOutput = new ProcessOutputAsString(0, output, "");

        File outFile = new File("C:\\temp_dir", "cOutput.mat");

        // Create the file
        // MatFile matFile = new MatFile(saveDirectory.getPath(), "cOutput.mat");
        MatFile matFile = new MatFile(outFile);

        TestCaseOutput processedOutput = TestCaseOutput.readTestCaseOutput("aName", processOutput,
                DataStore.newInstance("empty_setup"), outFile);

        // Iterate all the output variables
        for (OutputVariable outputVariable : processedOutput.getOutputVariables()) {
            // Create a new matlab variable and add it to the list
            matFile.addVariable(outputVariable.toMLArray());
        }

        // Write the matlab data file
        matFile.write();
    }

    @Test
    public void countInputVectors() {
        SpecsSystem.programStandardInit();

        File startFolder = new File("D:\\Dropbox\\MatlabCompiler\\MatlabToCTests");

        List<File> folderList = SpecsIo.getFoldersRecursive(startFolder);

        long totalInputVectors = 0;
        for (File folder : folderList) {
            if (!folder.getName().equals("input_vectors")) {
                continue;
            }

            SpecsLogs.msgInfo("Found input vector folder '" + folder.getPath() + "'");

            // Found input vectors folder. Count all .M files inside
            totalInputVectors += SpecsIo.getFilesRecursive(folder, ".m").size();
        }

        System.out.println("Total Input Vectors:" + totalInputVectors);

    }
}
