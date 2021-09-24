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

package org.specs.matisselib.tests.pass.branch.constant;

import java.util.Locale;

import pt.up.fe.specs.util.providers.ResourceProvider;

public enum ConstantBranchEliminationResource implements ResourceProvider {
    SIMPLE,
    RELEVANT,
    TRUE,
    FALSE,
    CONDITIONAL_CHANGE_TRUE,
    CONDITIONAL_CHANGE_FALSE,
    NESTED_BRANCH,
    NESTED_BRANCH2,
    LOOP_IN_BRANCH;

    private static final String path = "pass/branch/constant";

    @Override
    public String getResource() {
	return ConstantBranchEliminationResource.path + "/" + name().toLowerCase(Locale.UK) + ".txt";
    }

}
