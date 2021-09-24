/**
 * Copyright 2015 SPeCS.
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

package org.specs.MatlabProcessor;

import org.junit.Test;

import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.properties.SpecsProperty;

public class TestCase {

    @Test
    public void test() {
        SpecsSystem.programStandardInit();
        SpecsProperty.ShowStackTrace.applyProperty("true");

        // String filepath =
        // "C:\\Users\\JoaoBispo\\Dropbox\\Code-Repositories\\FiniteElementAnalysis\\sofea\\meshing\\bridge.m";

        // FileNode node = MatlabProcessorUtils.fromMFile(new File(filepath));

        // "single \""
        // "single ["
        // "mex CFLAGS=\"\\$CFLAGS -std=c99\" -largeArrayDims libsvmread.c"
        // "svm_Cs = [10^-3 10^-3.5 10^-2.5]; %10.^(-2.5:-0.5:-3.5);"
        // "A = 1 + 3^4^-5"
        // addpath ../foo

        /*
        try {
        System.in.read();
        } catch (IOException e) {
        LoggingUtils.msgWarn("Error message:\n", e);
        }
        long tic = System.nanoTime();
        MatlabProcessorUtils.fromMFile(new File(filepath));
        long toc = System.nanoTime();
        System.out.println("Time:" + ParseUtils.parseTime(toc - tic));
        */

        /*
        System.out
        	.println(MatlabProcessorUtils.toMFile(MatlabProcessorUtils
        		.fromMFile("C:\\Users\\JoaoBispo\\Dropbox\\Code-Repositories\\AMADEUS-Benchs\\dft-fft-cos-sin\\explore-dft-fft-cos-sin\\sin1.m")));
        System.out.println(MatlabProcessorUtils.toMFile(MatlabProcessorUtils
        	.fromMString("ls arg1 arg2;")));
        
        */

        /*
        File folder = new File("C:\\Users\\JoaoBispo\\Work\\SourceForge MATLAB - Unzipped");
        
        // Iterate over all files in folder, delete every file that has not .m extension
        List<File> files = IoUtils.getFilesRecursive(folder);
        
        System.out.println("----- Deleting files -------");
        files.stream()
        	.filter(file -> !IoUtils.getExtension(file).equals("m"))
        	.forEach(file -> {
        	    System.out.println("Deleting:" + file.getAbsolutePath());
        	    boolean success = file.delete();
        	    if (!success) {
        		System.out.println("!PROBLEM");
        	    }
        	});
        
        System.out.println("\n\n----- Deleting folders -------");
        // Delete empty folders after
        List<File> folders = IoUtils.getFoldersRecursive(folder);
        folders.stream()
        	.filter(emptyFolder -> emptyFolder.list().length == 0)
        	.forEach(emptyFolder -> {
        	    System.out.println("Deleting:" + emptyFolder.getAbsolutePath());
        	    boolean success = emptyFolder.delete();
        	    if (!success) {
        		System.out.println("!PROBLEM");
        	    }
        	});
        	*/
    }
}
