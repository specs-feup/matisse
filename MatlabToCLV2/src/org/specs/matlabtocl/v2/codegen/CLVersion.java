/**
 * Copyright 2016 SPeCS.
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

package org.specs.matlabtocl.v2.codegen;

import java.util.List;

public enum CLVersion {
    V1_0(""),
    V1_1("-cl-std=CL1.1"),
    V1_2("-cl-std=CL1.2"),
    V2_0("-cl-std=CL2.0"),
    V2_1("-cl-std=CL2.1");

    private final String flag;

    CLVersion(String flag) {
        this.flag = flag;
    }

    public static CLVersion getMaximumVersion(List<CLVersion> versions) {
        return versions
                .stream()
                .reduce(V1_0, CLVersion::getMaximumVersion);
    }

    public static CLVersion getMaximumVersion(CLVersion v1, CLVersion v2) {
        return v1.ordinal() < v2.ordinal() ? v2 : v1;
    }

    public String getFlag() {
        return flag;
    }
}
