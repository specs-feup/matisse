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

package org.suikasoft.CMainFunction.Tester;

import java.io.File;
import java.util.Collections;
import java.util.List;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.utilities.LineStream;

public class ScriptExeToSh {

    /*
    interface I1 {
    int m();
    }

    interface I2 {
    default int m() {
        return 0;
    }
    }

    class A implements I1, I2 {
    @Override
    public int m() {
        // TODO Auto-generated method stub
        return I2.super.m();
    }
    }
    */

    public static void main(String[] args) {

	// ProcessUtils.getNanoTime(() -> System.out.println("HELLO"));
	// Runnable r = () -> System.out.println("HELLO");

	File file = new File("C:\\Users\\JoaoBispo\\Desktop\\slow.sh");

	StringBuilder builder = new StringBuilder();

	List<String> lines = LineStream.readLines(file);
	Collections.sort(lines);
	for (String line : lines) {
	    int separator = line.lastIndexOf('/');
	    String folder = line.substring(0, separator);
	    String filename = line.substring(separator + 1, line.length());

	    builder.append("echo \"").append(filename).append("\"\n");
	    builder.append("cd ").append(folder).append("\n");
	    builder.append("./").append(filename).append("\n");
	    builder.append("cd ../../..\n");
	}

	File outputFile = new File(file.getParent(), file.getName() + ".new");

	SpecsIo.write(outputFile, builder.toString());
    }
}
