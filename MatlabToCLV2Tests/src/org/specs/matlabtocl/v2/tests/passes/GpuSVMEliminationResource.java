/**
 * Copyright 2017 SPeCS.
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

package org.specs.matlabtocl.v2.tests.passes;

import java.util.Locale;

import pt.up.fe.specs.util.providers.ResourceProvider;

public enum GpuSVMEliminationResource implements ResourceProvider {
    USE_SVM_THEN_COPY_TO_GPU,
    USE_SVM_THEN_RETURN_NO_ADDED_COPIES,
    DISPARITY_3_BUG;

    private static final String PATH = "optimizer/gpusvmelimination/";

    @Override
    public String getResource() {
        return PATH + name().toLowerCase(Locale.UK) + ".txt";
    }
}
