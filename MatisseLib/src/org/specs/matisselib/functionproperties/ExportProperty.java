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

package org.specs.matisselib.functionproperties;

import java.util.Optional;

public class ExportProperty extends FunctionProperty {
    private final String abiName;

    public ExportProperty(Optional<String> abiName) {
        this(abiName.orElse(null));
    }

    public ExportProperty(String abiName) {
        this.abiName = abiName;
    }

    public Optional<String> getAbiName() {
        return Optional.ofNullable(abiName);
    }

    @Override
    public String toString() {
        return "export" + (abiName == null ? "" : " " + abiName);
    }
}
