/*
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

package org.suikasoft.CMainFunction.Tester.App;

import java.io.File;

import pt.up.fe.specs.util.io.InputFiles;

/**
 * Data fields for EclipseDeployment.
 * 
 * @author Joao Bispo
 */
public class AppTesterData {

    // public final File baseInputFolder;
    public final InputFiles inputs;
    public final File output;
    public final Integer iterations;
    public final Double minimumTimeSec;
    public final Integer numRepeats;

    public AppTesterData(InputFiles inputs, File output, Integer executions, Double minimumTimeSec, Integer numRepeats) {

	// this.baseInputFolder = baseInputFolder;
	this.inputs = inputs;
	this.output = output;
	this.iterations = executions;
	this.minimumTimeSec = minimumTimeSec;
	this.numRepeats = numRepeats;
    }

}
