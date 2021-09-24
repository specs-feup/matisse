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

package org.specs.matisselib.tests.pass.elementwise;

import java.util.Locale;

import pt.up.fe.specs.util.providers.ResourceProvider;

public enum ElementWiseResource implements ResourceProvider {
    PLUS_SCALAR,
    PLUS_SCALAR2,
    PLUS_INLINE_CONSTANT,
    PLUS_MATRIX,
    PLUS_MATRIX_SAME_NUMEL,
    UMINUS,
    COMBINED,
    DO_NOT_COMBINE_REDUNDANT,
    ACCEPT_COMBINE_REDUNDANT_DIRECTIVE;

    private static final String path = "pass/elementwise";

    @Override
    public String getResource() {
        return ElementWiseResource.path + "/" + name().toLowerCase(Locale.UK) + ".txt";
    }
}
