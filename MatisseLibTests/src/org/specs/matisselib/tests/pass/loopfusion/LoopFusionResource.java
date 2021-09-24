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

package org.specs.matisselib.tests.pass.loopfusion;

import java.util.Locale;

import pt.up.fe.specs.util.providers.ResourceProvider;

public enum LoopFusionResource implements ResourceProvider {
    SIMPLE,
    SIMPLE2D_CONSTANT,
    SIMPLE2D_NO_FUSE,
    SIZE_AWARE,
    BRANCH_IN_FIRST_LOOP,
    BRANCH_IN_FIRST_LOOP2,
    NESTED,
    XBREAK,
    DIFFERENT_DEPTHS,
    DIFFERENT_DEPTHS2,
    DIFFERENT_DEPTHS3,
    DIFFERENT_DEPTHS4,
    DIFFERENT_DEPTHS5,
    DIFFERENT_DEPTHS6,
    UNUSED_VARIABLE,
    UNUSED_VARIABLE2,
    UNRELATED;

    private static final String path = "pass/loopfusion";

    @Override
    public String getResource() {
        return LoopFusionResource.path + "/" + name().toLowerCase(Locale.UK) + ".m";
    }

    public String getExpectedResource() {
        return LoopFusionResource.path + "/" + name().toLowerCase(Locale.UK) + ".txt";
    }
}
