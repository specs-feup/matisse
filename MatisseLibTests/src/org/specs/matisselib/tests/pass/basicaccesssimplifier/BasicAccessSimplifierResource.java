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

package org.specs.matisselib.tests.pass.basicaccesssimplifier;

import java.util.Locale;

import pt.up.fe.specs.util.providers.ResourceProvider;

public enum BasicAccessSimplifierResource implements ResourceProvider {
    SIMPLE_TXT,
    INIF_TXT,
    MAT2D_M,
    MAT2D_TXT;

    private static final String path = "pass/basicaccesssimplifier";

    @Override
    public String getResource() {
	return BasicAccessSimplifierResource.path + "/" + name().toLowerCase(Locale.UK).replace('_', '.');
    }
}
