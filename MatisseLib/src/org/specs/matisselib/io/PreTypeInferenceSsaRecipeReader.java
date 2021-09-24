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

package org.specs.matisselib.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.specs.matisselib.ssa.SsaPass;
import org.specs.matisselib.ssa.SsaRecipe;
import org.specs.matisselib.ssa.SsaRecipeBuilder;

public class PreTypeInferenceSsaRecipeReader {
    public static final List<String> DEFAULT_PASS_PACKAGES = Arrays.asList("org/specs/matisselib/passes/ssa");
    public static final String RECIPE_VERSION = "untyped-ssa v2";

    private PreTypeInferenceSsaRecipeReader() {
    }

    public static SsaRecipe read(File file, List<String> passPackages) throws IOException {
        Map<String, Class<? extends SsaPass>> passClasses = getPassClasses(passPackages);

        try (RawRecipeReader<SsaPass> reader = new RawRecipeReader<>(
                new FileInputStream(file),
                PreTypeInferenceSsaRecipeReader.RECIPE_VERSION,
                passClasses::get)) {

            SsaRecipeBuilder builder = new SsaRecipeBuilder();

            SsaPass pass;
            while ((pass = reader.readPass()) != null) {
                builder.addPass(pass);
            }

            return builder.getRecipe();
        }
    }

    public static HashMap<String, Class<? extends SsaPass>> getPassClasses(List<String> passPackages)
            throws IOException {

        return ClassFinderHelper.findClasses(passPackages, SsaPass.class);
    }
}
