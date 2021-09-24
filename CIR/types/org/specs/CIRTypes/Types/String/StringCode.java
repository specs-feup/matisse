/**
 * Copyright 2013 SPeCS.
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

package org.specs.CIRTypes.Types.String;

import org.specs.CIR.Types.Views.Code.ACode;

public class StringCode extends ACode {

    private final StringType type;

    public StringCode(StringType type) {
        super(type);
        this.type = type;
    }

    /*
        @Override
        public String getType() {
    	return literalType;
        }
    */
    @Override
    public String getSimpleType() {
        StringBuilder builder = new StringBuilder();

        if (type.isConstant()) {
            builder.append("const ");
        }

        builder.append("char *");

        return builder.toString();
    }

}
