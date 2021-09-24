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

package org.specs.matlabtocl.v2.codegen;

import org.specs.matlabtocl.v2.ssa.ParallelRegionSettings;

public class GeneratedSetRange extends GeneratedCodeSegment {
    private final String end;
    private final String value;

    public GeneratedSetRange(ParallelRegionSettings parallelSettings, String end, String value) {
        super(parallelSettings, CLVersion.V1_2);

        this.end = end;
        this.value = value;
    }

    public String getEnd() {
        return end;
    }

    public String getValue() {
        return value;
    }
}
