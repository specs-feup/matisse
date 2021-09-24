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

package org.specs.MatlabToCTester.Auxiliary;

import java.util.List;

/**
 * @author Joao Bispo
 * 
 */
public class CompilerOptions {

    // The compiler to use (fullpath or name if already in PATH)
    private final String compiler;
    // The optimization flag
    private final String optimizationFlag;
    // The compiler flags to be used
    private final List<String> compilerFlags;
    // The compiler flags to be used only on Linux
    private final List<String> linuxOnlyCompilerFlags;
    private final List<String> linkerFlags;

    public CompilerOptions(String compiler, String optimizationOption, List<String> compilerOptions,
	    List<String> linuxOnlyCompilerFlags,
	    List<String> linkerFlags) {
	this.compiler = compiler;
	this.optimizationFlag = optimizationOption;
	this.compilerFlags = compilerOptions;
	this.linuxOnlyCompilerFlags = linuxOnlyCompilerFlags;
	this.linkerFlags = linkerFlags;
    }

    /**
     * @return the compiler
     */
    public String getCompiler() {
	return this.compiler;
    }

    /**
     * @return the compilerOptions
     */
    public List<String> getCompilerFlags() {
	return this.compilerFlags;
    }

    /**
     * @return the compilerOptions
     */
    public List<String> getLinuxOnlyCompilerFlags() {
	return this.linuxOnlyCompilerFlags;
    }

    /**
     * @return the optimizationOption
     */
    public String getOptimizationFlag() {
	return this.optimizationFlag;
    }

    public List<String> getLinkerFlags() {
	return this.linkerFlags;
    }

}
