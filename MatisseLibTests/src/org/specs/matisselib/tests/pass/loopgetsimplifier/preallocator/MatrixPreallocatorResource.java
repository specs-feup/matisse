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

package org.specs.matisselib.tests.pass.loopgetsimplifier.preallocator;

import java.util.Locale;

import pt.up.fe.specs.util.providers.ResourceProvider;

public enum MatrixPreallocatorResource implements ResourceProvider {
    SIMPLE1D,
    SIMPLE1DX2,
    SIMPLE1D_DERIVED,
    SIMPLE1D_DROP_DERIVED,
    SIMPLE2D,
    SIMPLE2D_UNAVAILABLE,
    SIMPLE2D_EXTRA_USE,
    SIMPLE2D_PREALLOCATED,
    SIMPLE2D_INNER,
    MULTIPLE_ALLOCATIONS;

    private static final String path = "pass/loopgetsimplifier/preallocator";

    @Override
    public String getResource() {
        return MatrixPreallocatorResource.path + "/" + name().toLowerCase(Locale.UK) + ".txt";
    }
}
