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

package org.specs.MatlabToC;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.specs.MatlabToC.Functions.MatissePrimitives.CompatibilityPackageResource;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.providers.ResourceProvider;

public class CreateAuxiWithMatissePrimitives {

    public static void main(String[] args) {
	File outputFolder = new File(
		"C:\\Users\\JoaoBispo\\Dropbox\\Research\\Work\\2014-11-04 Journal Benchmarks Results\\matisse\\auxi_no_primitives");

	List<String> benchmarks = Arrays.asList(
		"capacitor",
		"cfd3_v1",
		"cfd3_v2",
		"conv_2",
		"copy_vs_pointer",
		"dilate",
		"dirich",
		"dotprod",
		"dotprod_v2",
		"editdist",
		"fft2d",
		"fft2d_v2",
		"fir_1d",
		"gauss",
		"grid_iterate",
		"hypotenuse_idiomatic",
		"latnrm",
		"monte_carlo_option_pricing",
		"nbody1d",
		"nbody3d",
		"rgb2yuv",
		"rgb2yuv_v2",
		"seidel",
		"subband_2d",
		"tridiagonal",
		"adapt",
		"closure",
		"crnich",
		"diffraction",
		"fdtd",
		"finediff"
		);

	// For each benchmark, copy the resources to a folder with the same name
	for (String benchmark : benchmarks) {
	    File benchFolder = SpecsIo.mkdir(outputFolder, benchmark);

	    for (ResourceProvider resource : CompatibilityPackageResource.values()) {
		File outputFile = new File(benchFolder, resource.getResourceName());
		SpecsIo.write(outputFile, SpecsIo.getResource(resource));
	    }
	}

    }
}
